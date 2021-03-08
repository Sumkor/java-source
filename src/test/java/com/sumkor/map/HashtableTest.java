package com.sumkor.map;

import org.junit.Test;

import java.util.*;

/**
 * @see java.util.Hashtable
 * @author Sumkor
 * @since 2021/2/11
 */
public class HashtableTest {

    /**
     * The iterators returned by the <tt>iterator</tt> method of the collections
     * returned by all of this class's "collection view methods" are
     * <em>fail-fast</em>
     * <p>
     * Hashtable 的所有 collection 视图方法所返回的迭代器 Iterator 都是快速失败的。
     */
    @Test(expected = ConcurrentModificationException.class)
    public void failFast01() {
        Hashtable<Object, Object> map = new Hashtable<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();// 创建迭代器
        /**
         * @see Hashtable.EntrySet#iterator()
         * @see Hashtable#getIterator(int)
         * @see Hashtable.Enumerator
         * 这里创建 Enumerator 对象，其中属性：protected int expectedModCount = modCount;
         */

        map.put("3", "c");// 改变结构
        System.out.println("map = " + map);

        iterator.next();// 快速失败，抛异常ConcurrentModificationException
        /**
         * @see Hashtable.Enumerator#next()
         * 判断 modCount != expectedModCount
         */
    }

    /**
     * The Enumerations returned by Hashtable's keys and elements methods are
     * <em>not</em> fail-fast.
     * <p>
     * Hashtable 的 Enumeration 迭代器不是快速失败的。
     */
    @Test
    public void failFast02() {
        Hashtable<Object, Object> map = new Hashtable<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Enumeration<Object> elements = map.elements(); // Enumeration 不是快速失败的，因为无法由 Enumeration 改变 map 结构。
        /**
         * @see Hashtable#elements()
         * @see Hashtable.Enumerator
         * 也是创建 Enumerator 对象，其中属性：protected int expectedModCount = modCount;
         */

        map.put("3", "c");// 改变结构

        Object element = elements.nextElement();// 非快速失败
        /**
         * @see Hashtable.Enumerator#nextElement()
         * 没有校验 modCount
         */
        System.out.println("element = " + element);
    }

    /**
     * Hashtable 使用外部迭代，非快速失败
     */
    @Test
    public void failFast03() {
        Hashtable<Object, Object> map = new Hashtable<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        for (int i = 0; i < map.size(); i++) {
            map.put("3", "c");// 改变结构
            System.out.println("map = " + map);
        }
    }

    // -----------------------

    /**
     * Collections.synchronizedMap 快速失败
     */
    @Test(expected = ConcurrentModificationException.class)
    public void failFast001() {
        Map<Object, Object> map = Collections.synchronizedMap(new HashMap<>());
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();// 创建迭代器

        map.put("3", "c");// 改变结构
        System.out.println("map = " + map);

        iterator.next();// 快速失败，抛异常ConcurrentModificationException
    }

}
