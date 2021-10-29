package com.sumkor.collection.queue.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Sumkor
 * @since 2021/5/11
 */
public class DelayQueueTest {

    /**
     * 延迟队列提供了在指定时间才能获取队列元素的功能，队列头元素是最接近过期的元素。
     * 没有过期元素的话，使用 poll() 方法会返回 null 值，超时判定是通过 getDelay(TimeUnit.NANOSECONDS) 方法的返回值小于等于 0 来判断。
     * 延时队列不能存放空元素。
     * https://www.cnblogs.com/hhan/p/10678466.html
     */
    public static void main(String[] args) throws InterruptedException {
        Item item1 = new Item("item1", 5, TimeUnit.SECONDS);
        Item item2 = new Item("item2", 10, TimeUnit.SECONDS);
        Item item3 = new Item("item3", 15, TimeUnit.SECONDS);
        DelayQueue<Item> queue = new DelayQueue<>();
        queue.put(item1);
        queue.put(item2);
        queue.put(item3);
        System.out.println("begin time:" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        for (int i = 0; i < 3; i++) {
            Item take = queue.take();
            System.out.format("name:{%s}, time:{%s}\n", take.name, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

    static class Item implements Delayed {
        /* 触发时间*/
        long time;
        String name;

        public Item(String name, long time, TimeUnit unit) {
            this.name = name;
            this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return time - System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed o) {
            Item item = (Item) o;
            return Long.compare(this.time, item.time);
        }

        @Override
        public String toString() {
            return "Item {" + "time=" + time + ", name='" + name + '\'' + '}';
        }
    }
}
