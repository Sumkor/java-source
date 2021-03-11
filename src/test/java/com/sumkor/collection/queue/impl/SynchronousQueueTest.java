package com.sumkor.collection.queue.impl;

import org.junit.Test;

import java.util.concurrent.SynchronousQueue;

/**
 * @author Sumkor
 * @since 2021/3/5
 */
public class SynchronousQueueTest {

    /**
     * @see java.util.concurrent.SynchronousQueue
     *
     * public class SynchronousQueue<E> extends AbstractQueue<E>
     *     implements BlockingQueue<E>, java.io.Serializable
     *
     * 阻塞队列，并发安全，无锁算法
     *
     * SynchronousQueue 是一个不存储元素的阻塞队列。每一个 put 操作必须等待一个 take 操作，否则不能继续添加元素。
     * SynchronousQueue可以看成是一个传球手，负责把生产者线程处理的数据直接传递给消费者线程。队列本身并不存储任何元素，非常适合传递性场景。
     *
     * ynchronousQueue 支持公平性和非公平性 2 种策略来访问队列。默认是采用非公平性策略访问队列。
     * 公平性策略底层使用了类似队列的数据结构，而非公平策略底层使用了类似栈的数据结构。
     * @see SynchronousQueue#SynchronousQueue()
     *
     * SynchronousQueue 的吞吐量高于 LinkedBlockingQueue 和 ArrayBlockingQueue。
     * https://www.cnblogs.com/xxyyy/p/13045310.html
     */

    final int REQUEST    = 0; // 请求模式，消费者请求数据
    final int DATA       = 1; // 数据模式，生产者提供数据
    final int FULFILLING = 2; // 匹配模式，表示数据从正一个节点传递给另外的节点 // 10

    /**
     * 已知满足 (m & FULFILLING) != 0 则为 FULFILLING 模式，说明 FULFILLING|mode 得到的是 FULFILLING 模式
     */
    @Test
    public void mode() {
        /**
         * @see SynchronousQueue.TransferStack#transfer(java.lang.Object, boolean, long)
         */
        System.out.println(FULFILLING|REQUEST);   // 2 // 10 | 00 = 10
        System.out.println(FULFILLING|DATA);      // 3 // 10 | 01 = 11
        System.out.println(FULFILLING|FULFILLING);// 2 // 10 | 10 = 10

        System.out.println(isFulfilling(FULFILLING|REQUEST));   // true
        System.out.println(isFulfilling(FULFILLING|DATA));      // true
        System.out.println(isFulfilling(FULFILLING|FULFILLING));// true

        int i = 5;
        if (i > 0) {
            System.out.println("11111111111");
        } else if (i > 1) {
            System.out.println("22222222222");
        }
    }

    boolean isFulfilling(int m) {
        return (m & FULFILLING) != 0;
    }
}
