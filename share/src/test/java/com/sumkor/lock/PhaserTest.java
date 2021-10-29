package com.sumkor.lock;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Phaser;

/**
 * @author Sumkor
 * @since 2021/4/23
 */
public class PhaserTest {

    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getId() + " hello world");
            }
        };
        List<Runnable> list = Arrays.asList(runnable, runnable, runnable);
//        runTasks(list);
        /**
         * 12 hello world
         * 11 hello world
         * 13 hello world
         */

        startTasks(list, 2);
        /**
         * 11 hello world
         * 12 hello world
         * 13 hello world
         * phase = 0 registeredParties = 3
         * 13 hello world
         * 12 hello world
         * 11 hello world
         * phase = 1 registeredParties = 3
         * 11 hello world
         * 13 hello world
         * 12 hello world
         * phase = 2 registeredParties = 3
         */
    }

    /**
     * A Phaser may be used instead of a CountDownLatch to control a one-shot action serving a variable number of parties.
     * The typical idiom is for the method setting this up to first register, then start the actions, then deregister, as in:
     */
    public static void runTasks(List<Runnable> tasks) {
        final Phaser phaser = new Phaser(1); // "1" to register self
        // create and start threads
        for (final Runnable task : tasks) {
            phaser.register();
            new Thread() {
                @Override
                public void run() {
                    phaser.arriveAndAwaitAdvance(); // await all creation
                    task.run();
                }
            }.start();
        }

        // allow threads to start and deregister self
        phaser.arriveAndDeregister();
    }

    /**
     * One way to cause a set of threads to repeatedly perform actions for a given number of iterations is to override onAdvance:
     *
     * @param iterations 迭代次数/阶数，表示 list 中的每个任务需要再执行几次
     */
    public static void startTasks(List<Runnable> tasks, final int iterations) {
        final Phaser phaser = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("phase = " + phase + " registeredParties = " + registeredParties);
                return phase >= iterations || registeredParties == 0;
            }
        };
        phaser.register();
        for (final Runnable task : tasks) {
            phaser.register();
            new Thread() {
                @Override
                public void run() {
                    do {
                        task.run();
                        phaser.arriveAndAwaitAdvance();
                    } while (!phaser.isTerminated());
                }
            }.start();
        }
        phaser.arriveAndDeregister(); // deregister self, don't wait
    }
}
