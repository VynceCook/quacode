#!/bin/bash

# ---------------------------------------
# HELP
# ---------------------------------------
# Notice that when a problem reach timeout, the other following run are
# not launch save time

# When adding a new problem, all init_ALGO functions must be completed !!!
# Furthermore, the definition of the problem must be add.
# The meaning of each element follows:
# gProblems[X,NAME]="ProblemName" <-- Name of the problem
# gProblems[X,ALGOS]="algoA algoB algoC ..." <-- List of algorithms to run on this problem
# gProblems[X,NBVARIANT]="1" <-- Nb variant for this problem
# gProblems[X,DATATOCOLLECT]="data1 data2 ..." <-- Data to collect for this problem
# gProblems[X,NBRUN]="?" <-- Number of run for each algorithm
# gProblems[X,MAXTIME]="?" <-[OPTIONAL]- Timeout in seconds before force stopping algorithm, if empty, no timeout
# gProblems[X,VARIANT,Y,NAME]="???" <-- Name of the Yth variant
# gProblems[X,VARIANT,Y,ALGOS]="algoA algoC" <-[OPTIONAL]- Filter to select which algorithms will be run on this variant, if empty, all algorithms will be run
# gProblems[X,VARIANT,Y,ARG,RANGEVALUE]="arg1 arg2 ..." <-[OPTIONAL]- List of argument for this variant (each algo will run for all list elements)
# gProblems[X,VARIANT,Y,algoA,NBRUN]="?" <-[OPTIONAL]- If present, it overrides the number of run for this algorithm in this variant
# gProblems[X,VARIANT,Y,algoC,NBRUN]="?" <-[OPTIONAL]- If present, it overrides the number of run for this algorithm in this variant
# gProblems[X,VARIANT,Y,algoC,MAXTIME]="?"  <-[OPTIONAL]- If present, it overrides the timeout for this algorithm in this variant

# ---------------------------------------
# SCRIPT PARAMETER
# ---------------------------------------
# declare -r ERROR="/dev/null"
declare -r ERROR="error.log"
declare -r LOG=""
declare -r RES_OUTPUT="results.csv"
# Set the problems to test you can reduce it
declare -r idProblems="0 1 2 3"

# ---------------------------------------
# PROBLEMS STATEMENT
# ---------------------------------------
declare -A gProblems

# BAKER PROBLEM
gProblems[0,NAME]="Baker"
gProblems[0,NBVARIANT]="1"
gProblems[0,ALGOS]="quacode quacode-nodecount queso qecode qecode-debug"
gProblems[0,DATATOCOLLECT]="solutions propagations nodes failures runtime"
gProblems[0,NBRUN]="10"
gProblems[0,MAXTIME]="1800"  # Seconds
gProblems[0,VARIANT,0,NAME]="_"
gProblems[0,VARIANT,0,ARG,RANGEVALUE]="0 1 3"
gProblems[0,VARIANT,0,quacode-nodecount,NBRUN]="1"
gProblems[0,VARIANT,0,quacode-nodecount,MAXTIME]="3600"
gProblems[0,VARIANT,0,qecode-debug,NBRUN]="1"
gProblems[0,VARIANT,0,qecode-debug,MAXTIME]="7200"

# NIMFIBO PROBLEM
gProblems[1,NAME]="NimFibo"
gProblems[1,ALGOS]="quacode quacode-nodecount queso qecode qecode-debug"
gProblems[1,NBVARIANT]="2"
gProblems[1,DATATOCOLLECT]="solutions propagations nodes failures runtime"
gProblems[1,NBRUN]="10"
gProblems[1,MAXTIME]="1800"  # Seconds
gProblems[1,VARIANT,0,NAME]="SANS_CUT"
gProblems[1,VARIANT,0,ALGOS]="quacode quacode-nodecount"
gProblems[1,VARIANT,0,ARG,RANGEVALUE]="4 5 6 7 8 9 10 11 12 13 14 15 16"
gProblems[1,VARIANT,0,quacode-nodecount,NBRUN]="1"
gProblems[1,VARIANT,0,quacode-nodecount,MAXTIME]="3600"
gProblems[1,VARIANT,0,qecode-debug,NBRUN]="1"
gProblems[1,VARIANT,0,qecode-debug,MAXTIME]="7200"
gProblems[1,VARIANT,1,NAME]="AVEC_CUT"
gProblems[1,VARIANT,1,ARG,RANGEVALUE]="4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22"
gProblems[1,VARIANT,1,quacode-nodecount,NBRUN]="1"
gProblems[1,VARIANT,1,quacode-nodecount,MAXTIME]="3600"
gProblems[1,VARIANT,1,qecode-debug,NBRUN]="1"
gProblems[1,VARIANT,1,qecode-debug,MAXTIME]="7200"

