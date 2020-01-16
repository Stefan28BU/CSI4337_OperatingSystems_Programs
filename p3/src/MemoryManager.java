/*
 * Author:       Yufan Xu
 * Assignment:   Project 3 - part 1: Main Memory
 * File:         MemoryManager.java
 * Description:  This program stimulates a variable-partition memory
 *               Management system
 * Date Created: 3/10/2019
 * Due Date:     3/25/2019
 */

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

public class MemoryManager {
    private static boolean quit = false;
    private static List<Integer> memory = null;
    private static Map<Integer, Integer> blockMap = null;

    // This function displays the contents in required format
    private static void display() {
        int c = 0;

        StringBuilder[] strArr = new StringBuilder[memory.size()];

        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = new StringBuilder();
        }

        for (int i = 0; i < memory.size(); i++) {

            String s = Integer.toString(i);
            strArr[c].append(s);
            strArr[c].append(" ");
            c++;

            if (memory.get(i) == -1) {
                if (i < memory.size() - 1) {
                    if (memory.get(i + 1) == -1) {
                        c--;
                    }
                }
            } else {
                if (i < memory.size() - 1) {
                    if (memory.get(i).equals(memory.get(i + 1))) {
                        c--;
                    }
                }
            }
        }

        strArr[strArr.length - 1] = new StringBuilder();

        System.out.println("Bytes:");

