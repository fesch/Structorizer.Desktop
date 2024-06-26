#!/usr/bin/ksh
# Generated by Structorizer 3.32-20 

# Copyright (C) 2020-03-21 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

# Draws a bar chart from the array "values" of size nValues. 
# Turtleizer must be activated and will scale the chart into a square of 
# 500 x 500 pixels 
# Note: The function is not robust against empty array or totally equal values. 
function drawBarChart {
 typeset -n values=$1
 typeset nValues=$2
# TODO: Check and revise the syntax of all expressions! 

 typeset yScale
 typeset yAxis
 typeset -E valMin
 typeset -E valMax
 typeset stripeWidth
 typeset stripeHeight
 typeset -i kMin
 typeset -i kMax
 typeset -i k
 # Used range of the Turtleizer screen 
 typeset -ir xSize=500
 typeset -ir ySize=500
 kMin=0
 kMax=0

 for (( k=1; k<=(( ${nValues}-1 )); k++ ))
 do

  if [[ ${values[${k}]} > ${values[${kMax}]} ]]
  then
   kMax=${k}

  else

   if [[ ${values[${k}]} < ${values[${kMin}]} ]]
   then
    kMin=${k}
   fi

  fi

 done

 valMin=${values[${kMin}]}
 valMax=${values[${kMax}]}
 yScale=$(( ${valMax} * 1.0 / (${ySize} - 1) ))
 yAxis=$(( ${ySize} - 1 ))

 if (( ${valMin} < 0 ))
 then

  if (( ${valMax} > 0 ))
  then
   yAxis=$(( ${valMax} * ${ySize} * 1.0 / (${valMax} - ${valMin}) ))
   yScale=(${valMax} - ${valMin}) * 1.0 / (${ySize} - 1)

  else
   yAxis=1
   yScale=$(( ${valMin} * 1.0 / (${ySize} - 1) ))
  fi

 fi

 # draw coordinate axes 
 gotoXY 1 $(( ${ySize} - 1 ))
 forward $(( ${ySize} -1 )) # color = ffffff
 penUp
 backward "${yAxis}" # color = ffffff
 right 90
 penDown
 forward $(( ${xSize} -1 )) # color = ffffff
 penUp
 backward $(( ${xSize}-1 )) # color = ffffff
 stripeWidth=$(( ${xSize} / ${nValues} ))

 for (( k=0; k<=(( ${nValues}-1 )); k++ ))
 do
  stripeHeight=$(( ${values[${k}]} * 1.0 / ${yScale} ))

  case (( ${k} % 3 )) in

   0)
     setPenColor 255 0 0
   ;;

   1)
     setPenColor 0 255 0
   ;;

   2)
     setPenColor 0 0 255
   ;;
  esac

  fd 1 # color = ffffff
  left 90
  penDown
  fd "${stripeHeight}" # color = ffffff
  right 90
  fd $(( ${stripeWidth} - 1 )) # color = ffffff
  right 90
  forward "${stripeHeight}" # color = ffffff
  left 90
  penUp
 done

}

# Tries to read as many integer values as possible upto maxNumbers 
# from file fileName into the given array numbers. 
# Returns the number of the actually read numbers. May cause an exception. 
function readNumbers {
 typeset fileName=$1
 typeset -n numbers=$2
 typeset -i maxNumbers=$3
# TODO: Check and revise the syntax of all expressions! 

 typeset -i number
 typeset -i nNumbers
 typeset -i fileNo
 nNumbers=0
 # TODO File API: Replace the "fileOpen" call by an appropriate shell construct 
 fileNo=$( fileOpen "${fileName}" )

 if (( ${fileNo} <= 0 ))
 then
  # throw "File could not be opened!" (FIXME!) 
 fi

 # try (FIXME!) 

  # TODO File API: Replace the "fileEOF" call by an appropriate shell construct 
  while (( ! fileEOF(${fileNo}) && ${nNumbers} < ${maxNumbers} ))
  do
   # TODO File API: Replace the "fileReadInt" call by an appropriate shell construct 
   number=$( fileReadInt "${fileNo}" )
   numbers[${nNumbers}]=${number}
   nNumbers=$(( ${nNumbers} + 1 ))
  done

 # catch error (FIXME!) 
  # throw (FIXME!) 
 # finally (FIXME!) 
  # TODO File API: Replace the "fileClose" call by an appropriate shell construct 
  fileClose "${fileNo}"
 # end try (FIXME!) 
 result70caac66=${nNumbers}
}
# TODO The exported algorithms made use of the Structorizer File API. 
#      Unfortunately there are no comparable constructs in shell 
#      syntax for automatic conversion. 
#      The respective lines are marked with a TODO File API comment. 
#      You might try something like "echo value >> filename" for output 
#      or "while ... do ... read var ... done < filename" for input. 
# Reads a random number file and draws a histogram accotrding to the 
# user specifications 
# TODO: Check and revise the syntax of all expressions! 

fileNo=$(( -10 ))

# NOTE: Represents a REPEAT UNTIL loop, see conditional break at the end. 
while :
do
 echo -n "Name/path of the number file" ; read file_name
 # TODO File API: Replace the "fileOpen" call by an appropriate shell construct 
 fileNo=$( fileOpen "${file_name}" )
 (( ! (${fileNo} > 0 || ${file_name} == "") )) || break
done

if (( ${fileNo} > 0 ))
then
 # TODO File API: Replace the "fileClose" call by an appropriate shell construct 
 fileClose "${fileNo}"
 echo -n "number of intervals" ; read nIntervals

 # Initialize the interval counters 
 for (( k=0; k<=(( ${nIntervals}-1 )); k++ ))
 do
  count[${k}]=0
 done

 # Index of the most populated interval 
 kMaxCount=0
 set -A numberArray
 nObtained=0
 # try (FIXME!) 
  readNumbers "${file_name}" numberArray 10000
  nObtained=${result70caac66}
 # catch failure (FIXME!) 
  echo failure
 # finally (FIXME!) 
  :
 # end try (FIXME!) 

 if (( ${nObtained} > 0 ))
 then
  min=${numberArray[0]}
  max=${numberArray[0]}

  for (( i=1; i<=(( ${nObtained}-1 )); i++ ))
  do

   if [[ ${numberArray[${i}]} < ${min} ]]
   then
    min=${numberArray[${i}]}

   else

    if [[ ${numberArray[${i}]} > ${max} ]]
    then
     max=${numberArray[${i}]}
    fi

   fi

  done

  # Interval width 
  width=$(( (${max} - ${min}) * 1.0 / ${nIntervals} ))

  for (( i=0; i<=(( ${nObtained} - 1 )); i++ ))
  do
   value=${numberArray[${i}]}
   k=1

   while (( ${k} < ${nIntervals} && ${value} > ${min} + ${k} * ${width} ))
   do
    k=$(( ${k} + 1 ))
   done

   count[${k}-1]=$(( ${count[${k}-1]} + 1 ))

   if (( ${count[${k}-1]} > ${count[${kMaxCount}]} ))
   then
    kMaxCount=$(( ${k}-1 ))
   fi

  done

  drawBarChart count "${nIntervals}"
  echo "Interval with max count: " ${kMaxCount} " (" ${count[${kMaxCount}]} ")"

  for (( k=0; k<=(( ${nIntervals}-1 )); k++ ))
  do
   echo (( ${count[${k}]}, " numbers in interval ", ${k}, " (", ${min} + ${k} * ${width}, " ... ", ${min} + (${k}+1) * ${width}, ")" ))
  done

 else
  echo "No numbers read."
 fi

fi