# MATRIXGAME PROBLEM
gProblems[2,NAME]="MatrixGame"
gProblems[2,ALGOS]="genMatrix quacode quacode-nodecount queso qecode qecode-debug"
gProblems[2,NBVARIANT]="1"
gProblems[2,DATATOCOLLECT]="solutions propagations nodes failures runtime"
gProblems[2,NBRUN]="10"
gProblems[2,MAXTIME]="1800"  # Seconds
gProblems[2,VARIANT,0,NAME]="_"
gProblems[2,VARIANT,0,ALGOS]="genMatrix quacode-nodecount quacode queso qecode-debug qecode"
gProblems[2,VARIANT,0,ARG,RANGEVALUE]="4 5 6 7 8 9 10 11"
gProblems[2,VARIANT,0,genMatrix,NBRUN]="1"
gProblems[2,VARIANT,0,queso,MAXTIME]="2400"
gProblems[2,VARIANT,0,qecode,MAXTIME]="2400"
gProblems[2,VARIANT,0,quacode-nodecount,NBRUN]="1"
gProblems[2,VARIANT,0,quacode-nodecount,MAXTIME]="3600"
gProblems[2,VARIANT,0,qecode-debug,NBRUN]="1"
gProblems[2,VARIANT,0,qecode-debug,MAXTIME]="7200"

# CONNECTFOUR PROBLEM
gProblems[3,NAME]="ConnectFour"
gProblems[3,ALGOS]="quacode quacode-nodecount queso"
gProblems[3,NBVARIANT]="3"
gProblems[3,DATATOCOLLECT]="solutions propagations nodes failures runtime"
gProblems[3,NBRUN]="10"
gProblems[3,MAXTIME]="1800"  # Seconds
gProblems[3,VARIANT,0,NAME]="SANS_CUT"
gProblems[3,VARIANT,0,ARG,RANGEVALUE]="4x4 4x5 5x4"
gProblems[3,VARIANT,0,quacode-nodecount,NBRUN]="1"
gProblems[3,VARIANT,0,quacode-nodecount,MAXTIME]="3600"
gProblems[3,VARIANT,1,NAME]="AVEC_CUT"
gProblems[3,VARIANT,1,ARG,RANGEVALUE]="4x4 4x5 5x4"
gProblems[3,VARIANT,1,quacode-nodecount,NBRUN]="1"
gProblems[3,VARIANT,1,quacode-nodecount,MAXTIME]="3600"
gProblems[3,VARIANT,2,NAME]="AVEC_CUT_HEUR"
gProblems[3,VARIANT,2,ARG,RANGEVALUE]="4x4 4x5 5x4"
gProblems[3,VARIANT,2,quacode-nodecount,NBRUN]="1"
gProblems[3,VARIANT,2,quacode-nodecount,MAXTIME]="3600"

# ---------------------------------------
# ENVIRONMENT VARIABLES
# ---------------------------------------
declare -r QUACODE_LD_LIBRARY_PATH="$HOME/builds/gecode-release"
declare -r QUACODE_PATH="$HOME/builds/quacode-release"
declare -r QUACODE_NODECOUNT_PATH="$HOME/builds/quacode-nodecount"
declare -r QUACODE_NODECOUNT_LD_LIBRARY_PATH="$HOME/builds/gecode-release"
declare -r QUESO_PATH="$HOME/Dropbox/Queso/queso-current"
declare -r QUESO_LD_LIBRARY_PATH="$QUESO_PATH"
declare -r QECODE_PATH="$HOME/builds/gecode-contribs-release/contribs/qecode/examples"
declare -r QECODE_LD_LIBRARY_PATH="$HOME/builds/gecode-contribs-release;$HOME/builds/gecode-contribs-release/contribs/qecode"
declare -r QECODE_DEBUG_PATH="$HOME/builds/gecode-contribs/contribs/qecode/examples"
declare -r QECODE_DEBUG_LD_LIBRARY_PATH="$HOME/builds/gecode-contribs;$HOME/builds/gecode-contribs/contribs/qecode"

# ---------------------------------------
# SECONDARY FUNCTIONS
# ---------------------------------------
function printToResult
{
  declare -r str=$1
  if [ -z "$RES_OUTPUT" ]; then
    echo -n $str                  # To standard output
  else
    echo -n $str >> $RES_OUTPUT   # To a given file
  fi
}

function printlnToResult
{
  declare -r str=$1
  if [ -z "$RES_OUTPUT" ]; then
    echo $str                  # To standard output
  else
    echo $str >> $RES_OUTPUT   # To a given file
  fi
}

function printToLog
{
  declare -r str=$1
  if [ -z "$LOG" ]; then
    echo -n $str           # To standard output
  else
    echo -n $str >> $LOG   # To a given file
  fi
}

function printlnToLog
{
  declare -r str=$1
  if [ -z "$LOG" ]; then
    echo $str           # To standard output
  else
    echo $str >> $LOG   # To a given file
  fi
}

# ---------------------------------------
# GENERATE MATRIX FOR MATRIXGAME PROBLEM
# ---------------------------------------
function init_genMatrix
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}

  declare -i depth=${params[VARIANT,ARG,RANGEVALUE]}
  declare -i boardSize=$(( 2 ** $depth ))
  matrixFile="matrixgame-$v-$depth.txt"
  if [ ! -f "$matrixFile" ]; then
    for (( i=0 ; i<$boardSize; i++ ))
    do
      for (( j=0 ; j<$boardSize; j++ ))
      do
        declare -i NB=$(( RANDOM % 100 ))
        if [ $j -ne 0 ]; then
          echo -n " " >> $matrixFile
        fi
        if [ $NB -lt 50 ]; then
          echo -n "0" >> $matrixFile
        else
          echo -n "1" >> $matrixFile
        fi
      done
      echo >> $matrixFile
    done
  fi
}

