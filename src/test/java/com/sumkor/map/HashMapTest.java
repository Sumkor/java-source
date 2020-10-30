package com.sumkor.map;

import org.junit.Test;

import java.util.*;

/**
 * HashMap 源码注释翻译
 * https://cloud.tencent.com/developer/article/1580487
 * <p>
 * Java 8系列之重新认识HashMap
 * https://zhuanlan.zhihu.com/p/21673805
 * <p>
 * (1) HashMap：它根据键的hashCode值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度，但遍历顺序却是不确定的。
 * HashMap最多只允许一条记录的键为null，允许多条记录的值为null。HashMap非线程安全，即任一时刻可以有多个线程同时写HashMap，可能会导致数据的不一致。
 * 如果需要满足线程安全，可以用 Collections的synchronizedMap方法使HashMap具有线程安全的能力，或者使用ConcurrentHashMap。
 * (2) Hashtable：Hashtable是遗留类，很多映射的常用功能与HashMap类似，不同的是它承自Dictionary类，并且是线程安全的，任一时间只有一个线程能写Hashtable，并发性不如ConcurrentHashMap，
 * 因为ConcurrentHashMap引入了分段锁。Hashtable不建议在新代码中使用，不需要线程安全的场合可以用HashMap替换，需要线程安全的场合可以用ConcurrentHashMap替换。
 * (3) LinkedHashMap：LinkedHashMap是HashMap的一个子类，保存了记录的插入顺序，在用Iterator遍历LinkedHashMap时，先得到的记录肯定是先插入的，也可以在构造时带参数，按照访问次序排序。
 * (4) TreeMap：TreeMap实现SortedMap接口，能够把它保存的记录根据键排序，默认是按键值的升序排序，也可以指定排序的比较器，
 * 当用Iterator遍历TreeMap时，得到的记录是排过序的。如果使用排序的映射，建议使用TreeMap。
 * 在使用TreeMap时，key必须实现Comparable接口或者在构造TreeMap传入自定义的Comparator，否则会在运行时抛出java.lang.ClassCastException类型的异常。
 * <p>
 * 对于上述四种Map类型的类，要求映射中的key是不可变对象。不可变对象是该对象在创建后它的哈希值不会被改变。如果对象的哈希值发生变化，Map对象很可能就定位不到映射的位置了。
 * <p>
 * 从结构实现来讲，HashMap是数组+链表+红黑树（JDK1.8增加了红黑树部分）实现的。
 *
 * @author Sumkor
 * @since 2020/10/21
 */
public class HashMapTest {

    /**
     * 由所有此类的 collection 视图方法所返回的迭代器都是快速失败的：
     * 在迭代器创建之后，如果从结构上对映射进行修改，除非通过迭代器本身的 remove 方法，其他任何时间任何方式的修改，迭代器都将抛出 ConcurrentModificationException。
     * 因此，面对并发的修改，迭代器很快就会完全失败，而不冒在将来不确定的时间发生任意不确定行为的风险。
     */
    @Test(expected = ConcurrentModificationException.class)
    public void fastFail01() {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();// 创建迭代器

        map.put("3", "c");// 改变结构。强调一点，内部结构发生变化指的是结构发生变化，如put新键值对。而某个key对应的value值被覆盖不属于结构变化。
        System.out.println("map = " + map);

        iterator.next();// 快速失败，抛异常ConcurrentModificationException
    }

    @Test
    public void fastFail02() {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();// 创建迭代器

        iterator.next();
        iterator.remove();// 这里改变结构不会导致快速失败
        System.out.println("map = " + map);

        iterator.next();// 正常
    }

    /**
     * 哈希桶数组索引位置的计算
     * <p>
     * 这里的 Hash 算法本质上就是三步：
     * 取 key 的 hashCode 值、高位运算、取模运算。
     * <p>
     * 一般是把 capacity 设计为素数，相对来说素数导致冲突的概率要小于合数。采用合数，主要是为了在取模和扩容时做优化，同时为了减少冲突。
     * 当 capacity 总是 2 的 n 次方时，h & (capacity-1) 运算等价于对 capacity 取模，也就是 h % capacity，但是 & 比 % 具有更高的效率
     * <p>
     * 为什么一般hashtable的桶数会取一个素数
     * https://blog.csdn.net/liuqiyao_01/article/details/14475159
     */
    @Test
    public void hash() {
        int capacity = 1 << 4;// 值为16。见 java.util.HashMap.DEFAULT_INITIAL_CAPACITY
        Object key = 17;

        int h = key.hashCode();// 第一步取hashCode值
        h = h ^ (h >>> 16);// 第二步高位参与运算
        h = h & (capacity - 1);// 第三步取模运算
        /**
         * @see HashMap#hash(java.lang.Object)
         */

        System.out.println("h = " + h);
    }

    /**
     * JDK7阈值计算
     */
    @Test
    public void threshold_jdk7() {
        int initialCapacity = 3;
        float loadFactor = 0.75f;

        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;
        System.out.println("capacity = " + capacity);// 4

        int threshold = (int) (capacity * loadFactor);
        System.out.println("threshold = " + threshold);// 3
    }


