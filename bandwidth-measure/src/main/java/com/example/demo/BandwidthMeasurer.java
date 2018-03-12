package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BandwidthMeasurer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BandwidthMeasurer.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BandwidthMeasurer.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("run");
    }
}