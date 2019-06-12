# Time-based events

## On Kafka

---
@snap[north span-100 headline]
### Context
@snapend

![](assets/diagrams/colleagues_benefits.png)
---
@snap[north span-100 headline]
### 1. Use case - Schedule
@snapend

On __joiningDate__ plus 6 months, **Discount** becomes effective and **DiscountAccount** should be created.

---
@snap[north span-100 headline]
### 1. Use case - Schedule
@snapend

![](assets/diagrams/colleague_benefits_kafka.png)
---
@snap[north span-100 headline]
### Concern?
@snapend

How to schedule an event?

Aren't events about the past?

![](assets/diagrams/time_based_events.png)

---
@snap[north-west bio-name] 
### Konrad Jakubiec
@snapend

@snap[west text-08 span-80] 


Open-minded Software Engineer **@VirtusLab**.<br/>


Passionate about Domain-driven Design, Reactive Microservices and Event-driven architecture.<br/>


Spending free time on self-growth, discovering new music and playing on drums.

@snapend

@snap[north-east span-20]
![](assets/img/me.jpg)
@snapend

@snap[south-west template-note text-gray] 
<kjakubiec@virtuslab.com>
@snapend


---
@snap[north span-100 headline]
### Agenda
@snapend

@ol

- Theoretical background
- Use Case
- Solution 
- Summary

@olend
---
@snap[north span-100 headline]
### Domain events
@snapend

![](assets/diagrams/domain_events.png)

---
@snap[north span-100 headline]
### Aggregate events
@snapend

![](assets/diagrams/aggregate_events.png)

---
@snap[north span-100 headline]
### Subscribers
@snapend

![](assets/diagrams/subscribers.png)


---
@snap[north span-100 headline]
### Time-based events
@snapend

![](assets/diagrams/time_based_events.png)

---
@snap[north span-100 headline]
### A niche
@snapend

@fa[quote-left](...expiring time frame will generally have a descriptive name that will become part of the Ubiquitous Language...)

+++
@snap[north span-100 headline]
### A niche
@snapend
@fa[quote-left](Therefore, you have a name for that particular time-based Domain Event.)

+++
@snap[north span-100 headline]
### A niche
@snapend
@fa[quote-left](Schedulers and Timers are also very interesting types of events, but they are a often times an implementation-level consideration.)


---
@snap[north span-100 headline]
### Retroactive event
@snapend

![](assets/diagrams/retroactive_event.png)


---
### Concern?
@snapend

How to schedule an event?

Aren't events about the past?

![](assets/diagrams/time_based_events.png)
---
@snap[north span-100 headline]
### Kafka Streams Topology
@snapend

![](assets/img/kafka-topology.jpg)

---
@snap[north span-100 headline]
### Kafka Streams DSL
@snapend

```
KTable<EmployeeId, Employee> employee = new KStreamBuilder()
    .stream("employee-events")
    .groupBy((employeeId, event) -> event.employeeId(), ...)
    .aggregate(..., "employee_store");

```

---
@snap[north span-100 headline]
### Concern?
@snapend

How to schedule an event?

Isn't events about the past?

![](assets/img/kafka-topology.jpg)

---
@snap[north span-100 headline]
### PAPI
@snapend

```
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
```

---
@title[Customize Slide Layout]

@snap[west span-50]
## Customize Slide Content Layout
@snapend

@snap[east span-50]
![](assets/img/presentation.png)
@snapend

---?color=#E58537
@title[Add A Little Imagination]

@snap[north-west]
#### Add a splash of @color[cyan](**color**) and you are ready to start presenting...
@snapend

@snap[west span-55]
@ul[spaced text-white]
- You will be amazed
- What you can achieve
- *With a little imagination...*
- And **GitPitch Markdown**
@ulend
@snapend

@snap[east span-45]
@img[shadow](assets/img/conference.png)
@snapend

---?image=assets/img/presenter.jpg

@snap[north span-100 headline]
## Now It's Your Turn
@snapend

@snap[south span-100 text-06]
[Click here to jump straight into the interactive feature guides in the GitPitch Docs @fa[external-link]](https://gitpitch.com/docs/getting-started/tutorial/)
@snapend