package org.tjg.util.concurrent;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ArrayBlockingQueue<T> extends AbstractQueue<T>
        implements BlockingQueue<T>, Serializable {

    private int putIndex;
    private int takeIndex;
    private int count;
    private final Object[] items;
    private final transient ReentrantLock lock;
    private final transient Condition notFull;
    private final transient Condition notEmpty;

    public ArrayBlockingQueue(int capacity, boolean fair) {
        this.items = new Object[capacity];
        lock = new ReentrantLock(fair);
        notFull = lock.newCondition();
        notEmpty = lock.newCondition();
        count = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iter();
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public void put(T t) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length) {
                notFull.await();
            }
            enqueue(t);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(T t) {
        lock.lock();
        try {
            if (count == items.length) {
                return false;
            }
            enqueue(t);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                if (nanos <= 0) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(t);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T poll() {
        lock.lock();
        try {
            return count == 0 ? null : dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T peek() {
        lock.lock();
        try {
            return itemAt(takeIndex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        lock.lock();
        try {
            return items.length - count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        lock.lock();
        try {
            int n = Integer.min(count, maxElements);
            for (int i=0; i<n; i++) {
                c.add(dequeue());
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    private void enqueue(T t) {
        items[putIndex] = t;
        if (++putIndex == items.length) {
            putIndex = 0;
        }
        count++;
        notEmpty.signal();
    }

    private T dequeue() {
        T x = itemAt(takeIndex);
        items[takeIndex] = null;
        if (++takeIndex == items.length) {
            takeIndex = 0;
        }
        count--;
        /* //TODO
        if (itrs != null)
            itrs.elementDequeued();
         */
        notFull.signal();
        return x;
    }

    @SuppressWarnings("unchecked")
    private T itemAt(int index) {
        return (T) items[index];
    }

    private class Iter implements Iterator<T> {
        private int nextIndex;
        private T nextItem;

        private Iter() {
            if (count == 0) {

            } else {
                nextItem = itemAt(nextIndex = takeIndex);
            }
        }

        @Override
        public boolean hasNext() {
            return nextItem == null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final T t = nextItem;
            if (++nextIndex == items.length) {
                nextIndex = 0;
            }
            nextItem = itemAt(nextIndex);
            return t;
        }
    }
}
