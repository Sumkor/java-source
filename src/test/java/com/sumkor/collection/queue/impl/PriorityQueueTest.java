package com.sumkor.collection.queue.impl;

import org.junit.Test;

import java.util.*;

/**
 * @author Sumkor
 * @since 2021/5/7
 */
public class PriorityQueueTest {

    /**
     * 默认是小顶堆
     */
    @Test
    public void test() {
        PriorityQueue<String> priorityQueue = new PriorityQueue<>();
        priorityQueue.add("5");
        priorityQueue.add("2");
        priorityQueue.add("1");
        priorityQueue.add("4");
        priorityQueue.add("9");
        priorityQueue.add("3");

        while (!priorityQueue.isEmpty()) {
            String poll = priorityQueue.poll();
            System.out.print(poll + " ");
        }
        // 1 2 3 4 5 9
    }

    /**
     * PriorityQueue 默认是小顶堆，改为大顶堆
     */
    @Test
    public void priority() {
        int[] nums = new int[]{1, 3, -1};
        int n = nums.length;
        PriorityQueue<Integer> queue = new PriorityQueue<>(n, (a, b) -> b - a);
        /**
         * Comparator
         *
         * a > b return -1
         * a = b return 0
         * a < b return 1
         */
        for (int num : nums) {
            queue.add(num);
        }
        Integer peek = queue.peek();
        System.out.println("peek = " + peek);
    }

    /**
     * https://blog.csdn.net/u013066244/article/details/78997869
     * Comparator 有两个参数 a 和 b，代表两笔先后数据，那么比较两笔数据：
     *
     * 如果要【升序】，则规定：
     * a < b return -1 // 不会交换 a 和 b
     * a = b return 0  // 当 a 与 b 相等时：如果返回 1，不会交换；如果返回 -1，会交换。
     * a > b return 1  // 交换 a 和 b
     *
     * 如果要【降序】就必须完全相反：
     * a < b return 1
     * a = b return 0
     * a > b return -1
     *
     * 如果要【倒序】，则比较结果永远返回 -1：
     * a < b return -1
     * a = b return -1
     * a > b return -1
     *
     * 假设不管大于、小于、等于，我们都返回 0，会发现顺序没有变；而且要是都返回 1 的话，顺序也是没有变的。都是不排序！
     * 其实 0 表示的是，相同元素不排序，要是我们把 a = b 返回为 -1，那么两个相同的元素会交换顺序；
     * 对数字而言交换顺序没有关系，但是里面要是 Map 对象的话，那就有关系，因为有时我们是希望相同元素不进行顺序调整的。
     *
     * ！！！一句话，返回负数，第一个参数放前面。
     */
    @Test
    public void comparator() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(6);
        list.add(5);
        list.add(8);
        list.add(8);
        list.add(4);
        // 升序（默认）
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if (a < b) {
                    return -1; // 返回负数，则第一个参数 a 放前面。因此是升序
                } else if (a > b) {
                    return 1;
                }
                return 0;
            }
        });
        System.out.println(list);

        // 降序
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if (a < b) {
                    return 1;
                } else if (a > b) {
                    return -1; // 返回负数，则第一个参数 a 放前面。因此是降序
                }
                return 0;
            }
        });
        System.out.println(list);

        // 倒序（基于上一步的结果）
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return -1;
            }
        });
        System.out.println(list);
    }

    /**
     * Comparator的用法
     * https://blog.csdn.net/u012250875/article/details/55126531
     */
}
