package com.sumkor.pool;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * @author Sumkor
 * @since 2021/4/26
 */
public class FutureTest {

    /**
     * 在当前线程获取 Callable 任务的执行结果，会进入阻塞直到完成
     */
    @Test
    public void call() {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                return "This is a result";
            }
        };
        try {
            String result = callable.call();
            System.out.println("Callable 执行的结果是: " + result);
        } catch (Exception e) {
            System.out.println("There is a exception.");
        }
    }

    /**
     * 异步执行 Callable 任务，使用 Future 来获取任务的执行结果
     */
    @Test
    public void future() throws InterruptedException {
        // 定义任务
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                if (true) {
                    throw new RuntimeException("oh!");
                }
                return "This is a result";
            }
        };
        // 异步执行任务
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<String> future = executorService.submit(callable);

        // 模拟处理其他耗时操作
        Thread.sleep(1000);

        // 获取任务异步执行结果
        try {
            String result = future.get();
            System.out.println("Callable 执行的结果是: " + result);
        } catch (ExecutionException e) {
            System.out.println("There is a exception.");
        }

        System.out.println("future.isDone() = " + future.isDone());
        System.out.println("future.isCancelled() = " + future.isCancelled());
    }

    /**
     * Executors#callable 将 Runnable 转换为 Callable
     */
    @Test
    public void executors() throws Exception {
        Callable<String> callable = Executors.callable(new Runnable() {
            @Override
            public void run() {
                System.out.println("run!");
            }
        }, "haha");
        String call = callable.call();
        System.out.println("call = " + call); // haha
    }

    /**
     * 三个线程依次执行：提交任务、等待任务、取消任务
     * 在任务未执行完的时候，取消任务。
     *
     * @author Sumkor
     * @since 2021/4/28
     */
    @Test
    public void cancel() throws InterruptedException {
        // 定义任务
        FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(10000);
                return "哦豁";
            }
        });

        CountDownLatch submitGate = new CountDownLatch(1); // 等待任务提交
        CountDownLatch endGate = new CountDownLatch(3);    // 等待线程执行完

        // 提交任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    submitGate.countDown();

                    System.out.println(Thread.currentThread().getName() + " 执行任务开始");
                    futureTask.run();
                    System.out.println(Thread.currentThread().getName() + " 执行任务结束");
                } finally {
                    endGate.countDown();
                }
            }
        }).start();

        // 等待任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    submitGate.await();
                    Thread.sleep(1000);// 等待 futureTask.run() 执行一段时间后再获取结果

                    System.out.println(Thread.currentThread().getName() + " 获取任务结果开始");
                    String result = futureTask.get();
                    System.out.println(Thread.currentThread().getName() + " 获取任务结果结束 " + result);
                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName() + " 获取任务结果失败 " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    endGate.countDown();
                }
            }
        }).start();

        // 取消任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    submitGate.await();
                    Thread.sleep(2000);// 等待 futureTask.get() 执行一段时间后再取消任务

                    System.out.println(Thread.currentThread().getName() + " 取消任务开始");
                    boolean cancel = futureTask.cancel(true);
                    System.out.println(Thread.currentThread().getName() + " 取消任务结束 " + cancel);
                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName() + " 取消任务失败 " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    endGate.countDown();
                }
            }
        }).start();

        endGate.await();
        /**
         * 执行结果：
         *
         * Thread-0 执行任务开始
         * Thread-1 获取任务结果开始
         * Thread-2 取消任务开始
         * Thread-0 执行任务结束
         * Thread-1 获取任务结果失败 null
         * Thread-2 取消任务结束 true
         * java.util.concurrent.CancellationException
         * 	at java.util.concurrent.FutureTask.report(FutureTask.java:121)
         * 	at java.util.concurrent.FutureTask.get(FutureTask.java:192)
         * 	at com.sumkor.pool.FutureTest$6.run(FutureTest.java:127)
         * 	at java.lang.Thread.run(Thread.java:745)
         *
         * 说明：
         * 线程 A 启动任务一段时间后，线程 B 来获取任务结果，进入等待。
         * 随后线程 C 取消任务，将线程 A 中断（线程 A 不会抛异常，因为 FutureTask#cancel 先一步修改了 state 导致 FutureTask#setException 中 CAS 失败）。
         * 此时线程 B 在等待中被唤醒（由线程 C 唤醒，检查到 state 为 INTERRUPTED）并抛出异常 CancellationException。
         */
    }
}
