package com.virtuslab.timeevents.kafka

import com.virtuslab.timeevents.ScheduleCommand
import com.virtuslab.timeevents.ScheduleCommand.Reschedule
import mu.KotlinLogging
import org.apache.kafka.streams.processor.AbstractProcessor
import org.apache.kafka.streams.processor.ProcessorContext

class Rescheduler : AbstractProcessor<String, ScheduleCommand>() {

    private val logger = KotlinLogging.logger {}
    private lateinit var stores: Stores

    override fun init(context: ProcessorContext) {
        super.init(context)
        stores = Stores(context)
    }

    override fun process(key: String, command: ScheduleCommand) = when (command) {
        is Reschedule -> reschedule(command)
        else -> context().forward(key, command)
    }

    private fun reschedule(command: Reschedule) =
        stores.onEffectiveDateFor(command.eventKey(), storeNewSchedule(command))

    private fun storeNewSchedule(command: Reschedule): (String) -> Unit = { currentEffectiveDate ->
        stores.run {
            timeBasedEvents.delete(command.scheduleKeyOf(currentEffectiveDate))
            timeBasedEvents.put(command.scheduleKey(), command.event)
            effectiveDates.put(command.eventKey(), command.effectiveDate())
        }
        logger.info { "Rescheduled event ${command.event}" }
    }

    companion object {
        const val NAME = "Rescheduler"
    }
}