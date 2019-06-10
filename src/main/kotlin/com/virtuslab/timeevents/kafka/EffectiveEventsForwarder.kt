package com.virtuslab.timeevents.kafka

import com.virtuslab.timeevents.ScheduleCommand
import mu.KotlinLogging
import org.apache.kafka.streams.processor.AbstractProcessor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import java.time.Duration

class EffectiveEventsForwarder(private val scheduleDuration: Duration) : AbstractProcessor<String, ScheduleCommand>() {

    private val logger = KotlinLogging.logger {}
    private lateinit var stores: Stores


    override fun init(context: ProcessorContext) {
        super.init(context)
        stores = Stores(context)
        context.schedule(scheduleDuration, PunctuationType.WALL_CLOCK_TIME) { forwardEffectiveEvents() }
    }

    override fun process(key: String, command: ScheduleCommand) {}

    private fun forwardEffectiveEvents() =
        stores.onEffectiveEvents { iterator ->
            iterator.forEach { keyValue ->
                context().forward(keyValue.value.id, keyValue.value)
                logger.info { "Forwarded event ${keyValue.value}" }
            }
        }

    companion object {
        const val NAME = "EffectiveEventsForwarder"
    }
}