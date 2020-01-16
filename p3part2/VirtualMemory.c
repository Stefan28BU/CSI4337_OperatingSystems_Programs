/*
 * Author:       Yufan Xu
 * Assignment:   Project 3 - part 2: Virtual Memory
 * File:         VirtualMemory.c
 * Description:  This program stimulates virtual memory page replacement
 *               algorithms FIFO and LRU and analyze each of them
 * Date Created: 3/10/2019
 * Due Date:     3/25/2019
 */

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <time.h>

typedef struct sharedVariables {
    double averageFaults;
    int numOfBelady;
    double percentOfBelady;
    int count;
    int increaseTime;
    int pageFaultCount;
} sharedVariables;

typedef struct FIFO {
    sharedVariables *vars;
    int fifoIndex;
    int *pageFaultArr;
    int *frameQueue;
} FIFO;

typedef struct LRU {
    sharedVariables *vars;
    int *lruCounterArr;
    int *uniqueReferenceArr;
    int position;
    int *frameQueue;
    int *pageFaultArr;
} LRU;

/* used by LRU */
int checkReferenceStringDuplication(int, int, int *);

/* used by LRU */
int getReferenceStringPosition(int *, int, int);

/* used by LRU */
int getPagePosition(int *stream, int size, int value);

/* used by LRU */
void createLookUpLRUCounterArray(int *, int *, int, int);

/* used by LRU */
int checkDuplicationInStream(int n, int size, int *stream);

/* used by LRU */
int getLRUIndex(int *arr, int n);

/* used by LRU and FIFO */
int checkIfPageInMemory(int, int, int *);

/* used by LRU and FIFO */
void insertionSort(int *, int);

/* used by LRU and FIFO */
double calculateAverageFaults(int *, int);

/* allocate memory for FIFO */
FIFO *initializeFIFO(int NUM_ITERATIONS, int f);

/* allocate memory for LRU */
LRU *initializeLRU(int NUM_ITERATIONS, int NUM_ACCESSES, int f);

/* clean up all memory allocated in FIFO */
void freeFIFO(FIFO *fifo);

/* clean up all memory allocated in LRU */
void freeLRU(LRU *lru);

/* runs FIFO algorithm */
void executeFIFO(FIFO *fifo, int *refStream, int NUM_ACCESSES, int f);

/* runs LRU algorithm */
void executeLRU(LRU *lru, int *refStream, int NUM_ACCESSES, int f);

/* calculate belady occurence percent */
double calculatePercentOfBelady(double numOfBelady, double totalIteration);

/* display analysis */
void displayAnalysis(FIFO *, FIFO *, FIFO *, LRU *, int, int, int);