function results_genMatrix
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
}

# ---------------------------------------
# QUACODE
# ---------------------------------------
function init_quacode
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A result
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare value=${params[VARIANT,ARG,RANGEVALUE]}

  result[LD_LIBRARY_PATH]=$QUACODE_LD_LIBRARY_PATH
  declare EXE=""

  # Set timeout
  declare timeOut=""
  if [ -n "${gProblems[$i,VARIANT,$v,quacode,MAXTIME]}" ]; then
    timeoutMilli=$(( ${gProblems[$i,VARIANT,$v,quacode,MAXTIME]} * 1000 ))
    timeOut="-time $timeoutMilli"
  elif [ -n "${gProblems[$i,MAXTIME]}" ]; then
    timeoutMilli=$(( ${gProblems[$i,MAXTIME]} * 1000 ))
    timeOut="-time $timeoutMilli"
  fi

  case "${gProblems[$i,NAME]}" in
    Baker)
      EXE="$QUACODE_PATH/baker $timeOut $value"
      ;;
    NimFibo)
      declare variantArg=""
      case "${gProblems[$i,VARIANT,$v,NAME]}" in
        "AVEC_CUT")
          variantArg="-cut true"
          ;;
        "SANS_CUT")
          variantArg="-cut false"
          ;;
      esac
      EXE="$QUACODE_PATH/nim-fibo $timeOut $variantArg $value"
      ;;
    MatrixGame)
      EXE="$QUACODE_PATH/matrix-game $timeOut"
      EXE="$EXE -file matrixgame-$v-$value.txt"
      ;;
    ConnectFour)
      declare variantArg=""
      case "${gProblems[$i,VARIANT,$v,NAME]}" in
        "SANS_CUT")
          variantArg="-QCSPmodel AllState -heuristic false"
          ;;
        "AVEC_CUT")
          variantArg="-QCSPmodel AllState+ -heuristic false"
          ;;
        "AVEC_CUT_HEUR")
          variantArg="-QCSPmodel AllState+ -heuristic true"
          ;;
      esac
      EXE="$QUACODE_PATH/connect-four $timeOut $variantArg"
      declare -i nbRow=${value%x*}
      declare -i nbCol=${value#*x}
      EXE="$EXE -row $nbRow -col $nbCol"
      ;;
    *)
      echo "Error, unknown problem: ${gProblems[$i,NAME]}" 1>&2
      result[EXE]="ERROR"
      ;;
  esac

  result[EXE]=$EXE
  declare -p result # Return result
}

function results_quacode
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare -i run=${params[RUN]}
  declare -r ALGO_OUT=${params[ALGO_OUT]}

  # Print collected data
  printToResult "${gProblems[$i,NAME]}"
  printToResult ";${gProblems[$i,VARIANT,$v,NAME]}"
  printToResult ";${params[VARIANT,ARG,RANGEVALUE]}"
  printToResult ";${params[ALGO]}"
  printToResult ";$run"

  # The echo will be catch as a return result
  declare TIMEOUT=""
  REGEX="^.*time[[:space:]]limit[[:space:]]reached"
  if [[ "$ALGO_OUT" =~  $REGEX ]]; then
    echo "TIMEOUT"
    TIMEOUT="OK"
  fi

  for data in ${gProblems[$i,DATATOCOLLECT]};
  do
    declare RES=""
    if [ -z "$TIMEOUT" ]; then
      case $data in
        runtime)
          REGEX="^.*runtime:[[:space:]]*[0-9:.]+[[:space:]]*\(([0-9]+)\."
          ;;
        *)
          REGEX="^.*$data:[[:space:]]*([0-9]+)(\.[0-9]+)?"
          ;;
      esac
      # [[ "$ALGO_OUT" =~  $REGEX ]] && echo OK 1>&2
      [[ "$ALGO_OUT" =~  $REGEX ]] && RES=${BASH_REMATCH[1]}
    fi

    printToResult ";$RES"
  done
  printlnToResult
}

