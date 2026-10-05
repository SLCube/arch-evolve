#!/usr/bin/env bash
set -euo pipefail

create_topic() {
    /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 \
        --create --if-not-exists --topic "$1" --partitions "$2" --replication-factor 1
}

# Match the application TopicBuilder definitions before any consumers subscribe.
create_topic order.created 50
create_topic order.completed 50
create_topic payment.authorized 15
create_topic payment.failed 3
create_topic delivery.created 50
create_topic order-created.DLT 3
create_topic order-completed.DLT 3
create_topic payment-authorized.DLT 3
create_topic payment-failed.DLT 3
echo 'KAFKA TOPICS READY'
