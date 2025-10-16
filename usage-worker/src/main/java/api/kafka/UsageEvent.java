package api.kafka;

public class UsageEvent {
    private String customerId;
    private long units;
    private long timestamp;

    public UsageEvent() {}

    public UsageEvent(String customerId, long units, long timestamp) {
        this.customerId = customerId;
        this.units = units;
        this.timestamp = timestamp;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public long getUnits() {
        return units;
    }

    public void setUnits(long units) {
        this.units = units;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UsageEvent{" +
                "customerId='" + customerId + '\'' +
                ", units=" + units +
                ", timestamp=" + timestamp +
                '}';
    }
}
