package api.controller;

import api.dto.common.MetricResponse;
import api.service.FaasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import api.dto.faas.FaasRequest;
import api.dto.faas.FaasResponse;
import api.model.Faas;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/faas")
@RequiredArgsConstructor
public class FaasController {
  private final FaasService faasService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getFaas() {
    Map<String, Object> faas = faasService.getFaas();
    return ResponseEntity.ok(faas);
  }

  @PostMapping
  public ResponseEntity<FaasResponse> addFaas(@RequestBody FaasRequest request) {
    Faas saved = faasService.addFaasMetric(
        request.getFuncName(),
        request.getMetricName(),
        request.getMetricValue()
    );

    FaasResponse response = new FaasResponse(
        saved.getId(),
        saved.getFuncName(),
        saved.getMetricName(),
        saved.getMetricValue(),
        saved.getTs()
    );

    return ResponseEntity.ok(response);
  }
}

