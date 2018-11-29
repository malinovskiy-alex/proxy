package com.malinovskiy.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import lombok.AllArgsConstructor;
import lombok.Data;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProxyResourceTest {

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, "users", "events");

    @BeforeClass
    public static void setup() {
        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
    }

    @Autowired
    private WebTestClient webClient;

    @Test
    public void proxyShouldHandleConfiguredUrlAndPutMessageInKafka() {

        //when
        webClient.post().uri("/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromObject(new User(1, "Alex")))
            .exchange()
            .expectStatus().isOk();

        //then
        Consumer<String, String> consumer = createConsumer("users");
        consumer.subscribe(Collections.singleton("users"));
        ConsumerRecords<String, String> records = consumer.poll(2_000);
        consumer.commitSync();

        assertThat(records.count()).isEqualTo(1);
    }

//    @Test
//    public void proxyShouldReturn404IfUrlIsAbsentInAllowedList() {
//        //when
//        webClient.post().uri("/v1/fake-users")
//            .contentType(MediaType.APPLICATION_JSON)
//            .body(BodyInserters.fromObject(new User(1, "Alex")))
//            .exchange()
//            .expectStatus().isNotFound();
//
//        //then
//        Consumer<String, String> consumer = createConsumer("fake-users");
//        consumer.subscribe(Collections.singleton("fake-users"));
//        ConsumerRecords<String, String> records = consumer.poll(2_000);
//        consumer.commitSync();
//
//        assertThat(records.count()).isEqualTo(0);
//    }

    private Consumer<String, String> createConsumer(String consumerGroup) {
        Map<String, Object> consumerProps = KafkaTestUtils
            .consumerProps(consumerGroup, "true", embeddedKafka.getEmbeddedKafka());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        return consumerFactory.createConsumer();
    }

    @Data
    @AllArgsConstructor
    private class User {
        private Integer id;
        private String name;
    }

}
