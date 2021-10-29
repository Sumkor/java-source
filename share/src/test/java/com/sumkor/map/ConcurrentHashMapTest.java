package com.sumkor.map;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Function;

/**
 * @see java.util.concurrent.ConcurrentHashMap
 * @author Sumkor
 * @since 2021/2/11
 */
public class ConcurrentHashMapTest {

    //region 基本操作

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
     * <p>
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

    /**
     * HashMap 允许 null key 和 null value
     * ConcurrentHashMap 均不允许，是因为无法分辨是 key 没找到的 null，还是有 key 值为 null，这在多线程里面是模糊不清的，所以压根就不让 put null。
     */
    @Test
    public void putNull() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put(null, 1);
        map.put(1, null);
    }

    //endregion 基本操作

    //region 复合操作

    /**
     * key 不存在则存入，返回旧值
     */
    @Test
    public void putIfAbsent() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        Object object0 = map.putIfAbsent("2", 3);
        Object object1 = map.putIfAbsent("3", 3);
        System.out.println(object0);
        System.out.println(object1);
        System.out.println(map);
        /**
         * 2
         * null
         * {1=1, 2=2, 3=3}
         */
    }

    /**
     * key 不存在则生成新的 value 进行插入，返回当前 value
     */
    @Test
    public void computeIfAbsent() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        Object object0 = map.computeIfAbsent("2", key -> 3); // key存在，不处理，返回旧value
        Object object1 = map.computeIfAbsent("3", key -> 3); // key不存在，执行函数生成value，存入key-value，返回新value
        System.out.println(object0);
        System.out.println(object1);
        System.out.println(map);
        /**
         * 2
         * 3
         * {1=1, 2=2, 3=3}
         */
    }

    /**
     * key 存在则生成新的 value 进行更新，返回当前 value
     */
    @Test
    public void computeIfPresent() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        Object object0 = map.computeIfPresent("2", (key, value) -> 3); // key存在，执行函数更新value，返回旧value
        Object object1 = map.computeIfPresent("3", (key, value) -> 3); // key不存在，不处理
        System.out.println(object0);
        System.out.println(object1);
        System.out.println(map);
        /**
         * 3
         * null
         * {1=1, 2=3}
         */
    }

    /**
     * 不管 key、value 存在与否，都将 key-value 设置为指定值
     */
    @Test
    public void compute() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put("1", 1);
        map.put("2", 2);
        Object object0 = map.compute("2", (key, value) -> 3);
        Object object1 = map.compute("3", (key, value) -> 3);
        System.out.println(object0);
        System.out.println(object1);
        System.out.println(map);
        /**
         * 3
         * 3
         * {1=1, 2=3, 3=3}
         */
    }

    /**
     * 在 concurrentHashMap 中使用 computeIfAbsent/compute 方法修改一个 key 时，如果其中的 lambda 函数修改的 key 与之 hash 冲突，会出现死循环。
     *
     * ConcurrentHashMap BUG 死循环
     * https://blog.csdn.net/zhanglong_4444/article/details/93638844
     *
     * 说明：
     * 当 map 中不存在 key="AaAa" 时，computeIfAbsent 会插入该 key，并将 lambda 函数的返回值 42 作为它的 value。
     * 而这个 lambda 函数其实会继续去对 key="BBBB" 的 Node 进行同样操作，并设置 value=42。
     * 但是由于这里的 “AaAa” 和 “BBBB” 这个字符串的 hashCode 一样，导致执行出现死循环。
     *
     * 分析：
     * 执行第一个 computeIfAbsent，根据 key "AaAa" 定位到一个空桶，此时 (f = tabAt(tab, i = (n - 1) & h)) == null 成立，因此在 i 位置插入预留节点 ReservationNode；
     * 接下来执行第二个 computeIfAbsent，根据 key "BBBB" 定位到同一个桶，由于桶中的节点 f 是上一步创建的 ReservationNode 类型对象，
     * 此时 f == null 不成立，并且 f 也无法进行其他操作，陷入死循环中。
     *
     * This is fixed in JDK 9 with JDK-8071667 . When the test case is run in JDK 9-ea, it gives a ConcurrentModification Exception.
     */
    @Test
    public void deadLock() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>(16);
        map.computeIfAbsent("AaAa", key -> map.computeIfAbsent("BBBB", key2 -> 42));
        System.out.println("======  end  =============");
    }

    @Test
    public void deadLock02() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>(16);
        map.compute("AaAa", (key, value) -> map.compute("BBBB", (key2, value2) -> 42));
        System.out.println("======  end  =============");
    }

    @Test
    public void deadLock03() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>(16);
        map.computeIfAbsent("AaAa", key -> map.put("BBBB", 42));
        System.out.println("======  end  =============");
    }

    /**
     * ConcurrentHashMap.computeIfAbsent(k,f) locks bin when k present
     * https://bugs.openjdk.java.net/browse/JDK-8161372
     *
     * 执行结果：把线程快照 dump 出来，可用看到每个线程都是 BLOCKED 状态
     *
     * 问题分析：
     * https://zhuanlan.zhihu.com/p/364340936
     *
     * computeIfAbsent 首先判断缓存 map 中是否存在指定 key 的值，如果不存在，会自动调用 mappingFunction (key) 计算 key 的 value，然后将 key = value 放入到缓存 Map。
     * ConcurrentHashMap 中重写了 computeIfAbsent 方法确保 mappingFunction 中的操作是线程安全的。
     * 官方说明中一段：
     * The entire method invocation is performed atomically, so the function is applied at most once per key.
     * Some attempted update operations on this map by other threads may be blocked while computation is in progress,
     * so the computation should be short and simple, and must not attempt to update any other mappings of this map.
     * 可以看到，为了保证原子性，当对相同 key 进行修改时，可能造成线程阻塞。
     * 显而易见这会造成比较严重的性能问题。
     *
     * 很多开发者都以为 computeIfAbsent 是不会造成线程 block 的，但事实却是相反的。
     * 而 Java 官方当时认为这个方法的设计没问题。但反思之后也觉得，在性能还不错的 ConcurrentHashMap 中有这么个拉胯兄弟确实不太合适。
     * 所以，官方在 JDK9 中修复了这个问题。
     *
     * 总结，其实就是多个线程同时阻塞在 synchronized 上，导致的性能问题。而这种阻塞其实是没必要的，可以避免。
     */
    @Test
    public void deadLock04() throws InterruptedException {
        final int MAP_SIZE = 20;
        final int THREADS = 20;
        final ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();

        for (int i = 0; i < MAP_SIZE; i++) {
            map.put(i, i);
        }

        Thread.sleep(5000);

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread() {
                public void run() {
                    int i = 0;
                    int result = 0;
                    while (result < Integer.MAX_VALUE) {
                        i = (i + 1) % MAP_SIZE;
                         result += map.computeIfAbsent(i, (key) -> key + key);
//                        result += computeIfAbsent(map, i, (key) -> key + key);
                    }
                }
            };
            threads.add(t);
            t.start();
        }
        threads.get(0).join();
    }

    /**
     * Mybatis 中的解决方法
     * org.apache.ibatis.util.MapUtil#computeIfAbsent
     */
    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<K, V> mappingFunction) {
        V value = map.get(key);
        if (value != null) {
            return value;
        }
        System.out.println("---");
        return map.computeIfAbsent(key, mappingFunction::apply);
    }

    //endregion 复合操作

    //region 扩容操作

    /**
     * 扩容时 sizeCtl = -N
     * N是int类型，分为两部分，高15位是指定容量标识，低16位表示并行扩容线程数+1
     * <p>
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
     * lastRun = C
     * 2=B ->
     * 1=A -> 3=C ->
     * --------------------------
     *
     * 执行结果：
     *
     * 1=A -> 2=B -> 3=C -> 4=D -> 5=E -> 6=F -> 8=H -> 10=J ->
     * --------------------------
     * lastRun = F
     * 4=D -> 2=B -> 6=F -> 8=H -> 10=J ->
     * 5=E -> 3=C -> 1=A ->
     * --------------------------
     */
    @Test
    public void transferLink() {
        int oldCap = 1;
        int newCap = 2;
        Node[] oldTable = new Node[oldCap];
        Node[] newTable = new Node[newCap];
        // A -> B -> C
//        Node firstLinkNode03 = new Node(new Integer(3).hashCode(), 3, "C", null);
//        Node firstLinkNode02 = new Node(new Integer(2).hashCode(), 2, "B", firstLinkNode03);
//        Node firstLinkNode01 = new Node(new Integer(1).hashCode(), 1, "A", firstLinkNode02);
        Node firstLinkNode10 = new Node(new Integer(10).hashCode(), 10, "J", null);
        Node firstLinkNode08 = new Node(new Integer(8).hashCode(), 8, "H", firstLinkNode10);
        Node firstLinkNode06 = new Node(new Integer(6).hashCode(), 6, "F", firstLinkNode08);
        Node firstLinkNode05 = new Node(new Integer(5).hashCode(), 5, "E", firstLinkNode06);
        Node firstLinkNode04 = new Node(new Integer(4).hashCode(), 4, "D", firstLinkNode05);
        Node firstLinkNode03 = new Node(new Integer(3).hashCode(), 3, "C", firstLinkNode04);
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
            System.out.println("lastRun = " + lastRun.getValue());
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

    //endregion 扩容

    //region 其他

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

    //endregion 其他
}
