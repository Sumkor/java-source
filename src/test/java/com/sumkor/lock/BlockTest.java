package com.sumkor.lock;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * @author Sumkor
 * @since 2021/3/30
 */
public class BlockTest {

    /**
     * 用于测试：阻塞之前获得锁，测试进入阻塞状态的时候，是否会释放锁？
     */
    public <T> void doBlock(Object object, T input, Consumer<T> consumer) {
        assert object != null;
        System.out.println(Thread.currentThread().getName() + " 准备获取锁");
        synchronized (object) {
            try {
                System.out.println(Thread.currentThread().getName() + " 已获得锁，准备进入阻塞");
                consumer.accept(input); // 阻塞方法
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println(Thread.currentThread().getName() + " 结束阻塞，准备释放锁");
            }
        }
    }

    /**
     * Thread.sleep 不会释放锁
     *
     * The thread does not lose ownership of any monitors.
     * @see Thread#sleep(long)
     */
    @Test
    public void sleepTest() throws InterruptedException {
        int num = 5;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        Object object = new Object();
        for (int i = 0; i < num; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doBlock(object, 1000, t -> {
                        try {
                            Thread.sleep(t);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    countDownLatch.countDown();
                }
            }, "Thread_" + i);
            thread.start();
        }
        countDownLatch.await();
        /**
         * Thread.sleep 操作不会释放锁，需要等获得锁的线程从休眠中醒过来后，主动释放锁，其他线程才能获取锁。
         * 执行结果如下：
         *
         * Thread_3 准备获取锁
         * Thread_3 已获得锁，准备进入阻塞
         * Thread_2 准备获取锁
         * Thread_4 准备获取锁
         * Thread_0 准备获取锁
         * Thread_1 准备获取锁
         * Thread_3 结束阻塞，准备释放锁   <- 这里释放锁之后，其他线程才能获取锁
         * Thread_1 已获得锁，准备进入阻塞
         * Thread_1 结束阻塞，准备释放锁
         * Thread_0 已获得锁，准备进入阻塞
         * Thread_0 结束阻塞，准备释放锁
         * Thread_4 已获得锁，准备进入阻塞
         * Thread_4 结束阻塞，准备释放锁
         * Thread_2 已获得锁，准备进入阻塞
         * Thread_2 结束阻塞，准备释放锁
         */
    }

    /**
     * Object.wait 会释放锁，被唤醒的时候会重新获得锁
     * 调用 object 对象的 wait 方法，当前线程必须获取该对象的监视器
     */
    @Test
    public void waitTest() throws InterruptedException {
        int num = 5;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        Object object = new Object();
        for (int i = 0; i < num; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doBlock(object, 1000, t -> {
                        try {
                            object.wait(t);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    countDownLatch.countDown();
                }
            }, "Thread_" + i);
            thread.start();
        }
        countDownLatch.await();
        /**
         * Object.wait 操作会释放锁。只要获取锁的线程调用了 Object.wait，其他线程就可以获得锁。
         * 执行结果：
         *
         * Thread_0 准备获取锁
         * Thread_0 已获得锁，准备进入阻塞  <- 这里进入阻塞之后，其他线程就能获取锁
         * Thread_4 准备获取锁
         * Thread_4 已获得锁，准备进入阻塞
         * Thread_2 准备获取锁
         * Thread_2 已获得锁，准备进入阻塞
         * Thread_3 准备获取锁
         * Thread_3 已获得锁，准备进入阻塞
         * Thread_1 准备获取锁
         * Thread_1 已获得锁，准备进入阻塞
         * Thread_2 结束阻塞，准备释放锁
         * Thread_4 结束阻塞，准备释放锁
         * Thread_0 结束阻塞，准备释放锁
         * Thread_3 结束阻塞，准备释放锁
         * Thread_1 结束阻塞，准备释放锁
         */
    }

    /**
     * LockSupport.park 不会释放锁
     * LockSupport.park 就是将线程转入 WAITING 状态
     */
    @Test
    public void lockSupportTest() throws InterruptedException {
        int num = 5;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        Object object = new Object();
        for (int i = 0; i < num; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doBlock(object, 1000, t -> {
                        try {
                            LockSupport.parkNanos(t);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    countDownLatch.countDown();
                }
            }, "Thread_" + i);
            thread.start();
        }
        countDownLatch.await();
        /**
         * LockSupport.park 不会释放锁，需要等获得锁的线程超时之后，主动释放锁，其他线程才能获取锁。
         * 执行结果：
         *
         * Thread_2 准备获取锁
         * Thread_2 已获得锁，准备进入阻塞
         * Thread_3 准备获取锁
         * Thread_4 准备获取锁
         * Thread_0 准备获取锁
         * Thread_2 结束阻塞，准备释放锁   <- 这里释放锁之后，其他线程才能获取锁
         * Thread_1 准备获取锁
         * Thread_0 已获得锁，准备进入阻塞
         * Thread_0 结束阻塞，准备释放锁
         * Thread_4 已获得锁，准备进入阻塞
         * Thread_4 结束阻塞，准备释放锁
         * Thread_3 已获得锁，准备进入阻塞
         * Thread_3 结束阻塞，准备释放锁
         * Thread_1 已获得锁，准备进入阻塞
         * Thread_1 结束阻塞，准备释放锁
         */
    }

    /**
     * Condition.await 会释放锁
     * 必须使用 ReentrantLock 来加锁之后，才可以用该 ReentrantLock 的 Condition.wait 来阻塞
     */
    @Test
    public void conditionTest() throws InterruptedException {
        int num = 5;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        for (int i = 0; i < num; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " 准备获取锁");
                    reentrantLock.lock();
                    try {
                        System.out.println(Thread.currentThread().getName() + " 已获得锁，准备进入阻塞");
                        condition.awaitNanos(1000); // 阻塞方法
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println(Thread.currentThread().getName() + " 结束阻塞，准备释放锁");
                        reentrantLock.unlock();
                    }
                    countDownLatch.countDown();
                }
            }, "Thread_" + i);
            thread.start();
        }
        countDownLatch.await();
        /**
         * Thread_1 准备获取锁
         * Thread_1 已获得锁，准备进入阻塞
         * Thread_1 结束阻塞，准备释放锁
         * Thread_2 准备获取锁
         * Thread_2 已获得锁，准备进入阻塞  <- 这里进入阻塞之后，其他线程就能获取锁
         * Thread_3 准备获取锁
         * Thread_3 已获得锁，准备进入阻塞
         * Thread_2 结束阻塞，准备释放锁
         * Thread_3 结束阻塞，准备释放锁
         * Thread_0 准备获取锁
         * Thread_0 已获得锁，准备进入阻塞
         * Thread_4 准备获取锁
         * Thread_4 已获得锁，准备进入阻塞
         * Thread_0 结束阻塞，准备释放锁
         * Thread_4 结束阻塞，准备释放锁
         */
    }

}
