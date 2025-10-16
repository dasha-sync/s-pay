package api.util;

import api.model.BillingSubscription;
import api.model.Card;
import api.repository.BillingSubscriptionRepository;
import api.repository.CardRepository;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StripeProvider {
  private final CardRepository cardRepository;
  private final BillingSubscriptionRepository billingSubscriptionRepository;

  @Value("${stripe.api.price-id}")
  private String priceId;

  @Value("${stripe.api.secret-key}")
  private String apiKey;

  public String createCustomerIfNotExists() throws Exception {
    BillingSubscription existingSub = billingSubscriptionRepository.findFirstByOrderByIdAsc();
    if (existingSub != null && existingSub.getStripeCustomerId() != null) {
      return existingSub.getStripeCustomerId();
    }

    Customer customer = Customer.create(Map.of(
        "email", "daria.kapur+" + UUID.randomUUID().toString().substring(0, 8) + "@gmail.com"
    ));

    BillingSubscription sub = new BillingSubscription();
    sub.setStripeCustomerId(customer.getId());
    sub.setStatus(BillingSubscription.SubscriptionStatus.PAUSED);
    sub.setAmount(BigDecimal.ZERO);
    billingSubscriptionRepository.save(sub);

    return customer.getId();
  }

  public void attachPaymentMethodIfNeeded(String paymentMethodId, String customerId) throws Exception {
    PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
    if (pm.getCustomer() == null) {
      pm.attach(Map.of("customer", customerId));
    }
  }

  public Card attachCard(String paymentMethodId) throws Exception {
    String customerId = createCustomerIfNotExists();
    PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
    pm.attach(Map.of("customer", customerId));

    Card card = new Card();
    card.setPaymentMethodId(pm.getId());
    card.setBrand(pm.getCard().getBrand());
    card.setLast4(pm.getCard().getLast4());
    card.setExpMonth(pm.getCard().getExpMonth());
    card.setExpYear(pm.getCard().getExpYear());

    return cardRepository.save(card);
  }


  public BillingSubscription createSubscription(Card card) throws Exception {
    String customerId = createCustomerIfNotExists();
    attachPaymentMethodIfNeeded(card.getPaymentMethodId(), customerId);

    Subscription stripeSub = createStripeSubscription(customerId, card.getPaymentMethodId());
    return buildBillingSubscription(card, stripeSub);
  }

  public void cancelSubscription(BillingSubscription subscription) throws Exception {
    Subscription.retrieve(subscription.getStripeSubscriptionId()).cancel();
  }

  public void updateSubscriptionPaymentMethod(BillingSubscription subscription, Card card) throws Exception {
    String customerId = createCustomerIfNotExists();
    attachPaymentMethodIfNeeded(card.getPaymentMethodId(), customerId);

    Subscription stripeSub = Subscription.retrieve(subscription.getStripeSubscriptionId());
    stripeSub.update(Map.of("default_payment_method", card.getPaymentMethodId()));
  }

  private Subscription createStripeSubscription(String customerId, String paymentMethodId) throws Exception {
    SubscriptionCreateParams params = SubscriptionCreateParams.builder()
        .setCustomer(customerId)
        .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
        .setDefaultPaymentMethod(paymentMethodId)
        .setCollectionMethod(SubscriptionCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY)
        .build();

    return Subscription.create(params);
  }

  private BillingSubscription buildBillingSubscription(Card card, Subscription stripeSub) {
    BillingSubscription sub = new BillingSubscription();
    sub.setAmount(BigDecimal.ZERO);
    sub.setPaymentMethodId(card.getPaymentMethodId());
    sub.setStripeSubscriptionId(stripeSub.getId());
    sub.setStatus(BillingSubscription.SubscriptionStatus.ACTIVE);
    return sub;
  }
}
