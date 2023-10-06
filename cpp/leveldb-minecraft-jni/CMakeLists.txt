cmake_minimum_required(VERSION 3.9)
project(leveldb-jni)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

if(CMAKE_CXX_COMPILER_ID STREQUAL "MSVC")
  # Disable C++ exceptions.
  string(REGEX REPLACE "/EH[a-z]+" "" CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}")
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /EHs-c-")
  add_definitions(-D_HAS_EXCEPTIONS=0)

  # Disable RTTI.
  string(REGEX REPLACE "/GR" "" CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}")
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /GR-")
else(CMAKE_CXX_COMPILER_ID STREQUAL "MSVC")
  # Enable strict prototype warnings for C code in clang and gcc.
  if(NOT CMAKE_C_FLAGS MATCHES "-Wstrict-prototypes")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wstrict-prototypes")
  endif(NOT CMAKE_C_FLAGS MATCHES "-Wstrict-prototypes")

  # Disable C++ exceptions.
  string(REGEX REPLACE "-fexceptions" "" CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}")
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-exceptions")

  # Disable RTTI.
  string(REGEX REPLACE "-frtti" "" CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}")
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-rtti")
endif(CMAKE_CXX_COMPILER_ID STREQUAL "MSVC")

add_library(leveldb-jni SHARED "leveldb_jni.cpp")

if(NOT ANDROID)
  find_package(JNI REQUIRED)
  target_include_directories(leveldb-jni PRIVATE ${JAVA_INCLUDE_PATH})
  target_include_directories(leveldb-jni PRIVATE ${JAVA_INCLUDE_PATH2})
endif()

if(UNIX)
  set(ZLIB_NAME libz.a)
else()
  set(ZLIB_NAME zlibstatic)
endif()

find_library(ZLIB_LIBRARY_PATH ${ZLIB_NAME})
find_library(LEVELDB_LIBRARY_PATH leveldb)
find_path(LEVELDB_INCLUDE_PATH leveldb/db.h)

target_include_directories(leveldb-jni PRIVATE ${LEVELDB_INCLUDE_PATH})

target_link_libraries(leveldb-jni ${ZLIB_LIBRARY_PATH})
target_link_libraries(leveldb-jni ${LEVELDB_LIBRARY_PATH})

install(TARGETS leveldb-jni)