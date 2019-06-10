package com.virtuslab.timeevents.kafka

import com.virtuslab.timeevents.DiscountBecameEffective
import com.virtuslab.timeevents.ScheduleCommand
import com.virtuslab.timeevents.ScheduleCommand.*
import com.virtuslab.timeevents.Time
import com.virtuslab.timeevents.TimeBasedEvent
import io.kotlintest.TestCase
import io.kotlintest.data.forall
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.specs.FeatureSpec
import io.kotlintest.tables.row
import org.apache.kafka.common.serialization.Serdes.String
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.test.OutputVerifier
import java.time.Duration
import java.time.LocalDate
import java.util.*


class EffectiveEventsForwarderSpec : FeatureSpec() {

    private val scheduleInterval = Duration.ofSeconds(10)
    private val topology = topology(scheduleInterval)
    private val props = Properties().apply {
        setProperty("bootstrap.servers", "dummy:1234")
        setProperty("application.id", "time-based-events-processing")
    }
    private val recordFactory =
        ConsumerRecordFactory<String, ScheduleCommand>(String().serializer(), ScheduleCommandSerializer)

    private val testDriver = TopologyTestDriver(topology, props)

    private val employeeId = "12345"

    override fun beforeTest(testCase: TestCase) {
        cleanStateStores()
        Time.restoreDefault()
    }

    init {

        feature("schedule time based event") {
            scenario("should forward past dated event") {
                val pastDatedEvent = pastDatedEvent()

                sendCommand(Schedule(pastDatedEvent))

                shouldForwardEvent(pastDatedEvent)
                shouldNotForwardNextEvent()
            }


            scenario("should forward future dated event on effective date") {
                val futureDatedEvent = futureDatedEvent()

                sendCommand(Schedule(futureDatedEvent))

                Time.setToday { futureDatedEvent.effectiveDate }
                shouldForwardEvent(futureDatedEvent)

                shouldNotForwardNextEvent()
            }

            scenario("should forward effective events for different keys") {
                forall(
                    row(pastDatedEvent("1")),
                    row(pastDatedEvent("2")),
                    row(pastDatedEvent("3")),
                    row(pastDatedEvent("4"))
                ) { event ->
                    sendCommand(Schedule(event))

                    shouldForwardEvent(event)
                }

            }
        }

        feature("reschedule time based event") {
            scenario("should forward event rescheduled to the past") {
                val futureDatedEvent = futureDatedEvent()

                sendCommand(Schedule(futureDatedEvent))

                shouldNotForwardNextEvent()

                val newEffectiveDate = Time.today().minusDays(10)
                val effectiveEvent = timeBasedEvent(newEffectiveDate)

                sendCommand(Reschedule(effectiveEvent))

                shouldForwardEvent(effectiveEvent)

                shouldNotForwardNextEvent()
            }

            scenario("should forward rescheduled event in the future") {
                val futureDatedEvent = futureDatedEvent()

                sendCommand(Schedule(futureDatedEvent))

                shouldNotForwardNextEvent()

                val newEffectiveDate = Time.today().plusDays(10)
                val effectiveEvent = timeBasedEvent(newEffectiveDate)

                sendCommand(Reschedule(effectiveEvent))

                Time.setToday { futureDatedEvent.effectiveDate }
                shouldNotForwardNextEvent()


                Time.setToday { newEffectiveDate }
                shouldForwardEvent(effectiveEvent)

                shouldNotForwardNextEvent()
            }

            scenario("should forward rescheduled event only for effective key") {
                val keyToReschedule = "444"

                sendCommand(Schedule(futureDatedEvent(keyToReschedule)))
                sendCommand(Schedule(futureDatedEvent("1234")))

                shouldNotForwardNextEvent()

                val rescheduledEvent = pastDatedEvent(keyToReschedule)
                sendCommand(Reschedule(rescheduledEvent))

                shouldForwardEvent(rescheduledEvent)

                shouldNotForwardNextEvent()
            }
        }


        feature("cancel time based event") {
            scenario("should cancel scheduled event") {
                val futureDatedEvent = futureDatedEvent()

                sendCommand(Schedule(futureDatedEvent))

                shouldNotForwardNextEvent()

                sendCommand(Cancel(futureDatedEvent))

                Time.setToday { futureDatedEvent.effectiveDate }
                shouldNotForwardNextEvent()
            }

            scenario("should cancel only event with given key") {
                val keyToCancel = "444"
                val eventToCancel = futureDatedEvent(keyToCancel)
                val futureDatedEvent = futureDatedEvent("1234")

                sendCommand(Schedule(eventToCancel))
                sendCommand(Schedule(futureDatedEvent))

                shouldNotForwardNextEvent()

                sendCommand(Cancel(eventToCancel))

                Time.setToday { futureDatedEvent.effectiveDate }
                shouldForwardEvent(futureDatedEvent)

                shouldNotForwardNextEvent()
            }
        }

    }

    private fun futureDatedEvent(id: String = employeeId) = timeBasedEvent(Time.today().plusDays(3), id)

    private fun pastDatedEvent(id: String = employeeId) = timeBasedEvent(Time.today().minusDays(6), id)

    private fun timeBasedEvent(date: LocalDate, id: String = employeeId) = DiscountBecameEffective(id, date)

    private fun sendCommand(scheduleCommand: ScheduleCommand) {
        testDriver.pipeInput(recordFactory.create(INPUT_TOPIC, scheduleCommand.event.id, scheduleCommand))
    }

    private fun shouldForwardEvent(expectedEvent: TimeBasedEvent) {
        advancePunctuationTime()
        OutputVerifier.compareKeyValue(
            nextForwardedEvent(),
            expectedEvent.id,
            expectedEvent
        )
    }

    private fun shouldNotForwardNextEvent() = advancePunctuationTime().also { nextForwardedEvent().shouldBeNull() }

    private fun nextForwardedEvent() =
        testDriver.readOutput(OUTPUT_TOPIC, String().deserializer(), TimeBasedEventDeserializer)


    private fun advancePunctuationTime() = testDriver.advanceWallClockTime(scheduleInterval.toMillis())


    private fun cleanStateStores() {
        testDriver.allStateStores.values.forEach {
            val kv = it as KeyValueStore<Any, Any>
            kv.all().forEach {
                kv.delete(it.key)
            }
        }
    }

}

