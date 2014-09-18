#!/bin/bash

# ---------------------------------------
# SCRIPT PARAMETER
# ---------------------------------------
declare -r RES_INTPUT_FILE="results.csv"

declare -A params
declare -r listParams="problem variant_name variant_range solver run"
declare -r listResults="solutions propagations nodes failures runtime"

params[problem]=""
params[variant_name]=""
params[solver]=""
params[variant_range]=""
params[run]=""

# ---------------------------------------
# GET RESULT FOR ONE SOLVER
# ---------------------------------------
function resultsForOneRun
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A results
  declare -i i=1;
  declare REGEX=""

  # Build regular expression
  for p in $listParams; do
    if [ -n "${params[$p]}" ]; then
      REGEX="$REGEX${params[$p]};"
    else
      REGEX="$REGEX[^;]*;"
    fi
  done
  for p in $listResults; do
    REGEX="$REGEX([^;]*);"
  done
  REGEX=${REGEX%;} # Remove the last ;

  if [[ ${params[INPUT]} =~ ^$REGEX$ ]]; then
    for p in $listResults; do
      results[$p]=${BASH_REMATCH[$i]}
      i=$(( $i + 1 ))
    done
  else
    echo "$REGEX" 1>&2
    echo "Error!" 1>&2
    exit 1
  fi

  declare -p results # Return results
}

# ---------------------------------------
# GET AVERAGE RESULT FOR ONE VARIANT VALUE
# ---------------------------------------
function resultsForVariantValue
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A results
  declare -i nbRun=0;
  declare REGEX=""

  # Build regular expression
  declare -i i=1;
  for p in $listParams; do
    if [ $i -eq 5 ]; then
      REGEX="$REGEX([^;]*);"
    elif [ -n "${params[$p]}" ]; then
      REGEX="$REGEX${params[$p]};"
    else
      REGEX="$REGEX[^;]*;"
    fi
    i=$(( $i + 1 ))
  done
  REGEX="$REGEX.*"

  for p in $listResults; do
    results[$p]="0"
  done

  # Get result and compute average
  for line in ${params[INPUT]}; do
    # The use of regex is meaningfull to catch the run number
    if [[ $line =~ ^$REGEX$ ]]; then
      params[INPUT]=$line
      params[run]=${BASH_REMATCH[1]}
      declare tmpRet=$(resultsForOneRun "$(declare -p params)") # send assocociative array in string form to function
      eval "declare -A tmpResults"=${tmpRet#*=}
      for p in $listResults; do
        if [ -z "${results[$p]}" ]; then
          results[$p]=""
        elif [ -z "${tmpResults[$p]}" ]; then
          results[$p]=""
        else
          results[$p]=$(( ${results[$p]} + ${tmpResults[$p]} ))
        fi
      done
      nbRun=$(( $nbRun + 1 ))
    fi
  done

  for p in $listResults; do
    if [ -n "${results[$p]}" ]; then
      results[$p]=$(( ${results[$p]} / $nbRun ))
    fi
  done
  declare -p results # Return results
}

# ---------------------------------------
# GET RESULT FOR ONE SOLVER ON A GIVEN
# VARIANT. IT RETURNS RESULTS FOR EACH
# VARIANT VALUE OF THE VARIANT RANGE
# ---------------------------------------
function resultsForSolver
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A results
  declare REGEX=""

  # Build regular expression
  declare -i i=1;
  for p in $listParams; do
    if [ $i -eq 3 ]; then
      REGEX="$REGEX([^;]*);"
    elif [ -n "${params[$p]}" ]; then
      REGEX="$REGEX${params[$p]};"
    else
      REGEX="$REGEX[^;]*;"
    fi
    i=$(( $i + 1 ))
  done
  REGEX="$REGEX.*"

  # Extract corresponding lines
  declare lines=""
  for line in ${params[INPUT]}; do
    if [[ $line =~ ^$REGEX$ ]]; then
      lines="$lines $line"
    fi
  done

  # Build list of variant values (no duplicate in the list)
  declare -a variant_range=$(
    for line in $lines; do
      [[ $line =~ ^$REGEX$ ]] && echo ${BASH_REMATCH[1]}
    done | uniq
  )

  # Iterate over list of variant values
  results[variant_range,list]=""
  for vr in $variant_range; do
    params[INPUT]=$lines
    params[variant_range]=$vr
    declare tmpRet=$(resultsForVariantValue "$(declare -p params)") # send assocociative array in string form to function
    results[variant_range,list]="${results[variant_range,list]} $vr"
    results[variant_range,$vr]=$tmpRet
  done
  declare -p results # Return results
}

# ---------------------------------------
# GET RESULT FOR ONE VARIANT
#     (ALL SOLVERS)
# ---------------------------------------
function resultsForVariant
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A results
  declare REGEX=""

  # Build regular expression
  declare -i i=1;
  for p in $listParams; do
    if [ $i -eq 4 ]; then
      REGEX="$REGEX([^;]*);"
    elif [ -n "${params[$p]}" ]; then
      REGEX="$REGEX${params[$p]};"
    else
      REGEX="$REGEX[^;]*;"
    fi
    i=$(( $i + 1 ))
  done
  REGEX="$REGEX.*"

  # Extract corresponding lines
  declare lines=""
  for line in ${params[INPUT]}; do
    if [[ $line =~ ^$REGEX$ ]]; then
      lines="$lines $line"
    fi
  done

  # Build list of solvers (no duplicate in the list)
  declare -a solvers=$(
    for line in $lines; do
      [[ $line =~ ^$REGEX$ ]] && echo ${BASH_REMATCH[1]}
    done | uniq
  )

  # Iterate over list of solvers
  results[solver,list]=""
  for s in $solvers; do
    params[INPUT]=$lines
    params[solver]=$s
    declare tmpRet=$(resultsForSolver "$(declare -p params)") # send assocociative array in string form to function
    results[solver,list]="${results[solver,list]} $s"
    results[solver,$s]=$tmpRet
  done
  declare -p results # Return results
}

