package api.repository;

import api.model.ResourceStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceStatsRepository extends JpaRepository<ResourceStats, Long> {
}

