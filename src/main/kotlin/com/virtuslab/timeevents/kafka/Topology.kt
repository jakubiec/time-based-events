package com.virtuslab.timeevents.kafka

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.processor.ProcessorSupplier
import java.time.Duration

const val INPUT_TOPIC = "time-based-events"
const val OUTPUT_TOPIC = "effective-time-based-events"

fun topology(scheduleInterval: Duration): Topology =
    Topology()
        .addSource(
            Topology.AutoOffsetReset.EARLIEST, "TimeBasedEvents", Serdes.String().deserializer(),
            ScheduleCommandDeserializer, INPUT_TOPIC
        )

        .addProcessor(
            EffectiveEventsForwarder.NAME,
            ProcessorSupplier { EffectiveEventsForwarder(scheduleInterval) },
           "TimeBasedEvents"
        )
        .addStateStore(
            timeBasedEventStore(),
            EffectiveEventsForwarder.NAME
        )
        .addStateStore(
            effectiveDatesStore(),
            EffectiveEventsForwarder.NAME
        )
        .addSink(
            "EffectiveEvents",
            OUTPUT_TOPIC,
            Serdes.String().serializer(),
            TimeBasedEventSerializer,
            EffectiveEventsForwarder.NAME
        )
