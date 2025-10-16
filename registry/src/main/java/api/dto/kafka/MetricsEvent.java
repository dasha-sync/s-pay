package api.dto.kafka;

import lombok.Data;

import java.util.Map;

@Data
public class MetricsEvent {
  private Long memory_usage;
  private Long cpu_usage;
  private Long network_receive;
  private Long network_transmit;
  private Long coldstart;

  public Map<String, Long> toMetricsMap() {
    return Map.of(
        "memory_usage", memory_usage,
        "cpu_usage", cpu_usage,
        "network_receive", network_receive,
        "network_transmit", network_transmit,
        "coldstart", coldstart
    );
  }
}
