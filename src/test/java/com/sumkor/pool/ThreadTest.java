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

    @Test
    public void state() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("thread.isAlive() = " + thread.isAlive());  // false
        System.out.println("thread.getState() = " + thread.getState());// NEW

        thread.start();
        Thread.sleep(200);
        System.out.println("thread.isAlive() = " + thread.isAlive());  // true
        System.out.println("thread.getState() = " + thread.getState());// TIMED_WAITING

        thread.join();
        System.out.println("thread.isAlive() = " + thread.isAlive());  // false
        System.out.println("thread.getState() = " + thread.getState());// TERMINATED
    }

    @Test
    public void join() throws InterruptedException {
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(Thread.currentThread().getName() + " 开始运行...");
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + " 结束运行...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "threadB");
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(Thread.currentThread().getName() + " 开始运行...");
                    threadB.start();
                    threadB.join();
                    System.out.println(Thread.currentThread().getName() + " 结束运行...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "threadA");
        threadA.start();
        threadA.join();
    }

    @Test
    public void dealInterrupt() {
        Thread subThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (!Thread.currentThread().isInterrupted()) { // 循环检查中断状态
                    try {
                        System.out.println(Thread.currentThread().getName() + " 开始第【" + i + "】休眠...");
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName() + " 结束第【" + i + "】休眠...");
                        ++i;
                    } catch (InterruptedException e) {// 调用sleep受阻，中断状态true将被清除为false
                        System.out.println(Thread.currentThread().getName() + " " + e.getMessage());
                        // 只要正确地处理中断，也可以让循环停止。
                        // Thread.currentThread().interrupt();
                    }
                }
            }
        });
        subThread.start();

        // 主线程执行一段时间，中断子线程，再继续观察子线程一段时间
        try {
            Thread.sleep(1000);
            subThread.interrupt();
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " " + e.getMessage());
        }
        /**
         * 会出现两种结果：
         *
         * 结果一：
         *
         * Thread-0 开始第【0】休眠...
         * Thread-0 sleep interrupted
         * Thread-0 开始第【0】休眠...
         * Thread-0 结束第【0】休眠...
         * Thread-0 开始第【1】休眠...
         * Thread-0 结束第【1】休眠...
         * Thread-0 开始第【2】休眠...
         * Thread-0 结束第【2】休眠...
         * Thread-0 开始第【3】休眠...
         * Thread-0 结束第【3】休眠...
         * Thread-0 开始第【4】休眠...
         * Thread-0 结束第【4】休眠...
         * Thread-0 开始第【5】休眠...
         *
         * 结果二：
         *
         * Thread-0 开始第【0】休眠...
         * Thread-0 结束第【0】休眠...
         *
         */
    }

    @Test
    public void dealInterrupt02() {
        Thread subThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                boolean isLoop = true;
                while (isLoop) { // 循环检查中断状态
                    try {
                        System.out.println(Thread.currentThread().getName() + " 开始第【" + i + "】休眠...");
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName() + " 结束第【" + i + "】休眠...");
                        ++i;
                    } catch (InterruptedException e) { // 调用sleep受阻，中断状态true将被清除为false
                        System.out.println(Thread.currentThread().getName() + " " + e.getMessage());
                        isLoop = false;
                    }
                }
            }
        });
        subThread.start();

        // 主线程执行一段时间，中断子线程，再继续观察子线程一段时间
        try {
            Thread.sleep(1000);
            subThread.interrupt();
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " " + e.getMessage());
        }
    }
}
