package api.repository;

import api.model.BillingSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingSubscriptionRepository extends JpaRepository<BillingSubscription, Long> {
  Optional<BillingSubscription> findByStatus(BillingSubscription.SubscriptionStatus status);

  boolean existsBy();

  BillingSubscription findFirstByOrderByIdAsc();
}