# ---------------------------------------
# QUACODE-NODECOUNT
# ---------------------------------------
function init_quacode-nodecount
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A result
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare value=${params[VARIANT,ARG,RANGEVALUE]}

  result[LD_LIBRARY_PATH]=$QUACODE_NODECOUNT_LD_LIBRARY_PATH
  declare EXE=""

  # Set timeout
  declare timeOut=""
  if [ -n "${gProblems[$i,VARIANT,$v,quacode-nodecount,MAXTIME]}" ]; then
    timeoutMilli=$(( ${gProblems[$i,VARIANT,$v,quacode-nodecount,MAXTIME]} * 1000 ))
    timeOut="-time $timeoutMilli"
  elif [ -n "${gProblems[$i,MAXTIME]}" ]; then
    timeoutMilli=$(( ${gProblems[$i,MAXTIME]} * 1000 ))
    timeOut="-time $timeoutMilli"
  fi

  case "${gProblems[$i,NAME]}" in
    Baker)
      EXE="$QUACODE_NODECOUNT_PATH/baker $timeOut $value"
      ;;
    NimFibo)
      declare variantArg=""
      case "${gProblems[$i,VARIANT,$v,NAME]}" in
        "SANS_CUT")
          variantArg="-cut false"
          ;;
        "AVEC_CUT")
          variantArg="-cut true"
          ;;
      esac
      EXE="$QUACODE_NODECOUNT_PATH/nim-fibo $timeOut $variantArg $value"
      ;;
    MatrixGame)
      EXE="$QUACODE_NODECOUNT_PATH/matrix-game $timeOut"
      EXE="$EXE -file matrixgame-$v-$value.txt"
      ;;
    ConnectFour)
      declare variantArg=""
      case "${gProblems[$i,VARIANT,$v,NAME]}" in
        "SANS_CUT")
          variantArg="-QCSPmodel AllState -heuristic false"
          ;;
        "AVEC_CUT")
          variantArg="-QCSPmodel AllState+ -heuristic false"
          ;;
        "AVEC_CUT_HEUR")
          variantArg="-QCSPmodel AllState+ -heuristic true"
          ;;
      esac
      EXE="$QUACODE_NODECOUNT_PATH/connect-four $timeOut $variantArg"
      declare -i nbRow=${value%x*}
      declare -i nbCol=${value#*x}
      EXE="$EXE -row $nbRow -col $nbCol"
      ;;
    *)
      echo "Error, unknown problem: ${gProblems[$i,NAME]}" 1>&2
      result[EXE]="ERROR"
      ;;
  esac

  result[EXE]=$EXE
  declare -p result # Return result
}

function results_quacode-nodecount
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare -i run=${params[RUN]}
  declare -r ALGO_OUT=${params[ALGO_OUT]}

  # Print collected data
  printToResult "${gProblems[$i,NAME]}"
  printToResult ";${gProblems[$i,VARIANT,$v,NAME]}"
  printToResult ";${params[VARIANT,ARG,RANGEVALUE]}"
  printToResult ";${params[ALGO]}"
  printToResult ";$run"

  # The echo will be catch as a return result
  declare TIMEOUT=""
  REGEX="^.*time[[:space:]]limit[[:space:]]reached"
  if [[ "$ALGO_OUT" =~  $REGEX ]]; then
    echo "TIMEOUT"
    TIMEOUT="OK"
  fi

  for data in ${gProblems[$i,DATATOCOLLECT]};
  do
    declare RES=""
    if [ -z "$TIMEOUT" ]; then
      case $data in
        runtime)
          REGEX="^.*runtime:[[:space:]]*[0-9:.]+[[:space:]]*\(([0-9]+)\."
          ;;
        nodes)
          REGEX="^.*opened nodes:[[:space:]]*([0-9]+)"
          ;;
        *)
          REGEX="^.*$data:[[:space:]]*([0-9]+)(\.[0-9]+)?"
          ;;
      esac
      # [[ "$ALGO_OUT" =~  $REGEX ]] && echo OK 1>&2
      [[ "$ALGO_OUT" =~  $REGEX ]] && RES=${BASH_REMATCH[1]}
    fi

    printToResult ";$RES"
  done
  printlnToResult
}

# ---------------------------------------
# QUESO
# ---------------------------------------
function init_queso
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A result
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare value=${params[VARIANT,ARG,RANGEVALUE]}

  result[LD_LIBRARY_PATH]=$QUESO_LD_LIBRARY_PATH
  declare EXE=""

  case "${gProblems[$i,NAME]}" in
    Baker)
      EXE="java -Xmx500M -server -cp $QUESO_PATH/lib/trove.jar:$QUESO_PATH/classes/ queso.experiments.bakertest $value"
      ;;
    NimFibo)
      EXE="java -Xmx500M -server -cp $QUESO_PATH/lib/trove.jar:$QUESO_PATH/classes/ queso.experiments.nimfibo $value"
      ;;
    MatrixGame)
      EXE="java -Xmx500M -server -cp $QUESO_PATH/lib/trove.jar:$QUESO_PATH/classes/ queso.experiments.matrixgame"
      EXE="$EXE -file matrixgame-$v-$value.txt"
      ;;
    ConnectFour)
      declare variantArg=""
      case "${gProblems[$i,VARIANT,$v,NAME]}" in
        "SANS_CUT")
          variantArg=""
          ;;
        "AVEC_CUT")
          variantArg="-pv"
          ;;
        "AVEC_CUT_HEUR")
          variantArg="-pv -heur"
          ;;
      esac
      EXE="java -Xmx500M -server -cp $QUESO_PATH/lib/trove.jar:$QUESO_PATH/classes/ queso.experiments.connectfourfullor2"
      declare -i nbRow=${value%x*}
      declare -i nbCol=${value#*x}
      EXE="$EXE -row $nbRow -col $nbCol $variantArg"
      ;;
    *)
      echo "Error, unknown problem: ${gProblems[$i,NAME]}" 1>&2
      result[EXE]="ERROR"
      ;;
  esac

  # Set timeout
  if [ -n "${gProblems[$i,VARIANT,$v,queso,MAXTIME]}" ]; then
    EXE="timeout ${gProblems[$i,VARIANT,$v,queso,MAXTIME]} $EXE"
  elif [ -n "${gProblems[$i,MAXTIME]}" ]; then
    EXE="timeout ${gProblems[$i,MAXTIME]} $EXE"
  fi

  result[EXE]=$EXE
  declare -p result # Return result
}

