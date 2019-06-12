# Time-based events

This project explores some solutions which provide support for concept related to time based events.

## Theoretical background

A _domain event_ is something that happened in the past. 

However, sometimes not only _aggregates_ are source of events. 
If in our domain an expiration of form of time is an important concept it may be modelled as a _time-based event_. 

For example, **FinancialYearEnded** which is context wide event. 

Moreover, events like **StudentBecameAnAdult** are purely aggregate specific but still trigger by time.
 
 
## Kafka based solution

A codebase contains some proof of concept build with Kafka Streams as an implementation of mechanism for time-based events.


## [Presentation](https://gitpitch.com/jakubiec/time-based-events#/)