package com.sumkor.map;

import org.junit.Test;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @see java.util.concurrent.ConcurrentHashMap
 * @author Sumkor
 * @since 2021/2/11
 */
public class ConcurrentHashMapTest {

    /**
     * 存入
     * @see ConcurrentHashMap#putVal(java.lang.Object, java.lang.Object, boolean)
     *
     * 取出
     * @see ConcurrentHashMap#get(java.lang.Object)
     *
     * 初始化 table
     * @see ConcurrentHashMap#initTable()
     *
     * 帮助扩容
     * @see ConcurrentHashMap#helpTransfer(java.util.concurrent.ConcurrentHashMap.Node[], java.util.concurrent.ConcurrentHashMap.Node)
     *
     * 计算 size，判断扩容
     * @see ConcurrentHashMap#addCount(long, int)
     *
     * 扩容
     * @see ConcurrentHashMap#transfer(java.util.concurrent.ConcurrentHashMap.Node[], java.util.concurrent.ConcurrentHashMap.Node[])
     */

    /**
     * 失败安全 Iterator
     */
    @Test
    public void failSafe() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();// 创建迭代器
        /**
         * @see ConcurrentHashMap.EntrySetView#iterator()
         * 这里创建了对象 {@link ConcurrentHashMap.EntryIterator#EntryIterator(java.util.concurrent.ConcurrentHashMap.Node[], int, int, int, java.util.concurrent.ConcurrentHashMap)}
         * 没有 modCount 这个变量了
         */

        map.put("3", "c");// 改变结构
        System.out.println("map = " + map);