function results_queso
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare TIMEOUT=""
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare -i run=${params[RUN]}
  declare -r ALGO_OUT=${params[ALGO_OUT]}

  # Print collected data
  printToResult "${gProblems[$i,NAME]}"
  printToResult ";${gProblems[$i,VARIANT,$v,NAME]}"
  printToResult ";${params[VARIANT,ARG,RANGEVALUE]}"
  printToResult ";${params[ALGO]}"
  printToResult ";$run"

  for data in ${gProblems[$i,DATATOCOLLECT]};
  do
    declare RES=""
    declare REGEX=""
    case $data in
      solutions)
        REGEX="^.*Setup[[:space:]]time:[[:space:]]*[0-9]+[[:space:]]*([a-z]+)"
        ;;
      nodes)
        REGEX="^.*Nodes:[[:space:]]*([0-9]+)"
        ;;
      runtime)
        REGEX="^.*Setup[[:space:]]time:[[:space:]]*([0-9]+).*Search[[:space:]]time:[[:space:]]*([0-9]+)"
        ;;
      propagations)
        REGEX="^.*Propagations:[[:space:]]*([0-9]+)"
        ;;
    esac
    # [[ "$ALGO_OUT" =~  $REGEX ]] && echo OK
    [[ "$ALGO_OUT" =~  $REGEX ]] && RES=${BASH_REMATCH[1]}

    if [ "$data" = "solutions" ]; then
      if [ -n "$RES" ]; then
        if [ "$RES" = "true" ]; then
          RES=1
        else
          RES=0
        fi
      fi
    elif [ "$data" = "runtime" ]; then
      if [ -z "$RES" ]; then
        TIMEOUT="OK"
      else
        RES=$(( $RES + ${BASH_REMATCH[2]]} ))
      fi
    fi
    printToResult ";$RES"
  done
  printlnToResult

  # The echo will be catch as a return result
  if [ -n "$TIMEOUT" ]; then
    echo "TIMEOUT"
  fi
}

# ---------------------------------------
# QECODE
# ---------------------------------------
function init_qecode
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A result
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare value=${params[VARIANT,ARG,RANGEVALUE]}

  result[LD_LIBRARY_PATH]=$QECODE_LD_LIBRARY_PATH
  declare EXE=""

  case "${gProblems[$i,NAME]}" in
    Baker)
      EXE="$QECODE_PATH/Baker $value"
      ;;
    NimFibo)
      EXE="$QECODE_PATH/NimFibo $value"
      ;;
    MatrixGame)
      EXE="$QECODE_PATH/MatrixGame"
      EXE="$EXE matrixgame-$v-$value.txt"
      ;;
    *)
      echo "Error, unknown problem: ${gProblems[$i,NAME]}" 1>&2
      result[EXE]="ERROR"
      ;;
  esac

  # Set timeout
  if [ -n "${gProblems[$i,VARIANT,$v,qecode,MAXTIME]}" ]; then
    EXE="timeout ${gProblems[$i,VARIANT,$v,qecode,MAXTIME]} $EXE"
  elif [ -n "${gProblems[$i,MAXTIME]}" ]; then
    EXE="timeout ${gProblems[$i,MAXTIME]} $EXE"
  fi

  result[EXE]=$EXE
  declare -p result # Return result
}

function results_qecode
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare TIMEOUT=""
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare -i run=${params[RUN]}
  declare -r ALGO_OUT=${params[ALGO_OUT]}

  # Print collected data
  printToResult "${gProblems[$i,NAME]}"
  printToResult ";${gProblems[$i,VARIANT,$v,NAME]}"
  printToResult ";${params[VARIANT,ARG,RANGEVALUE]}"
  printToResult ";${params[ALGO]}"
  printToResult ";$run"

  for data in ${gProblems[$i,DATATOCOLLECT]};
  do
    declare RES=""
    declare REGEX=""
    case $data in
      solutions)
        REGEX="^.*outcome:[[:space:]]*([A-Z]+)"
        ;;
      nodes)
        REGEX="^.*nodes[[:space:]]visited:[[:space:]]*([0-9]+)"
        ;;
      runtime)
        REGEX="^.*Time[[:space:]]taken:[[:space:]]*([0-9]+)"
        ;;
    esac
    # [[ "$ALGO_OUT" =~  $REGEX ]] && echo OK
    [[ "$ALGO_OUT" =~  $REGEX ]] && RES=${BASH_REMATCH[1]}

    if [ "$data" = "solutions" ]; then
      if [ -n "$RES" ]; then
        if [ "$RES" = "TRUE" ]; then
          RES=1
        else
          RES=0
        fi
      fi
    elif [ "$data" = "runtime" ]; then
      if [ -z "$RES" ]; then
        TIMEOUT="OK"
      fi
      if [ -n "$RES" -a "$RES" != "0" ]; then
        RES=$(( $RES / 1000 ))
      fi
    fi
    printToResult ";$RES"
  done
  printlnToResult

  # The echo will be catch as a return result
  if [ -n "$TIMEOUT" ]; then
    echo "TIMEOUT"
  fi
}

