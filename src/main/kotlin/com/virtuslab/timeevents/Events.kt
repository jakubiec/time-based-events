package com.virtuslab.timeevents

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = DiscountBecameEffective::class, name = "DiscountBecameEffective")
)
sealed class TimeBasedEvent(val id: String) {
    abstract val effectiveDate: LocalDate

    fun eventKey() = "$id:${this::class.simpleName}"

    fun effectiveDateKey(date: String = effectiveDate.isoString()) = "$date:${eventKey()}"

    fun effectiveDate(): String = effectiveDate.isoString()
}

data class DiscountBecameEffective(
    val employeeId: String,
    override val effectiveDate: LocalDate
) : TimeBasedEvent(employeeId)