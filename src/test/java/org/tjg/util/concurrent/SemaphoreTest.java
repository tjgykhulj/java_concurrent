package org.tjg.util.concurrent;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SemaphoreTest {

    private long getTime() {
        long time = System.currentTimeMillis()/1000;
        return time % 1000;
    }

    @Test
    public void test() throws InterruptedException {
        Semaphore s = new Semaphore(10, true);
        Thread[] t = new Thread[10];
        for (int i=0; i<10; i++) {
            final int permits = i+1;
            t[i] = new Thread(() -> {
                try {
                    s.acquire(permits);
                    System.out.printf("%d thread %d acquire\n", getTime(), permits);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    s.release(permits);
                    System.out.printf("%d thread %d release\n", getTime(), permits);
                }
            });
            t[i].start();
        }
        for (int i=0; i<10; i++) t[i].join();
    }


    @Test
    public void testOk() {
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (int i=0; i<3; i++) {
            map.put(i, Arrays.asList(i, i*2, i*3, i*4));
        }
        System.out.println(map.values().stream().map(Collection::size).collect(Collectors.toSet()).size());
    }
}
