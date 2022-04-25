package com.sbt.metrics.kafka.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MetricsFromKafkaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsFromKafkaService.class);

    @Value("${database.driverClassName}")
    private String databaseDriverClassName;

    @Value("${database.jdbcUrl}")
    private String databaseJdbcUrl;

    @Value("${database.username}")
    private String databaseUserName;

    @Value("${database.password}")
    private String databasePassword;

    DatabaseService databaseService;
    private long counter = 0;

    public MetricsFromKafkaService() {
    }

    @PostConstruct
    public void init() {
        connectDB();
    }

    @EnableKafka
    @Component
    public class Listener {
        @KafkaListener(
                topics = "${spring.kafka.eventsTopic:kogito-processinstances-events}"
        )
        public void messageListener(String msg) {

            counter++;
            LOGGER.debug("{}: {}", counter, msg);

            try {
                JSONObject jsonObject = new JSONObject(msg);
                databaseService.add(jsonObject);
            } catch (JSONException e) {
                LOGGER.info("", e);
            }

        }
    }

    private void connectDB() {
        databaseService = new DatabaseService(
                databaseDriverClassName,
                databaseJdbcUrl,
                databaseUserName,
                databasePassword);
    }
}