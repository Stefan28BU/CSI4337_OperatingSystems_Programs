/**************************************************************************
 * file:               ThreadedMonteCarlo.java
 * author:             Yufan Xu
 * assignment:         project 1 - part 2
 * description:        this program uses multiple threads to perform
 *                     monte carlo method for estimating PI
 * date created:       1/30/2018
 * date last modified: 1/30/2018
 * date due:           1/30/2018
 *************************************************************************/

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Scanner;

//class Point for storing coordinates as points
class Point {
    double x, y, z;
}

//this class combines the count of z and total number of threads together
class ThreadManager {
    static int totalValidZ = 0;
    static int threadCount = 0;
}

//MyThread class
class MyThread extends Thread {
    private ArrayList<Point> pointList = null;
    private int countDist = 0;
    private int threadSum = 0;
    private int countDist2 = 0;
    private int threadCount = 0;
    private int remainder = 0;
    private int pointPerThread = 0;
    private int pointForLastThread = 0;
    private int c = 0;
    private int position = 0;

    //constructor to pass all the needed information
    public MyThread(int threadSum1, int threadCount1, int remainder1, int pointPerThread1, int pointForLastThread1, int c1, int position1, ArrayList<Point> pList) {
        threadSum = threadSum1;
        threadCount = threadCount1;
        remainder = remainder1;
        pointPerThread = pointPerThread1;
        pointForLastThread = pointForLastThread1;
        c = c1;
        position = position1;
        pointList = pList;
    }

    //override the original run() methods to separate work for each thread
    public void run(){
        //spawns threads to calculate z and number of points inside of the circle
        if (remainder == 0) {
            for (int j = position; j < position + pointPerThread; j++) {
                countDist2 = getTotalOfValidPoints(j,c);
            }
        } else {
            if (threadCount < threadSum - 1) {
                for (int j = position; j < position + pointPerThread; j++) {
                    countDist2 = getTotalOfValidPoints(j,c);
                }
            } else {
                for (int j = position; j < position + pointForLastThread; j++) {
                    countDist2 = getTotalOfValidPoints(j,c);
                }
            }
        }
        ThreadManager.totalValidZ += countDist2;
        int total = ThreadManager.totalValidZ;

        ThreadManager.threadCount++;
        int count = ThreadManager.threadCount;

        if (count == threadSum) {
            System.out.println();
            System.out.println("Number of 'z' that is <= 1: " + total);
            calculateAndPrintPI(total);
        }
    }

    //return the number of points inside of the circle
    public int getTotalOfValidPoints(int i, int j) {
        double xSqu = pointList.get(i).x * pointList.get(i).x;
        double ySqu = pointList.get(i).y * pointList.get(i).y;

        pointList.get(i).z = xSqu + ySqu;

        System.out.println("Thread[" + j + "]->z=" + pointList.get(i).z);

        if (pointList.get(i).z <= 1) {
            countDist ++;
        }
        return countDist;
    }

    //calculates and print the result of PI
    public void calculateAndPrintPI(int count) {
        double pi = (double) count / pointList.size() * 4;
        BigDecimal PI = new BigDecimal(pi);
        PI = PI.round(new MathContext(7));

        System.out.println();
        System.out.println("<-------------RESULT------------->");
        System.out.println("<                                >");
        System.out.println("< The estimate of PI is "+ PI + " >");
        System.out.println("<                                >");
        System.out.println("<-------------------------------->");
    }
}

//class ThreadedMonteCarlo
public class ThreadedMonteCarlo {

    //this function reads points from file and store them to class
    public static ArrayList<Point> parseData(String fileName) throws FileNotFoundException {
        Scanner inputStream;
        File inputFile = new File(fileName).getAbsoluteFile();

        if (!inputFile.exists()) {
            System.err.println("File does not exist");
            System.exit(1);
        }
        inputStream = new Scanner(inputFile);

        String pointNumStr = inputStream.nextLine();
        int pArrSize = Integer.parseInt(pointNumStr);
        ArrayList<Point> pArr = new ArrayList<>(pArrSize);

        int i = 0;
        while (inputStream.hasNext()) {
            Point point = new Point();
            String dataStr = inputStream.nextLine();
            String [] str1 = dataStr.split(", ");
            point.x = Double.parseDouble(str1[0]);
            point.y = Double.parseDouble(str1[1]);

            pArr.add(point);
            i ++;
        }

        if (pArrSize != i) {
            System.err.println("Wrong number of points in the file");
            System.exit(1);
        }
        System.out.println("Total number of point/points: " + pointNumStr);
        inputStream.close();

        return pArr;
    }

    //main function
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.println("Wrong number of inputs");
            System.exit(1);
        }

        int threadSum = Integer.parseInt(args[1]);
        System.out.println("Total number of thread/threads: " + threadSum);
        ArrayList<Point> pointList = parseData(args[0]);

        //limits the maximum number of threads to the total number of points in the file*/
        //this is for better efficiency and avoid unnecessary work*/
        if (threadSum > pointList.size()) {
            threadSum = pointList.size();
            System.out.print("The value you have entered will create ");
            System.out.print("too many unnecessary threads. For better efficiency, ");
            System.out.println("the total number of threads is reduced to: " + threadSum);
            System.out.println();
        }

        int pointPerThread = pointList.size() / threadSum;
        int remainder = pointList.size() % threadSum;
        int pointForLastThread = pointPerThread + remainder;

        //displays the general information about work separation based on the user input
        if (remainder == 0) {
            System.out.println("Points being stored in each thread: " + pointPerThread);
            System.out.println();
        } else {
            System.out.println("Points being stored in each thread, except the last thread: " + pointPerThread);
            System.out.println("Points being stored in the last thread: " + pointForLastThread);
            System.out.println();
        }

        //displays points from file
        System.out.println("Points read from file: ");
        for (int k = 0; k < pointList.size(); k++) {
            System.out.println("Point[" + k + "]: (" + pointList.get(k).x + ", " + pointList.get(k).y + ")");
        }
        System.out.println();
        System.out.println("Value of 'z' in each child: ");

        int position = 0;
        MyThread [] tList = new MyThread[threadSum];

        int threadCount = 0;
        for (int i = 0 ; i < threadSum; i ++) {
            tList[i] = new MyThread(threadSum, threadCount, remainder, pointPerThread, pointForLastThread, i, position, pointList);
            tList[i].start();

            position += pointPerThread;
            threadCount ++;
        }
    }
}