/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atc;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Maxine
 */
class Gate implements Runnable {

    private char name;
    private Airport airport;
    private Aircraft currAircraft;
    private AtomicBoolean isAvailable;

    public Gate(char name, Airport airport) {
        this.name = name;
        this.airport = airport;
        isAvailable = new AtomicBoolean(true);
        currAircraft = null;
    }

    @Override
    public void run() {
        //TODO Close the gate
        //TODO wait if list is empty, and the aircraft need to inform the gate
        while (true) {
            if (isAvailable.get()) {
                try {
                    synchronized (this){
                        //Problem 7
                        //Aircraft that is not given priority to land lands before the aircraft that is given priority
                        //Starvation
                        if ((currAircraft = airport.getUrgentQueue().poll()) == null) {
                            //Just to be safe, the scenario is when another gate already taken the aircraft, the list is empty and may cause starvation.
                            //currAircraft = airport.getUrgentQueue().poll(100, TimeUnit.MILLISECONDS);
                            //currAircraft = airport.getUrgentQueue().poll();
                            currAircraft = airport.getNormalQueue().poll();
                        }
//                        else {
//                            //Just to be safe, the scenario is when the item in queue is changed, the list is empty and may cause starvation.
//                            //currAircraft = airport.getNormalQueue().poll(100, TimeUnit.MILLISECONDS);
//                            currAircraft = airport.getNormalQueue().poll();
//                        }

                        if (currAircraft != null) {
                            //Ensure that the aircraft does not left the airport
                            synchronized (currAircraft) {
                                if (currAircraft.getStatus().compareAndSet(Status.QUEUE, Status.LANDING) || currAircraft.getStatus().compareAndSet(Status.URGENT, Status.LANDING)) {
                                    isAvailable.set(false);
                                    System.out.println(this.getGateCodeName() + " is assigned to "+currAircraft.getAircraftCodeName()+".");
                                    currAircraft.setAssignedGate(this);
                                    this.wait();
                                    System.out.println(this.getGateCodeName() + " is available.");
                                    isAvailable.set(true);

                                }
                            }

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public char getName() {
        return name;
    }

    public void setName(char name) {
        this.name = name;
    }

    public String getGateCodeName() {
        return "Gate " + name;
    }

}
