package com.sbt.metrics.kafka.service.kafka;

import com.sbt.metrics.kafka.service.database.DatabaseProcessInstancesEventsService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaProcessInstancesEventsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProcessInstancesEventsService.class);

    @Value("${database.driverClassName}")
    private String databaseDriverClassName;

    @Value("${database.jdbcUrl}")
    private String databaseJdbcUrl;

    @Value("${database.username}")
    private String databaseUserName;

    @Value("${database.password}")
    private String databasePassword;

    @Value("${database.schema:}")
    private String databaseSchema;

    @Value("${database.sizeLimit:80000}")
    private String databaseSizeLimit;


    @Value("${kafka.processinstances-events.bootstrap-servers:}")
    private String kafkaBootstrapServers;

    @Value("${kafka.processinstances-events.consumer.groupId:}")
    private String kafkaConsumerGroupId;


    DatabaseProcessInstancesEventsService databaseProcessInstancesEventsService;
    private long counter = 0;

    public KafkaProcessInstancesEventsService() {
    }

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "35000");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5"); // Извлекаем количество сообщений за раз
        return props;
    }

    @Bean("containerFactoryProcessInstancesEvents")
    public ConcurrentKafkaListenerContainerFactory listenerContainer() {
        ConcurrentKafkaListenerContainerFactory container = new ConcurrentKafkaListenerContainerFactory();
        container.setConsumerFactory(new DefaultKafkaConsumerFactory(consumerProps()));

//        container.setConcurrency(5); // степень параллелизма, которая меньше или равна количеству разделов темы
//        container.setBatchListener(true); // пакетный мониторинг
        return container;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("version 2022-05-13 07:20"); // сборка
        connectDB();
    }

    @Scheduled(fixedDelay = 60000)
    private void reconnect() {
        databaseProcessInstancesEventsService.connect();
    }

    @EnableKafka
    @Component
    public class Listener {
        @KafkaListener(
                containerFactory = "containerFactoryProcessInstancesEvents",
                topics = "${kafka.processinstances-events.eventsTopic:kogito-processinstances-events}",
                concurrency = "${kafka.processinstances-events.listener.concurency:4}"
        )
        public void messageListener(String msg) {
            counter++;
            LOGGER.debug("{}: {}", counter, msg);

            try {
                JSONObject jsonObject = new JSONObject(msg);
                if (jsonObject.getString("type").equalsIgnoreCase("ProcessInstanceEvent")) {
                    databaseProcessInstancesEventsService.add(jsonObject);
                } else {
                    LOGGER.warn("Формат данных: {}", jsonObject);
                }

            } catch (JSONException e) {
                LOGGER.info("", e);
            }
        }
    }

    private void connectDB() {
        databaseProcessInstancesEventsService = new DatabaseProcessInstancesEventsService(
                databaseDriverClassName,
                databaseJdbcUrl,
                databaseUserName,
                databasePassword,
                databaseSchema,
                Integer.parseInt(databaseSizeLimit));
    }
}