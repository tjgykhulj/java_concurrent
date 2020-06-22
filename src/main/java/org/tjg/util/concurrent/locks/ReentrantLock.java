package org.tjg.util.concurrent.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author Arnold Tang
 */
public class ReentrantLock implements Lock {

    private final NonfairSync sync;

    public ReentrantLock() {
        this(false);
    }

    public ReentrantLock(boolean fair) {
        sync = fair? new FairSync(): new NonfairSync();
    }

    @Override
    public void lock() {
        sync.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    public boolean isLocked() {
        return sync.isLocked();
    }


    private class NonfairSync extends AbstractQueuedSynchronizer {

        void lock() {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                acquire(1);
            }
        }

        boolean isLocked() {
            return getState() != 0;
        }

        Condition newCondition() {
            return new ConditionObject();
        }

        @Override
        protected boolean tryAcquire(int arg) {
            Thread thread = Thread.currentThread();
            int state = getState();
            if (state == 0) {
                if (compareAndSetState(0, arg)) {
                    setExclusiveOwnerThread(thread);
                    return true;
                }
            } else if (getExclusiveOwnerThread() == thread) {
                setState(state + arg);
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getExclusiveOwnerThread() != Thread.currentThread()) {
                throw new IllegalMonitorStateException("try to release a lock which not held by self");
            }
            int newState = getState() - arg;
            boolean free = false;
            if (newState == 0) {
                setExclusiveOwnerThread(null);
                free = true;
            }
            setState(newState);
            return free;
        }

        @Override
        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }
    }

    private class FairSync extends NonfairSync {

        @Override
        void lock() {
            acquire(1);
        }

        @Override
        protected boolean tryAcquire(int arg) {
            Thread thread = Thread.currentThread();
            int state = getState();
            if (state == 0) {
                if (!hasQueuedPredecessors() && compareAndSetState(0, arg)) {
                    setExclusiveOwnerThread(thread);
                    return true;
                }
            } else if (getExclusiveOwnerThread() == thread) {
                setState(state + arg);
                return true;
            }
            return false;
        }
    }
}
