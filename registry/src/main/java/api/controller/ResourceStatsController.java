package api.controller;

import api.dto.common.ApiResponse;
import api.dto.resourceStats.ResourceCostRequest;
import api.dto.resourceStats.ResourceResponse;
import api.dto.resourceStats.ResourceUpdateRequest;
import api.service.ResourceStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceStatsController {

  private final ResourceStatsService resourceStatsService;

  @PostMapping("/update")
  public ResponseEntity<ResourceResponse> updateResources(
      @RequestBody ResourceUpdateRequest request) {

    ResourceResponse response = resourceStatsService.updateResources(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<ResourceResponse> getAll() {
    ResourceResponse response = resourceStatsService.getAll();
    return ResponseEntity.ok(response);
  }

  @PutMapping("/update-costs")
  public ResponseEntity<ResourceResponse> updateCosts(
      @RequestBody ResourceCostRequest request) {

    ResourceResponse response = resourceStatsService.updateCost(request);
    return ResponseEntity.ok(response);
  }
}
