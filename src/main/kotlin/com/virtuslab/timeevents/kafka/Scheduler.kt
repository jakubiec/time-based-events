package com.virtuslab.timeevents.kafka

import com.virtuslab.timeevents.ScheduleCommand
import com.virtuslab.timeevents.ScheduleCommand.Reschedule
import com.virtuslab.timeevents.ScheduleCommand.Schedule
import com.virtuslab.timeevents.TimeBasedEvent
import mu.KotlinLogging
import org.apache.kafka.streams.processor.AbstractProcessor
import org.apache.kafka.streams.processor.ProcessorContext

class Scheduler : AbstractProcessor<String, ScheduleCommand>() {

    private val logger = KotlinLogging.logger {}
    private lateinit var stores: Stores

    override fun init(context: ProcessorContext) {
        super.init(context)
        stores = Stores(context)
    }

    override fun process(key: String, command: ScheduleCommand) = when (command) {
        is Schedule -> schedule(key, command)
        else -> context().forward(key, command)
    }

    private fun schedule(key: String, command: Schedule) =
        stores.onEffectiveDateFor(command.eventKey(), reschedule(key, command.event), storeSchedule(command))


    private fun reschedule(key: String, event: TimeBasedEvent): (String) -> Unit = {
        logger.info { "Forwarding to reschedule" }
        context().forward(key, Reschedule(event))
    }

    private fun storeSchedule(command: Schedule): () -> Unit = {
        stores.run {
            timeBasedEvents.put(command.scheduleKey(), command.event)
            effectiveDates.put(command.eventKey(), command.effectiveDate())
        }
        logger.info { "Scheduled ${command.event}" }
    }

    companion object {
        const val NAME = "Scheduler"
    }

}