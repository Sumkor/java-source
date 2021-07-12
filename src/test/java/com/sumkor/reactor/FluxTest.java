package com.sumkor.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;

/**
 * @author Sumkor
 * @since 2021/7/11
 */
public class FluxTest {

    @Test
    public void intro() {
        Flux<String> flux = Flux.just("apple", "banana", "pear");
        flux.subscribe(t -> System.out.println("t = " + t));
    }
}
