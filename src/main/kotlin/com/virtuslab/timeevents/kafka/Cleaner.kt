package com.virtuslab.timeevents.kafka

import com.virtuslab.timeevents.TimeBasedEvent
import mu.KotlinLogging
import org.apache.kafka.streams.processor.AbstractProcessor
import org.apache.kafka.streams.processor.ProcessorContext

class Cleaner : AbstractProcessor<String, TimeBasedEvent>() {
    private val logger = KotlinLogging.logger {}
    private lateinit var stores: Stores

    override fun init(context: ProcessorContext) {
        super.init(context)
        stores = Stores(context)
    }

    override fun process(key: String, event: TimeBasedEvent) = clean(event)

    private fun clean(event: TimeBasedEvent): Unit = stores.run {
        timeBasedEvents.delete(event.effectiveDateKey())
        //key=employeeId:eventType -> value=2019-
        effectiveDates.delete(event.eventKey())
        logger.info { "Cleaned stores for effective event $event" }
    }

    companion object {
        const val NAME = "Cleaner"
    }

}