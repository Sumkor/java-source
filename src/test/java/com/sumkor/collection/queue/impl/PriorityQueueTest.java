package com.sumkor.collection.queue.impl;

import org.junit.Test;

import java.util.*;

/**
 * @author Sumkor
 * @since 2021/5/7
 */
public class PriorityQueueTest {

    /**
     * 自然排序
     */
    @Test
    public void test() {
        PriorityQueue<String> priorityQueue = new PriorityQueue<>();
        priorityQueue.add("b");
        priorityQueue.add("c");
        priorityQueue.add("a");
        priorityQueue.add("1");
        priorityQueue.add("2");
        priorityQueue.add("3");
        System.out.println("priorityQueue = " + priorityQueue); // [1, 2, 3, c, a, b]
    }

    class Apple {
        public String color;
        public int weight;

        public Apple(String color, int weight) {
            super();
            this.color = color;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Apple [color=" + color + ", weight=" + weight + "]";
        }
    }

    /**
     * Comparator的用法
     * https://blog.csdn.net/u012250875/article/details/55126531
     *
     * 按重量排序
     */
    @Test
    public void sort() {
        List<Apple> list = new ArrayList<>();
        list.add(new Apple("红", 205));
        list.add(new Apple("红", 131));
        list.add(new Apple("绿", 248));
        list.add(new Apple("绿", 153));
        list.add(new Apple("黄", 119));
        list.add(new Apple("黄", 224));

        /**
         * @see List#sort(java.util.Comparator)
         * @see Arrays#sort(java.lang.Object[], java.util.Comparator)
         */
        Collections.sort(list, new Comparator<Apple>() {
            @Override
            public int compare(Apple o1, Apple o2) {
//                return o2.weight - o1.weight; // 重量从大到小
                return o1.weight - o2.weight; // 重量从小到大
                /**
                 * compare 返回值
                 *
                 * 负数：第一个数 小于 第二个数
                 * 为零：第一个数 相等 第二个数
                 * 正数：第一个数 大于 第二个数
                 */
            }
        });

        for (Apple apple : list) {
            System.out.println(apple);
        }
    }

    @Test
    public void priority() {
        PriorityQueue<Apple> priorityQueue = new PriorityQueue<>(new Comparator<Apple>() {
            @Override
            public int compare(Apple o1, Apple o2) {
//                return o2.weight - o1.weight; // 重量从大到小
                return o1.weight - o2.weight; // 重量从小到大
            }
        });
        priorityQueue.add(new Apple("红", 205));
        priorityQueue.add(new Apple("红", 131));
        priorityQueue.add(new Apple("绿", 248));
        priorityQueue.add(new Apple("绿", 153));
        priorityQueue.add(new Apple("黄", 119));
        priorityQueue.add(new Apple("黄", 224));

        while (!priorityQueue.isEmpty()) {
            System.out.println(priorityQueue.poll());
        }
    }

    /**
     * 按颜色分组
     */
    @Test
    public void group() {
        List<Apple> list = new ArrayList<>();
        list.add(new Apple("红", 205));
        list.add(new Apple("红", 131));
        list.add(new Apple("绿", 248));
        list.add(new Apple("绿", 153));
        list.add(new Apple("黄", 119));
        list.add(new Apple("黄", 224));

        // 按颜色分组
        Map<String, List<Apple>> result = new HashMap<>();
        for (Apple apple : list) {
            List<Apple> apples = result.get(apple.color);
            if (apples == null) {
                apples = new ArrayList<>();
                result.put(apple.color, apples);
            }
            if (!apples.contains(apple)) {
                apples.add(apple);
            }
        }
        for (Map.Entry<String, List<Apple>> entry : result.entrySet()) {
            System.out.println(entry.getValue());
        }
        /**
         * [Apple [color=红, weight=205], Apple [color=红, weight=131]]
         * [Apple [color=黄, weight=119], Apple [color=黄, weight=224]]
         * [Apple [color=绿, weight=248], Apple [color=绿, weight=153]]
         */
    }
}
