package com.virtuslab.timeevents.kafka

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.KafkaStreams
import java.time.Duration
import java.util.*


private val logger = KotlinLogging.logger {}


fun main(args: Array<String>) {

     println(topology(Duration.ofSeconds(30)).describe())


}

private fun processStream() {



    val topology = topology(Duration.ofSeconds(30))


    val props = Properties()
    props["bootstrap.servers"] = "localhost:9092"
    props["application.id"] = "time-based-events-processing"
    props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    val streams = KafkaStreams(topology, props)
    streams.cleanUp()
    streams.start()

    Runtime.getRuntime().addShutdownHook(Thread(Runnable { streams.close() }))
}
