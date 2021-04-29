package com.sumkor.lock;

import java.util.concurrent.Exchanger;

/**
 * @author Sumkor
 * @since 2021/4/29
 */
public class ExchangerTest {

    /**
     * 消息
     */
    static class Message {

        String value;

        public Message(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 生产者
     */
    static class Producer implements Runnable {
        private final Exchanger<Message> exchanger;

        public Producer(Exchanger<Message> exchanger) {
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            Message message = new Message(null);
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);
                    message.setValue(String.valueOf(i));
                    System.out.println(Thread.currentThread().getName() + ": 生产了数据[" + i + "]");
                    message = exchanger.exchange(message);
                    System.out.println(Thread.currentThread().getName() + ": 交换得到数据[" + message.getValue() + "]");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 消费者
     */
    static class Consumer implements Runnable {
        private final Exchanger<Message> exchanger;

        public Consumer(Exchanger<Message> exchanger) {
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            Message message = new Message(null);
            while (true) {
                try {
                    Thread.sleep(1000);
                    message = exchanger.exchange(message);
                    System.out.println(Thread.currentThread().getName() + ": 消费了数据[" + message.getValue() + "]");
                    message.setValue(null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 利用 Exchanger 实现生产者-消息者模式：
     * https://segmentfault.com/a/1190000015963932
     */
    public static void main(String[] args) {
        Exchanger<Message> exchanger = new Exchanger<>();
        Thread t1 = new Thread(new Consumer(exchanger), "消费者-t1");
        Thread t2 = new Thread(new Producer(exchanger), "生产者-t2");

        t1.start();
        t2.start();
        /**
         * 执行结果：
         *
         * 生产者-t2: 生产了数据[0]
         * 消费者-t1: 消费了数据[0]
         * 生产者-t2: 交换得到数据[null]
         * 生产者-t2: 生产了数据[1]
         * 生产者-t2: 交换得到数据[null]
         * 消费者-t1: 消费了数据[1]
         * 生产者-t2: 生产了数据[2]
         * 消费者-t1: 消费了数据[2]
         * 生产者-t2: 交换得到数据[null]
         *
         * 说明：
         *
         * 上述示例中，生产者生产了 3 个数据：0、1、2。通过 Exchanger 与消费者进行交换。
         * 可以看到，消费者消费完后会将空的 Message 交换给生产者。
         */
    }
}
