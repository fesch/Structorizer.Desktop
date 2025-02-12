#!/usr/bin/ksh
# Generated by Structorizer 3.32-26 

# Copyright (C) 2017-09-18 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

# Detects whether the given year is a leap year in the Gregorian calendar 
# (extrapolated backwards beyonds its inauguration) 
function isLeapYear {
 typeset year=$1
# TODO: Check and revise the syntax of all expressions! 

 typeset isLeapYear
 # Most years aren't leap years... 
 isLeapYear=0

 if [[ (${year} % 4 == 0) && (${year} % 100 != 0) ]]
 then
  # This is a standard leap year 
  isLeapYear=1

 else

  if (( ${year} % 400 == 0 ))
  then
   # One of the rare leap years 
   # occurring every 400 years 
   isLeapYear=1
  fi

 fi

 result11758f2a=${isLeapYear}
}

# Computes the number of days the given month (1..12) 
# has in the the given year 
function daysInMonth423 {
 typeset -n aDate=$1
# TODO: Check and revise the syntax of all expressions! 

 typeset isLeap
 typeset -i days

 # select the case where illegal values are also considered 
 case ${aDate[month]} in

  1|3|5|7|8|10|12)
    days=31
  ;;

  4|6|9|11)
    days=30
  ;;

  2)
    # Default value for February 
    days=28
    # To make the call work it has to be done in 
    # a separate element (cannot be performed 
    # as part of the condition of an Alternative) 
    isLeapYear ${aDate[year]}
    isLeap=${result11758f2a}

    if [[ ${isLeap} ]]
    then
     days=29
    fi

  ;;

  *)
   # This is the return value for illegal months. 
   # It is easy to check 
   days=0
  ;;
 esac

 result612679d6=${days}
}
# = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

# auxCopyAssocArray() - copies an associative array via name references 
auxCopyAssocArray() {
 typeset -n target=$1
 typeset -n source=$2
 typeset key
 for key in "${!source[@]}"; do
  target[$key]="${source[$key]}"
 done
}

# Several declaration and initialisation variants for test of Analyser, Executor, and Generators 
# TODO: Check and revise the syntax of all expressions! 

typeset -A today
typeset -A someDay
typeset -A me
typeset -A someDay=([day]=24 [month]=2 [year]=2017)
daysInMonth423 someDay
nDays=${result612679d6}
typeset -A today=([year]=2018 [month]=7 [day]=20)
typeset -A me=([name]="roger" [birth]=Date{1985, 3, 6} [test]={0, 8, 15})
set -A declArray 9.0 7.5 $(( -6.4 )) 1.7 0.0
set -A explArray 7.1 0.5 $(( -1.5 ))
set -A doof 0.4
set -A dull $(( -12.7 )) 96.03
set -A values 47 11
