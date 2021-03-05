package com.sumkor.map;

import org.junit.Test;

import java.util.*;

/**
 * @author Sumkor
 * @since 2021/3/4
 */
public class LinkedHashMapTest {

    /**
     * 快速失败
     * 非线程安全的标配
     */
    @Test(expected = ConcurrentModificationException.class)
    public void failFast() {
        LinkedHashMap<Integer, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put(8, "a");
        linkedHashMap.put(3, "b");
        linkedHashMap.put(6, "c");
        linkedHashMap.put(9, "d");
        linkedHashMap.put(4, "e");
        Iterator<Map.Entry<Integer, String>> iterator = linkedHashMap.entrySet().iterator();
        /**
         * @see LinkedHashMap.LinkedKeySet#iterator()
         * @see LinkedHashMap.LinkedHashIterator#LinkedHashIterator()
         * expectedModCount = modCount;
         */

        linkedHashMap.put(5, "g");

        Map.Entry<Integer, String> next = iterator.next();
        /**
         * @see LinkedHashMap.LinkedKeyIterator#next()
         * @see LinkedHashMap.LinkedHashIterator#nextNode()
         * modCount != expectedModCount
         */
        System.out.println("next = " + next);
    }

    /**
     * 遍历顺序测试
     */
    @Test
    public void forEach() {
        // LinkedHashMap 按照插入的顺序排序
        LinkedHashMap<Integer, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put(8, "a");
        linkedHashMap.put(3, "b");
        linkedHashMap.put(6, "c");
        linkedHashMap.put(9, "d");
        linkedHashMap.put(4, "e");
        linkedHashMap.entrySet().forEach(entry -> System.out.print(entry + " ")); // 8=a 3=b 6=c 9=d 4=e
        System.out.println();

        // HashMap 按照 key.hash 排序（无冲突情况下）
        HashMap<Integer, String> hashMap = new HashMap<>();
        hashMap.put(8, "a");
        hashMap.put(3, "b");
        hashMap.put(6, "c");
        hashMap.put(9, "d");
        hashMap.put(4, "e");
        hashMap.entrySet().forEach(entry -> System.out.print(entry + " ")); // 3=b 4=e 6=c 8=a 9=d
        System.out.println();

        // HashMap 按照 value 排序
        hashMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(entry -> System.out.print(entry + " ")); // 8=a 3=b 6=c 9=d 4=e
    }

    /**
     * 设置访问顺序
     * accessOrder = true，则会把最近访问的元素，放在链表最后
     */
    @Test
    public void accessOrder() {
        LinkedHashMap<Integer, String> linkedHashMap = new LinkedHashMap<>(16, 0.75f, true);
        /**
         * 是否按访问顺序排序。true-访问顺序，false-插入顺序，不设置默认是 false。
         * @see LinkedHashMap#accessOrder
         */
        linkedHashMap.put(8, "a");
        linkedHashMap.put(3, "b");
        linkedHashMap.put(6, "c");
        linkedHashMap.put(9, "d");
        linkedHashMap.put(4, "e");
        linkedHashMap.entrySet().forEach(entry -> System.out.print(entry + " ")); // 8=a 3=b 6=c 9=d 4=e
        System.out.println();


        linkedHashMap.get(6);
        /**
         * @see LinkedHashMap#afterNodeAccess(java.util.HashMap.Node)
         * 把尾节点指向当前节点（当前所读的节点）
         */
        linkedHashMap.entrySet().forEach(entry -> System.out.print(entry + " ")); // 8=a 3=b 9=d 4=e 6=c
        System.out.println();


        linkedHashMap.put(8, "-a");
        /**
         * @see HashMap#put(java.lang.Object, java.lang.Object)
         * @see HashMap#putVal(int, java.lang.Object, java.lang.Object, boolean, boolean)
         *
         * 1. 若旧节点存在，需要覆盖其 value，会执行
         * @see LinkedHashMap#afterNodeAccess(java.util.HashMap.Node)
         *
         * 当 accessOrder 为 true，且当前节点e不是尾节点，则把尾节点指向当前节点（当前覆盖加入的节点）
         *
         * 2. 放入元素完毕，会执行
         * @see LinkedHashMap#afterNodeInsertion(boolean)
         *
         * 由 linkedHashMap#put 方法放入元素，evict 总是为 true.
         * 但是 {@link LinkedHashMap#removeEldestEntry(java.util.Map.Entry)} 总是为 false
         * 所以 linkedHashMap 插入元素并不会做删除操作
         */
        linkedHashMap.entrySet().forEach(entry -> System.out.print(entry + " ")); // 3=b 9=d 4=e 6=c 8=-a
        System.out.println();


        linkedHashMap.remove(9);
        /**
         * @see HashMap#removeNode(int, java.lang.Object, java.lang.Object, boolean, boolean)
         * @see LinkedHashMap#afterNodeRemoval(java.util.HashMap.Node)
         * 把节点从双向链表中删除
         */
        linkedHashMap.entrySet().forEach(entry -> System.out.print(entry + " ")); // 3=b 4=e 6=c 8=-a
        System.out.println();
    }

