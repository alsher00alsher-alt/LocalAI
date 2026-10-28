cmake_minimum_required(VERSION 3.22.1)
project(ahmed_ai_jni CXX C)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(LLAMA_CURL OFF CACHE BOOL "" FORCE)

add_subdirectory(llama.cpp llama-build)
add_library(ahmed_ai_jni SHARED llama_jni.cpp)
target_link_libraries(ahmed_ai_jni llama android log)
