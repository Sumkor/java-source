package com.sumkor.reactor;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Sumkor
 * @since 2021/7/11
 */
public class MonoTest {

    /**
     * 在Reactor中，有两个非常重要的类，就是Mono和Flux。
     * 它们都是数据源，在它们内部都已经实现了“以数据为主线”和“在变化时通知处理者”这两个功能，而且还提供了方法让我们来插入逻辑代码用于“对变化做出反应”。
     * Mono表示0个或1个数据，Flux表示0到多个数据。
     *
     * 先从简单的Mono开始。
     * 设计一个简单的示例，首先创建一个数据源，只包含一个数据10，第一个处理就是加1，第二个处理就是奇偶性过滤，第三个处理就是把这个数据消费掉，然后就结束了。
     * https://www.cnblogs.com/lixinjie/p/step-into-reactive-programing-in-an-hour.html
     */
    public static void main(String[] args) {
        displayCurrStep(1, null);
        //创建一个数据源
        Mono.just(10)
                //延迟5秒再发射数据
                .delayElement(Duration.ofSeconds(5))
                //在数据上执行一个转换
                .map(n -> {
                    displayCurrStep(2, n);
                    delaySeconds(2);
                    return n + 1;
                })
                //在数据上执行一个过滤
                .filter(n -> {
                    displayCurrStep(3, n);
                    delaySeconds(3);
                    return n % 2 == 0;
                })
                //如果数据没了就用默认值
                .defaultIfEmpty(9)
                //订阅一个消费者把数据消费了
                .subscribe(n -> {
                    displayCurrStep(4, n);
                    delaySeconds(2);
                    System.out.println(n + " consumed, worker Thread over, exit.");
                });
        displayCurrStep(5, null);
        pause();
        /**
         * 执行结果：
         *
         * Step:1; threadId:1; time:00:05:00.641; value:null
         * Step:5; threadId:1; time:00:05:00.858; value:null
         * main Thread over, paused.
         * Step:2; threadId:12; time:00:05:05.865; value:10
         * Step:3; threadId:12; time:00:05:07.873; value:11
         * Step:4; threadId:12; time:00:05:10.879; value:9
         * 9 consumed, worker Thread over, exit.
         *
         * 可以看到不到1秒钟时间主线程就执行完了。然后5秒后数据从数据源发射出来进入第一步处理，2秒后进入第二步处理，3秒后进入第三步处理，数据被消费掉，就结束了。其中主线程Id是1，工作线程Id是12。
         * 这段代码其实是建立了一个数据通道，在通道的指定位置上插入处理逻辑，等待数据到来。
         * 主线程执行的是建立通道的代码，主线程很快执行完，通道就建好了。此时只是一个空的通道，根本就没有数据。
         * 在数据到来时，由工作线程执行每个节点的逻辑代码来处理数据，然后把数据传入下一个节点，如此反复直至结束。
         * 所以，在写响应式代码的时候，心里一定要默念着，我所做的事情就是建立一条数据通道，在通道上指定的位置插入适合的逻辑处理代码。同时还要切记，主线程执行完时，只是建立了通道，并没有数据。
         */
    }

    //显示当前时间、当前线程Id、数值
    static void displayCurrStep(int step, Integer value) {
        System.out.println("Step:" + step + "; threadId:" + Thread.currentThread().getId() + "; time:" + LocalTime.now() + "; value:" + value);
    }

    //延迟若干秒
    static void delaySeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //主线程暂停
    static void pause() {
        try {
            System.out.println("main Thread over, paused.");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
