package api.service;

import api.dto.kafka.MetricsEvent;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class FaasMetricService {

  private final MeterRegistry meterRegistry;

  private final Map<String, Map<String, Long>> metricsValues = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Gauge>> registeredGauges = new ConcurrentHashMap<>();

  public void saveMetrics(MetricsEvent event) {
    if (event == null || event.getFuncName() == null) return;

    metricsValues.computeIfAbsent(event.getFuncName(), k -> new ConcurrentHashMap<>());
    registeredGauges.computeIfAbsent(event.getFuncName(), k -> new ConcurrentHashMap<>());

    // metric1
    metricsValues.get(event.getFuncName()).put("metric1", event.getMetric1());
    registerOrUpdateGauge(event.getFuncName(), "metric1");

    // metric2
    metricsValues.get(event.getFuncName()).put("metric2", event.getMetric2());
    registerOrUpdateGauge(event.getFuncName(), "metric2");
  }

  private void registerOrUpdateGauge(String funcName, String metricType) {
    Map<String, Gauge> gaugesForFunc = registeredGauges.get(funcName);
    Map<String, Long> valuesForFunc = metricsValues.get(funcName);

    gaugesForFunc.computeIfAbsent(metricType, mt ->
        Gauge.builder(mt, () -> valuesForFunc.get(mt)) // имя метрики = metric1 / metric2
            .description("Metric " + mt + " for function " + funcName)
            .tag("function", funcName) // тег — имя функции
            .register(meterRegistry)
    );
  }

  public void sendMockMetrics(String funcName) {
    if (funcName == null || funcName.isEmpty()) return;

    metricsValues.computeIfAbsent(funcName, k -> new ConcurrentHashMap<>());
    registeredGauges.computeIfAbsent(funcName, k -> new ConcurrentHashMap<>());

    long metric1 = (long) (Math.random() * 100);
    long metric2 = (long) (Math.random() * 100);

    metricsValues.get(funcName).put("metric1", metric1);
    metricsValues.get(funcName).put("metric2", metric2);

    registerOrUpdateGauge(funcName, "metric1");
    registerOrUpdateGauge(funcName, "metric2");
  }
}
