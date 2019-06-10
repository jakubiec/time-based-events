package com.virtuslab.timeevents.kafka

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.virtuslab.timeevents.ScheduleCommand
import com.virtuslab.timeevents.TimeBasedEvent
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()).enableDefaultTyping()
private val logger = KotlinLogging.logger {}


object ScheduleCommandDeserializer : Deserializer<ScheduleCommand> {
    override fun deserialize(topic: String, data: ByteArray): ScheduleCommand = objectMapper.readValue(data)

    override fun close() = logger.info { "Closing ScheduleCommandDeserializer" }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
}

object ScheduleCommandSerializer : Serializer<ScheduleCommand> {
    override fun serialize(topic: String, data: ScheduleCommand): ByteArray = objectMapper.writeValueAsBytes(data)

    override fun close() = logger.info { "Closing ScheduleCommandDeserializer" }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
}


object TimeBasedEventSerde : Serde<TimeBasedEvent> {
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}

    override fun deserializer(): Deserializer<TimeBasedEvent> = TimeBasedEventDeserializer

    override fun close() {}

    override fun serializer(): Serializer<TimeBasedEvent> = TimeBasedEventSerializer
}

object TimeBasedEventSerializer : Serializer<TimeBasedEvent> {
    override fun serialize(topic: String, data: TimeBasedEvent): ByteArray = objectMapper.writeValueAsBytes(data)

    override fun close() = logger.info { "Closing TimeBasedEventSerializer" }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
}

object TimeBasedEventDeserializer : Deserializer<TimeBasedEvent> {
    override fun deserialize(topic: String, data: ByteArray): TimeBasedEvent = objectMapper.readValue(data)

    override fun close() = logger.info { "Closing TimeBasedEventDeserializer" }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
}