
CMAKE_MINIMUM_REQUIRED(VERSION 2.8)

PROJECT(GecodeQCSP)

# determine, whether we want a static binary
SET(STATIC_LINKING FALSE CACHE BOOL "Build a static binary?")
# do we want static libraries?
IF(STATIC_LINKING)
    # When STATIC_LINKING is TRUE, than cmake looks for libraries ending
    # with .a. This is for linux only!
    SET(CMAKE_FIND_LIBRARY_SUFFIXES ".a")
    SET(CMAKE_EXE_LINKER_FLAGS "-static")
    # Remove flags to get rid off all the -Wl,Bydnamic
    SET(CMAKE_EXE_LINK_DYNAMIC_C_FLAGS)
    SET(CMAKE_EXE_LINK_DYNAMIC_CXX_FLAGS)
    # Use static libs for Boost
    SET(Boost_USE_STATIC_LIBS ON)
    SET(Boost_USE_STATIC_RUNTIME ON)
ENDIF(STATIC_LINKING)

SET(CMAKE_VERBOSE_MAKEFILE TRUE)
SET(GECODE_SRC "${CMAKE_SOURCE_DIR}" CACHE PATH "Path where GeCode source is installed")
SET(GECODE_BIN "${GECODE_SRC}" CACHE PATH "Path where GeCode libs and binaries are installed")

SET(LOG_AUDIT FALSE CACHE BOOL "Set to true to generate log output in parallel qcsp algorithm")

INCLUDE(CheckCXXCompilerFlag)
SET(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_MODULE_PATH})

FIND_PACKAGE(Gecode)

IF(NOT GECODE_FOUND)
  MESSAGE(FATAL_ERROR "Gecode is needed, consider to install it")