int main(int argc, char **argv) {
    errno = 0;

    /* check for correct number of inputs */
    if (argc != 5) {
        fprintf(stderr, "Please Enter 4 inputs, use only numbers\n");
        exit(1);
    }

    int i, j;

    /* parse command line arguments */
    int f = (int) strtol(argv[1], NULL, 10);
    int fPlusOne = f + 1;
    int fPlusTwo = f + 2;
    int x = (int) strtol(argv[2], NULL, 10);
    int NUM_ACCESSES = (int) strtol(argv[3], NULL, 10);
    int NUM_ITERATIONS = (int) strtol(argv[4], NULL, 10);

    /* check for valid input */
    if (errno != 0) {
        fprintf(stderr, "Please enter numbers only, abort\n");
        exit(1);
    } else if (f <= 0 || x <= 0 || NUM_ACCESSES <= 0 || NUM_ITERATIONS <= 0) {
        fprintf(stderr, "Please enter numbers that >= 1 only, abort\n");
        exit(1);
    }

    time_t t;

    /* allocating memory */
    FIFO *fifo = initializeFIFO(NUM_ITERATIONS, f);
    FIFO *fifoFramePlusOne = initializeFIFO(NUM_ITERATIONS, fPlusOne);
    FIFO *fifoFramePlusTwo = initializeFIFO(NUM_ITERATIONS, fPlusTwo);

    LRU *lru = initializeLRU(NUM_ITERATIONS, NUM_ACCESSES, f);

    int *refStream = (int *) malloc(NUM_ACCESSES * sizeof(int));
    int *tempArray = (int *) malloc(NUM_ACCESSES * sizeof(int));

    /* initialize variables */
    for (i = 0; i < NUM_ITERATIONS; i++) {
        fifo->pageFaultArr[i] = 0;
        fifoFramePlusOne->pageFaultArr[i] = 0;
        fifoFramePlusTwo->pageFaultArr[i] = 0;
        lru->pageFaultArr[i] = 0;
    }

    srand((unsigned) time(&t));

    fifoFramePlusOne->vars->numOfBelady = 0;
    fifoFramePlusTwo->vars->numOfBelady = 0;

    for (i = 0; i < NUM_ITERATIONS; i++) {
        fifo->fifoIndex = 0;
        fifo->vars->count = 0;
        fifo->vars->pageFaultCount = 0;

        fifoFramePlusOne->fifoIndex = 0;
        fifoFramePlusOne->vars->count = 0;
        fifoFramePlusOne->vars->pageFaultCount = 0;

        fifoFramePlusTwo->fifoIndex = 0;
        fifoFramePlusTwo->vars->count = 0;
        fifoFramePlusTwo->vars->pageFaultCount = 0;

        lru->position = 0;
        lru->vars->count = 0;
        lru->vars->increaseTime = 0;
        lru->vars->pageFaultCount = 0;

        for (j = 0; j < NUM_ACCESSES; j++) {
            refStream[j] = 0;
            lru->uniqueReferenceArr[j] = 0;
            lru->lruCounterArr[j] = 0;
        }

        for (j = 0; j < f; j++) {
            fifo->frameQueue[j] = 0;
            fifoFramePlusOne->frameQueue[j] = 0;
            fifoFramePlusTwo->frameQueue[j] = 0;
            lru->frameQueue[j] = 0;
        }

        for (j = 0; j < NUM_ACCESSES; j++) {
            tempArray[j] = 0;
        }

        for (j = 0; j < NUM_ACCESSES; j++) {
            refStream[j] = 1 + rand() % x;
        }

        /* run FIFO f, FIFO f + 1, FIFO f + 2 */
        executeFIFO(fifo, refStream, NUM_ACCESSES, f);
        executeFIFO(fifoFramePlusOne, refStream, NUM_ACCESSES, fPlusOne);
        executeFIFO(fifoFramePlusTwo, refStream, NUM_ACCESSES, fPlusTwo);

        /* record times when Belady occurs */
        if (fifoFramePlusOne->vars->pageFaultCount > fifo->vars->pageFaultCount) {
            fifoFramePlusOne->vars->numOfBelady++;
        }

        if (fifoFramePlusTwo->vars->pageFaultCount > fifo->vars->pageFaultCount) {
            fifoFramePlusTwo->vars->numOfBelady++;
        }

        /* store page fault counters */
        fifo->pageFaultArr[i] =
                fifo->vars->pageFaultCount;

        fifoFramePlusOne->pageFaultArr[i] =
                fifoFramePlusOne->vars->pageFaultCount;

        fifoFramePlusTwo->pageFaultArr[i] =
                fifoFramePlusTwo->vars->pageFaultCount;

        /* create a reference array for LRU counter array */
        for (j = 0; j < NUM_ACCESSES; j++) {
            createLookUpLRUCounterArray(
                    lru->uniqueReferenceArr,
                    tempArray,
                    refStream[j],
                    j + 1);
        }

        /* run LRU */
        executeLRU(lru, refStream, NUM_ACCESSES, f);

        lru->pageFaultArr[i] = lru->vars->pageFaultCount;
    }

    /* get MAX and MIN faults and AVG faults for each algorithm */
    insertionSort(lru->pageFaultArr, NUM_ITERATIONS);
    lru->vars->averageFaults =
            calculateAverageFaults(lru->pageFaultArr, NUM_ITERATIONS);

    insertionSort(fifo->pageFaultArr, NUM_ITERATIONS);
    insertionSort(fifoFramePlusOne->pageFaultArr, NUM_ITERATIONS);
    insertionSort(fifoFramePlusTwo->pageFaultArr, NUM_ITERATIONS);

    fifo->vars->averageFaults = calculateAverageFaults(
            fifo->pageFaultArr, NUM_ITERATIONS
    );

    fifoFramePlusOne->vars->averageFaults = calculateAverageFaults(
            fifoFramePlusOne->pageFaultArr, NUM_ITERATIONS
    );

    fifoFramePlusTwo->vars->averageFaults = calculateAverageFaults(
            fifoFramePlusTwo->pageFaultArr, NUM_ITERATIONS
    );

    /* calculate Belady percent */
    fifoFramePlusOne->vars->percentOfBelady =
            calculatePercentOfBelady(
                    (double) fifoFramePlusOne->vars->numOfBelady,
                    (double) NUM_ITERATIONS
            );

    fifoFramePlusTwo->vars->percentOfBelady =
            calculatePercentOfBelady(
                    (double) fifoFramePlusTwo->vars->numOfBelady,
                    (double) NUM_ITERATIONS
            );

    /* display result */
    displayAnalysis(fifo, fifoFramePlusOne,
                    fifoFramePlusTwo, lru,
                    NUM_ITERATIONS, NUM_ACCESSES, f
    );

    /* clean up heap memory */
    freeFIFO(fifo);
    freeFIFO(fifoFramePlusOne);
    freeFIFO(fifoFramePlusTwo);
    freeLRU(lru);
    free(refStream);
    free(tempArray);

    return 0;
}

