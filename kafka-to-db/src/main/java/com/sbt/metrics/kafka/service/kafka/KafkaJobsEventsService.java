package com.sbt.metrics.kafka.service.kafka;

import com.sbt.metrics.kafka.service.database.DatabaseJobsEventsService;
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
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaJobsEventsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaJobsEventsService.class);

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


    @Value("${kafka.jobs-events.bootstrap-servers:}")
    private String kafkaBootstrapServers;

    @Value("${kafka.jobs-events.consumer.groupId:}")
    private String kafkaConsumerGroupId;


    DatabaseJobsEventsService databaseJobsEventsService;
    private long counter = 0;

    public KafkaJobsEventsService() {
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


//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
//        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, JsonDeserializer.class);
//        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
//        props.put(JsonDeserializer.KEY_DEFAULT_TYPE, "com.example.MyKey");
//        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.MyValue");
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example");

        return props;
    }

    @Bean("containerFactoryJobsEvents")
    public ConcurrentKafkaListenerContainerFactory listenerContainer() {
        ConcurrentKafkaListenerContainerFactory container = new ConcurrentKafkaListenerContainerFactory();
        container.setConsumerFactory(new DefaultKafkaConsumerFactory(consumerProps()));
        return container;
    }

    @PostConstruct
    public void init() {
        connectDB();
    }

    @Scheduled(fixedDelay = 60000)
    private void reconnect() {
        databaseJobsEventsService.connect();
    }

    @EnableKafka
    @Component
    public class Listener {
        @KafkaListener(
                containerFactory = "containerFactoryJobsEvents",
                topics = "${kafka.jobs-events.eventsTopic:kogito-jobs-events}",
                concurrency = "${kafka.jobs-events.listener.concurency:4}"
        )
        public void messageListener(String msg) {
            counter++;
            LOGGER.debug("{}: {}", counter, msg);

            try {
                JSONObject jsonObject = new JSONObject(msg);
                if (jsonObject.getString("type").equalsIgnoreCase("JobEvent")) {
                    databaseJobsEventsService.add(jsonObject);
                } else {
                    LOGGER.warn("Формат данных: {}", jsonObject);
                }
            } catch (JSONException e) {
                LOGGER.info("", e);
            }
        }
    }

    private void connectDB() {
        databaseJobsEventsService = new DatabaseJobsEventsService(
                databaseDriverClassName,
                databaseJdbcUrl,
                databaseUserName,
                databasePassword,
                databaseSchema);
    }
}