# ---------------------------------------
# QECODE DEBUG
# ---------------------------------------
function init_qecode-debug
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A result
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare value=${params[VARIANT,ARG,RANGEVALUE]}

  result[LD_LIBRARY_PATH]=$QECODE_DEBUG_LD_LIBRARY_PATH
  declare EXE=""

  case "${gProblems[$i,NAME]}" in
    Baker)
      EXE="$QECODE_DEBUG_PATH/Baker $value"
      ;;
    NimFibo)
      EXE="$QECODE_DEBUG_PATH/NimFibo $value"
      ;;
    MatrixGame)
      EXE="$QECODE_DEBUG_PATH/MatrixGame"
      EXE="$EXE matrixgame-$v-$value.txt"
      ;;
    *)
      echo "Error, unknown problem: ${gProblems[$i,NAME]}" 1>&2
      result[EXE]="ERROR"
      ;;
  esac

  # Set timeout
  if [ -n "${gProblems[$i,VARIANT,$v,qecode-debug,MAXTIME]}" ]; then
    EXE="timeout ${gProblems[$i,VARIANT,$v,qecode-debug,MAXTIME]} $EXE"
  elif [ -n "${gProblems[$i,MAXTIME]}" ]; then
    EXE="timeout ${gProblems[$i,MAXTIME]} $EXE"
  fi

  result[EXE]=$EXE
  declare -p result # Return result
}

function results_qecode-debug
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare TIMEOUT=""
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare -i run=${params[RUN]}
  declare -r ALGO_OUT=${params[ALGO_OUT]}

  # Print collected data
  printToResult "${gProblems[$i,NAME]}"
  printToResult ";${gProblems[$i,VARIANT,$v,NAME]}"
  printToResult ";${params[VARIANT,ARG,RANGEVALUE]}"
  printToResult ";${params[ALGO]}"
  printToResult ";$run"

  for data in ${gProblems[$i,DATATOCOLLECT]};
  do
    declare RES=""
    declare REGEX=""
    case $data in
      solutions)
        REGEX="^.*outcome:[[:space:]]*([A-Z]+)"
        ;;
      nodes)
        REGEX="^.*nodes[[:space:]]visited:[[:space:]]*([0-9]+)"
        ;;
      runtime)
        REGEX="^.*Time[[:space:]]taken:[[:space:]]*([0-9]+)"
        ;;
      propagations)
        REGEX="^.*propagations:[[:space:]]*([0-9]+)"
        ;;
    esac
    # [[ "$ALGO_OUT" =~  $REGEX ]] && echo OK
    [[ "$ALGO_OUT" =~  $REGEX ]] && RES=${BASH_REMATCH[1]}

    if [ "$data" = "solutions" ]; then
      if [ -n "$RES" ]; then
        if [ "$RES" = "TRUE" ]; then
          RES=1
        else
          RES=0
        fi
      fi
    elif [ "$data" = "runtime" ]; then
      if [ -z "$RES" ]; then
        TIMEOUT="OK"
      fi
      if [ -n "$RES" -a "$RES" != "0" ]; then
        RES=$(( $RES / 1000 ))
      fi
    fi
    printToResult ";$RES"
  done
  printlnToResult

  # The echo will be catch as a return result
  if [ -n "$TIMEOUT" ]; then
    echo "TIMEOUT"
  fi
}

