/**************************************************************************
 * file:               MonteCarlo.c
 * author:             Yufan Xu
 * assignment:         project 1 - part 1
 * description:        this program uses multiple processes to perform
 *                     monte carlo method for estimating PI
 * date created:       1/30/2018
 * date last modified: 1/30/2018
 * date due:           1/30/2018
 *************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>
#include <string.h>

/*Point struct*/
typedef struct {
    double x, y, z;
} Point;

/*this function reads points from file and stores them in the struct*/
Point* parseData(const char *fileName, int *size) {
    FILE* fp = fopen(fileName, "r");
    Point *pArr = NULL;
    int pointTotal;

    if (!fp) {
        perror("Failed to load file\n");
        exit(1);
    }

    fscanf(fp, "%d\n", &pointTotal);
    printf("Total number of point/points: %d\n", pointTotal);
    *size = pointTotal;

    pArr = (Point*)malloc(sizeof(Point) * pointTotal);

    int i = 0;
    while (fscanf(fp, "%lf, %lf\n", &pArr[i].x, &pArr[i].y) != EOF) {
        i ++;
    }
    fclose (fp);

    if (pointTotal != i) {
        perror("Wrong number of Points\n");
        exit(1);
    }

    return pArr;
}

/*this function counts the number of points inside of the circle*/
void countValidPoints(double dist, int *count) {
    int temp = *count;
    if (dist <= 1) {
        temp ++;
        *count = temp;
    }
}

/*main function*/
int main(int argc, char ** argv) {
    int childNum, i, j, size, pointPerChild, remainder, pointForLastChild, fd[2];
    double PI;

    if (argc != 3) {
        perror("Wrong number of inputs\n");
        exit(1);
    }

    childNum = atoi(argv[2]);
    printf("Total number of process/processes: %d\n", childNum);

    Point *pointArray = parseData(argv[1], &size);

    /*limits the maximum number of process to the total number of points in the file*/
    /*this is for better efficiency and avoid unnecessary work*/
    if (childNum > size) {
        childNum = size;
        printf("The value you have entered will create ");
        printf("too many unnecessary process. For better efficiency, ");
        printf("the total number of process is reduced to: %d\n\n", childNum);
    }

    pointPerChild = size / childNum;
    remainder = size % childNum;
    pointForLastChild = pointPerChild + remainder;

    /*displays the general information about work separation based on the user input*/
    if (remainder == 0) {
        printf("Points being stored in each child: %d\n\n", pointPerChild);
    } else {
        printf("Points being stored in each child, except the last child: %d\n", pointPerChild);
        printf("Points being stored in the last child: %d\n\n", pointForLastChild);
    }

    /*creates an array of pid with size of user input*/
    int pidArr[childNum];

    /*displays the points read from file*/
    printf("Points read from file: \n");
    for (i = 0; i < size; i++) {
        printf("Point[%d]: (%lf, %lf)\n", i, pointArray[i].x, pointArray[i].y);
    }
    printf("\n");

    int countDist = 0;
    int position = 0;
    int childCount = 0;

    /*spawns processes*/
    /*for child process:*/
    /*calculates distance, number of points inside of the circle, write the result to parent*/

    /*for parent*/
    /*reads the total number of points inside of the circle, calculates PI*/
    printf("Value of 'z' in each child: \n");
    for (i = 0; i < childNum; i ++) {
        if (pipe(fd) == -1) {
            perror("Bad Piping\n");
            exit(1);
        }
        pidArr[i] = fork();

        if (pidArr[i] == -1) {
            perror("Error while forking\n");
            exit(1);
        }
        /*child process*/
        else if (pidArr[i] == 0) {
            if (remainder == 0) {
                for (j = position; j < pointPerChild + position; j++) {
                    pointArray[j].z = pointArray[j].x * pointArray[j].x + pointArray[j].y * pointArray[j].y;
                    printf("Child[%d]->z=%lf\n", i, pointArray[j].z);
                    countValidPoints(pointArray[j].z, &countDist);
                }
            } else {
                if (childCount < childNum - 1) {
                    for (j = position; j < pointPerChild + position; j++) {
                        pointArray[j].z = pointArray[j].x * pointArray[j].x + pointArray[j].y * pointArray[j].y;
                        printf("Child[%d]->z=%lf\n", i, pointArray[j].z);
                        countValidPoints(pointArray[j].z, &countDist);
                    }
                } else {
                    for (j = position; j < position + pointForLastChild; j++) {
                        pointArray[j].z = pointArray[j].x * pointArray[j].x + pointArray[j].y * pointArray[j].y;
                        printf("Child[%d]->z=%lf\n", i, pointArray[j].z);
                        countValidPoints(pointArray[j].z, &countDist);
                    }
                }
            }
            close(fd[0]);
            write(fd[1], &countDist, sizeof(int));
            free(pointArray);
            exit(0);
        }
        /*parent*/
        else {
            close(fd[1]);
            read(fd[0], &countDist, sizeof(int));
            PI = (double) countDist / size * 4;
            position += pointPerChild;
            childCount ++;
        }
    }
    /*waits for all children to complete*/
    for (i = 0; i < childNum; i ++) {
        wait(NULL);
    }

    /*displays result of PI based on the calculation*/
    printf("\nNumber of 'z' that is <= 1: %d \n\n", countDist);
    printf("<-------------RESULT------------->\n");
    printf("<                                >\n");
    printf("< The estimate of PI is %lf >\n", PI);
    printf("<                                >\n");
    printf("<-------------------------------->\n");

    free(pointArray);

    return 0;
}