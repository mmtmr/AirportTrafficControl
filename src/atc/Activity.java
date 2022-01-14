/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atc;

import static atc.Main.minutesToMilliseconds;
import java.util.Random;


/**
 *
 * @author Maxine
 */
class Activity implements Runnable {

    private int id;
    private String name;
    private String aircraftCodeName;
    private int rangeInMinutes;
    private int minTimeInMinutes;
    private int duration = 0;

    public Activity(int id,  String aircraftCodeName,String name, int rangeInMinutes, int minTimeInMinutes) {
        this.id = id;
        this.name = name;
        this.aircraftCodeName = aircraftCodeName;
        this.rangeInMinutes = rangeInMinutes;
        this.minTimeInMinutes = minTimeInMinutes;
    }

    @Override
    public void run() {
        try {
            //System.out.println(aircraftCodeName+" ongoing activity " + id + " " + name);
            duration = new Random().nextInt(rangeInMinutes * minutesToMilliseconds) + minTimeInMinutes * minutesToMilliseconds;
            Thread.sleep(duration);
            //System.out.println(aircraftCodeName+" completed activity " + id + " " + name + " in " + duration + " milliseconds.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
