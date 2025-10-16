package api.kafka.consumer;

import api.dto.kafka.MetricsEvent;
import api.service.business.FaasMetricService;
import api.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsConsumer {

  @Value("${app.kafka.metrics-topic:metrics}")
  private String metricsTopic;

  @KafkaListener(topics = "${app.kafka.metrics-topic:metrics}")
  public void handleMetrics(@Payload MetricsEvent event) {
    if (event == null) {
      log.warn("Received null MetricsEvent");
      return;
    }
  }
}
