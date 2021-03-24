package com.sumkor.collection.queue.impl;

import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Sumkor
 * @since 2021/3/5
 */
public class ConcurrentLinkedQueueTest {

    /**
     * @see java.util.concurrent.ConcurrentLinkedQueue
     *
     * public class ConcurrentLinkedQueue<E> extends AbstractQueue<E>
     *         implements Queue<E>, java.io.Serializable
     *
     * 非阻塞队列，并发安全，无锁算法
     */

    /**
     * IDEA debug ConcurrentLinkedQueue 的时候踩的坑
     * https://blog.csdn.net/AUBREY_CR7/article/details/106331490
     */
    @Test
    public void test() {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");
        queue.offer("d");

        queue.toString();
    }
}
