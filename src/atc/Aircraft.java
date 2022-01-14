/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atc;

import static atc.Main.bufferingTime;
import static atc.Main.durationToAnotherAirport;
import static atc.Main.gateCount;
import static atc.Main.maximumTimeToDock;
import static atc.Main.minutesToMilliseconds;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import static atc.Main.maximumTimeToUndock;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Maxine
 */
enum Status {
    NEW(0), QUEUE(1), URGENT(1), LANDING(2), DOCKING(3), GATE(4), UNDOCKING(5), TAKEOFF(6), LEFT(7), ISSUE(-1);

    private int phase;

    private Status(int phase) {
        this.phase = phase;
    }

    public int getPhase() {
        return phase;
    }

}

class Aircraft implements Runnable {

    Random rnd = new Random();
    private String id;
    private int fuelTime;
    private long arriveTime;
    private Airport airport;
    private Gate assignedGate;
    private AtomicReference<Status> status;

    public Aircraft(String id, Airport airport) {
        this.id = id;
        this.airport = airport;
        arriveTime = System.currentTimeMillis();
        //TODO DEBUGGING
        //      fuelTime = (rnd.nextInt(50) + 31) * minutesToMilliseconds;
        fuelTime = rnd.nextInt(maximumTimeToUndock * 5) + (durationToAnotherAirport + bufferingTime);
        status = new AtomicReference<Status>(Status.NEW);
        assignedGate = null;
    }

