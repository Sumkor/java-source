package com.sumkor.collection;

import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 写时复制，通俗的理解就是当我们需要修改（增/删/改）列表中的元素时，不直接进行修改，而是先将列表 Copy，然后在新的副本上进行修改，修改完成之后，再将引用从原列表指向新列表。
 * <p>
 * 适用于：读多写少，
 * 不适用于：大数据量的场景。
 * <p>
 * 只能保证数据的最终一致性，不能保证数据的实时一致性：
 * 读操作读到的数据只是一份快照。所以如果希望写入的数据可以立刻被读到，并不适合。
 *
 * @see java.util.concurrent.CopyOnWriteArrayList
 * @author Sumkor
 * @since 2021/3/3
 */
public class CopyOnWriteArrayListTest {

    /**
     * 失败安全
     * <p>
     * 每次添加元素，都用 ReentrantLock 加锁，并使用 Arrays.copyOf 复制为新数组，长度加一
     * 创建迭代器时，取得是创建当前的数组，作为快照（浅拷贝）。
     * 后续继续往 list 中添加信息，不会影响到 迭代器 中的快照数组。
     * <p>
     * 也就是说，如果此时有其它线程正在修改元素，并不会在迭代中反映出来，因为修改都是在新数组中进行的。
     */
    @Test
    public void failSafe() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        Iterator<String> iterator = list.iterator();
        /**
         * @see CopyOnWriteArrayList#iterator()
         * 此时会传入当前数组，作为快照（浅拷贝）
         * @see CopyOnWriteArrayList.COWIterator#COWIterator(java.lang.Object[], int)
         * 并没有 modCount 变量
         */

        list.add("d"); // 写时复制

        while (iterator.hasNext()) {
            String next = iterator.next();
            System.out.println("next = " + next); // a、b、c，注意并不会打印 d
        }
        /**
         * @see CopyOnWriteArrayList.COWIterator#next()
         * 返回的是快照数组
         * @see CopyOnWriteArrayList.COWIterator#snapshot
         */
    }

    /**
     * CopyOnWriteArrayList 与 ArrayList 中关于 size 的区别：
     * CopyOnWriteArrayList 返回的是底层数组的大小 length
     * ArrayList 返回的是 ArrayList#size 属性，每次加减元素都需要计算该值，并不是底层数组的大小
     */
    @Test
    public void size() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        System.out.println("list.size() = " + list.size());
    }

    /**
     * add 复制前加锁
     * get 从最新的数组读，不用加锁
     */
    @Test
    public void get() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("a");
        String element = list.get(0);
        System.out.println("element = " + element);
    }
}
