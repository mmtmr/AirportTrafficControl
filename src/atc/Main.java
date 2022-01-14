/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package atc;

/**
 *
 * @author Maxine
 */
public class Main {

    public static int gateCount = 2;
    public static int minutesToMilliseconds = 30;
    public static int maximumTimeToUndock = (75 + (gateCount*2-2)) * minutesToMilliseconds; //Added the maximum time the aircraft will have to wait to get passed the intersection
    public static int maximumTimeToDock = (12 + (gateCount*2-2)) * minutesToMilliseconds; //Added the maximum time the aircraft will have to wait to get passed the intersection

    public static int durationToAnotherAirport = 30 * minutesToMilliseconds;
    public static int bufferingTime = 1 * minutesToMilliseconds;
    public static int maximumTimeFromLandingToDock = 8 * minutesToMilliseconds;

    public static void main(String[] args) {
       
        Airport asiaPacificAirport = new Airport(gateCount, 10); //maxAircraftInQueue=10; gateCount=2
        asiaPacificAirport.operate();
    }
}
