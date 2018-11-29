package com.malinovskiy.resource;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@RestController
public class ProxyResource {

    private final KafkaSender<String, String> sender;

    @Autowired
    public ProxyResource(KafkaSender<String, String> sender) {
        this.sender = sender;
    }

    @PostMapping(path = "/v1/{topicName}")
    public Mono<ResponseEntity<?>> postData(
        @PathVariable("topicName") String topicName, String message) {
        return sender.send(Mono.just(SenderRecord.create(new ProducerRecord<>(topicName, null, message), message)))
            .flatMap(result -> {
                if (result.exception() != null) {
                    return Flux.just(ResponseEntity.badRequest()
                        .body(result.exception().getMessage()));
                }
                return Flux.just(ResponseEntity.ok().build());
            })
            .next();
    }
}
