cmake_minimum_required(VERSION 3.8)
project(p4)

set(CMAKE_C_STANDARD 99)

set(SOURCE_FILES BinaryTreeWriter.c)
add_executable(p4 ${SOURCE_FILES})