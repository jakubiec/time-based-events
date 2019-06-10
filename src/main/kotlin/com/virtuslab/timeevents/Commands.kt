package com.virtuslab.timeevents

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.virtuslab.timeevents.ScheduleCommand.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    Type(value = Schedule::class, name = "Schedule"),
    Type(value = Reschedule::class, name = "Reschedule"),
    Type(value = Cancel::class, name = "Cancel")

)
sealed class ScheduleCommand {
    abstract val event: TimeBasedEvent

    data class Schedule(override val event: TimeBasedEvent) : ScheduleCommand()

    data class Reschedule(override val event: TimeBasedEvent) : ScheduleCommand()

    data class Cancel(override val event: TimeBasedEvent) : ScheduleCommand()

    fun scheduleKey(): String = event.effectiveDateKey()

    fun eventKey(): String = event.eventKey()

    fun effectiveDate(): String = event.effectiveDate()

    fun scheduleKeyOf(effectiveDate: String) = event.effectiveDateKey(effectiveDate)

}