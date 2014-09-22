#!/bin/bash

file=$1
declare resInput=$(cat $file | grep "INSTANCE")

# Computes the number of men and number of women
declare -i nbWomen=0
declare -i nbMen=0
declare line=$(cat $file | grep "INSTANCE" | head -1)
REGEX="INSTANCE.*val\(PH_([0-9]).*"
if [[ $line =~ ^$REGEX$ ]]; then
  nbMen=${BASH_REMATCH[1]}
  nbMen=$(( $nbMen + 1 ))
fi
REGEX="INSTANCE.*val\(PF_([0-9]).*"
if [[ $line =~ ^$REGEX$ ]]; then
  nbWomen=${BASH_REMATCH[1]}
  nbWomen=$(( $nbWomen + 1 ))
fi
echo "Nb Men: $nbMen, Nb Women: $nbWomen"

BACK_IFS=$IFS
IFS=$'\n'
for line in $resInput; do
  declare -A schedule
  declare -A scheduleVerif
  declare -i nbMeet=0;
  declare -i nbMeetVerif=0;
  declare -i i=0;
  declare -i j=0;

  REGEX=".*\(letsPlay,1\).*"
  if [[ $line =~ ^$REGEX$ ]]; then
    for (( i=0 ; i < $nbMen; i++ )); do
      for (( j=0 ; j < $nbWomen; j++ )); do
        REGEX=".*\(RH_$i\^([0-9]+),$j\).*"
        if [[ $line =~ ^$REGEX$ ]]; then
          schedule[$i,$j]=${BASH_REMATCH[1]}
          nbMeet=$(( $nbMeet + 1 ))
        else
          schedule[$i,$j]="_"
        fi
        REGEX=".*\(RF_$j\^([0-9]+),$i\).*"
        if [[ $line =~ ^$REGEX$ ]]; then
          scheduleVerif[$i,$j]=${BASH_REMATCH[1]}
          nbMeetVerif=$(( $nbMeetVerif + 1 ))
        else
          scheduleVerif[$i,$j]="_"
        fi
      done
    done
  fi

  # Check that meeting for men and women are symetric
  if [ $nbMeet -ne $nbMeetVerif ]; then
    echo "VERIF - NbMeet not match"
    exit
  fi
  for (( i=0 ; i < $nbMen; i++ )); do
    for (( j=0 ; j < $nbWomen; j++ )); do
      if [ "${scheduleVerif[$i,$j]}" != "${schedule[$i,$j]}" ]; then
        echo "VERIF - Meet man $i with woman $j not match"
        exit
      fi
    done
  done

  if [ $nbMeet -gt 0 ]; then
    echo $line
    for (( j=0 ; j < $nbWomen; j++ )); do
      for (( i=0 ; i < $nbMen; i++ )); do
        echo -n "${schedule[$i,$j]} "
      done
      echo
    done
  fi
done
IFS=$BACK_IFS
