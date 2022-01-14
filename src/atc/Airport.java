/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atc;

import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Maxine
 */
class Airport {

    Comparator<Aircraft> fuelTimeSorter = Comparator.comparing(Aircraft::getNoFuelTime);
    private Gate gates[];
    private BlockingQueue<Aircraft> normalQueue;
    private PriorityBlockingQueue<Aircraft> urgentQueue;
    private ReentrantLock runway;
    private Semaphore intersection;

    //todo
    int counter = 0;

    public Airport(int gatesCount, int maxAircraftInQueue) {

        gates = new Gate[gatesCount];
        char gateName = 'A';
        for (int i = 0; i < gatesCount; i++, gateName++) {
            gates[i] = new Gate(gateName, this);

        }

        normalQueue = new ArrayBlockingQueue<>(maxAircraftInQueue, true);

        urgentQueue = new PriorityBlockingQueue<>(gatesCount, fuelTimeSorter);

        runway = new ReentrantLock(true); //To avoid starvation

        intersection = new Semaphore(1, true);

    }

    public void operate() {
        //Gate is open
        for (Gate g : gates) {
            new Thread(g).start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Airport.class.getName()).log(Level.SEVERE, null, ex);
        }
        new Thread() {
            @Override
            public void run() {
                while (counter < 10) {
                    counter++;

//                    for (int i = 1; i <= 3; i++) {
//                        new Thread(new Aircraft("" + counter+"-"+i, Airport.this)).start();
//                    }
                   new Thread(new Aircraft("" + counter, Airport.this)).start();

                    try {
                        this.sleep(new Random().nextInt(4) * 1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

//    public boolean aircraftComing(Aircraft a) throws InterruptedException {
//        if (!normalQueue.add(a)) {
//            System.out.println("Too many planes are queueing.");
//            return false;
//        }
//        return true;
//    }
//    public Aircraft aircraftLanding() throws InterruptedException {
//        try {
//            runway.unlock();
//            return normalQueue.take();
//        } finally {
//            runway.lock();
//        }
//    }
    public Gate[] getGates() {
        return gates;
    }

    public void setGates(Gate[] gates) {
        this.gates = gates;
    }

    public BlockingQueue<Aircraft> getNormalQueue() {
        return normalQueue;
    }

    public void setNormalQueue(BlockingQueue<Aircraft> normalQueue) {
        this.normalQueue = normalQueue;
    }

    public PriorityBlockingQueue<Aircraft> getUrgentQueue() {
        return urgentQueue;
    }

    public void setUrgentQueue(PriorityBlockingQueue<Aircraft> urgentQueue) {
        this.urgentQueue = urgentQueue;
    }

    public ReentrantLock getRunway() {
        return runway;
    }

    public void setRunway(ReentrantLock runway) {
        this.runway = runway;
    }

    public Semaphore getIntersection() {
        return intersection;
    }

    public void setIntersection(Semaphore intersection) {
        this.intersection = intersection;
    }

}