    @Override
    public void run() {

        //The thread will be alive until the aircraft had taken off
        while (status.get() != Status.LEFT && status.get() != Status.ISSUE) {
            //Add new incoming aircraft into landing queue, there is a limited capacity for landing queue.
            if (status.get() == Status.NEW) {
                try {
                    if (arriveTime + fuelTime - System.currentTimeMillis() + bufferingTime < durationToAnotherAirport) {
                        if (status.compareAndSet(Status.NEW, Status.LEFT)) {
                            System.out.println(this.getAircraftCodeName() + " does not have enough fuel to wait, the pilot is flying to another airport.");
                        } else {
                            System.out.println(this.getAircraftCodeName() + "'s status is illegal!");
                        }
                    } else {
                        airport.getNormalQueue().add(this);
                        if (status.compareAndSet(Status.NEW, Status.QUEUE)) {
                            System.out.println(this.getAircraftCodeName() + " is queueing with " + fuelTime + " milliseconds of fuel time remaining.");
                        } else {
                            System.out.println(this.getAircraftCodeName() + "'s status is illegal!");
                        }
                    }

                } catch (IllegalStateException e) {
                    System.out.println("The airport is too packed. " + this.getAircraftCodeName() + " is flying to another airport.");
                    break;
                }
            }

            //Add aircraft that is running out of fuel into priority queue. Only two aircraft is allowed in the priority queue.
            if (status.get() == Status.QUEUE) {
                //The aircraft will consider the extreme situation that the plane took the maximum time to undock

                if (arriveTime + fuelTime - System.currentTimeMillis() < durationToAnotherAirport + maximumTimeToUndock) {
                    try {

                        //TODO: Do not use PriorityBoundedQueue because it may cause starvation, it is already ensure that the aircraft enters the emergency queue can be landed in time. Instead, ensure fuel time is enough for the plane to travel to another airport.
                        // Or actually can just remove the one from the list
//                        System.out.println(airport.getUrgentQueue().size());
//TODO synchronized priority queue why not needed?
//                        if (airport.getUrgentQueue().size() < 2) {
                        airport.getNormalQueue().remove(this);
                        airport.getUrgentQueue().add(this);
                        if (!(status.compareAndSet(Status.QUEUE, Status.URGENT) || status.get() == Status.LANDING)) { //The gate thread might have already obtain this aircraft before changing status
                            System.out.println(this.getAircraftCodeName() + "'s status is illegal!");
                        }
                        System.out.println(this.getAircraftCodeName() + " is running low on fuel and has been added to the emergency queue.");

//                            System.out.println(airport.getUrgentQueue().size());
//                        } else {
//                            if (status.compareAndSet(Status.QUEUE, Status.LEFT)) { //In case the gate obtained this aircraft
//                                airport.getNormalQueue().remove(this);
//                                if (arriveTime + fuelTime - System.currentTimeMillis() >= durationToAnotherAirport) {
//                                    System.out.println(this.getAircraftCodeName() + " is running low on fuel but the emergency queue if already full. The pilot is flying to another airport.");
//                                } else {
//                                    //When the plane does not have time to fly to another airport
//                                    status.set(Status.ISSUE);
//                                    System.out.println(this.getAircraftCodeName() + " does not have the fuel to fly to another airport. The plane is landing in an abandoned airport just in case.");
//                                }
//                            }
//                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }

            //TODO: Runway control elsewhere using trylock just in case deadlock happens
            // also remember to lock gate --- noneed gate is htread
            //TODO: ERROR HANDLING WHEN GATE IS NULL?
            //Inform that the aircraft is landing and assign a duration for the aircraft to land
            if (status.get() == Status.LANDING) {
                try {
                    int landingTime = rnd.nextInt(3 * minutesToMilliseconds) + 1 * minutesToMilliseconds; //The pilot will also perform emergency landing the fuel time is less than landing time.

                    //Problem 5
                    //Two aircrafts are using the runway at the same time
                    
                    if (airport.getRunway().tryLock(arriveTime + fuelTime - System.currentTimeMillis() - landingTime, TimeUnit.MILLISECONDS)) {
                        System.out.println(this.getAircraftCodeName() + " is using the runway for landing after assigned to " + assignedGate.getGateCodeName() + ".");

                        //safety precaution
//                    if (airport.getRunway().getOwner()==this) {
//                        System.out.println(this.getAircraftCodeName()+" is flying to another airport because the ");
//                    } else {
                        //will it call the landing time twice?
//                    int landingTime = (rnd.nextInt(3) + 1) * minutesToMilliseconds;
//                    System.out.println(this.getAircraftCodeName() + " will complete the landing in " + landingTime / 1000 + " seconds.");
                        Thread.sleep(landingTime);
                        System.out.println(this.getAircraftCodeName() + " has completed landing in " + landingTime + " milliseconds.");
                        if (!status.compareAndSet(Status.LANDING, Status.DOCKING)) {
                            System.out.println(this.getAircraftCodeName() + "'s status is illegal!");

                        }
                        airport.getRunway().unlock();
                        System.out.println(this.getAircraftCodeName() + " has freed the runway access after landing.");
                    } else {
                        System.out.println(this.getAircraftCodeName() + " is emergency landing because running out of fuel waiting for runway. STARVATION" + (arriveTime + fuelTime - System.currentTimeMillis()));
                        status.set(Status.ISSUE);
                        assignedGate.notify();
                    }
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Aircraft docking
            if (status.get() == Status.DOCKING) {
                try {

                    //Intersection if not first gate
                    if (assignedGate.getName() != 'A') {
                        airport.getIntersection().acquire();
                        System.out.println(this.getAircraftCodeName() + " is using the intersection.");
                        Thread.sleep(1 * minutesToMilliseconds);
                        airport.getIntersection().release();
                    }

                    int dockingTime = rnd.nextInt(4 * minutesToMilliseconds);
                    Thread.sleep(dockingTime);
                    System.out.println(this.getAircraftCodeName() + " has docked to " + assignedGate.getGateCodeName() + " in " + dockingTime + " milliseconds.");
                    if (!status.compareAndSet(Status.DOCKING, Status.GATE)) {
                        System.out.println(this.getAircraftCodeName() + "'s status is illegal!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Aircraft activities ongoing simultaneously.
            if (status.get() == Status.GATE) { //TODO INCOMPELTE ADD ACTIVITIES
                try {
                    
                    //Problem 
                    System.out.println(this.getAircraftCodeName() + " is busy.");
                    Thread act4 = new Thread(new Activity(4, this.getAircraftCodeName(), "Passengers disembark", 11, 5));
                    Thread act5 = new Thread(new Activity(5, this.getAircraftCodeName(), "Refill supplies and clean", 21, 10));
                    Thread act6 = new Thread(new Activity(6, this.getAircraftCodeName(), "Refill fuel", 51, 10));
                    Thread act7 = new Thread(new Activity(7, this.getAircraftCodeName(), "Passengers embark", 11, 5));
                    act4.start();
                    act5.start();
                    act6.start();
                    act4.join();
                    act5.join();

                    act7.start();
                    act6.join();
                    act7.join();

                    System.out.println(this.getAircraftCodeName() + " is ready to go! Runway access is granted. Undocking...");
                    if (!status.compareAndSet(Status.GATE, Status.UNDOCKING)) {
                        System.out.println(this.getAircraftCodeName() + "'s status is illegal!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Aircraft undocking
            if (status.get() == Status.UNDOCKING) {
                try {
                    synchronized (assignedGate) {
                        //Intersection if not last gate
                        //Lock the runway, aircraft waits for the runway to become available
                        //Move away from
                        Thread.sleep(1 * minutesToMilliseconds);
                        if (assignedGate.getName() != 'A' + gateCount - 1) {
                            airport.getIntersection().acquire();
                            System.out.println(this.getAircraftCodeName() + " is using the intersection.");
                            Thread.sleep(1 * minutesToMilliseconds);
                            airport.getIntersection().release();
                        }
                        System.out.println(this.getAircraftCodeName() + " is checking for runway availability.");
                        airport.getRunway().lock();
                        int undockingTime = (rnd.nextInt(4 * minutesToMilliseconds));
                        Thread.sleep(undockingTime);
                        System.out.println(this.getAircraftCodeName() + " has undocked from " + assignedGate.getGateCodeName() + " in " + undockingTime + " milliseconds.");
                        if (!status.compareAndSet(Status.UNDOCKING, Status.TAKEOFF)) {
                            System.out.println(this.getAircraftCodeName() + "'s status is illegal!");
                        }
                        assignedGate.notify();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Inform that the aircraft is taking off and assign a duration for the aircraft to take off
            if (status.get() == Status.TAKEOFF) {
                try {
                    int takeOffTime = (rnd.nextInt(3 * minutesToMilliseconds) + 1 * minutesToMilliseconds);
                    Thread.sleep(takeOffTime);
                    System.out.println(this.getAircraftCodeName() + " took off in " + takeOffTime + " milliseconds. We wish them a safe flight.");
                    if (!status.compareAndSet(Status.TAKEOFF, Status.LEFT)) {
                        System.out.println(this.getAircraftCodeName() + "'s status is illegal!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    airport.getRunway().unlock();
                    System.out.println(this.getAircraftCodeName() + " has freed the runway access after taking off.");

                }
            }
             
            if (arriveTime + fuelTime- System.currentTimeMillis() + bufferingTime >= durationToAnotherAirport && arriveTime + fuelTime- System.currentTimeMillis() - durationToAnotherAirport < maximumTimeToDock) { //also larger than the time to another airport
                if (status.compareAndSet(Status.URGENT, Status.LEFT)||status.compareAndSet(Status.QUEUE, Status.LEFT)) {
                    System.out.println(airport.getUrgentQueue().peek()==this||status.get()==Status.QUEUE);
                    airport.getNormalQueue().remove(this);
                    airport.getUrgentQueue().remove(this);
                    System.out.println(this.getAircraftCodeName() + " left for another airport because the airport is too busy and it is running out of fuel.");
                    
                }
            }

            
            //Not allow scenarios
            
            //Problem 2
            //Aircraft does not have enough fuel time to reach another airport, which is when the fuel time is less than 30 minutes.
            if (arriveTime + fuelTime - System.currentTimeMillis() < durationToAnotherAirport && status.get().getPhase() < 2) {
                //Should be leaving from urgent queue
                //System.err.println(this.getAircraftCodeName() + " does not have the fuel to fly to another airport. The plane is landing in an abandoned airport just in case.");
                airport.getNormalQueue().remove(this);
                airport.getUrgentQueue().remove(this);
                status.set(Status.ISSUE);
            }
            
            //Problem 1
            //Aircraft crashes when the fuel time is less than 0. 
            //Starvation
            //PS: Incorrect solution for problem 2.
            if (arriveTime + fuelTime - System.currentTimeMillis() < 0 && status.get().getPhase() <= 2) {
                airport.getNormalQueue().remove(this);
                airport.getUrgentQueue().remove(this);
                System.err.println("BBC News Report: " + this.getAircraftCodeName() + " had crashed in Asia Pacific Airport due to disastrous air traffic control system.");

                if (assignedGate != null) {
                    assignedGate.notify();
                    //No use, runway is still lock
                }

                status.set(Status.ISSUE);
            }

            //Problem 3
            //Aircraft stops at the middle because not enough fuel to coast to the gate
            //Deadlock
            if (arriveTime + fuelTime - System.currentTimeMillis() < 0 && status.get().getPhase() == 3) {
                System.err.println(this.getAircraftCodeName() + " ran out of fuel at the middle of the road. DEADLOCK!");
                status.set(Status.ISSUE);
            }

        }
        if (status.get() == Status.ISSUE) {
            System.err.println(this);
        }
    }

    public String getAircraftCodeName() {
        return "Aircraft No." + id;
    }

    public int getNoFuelTime() {
        return (int) (arriveTime + fuelTime);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFuelTime() {
        return fuelTime;
    }

    public void setFuelTime(int fuelTime) {
        this.fuelTime = fuelTime;
    }

    public long getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(long arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Airport getAirport() {
        return airport;
    }

    public void setAirport(Airport airport) {
        this.airport = airport;
    }

    public Gate getAssignedGate() {
        return assignedGate;
    }

    public void setAssignedGate(Gate assignedGate) {
        this.assignedGate = assignedGate;
    }

    public AtomicReference<Status> getStatus() {
        return status;
    }

    public void setStatus(AtomicReference<Status> status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Aircraft{" + "rnd=" + rnd + ", id=" + id + ", fuelTime=" + fuelTime + ", arriveTime=" + arriveTime + ", airport=" + airport + ", assignedGate=" + assignedGate + ", status=" + status + '}';
    }

}
