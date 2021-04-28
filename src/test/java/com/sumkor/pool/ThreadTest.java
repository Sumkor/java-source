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
        thread.start();
        thread.join();

        thread.stop();
        thread.resume();

        thread.start();
    }
}