# ---------------------------------------
# GET RESULT FOR ONE PROBLEM
#     (ALL VARIANT AND ALL SOLVERS)
# ---------------------------------------
function resultsForProblem
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A results
  declare REGEX=""

  # Build regular expression
  declare -i i=1;
  for p in $listParams; do
    if [ $i -eq 2 ]; then
      REGEX="$REGEX([^;]*);"
    elif [ -n "${params[$p]}" ]; then
      REGEX="$REGEX${params[$p]};"
    else
      REGEX="$REGEX[^;]*;"
    fi
    i=$(( $i + 1 ))
  done
  REGEX="$REGEX.*"

  # Extract corresponding lines
  declare lines=""
  for line in ${params[INPUT]}; do
    if [[ $line =~ ^$REGEX$ ]]; then
      lines="$lines $line"
    fi
  done

  # Build list of variant values (no duplicate in the list)
  declare -a variant_names=$(
    for line in $lines; do
      [[ $line =~ ^$REGEX$ ]] && echo ${BASH_REMATCH[1]}
    done | uniq
  )

  # Iterate over list of variant names
  results[variant_name,list]=""
  for vn in $variant_names; do
    params[INPUT]=$lines
    params[variant_name]=$vn
    declare tmpRet=$(resultsForVariant "$(declare -p params)") # send assocociative array in string form to function
    results[variant_name,list]="${results[variant_name,list]} $vn"
    results[variant_name,$vn]=$tmpRet
  done
  declare -p results # Return results
}

# ---------------------------------------
# GET RESULT FOR ALL PROBLEMS
# ALL VARIANT AND ALL SOLVERS
# ---------------------------------------
function resultsForAll
{
  # eval string into a new assocociative array
  eval "declare -A params"=${1#*=}
  declare -A results
  declare REGEX=""

  # Build regular expression
  declare -i i=1;
  for p in $listParams; do
    if [ $i -eq 1 ]; then
      REGEX="$REGEX([^;]*);"
    elif [ -n "${params[$p]}" ]; then
      REGEX="$REGEX${params[$p]};"
    else
      REGEX="$REGEX[^;]*;"
    fi
    i=$(( $i + 1 ))
  done
  REGEX="$REGEX.*"

  # Extract corresponding lines
  declare lines=""
  for line in ${params[INPUT]}; do
    if [[ $line =~ ^$REGEX$ ]]; then
      lines="$lines $line"
    fi
  done

  # Build list of variant values (no duplicate in the list)
  declare -a problems=$(
    for line in $lines; do
      [[ $line =~ ^$REGEX$ ]] && echo ${BASH_REMATCH[1]}
    done | uniq
  )

  # Iterate over list of variant names
  results[problem,list]=""
  for pb in $problems; do
    params[INPUT]=$lines
    params[problem]=$pb
    declare tmpRet=$(resultsForProblem "$(declare -p params)") # send assocociative array in string form to function
    results[problem,list]="${results[problem,list]} $pb"
    results[problem,$pb]=$tmpRet
  done
  declare -p results # Return results
}

# ---------------------------------------
# EGREP FROM RESULTS
# ---------------------------------------
function egrepResults
{
  declare REGEX=""
  for p in $listParams; do
    if [ -n "${params[$p]}" ]; then
      REGEX="$REGEX${params[$p]};"
    else
      REGEX="$REGEX[^;]*;"
    fi
  done

  for p in $listResults; do
    REGEX="$REGEX([^;]*);"
  done
  REGEX=${REGEX%;} # Remove the last ;

  declare RES_INPUT=$(cat $RES_INTPUT_FILE | grep -v "^#" | grep -v "^$" | egrep $REGEX)
  params[INPUT]=$RES_INPUT
  declare tmpRet=$(resultsForAll "$(declare -p params)") # send assocociative array in string form to function
  eval "declare -A results"=${tmpRet#*=}
  declare -p results
}

declare tmpRet=$(egrepResults)
eval "declare -A results"=${tmpRet#*=}

# Print results
for r in $listResults; do
  echo
  echo "---------------------------------------"
  echo "$r"
  echo "---------------------------------------"
  for pb in ${results[problem,list]}; do
    eval "declare -A resPb"=${results[problem,$pb]#*=}

    for vn in ${resPb[variant_name,list]}; do
      eval "declare -A resVn"=${resPb[variant_name,$vn]#*=}

      for s in ${resVn[solver,list]}; do
        eval "declare -A resSolvers"=${resVn[solver,$s]#*=}
        echo -n "[ $pb $s $vn ] "

        declare INFTY=""
        for vr in ${resSolvers[variant_range,list]}; do
          eval "declare -A resVr"=${resSolvers[variant_range,$vr]#*=}
          if [ -n "${resVr[$r]}" ]; then
            echo -n "($vr,${resVr[$r]})"
          else
            if [ -z "$INFTY" ]; then
              echo -n "($vr,oo)"
              INFTY="OK"
            fi
          fi
        done
        echo

      done
    done
  done
done
