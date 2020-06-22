package org.tjg.util.concurrent;

import org.junit.Test;
import org.tjg.util.concurrent.ArrayBlockingQueue;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class ArrayBlockingQueueTest {

    private BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3, true);
    private Random rand = new Random();

    public void putTask() {
        Thread t = Thread.currentThread();
        try {
            while (!t.isInterrupted()) {
                System.out.printf("[producer %d] try to put one\n", t.getId());
                queue.put(rand.nextInt());
                System.out.printf("[producer %d] put one\n", t.getId());
                Thread.sleep(100 + rand.nextInt(1500));
            }
        } catch (InterruptedException ignore) {
        }
        System.out.printf("put task %d end\n", t.getId());
    }

    @Test
    public void test() {
        int n = 10;
        Thread[] producers = new Thread[n];
        for (int i=0; i<n; i++) {
            producers[i] = new Thread(this::putTask);
            producers[i].start();
        }
        for (int i=0; i<100; i++) {
            try {
                System.out.printf("[consumer] get %d: %d\n", i, queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i=0; i<n; i++) producers[i].interrupt();
    }
}
