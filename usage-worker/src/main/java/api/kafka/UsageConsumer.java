package api.kafka;

import api.stripe.StripeUsageClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UsageConsumer {
    private final StripeUsageClient stripeUsageClient;

    public UsageConsumer(StripeUsageClient stripeUsageClient) {
        this.stripeUsageClient = stripeUsageClient;
    }

    @KafkaListener(topics = "stripe-usage-topic", groupId = "usage-worker-group")
    public void consume(UsageEvent event) {
        try {
            System.out.println("Received usage event: " + event);
            stripeUsageClient.reportUsage(
                    event.getCustomerId(),
                    event.getUnits(),
                    event.getTimestamp());
        } catch (Exception e) {
            System.err.println("Error reporting usage to Stripe: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
