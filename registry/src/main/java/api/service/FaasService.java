package api.service;

import api.dto.faas.FaasResponse;
import api.dto.kafka.MetricsEvent;
import api.model.Faas;
import api.repository.FaasRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaasService {
  private final FaasRepository faasRepository;

  @Transactional
  public void saveMetrics(MetricsEvent event) {
    List<Faas> metricsToSave = buildMetrics(event);
    faasRepository.saveAll(metricsToSave);
    log.debug("Saved {} metrics for func={}", metricsToSave.size(), event.getFuncName());
  }

  public Map<String, Object> getFaas() {
    List<Faas> entities = faasRepository.findAll();

    Map<String, Map<String, List<Map<String, Object>>>> grouped = entities.stream()
        .collect(Collectors.groupingBy(
            Faas::getFuncName,
            Collectors.groupingBy(
                Faas::getMetricName,
                Collectors.mapping(f -> {
                  Map<String, Object> m = new LinkedHashMap<>();
                  m.put("id", f.getId());
                  m.put("metricValue", f.getMetricValue());
                  m.put("ts", f.getTs());
                  return m;
                }, Collectors.toList())
            )
        ));

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("message", "User FaaS functions");
    result.put("data", grouped);
    return result;
  }


  private List<Faas> buildMetrics(MetricsEvent event) {
    return event.toMetricsMap().entrySet().stream()
        .filter(e -> e.getValue() != null && e.getValue() != 0)
        .map(e -> new Faas(null, event.getFuncName(), e.getKey(), e.getValue(), null))
        .toList();
  }

  @Transactional
  public Faas addFaasMetric(String funcName, String metricName, Long metricValue) {
    Faas metric = new Faas();
    metric.setFuncName(funcName);
    metric.setMetricName(metricName);
    metric.setMetricValue(metricValue != null ? metricValue : 0L);

    Faas saved = faasRepository.save(metric);
    log.debug("Added FaaS metric func={}, metric={}, value={}", funcName, metricName, metricValue);
    return saved;
  }
}
