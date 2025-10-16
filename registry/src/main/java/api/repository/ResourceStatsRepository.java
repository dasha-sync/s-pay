package api.repository;

import api.model.ResourceStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceStatsRepository extends JpaRepository<ResourceStats, Long> {
  Optional<ResourceStats> findTopByOrderByIdDesc();
}

