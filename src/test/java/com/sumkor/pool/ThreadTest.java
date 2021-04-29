package com.sumkor.pool;

import org.junit.Test;

/**
 * @author Sumkor
 * @since 2021/4/28
 */
public class ThreadTest {

    @Test
    public void start() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("hello");
            }
        });
        System.out.println(thread.isAlive()); // false
        thread.start();
        System.out.println(thread.isAlive()); // true

        thread.join();

        thread.stop();
        thread.resume();

        // thread.start();
    }

    @Test
    public void sleep() throws InterruptedException {
        Thread.currentThread().sleep(100);
        Thread.sleep(100);
    }
}
