#!/bin/bash

QECODE="$HOME/builds/gecode-contribs/contribs/qecode/examples/MatrixGame 5"
QECODE_LD="$HOME/builds/gecode-contribs:$HOME/builds/gecode-contribs/contribs/qecode"

QUACODE="$HOME/builds/quacode/matrix-game"
QUACODE_LD="$HOME/builds/gecode-debug:$HOME/builds/quacode"

FILE="$HOME/matrix-bench.txt"

OK=1
I=0
while [ $OK -eq 1 ]; do
  export LD_LIBRARY_PATH=$QECODE_LD
  QECODE_OUT=$($QECODE 2>/dev/null);
  TMP=$(echo "$QECODE_OUT" | grep outcome);
  TMP1=${TMP/FALSE/0}
  TMP2=${TMP1/TRUE/1}
  QECODE_SOL=${TMP2:(-1):1};
  QECODE_MAT=$(echo "$QECODE_OUT" |grep "^[01]")
  
  echo "$QECODE_MAT" > $FILE
  
  export LD_LIBRARY_PATH=$QUACODE_LD
  QUACODE_OUT=$($QUACODE -file $FILE 2>/dev/null);
  TMP=$(echo "$QUACODE_OUT" | grep solutions);
  QUACODE_SOL=${TMP:(-1):1};
  QUACODE_MAT=$(echo "$QUACODE_OUT" |grep "^[01]")
  
  if [ $QECODE_SOL -eq $QUACODE_SOL ]; then
    case "$I" in
      "0")
          echo -en "\r-";
          ;;
      "1")
          echo -en "\r/";
          ;;
      "2")
          echo -en "\r|";
          ;;
      "3")
          echo -en "\r\\";
          I=-1;
          ;;
    esac
    I=$(($I + 1))
    rm "$FILE"
    rm -f "$FILE-qecode"
    rm -f "$FILE-quacode"
  else
    echo
    echo "QECODE: $QECODE_SOL  -- QUACODE : $QUACODE_SOL"
    echo "$QECODE_OUT" > "$FILE-qecode"
    echo "$QUACODE_OUT" > "$FILE-quacode"
    OK=0
  fi
done

