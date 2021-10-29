package com.sumkor.lock;

import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author Sumkor
 * @since 2021/4/23
 */
public class CyclicBarrierTest {

    static class TaskThread extends Thread {

        CyclicBarrier barrier;

        public TaskThread(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                System.out.println(getName() + " 到达栅栏 A");
                barrier.await();
                System.out.println(getName() + " 通过栅栏 A");

                Thread.sleep(2000);
                System.out.println(getName() + " 到达栅栏 B");
                barrier.await();
                System.out.println(getName() + " 通过栅栏 B");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 循环栅栏：等待所有线程都完成第一个任务后，才会进入下一个任务
     * CountDownLatch 是一次性的，CyclicBarrier 是可循环利用的
     */
    public static void main(String[] args) {
        int threadNum = 5;
        CyclicBarrier barrier = new CyclicBarrier(threadNum, new Runnable() {

            /**
             * 屏障任务
             * 所有线程都到达栅栏之后，其中一个线程需执行该任务，完成之后所有线程才可通过栅栏
             */
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " 完成最后任务");
            }
        });

        for(int i = 0; i < threadNum; i++) {
            new TaskThread(barrier).start();
        }
        /**
         * 执行结果：
         *
         * Thread-0 到达栅栏 A
         * Thread-2 到达栅栏 A
         * Thread-4 到达栅栏 A
         * Thread-3 到达栅栏 A
         * Thread-1 到达栅栏 A
         * Thread-1 完成最后任务
         * Thread-1 通过栅栏 A
         * Thread-0 通过栅栏 A
         * Thread-4 通过栅栏 A
         * Thread-3 通过栅栏 A
         * Thread-2 通过栅栏 A
         * Thread-0 到达栅栏 B
         * Thread-1 到达栅栏 B
         * Thread-4 到达栅栏 B
         * Thread-2 到达栅栏 B
         * Thread-3 到达栅栏 B
         * Thread-3 完成最后任务
         * Thread-3 通过栅栏 B
         * Thread-0 通过栅栏 B
         * Thread-4 通过栅栏 B
         * Thread-1 通过栅栏 B
         * Thread-2 通过栅栏 B
         *
         * Process finished with exit code 0
         */
    }

    @Test
    public void testException() {
        int i = testException(0);
        System.out.println("i = " + i);
        /**
         * java.lang.RuntimeException: 发生异常
         */
    }

    public int testException(int index) {
        boolean ranAction = false;
        if (index == 0) {
            try {
                ranAction = true;
                if (true) {
                    throw new RuntimeException("发生异常");
                }
                System.out.println("执行逻辑A");
                return 0;
            } finally {
                if (!ranAction) {
                    System.out.println("没有执行任务A");
                }
            }
        }

        System.out.println("执行逻辑B");
        return 1;
    }
}
