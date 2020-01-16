/*
 * Author:                 Yufan Xu
 * Assignment Title:       Project 4 - BinaryTreeWriter.c
 * Assignment Description: This program writes the structure of a binary tree
 *                         to a binary file
 *                         Semaphore
 * Due Date:               04/08/2019
 * Date Created:           04/08/2019
 * Date Last Modified:     04/08/2019
 */

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <time.h>

typedef struct {
    int32_t loc;
    char value[2];

    int32_t left;
    int32_t right;
} Node;

int main(int argc, char **argv) {
    errno = 0;
    time_t t;
    srand((unsigned) time(&t));

    /* error checking for input */
    if (argc != 3) {
        fprintf(stderr, "Wrong number of input\n");
        exit(1);
    }

    char *fileName = argv[1];
    int nodeNum = (int) strtol(argv[2], NULL, 10);

    if (errno != 0) {
        fprintf(stderr, "Please enter numbers only, abort\n");
        exit(1);
    } else if (nodeNum <= 0) {
        fprintf(stderr, "Please enter numbers that >= 1 only, abort\n");
        exit(1);
    }

    /* allocating memory for Node array */
    Node *array = (Node*) malloc(sizeof(Node) * nodeNum);

    int i, j;
    int count = 0;

    /* create binary tree structure and stores in array */
    for (i = 0; i < nodeNum; i ++) {
        array[i].loc = i;
        array[i].left = i + 1 + count;
        array[i].right = i + 2 + count;

        if (i + 1 + count >= nodeNum) {
            array[i].left = -1;
        }
        if (i + 2 + count >= nodeNum) {
            array[i].right = -1;
        }

        for (j = 0; j < 2; j++) {
            char c = (char) ('Z' - rand() % 26);
            array[i].value[j] = c;
        }
        count ++;
    }

    /* print structure before store to file */
    for (i = 0; i < nodeNum; i ++) {
        printf("%d %d %d %c %c\n", array[i].loc, array[i].left, array[i].right,
               array[i].value[0], array[i].value[1]);
    }

    FILE *fp;

    fp = fopen(fileName, "w+");

    /* temp array that stores structure read from the file */
    Node *result = (Node*) malloc(sizeof(Node) * nodeNum);

    /* write structure to binary file */
    for (i = 0; i < nodeNum; i ++) {
        fwrite(&array[i], sizeof(Node), 1, fp);
    }

    /* restores file pointer to first address to file */
    rewind(fp);

    /* read from the file that was just written to */
    for (i = 0; i < nodeNum; i ++) {
        fread(&result[i], sizeof(Node), 1, fp);
    }

    /* print the read result to screen to see if they match */
    printf("\n");
    for (i = 0; i < nodeNum; i ++) {
        printf("File: %d %d %d %c %c\n", result[i].loc, result[i].left,
               result[i].right, result[i].value[0], result[i].value[1]);
    }

    /* free memory, close file pointer, exit program */
    fclose(fp);
    free(result);
    free(array);

    return 0;
}