package api.controller;

import api.dto.subscription.SubscriptionAmountRequest;
import api.dto.subscription.SubscriptionResponse;
import api.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
  private final SubscriptionService subscriptionService;

  @PostMapping("/create/{cardId}")
  public ResponseEntity<SubscriptionResponse> createSubscription(
      @PathVariable Long cardId) throws Exception {
    SubscriptionResponse subscription = subscriptionService.createSubscription(cardId);
    return ResponseEntity.ok(subscription);
  }

  @DeleteMapping("/cancel")
  public ResponseEntity<SubscriptionResponse> cancelSubscription() throws Exception {
    SubscriptionResponse subscription = subscriptionService.cancelSubscription();
    return ResponseEntity.ok(subscription);
  }

  @PutMapping("/change-payment-method/{cardId}")
  public ResponseEntity<SubscriptionResponse> updatePaymentMethod(
      @PathVariable Long cardId) throws Exception {
    SubscriptionResponse subscription = subscriptionService.updatePaymentMethod(cardId);
    return ResponseEntity.ok(subscription);
  }

  @GetMapping
  public ResponseEntity<SubscriptionResponse> getSubscription() {
    SubscriptionResponse subscription = subscriptionService.getSubscription();
    return ResponseEntity.ok(subscription);
  }

  @PutMapping("/amount/overwrite")
  public ResponseEntity<SubscriptionResponse> overwriteAmount(
      @RequestBody SubscriptionAmountRequest request
  ) {
    SubscriptionResponse response = subscriptionService.overwriteAmount(request.getAmount());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/amount/increase")
  public ResponseEntity<SubscriptionResponse> increaseAmount(
      @RequestBody SubscriptionAmountRequest request
  ) {
    SubscriptionResponse response = subscriptionService.increaseAmount(request.getAmount());
    return ResponseEntity.ok(response);
  }

  @PutMapping("/amount/reset")
  public ResponseEntity<SubscriptionResponse> resetAmount() {
    SubscriptionResponse response = subscriptionService.resetAmount();
    return ResponseEntity.ok(response);
  }
}
