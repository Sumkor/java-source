package com.sumkor.map;

import org.junit.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * @author Sumkor
 * @see java.util.WeakHashMap
 * @since 2021/3/8
 */
public class WeakHashMapTest {

    /**
     * 弱引用：当垃圾收集器开始工作，无论当前内存是否足够，都会回收掉只被弱引用关联的对象
     * <p>
     * -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:SurvivorRatio=8
     * 堆大小为20m，其中新生代大小为10m，按照1:8比例分配，Eden区大小设置为8m
     * 此时存入8m大小的变量，直接存入老年代（tenured generation）
     * <p>
     * 执行结果：
     * weakReference.get() = [B@f2a0b8e
     * weakReference.get() = [B@f2a0b8e
     * [GC (System.gc())  15209K->9574K(19456K), 0.0209182 secs]
     * [Full GC (System.gc())  9574K->1323K(19456K), 0.0239549 secs]
     * weakReference.get() = null
     */
    @Test
    public void weakReference() {
        byte[] allocation01 = new byte[1024 * 1024 * 8];
        WeakReference<byte[]> weakReference = new WeakReference<byte[]>(allocation01);

        System.out.println("weakReference.get() = " + weakReference.get());// [B@154ebadd
        allocation01 = null;
        System.out.println("weakReference.get() = " + weakReference.get());// [B@154ebadd

        System.gc();
        System.out.println("weakReference.get() = " + weakReference.get());// null
    }

    /**
     * @see java.lang.ref.Reference
     * <p>
     * Reference 类把内存分为 4 种状态：
     * Active：初始化，内存刚被分配。
     * Pending：对象被回收，放入引用队列之前。
     * Enqueued：对象被回收，放入引用队列之后。
     * Inactive：对象被回收，引用队列中已移除。不能再变为其它状态。
     * <p>
     * Reference 类是通过其中的 queue 属性和 next 属性来记录该状态。
     * Active：queue = ReferenceQueue实例 或 ReferenceQueue.NULL; next = null
     * Pending：queue = ReferenceQueue实例; next = this
     * Enqueued：queue = ReferenceQueue.ENQUEUED; next = 队列的下一个节点
     * Inactive：queue = ReferenceQueue.NULL; next = this.
     * <p>
     * @see java.lang.ref.ReferenceQueue
     * 引用队列，当检测到对象的可到达性更改时，垃圾回收器将已注册的引用对象添加到队列中，
     * ReferenceQueue 实现了入队（enqueue）和出队（poll），还有 remove 操作，内部元素 head 就是泛型的 Reference。
     * <p>
     * https://blog.csdn.net/gdutxiaoxu/article/details/80738581
     */
    @Test
    public void referenceQueue() throws InterruptedException {
        // 创建一个引用队列
        ReferenceQueue referenceQueue = new ReferenceQueue();

        /**
         * 创建弱引用，此时 Reference 状态为 Active，
         * 其中
         * {@link Reference#pending} 属性为空，
         * {@link Reference#queue} 属性为自定义的引用队列，并且 next 为 null
         */
        WeakReference weakReference = new WeakReference(new Object(), referenceQueue);
        System.out.println(weakReference);// java.lang.ref.WeakReference@f2a0b8e

        /**
         * 当 GC 执行后，由于是弱引用，回收该 object 对象，并且置于 Reference#pending 属性上，同时 Reference#referent 属性置为 null（即 weakReference#get 将得到 null）。
         * 此时 Reference 的状态为 Pending。
         * ReferenceHandler 定时（守护线程，while true循环）从 Reference#pending 属性中取下该元素，并且将该元素放入到队列中（头插法），
         * 此时 Reference 状态为 Enqueued，
         * Reference#queue 为 {@link ReferenceQueue#ENQUEUED}，即解开 Reference 对象与 ReferenceQueue 队列之间的关联。
         *
         * @see Reference#tryHandlePending(boolean)
         * @see ReferenceQueue#enqueue(java.lang.ref.Reference)
         */
        System.gc();

        /**
         * 当从队列里面取出该元素，Reference 状态为 Inactive，
         * Reference.queue = Reference.NULL
         *
         * @see ReferenceQueue#remove(long)
         * @see ReferenceQueue#reallyPoll()
         */
        Reference reference = referenceQueue.remove();
        System.out.println(reference);// java.lang.ref.WeakReference@f2a0b8e

        reference = referenceQueue.poll();
        System.out.println(reference);// null
    }

    /**
     * 使用 WeakHashMap 之前，先了解 HashMap 的不足。
     * <p>
     * 假设 HashMap 的 key 为 WeakReference 对象，当弱引用对象被回收，HashMap#size 不能准确反映 Map 中有效数据的大小。
     * 在这次处理中，Map 并没有因为不断加入的 1M 对象由产生 OOM异常，并且最终运行结果之后 Map中的确有 1 万个对象。
     * 不过其中的 key(即 weakReference)对象中的 byte[] 对象却被回收了。即不断 new 出来的 1M 数组被 gc 掉了。
     * 从执行结果，我们看到有 9995 个对象被 gc，即意味着在 map 的 key 中，除了 weakReference 对象之外，没有我们想要的业务对象。
     * 那么在这样的情况下，这 9995 个 entry 可以认为是没有任何意义的对象，我们期望可以将其移除掉，并且 size 值可以打印出 5，而不是 10000.
     * WeakHashMap就是这样的一个类似实现。
     * <p>
     * https://www.cnblogs.com/dreamroute/p/5029899.html
     */
    @Test
    public void hashMap() throws InterruptedException {
        ReferenceQueue referenceQueue = new ReferenceQueue(); // 引用队列
        Object value = new Object();
        HashMap<Object, Object> map = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            byte[] bytes = new byte[1024 * 1024]; // 1M
            WeakReference<byte[]> weakReference = new WeakReference<byte[]>(bytes, referenceQueue);
            map.put(weakReference, value);
        }
        System.out.println("map.size->" + map.size()); // 10000

        Thread thread = new Thread(() -> {
            try {
                int cnt = 0;
                WeakReference<byte[]> k;
                while ((k = (WeakReference) referenceQueue.remove(1000)) != null) {
                    System.out.println((cnt++) + "回收了:" + k);
                }
            } catch (InterruptedException e) {
                //结束循环
            }
        });
        thread.setDaemon(true);
        thread.start();
        thread.join();// 主线程需要等待，直到当前线程thread消亡

        System.out.println("map.size->" + map.size()); // 10000
    }

    @Test
    public void weakHashMap() throws InterruptedException {
        WeakHashMap<Object, Object> map = new WeakHashMap<>();
        /**
         * WeakHashMap 内部定义了引用队列
         * @see WeakHashMap#queue
         *
         * WeakHashMap 中的元素类，继承了 WeakReference，将 key 作为弱引用的 {@link Reference#referent}
         * @see WeakHashMap.Entry
         */
        Object value = new Object();
        for (int i = 0; i < 10000; i++) {
            byte[] bytes = new byte[1024 * 1024]; // 1M
            map.put(bytes, value);
            /**
             * @see WeakHashMap#put(java.lang.Object, java.lang.Object)
             *
             * 已知被回收的对象，会由 GC 存入引用队列中
             * 其中，存入元素之前，获取当前 table 数组，该操作会触发拉取引用队列数据，清除 Map 中废弃的元素
             * @see WeakHashMap#getTable()
             * @see WeakHashMap#expungeStaleEntries()
             */
        }
        System.out.println("map.size->" + map.size()); // 609
    }

}
