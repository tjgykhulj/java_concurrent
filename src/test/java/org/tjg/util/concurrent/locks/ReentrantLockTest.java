package org.tjg.util.concurrent.locks;

import org.junit.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ReentrantLockTest {


    // 非公平锁下，应该每次有大几率是线程1快速走完循环，线程2只能默默排队等锁
    @Test
    public void testNonfairCase() {
        NThreadsAcquireOneLock(2, new ReentrantLock(false));
    }


    // 公平锁下，应该3个线程轮流持锁
    @Test
    public void testFairCase() {
        NThreadsAcquireOneLock(3, new ReentrantLock(true));
    }

    private void NThreadsAcquireOneLock(int n, ReentrantLock lock)  {
        Thread[] threads = new Thread[n];
        for (int i=0; i<n; i++) {
            threads[i] = new Thread(() -> task(lock));
            threads[i].start();
        }
        for (int i=0; i<n; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void task(ReentrantLock lock) {
        final long id = Thread.currentThread().getId();
        for (int i=0; i<10; i++) {
            lock.lock();
            try {
                System.out.printf("thread %d: %d\n", id, i);
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
}
