package com.sumkor.list;

import org.junit.Test;

import java.util.*;

/**
 * List子类的特点（总结）
 *
 * ArrayList:
 * ​ 底层数据结构是数组，查询快，增删慢。
 * ​ 线程不安全，效率高。
 *
 * Vector:
 * ​ 底层数据结构是数组，查询快，增删慢。
 * ​ 线程安全，效率低。
 *
 * LinkedList:
 * ​ 底层数据结构是链表，查询慢，增删快。
 * ​ 线程不安全，效率高。
 *
 * 使用具体情况：
 * ​ 保证安全：Vector(即使要安全，也不用这个，后面有替代的)
 * ​ 不保证安全：ArrayList或者LinkedList
 * ​ 查询多：ArrayList
 * ​ 增删多：LinkedList
 *
 * @author Sumkor
 * @since 2021/2/23
 */
public class ArrayListTest {

    /**
     * 快速失败
     *
     * 由 iterator() 和 listIterator() 方法放回的迭代器都是快速失败的
     * 创建迭代器之后，除非调用迭代器自身的方法，如：iterator.remove()、listIterator.add() 等，否则任何对于结构上的修改，都会抛异常
     */
    @Test(expected = ConcurrentModificationException.class)
    public void failFast01() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");

        Iterator<String> iterator = list.iterator();
        /**
         * @see ArrayList#iterator()
         * 创建迭代器时，将 modCount 赋值给 expectedModCount
         */
        list.add("c");
        System.out.println("list = " + list);

        iterator.next(); // 快速失败
        /**
         * @see ArrayList.Itr#next()
         * @see ArrayList.Itr#checkForComodification()
         */
    }

    /**
     * ListIterator 继承 Iterator，是 List 集合特有的迭代器
     */
    @Test
    public void listIterator() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        ListIterator<String> lit = list.listIterator();
        //正向遍历
        while (lit.hasNext()) {
            String s = lit.next();
            System.out.print(s + " ");
        }

        System.out.println();
        //逆向遍历
        while (lit.hasPrevious()) {
            String s = lit.previous();
            System.out.print(s + " ");
        }
    }

}
