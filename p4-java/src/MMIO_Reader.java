/*
 * Author:                 Yufan Xu
 * Assignment Title:       Project 4 - MMIO_Reader.java
 * Assignment Description: This program uses java Semaphores to read and
 *                         write the binary file we created from c
 * Due Date:               04/08/2019
 * Date Created:           04/08/2019
 * Date Last Modified:     04/08/2019
 */

/*
 * references:
 *     https://en.wikipedia.org/wiki/Readers%E2%80%93writers_problem
 *     https://howtodoinjava.com/java7/nio/memory-mapped-files-mappedbytebuffer/
 */

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class MMIO_Reader {

    private static final int MAX_SLEEP_TIME_Reader = 15000;
    private static final int MAX_SLEEP_TIME_Writer = 10000;
    private static Random rndGen = new Random(new Date().getTime());
    private static MappedByteBuffer byteBuffer = null;
    private static int NUM_READERS = 0;
    private static int NUM_WRITERS = 0;
    private static int readerCount = 0;
    private static int writerCount = 0;

    private static Semaphore readerTry = new Semaphore(1);
    private static Semaphore readerMutex = new Semaphore(1);
    private static Semaphore writerMutex = new Semaphore(1);
    private static Semaphore resource = new Semaphore(1);
    private static Semaphore sharedLock = new Semaphore(1);

    private static int arrSize = 0;
    private static int printCount = 0;

    // class that stores the structure of the binary tree
    private static class Node {
        private int right = -1;
        private int left = -1;
        private char[] value = new char[2];
        private int loc = -1;

        Node() {
            value[0] = ' ';
            value[1] = ' ';
        }
    }

    // function that restores the binary tree structure from binary file
    private static List<Node> reConstructBinaryTree() {

        // initialize binary tree with empty nodes
        List<Node> array = new ArrayList<>(arrSize);
        for (int i = 0; i < arrSize; i++) {
            Node node = new Node();
            array.add(node);
        }

        int nodeCount = 0;
        int value1Index = 4;
        int value2Index = 5;
        int nodeSizeInBytes = 16;
        int count = 0;

        // reconstructing tree
        for (int i = 0; i < arrSize; i++) {
            array.get(i).loc = i;
            array.get(i).left = i + 1 + count;
            array.get(i).right = i + 2 + count;

            if (i + 1 + count >= arrSize) {
                array.get(i).left = -1;
            }
            if (i + 2 + count >= arrSize) {
                array.get(i).right = -1;
            }

            // convert decimal values to from buffer to char and store in tree
            int value1Decimal = byteBuffer.get(value1Index + nodeCount);
            int value2Decimal = byteBuffer.get(value2Index + nodeCount);

            array.get(i).value[0] = (char) value1Decimal;
            array.get(i).value[1] = (char) value2Decimal;

            nodeCount += nodeSizeInBytes;

            count++;
        }

        return array;
    }

    // recursive function that prints the left child of a node and
    // all of its descendents
    private static void printDescendents(Node node, List<Node> array) {

        // formatting output
        if (printCount == 0) {
            System.out.print("Start node: " + node.loc + " -> ");
        } else if (printCount == 1){
            System.out.print(node.loc + "(left child) -> descendents { ");
        } else {
            System.out.print(node.loc + " ");
        }
        printCount++;

        // base case: if such node has no children, return
        if (node.left == -1 && node.right == -1) {
            if (printCount == 1) {
                System.out.print("{ no left child ");
            }
            return;
        }

        // printing left
        if (node.left != -1) {
            printDescendents(array.get(node.left), array);
        }

        // printing right
        if (node.right != -1) {
            printDescendents(array.get(node.right), array);
        }
    }

    private static class Reader implements Runnable {

        @Override
        public void run() {

            // reader runs forever
            while (true) {

                // reader try to enter this section
                readerTry.acquireUninterruptibly();

                // avoid other readers to enter
                readerMutex.acquireUninterruptibly();

                // a reader entered
                readerCount++;

                // if a reader is already in here, lock the critical section
                if (readerCount == 1) {
                    resource.acquireUninterruptibly();
                }

                // reader lock released, but resource can still be read once at a time
                readerMutex.release();

                // reader finishes trying resource
                readerTry.release();

                // double resources lock
                sharedLock.acquireUninterruptibly();

                // ------- critical section -------
                List<Node> array = reConstructBinaryTree();

                Random rn = new Random();
                int index = rn.nextInt(arrSize);

                System.out.print(Thread.currentThread().getName() + ": ");
                printDescendents(array.get(index), array);
                System.out.println("}\n");
                printCount = 0;

                // ----- exit critical section -----

                // release double lock
                sharedLock.release();

                // reserve exit section
                readerMutex.acquireUninterruptibly();

                // now the reader is about to exit
                readerCount--;

                // if a reader is about to exit, make resource available
                if (readerCount == 0) {
                    resource.release();
                }

                // release for exit section
                readerMutex.release();

                // reader sleeps for a while
                try {
                    Thread.sleep(rndGen.nextInt(MAX_SLEEP_TIME_Reader));
                } catch (InterruptedException e) {
                    System.err.println(e.toString());
                    System.exit(1);
                }
            }
        }
    }

    private static class Writer implements Runnable {

        @Override
        public void run() {

            // writer runs forever
            while (true) {

                // a writer is entering, lock to avoid other writers get in
                writerMutex.acquireUninterruptibly();

                // a writer has entered
                writerCount++;

                // if a writer is here, disallow readers to read
                if (writerCount == 1) {
                    readerTry.acquireUninterruptibly();
                }

                // release entry section for readers
                writerMutex.release();

                // double resource lock
                sharedLock.acquireUninterruptibly();

                // lock the resource for other writers
                resource.acquireUninterruptibly();

                // ---------- critical section ----------
                Random rn = new Random();
                int index = rn.nextInt(arrSize) * 16;

                char randomChar = (char) ((int) 'A' + Math.random() * ((int) 'Z' - (int) 'A' + 1));
                char orginalChar = (char) byteBuffer.get(index + 4);
                byteBuffer.put(index + 4, (byte) randomChar);

                char randomChar2 = (char) ((int) 'A' + Math.random() * ((int) 'Z' - (int) 'A' + 1));
                char orginalChar2 = (char) byteBuffer.get(index + 5);
                byteBuffer.put(index + 5, (byte) randomChar2);

                System.out.println(Thread.currentThread().getName() + ": " + (index / 16)
                        + " (modified node) -> { modifiedValue[0] = '"
                        + orginalChar + "' -> "
                        +  "'" + randomChar + "', modifiedValue[1] = '"
                        + orginalChar2 + "' -> " + "'"
                        + randomChar2 + "' }\n");

                // ------- exit critical section -------

                // release the resource for other writers
                resource.release();

                // release shared resource lock
                sharedLock.release();

                // reserve exit section for the writer
                writerMutex.acquireUninterruptibly();

                // the writer is about to exit
                writerCount--;

                // if the writer is about to exit, allow the reader read
                if (writerCount == 0) {
                    readerTry.release();
                }

                // release exit section for writers
                writerMutex.release();

                // writer sleeps for a while
                try {
                    Thread.sleep(rndGen.nextInt(MAX_SLEEP_TIME_Writer));
                } catch (InterruptedException e) {
                    System.err.println(e.toString());
                    System.exit(1);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        // error checking for inputs
        // stores inputs to objects for future use,
        // if a invalid input is detected, terminate
        if (args.length != 3) {
            System.err.println("Wrong number of inputs");
            System.exit(1);
        }

        String fileName = args[0];

        int bufferSize = 0;

        try (RandomAccessFile inputFile = new RandomAccessFile(fileName, "rw")) {
            byteBuffer = inputFile.getChannel()
                    .map(FileChannel.MapMode.READ_WRITE, 0, inputFile.getChannel().size());

            System.out.println("File Size: " + inputFile.getChannel().size());

            bufferSize = (int) inputFile.getChannel().size();

            if (bufferSize == 0) {
                System.out.println("File size is 0, abort");
                System.exit(0);
            }

            arrSize = (int) inputFile.getChannel().size() / 16;
        }

        try {
            NUM_READERS = Integer.parseInt(args[1]);
            NUM_WRITERS = Integer.parseInt(args[2]);

        } catch (NumberFormatException e) {
            System.err.println("Looks like you did not enter numbers for the last two arguments");
            System.exit(1);
        }

        // generate writers
        for (int i = 0; i < NUM_WRITERS; i++) {
            Thread t1 = new Thread(new Writer());
            t1.setName("Writer[" + i + "]");
            t1.start();
        }

        // generate readers
        for (int i = 0; i < NUM_READERS; i++) {
            Thread t1 = new Thread(new Reader());
            t1.setName("Reader[" + i + "]");
            t1.start();
        }
    }
}