ELSE (NOT GECODE_FOUND)
  INCLUDE_DIRECTORIES(${CMAKE_CURRENT_BINARY_DIR})
  INCLUDE_DIRECTORIES(${GECODE_BIN})
  INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR})
  INCLUDE_DIRECTORIES(${GECODE_SRC})

  IF (CMAKE_COMPILER_IS_GNUCXX)
    ADD_DEFINITIONS(-Wall)
    ADD_DEFINITIONS(-Wextra)
    ADD_DEFINITIONS(-Wno-unused-local-typedefs)
    ADD_DEFINITIONS(-fimplement-inlines)
    ADD_DEFINITIONS(-fno-inline-functions)
    ADD_DEFINITIONS(-pipe)
    ADD_DEFINITIONS(-fPIC)
    SET(CMAKE_CXX_FLAGS_DEBUG "-ggdb")
  ENDIF ()

  check_cxx_compiler_flag(-fvisibility=hidden HAVE_VISIBILITY_HIDDEN_FLAG)
  IF (HAVE_VISIBILITY_HIDDEN_FLAG)
    SET(GECODE_GCC_HAS_CLASS_VISIBILITY "/**/")
    ADD_DEFINITIONS(-fvisibility=hidden)
  ENDIF()

  IF (WIN32)
    SET(GECODE_THREADS_WINDOWS 1)
  ELSE ()
    SET(GECODE_THREADS_PTHREADS 1)
  ENDIF ()

  SET(QCSP_LIB_HEADERS
    quacode/qcsp.hh
    quacode/qspaceinfo.hh
    quacode/support/dynamic-list.hh
    quacode/support/log.hh
    quacode/search/sequential/qpath.hh
    quacode/search/sequential/qdfs.hh
    quacode/qint/qbool.hh
  )
  SET(QCSP_LIB_HPP
    quacode/qspaceinfo.hpp
    quacode/search/qdfs.hpp
    quacode/qint/watch.hpp
    quacode/qint/qbool/clause.hpp
    quacode/qint/qbool/eq.hpp
    quacode/qint/qbool/eqv.hpp
    quacode/qint/qbool/or.hpp
    quacode/qint/qbool/xor.hpp
    quacode/qint/qbool/xorv.hpp
  )
  SET(QCSP_LIB_SRCS
    quacode/qspaceinfo.cpp
    quacode/support/log.cpp
    quacode/search/qdfs.cpp
    quacode/search/sequential/qpath.cpp
    quacode/qint/qbool/eqv.cpp
    quacode/qint/qbool/qbool.cpp
    ${GECODE_SRC}/gecode/search/meta/nogoods.cpp
    ${GECODE_SRC}/gecode/search/parallel/engine.cpp
    ${GECODE_SRC}/gecode/search/parallel/path.cpp
  )
  SET(QCSP_EXAMPLES_SRCS
    quacode/examples/qbf.cpp
    quacode/examples/qdimacs.cpp
    quacode/examples/nim-fibo.cpp
    quacode/examples/matrix-game.cpp
    quacode/examples/connect-four.cpp
    quacode/examples/baker.cpp
  )
  SET(FLATZINC_HEADERS
    quacode/flatzinc.hh
    quacode/flatzinc/ast.hh
    quacode/flatzinc/conexpr.hh
    quacode/flatzinc/flatzinc.hh
    quacode/flatzinc/option.hh
    quacode/flatzinc/plugin.hh
    quacode/flatzinc/registry.hh
    quacode/flatzinc/symboltable.hh
    quacode/flatzinc/varspec.hh
  )
  SET(FLATZINC_HPP
  )
  SET(FLATZINC_SRCS
    quacode/flatzinc/fzn-quacode.cpp
    quacode/flatzinc/flatzinc.cpp
    quacode/flatzinc/registry.cpp
  )


  SOURCE_GROUP("Hpp Files" REGULAR_EXPRESSION ".hpp")

  SET_SOURCE_FILES_PROPERTIES(${ALL_HEADERS} PROPERTIES HEADER_FILE_ONLY TRUE)
  SET_SOURCE_FILES_PROPERTIES(${ALL_HPP} PROPERTIES HEADER_FILE_ONLY TRUE)

  SET(EXTRA_SRCS "")

  IF (LOG_AUDIT)
    ADD_DEFINITIONS(-DLOG_AUDIT)
  ENDIF()

  ADD_LIBRARY(quacode ${QCSP_LIB_SRCS} ${EXTRA_SRCS} ${QCSP_LIB_HEADERS} ${QCSP_LIB_HPP})
  SET_TARGET_PROPERTIES(quacode PROPERTIES DEFINE_SYMBOL  "GECODE_BUILD_QCSP")
  SET(QCSP_LIB "")
  IF (BUILD_SHARED_LIBS)
    SET(QCSP_LIB -L${CMAKE_CURRENT_BINARY_DIR} -lquacode)
  ELSE()
    SET(QCSP_LIB quacode)
  ENDIF()

  # Add target for Quacode/FlatZinc
  FIND_PACKAGE(BISON 3.0.0)
  FIND_PACKAGE(FLEX)
  IF (BISON_FOUND AND FLEX_FOUND)
    BISON_TARGET(FLATZINCPARSER quacode/flatzinc/parser.yxx ${CMAKE_CURRENT_BINARY_DIR}/parser.tab.cpp)
    FLEX_TARGET(FLATZINCSCANNER quacode/flatzinc/lexer.lxx  ${CMAKE_CURRENT_BINARY_DIR}/lexer.cpp)
    ADD_FLEX_BISON_DEPENDENCY(FLATZINCSCANNER FLATZINCPARSER)
    ADD_EXECUTABLE(qfzn-quacode ${FLATZINC_SRCS} ${BISON_FLATZINCPARSER_OUTPUTS} ${FLEX_FLATZINCSCANNER_OUTPUTS})
    FIND_PACKAGE(Qt4 4.4 COMPONENTS QtCore)
    IF (QT_FOUND)
      GET_PROPERTY(DIRS DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} PROPERTY INCLUDE_DIRECTORIES)
      SET(DIRS ${DIRS} ${QT_INCLUDE_DIR} ${QT_QTCORE_INCLUDE_DIR})
      SET_TARGET_PROPERTIES(qfzn-quacode PROPERTIES INCLUDE_DIRECTORIES "${DIRS}")
      TARGET_LINK_LIBRARIES(qfzn-quacode ${QCSP_LIB} ${GECODE_LIBRARIES} ${QT_QTCORE_LIBRARY} pthread)
    ELSE()
      TARGET_LINK_LIBRARIES(qfzn-quacode ${QCSP_LIB} ${GECODE_LIBRARIES} pthread)
    ENDIF()

    # set -static, when STATIC_LINKING is TRUE and set LINK_SEARCH_END_STATIC
    # to remove the additional -bdynamic from the linker line.
    IF(STATIC_LINKING)
      SET(CMAKE_EXE_LINKER_FLAGS "-static")
      SET_TARGET_PROPERTIES(qfzn-quacode PROPERTIES LINK_SEARCH_END_STATIC 1)
    ENDIF(STATIC_LINKING)

    IF (BUILD_SHARED_LIBS)
      ADD_DEPENDENCIES(qfzn-quacode quacode )
    ENDIF()
  ELSE()
    MESSAGE(STATUS "Can't find Flex and Bison, QFlatZinc will be not built.")
  ENDIF()


  # Add targets for examples
  FOREACH (example ${QCSP_EXAMPLES_SRCS})
    GET_FILENAME_COMPONENT(exampleBin ${example} NAME_WE)
    ADD_EXECUTABLE(${exampleBin} ${example})
    TARGET_LINK_LIBRARIES(${exampleBin} ${QCSP_LIB} ${GECODE_LIBRARIES} pthread)

    # set -static, when STATIC_LINKING is TRUE and set LINK_SEARCH_END_STATIC
    # to remove the additional -bdynamic from the linker line.
    IF(STATIC_LINKING)
        SET(CMAKE_EXE_LINKER_FLAGS "-static")
        SET_TARGET_PROPERTIES(${exampleBin} PROPERTIES LINK_SEARCH_END_STATIC 1)
    ENDIF(STATIC_LINKING)

    IF (BUILD_SHARED_LIBS)
      ADD_DEPENDENCIES(${exampleBin} quacode )
    ENDIF()
  ENDFOREACH ()

  # Add targets for examples which need threads and SIBUS

ENDIF(NOT GECODE_FOUND)
