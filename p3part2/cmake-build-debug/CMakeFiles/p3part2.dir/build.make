# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.8

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /Applications/CLion.app/Contents/bin/cmake/bin/cmake

# The command to remove a file.
RM = /Applications/CLion.app/Contents/bin/cmake/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /Users/Stefan_Xu/Desktop/CSI4337/p3part2

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /Users/Stefan_Xu/Desktop/CSI4337/p3part2/cmake-build-debug

# Include any dependencies generated for this target.
include CMakeFiles/p3part2.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/p3part2.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/p3part2.dir/flags.make

CMakeFiles/p3part2.dir/VirtualMemory.c.o: CMakeFiles/p3part2.dir/flags.make
CMakeFiles/p3part2.dir/VirtualMemory.c.o: ../VirtualMemory.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/Users/Stefan_Xu/Desktop/CSI4337/p3part2/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object CMakeFiles/p3part2.dir/VirtualMemory.c.o"
	/Library/Developer/CommandLineTools/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/p3part2.dir/VirtualMemory.c.o   -c /Users/Stefan_Xu/Desktop/CSI4337/p3part2/VirtualMemory.c

CMakeFiles/p3part2.dir/VirtualMemory.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/p3part2.dir/VirtualMemory.c.i"
	/Library/Developer/CommandLineTools/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /Users/Stefan_Xu/Desktop/CSI4337/p3part2/VirtualMemory.c > CMakeFiles/p3part2.dir/VirtualMemory.c.i

CMakeFiles/p3part2.dir/VirtualMemory.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/p3part2.dir/VirtualMemory.c.s"
	/Library/Developer/CommandLineTools/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /Users/Stefan_Xu/Desktop/CSI4337/p3part2/VirtualMemory.c -o CMakeFiles/p3part2.dir/VirtualMemory.c.s

CMakeFiles/p3part2.dir/VirtualMemory.c.o.requires:

.PHONY : CMakeFiles/p3part2.dir/VirtualMemory.c.o.requires

CMakeFiles/p3part2.dir/VirtualMemory.c.o.provides: CMakeFiles/p3part2.dir/VirtualMemory.c.o.requires
	$(MAKE) -f CMakeFiles/p3part2.dir/build.make CMakeFiles/p3part2.dir/VirtualMemory.c.o.provides.build
.PHONY : CMakeFiles/p3part2.dir/VirtualMemory.c.o.provides

CMakeFiles/p3part2.dir/VirtualMemory.c.o.provides.build: CMakeFiles/p3part2.dir/VirtualMemory.c.o


# Object files for target p3part2
p3part2_OBJECTS = \
"CMakeFiles/p3part2.dir/VirtualMemory.c.o"

# External object files for target p3part2
p3part2_EXTERNAL_OBJECTS =

p3part2: CMakeFiles/p3part2.dir/VirtualMemory.c.o
p3part2: CMakeFiles/p3part2.dir/build.make
p3part2: CMakeFiles/p3part2.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/Users/Stefan_Xu/Desktop/CSI4337/p3part2/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking C executable p3part2"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/p3part2.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/p3part2.dir/build: p3part2

.PHONY : CMakeFiles/p3part2.dir/build

CMakeFiles/p3part2.dir/requires: CMakeFiles/p3part2.dir/VirtualMemory.c.o.requires

.PHONY : CMakeFiles/p3part2.dir/requires

CMakeFiles/p3part2.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/p3part2.dir/cmake_clean.cmake
.PHONY : CMakeFiles/p3part2.dir/clean

CMakeFiles/p3part2.dir/depend:
	cd /Users/Stefan_Xu/Desktop/CSI4337/p3part2/cmake-build-debug && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /Users/Stefan_Xu/Desktop/CSI4337/p3part2 /Users/Stefan_Xu/Desktop/CSI4337/p3part2 /Users/Stefan_Xu/Desktop/CSI4337/p3part2/cmake-build-debug /Users/Stefan_Xu/Desktop/CSI4337/p3part2/cmake-build-debug /Users/Stefan_Xu/Desktop/CSI4337/p3part2/cmake-build-debug/CMakeFiles/p3part2.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/p3part2.dir/depend

