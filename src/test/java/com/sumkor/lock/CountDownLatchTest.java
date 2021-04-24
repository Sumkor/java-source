package com.sumkor.lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author Sumkor
 * @since 2021/4/23
 */
public class CountDownLatchTest {

    private static void doSomeTask() {
        System.out.println("Hello World");
    }

    private static void onCompletion() {
        System.out.println("All tasks finished");
    }

    /**
     * 闭锁相当于一扇门，
     * 在闭锁到达结束状态之前，这扇门一直是关闭着的，任何线程都不可以通过，
     * 当闭锁到达结束状态时，这扇门才会打开并容许所有线程通过。
     * 它可以使一个或多个线程等待一组事件发生。
     */
    public static void main(String[] args) throws InterruptedException {
        final int nThreads = 10;
        final CountDownLatch startGate = new CountDownLatch(1);     // 启动门使得主线程能同时释放所有的工作线程
        final CountDownLatch endGate = new CountDownLatch(nThreads);// 结束门使得主线程能等待最后一个线程执行完成

        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        startGate.await();
                        try {
                            doSomeTask();
                        } finally {
                            endGate.countDown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
            };
            t.start();
        }

        startGate.countDown();
        endGate.await();
        onCompletion();
    }

    /**
     * 倒计时过程中，其中一个线程出错没有 countDown
     * 会导致 await 一直阻塞
     */
    @Test
    public void countDownWithException() throws InterruptedException {
        final int nThreads = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (index == 8) {
                        throw new RuntimeException("出现异常");
                    }
                    try {
                        doSomeTask();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            }).start();
        }
        countDownLatch.await();
    }

}
