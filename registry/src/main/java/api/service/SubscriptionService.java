package api.service;

import api.dto.subscription.SubscriptionResponse;
import api.exception.GlobalException;
import api.model.BillingSubscription;
import api.model.Card;
import api.repository.BillingSubscriptionRepository;
import api.repository.CardRepository;
import api.util.StripeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
  private final StripeProvider stripeProvider;
  private final BillingSubscriptionRepository billingSubscriptionRepository;
  private final CardRepository cardRepository;

  public SubscriptionResponse createSubscription(Long cardId) throws Exception {
    ensureNoActiveSubscription();
    Card card = validateCard(cardId);

    BillingSubscription subscription = stripeProvider.createSubscription(card);
    BillingSubscription saved = billingSubscriptionRepository.save(subscription);

    return mapToDto(saved);
  }

  public SubscriptionResponse cancelSubscription() throws Exception {
    BillingSubscription subscription = findActiveSubscription();

    stripeProvider.cancelSubscription(subscription);
    subscription.setStatus(BillingSubscription.SubscriptionStatus.CANCELLED);

    return mapToDto(billingSubscriptionRepository.save(subscription));
  }

  public SubscriptionResponse updatePaymentMethod(Long cardId) throws Exception {
    BillingSubscription subscription = findActiveSubscription();
    Card card = validateCard(cardId);

    stripeProvider.updateSubscriptionPaymentMethod(subscription, card);
    subscription.setPaymentMethodId(card.getPaymentMethodId());

    return mapToDto(billingSubscriptionRepository.save(subscription));
  }

  public SubscriptionResponse getSubscription() {
    return mapToDto(findActiveSubscription());
  }

  @Transactional
  public SubscriptionResponse overwriteAmount(BigDecimal amount) {
    BillingSubscription subscription = findActiveSubscription();
    subscription.setAmount(amount);

    BillingSubscription saved = billingSubscriptionRepository.save(subscription);
    return mapToDto(saved);
  }

  @Transactional
  public SubscriptionResponse increaseAmount(BigDecimal amount) {
    BillingSubscription subscription = findActiveSubscription();

    BigDecimal newAmount = subscription.getAmount().add(amount);
    subscription.setAmount(newAmount);

    BillingSubscription saved = billingSubscriptionRepository.save(subscription);
    return mapToDto(saved);
  }

  @Transactional
  public SubscriptionResponse resetAmount() {
    BillingSubscription subscription = findActiveSubscription();
    subscription.setAmount(BigDecimal.ZERO);

    BillingSubscription saved = billingSubscriptionRepository.save(subscription);
    return mapToDto(saved);
  }

  private Card validateCard(Long cardId) {
    Card card = cardRepository.findById(cardId)
        .orElseThrow(() -> new GlobalException("Card not found", "NOT_FOUND"));
    return card;
  }

  private void ensureNoActiveSubscription() {
    billingSubscriptionRepository.findByStatus(BillingSubscription.SubscriptionStatus.ACTIVE)
        .ifPresent(s -> {
          throw new GlobalException("User already has an active subscription", "CONFLICT");
        });
  }

  private BillingSubscription findActiveSubscription() {
    return billingSubscriptionRepository.findByStatus(BillingSubscription.SubscriptionStatus.ACTIVE)
        .orElseThrow(() -> new GlobalException("No active subscription found", "NOT_FOUND"));
  }

  private SubscriptionResponse mapToDto(BillingSubscription sub) {
    return new SubscriptionResponse(
        sub.getAmount());
  }
}
