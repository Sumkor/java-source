package com.sumkor.pool;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;

/**
 * 使用 CompletableFuture
 * https://www.liaoxuefeng.com/wiki/1252599548343744/1306581182447650
 * <p>
 * 特点：异步回调、串行执行、并行执行
 *
 * @author Sumkor
 * @since 2021/1/13
 */
public class CompletableFutureTest {

    /**
     * 异步回调机制，优点：
     * <p>
     * 异步任务结束时，会自动回调某个对象的方法；
     * 异步任务出错时，会自动回调某个对象的方法；
     * 主线程设置好回调后，不再关心异步任务的执行。
     */
    @Test
    public void callback() throws Exception {
        // 创建异步执行任务:
        CompletableFuture<Double> cf = CompletableFuture.supplyAsync(CompletableFutureTest::fetchPrice);
        // 如果执行成功:
        cf.thenAccept((result) -> {
            System.out.println("price: " + result);
        });
        // 如果执行异常:
        cf.exceptionally((e) -> {
            e.printStackTrace();
            return null;
        });

//        cf.complete(10d);// 结束future任务，并显式设置结果

//        Double aDouble = cf.get();// 阻塞直到获得值
//        System.out.println("aDouble = " + aDouble);

        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:
        Thread.sleep(2000);
    }

    /**
     * 串行执行
     * 定义两个 CompletableFuture，第一个 CompletableFuture 根据证券名称查询证券代码，第二个 CompletableFuture 根据证券代码查询证券价格，这两个 CompletableFuture 实现串行操作
     */
    @Test
    public void serialExec() throws Exception {
        // 第一个任务:
        CompletableFuture<String> cfQuery = CompletableFuture.supplyAsync(() -> {
            return queryCode("中国石油");
        });
        // cfQuery成功后继续执行下一个任务:
        CompletableFuture<Double> cfFetch = cfQuery.thenApplyAsync((code) -> {
            return fetchPrice(code);
        });
        // cfFetch成功后打印结果:
        cfFetch.thenAccept((result) -> {
            System.out.println("price: " + result);
        });
        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:
        Thread.sleep(2000);
    }

    /**
     * 并行执行
     * 同时从新浪和网易查询证券代码，只要任意一个返回结果，就进行下一步查询价格，查询价格也同时从新浪和网易查询，只要任意一个返回结果，就完成操作：
     */
    @Test
    public void parallelExec() throws Exception {
        // 两个CompletableFuture执行异步查询:
        CompletableFuture<String> cfQueryFromSina = CompletableFuture.supplyAsync(() -> {
            return queryCode("中国石油", "https://finance.sina.com.cn/code/");
        });
        CompletableFuture<String> cfQueryFrom163 = CompletableFuture.supplyAsync(() -> {
            return queryCode("中国石油", "https://money.163.com/code/");
        });

        // 用anyOf合并为一个新的CompletableFuture:
        CompletableFuture<Object> cfQuery = CompletableFuture.anyOf(cfQueryFromSina, cfQueryFrom163);

        // 两个CompletableFuture执行异步查询:
        CompletableFuture<Double> cfFetchFromSina = cfQuery.thenApplyAsync((code) -> {
            return fetchPrice((String) code, "https://finance.sina.com.cn/price/");
        });
        CompletableFuture<Double> cfFetchFrom163 = cfQuery.thenApplyAsync((code) -> {
            return fetchPrice((String) code, "https://money.163.com/price/");
        });

        // 用anyOf合并为一个新的CompletableFuture:
        CompletableFuture<Object> cfFetch = CompletableFuture.anyOf(cfFetchFromSina, cfFetchFrom163);

        // 最终结果:
        cfFetch.thenAccept((result) -> {
            System.out.println("price: " + result);
        });
        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:
        Thread.sleep(2000);
    }

    static Double fetchPrice() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        if (Math.random() < 0.3) {
            throw new RuntimeException("fetch price failed!");
        }
        return 5 + Math.random() * 20;
    }

    static Double fetchPrice(String code) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        return 5 + Math.random() * 20;
    }

    static Double fetchPrice(String code, String url) {
        System.out.println("query price from " + url + "...");
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
        }
        return 5 + Math.random() * 20;
    }

    static String queryCode(String name) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        return "601857";
    }

    static String queryCode(String name, String url) {
        System.out.println("query code from " + url + "...");
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
        }
        return "601857";
    }

    /**
     * 两个线程共享同一个 CompletableFuture 对象，
     * 线程一调用 CompletableFuture.get，一直阻塞着，
     * 后面线程二调用了 CompletableFuture.complete(Response)，
     * 此时线程一就能拿到 Response 对象。
     * <p>
     * 实际上，dubbo 中的 IO 线程接收到响应的数据包之后，派发给线程池解码，
     * 线程池解码完成后，通过 CompletableFuture.complete 唤醒了一直阻塞着等待响应结果的用户线程。
     */
    @Test
    public void getAndSet() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Thread getThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(Thread.currentThread().getName() + " 获取completableFuture结果开始");
                    String result = completableFuture.get();
                    System.out.println(Thread.currentThread().getName() + " 获取completableFuture结果结束" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread setThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(Thread.currentThread().getName() + " 设置completableFuture结果开始");
                    Thread.sleep(2000L);
                    completableFuture.complete("haha");
                    System.out.println(Thread.currentThread().getName() + " 设置completableFuture结果结束");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            getThread.start();
            setThread.start();
            // thread.join的含义是当前线程需要等待指定线程终止之后才从thread.join返回。简单来说，就是线程没有执行完之前，会一直阻塞在join方法处。
            getThread.join();
            System.out.println(Thread.currentThread().getName() + " 主线程执行结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
