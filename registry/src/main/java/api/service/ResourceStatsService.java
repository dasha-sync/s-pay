package api.service;

import api.dto.resourceStats.*;
import api.model.ResourceStats;
import api.repository.ResourceStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceStatsService {

  private final ResourceStatsRepository repository;

  private ResourceStats getOrCreate() {
    List<ResourceStats> all = repository.findAll();
    if (all.isEmpty()) {
      ResourceStats stats = new ResourceStats();
      stats.setCpu(0);
      stats.setRam(0);
      stats.setTraffic(0);
      repository.save(stats);
      return stats;
    }
    return all.get(0);
  }

  @Transactional
  public ResourceResponse updateResources(ResourceUpdateRequest dto) {
    ResourceStats stats = getOrCreate();
    if (dto.getCpu() != null) stats.setCpu(dto.getCpu());
    if (dto.getRam() != null) stats.setRam(dto.getRam());
    if (dto.getTraffic() != null) stats.setTraffic(dto.getTraffic());
    repository.save(stats);
    return mapToResponse(stats);
  }

  public ResourceResponse getAll() {
    return mapToResponse(getOrCreate());
  }

  @Transactional
  public ResourceResponse updateCost(ResourceCostRequest dto) {
    ResourceStats stats = getOrCreate();
    if (dto.getCpuCost() != null) stats.setCpuCost(dto.getCpuCost());
    if (dto.getGbCost() != null) stats.setGbCost(dto.getGbCost());
    if (dto.getTrafficCost() != null) stats.setTrafficCost(dto.getTrafficCost());
    repository.save(stats);
    return mapToResponse(stats);
  }

  private ResourceResponse mapToResponse(ResourceStats stats) {
    return new ResourceResponse(
        stats.getCpu(),
        stats.getRam(),
        stats.getTraffic(),
        stats.getCpuCost(),
        stats.getGbCost(),
        stats.getTrafficCost()
    );
  }
}
