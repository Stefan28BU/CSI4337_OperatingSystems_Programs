cmake_minimum_required(VERSION 3.8)
project(P1)

set(CMAKE_C_STANDARD 99)

set(SOURCE_FILES MonteCarlo.c)
add_executable(P1 ${SOURCE_FILES})