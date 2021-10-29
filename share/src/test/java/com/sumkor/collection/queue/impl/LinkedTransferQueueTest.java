package com.sumkor.collection.queue.impl;

import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;

/**
 * @author Sumkor
 * @since 2021/3/16
 */
public class LinkedTransferQueueTest {

    /**
     * -verbose:gc -Xms40M -Xmx40M -Xmn10M -XX:SurvivorRatio=8
     * 堆大小为40m，其中新生代大小为10m，按照1:8比例分配，Eden区大小设置为8m
     * 此时存入8m大小的变量，直接存入老年代（tenured generation）
     * <p>
     * 验证：
     * 对于链表
     * nodeA ← nodeB ← nodeC
     * <p>
     * 如果把 nodeB 的 item 域自引用，把 nodeC 指向 nodeA，就算没有断开 nodeB 到 nodeA 的引用，nodeB 也会被垃圾回收调。
     * nodeB
     * ↓
     * nodeA ← nodeC
     */
    @Test
    public void testGC() throws InterruptedException {
        // A ← B ← C
        MyNode<Object> nodeA = new MyNode<>("A", get8M(), null);
        MyNode<Object> nodeB = new MyNode<>("B", get8M(), nodeA);
        MyNode<Object> nodeC = new MyNode<>("C", get8M(), nodeB);

        nodeC.setNext(nodeA);

        nodeB.setItem(nodeB);// nodeB的item域自引用，目的是把item域原先指向的8m数据改为不可达
        nodeB = null;

        System.gc();

        MyNode<Object> nodeD = new MyNode<>("D", get8M(), nodeC);
        Thread.currentThread().join();
    }

    private byte[] get8M() {
        return new byte[1024 * 1024 * 8];
    }

    static class MyNode<E> {
        volatile String name;
        volatile E item;
        volatile MyNode<E> next;

        public MyNode(String name, E item, MyNode<E> next) {
            this.name = name;
            this.item = item;
            this.next = next;
        }

        public E getItem() {
            return item;
        }

        public void setItem(E item) {
            this.item = item;
        }

        public MyNode<E> getNext() {
            return next;
        }

        public void setNext(MyNode<E> next) {
            this.next = next;
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println(name + "要回收了");
            super.finalize();
        }
    }

    /**
     * Bug:LinkedTransferQueue的数据暂失和CPU爆满以及修复
     * http://ifeve.com/buglinkedtransferqueue-bug/
     * https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8043743
     */
    public static void main(String[] args) throws InterruptedException {
        final BlockingQueue<Long> queue = new LinkedTransferQueue<Long>();

        CountDownLatch countDownLatch01 = new CountDownLatch(1);
        CountDownLatch countDownLatch02 = new CountDownLatch(1);

        Runnable takeTask = new Runnable() {
            public void run() {
                countDownLatch01.countDown();
                System.out.println("takeTask--------------------" + Thread.currentThread().getId());
                try {
                    System.out.println("takeTask--------------------" + Thread.currentThread().getId() + " " + queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Runnable takeTaskInterrupted = new Runnable() {
            public void run() {
                try {
                    countDownLatch01.await();
                    countDownLatch02.countDown();
                    System.out.println("takeTaskInterrupted---------" + Thread.currentThread().getId());
                    Thread.currentThread().interrupt();
                    System.out.println("takeTaskInterrupted---------" + Thread.currentThread().getId() + " " + queue.take());
                } catch (InterruptedException e) {
                    System.out.println("takeTaskInterrupted---------" + Thread.currentThread().getId() + " " + e);
                }
            }
        };
        Runnable offerTask = new Runnable() {
            public void run() {
                try {
                    countDownLatch02.await();
                    System.out.println("offerTask-------------------" + Thread.currentThread().getId());
                    queue.offer(8L);
                    System.out.println("offerTask-------------------" + Thread.currentThread().getId() + " offerTask thread has come out!");
                } catch (InterruptedException e) {
                    System.out.println("offerTask-------------------" + Thread.currentThread().getId() + " " + e);
                }
            }
        };

        Thread takeThread = new Thread(takeTask); // untimed call to take
        takeThread.start();

        Thread takeInterruptedThead = new Thread(takeTaskInterrupted); // untimed call to take with interrupted status
        takeInterruptedThead.start();

        Thread offerThread = new Thread(offerTask);
        offerThread.start();

//        new Thread(takeTask).start();// first untimed call to take
//        new Thread(takeTaskInterrupted).start();// second untimed call to take with interrupted status
//        new Thread(offerTask).start();//  a call to offer

    }

    /**
     * Thread.yield() 方法，使当前线程由执行状态，变成为就绪状态，让出 CPU 时间，再跟其他线程一起争夺 CPU 时间片
     */
    @Test
    public void yieldInLoop() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 1; i <= 10; i++) {
                        System.out.println("" + Thread.currentThread().getName() + "-----" + i);
                        // 当i为5时，该线程就让出CPU时间片，再跟其他线程一起争夺CPU时间片（也就是谁先抢到谁执行）
                        // 1. 如果当前线程抢到了，则继续执行
                        // 2. 如果当前线程没有抢到，则等待CPU的下一次调度（其他线程执行完毕或让出CPU时间片）
                        if (i == 5) {
                            Thread.yield();
                        }
                    }
                    countDownLatch.countDown();
                }
            }, "thread_" + i);
            thread.start();
        }
        countDownLatch.await();
    }

}