# ---------------------------------------
# RUN PROBLEMS
# ---------------------------------------
# Run a specific algorithm
function runAlgo
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -i i=${params[NUMPB]}
  declare -i v=${params[VARIANT,NUM]}
  declare algo=${params[ALGO]}

  # Array of collected data
  declare -A collectedData

  declare -i nbRuns=${gProblems[$i,NBRUN]}
  # We test if for this algorithm there is another NBRUN to fit
  if [ -n "${gProblems[$i,VARIANT,$v,$algo,NBRUN]}" ]; then
    nbRuns=${gProblems[$i,VARIANT,$v,$algo,NBRUN]}
  fi

  declare -i startRun=1
  if [ -n "${params[START_RUN]}" ]; then
    startRun=${params[START_RUN]}
  fi
  for (( run=startRun; run<=$nbRuns; run++ ));
  do
    params[RUN]=$run
    # Run algorithm
    declare date=$(date +%Y%m%d-%H%M.%S)
    # Call specific method of algorithm: init_??
    declare tmpRetInit=$(init_$algo "$(declare -p params)") # pass assocociative array in string form to function
    eval "declare -A retInit"=${tmpRetInit#*=}
    if [ -n "${retInit[EXE]}" ]; then
      if [ "${retInit[EXE]}" = "ERROR" ]; then
        # A fatal error occured in init_??
        exit 1
      fi
      echo Run $run at $date: ${retInit[EXE]}
      # Create temporary file name
      tmpFile="/tmp/$(basename $0).$$.tmp"
      # Create new descriptor to redirect stderr from algo to this one (here 3) and redirect
      # all writing to tmpFile
      exec 3<> $tmpFile
      export LD_LIBRARY_PATH=${retInit[LD_LIBRARY_PATH]}
      ALGO_OUT=$(${retInit[EXE]} 2>&3 )
      # Close descriptor 3
      exec 3>&-
      declare -i errorSize=$(stat -c %s $tmpFile)
      # If we get error, we print it to a file or to stderr is no file is given
      if [ "$errorSize" != 0 ]; then
        if [ -n "$ERROR" ]; then
          echo Run $run at $date: ${retInit[EXE]} >>$ERROR
          cat $tmpFile >>$ERROR
        else
          cat $tmpFile 1>&2
        fi
      fi
      rm -f $tmpFile

      # Print results
      params[ALGO_OUT]=$ALGO_OUT
      # Call specific method of algorithm: results_??
      declare tmpRetResult=$(results_${params[ALGO]} "$(declare -p params)") # pass assocociative array in string form to function
      if [ "$tmpRetResult" = "TIMEOUT" ]; then
        break
      fi
    fi
  done
}

