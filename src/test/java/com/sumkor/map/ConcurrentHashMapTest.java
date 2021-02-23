package com.sumkor.map;

import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
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
     * 失败安全
     */
    @Test
    public void failSafe() {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();// 创建迭代器

        map.put("3", "c");// 改变结构
        System.out.println("map = " + map);

        iterator.next();// 失败安全，不抛异常
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
         * -1 表示 ForwardingNode 节点
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
    public void hash02() {
        int h = -1231545;
        System.out.println("h = " + Integer.toBinaryString(h));
        System.out.println(Integer.toBinaryString(0x7fffffff));// 0111 1111 1111 1111 1111 1111 1111 1111
        h = h & 0x7fffffff;
        System.out.println("h = " + Integer.toBinaryString(h));
        System.out.println(h);
    }

    @Test
    public void testInt() {
        int a = 1;
        int b = a;

        ++a;

        System.out.println("a = " + a);// 2
        System.out.println("b = " + b);// 1
    }

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
