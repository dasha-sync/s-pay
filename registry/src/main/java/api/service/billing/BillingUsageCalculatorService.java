package api.service.billing;

import api.config.BillingWeightsProperties;
import api.metric.UserResourceMetric;
import api.model.BillingSubscription;
import api.repository.BillingSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingUsageCalculatorService {
  private final BillingSubscriptionRepository subscriptionRepository;

  @Transactional
  public void recalculateUsage() {
    // Собираем активные подписки
    var activeSubs = subscriptionRepository.findByStatus(BillingSubscription.SubscriptionStatus.ACTIVE);

    log.info("Recalculating usage for {} active subscriptions", activeSubs.size());

    for (var sub : activeSubs) {
      var username = sub.getUser().getUsername();

      // Получаем значения метрик из Micrometer
      double requests = metrics.getCounterValue("user_requests_total", username);
      double cpuMs = metrics.getCounterValue("user_cpu_time_ms_total", username);
      double netIn = metrics.getCounterValue("user_network_in_bytes_total", username);
      double netOut = metrics.getCounterValue("user_network_out_bytes_total", username);
      double memMb = metrics.getMemoryUsage(username);

      log.info("requests {}", requests);

      // 💡 Алгоритм тарификации
      double cpuSec = cpuMs / 1000.0;
      double trafficMb = (netIn + netOut) / 1_000_000.0;

      double units = weights.request() * requests
          + weights.cpu() * cpuSec
          + weights.network() * trafficMb
          + weights.memory() * memMb;

      // обновляем amount (Stripe units)
      sub.setAmount(BigDecimal.valueOf(units));
      subscriptionRepository.save(sub);

      log.info("Calculated units={} for user={}", units, username);
    }
  }
}
