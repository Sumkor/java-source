package com.sumkor.map;

import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sumkor
 * @since 2021/2/11
 */
public class ConcurrentHashMapTest {

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

    @Test
    public void testInt() {
        int a = 1;
        int b = a;

        ++a;

        System.out.println("a = " + a);
        System.out.println("b = " + b);
    }
}
