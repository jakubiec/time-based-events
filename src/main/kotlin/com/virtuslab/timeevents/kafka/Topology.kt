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
            Scheduler.NAME,
            ProcessorSupplier { Scheduler() },
            "TimeBasedEvents"
        )
        .addProcessor(
            Rescheduler.NAME,
            ProcessorSupplier { Rescheduler() },
            Scheduler.NAME
        )
        .addProcessor(
            Canceller.NAME,
            ProcessorSupplier { Canceller() },
            Rescheduler.NAME
        )
        .addProcessor(
            EffectiveEventsForwarder.NAME,
            ProcessorSupplier { EffectiveEventsForwarder(scheduleInterval) },
            Canceller.NAME
        )
        .addProcessor(
            Cleaner.NAME,
            ProcessorSupplier { Cleaner() },
            EffectiveEventsForwarder.NAME
        )
        .addStateStore(
            timeBasedEventStore(),
            Scheduler.NAME,
            Rescheduler.NAME,
            Canceller.NAME,
            EffectiveEventsForwarder.NAME,
            Cleaner.NAME
        )
        .addStateStore(
            effectiveDatesStore(),
            Scheduler.NAME,
            Rescheduler.NAME,
            Canceller.NAME,
            EffectiveEventsForwarder.NAME,
            Cleaner.NAME
        )
        .addSink(
            "EffectiveEvents",
            OUTPUT_TOPIC,
            Serdes.String().serializer(),
            TimeBasedEventSerializer,
            EffectiveEventsForwarder.NAME
        )
