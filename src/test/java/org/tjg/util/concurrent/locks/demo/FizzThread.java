package org.tjg.util.concurrent.locks.demo;

import org.tjg.util.concurrent.locks.ReentrantLock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class FizzThread extends Thread {

    static Lock lock = new ReentrantLock(true);
    static Condition c = lock.newCondition();

    private final int index;
    private final int size;
    private int offset;

    private static volatile int cnt;

    private FizzThread(int index, int size) {
        this.index = index;
        this.size = size;
    }

    @Override
    public void run() {
        for (int i=0; i<size; i++) {
            lock.lock();
            try {
                while (cnt / 4 < i) {
                    c.await();
                }
                System.out.println(index + " " + i);
                System.out.flush();
                if (++cnt % 4 == 0) {
                    c.signalAll();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[4];
        for (int i=0; i<4; i++) {
            threads[i] = new FizzThread(i, 100);
            threads[i].start();
        }
        for (int i=0; i<4; i++) threads[i].join();
    }
}
