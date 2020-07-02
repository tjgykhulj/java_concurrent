package org.tjg.util.concurrent;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author Arnold Tang
 */
public class Semaphore implements Serializable {
    private static final long serialVersionUID = -6382799327348059974L;

    private final Sync sync;

    public Semaphore(int permits) {
        this(permits, false);
    }

    public Semaphore(int permits, boolean fair) {
        sync = new Sync(permits, fair);
    }

    // ----------------------------- acquire -----------------------------

    public void acquire(int permits) throws InterruptedException {
        if (permits < 0) {
            throw new IllegalArgumentException();
        }
        sync.acquireSharedInterruptibly(permits);
    }

    public void acquireUninterruptibly() {
        sync.acquireShared(1);
    }

    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    public boolean tryAcquire(int permits) {
        if (permits < 0) {
            throw new IllegalArgumentException();
        }
        return sync.tryAcquireShared(permits, false) >= 0;
    }

    public boolean tryAcquire(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    // ----------------------------- release -----------------------------

    public void release(int permits) {
        if (permits < 0) {
            throw new IllegalArgumentException();
        }
        sync.releaseShared(permits);
    }

    public void release() {
        release(1);
    }
    public boolean tryRelease() {
        return tryRelease(1);
    }

    public boolean tryRelease(int permits) {
        if (permits < 0) {
            throw new IllegalArgumentException();
        }
        return sync.tryReleaseShared(permits);
    }

    // ----------------------------- other -----------------------------

    public int availablePermits() {
        return sync.availablePermits();
    }

    // ----------------------------- sync -----------------------------

    class Sync extends AbstractQueuedSynchronizer {

        private boolean fair;

        Sync(int permits, boolean fair) {
            this.fair = fair;
            setState(permits);
        }

        int availablePermits() {
            return getState();
        }

        private int tryAcquireShared(int acquires, boolean fair) {
            while (true) {
                if (fair && hasQueuedPredecessors()) {
                    return -1;
                }
                int state = getState();
                int newState = state - acquires;
                if (newState < 0 || compareAndSetState(state, newState)) {
                    return newState;
                }
            }
        }

        @Override
        protected final boolean tryReleaseShared(int releases) {
            while (true) {
                int state = getState();
                int next = state + releases;
                if (next < state) {
                    throw new Error("Maximum permit count exceeded");
                }
                if (compareAndSetState(state, next)) {
                    return true;
                }
            }
        }

        @Override
        protected int tryAcquireShared(int acquires) {
            return tryAcquireShared(acquires, this.fair);
        }
    }
}
