package com.sumkor.threadlocal;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Sumkor
 * @since 2021/5/10
 */
public class InheritableThreadLocalTest {

    /**
     * 主线程传递给子线程
     */
    @Test
    public void baseUse() throws InterruptedException {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
        threadLocal.set("haha");
        String result = threadLocal.get();
        System.out.println(Thread.currentThread().getName() + " " + result); // haha

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = threadLocal.get();
                System.out.println(Thread.currentThread().getName() + " " + result);
            }
        }, "sub");
        thread.start();
        thread.join();
    }

    // --------------------------------------

    /**
     * 问题复现：主线程中对于本地变量的修改并没有完全引起线程池中的线程本地变量的修改
     * <p>
     * 线程池中是缓存使用过的线程，当线程被重复调用的时候并没有再重新初始化init()线程
     */
    @Test
    public void poolProblem() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        ThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
        inheritableThreadLocal.set("主线程赋值1");

        /**
         * {@link AbstractExecutorService#submit(java.lang.Runnable)}
         * 其中
         * 先对任务进行封装 {@link AbstractExecutorService#newTaskFor(java.lang.Runnable, java.lang.Object)}
         * 再对任务进行执行 {@link ThreadPoolExecutor#execute(java.lang.Runnable)}
         * 其中，通过addWorker()进行线程的创建和添加，这也就是主线程和当前线程池中的线程传递本地变量的地方：
         * {@link ThreadPoolExecutor#addWorker(java.lang.Runnable, boolean)}
         * {@link ThreadFactory#newThread(java.lang.Runnable)}
         * {@link Executors.DefaultThreadFactory#newThread(java.lang.Runnable)}
         * {@link Thread#Thread(java.lang.ThreadGroup, java.lang.Runnable, java.lang.String, long)}
         */
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + inheritableThreadLocal.get());
            }
        });
        Thread.sleep(1000);

        inheritableThreadLocal.set("主线程赋值2");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + inheritableThreadLocal.get());
            }
        });
        Thread.sleep(1000);

        inheritableThreadLocal.set("主线程赋值3");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + inheritableThreadLocal.get());
            }
        });
        Thread.sleep(1000);
        /**
         * 执行结果：
         *
         * pool-1-thread-1 主线程赋值1
         * pool-1-thread-2 主线程赋值2
         * pool-1-thread-1 主线程赋值1
         */
    }

    // --------------------------------------

    class Context {
        private String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    /**
     * 利用 inheritableThreadLocal 传递同一个对象，对该对象的属性值进行修改，达到在线程池中传递数据的目的
     */
    @Test
    public void poolProblemFix() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        ThreadLocal<Context> inheritableThreadLocal = new InheritableThreadLocal<>();
        Context context = new Context();
        inheritableThreadLocal.set(context);

        context.setData("主线程赋值1");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + inheritableThreadLocal.get().getData());
            }
        });
        Thread.sleep(1000);

        context.setData("主线程赋值2");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + inheritableThreadLocal.get().getData());
            }
        });
        Thread.sleep(1000);

        context.setData("主线程赋值3");
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + inheritableThreadLocal.get().getData());
            }
        });
        Thread.sleep(1000);
    }

    // --------------------------------------

    private static ThreadLocal<String> threadLocal = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "null";
        }
    };

    /**
     * 使用线程池的过程中，任务的构建是在主线程中执行的，而任务的执行是在子线程中执行的。
     * 因此可以利用 context 属性传递父子线程的本地变量。
     */
    public static class WrapRunnable implements Runnable {

        private String context; // 用于传递数据

        private Runnable runnable;

        public WrapRunnable(Runnable runnable) {
            this.runnable = runnable;
            this.context = threadLocal.get(); // 取得父线程的本地变量
        }

        @Override
        public void run() {
            threadLocal.set(this.context); // 赋值给子线程的本地变量
            runnable.run();
        }
    }

    @Test
    public void poolProblemFix02() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        threadPoolExecutor.prestartAllCoreThreads(); // 预先启动线程，完成子线程的本地变量赋值

        threadLocal.set("主线程赋值1");
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + threadLocal.get());
            }
        });
        Thread.sleep(1000);

        threadLocal.set("主线程赋值2");
        threadPoolExecutor.submit(new WrapRunnable(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + threadLocal.get());
                threadLocal.set("子线程赋值4");
            }
        }));
        Thread.sleep(1000);

        threadLocal.set("主线程赋值3");
        threadPoolExecutor.submit(new WrapRunnable(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " " + threadLocal.get());
            }
        }));
        Thread.sleep(1000);
    }
}
