package com.post_hub.iam_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.post_hub.iam_service.kafka.model.utils.PostHubService;
import com.post_hub.iam_service.kafka.model.utils.UtilMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class MessageProducer {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${additional.kafka.topic.iam.service.logs}")
    private String logsOutTopic;

    @Value(value = "${kafka.enabled}")
    private boolean isKafkaEnabled;

    public void sendLogs(@NotNull @Valid UtilMessage message) {
        if (!isKafkaEnabled) {
            log.trace("Kafka is not enabled. Message will not be placed im iam_logs topic [message={}] ", message);
            return;
        }
        try {
            message.setService(PostHubService.IAM_SERVICE);
            String messageJson = objectMapper.writeValueAsString(message);
            log.debug("Sending message to Kafka: {}", messageJson);
            kafkaTemplate.send(logsOutTopic, messageJson).get();
            log.debug("Kafka {} message sent. Topic: '{}', message: '{}'", message.getActionType(), logsOutTopic, messageJson);
        } catch (Exception cause) {
            log.error("Kafka message didn't send. ", cause);
        }
    }
}
