package com.sumkor.spi;

/**
 * @author Sumkor
 * @since 2020/8/30
 */
public class MyServiceLoaderImpl2 implements IMyServiceLoader {

    @Override
    public String sayHello() {
        return "hello2";
    }

    @Override
    public String getName() {
        return "name2";
    }
}
