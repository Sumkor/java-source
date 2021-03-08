package com.sumkor.collection;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

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
 * @see java.util.ArrayList
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
         * @see ArrayList.Itr
         * 其中 int expectedModCount = modCount;
         * 即创建迭代器时，将 modCount 赋值给 expectedModCount
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
        ArrayList<String> list = new ArrayList<>();
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

    /**
     * list.size() 计算的是往 list 中添加元素的数量，并不是 list 底层数组的大小
     */
    @Test
    public void size() {
        ArrayList<Integer> list = new ArrayList<>(8);
        list.add(0);
        System.out.println("list.size() = " + list.size());
    }

    @Test
    public void add() {
        ArrayList<Integer> list = new ArrayList<>(3);
        list.add(0);
        list.add(0, 1);
        System.out.println("list = " + list);// [1, 0]

        List<Integer> subList = list.subList(0, 1); // 返回的是 ArrayList$SubList 对象
        System.out.println("subList = " + subList);// [1]
    }

    @Test
    public void arrayCopy() {
        int[] array01 = {1, 2, 3, 4, 5};
        int[] array02 = new int[array01.length];
        System.arraycopy(array01, 0, array02, 0, 5);

        // int[] 类型转换为 List
        List<Integer> list = Arrays.stream(array02).boxed().collect(Collectors.toList());
        System.out.println("array02 = " + list); // [1, 2, 3, 4, 5]

        /**
         * 模拟插入
         * @see ArrayList#add(int, java.lang.Object)
         */
        int index = 2;
        int element = 9;
        int[] array03 = {1, 2, 3, 4, 5, 0, 0, 0, 0, 0};
        System.arraycopy(array03, index, array03, index + 1, 5 - index);
        // 将 array03 中的从 index 位开始（包含index位）的数列 [3, 4, 5]，复制到新数组的 index + 1 位置
        System.out.println("array03 = " + Arrays.stream(array03).boxed().collect(Collectors.toList()));// [1, 2, 3, 3, 4, 5, 0, 0, 0, 0]
        array03[index] = element;
        System.out.println("array03 = " + Arrays.stream(array03).boxed().collect(Collectors.toList()));// [1, 2, 9, 3, 4, 5, 0, 0, 0, 0]
    }

    /**
     * subList() 方法返回的是 ArrayList$SubList 对象，可理解为是原数组的一个视图
     */
    @Test
    public void subList() {
        ArrayList<Integer> list = new ArrayList<>(3);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        List<Integer> subList = list.subList(1, 3); // 返回的是 ArrayList$SubList 对象
        System.out.println("subList = " + subList);// [2, 3]

        subList.add(5);
        /**
         * @see AbstractList#add(java.lang.Object)
         * @see ArrayList.SubList#add(int, java.lang.Object)
         */
        subList.add(6);
        subList.add(7);
        System.out.println("subList = " + subList); // [2, 3, 5, 6, 7]
        System.out.println("list = " + list);// [1, 2, 3, 5, 6, 7, 4]

        list.add(1, 0);
        System.out.println("list = " + list);// [1, 0, 2, 3, 5, 6, 7, 4]
        System.out.println("subList = " + subList); // java.util.ConcurrentModificationException
        /**
         * subList#add 等操作是对原始 list进行操作，并把操作后的 modCount 赋给自己
         * 对原始的 list 进行 add 的时候，subList 的 modCount 感知不到。
         * 导致 subList#checkForComodification 失败
         * https://www.cnblogs.com/turn2i/p/10576682.html
         */
    }

    /**
     * List 中的泛型
     */
    @Test
    public void ClassCast() {
        List list = new ArrayList();
        list.add("abc");
        list.add(new Integer(1)); //可以通过编译
        for (Object object : list) {
            System.out.println(object);// 正常
            System.out.println((String)object);//抛出ClassCastException异常
        }
    }

    /**
     * List 转换 数组
     */
    @Test
    public void toArray() {
        List<String> list = new ArrayList<>();
        list.add("a");
        String[] arrays = new String[list.size()];
        arrays = list.toArray(arrays);

        for (String array : arrays) {
            System.out.println("array = " + array);
        }
    }

    /**
     * 数组 转换 List
     */
    @Test
    public void asList01() {
        List<String> list = Arrays.asList("1", "2", "3");
        /**
         * 得到的是 {@link java.util.Arrays.ArrayList} 对象，允许修改，不支持增删（结构变动）
         */
        System.out.println("list = " + list);// [1, 2, 3]

        list.set(0, "0");// 正常
        System.out.println("list = " + list);// [0, 2, 3]

        list.remove(0);// java.lang.UnsupportedOperationException
        list.add("4");// java.lang.UnsupportedOperationException
    }

    /**
     * 数组 转换 List
     */
    @Test
    public void asList02() {
        List<String> list = new ArrayList<>(Arrays.asList("1", "2", "3")); // 正常CRUD

        Collections.addAll(list, new String[]{"4", "5", "6"});
        Collections.addAll(list, "7", "8", "9");

        System.out.println("list = " + list); // [1, 2, 3, 4, 5, 6, 7, 8, 9]
    }

    @Test
    public void filter() {
        List<String> list = Arrays.asList("a", "b", "c");
        System.out.println("list = " + list);

        List<String> list2 = list.stream().filter(t -> t.equals("b")).collect(Collectors.toList());// 并不会改变原有list
        System.out.println("list = " + list);
        System.out.println("list2 = " + list2);
    }
}
