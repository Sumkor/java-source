package com.sumkor.collection;

import org.junit.Test;

import java.util.*;

/**
 * @author Sumkor
 * @since 2021/3/3
 */
public class LinkedListTest {

    /**
     * 关于迭代器
     * ArrayList 遍历的时候用的是内部类 Itr，LinkedList 遍历的时候用的是父类 AbstractList.Itr
     * 它们都是快速失败的
     */
    @Test(expected = ConcurrentModificationException.class)
    public void fastFail() {
        LinkedList<String> list = new LinkedList<>();
        list.add("a");
        list.add("b");

        Iterator<String> iterator = list.iterator();
        /**
         * @see AbstractSequentialList#iterator()
         * 创建了 {@link AbstractList.ListItr} 对象，该对象继承了 {@link AbstractList.Itr}
         * 其中 int expectedModCount = modCount;
         */
        list.add("c");
        iterator.next(); // ConcurrentModificationException
        /**
         * @see AbstractList.Itr#next()
         * @see AbstractList.Itr#checkForComodification()
         */
    }

    @Test
    public void add() {
        LinkedList<String> list = new LinkedList<>();
        list.add("a");
        /**
         * @see LinkedList#linkLast(java.lang.Object)
         */
    }

    /**
     * 遍历、获取节点
     */
    @Test
    public void next() {
        LinkedList<String> list = new LinkedList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");
        list.add("f");
        Iterator<String> iterator = list.iterator();
        iterator.next();
        /**
         * @see AbstractList.Itr#next()
         * @see LinkedList#get(int)
         * @see LinkedList#node(int)
         */

        String node = list.get(3);
        /**
         * @see LinkedList#node(int)
         */
        System.out.println("node = " + node);
    }

    /**
     * TODO
     * @see java.util.Deque
     * @see java.util.Queue
     */
}