function runAll
{
  # Main loop for running all algoritms
  # eval string into a new assocociative array
  eval "declare -A initParams"=${1#*=}

  declare -A params
  declare -i bFlag=0 # Flag used to known if we are in the first turn of the loop (usefull for continuing an interrupted launch)
  for i in ${initParams[LIST_PROBLEMS]};
  do
    params[NUMPB]=$i
    if [ -z "${initParams[START_VARIANT]}" -o $bFlag -eq 1 ]; then
      # Construct the end of the header line in file results
      declare outputHeaderLine="#problem;variant_name;variant_range;solver;run"
      for data in ${gProblems[$i,DATATOCOLLECT]};
      do
        if [ "$data" = "runtime" ]; then
          outputHeaderLine="$outputHeaderLine;$data(ms)"
        else
          outputHeaderLine="$outputHeaderLine;$data"
        fi
      done
      printlnToResult $outputHeaderLine
    fi

    # Set startVariant from problem description or last interrupted experiment
    declare startVariant=0
    if [ -n "${initParams[START_VARIANT]}" -a $bFlag -eq 0 ]; then
      startVariant=${initParams[START_VARIANT]}
    fi
    for (( v=$startVariant; v<${gProblems[$i,NBVARIANT]}; v++ ))
    do
      params[VARIANT,NUM]=$v
      params[VARIANT,ARG,VALUE]=${gProblems[$i,VARIANT,$v,ARG,VALUE]}
      printlnToLog "Problem: ${gProblems[$i,NAME]} / Variant: ${gProblems[$i,VARIANT,${params[VARIANT,NUM]},NAME]}"

      # Set algoList from problem description or last interrupted experiment
      declare algoList=${gProblems[$i,ALGOS]}
      if [ -n "${initParams[LIST_ALGOS]}" -a $bFlag -eq 0 ]; then
        algoList=${initParams[LIST_ALGOS]}
      fi
      for algo in $algoList;
      do
        # Set START_RUN from problem description or last interrupted experiment
        if [ -n "${initParams[START_RUN]}" -a $bFlag -eq 0 ]; then
          params[START_RUN]=${initParams[START_RUN]}
        fi
        # If $algo is in the list of algorithms for this variant, or if the list of algorithms for this variant is empty
        if [[ "${gProblems[$i,VARIANT,$v,ALGOS]}" == "" || ${gProblems[$i,VARIANT,$v,ALGOS]} =~ .*$algo.* ]]; then
          params[ALGO]=$algo
          printlnToLog ">> $algo"

          if [ -n "${gProblems[$i,VARIANT,$v,ARG,RANGEVALUE]}" ]; then
            # Set variantArgList from problem description or last interrupted experiment
            declare variantArgList=${gProblems[$i,VARIANT,$v,ARG,RANGEVALUE]}
            if [ -n "${initParams[LIST_VARIANT_ARG]}" -a $bFlag -eq 0 ]; then
              variantArgList=${initParams[LIST_VARIANT_ARG]}
            fi

            # We launch the problem for a range of values
            for val in $variantArgList
            do
              params[VARIANT,ARG,RANGEVALUE]=$val
              runAlgo "$(declare -p params)" # pass assocociative array in string form to function
            done
          else
            # We launch the problem one time
            runAlgo "$(declare -p params)" # pass assocociative array in string form to function
          fi
          # We have to set START_RUN to "" because bFlag is not passed to runAlgo
          params[START_RUN]=""
        fi
        bFlag=1
      done
      printlnToResult
    done
  done
}

# ---------------------------------------
# MAIN
# ---------------------------------------
# Main loop for running all algoritms
declare -A params
# Test if we have to continue a previous interrupted experiment
if [ -e $RES_OUTPUT ]; then
  declare lastLine=$(tail -3 $RES_OUTPUT | grep -ve "^$" | grep -v "#" | tail -1)
  declare REGEX="^([0-9a-zA-Z_-]+);([0-9a-zA-Z_-]+);([0-9a-zA-Z_-]*);([0-9a-zA-Z_-]+);([0-9]+)"
  [[ "$lastLine" =~  $REGEX ]]
  declare lastProblemName=${BASH_REMATCH[1]}
  declare lastVariantName=${BASH_REMATCH[2]}
  declare lastVariantArg=${BASH_REMATCH[3]}
  declare lastAlgo=${BASH_REMATCH[4]}
  declare lastRun=${BASH_REMATCH[5]}

  if [ -z "$lastProblemName" ]; then
    echo "Error, could not continue previous experiment"
    exit 1
  fi

  # Get last problem id
  declare -i lastProblemId=-1
  for (( i=0; i<100; i++ ))
  do
    if [ "${gProblems[$i,NAME]}" = "$lastProblemName" ]; then
      lastProblemId=i
      break
    fi
  done

  # Construct list of problems
  [[ "$idProblems" =~  ^.*($lastProblemId([[:space:]]|$).*) ]] && params[LIST_PROBLEMS]=${BASH_REMATCH[1]}

  # Get last variant id
  declare -i lastVariantId=-1
  for (( i=0; i<${gProblems[$lastProblemId,NBVARIANT]}; i++ ))
  do
    if [ ${gProblems[$lastProblemId,VARIANT,$i,NAME]} = "$lastVariantName" ]; then
      lastVariantId=i
      break
    fi
  done
  params[START_VARIANT]=$lastVariantId

  # Construct list of variant range
  [[ "${gProblems[$lastProblemId,VARIANT,$lastVariantId,ARG,RANGEVALUE]}" =~  ^.*($lastVariantArg([[:space:]]|$).*) ]] && params[LIST_VARIANT_ARG]=${BASH_REMATCH[1]}

  # Construct list of algorithms
  [[ "${gProblems[$lastProblemId,ALGOS]}" =~  ^.*($lastAlgo([[:space:]]|$).*) ]] && params[LIST_ALGOS]=${BASH_REMATCH[1]}

  # Get last run number
  params[START_RUN]=$lastRun

  # Compute next run
  # First, get total number of run to do for this variant of problem
  declare -i NBRUN=-1
  if [ -n "${gProblems[$lastProblemId,VARIANT,$lastVariantId,$lastAlgo,NBRUN]}" ]; then
    NBRUN=${gProblems[$lastProblemId,VARIANT,$lastVariantId,$lastAlgo,NBRUN]}
  else
    NBRUN=${gProblems[$lastProblemId,NBRUN]}
  fi

  # Trim params
  params[LIST_PROBLEMS]=$(echo ${params[LIST_PROBLEMS]} | sed 's/^ *//g' | sed 's/ *$//g')
  params[LIST_VARIANT_ARG]=$(echo ${params[LIST_VARIANT_ARG]} | sed 's/^ *//g' | sed 's/ *$//g')
  params[LIST_ALGOS]=$(echo ${params[LIST_ALGOS]} | sed 's/^ *//g' | sed 's/ *$//g')

  if [ ${params[START_RUN]} -lt $NBRUN ]; then
    # It remains run
    params[START_RUN]=$(( ${params[START_RUN]} + 1 ))
  elif [ "${params[LIST_VARIANT_ARG]}" != "$lastVariantArg" ]; then
    # It remains variant arg value
    params[START_RUN]=""
    params[LIST_VARIANT_ARG]=${params[LIST_VARIANT_ARG]#* }
  elif [ "${params[LIST_ALGOS]}" != "$lastAlgo" ]; then
    # It remains algo
    params[START_RUN]=""
    params[LIST_VARIANT_ARG]=""
    params[LIST_ALGOS]=${params[LIST_ALGOS]#* }
  elif [ ${params[START_VARIANT]} -lt $(( ${gProblems[$lastProblemId,NBVARIANT]} - 1 )) ]; then
    # It remains variant
    params[START_RUN]=""
    params[LIST_VARIANT_ARG]=""
    params[LIST_ALGOS]=""
    params[START_VARIANT]=$(( ${params[START_VARIANT]} + 1 ))
  elif [ "${params[LIST_PROBLEMS]}" != "$lastProblemId" ]; then
    # It remains problem
    params[START_RUN]=""
    params[LIST_VARIANT_ARG]=""
    params[LIST_ALGOS]=""
    params[START_VARIANT]=""
    params[LIST_PROBLEMS]=${params[LIST_PROBLEMS]#* }
  else
    # Nothing to do
    echo All test have been launched
    exit 0
  fi
  runAll "$(declare -p params)" # pass assocociative array in string form to function
else
  rm -f $LOG
  rm -f $RES_OUTPUT
  params[LIST_PROBLEMS]=$idProblems
  runAll "$(declare -p params)" # pass assocociative array in string form to function
fi
