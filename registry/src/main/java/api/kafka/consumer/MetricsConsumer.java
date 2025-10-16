package api.kafka.consumer;

import api.dto.kafka.MetricsEvent;
import api.kafka.producer.KafkaUsageProducer;
import api.model.BillingSubscription;
import api.model.ResourceStats;
import api.repository.BillingSubscriptionRepository;
import api.repository.ResourceStatsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsConsumer {

  private final ResourceStatsRepository resourceStatsRepository;
  private final BillingSubscriptionRepository billingSubscriptionRepository;
  private final KafkaUsageProducer usageProducer;

  private final Map<String, MetricsEvent> previousMetrics = new HashMap<>();

  @Value("${app.kafka.metrics-topic:metrics}")
  private String metricsTopic;

  @KafkaListener(topics = "${app.kafka.metrics-topic:metrics}")
  @Transactional
  public void handleMetrics(@Payload MetricsEvent event) {
    String funcName = "hello";
    MetricsEvent previous = previousMetrics.get(funcName);

    if (previous == null) {
      previousMetrics.put(funcName, event);
      log.info("First metrics for function {} received", funcName);
      return;
    }

    // Получаем актуальные тарифы из БД
    Optional<ResourceStats> optionalStats = resourceStatsRepository.findTopByOrderByIdDesc();
    if (optionalStats.isEmpty()) {
      log.error("No ResourceStats found in DB, cannot calculate costs");
      return;
    }

    ResourceStats costs = optionalStats.get();

    // Вычисляем дельты
    double cpuDelta = Math.abs(event.getCpu_usage() - previous.getCpu_usage());
    double ramDelta = Math.abs(event.getMemory_usage() - previous.getMemory_usage());
    double trafficDelta = Math.abs(event.getNetwork_receive() - previous.getNetwork_receive())
        + Math.abs(event.getNetwork_transmit() - previous.getNetwork_transmit());

    // Считаем стоимости
    double cpuCost = cpuDelta * costs.getCpuCost();
    double ramCost = (ramDelta / (1024.0 * 1024.0)) * costs.getGbCost(); // bytes → GB
    double trafficCost = trafficDelta * costs.getTrafficCost();
    double totalCost = cpuCost + ramCost + trafficCost;

    log.info("""
                Metrics for func: {}
                CPU Δ = {}, RAM Δ = {}, TR Δ = {}
                Costs → CPU: {}, RAM: {}, TR: {}
                TOTAL = {}
                """, funcName, cpuDelta, ramDelta, trafficDelta, cpuCost, ramCost, trafficCost, totalCost);

    // Сохраняем ResourceStats (по желанию)
    ResourceStats stats = new ResourceStats();
    stats.setCpu((int) cpuDelta);
    stats.setRam((int) ramDelta);
    stats.setTraffic((int) trafficDelta);
    stats.setCpuCost(cpuCost);
    stats.setGbCost(ramCost);
    stats.setTrafficCost(trafficCost);
    resourceStatsRepository.save(stats);

    // Обновляем подписку (например, активную)
    billingSubscriptionRepository.findTopByStatus(BillingSubscription.SubscriptionStatus.ACTIVE)
        .ifPresent(subscription -> {
          BigDecimal newAmount = subscription.getAmount().add(BigDecimal.valueOf(totalCost));
          subscription.setAmount(newAmount);
          billingSubscriptionRepository.save(subscription);
          log.info("Updated subscription {} amount = {}", subscription.getId(), newAmount);

          usageProducer.sendUsageEvent(subscription);
        });


    previousMetrics.put(funcName, event);
  }
}
