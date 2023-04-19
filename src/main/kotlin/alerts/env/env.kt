package alerts.env

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.util.*

@ConfigurationProperties("github")
data class Github @ConstructorBinding constructor(val url: String, val token: String?)

interface Topic {
  val name: String
  val numPartitions: Int
  val replicationFactor: Short
}

@ConfigurationProperties("kafka.subscription")
data class SubscriptionTopic @ConstructorBinding constructor(
  override val name: String,
  override val numPartitions: Int,
  override val replicationFactor: Short,
): Topic

@ConfigurationProperties("kafka.event")
data class EventTopic @ConstructorBinding constructor(
  override val name: String,
  override val numPartitions: Int,
  override val replicationFactor: Short,
): Topic

@ConfigurationProperties("kafka.notification")
data class NotificationTopic @ConstructorBinding constructor(
  override val name: String,
  override val numPartitions: Int,
  override val replicationFactor: Short,
): Topic

@ConfigurationProperties("kafka.consumer")
data class KafkaConsumer @ConstructorBinding constructor(
  val groupId: String,
  val autoOffsetReset: String,
  val enableAutoCommit: String,
)

@ConfigurationProperties("kafka.producer")
data class KafkaProducer @ConstructorBinding constructor(
  val acks: String
)

@ConfigurationProperties("kafka")
data class Kafka @ConstructorBinding constructor(
  val bootstrapServers: String,
  val schemaRegistryUrl: String,
  val consumer: KafkaConsumer,
  val producer: KafkaProducer,
  val subscription: SubscriptionTopic,
  val event: EventTopic,
  val notification: NotificationTopic,
) {

  fun consumerProperties() = with(consumer) {
    Properties().apply {
      put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
      put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset)
      put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit)
    }
  }

  fun producerProperties() = with(producer) {
    Properties().apply {
      put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      put(ProducerConfig.ACKS_CONFIG, acks)
    }
  }
}

@ConfigurationProperties("spring.flyway")
data class FlywayProperties @ConstructorBinding constructor(
  val url: String,
  val schemas: String,
  val user: String,
  val password: String
)
