package org.tjg.util.concurrent.locks.demo;

import org.tjg.util.concurrent.locks.ReentrantLock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class AbcThread extends Thread {

    private final int index;
    private final Lock lock;
    private final Condition cond;

    private static volatile int wrongCnt = 0;
    private static volatile int cnt = 0;
    private static final char[] CHARS = {'A','B','C'};
    private static final int SIZE = 1000;

    public AbcThread(int index, Lock lock, Condition cond) {
        this.index = index;
        this.lock = lock;
        this.cond = cond;
    }

    @Override
    public void run() {
        for (int i = 0; i < SIZE; i++) {
            lock.lock();
            try {
                if (AbcThread.cnt % 3 != index) {
                    wrongCnt++;
                    cond.await();
                }
                System.out.print(CHARS[index]);
                System.out.flush();
                AbcThread.cnt++;
                cond.signalAll();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        System.out.println(wrongCnt);
    }

    public static void main(String args[]) {
        Lock lock = new ReentrantLock(true);
        Condition condition = lock.newCondition();
        for (int i=0; i<3; i++) {
            new AbcThread(i, lock, condition).start();
        }
    }
}