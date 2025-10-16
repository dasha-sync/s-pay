package api.stripe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class StripeUsageClient {
    @Value("${stripe.api-key}")
    private String stripeApiKey;

    private final HttpClient httpClient;

    public StripeUsageClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public void reportUsage(String customerId, long quantity, long timestamp) throws Exception {
        long timestampSeconds = timestamp / 1000;

        String endpoint = "https://api.stripe.com/v1/billing/meter_events";

        String body = "event_name=" + encode("resource_usage")
                + "&timestamp=" + timestampSeconds
                + "&payload[stripe_customer_id]=" + encode(customerId)
                + "&payload[value]=" + quantity;

        sendPostRequest(endpoint, body);
    }

    private void sendPostRequest(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + stripeApiKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException(
                    "Failed to report usage. Status: " + response.statusCode() + ", Body: " + response.body());
        }

        System.out.println("✅ Reported usage to Stripe: " + response.body());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
