package org.tjg.util.concurrent.locks.demo;

import java.util.concurrent.Semaphore;

public class SemaphoreThread extends Thread {

    private static int SIZE = 1000;
    private static final char[] CHARS = {'A','B','C'};

    private final int index;
    private final Semaphore current;
    private final Semaphore next;

    public SemaphoreThread(int index, Semaphore current, Semaphore next) {
        this.index = index;
        this.current = current;
        this.next = next;
    }

    public static void main(String[] args) {
        Semaphore[] semaphores = {
            new Semaphore(1), new Semaphore(0), new Semaphore(0)
        };
        for (int i=0; i<3; i++) {
            new SemaphoreThread(i, semaphores[i], semaphores[(i + 1) % 3]).start();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < SIZE; i++) {
            try {
                current.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print(CHARS[index]);
            next.release();
        }
    }
}
