package com.sbt.metrics.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaToDbAppMain {

    public static void main(String[] args) {
        SpringApplication.run(KafkaToDbAppMain.class, args);
    }

}