    /**
     * 如何做到根据插入的顺序遍历？
     */
    @Test
    public void putOrder() {
        LinkedHashMap<Integer, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put(8, "a");
        /**
         * @see HashMap#put(java.lang.Object, java.lang.Object)
         * @see HashMap#putVal(int, java.lang.Object, java.lang.Object, boolean, boolean)
         *
         * LinkedHashMap 重写了 newNode()方法，通过此方法保证了插入的顺序性。
         * @see LinkedHashMap#newNode(int, java.lang.Object, java.lang.Object, java.util.HashMap.Node)
         * @see LinkedHashMap#linkNodeLast(java.util.LinkedHashMap.Entry)
         */
        linkedHashMap.put(3, "b");
        linkedHashMap.put(6, "c");

        Iterator<Map.Entry<Integer, String>> iterator = linkedHashMap.entrySet().iterator();
        Map.Entry<Integer, String> next = iterator.next();
        /**
         * @see LinkedHashMap.LinkedKeyIterator#next()
         * @see LinkedHashMap.LinkedHashIterator#nextNode()
         * 根据 e.after 指针来遍历
         */
        System.out.println("next = " + next);

        next = iterator.next();
        System.out.println("next = " + next);

        next = iterator.next();
        System.out.println("next = " + next);
    }

    /**
     * LRU 全称是 Least Recently Used，即最近最久未使用的意思。
     * LRU 算法的设计原则是：如果一个数据在最近一段时间没有被访问到，那么在将来它被访问的可能性也很小。也就是说，当限定的空间已存满数据时，应当把最久没有被访问到的数据淘汰。
     * 当存在热点数据时，LRU 的效率很好，但偶发性的、周期性的批量操作会导致 LRU 命中率急剧下降，缓存污染情况比较严重。
     */
    @Test
    public void lruTest() {
        /**
         * 只需要重写 {@link LinkedHashMap#removeEldestEntry} 方法即可。
         * 激活 {@link LinkedHashMap#afterNodeInsertion(boolean)} 中插入完成后的删除逻辑。
         */
        // 创建一个只有5个元素的缓存
        LRU<Integer, Integer> lru = new LRU<>(5, 0.75f);
        lru.put(1, 1);
        lru.put(2, 2);
        lru.put(3, 3);
        lru.put(4, 4);
        lru.put(5, 5);
        lru.put(6, 6);
        lru.put(7, 7);

        System.out.println(lru.get(4));

        lru.put(6, 666);

        // 输出: {3=3, 5=5, 7=7, 4=4, 6=666}
        // 可以看到最旧的元素被删除了
        // 且最近访问的4被移到了后面
        System.out.println(lru);
    }

    class LRU<K, V> extends LinkedHashMap<K, V> {

        // 保存缓存的容量
        private int capacity;

        /**
         * 基于 LinkedHashMap 实现，accessOrder 设置为 true，按访问顺序。
         * 即调用 get(key) 之后，将此 Entry 插入到链表尾部。
         */
        public LRU(int capacity, float loadFactor) {
            super(capacity, loadFactor, true);
            this.capacity = capacity;
        }

        /**
         * 重写 removeEldestEntry 方法设置何时移除旧元素
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            // 当元素个数大于了缓存的容量, 就移除元素
            return size() > this.capacity;
        }
    }
}
