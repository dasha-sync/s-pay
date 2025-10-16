import time
import os
import requests
import re
import json
from collections import defaultdict
from confluent_kafka import Producer

# === CONFIGURATION ===
PROMETHEUS_URL = os.getenv("PROMETHEUS_URL", "http://localhost:9090")
POD_PATTERN = re.compile(r'^function-.*$')
POLL_INTERVAL = 10  # seconds

# Kafka settings
KAFKA_BROKER = os.getenv("KAFKA_BROKER", "localhost:9092")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "pod_metrics")

# Metrics to track
METRICS = {
    "container_memory_usage_bytes": "memory_usage",
    "container_cpu_usage_seconds_total": "cpu_usage",
    "container_network_receive_bytes_total": "network_receive",
    "container_network_transmit_bytes_total": "network_transmit",
    "container_start_time_seconds": "coldstart",
}

# === GLOBAL STATE ===
active_pods = defaultdict(lambda: {"pod": "", "first": None, "last": None})

# === KAFKA PRODUCER ===
producer = Producer({"bootstrap.servers": KAFKA_BROKER})


def delivery_report(err, msg):
    """Delivery callback."""
    if err is not None:
        print(f"❌ Delivery failed: {err}")
    else:
        print(f"✅ Sent to {msg.topic()} [{msg.partition()}] @ offset {msg.offset()}")


def send_to_kafka(data):
    print(data)
    try:
        producer.produce(
            KAFKA_TOPIC,
            value=json.dumps(data),
            callback=delivery_report,
        )
        producer.poll(0)  # Trigger delivery callbacks
    except BufferError:
        print("⚠️ Local producer queue is full, flushing...")
        producer.flush()
        producer.produce(KAFKA_TOPIC, value=json.dumps(data))
#

def query_prometheus(metric_name):
    """Query Prometheus for a specific metric."""
    url = f"{PROMETHEUS_URL}/api/v1/query"
    try:
        response = requests.get(url, params={"query": metric_name}, timeout=10)
        response.raise_for_status()
        return response.json()["data"]["result"]
    except Exception as e:
        print(f"Error querying {metric_name}: {e}")
        return []


def collect_metrics():
    """Collect all metrics defined in METRICS."""
    collected = defaultdict(dict)
    for metric, alias in METRICS.items():
        results = query_prometheus(metric)
        for result in results:
            labels = result.get('metric', {})
            pod_name = labels.get('pod')
            if not pod_name or not POD_PATTERN.match(pod_name):
                continue

            try:
                value = float(result['value'][1])
            except (KeyError, ValueError, IndexError):
                continue

            collected[pod_name][alias] = value
    return collected


def main():
    print(f"Monitoring pods matching pattern '{POD_PATTERN.pattern}' and sending to Kafka topic '{KAFKA_TOPIC}'...")
    try:
        while True:
            current_data = collect_metrics()
            current_pods = set(current_data.keys())

            # New or existing pods
            for pod, metrics in current_data.items():
                if active_pods[pod]["first"] is None:
                    active_pods[pod]["pod"] = pod
                    active_pods[pod]["first"] = metrics.copy()
                    print(f"🟢 New pod detected: {pod}")
                active_pods[pod]["last"] = metrics.copy()

            # Disappeared pods
            disappeared_pods = set(active_pods.keys()) - current_pods
            for pod in disappeared_pods:
                completed_pod = active_pods.pop(pod)
                send_to_kafka(completed_pod)
                print(f"🔴 Pod disappeared: {pod}")

            time.sleep(POLL_INTERVAL)

    except KeyboardInterrupt:
        print("\n📦 Monitoring stopped. Flushing Kafka queue...")
        producer.flush()
        print("✅ Done.")


if __name__ == "__main__":
    main()
