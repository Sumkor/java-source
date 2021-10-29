package com.sumkor.threadlocal;

import org.junit.Test;

/**
 * @author Sumkor
 * @since 2021/5/6
 */
public class ThreadPoolExecutorTest {

    private final class Worker implements Runnable {

        final Thread thread; // 工作线程

        public Worker() {
            thread = new Thread(this);
        }

        @Override
        public void run() {
            System.out.println("run worker");
        }
    }

    /**
     * addWorker 中如何触发 runWorker
     */
    @Test
    public void addWorker() throws InterruptedException {
        Worker worker = new Worker();
        worker.thread.start();
        worker.thread.join();
    }
}
