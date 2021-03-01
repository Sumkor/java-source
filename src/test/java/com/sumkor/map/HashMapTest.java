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
     * HashMap 的所有 collection 视图方法所返回的迭代器都是快速失败的：
     * 在迭代器创建之后，如果从结构上对映射进行修改，除非通过迭代器本身的 remove 方法，其他任何时间任何方式的修改，迭代器都将抛出 ConcurrentModificationException。
     * 因此，面对并发的修改，迭代器很快就会完全失败，而不冒在将来不确定的时间发生任意不确定行为的风险。
     */
    @Test(expected = ConcurrentModificationException.class)
    public void failFast01() {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();// 创建迭代器
        /**
         * @see HashMap.EntrySet#iterator()
         * 这里创建了 {@link HashMap.EntryIterator} 对象。
         * 由于 EntryIterator extends HashIterator，因此执行 HashIterator 构造函数
         * @see HashMap.HashIterator#HashIterator()
         * 将 modCount 赋值给 expectedModCount
         */

        map.put("3", "c");// 改变结构。强调一点，内部结构发生变化指的是结构发生变化，如put新键值对。而某个key对应的value值被覆盖不属于结构变化。
        System.out.println("map = " + map);

        iterator.next();// 快速失败，抛异常ConcurrentModificationException
        /**
         * @see HashMap.EntryIterator#next()
         * @see HashMap.HashIterator#nextNode()
         */
    }

    @Test
    public void failFast02() {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);

        Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator();// 创建迭代器

        iterator.next();
        iterator.remove();// 这里改变结构不会导致快速失败
        /**
         * @see HashMap.HashIterator#remove()
         * 其中执行 expectedModCount = modCount 对两者的值进行同步
         */
        System.out.println("map = " + map);

        iterator.next();// 正常
    }

    @Test(expected = ConcurrentModificationException.class)
    public void failFast03() {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        System.out.println("map = " + map);
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            map.put("3", "c");
        }
        /**
         * 等价于：
         *         Iterator var2 = map.entrySet().iterator();
         *         while(var2.hasNext()) {
         *             Entry<Object, Object> entry = (Entry)var2.next();
         *             map.put("3", "c");
         *         }
         */
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

    @Test
    public void mod() {
        System.out.println(Integer.toBinaryString(4));// 100
        System.out.println(Integer.toBinaryString(3));// 11
        System.out.println(Integer.toBinaryString(7));// 111
        System.out.println(7 % 4);// 3
        System.out.println(7 & 3);// 3
        System.out.println();

        /**
         * 整数除法取整
         *
         * 考虑这样一个计算题：18 除以 5，要得到一个整数结果，究竟应该是 3 还是 4？这就是一个问题了。计算机上有几种对于结果取整的方法：
         *
         *     向上取整，向+∞方向取最接近精确值的整数，也就是取比实际结果稍大的最小整数，也叫 Ceiling 取整。这种取整方式下，17 / 10 == 2，5 / 2 == 3, -9 / 4 == -2。
         *     向下取整，向-∞方向取最接近精确值的整数，也就是取比实际结果稍小的最大整数，也叫 Floor 取整。这种取整方式下，17 / 10 == 1，5 / 2 == 2, -9 / 4 == -3。
         *     向零取整，向0方向取最接近精确值的整数，换言之就是舍去小数部分，因此又称截断取整（Truncate）。这种取整方式下，17 / 10 == 1，5 / 2 == 2, -9 / 4 == -2。
         *
         * 作者：丰俊文
         * 链接：https://www.jianshu.com/p/452c1a5acd31
         * 来源：简书
         * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
         *
         *
         * 取模运算实际上是计算两数相除以后的余数。假设 q 是 a、b 相除产生的商(quotient)，r 是相应的余数(remainder)，
         * 那么在几乎所有的计算系统中，都满足：
         * a = b x q + r，其中 |r|<|a|
         * r = a - (a / b) x b
         */

        /**
         * 例子：
         * Truncate 法：
         * r = (-7)-(-7/4)x4 = (-7)-(-1)x4 = -3
         * Ceiling 法：
         * r = (-7)-(-7/4)x4 = (-7)-(-1)x4 = -3
         * Floor 法：
         * r = (-7)-(-7/4)x4 = (-7)-(-2)x4 = 1
         */
        System.out.println(-7 % 4);// -3
        System.out.println(-7 & 3);// 1
        System.out.println();

        /**
         * 例子：
         * Truncate 法：
         * r = (-9)-(-9/4)x4 = (-9)-(-2)x4 = -1
         * Ceiling 法：
         * r = (-9)-(-9/4)x4 = (-9)-(-2)x4 = -1
         * Floor 法：
         * r = (-9)-(-9/4)x4 = (-9)-(-3)x4 = 3
         */
        System.out.println(-9 % 4);// -1
        System.out.println(-9 & 3);// 3
        System.out.println();

        /**
         * 当被除数为 2^n 时，java 中的除法和取余如何用位运算代替
         */
        System.out.println(Integer.toBinaryString(9));// 1001
        System.out.println(9 / 4);// 2 除数
        System.out.println(9 % 4);// 1 余数
        System.out.println(9 >>> 2);// 9 >> 2^n = 9 >> n = 2
        System.out.println(9 & 3);// 9 & (2^n - 1) = 9 & 3 = 1
        System.out.println(Integer.toBinaryString(-9));// 1111 1111 1111 1111  1111 1111 1111 0111 (补码=原码的反码+1)
        System.out.println(-9 / 4);// -2
        System.out.println(-9 % 4);// -1
        System.out.println(-9 >> 2);// -3
        System.out.println(-9 & 3);// 3
        System.out.println();

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
     * <p>
     * 新的 Entry 节点在插入链表的时候，是怎么插入的？
     * java7 用的是头插法，就是说新来的值会取代原有的值的位置，原有的值就顺推到链表中去，因为写这个代码的作者认为后来的值被查找的可能性更大一点，提升查找的效率。
     * 但是，在 java8 之后，都是所用尾部插入了。
     * <p>
     * 为啥 java7 用头插法，java8 之后改成尾插了呢？
     * 在 rehash 扩容操作中，旧链表迁移新链表的时候，由于 java7 用头插法，如果在新表的数组索引位置相同，则链表元素会倒置；但是 java8 用的是双指针，rehash 操作后元素不会倒置。
     * 使用头插会改变链表的上的顺序，并发情况下的同时 put 触发 rehash，可能会导致链表中出现环。但是如果使用尾插，在扩容时会保持链表元素原本的顺序，就不会出现链表成环的问题了。
     * https://www.cnblogs.com/aobing/p/12014271.html
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
            } else {
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
     * <p>
     * 打印结果：
     * 1=A -> 2=B -> 3=C ->
     * --------------------------
     * 2=B ->
     * 1=A -> 3=C ->
     * --------------------------
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
                        loTail.next = e; // 把loTail.next指向e。
                    loTail = e;
                } else {
                    if (hiTail == null)
                        hiHead = e;
                    else
                        hiTail.next = e; // 把hiTail.next指向e。若hiTail.next原先并不指向e，该操作会改变oldTable[j]上的旧链表结构
                    hiTail = e; // 把hiTail指向e所指向的节点，此时hiTail.next指向e.next相同的节点
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
