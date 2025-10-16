package api.controller;

import api.service.FaasMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsMockController {
  private final FaasMetricService faasMetricService;

  @PostMapping("/mock")
  public ResponseEntity<String> sendMockMetrics(
      @RequestParam String funcName,
      @RequestParam(defaultValue = "1") int count
  ) {
    if (count <= 0) {
      return ResponseEntity.badRequest().body("Count must be greater than 0");
    }

    for (int i = 0; i < count; i++) {
      faasMetricService.sendMockMetrics(funcName);
    }

    return ResponseEntity.ok("Mock metrics sent " + count + " times for function " + funcName);
  }
}