int checkIfPageInMemory(int n, int size, int *memory) {
    int i;

    int isFound = 0;

    for (i = 0; i < size; i++) {

        if (n == memory[i]) {
            isFound = 1;
            break;
        }
    }

    return isFound;
}

int checkDuplicationInStream(int n, int size, int *stream) {
    int i;

    int isFound = 0;

    for (i = 0; i < size; i++) {

        if (n == stream[i]) {
            isFound = 1;
            break;
        }
    }

    return isFound;
}


void insertionSort(int *array, int n) {
    int i, key, j;

    for (i = 1; i < n; i++) {
        key = array[i];
        j = i - 1;

        while (j >= 0 && array[j] > key) {
            array[j + 1] = array[j];
            j = j - 1;
        }
        array[j + 1] = key;
    }
}

int getReferenceStringPosition(int *stream, int size, int value) {
    int i;
    int position = 0;

    for (i = 0; i < size; i++) {
        if (value == stream[i]) {
            position = i;
            break;
        }
    }

    return position;
}

int getPagePosition(int *stream, int size, int value) {
    int i;
    int position = 0;

    for (i = 0; i < size; i++) {
        if (value == stream[i]) {
            position = i;
            break;
        }
    }

    return position;
}

void createLookUpLRUCounterArray(int *arr, int *temp, int val, int size) {

    int i;

    if (size == 1) {
        arr[0] = val;
    } else {

        if (checkReferenceStringDuplication(val, size, arr) == 0) {
            for (i = 0; i < size; i++) {
                temp[i] = arr[i];
            }
            temp[size - 1] = val;
        } else {
            for (i = 0; i < size; i++) {
                temp[i] = arr[i];
            }
        }

        for (i = 0; i < size; i++) {
            arr[i] = temp[i];
        }
    }
}


int checkReferenceStringDuplication(int n, int size, int *arr) {
    int i;

    int isFound = 0;

    for (i = 0; i < size; i++) {

        if (n == arr[i]) {
            isFound = 1;
            break;
        }
    }

    return isFound;
}

int getLRUIndex(int *arr, int n) {
    int i, retIndex = 0;

    int min = arr[0];

    for (i = 1; i < n; i++) {
        if (arr[i] < min) {
            min = arr[i];
            retIndex = i;
        }
    }

    return retIndex;
}

