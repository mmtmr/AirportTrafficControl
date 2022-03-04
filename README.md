# Airport Traffic Control (ATC)
Assignment for Concurrent Programming


## Background
### Asia Pacific Airport
Asia Pacific Airport has two gates and one runway for landing and take-off. Each gate and runway only allow one aircraft to access it. The aircraft does not have problem moving from runway to Gate A and moving from Gate B to runway. However, traffic control is needed when aircraft is moving from Gate A to runway, and another aircraft is moving from runway to Gate B. An intersection is created for this purpose.

There is no waiting area on the ground for the aircrafts to wait for the gate to become available, all aircrafts will have to wait in queue. The gate will become available once the aircraft had finished undocked.

### ATC Tasks
An automated Air Traffic Controller (ATC) system will be created for Asia Pacific Airport. The tasks that shall be automated are to guide the aircraft to land, coast to gate, and coast to runway. The biggest challenges to automating these tasks are to ensure that the aircraft does not collide with another aircraft and causes minimum disruption to the airport traffic. These challenges can be accomplished by using appropriate concurrent programming techniques.

### Fuel Shortage in Aircraft
For each aircraft waiting in the queue, the fuel can sustain the aircraft to wait at least the flying time to another airport plus the decision time randomly assigned to every aircraft between one to five minutes. The aircraft will not consume fuel time after landing. The aircraft will be given priority to land if the remaining waiting time is less than the sum of flying time to another airport and the time an aircraft will wait in patience.

### Flow of Aircraft Activities
1. Queuing
2. Landing
3. Docking
4. Gate
5. Undocking
6. Take-Off

## Problem Specification
In order to increase the efficiency of Air Traffic Controller (ATC), the proposed system should tackle the problems that are causing delays and deadlocks.
A poorly planned queuing algorithm is one of the potential sources of delay. This is when the ATC only grants permits to the most recent landing request, ignoring the submitted earlier request. This scenario is also called starvation in concurrent programming. Starvation occurs when fairness is not ensured in the queue. Besides that, the requests from the aircraft that are experiencing fuel shortages are also not prioritised, forcing the aircraft to be landing elsewhere.
The next potential source of delay is complicated traffic management. In Asia Pacific Airport, the aircraft coasting from Gate A to runway and the aircraft coasting from runway to Gate B has the risk to collide, especially when there is a heavy fog. Therefore, ATC takes this security risk seriously.
They always let one of the aircrafts to wait in the sky or at the gate to let the other aircraft use this intersection. However, this causes delay for the gate to become available for another aircraft to land.

To implement an automated system, the causes of deadlocks should be identified explicitly.
Deadlock occurs when thread-safe implementation is introduced to the system to eliminate data race.
For instance, a landing aircraft on the runway is waiting for an available gate to dock at. Likewise, the aircraft in the gates are waiting for the runway to become available to undock from the gate.
These are the conditions that will cause deadlock:
1. Runway and Gate can only be used by one Aircraft. (Serially Reusable Resource)
2. Aircraft will not release the Gate/Runway while waiting for the Runway/Gate to be released. (Incremental Acquisition)
3. Only the Aircraft can release the Runway or Gate used by it. (No pre-emption)
4. Aircraft will perform these operations in order: Land using Runway, Wait at Gate, Takeoff using Runway. The resources obtained are in a cyclic pattern: Runway -> Gate -> Runway. (Cyclic queue)


## Requirements
- Zero Collision
- Smmoth Traffic
- Minimum Disruption
- Fuel Shortage
- Concurrent Activities


## Concepts
- BlockingQueue
- Lock
- Semaphore
- Atomic Variable
- Synchronization

## Additional Information (Road Maps)
![Road Name](https://user-images.githubusercontent.com/76145646/156766982-33d6fff7-4fc8-4f29-8120-e1936615187d.png)
![Road Capacity](https://user-images.githubusercontent.com/76145646/156766997-a96a8096-674f-44c1-90e9-f7e231df1d8c.png)
