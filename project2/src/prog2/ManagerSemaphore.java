/*
 * Author:                 Yufan Xu
 * Assignment Title:       Project 1 - ManagerSemaphore.java
 * Assignment Description: This program synchronizes ManagerMonitor using
 *                         Semaphore
 * Due Date:               02/22/2019
 * Date Created:           02/18/2019
 * Date Last Modified:     02/22/2019
 */

package prog2;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class ManagerSemaphore {

    // Maximum time in between fan arrivals
    private static final int MAX_TIME_IN_BETWEEN_ARRIVALS = 3000;

    // Maximum amount of break time in between celebrity photos
    private static final int MAX_BREAK_TIME = 10000;

    // Maximum amount of time a fan spends in the exhibit
    private static final int MAX_EXHIBIT_TIME = 10000;

    // Minimum number of fans for a photo
    private static final int MIN_FANS = 3;

    // Maximum number of fans allowed in queue
    private static final int MAX_ALLOWED_IN_QUEUE = 10;

    // Holds the queue of fans
    private static ArrayList<Fan> line = new ArrayList<Fan>();

    // The current number of fans in line
    private static int numFansInLine = 0;

    // For generating random times
    private Random rndGen = new Random(new Date().getTime());

    // Semaphore for controlling the maximum number of fans in queue
    private static Semaphore semMax = new Semaphore(MAX_ALLOWED_IN_QUEUE);

    // Semaphore for waking up the celebrity when there are more than 3 fans
    private static Semaphore semMin = new Semaphore(numFansInLine);

    // memory lock for mutual exclusion
    private static Semaphore mutexLock = new Semaphore(1);

    public static void main(String[] args) {
        new ManagerSemaphore().go();
    }

    private void go() {
        // Create the celebrity thread
        Celebrity c = new Celebrity();
        new Thread(c, "Celebrity").start();

        // Continually generate new fans
        int i = 0;

        while (true) {
            new Thread(new Fan(), "Fan " + i++).start();
            try {
                Thread.sleep(rndGen.nextInt(MAX_TIME_IN_BETWEEN_ARRIVALS));
            } catch (InterruptedException e) {
                System.err.println(e.toString());
                System.exit(1);
            }
        }
    }

    class Celebrity implements Runnable {
        @Override
        public void run() {
            while (true) {

                // Block for every less than three fans in line
                semMin.acquireUninterruptibly(MIN_FANS);

                //<---------------mutual exclusion---------------->
                mutexLock.acquireUninterruptibly();

                // Check to see if celebrity flips out
                checkCelebrityOK();

                // Take picture with fans
                System.out.println("Celebrity takes a picture with fans");

                // Adjust the numFans variable
                // Remove the fans from the line
                for (int i = 0; i < MIN_FANS; i++) {
                    System.out.println(line.remove(0).getName() + ": OMG! Thank you!");
                }
                numFansInLine -= MIN_FANS;
                mutexLock.release();
                //<---------------mutual exclusion---------------->

                // Release for increasing the available slots in queue
                semMax.release(MIN_FANS);

                // Take a break
                try {
                    Thread.sleep(rndGen.nextInt(MAX_BREAK_TIME));
                } catch (InterruptedException e) {
                    System.err.println(e.toString());
                    System.exit(1);
                }
            }
        }
    }

    public void checkCelebrityOK() {
        if (numFansInLine > MAX_ALLOWED_IN_QUEUE) {
            System.err.println("Celebrity becomes claustrophobic and flips out");
            System.exit(1);
        }

        if (numFansInLine < MIN_FANS) {
            System.err.println("Celebrity becomes enraged that he was woken from nap for too few fans");
            System.exit(1);
        }
    }

    class Fan implements Runnable {
        String name;

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            // Set the thread name
            name = Thread.currentThread().toString();

            System.out.println(Thread.currentThread() + ": arrives");

            // Look in the exhibit for a little while
            try {
                Thread.sleep(rndGen.nextInt(MAX_EXHIBIT_TIME));
            } catch (InterruptedException e) {
                System.err.println(e.toString());
                System.exit(1);
            }

            // Get in line
            System.out.println(Thread.currentThread() + ": gets in line");

            // When a fan gets in line,
            // acquire for decreasing the available slots in queue,
            // release for the semMin until the available permits
            // access 0 so that the celebrity can be woke up
            semMax.acquireUninterruptibly();

            //<---------------mutual exclusion---------------->
            mutexLock.acquireUninterruptibly();

            line.add(0, this);
            numFansInLine++;

            mutexLock.release();
            //<---------------mutual exclusion---------------->

            semMin.release();
        }
    }
}