        iterator.next();// 失败安全，不抛异常
        /**
         * @see ConcurrentHashMap.EntryIterator#next()
         */
    }

    /**
     * 失败安全 Enumeration
     */
    @Test
    public void failSafe02() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Enumeration<Object> elements = map.elements(); // 创建迭代器

        map.put("3", "c");

        Object element = elements.nextElement(); // 失败安全，不抛异常
        /**
         * @see ConcurrentHashMap.ValueIterator#next()
         */
        System.out.println("element = " + element);
    }

    /**
     * load factor 默认为 0.75，在 ConcurrentHashMap 不常用，使用 n - (n >>> 2) 替代了
     */
    @Test
    public void loadFactor() {
        int cap = 16;
        float loadFactor = 0.75f;

        System.out.println("threshold = " + cap * loadFactor);
        System.out.println("threshold = " + (cap - (cap >>> 2)));
    }

    /**
     * 哈希桶数组索引位置的计算
     * https://blog.csdn.net/qq_38262266/article/details/108789396
     */
    @Test
    public void hash() {
        int capacity = 16;// 值为16。见 java.util.concurrent.ConcurrentHashMap.DEFAULT_CAPACITY
        Object key = 17;

        int h = key.hashCode();// 第一步取hashCode值
        h = h ^ (h >>> 16);// 第二步高位参与运算
        h = h & 0x7fffffff;// 第三步与HASH_BITS相与，主要作用是使hash值为正数
        /**
         * @see ConcurrentHashMap#spread(int)
         *
         * 在 ConcurrentHashMap 之中，hash值为负数有特殊的含义：
         * -1 表示 ForwardingNode 节点 {@link ConcurrentHashMap#MOVED}
         * -2 表示 TreeBin 节点 {@link ConcurrentHashMap#TREEBIN}
         */

        h = h & (capacity - 1);// 第四步取模运算
        /**
         * @see ConcurrentHashMap#putVal(java.lang.Object, java.lang.Object, boolean)
         *
         */

        System.out.println("h = " + h);
    }

    /**
     * 负数与0x7fffffff相与，得到正数
     */
    @Test
    public void hashBits() {
        int h = -1231545;
        System.out.println("h = " + Integer.toBinaryString(h));
        /**
         * @see ConcurrentHashMap#HASH_BITS
         */
        System.out.println(Integer.toBinaryString(0x7fffffff));// 0111 1111 1111 1111 1111 1111 1111 1111
        h = h & 0x7fffffff;
        System.out.println("h = " + Integer.toBinaryString(h));
        System.out.println(h);
    }

    /**
     * 不用帮忙扩容
     *
     * 计算 hash 值，定位到该 table 索引位置，如果是首节点符合就返回。
     * 如果遇到树结构 TreeBin 或扩容节点 ForwardingNode，会调用对应的 find 方法，查找该节点，匹配就返回
     * 以上都不符合的话，说明是链表结构，往下遍历节点，匹配就返回，否则最后就返回 null
     */
    @Test
    public void get() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put("1", "a");
        Object object = map.get("1");
        System.out.println("object = " + object);
        /**
         * TreeBin的查找，读写锁。
         * 由于红黑树的插入、删除会涉及整个结构的调整，所以通常存在读写并发操作的时候，是需要加锁的。
         * @see ConcurrentHashMap.TreeBin#find(int, java.lang.Object)
         *
         * ForwardingNode的查找，在 nextTable 上查找
         * @see ConcurrentHashMap.ForwardingNode#find(int, java.lang.Object)
         */
    }

    //-------------------------------------------------------------

    /**
     * 扩容时 sizeCtl = -N
     * N是int类型，分为两部分，高15位是指定容量标识，低16位表示并行扩容线程数+1
     *
     * https://blog.csdn.net/tp7309/article/details/76532366
     */
    @Test
    public void sizeCtl() {
        /**
         * 插入元素的时候，检查 sizeCtl 看是否需要扩容，此时
         * {@link ConcurrentHashMap#putVal} 操作调用了 {@link ConcurrentHashMap#addCount}
         * 若这里检查到 size > sizeCtl阈值，则进行扩容。
         */

        /**
         * 首先关注其中的 {@link ConcurrentHashMap#resizeStamp} 方法
         */
        int n = 8;
        /**
         * 设置容量为 8，二进制表示如下：
         * 0000 0000 0000 0000 0000 0000 0000 1000
         */

        n = Integer.numberOfLeadingZeros(n);
        /**
         * Integer.numberOfLeadingZeros(n) 用于计算 n 转换成二进制后前面有几个 0。
         * 已知 ConcurrentHashMap 的容量必定是 2 的幂次方，所以不同的容量 n 前面 0 的个数必然不同，
         * 这里相当于用 0 的个数来记录 n 的值。
         *
         * Integer.numberOfLeadingZeros(8)=28，二进制表示如下：
         * 0000 0000 0000 0000 0000 0000 0001 1100
         */

        int rs = n | (1 << (RESIZE_STAMP_BITS - 1));
        /**
         * (1 << (RESIZE_STAMP_BITS - 1)即是 1<<15，表示为二进制即是高 16 位为 0，低 16 位为 1：
         * 0000 0000 0000 0000 1000 0000 0000 0000
         *
         * 再与 n 作或运算，得到二进制如下：
         * 0000 0000 0000 0000 1000 0000 0001 1100
         */
        System.out.println("rs = " + Integer.toBinaryString(rs));
        /**
         * 结论：resizeStamp()的返回值：高16位置0，第16位为1，低15位存放当前容量n扩容标识，用于表示是对n的扩容。
         */

        int sizeCtl = (rs << RESIZE_STAMP_SHIFT) + 2;
        /**
         * rs << 16，左移 16 后最高位为 1，所以成了一个负数。计算得到 sizeCtl 二进制如下：
         * 1000 0000 0001 1100 0000 0000 0000 0010
         */
        System.out.println("sizeCtl = " + Integer.toBinaryString(sizeCtl));
        /**
         * 那么在扩容时 sizeCtl 值的意义便如下所示：
         * 高15位：容量n扩容标识
         * 低16位：并行扩容线程数+1
         */

        System.out.println("MAX_RESIZERS = " + MAX_RESIZERS);
        /**
         * 最大的可参与扩容的线程数：65535
         */
    }

    /**
     * The number of bits used for generation stamp in sizeCtl.
     * Must be at least 6 for 32bit arrays.
     */
    private static int RESIZE_STAMP_BITS = 16;

    /**
     * The bit shift for recording size stamp in sizeCtl.
     */
    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

    /**
     * The maximum number of threads that can help resize.
     * Must fit in 32 - RESIZE_STAMP_BITS bits.
     */
    private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;

    //-------------------------------------------------------------

    /**
     * 扩容时，链表迁移算法
     *
     * 执行结果：
     *
     * 1=A -> 2=B -> 3=C ->
     * --------------------------
     * 2=B ->
     * 1=A -> 3=C ->
     * --------------------------
     */
    @Test
    public void transferLink() {
        int oldCap = 1;
        int newCap = 2;
        Node[] oldTable = new Node[oldCap];
        Node[] newTable = new Node[newCap];
        // A -> B -> C
//        Node firstLinkNode03 = new Node(new Integer(4).hashCode(), 4, "D", null);
        Node firstLinkNode03 = new Node(new Integer(3).hashCode(), 3, "C", null);
        Node firstLinkNode02 = new Node(new Integer(2).hashCode(), 2, "B", firstLinkNode03);
        Node firstLinkNode01 = new Node(new Integer(1).hashCode(), 1, "A", firstLinkNode02);
        oldTable[0] = firstLinkNode01;
        printTable(oldTable);

        // 赋值
        int i = 0;
        int n = oldCap;
        Node f = firstLinkNode01;
        int fh = firstLinkNode01.hash;

        /**
         * 单个桶元素扩容
         * @see ConcurrentHashMap#transfer(java.util.concurrent.ConcurrentHashMap.Node[], java.util.concurrent.ConcurrentHashMap.Node[])
         */
        Node ln, hn;
        if (fh >= 0) { // 链表节点。非链表节点hash值小于0
            int runBit = fh & n; // 根据 hash&n 的结果，将所有结点分为两部分
            Node lastRun = f;
            for (Node p = f.next; p != null; p = p.next) { // 遍历原链表得到lastRun，该节点作为新链表的起始节点（新链表采用头插法）
                int b = p.hash & n; // 遍历链表的每个节点，依次计算 hash&n
                if (b != runBit) {
                    runBit = b;
                    lastRun = p;
                }
            }
            if (runBit == 0) { // 判断lastRun节点是属于高位还是地位
                ln = lastRun;
                hn = null;
            } else {
                hn = lastRun;
                ln = null;
            }
            for (Node p = f; p != lastRun; p = p.next) {
                int ph = p.hash;
                Object pk = p.key;
                Object pv = p.val;
                if ((ph & n) == 0)
                    ln = new Node(ph, pk, pv, ln); // hash&n为0，索引位置不变，作低位链表。这里采用头插法
                else
                    hn = new Node(ph, pk, pv, hn); // hash&n不为0，索引变成“原索引+oldCap”，作高位链表
            }
            newTable[i] = ln;
            newTable[i + n] = hn;
            printTable(newTable);
        }
    }

    /**
     * ConcurrentHashMap 中的 Node 结构
     */
    static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        volatile V val;
        volatile Node<K, V> next;

        Node(int hash, K key, V val, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return val;
        }

        public final int hashCode() {
            return key.hashCode() ^ val.hashCode();
        }

        public final String toString() {
            return key + "=" + val;
        }

        public final V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            Object k, v, u;
            Map.Entry<?, ?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?, ?>) o).getKey()) != null &&
                    (v = e.getValue()) != null &&
                    (k == key || k.equals(key)) &&
                    (v == (u = val) || v.equals(u)));
        }

        /**
         * Virtualized support for map.get(); overridden in subclasses.
         */
        Node<K, V> find(int h, Object k) {
            Node<K, V> e = this;
            if (k != null) {
                do {
                    K ek;
                    if (e.hash == h &&
                            ((ek = e.key) == k || (ek != null && k.equals(ek))))
                        return e;
                } while ((e = e.next) != null);
            }
            return null;
        }
    }

    /**
     * HashMap 中的 Node 结构，打印
     */
    private void printTable(Node[] table) {
        for (int i = 0; i < table.length; i++) {
            Node tmpNode = table[i];// 用于打印，不改变table的结构
            while (tmpNode != null) {
                System.out.print(tmpNode + " -> ");
                tmpNode = tmpNode.next;
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }

    //-------------------------------------------------------------

    /**
     * node 指针赋值的调试，这里看到实际是被编译器优化过了
     */
    @Test
    public void testCopy() {
        int a = 1;
        int b = a;

        ++a;

        System.out.println("a = " + a);// 2
        System.out.println("b = " + b);// 1

        Node aNode = new Node(1, 1, 1, null);
        Node bNode = new Node(2, 2, 2, null);
        Node cNode = new Node(3, 3, 3, null);

        aNode = bNode;
        bNode = cNode; // 这里并不会连带修改aNode
        System.out.println("aNode = " + aNode);// bNode
        System.out.println("bNode = " + bNode);// cNode
        System.out.println("cNode = " + cNode);// cNode

        /**
         * 编译后的字节码文件 .class
         *
         *         int a = 1;
         *         int b = a;
         *         int a = a + 1;
         *         System.out.println("a = " + a);
         *         System.out.println("b = " + b);
         *         new ConcurrentHashMapTest.Node(1, 1, 1, (ConcurrentHashMapTest.Node)null);
         *         ConcurrentHashMapTest.Node bNode = new ConcurrentHashMapTest.Node(2, 2, 2, (ConcurrentHashMapTest.Node)null);
         *         ConcurrentHashMapTest.Node cNode = new ConcurrentHashMapTest.Node(3, 3, 3, (ConcurrentHashMapTest.Node)null);
         *         System.out.println("aNode = " + bNode);
         *         System.out.println("bNode = " + cNode);
         *         System.out.println("cNode = " + cNode);
         */
    }

    @Test
    public void cas() {
        /**
         * Unsafe.compareAndSwapInt() 方法解读
         *
         * public final native boolean compareAndSwapInt(Object o, long offset, int expected, int x);
         *
         * 此方法是 Java 的 native 方法，并不由 Java 语言实现。
         * 方法的作用是，读取传入对象 o 在内存中偏移量为 offset 位置的值与期望值 expected 作比较。
         * 相等就把 x 值赋值给 offset 位置的值。方法返回 true。
         * 不相等，就取消赋值，方法返回 false。
         * 这也是 CAS 的思想，及比较并交换。用于保证并发时的无锁并发的安全性。
         *
         * 可使用 {@link AtomicInteger} {@link AtomicIntegerFieldUpdater}达到同样的效果
         */
        AtomicInteger atomicInteger = new AtomicInteger(55);
        System.out.println(atomicInteger.getAndIncrement()); // 55
        System.out.println(atomicInteger.get()); // 56
        System.out.println(atomicInteger.incrementAndGet()); // 57
        System.out.println(atomicInteger.get()); // 57
    }
}