int getMaxTime(int *arr, int n) {
    int i;

    int max = arr[0];
    for (i = 1; i < n; i++) {
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    return max;
}

double calculateAverageFaults(int *array, int size) {
    int sum = 0, i;
    double avg = 0;

    for (i = 0; i < size; i++) {
        sum = sum + array[i];
    }
    avg = (double) sum / i;

    return avg;
}

FIFO *initializeFIFO(int NUM_ITERATIONS, int f) {
    FIFO *fifo = (FIFO *) malloc(sizeof(FIFO));
    fifo->vars = (sharedVariables *) malloc(sizeof(sharedVariables));

    fifo->frameQueue = (int *) malloc(f * sizeof(int));
    fifo->pageFaultArr = (int *) malloc(NUM_ITERATIONS * sizeof(int));

    return fifo;
}


LRU *initializeLRU(int NUM_ITERATIONS, int NUM_ACCESSES, int f) {
    LRU *lru = (LRU *) malloc(sizeof(LRU));

    lru->vars = (sharedVariables *) malloc(sizeof(sharedVariables));

    lru->frameQueue = (int *) malloc(f * sizeof(int));
    lru->pageFaultArr = (int *) malloc(NUM_ITERATIONS * sizeof(int));

    lru->uniqueReferenceArr = (int *) malloc(NUM_ACCESSES * sizeof(int));
    lru->lruCounterArr = (int *) malloc(NUM_ACCESSES * sizeof(int));

    return lru;
}

void freeFIFO(FIFO *fifo) {
    free(fifo->frameQueue);
    free(fifo->pageFaultArr);
    free(fifo->vars);
    free(fifo);
}

void freeLRU(LRU *lru) {
    free(lru->frameQueue);
    free(lru->pageFaultArr);
    free(lru->uniqueReferenceArr);
    free(lru->lruCounterArr);
    free(lru->vars);
    free(lru);
}

void executeFIFO(FIFO *fifo, int *refStream, int NUM_ACCESSES, int f) {
    int j;
    for (j = 0; j < NUM_ACCESSES; j++) {
        if (fifo->fifoIndex == f) {
            fifo->fifoIndex = 0;
        }

        if (checkIfPageInMemory(refStream[j], f, fifo->frameQueue) == 0) {
            fifo->vars->pageFaultCount++;

            if (fifo->vars->count < f) {
                fifo->frameQueue[fifo->vars->count] = refStream[j];

                fifo->vars->count++;
            } else if (fifo->vars->count == f) {
                fifo->frameQueue[fifo->fifoIndex] = refStream[j];
                fifo->fifoIndex++;
            }
        }
    }
}

void executeLRU(LRU *lru, int *refStream, int NUM_ACCESSES, int f) {
    int j, k, l;

    for (j = 0; j < NUM_ACCESSES; j++) {

        /* get the position of the page needs to be replaced */
        lru->position = getReferenceStringPosition(
                lru->frameQueue, f, refStream[j]
        );

        /* if page not in memory yet, do the following */
        if (checkIfPageInMemory(refStream[j], f, lru->frameQueue) == 0) {
            lru->vars->pageFaultCount++;

            if (lru->vars->count < f) {
                lru->frameQueue[lru->vars->count] = refStream[j];

                if (refStream[lru->vars->count] != 0) {
                    for (k = lru->vars->count; k < lru->vars->count + lru->vars->count; k++) {
                        for (l = 0; l < lru->vars->count + lru->vars->increaseTime; l++) {
                            lru->lruCounterArr[k]++;
                        }
                    }
                }
                lru->vars->count++;

            } else if (lru->vars->count == f) {

                int indexToReplace =
                        getLRUIndex(lru->lruCounterArr, f);

                int pageToReplace =
                        lru->frameQueue[indexToReplace];

                int pageIndex =
                        getPagePosition(lru->frameQueue, f, pageToReplace);

                if (checkDuplicationInStream(refStream[j],
                                             NUM_ACCESSES,
                                             lru->uniqueReferenceArr) == 1) {
                    lru->uniqueReferenceArr[lru->position] = 0;
                }
                lru->uniqueReferenceArr[indexToReplace] = refStream[j];

                lru->frameQueue[pageIndex] = refStream[j];

                lru->lruCounterArr[pageIndex] =
                        getMaxTime(lru->lruCounterArr, NUM_ACCESSES) + 1;
            }
        } else {
            lru->lruCounterArr[lru->position] =
                    getMaxTime(lru->lruCounterArr, NUM_ACCESSES) + 1;

            lru->vars->increaseTime++;
        }
    }
}

double calculatePercentOfBelady(double numOfBelady, double totalIteration) {
    return ((numOfBelady / totalIteration) * 100);
}

void displayAnalysis(FIFO *fifo, FIFO *fifo1, FIFO *fifo2,
                     LRU *lru, int NI, int NA, int f) {
    printf("Number of frames: %d\n", f);
    printf("Number of accesses: %d\n", NA);
    printf("Number of iterations: %d\n", NI);

    printf("Algorithm       ");
    printf("Average faults     ");
    printf("Min faults    ");
    printf("Max faults    ");
    printf("Number of Belady    ");
    printf("Percent of Belady\n");

    printf("FIFO f:%17f%12d%14d\n",
           fifo->vars->averageFaults,
           fifo->pageFaultArr[0],
           fifo->pageFaultArr[NI - 1]
    );
    printf("FIFO f + 1:%13f%12d%14d%14d%27f %%\n",
           fifo1->vars->averageFaults,
           fifo1->pageFaultArr[0],
           fifo1->pageFaultArr[NI - 1],
           fifo1->vars->numOfBelady,
           fifo1->vars->percentOfBelady
    );
    printf("FIFO f + 2:%13f%12d%14d%14d%27f %%\n",
           fifo2->vars->averageFaults,
           fifo2->pageFaultArr[0],
           fifo2->pageFaultArr[NI - 1],
           fifo2->vars->numOfBelady,
           fifo2->vars->percentOfBelady
    );
    printf("LRU:%20f%12d%14d\n",
           lru->vars->averageFaults,
           lru->pageFaultArr[0],
           lru->pageFaultArr[NI - 1]
    );
}