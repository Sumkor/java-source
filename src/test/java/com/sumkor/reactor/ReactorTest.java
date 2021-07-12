package com.sumkor.reactor;

import org.junit.Test;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author Sumkor
 * @since 2021/7/12
 */
public class ReactorTest {

    /**
     * Mono同步执行任务
     */
    @Test
    public void test() throws InterruptedException {
        System.out.println(Thread.currentThread().getId());

        Mono<Integer> monoJust = Mono.just(10);
        Mono<Integer> monoMap = monoJust.map(t -> t + 1);
        monoMap.subscribe(t -> {
            System.out.println("t = " + t);
            System.out.println(Thread.currentThread().getId());
        });
        Thread.sleep(5000);
    }

    /**
     * Mono实现异步执行任务的原理，就是把任务丢给线程池执行
     */
    @Test
    public void delay() throws InterruptedException {
        System.out.println("Thread.currentThread().getId() = " + Thread.currentThread().getId());
        Mono<Integer> monoJust = Mono.just(10);
        Mono<Integer> monoDelay = monoJust.delayElement(Duration.ofSeconds(1));
        monoDelay.subscribe(new BaseSubscriber<Integer>() {
                                @Override
                                protected void hookOnNext(Integer value) {
                                    System.out.println("Thread.currentThread().getId() = " + Thread.currentThread().getId());
                                    System.out.println("value = " + value);
                                }
                            }
        );
        Thread.sleep(5000);
        /**
         * 由于调用了 Mono 对象的 just() 方法，会构建 MonoJust 对象。
         * 由于调用了 Mono 对象的 delayElement() 方法，会构建 MonoDelayElement 对象。
         *
         * 入口，为 MonoDelayElement#subscribe 方法
         * @see reactor.core.publisher.InternalMonoOperator#subscribe(reactor.core.CoreSubscriber)
         *
         * 其中会进行溯源，先调用父组件（MonoJust）的 subscribe 方法，并将当前组件作为父组件的 Subscriber，即 DelayElementSubscriber 实例
         * @see reactor.core.publisher.MonoDelayElement#subscribeOrReturn(reactor.core.CoreSubscriber)
         *
         * 首先执行 MonoJust#subscribe 方法，入参为 DelayElementSubscriber
         * @see reactor.core.publisher.MonoJust#subscribe(reactor.core.CoreSubscriber)
         *
         * 触发 DelayElementSubscriber#onSubscribe 方法
         * @see reactor.core.publisher.MonoDelayElement.DelayElementSubscriber#onSubscribe(org.reactivestreams.Subscription)
         * @see reactor.core.publisher.Operators.ScalarSubscription#request(long)
         *
         * 接着触发 DelayElementSubscriber#onNext 方法
         * @see reactor.core.publisher.MonoDelayElement.DelayElementSubscriber#onNext(Object)
         * @see reactor.core.scheduler.Schedulers.CachedScheduler#schedule(Runnable, long, java.util.concurrent.TimeUnit)
         * @see reactor.core.scheduler.ParallelScheduler#schedule(Runnable, long, java.util.concurrent.TimeUnit)
         *
         * 实际是把 Runnable 任务封装为 SchedulerTask，丢给线程池 ScheduledThreadPoolExecutor 执行！！！
         * @see reactor.core.scheduler.Schedulers#directSchedule(java.util.concurrent.ScheduledExecutorService, Runnable, reactor.core.Disposable, long, java.util.concurrent.TimeUnit)
         * @see java.util.concurrent.ScheduledThreadPoolExecutor#schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit)
         *
         * 线程池中的线程执行任务，也就是说，FutureTask 中的任务为 SchedulerTask
         * @see java.util.concurrent.FutureTask#run()
         * @see reactor.core.scheduler.SchedulerTask#call()
         */
    }

    /**
     * Flux如何对任务执行结果进行分发
     */
    @Test
    public void onError() throws InterruptedException {
        System.out.println("Thread.currentThread().getId() = " + Thread.currentThread().getId());
//        Mono<Integer> monoJust = Mono.just(10);
        Mono<Integer> monoJust = Mono.just(5);
        monoJust.delayElement(Duration.ofSeconds(1))
                .map(t -> {
                    if (t == 10) {
                        throw new RuntimeException("报错了!");
                    } else {
                        return t;
                    }
                })
                .subscribe(
                        (consumer) -> {
                            System.out.println("consumer = " + consumer);
                        },
                        (errorConsumer) -> {
                            System.out.println("consumer = " + errorConsumer);
                        });
        Thread.sleep(10000);
        /**
         * 将任务传递给线程池运行
         * @see reactor.core.publisher.MonoDelayElement.DelayElementSubscriber#onNext(Object)
         *
         * 关注任务的执行
         * @see java.util.concurrent.FutureTask#run()
         * @see reactor.core.scheduler.SchedulerTask#call()
         * 实际是执行 {@link reactor.core.publisher.MonoDelayElement.DelayElementSubscriber#onNext(Object)} 中配置的 lambda 表达式：() -> complete(t)
         * 进入
         * @see reactor.core.publisher.Operators.MonoSubscriber#complete(Object)
         * @see reactor.core.publisher.FluxMap.MapSubscriber#onNext(Object)
         * 这里若执行成功，会继续进入 onNext，执行用户代码中配置的 lambda；若执行失败，则进入 onError
         * @see reactor.core.publisher.LambdaMonoSubscriber#onNext(java.lang.Object)
         */
    }
}
