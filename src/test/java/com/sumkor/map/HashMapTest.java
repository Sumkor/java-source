package com.sumkor.map;

import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * HashMap 源码注释翻译
 * https://cloud.tencent.com/developer/article/1580487
 *
 * Java 8系列之重新认识HashMap
 * https://zhuanlan.zhihu.com/p/21673805
 *
 * (1) HashMap：它根据键的hashCode值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度，但遍历顺序却是不确定的。
 * HashMap最多只允许一条记录的键为null，允许多条记录的值为null。HashMap非线程安全，即任一时刻可以有多个线程同时写HashMap，可能会导致数据的不一致。
 * 如果需要满足线程安全，可以用 Collections的synchronizedMap方法使HashMap具有线程安全的能力，或者使用ConcurrentHashMap。
 * (2) Hashtable：Hashtable是遗留类，很多映射的常用功能与HashMap类似，不同的是它承自Dictionary类，并且是线程安全的，任一时间只有一个线程能写Hashtable，并发性不如ConcurrentHashMap，
 * 因为ConcurrentHashMap引入了分段锁。Hashtable不建议在新代码中使用，不需要线程安全的场合可以用HashMap替换，需要线程安全的场合可以用ConcurrentHashMap替换。
 * (3) LinkedHashMap：LinkedHashMap是HashMap的一个子类，保存了记录的插入顺序，在用Iterator遍历LinkedHashMap时，先得到的记录肯定是先插入的，也可以在构造时带参数，按照访问次序排序。
 * (4) TreeMap：TreeMap实现SortedMap接口，能够把它保存的记录根据键排序，默认是按键值的升序排序，也可以指定排序的比较器，
 * 当用Iterator遍历TreeMap时，得到的记录是排过序的。如果使用排序的映射，建议使用TreeMap。
 * 在使用TreeMap时，key必须实现Comparable接口或者在构造TreeMap传入自定义的Comparator，否则会在运行时抛出java.lang.ClassCastException类型的异常。
 *
 * 对于上述四种Map类型的类，要求映射中的key是不可变对象。不可变对象是该对象在创建后它的哈希值不会被改变。如果对象的哈希值发生变化，Map对象很可能就定位不到映射的位置了。
 *
 * 从结构实现来讲，HashMap是数组+链表+红黑树（JDK1.8增加了红黑树部分）实现的。
 *
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

        map.put("3", "c");// 改变结构
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

}
