package com.virtuslab.timeevents.kafka

import com.virtuslab.timeevents.Time
import com.virtuslab.timeevents.TimeBasedEvent
import com.virtuslab.timeevents.isoString
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueIterator
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.StoreBuilder
import org.apache.kafka.streams.state.Stores


const val TIME_BASED_EVENTS_STORE = "time-based-events"
const val EFFECTIVE_DATES_STORE = "effective-dates"

fun timeBasedEventStore(): StoreBuilder<KeyValueStore<String, TimeBasedEvent>> =
    Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(TIME_BASED_EVENTS_STORE),
        Serdes.String(),
        TimeBasedEventSerde
    ).withLoggingEnabled(
        mutableMapOf()
    )

fun effectiveDatesStore(): StoreBuilder<KeyValueStore<String, String>> =
    Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(EFFECTIVE_DATES_STORE),
        Serdes.String(),
        Serdes.String()
    ).withLoggingEnabled(
        mutableMapOf()
    )

class Stores(context: ProcessorContext) {
    val timeBasedEvents: KeyValueStore<String, TimeBasedEvent> = context.keyValueStore(TIME_BASED_EVENTS_STORE)
    val effectiveDates: KeyValueStore<String, String> = context.keyValueStore(EFFECTIVE_DATES_STORE)


    private fun <K, V> ProcessorContext.keyValueStore(name: String) = getStateStore(name) as KeyValueStore<K, V>

    fun onEffectiveDateFor(
        eventKey: String,
        onExistingEffectiveDate: (String) -> Unit,
        onNonExistingEffectiveDate: () -> Unit = {}
    ) = effectiveDates.get(eventKey)?.let { onExistingEffectiveDate(it) } ?: onNonExistingEffectiveDate()


    fun onEffectiveEvents(iterateOver: (KeyValueIterator<String, TimeBasedEvent>) -> Unit) =
        timeBasedEvents.range("0", Time.tomorrow().isoString()).use { iterateOver(it) }
}