        for (StringBuilder str : strArr) {
            String outStr = "";

            String s = str.toString();
            if (!s.equals("")) {

                String[] sArr = s.split(" ");

                int first = Integer.parseInt(sArr[0]);

                int value = memory.get(first);

                if (value == -1) {
                    outStr = "Free";
                } else {
                    outStr = "Item " + value;
                }

                int beginIndex = Integer.parseInt(sArr[0]) + 1;
                int endIndex = Integer.parseInt(sArr[sArr.length - 1]) + 1;

                if (beginIndex != endIndex) {
                    System.out.println(beginIndex + "-" + endIndex + "    " + outStr);
                } else {
                    System.out.println(beginIndex + "      " + outStr);
                }
            }
        }
    }

    // This function update current free blocks in memory,
    // as well as combining adjacent free blocks into one
    private static void updateFreeBlocks() {
        blockMap.clear();

        int key = 0;
        int holeSize = 0;
        for (int i = 0; i < memory.size(); i++) {
            if (memory.get(i) == -1) {
                key = i - holeSize;
                holeSize++;

                blockMap.put(key, holeSize);
            } else {
                key++;
                key += holeSize;
                holeSize = 0;
            }
        }
    }

    // This function returns the smallest block that can fit the process, otherwise
    // it returns null
    private static Map.Entry<Integer, Integer> getSmallestAvailableHole(int pSize) {
        Map.Entry<Integer, Integer> smallestHole = null;

        for (Map.Entry<Integer, Integer> entry : blockMap.entrySet()) {
            if (smallestHole == null || smallestHole.getValue() > entry.getValue()) {
                if (entry.getValue() >= pSize) {
                    smallestHole = entry;
                }
            }
        }
        return smallestHole;
    }

    // This function returns the biggest block that can fit the process, otherwise
    // it returns null
    private static Map.Entry<Integer, Integer> getBiggestAvailableHole(int pSize) {
        Map.Entry<Integer, Integer> biggestHole = null;

        for (Map.Entry<Integer, Integer> entry : blockMap.entrySet()) {
            if (biggestHole == null || biggestHole.getValue() < entry.getValue()) {
                if (entry.getValue() >= pSize) {
                    biggestHole = entry;
                }
            }
        }
        return biggestHole;
    }

    // This function returns the first block that can fit the process, otherwise
    // it returns null
    private static Map.Entry<Integer, Integer> getFirstAvailableHole(int pSize) {
        Map.Entry<Integer, Integer> firstHole = null;

        for (Map.Entry<Integer, Integer> entry : blockMap.entrySet()) {
            if (entry.getValue() >= pSize) {
                firstHole = entry;
                break;
            }
        }
        return firstHole;
    }

    // This function demonstrates the best fit algorithm
    private static void bestFit(int pSize, int pid) {
        Map.Entry<Integer, Integer> smallestHole = null;

        updateFreeBlocks();

        if ((smallestHole = getSmallestAvailableHole(pSize)) == null) {
            System.out.println("Cannot fit item " + pid);
        } else {
            System.out.println("Successfully added item " + pid);

            for (int i = smallestHole.getKey(); i < smallestHole.getKey() + pSize; i++) {
                memory.set(i, pid);
            }
            updateFreeBlocks();
        }
    }

    // This function demonstrates the worst fit algorithm
    private static void worstFit(int pSize, int pid) {
        Map.Entry<Integer, Integer> biggestHole = null;

        updateFreeBlocks();

        if ((biggestHole = getBiggestAvailableHole(pSize)) == null) {
            System.out.println("Cannot fit item " + pid);
        } else {
            System.out.println("Successfully added item " + pid);

            for (int i = biggestHole.getKey(); i < biggestHole.getKey() + pSize; i++) {
                memory.set(i, pid);
            }
            updateFreeBlocks();
        }
    }

    // This function demonstrates the first fit algorithm
    private static void firstFit(int pSize, int pid) {
        Map.Entry<Integer, Integer> firstHole = null;

        updateFreeBlocks();

        if ((firstHole = getFirstAvailableHole(pSize)) == null) {
            System.out.println("Cannot fit item " + pid);
        } else {
            System.out.println("Successfully added item " + pid);

            for (int i = firstHole.getKey(); i < firstHole.getKey() + pSize; i++) {
                memory.set(i, pid);
            }
            updateFreeBlocks();
        }
    }

    // This functions frees the process with id, creates a new free block
    private static void free(int pid) {
        int key = 0;
        int pS = 0;

        for (int i = 0; i < memory.size(); i++) {
            if (memory.get(i) == pid) {
                memory.set(i, -1);

                key = i - pS;
                pS++;
            }
        }
        blockMap.put(key, pS);
    }

    // Main function
    public static void main(String[] args) {

        // Check if user entered the memory size as a command-line argument
        if (args.length != 1) {
            System.err.print("You need to specify size of memory first, ");
            System.err.println("use only one command-line argument");
            System.exit(1);
        }
        String command = "";
        int id = 0;
        int size = 0;
        String method = "";
        int memSize = Integer.parseInt(args[0]);

        // Allocate memory for data structures
        memory = new ArrayList<>(memSize);
        blockMap = new HashMap<>(memSize);

        // Initialize memory with -1 meaning it is empty
        for (int i = 0; i < memSize; i++) {
            memory.add(-1);
        }

        // Prints a menu to direct user input
        // User can still enter invalid inputs
        // The program handles them
        System.out.println("Now you can enter command :)");
        System.out.println("Choose from:");
        System.out.println("1. a <id> <size> <method>");
        System.out.println("   available methods: f, b, w");
        System.out.println("2. f <id>");
        System.out.println("   id should be non-negative numbers");
        System.out.println("3. d");
        System.out.println("4. q");

        // Now prompt user input from standard in
        Scanner reader = new Scanner(System.in);

        // Program keeps running until user hits the quit key (q)
        // Many input validation and error handling performed in the loop
        while (!quit) {
            String userInput = reader.nextLine();
            String[] inputArr = userInput.split(" ");

            if (inputArr.length != 4 && inputArr.length != 2 && inputArr.length != 1) {
                System.err.println("Wrong number of inputs, you need 4 or 2 or 1, abort");
                System.exit(1);
            }

            if (inputArr.length == 4) {
                command = inputArr[0];

                try {
                    id = Integer.parseInt(inputArr[1]);
                    size = Integer.parseInt(inputArr[2]);
                } catch (NumberFormatException ex) {
                    System.err.println("You need to enter numbers for id and size, abort");
                    System.exit(1);
                }

                method = inputArr[3];

                if (id < 0) {
                    System.out.println("Cannot have negative ID for your item");
                } else {
                    if (memory.contains(id)) {
                        System.out.println("Item " + id + " is already in memory");
                    } else {
                        if (command.equalsIgnoreCase("a")) {
                            if (method.equalsIgnoreCase("b")) {
                                bestFit(size, id);
                            } else if (method.equalsIgnoreCase("w")) {
                                worstFit(size, id);
                            } else if (method.equalsIgnoreCase("f")) {
                                firstFit(size, id);
                            } else {
                                System.out.println("No Such Method");
                            }
                        }
                    }
                }
            } else {
                command = inputArr[0];

                if (command.equalsIgnoreCase("q")) {
                    quit = true;
                } else if (command.equalsIgnoreCase("f")) {

                    try {
                        id = Integer.parseInt(inputArr[1]);
                    } catch (IndexOutOfBoundsException e) {
                        System.err.println("Your need to enter the id of item you want to free");
                        System.exit(1);
                    } catch (NumberFormatException e) {
                        System.err.println("Your need to enter the item id in numbers, abort");
                        System.exit(1);
                    }

                    if (memory.contains(id)) {
                        free(id);
                        System.out.println("You have successfully freed item " + id);
                    } else {
                        System.out.println("Item " + id + " not exist in memory");
                    }
                } else if (command.equalsIgnoreCase("d")) {
                    display();
                } else {
                    System.out.println("No Such Command");
                }
            }
        }
        reader.close();
    }
}
