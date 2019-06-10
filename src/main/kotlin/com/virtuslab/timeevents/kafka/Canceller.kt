package com.virtuslab.timeevents.kafka

import com.virtuslab.timeevents.ScheduleCommand
import com.virtuslab.timeevents.ScheduleCommand.Cancel
import mu.KotlinLogging
import org.apache.kafka.streams.processor.AbstractProcessor
import org.apache.kafka.streams.processor.ProcessorContext

class Canceller : AbstractProcessor<String, ScheduleCommand>() {

    private val logger = KotlinLogging.logger {}
    private lateinit var stores: Stores

    override fun init(context: ProcessorContext) {
        super.init(context)
        stores = Stores(context)
    }

    override fun process(key: String, command: ScheduleCommand) = when (command) {
        is Cancel -> cancel(command)
        else -> logger.warn { "Cannot handle command $command" }
    }

    private fun cancel(command: Cancel) = stores.onEffectiveDateFor(command.eventKey(), deleteSchedule(command))

    private fun deleteSchedule(command: Cancel): (String) -> Unit = { currentEffectiveDate ->
        stores.run {
            timeBasedEvents.delete(command.scheduleKeyOf(currentEffectiveDate))
            effectiveDates.delete(command.eventKey())
        }
        logger.info { "Cancelled event ${command.event}" }
    }

    companion object {
        const val NAME = "Canceller"
    }

}