    /**
     * JDK8阈值计算：
     * threshold = tableSizeFor(initialCapacity)
     * capacity = threshold
     * threshold = capacity * Load factor
     * <p>
     * 默认的负载因子0.75是对空间和时间效率的一个平衡选择。
     * 如果内存空间很多而又对时间效率要求很高，可以降低负载因子Load factor的值；
     * 相反，如果内存空间紧张而对时间效率要求不高，可以增加负载因子loadFactor的值，这个值可以大于1。
     */
    @Test
    public void threshold_jdk8() {
        int initialCapacity = 3;
        float loadFactor = 0.75f;

        /**
         * @see HashMap#HashMap(int, float)
         * @see HashMap#tableSizeFor(int)
         */
        int threshold = tableSizeFor(initialCapacity);

        /**
         * @see HashMap#resize()
         */
        int capacity = threshold;
        System.out.println("capacity = " + capacity);// 4

        threshold = (int) (capacity * loadFactor);
        System.out.println("threshold = " + threshold);// 3
    }

    /**
     * 找到大于等于 cap 的最小的 2 的幂（cap 如果就是 2 的幂，则返回的还是这个数）
     * <p>
     * HashMap中tableSizeFor
     * https://www.cnblogs.com/shujiying/p/12460808.html
     */
    private int tableSizeFor(int cap) {
        final int MAXIMUM_CAPACITY = 1 << 30;
        int n = cap - 1;// 为什么要对cap做减1操作：如果cap已经是2的幂，又没有执行这个减1操作，则执行完后面的几条无符号右移操作之后，返回的capacity将是这个cap的2倍
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * 注意，table初始大小（即capacity）并不是构造函数中的 initialCapacity
     */
    @Test
    public void resize() {
        HashMap<Object, Object> map = new HashMap<>(3);
        map.put("111", "aaa");
        map.put("222", "bbb");
        map.put("333", "ccc");
        map.put("444", "ddd");
    }

    /**
     * 旧链表数据迁移至新链表
     * 本例中，扩容前后桶的个数相同，均为1
     */
    @Test
    public void resizeLink() {
        Node[] oldTable = new Node[1];
        Node[] newTable = new Node[1];

        // A -> B -> C
        Node firstLinkNode03 = new Node(1, 3, "C", null);
        Node firstLinkNode02 = new Node(1, 2, "B", firstLinkNode03);
        Node firstLinkNode01 = new Node(1, 1, "A", firstLinkNode02);
        oldTable[0] = firstLinkNode01;
        // print
        printTable(oldTable);

        /**
         * HashMap中resize迁移算法，简化
         * @see HashMap#resize()
         */
        Node loHead = null, loTail = null; // low位链表，其桶位置不变，head和tail分别代表首尾指针
        Node e = oldTable[0];// 将要处理的元素
        Node next;
        do {
            next = e.next;
            if (loTail == null) {
                loHead = e; // 总是指向头结点
            }
            else {
                loTail.next = e; // 本例中，赋值前就是相同的：即loTail.next就是指向e
            }
            loTail = e; // loTail和e两者指向的节点同步
        } while ((e = next) != null); // 这里e指向下一个节点，但是loTail还是指向e原来的节点，没有变。当e指向null时，说明loTail已经指向尾节点
        if (loTail != null) {
            loTail.next = null;
            newTable[0] = loHead; // 原索引位置。本例中，以loHead为首节点的链表结构，没有变，即还是 A -> B -> C
        }
        // print
        printTable(newTable);
    }

    /**
     * 旧链表数据迁移至新链表
     * 由于每次扩容是2次幂的扩展(指数组长度/桶数量扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。
     * 本例中，桶的数量由1扩容为2.
     * <p>
     * HashMap扩容时的rehash方法中(e.hash & oldCap) == 0算法推导
     * https://blog.csdn.net/u010425839/article/details/106620440/
     */
    @Test
    public void resizeLink02() {
        int oldCap = 1;
        int newCap = 2;
        Node[] oldTable = new Node[oldCap];
        Node[] newTable = new Node[newCap];

        // A -> B -> C
        Node firstLinkNode03 = new Node(new Integer(3).hashCode(), 3, "C", null);
        Node firstLinkNode02 = new Node(new Integer(2).hashCode(), 2, "B", firstLinkNode03);
        Node firstLinkNode01 = new Node(new Integer(1).hashCode(), 1, "A", firstLinkNode02);
        oldTable[0] = firstLinkNode01;
        // print
        printTable(oldTable);

        /**
         * HashMap中resize迁移算法
         * @see HashMap#resize()
         */
        for (int j = 0; j < oldCap; ++j) {
            Node loHead = null, loTail = null; // low位链表，其桶位置不变，head和tail分别代表首尾指针
            Node hiHead = null, hiTail = null; // high位链表，其桶位于追加后的新数组中
            Node e = oldTable[j];// 将要处理的元素
            Node next;
            do {
                next = e.next;
                if ((e.hash & oldCap) == 0) { // 是0的话索引没变，是1的话索引变成“原索引+oldCap”
                    if (loTail == null)
                        loHead = e; // 总是指向头结点
                    else
                        loTail.next = e;
                    loTail = e;
                }
                else {
                    if (hiTail == null)
                        hiHead = e;
                    else
                        hiTail.next = e; // 把hiTail.next指向e。若hiTail.next原先并不指向e，表示loTail原先后续的节点链都不要了，改为从e位置开始的节点链。该操作会改变原链表oldTable[j]的结构，也会导致hiHead为首节点的链表结构变化！
                    hiTail = e;
                }
            } while ((e = next) != null);
            if (loTail != null) {
                loTail.next = null; // 这一步是必须的，loTail.next有可能还其他节点，需要设为null
                newTable[j] = loHead; // 原索引
            }
            if (hiTail != null) {
                hiTail.next = null;
                newTable[j + oldCap] = hiHead; // 原索引+oldCap
            }
        }
        printTable(newTable);
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

    /**
     * HashMap 中的 Node 结构
     */
    static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final String toString() {
            return key + "=" + value;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

}
