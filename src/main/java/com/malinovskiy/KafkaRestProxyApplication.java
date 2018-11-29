package com.malinovskiy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class KafkaRestProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaRestProxyApplication.class, args);
    }
}
