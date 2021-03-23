package com.sumkor.collection.queue.impl;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TransferQueue;

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
     * SynchronousQueue 支持公平性和非公平性 2 种策略来访问队列。默认是采用非公平性策略访问队列。
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

    /**
     * 研究 TransferQueue#advanceHead
     */
    @Test
    public void advanceHead() {
        TransferQueue queue = new TransferQueue();
        System.out.println("queue.head = " + queue.head);
        // 头节点
        TransferQueue.QNode head = queue.getHead();
        System.out.println("head = " + head);
        // 新节点
        TransferQueue.QNode newNode = new TransferQueue.QNode(null, false);
        System.out.println("newNode = " + newNode);
        System.out.println();
        // 测试
        queue.advanceHead(head, newNode);
        System.out.println("queue.head = " + queue.head);
        System.out.println("head = " + head);
        System.out.println("newNode = " + newNode);
        /**
         * 执行结果：
         *
         * queue.head = com.sumkor.collection.queue.impl.SynchronousQueueTest$TransferQueue$QNode@593634ad
         *       head = com.sumkor.collection.queue.impl.SynchronousQueueTest$TransferQueue$QNode@593634ad
         *    newNode = com.sumkor.collection.queue.impl.SynchronousQueueTest$TransferQueue$QNode@20fa23c1
         *
         * queue.head = com.sumkor.collection.queue.impl.SynchronousQueueTest$TransferQueue$QNode@20fa23c1
         *       head = com.sumkor.collection.queue.impl.SynchronousQueueTest$TransferQueue$QNode@593634ad
         *    newNode = com.sumkor.collection.queue.impl.SynchronousQueueTest$TransferQueue$QNode@20fa23c1
         *
         * 可知，UNSAFE.compareAndSwapObject(this, headOffset, h, nh) 操作只是 CAS 改变 TransferQueue#head 属性的值
         */
    }

    static final class TransferQueue {

        static final class QNode {
            volatile QNode next;          // next node in queue // 队列的下一个节点
            volatile Object item;         // CAS'ed to or from null // 数据元素
            volatile Thread waiter;       // to control park/unpark // 等待着的线程
            final boolean isData; // true表示为DATA类型，false表示为REQUEST类型

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }
        }

        /** Head of queue */
        transient volatile QNode head; // 头节点
        /** Tail of queue */
        transient volatile QNode tail; // 尾节点
        /**
         * Reference to a cancelled node that might not yet have been
         * unlinked from queue because it was the last inserted node
         * when it was cancelled.
         */
        transient volatile QNode cleanMe;

        void advanceHead(QNode h, QNode nh) {
            if (h == head &&
                    UNSAFE.compareAndSwapObject(this, headOffset, h, nh))
                h.next = h; // forget old next
        }

        public QNode getHead() {
            return head;
        }

        TransferQueue() {
            QNode h = new QNode(null, false); // initialize to dummy node.
            head = h;
            tail = h;
        }

        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        private static final long tailOffset;
        private static final long cleanMeOffset;
        static {
            try {
                // UNSAFE = sun.misc.Unsafe.getUnsafe();
                // 获取 Unsafe 内部的私有的实例化单例对象
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                // 无视权限
                field.setAccessible(true);
                UNSAFE = (Unsafe) field.get(null);

                Class<?> k = SynchronousQueueTest.TransferQueue.class;
                headOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("head"));
                tailOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("tail"));
                cleanMeOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("cleanMe"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    // --------------------------------

    @Test
    public void test() throws InterruptedException {
        SynchronousQueue queue = new SynchronousQueue();
        queue.remove();
    }
}
