package com.exchange.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RandomPriceGeneratorTest {

    private RandomPriceGenerator gen = new RandomPriceGenerator(10);

    @Before
    public void setUp() {
        gen.setListener((s, d) -> System.out.println(s + " " + d));
        gen.start();
    }

    @Test
    public void test() throws InterruptedException {
        gen.subscribe("AAPL");

        Thread.sleep(5_000);

        gen.subscribe("IBM");

        Thread.sleep(5_000);

        gen.unsubscribe("AAPL");

        Thread.sleep(5_000);

        gen.unsubscribe("IBM");

        Thread.sleep(5_000);
    }

    @After
    public void tearDown() {
        gen.shutdown();
    }
}