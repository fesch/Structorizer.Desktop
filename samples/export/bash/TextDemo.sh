#!/bin/bash

# Demo program for routine drawText() 
# Asks the user to enter a text, a wanted text height and colour, 
# and then draws this string onto the turtle screen. Places every 
# entered text to a new line. 
# (generated by Structorizer 3.30-06) 
#  
# Copyright (C) 2019-10-10 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 
#  

# Draws a blank for font height h, ignoring the colorNo 
function blank() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 width=$(( ${h}/2.0 ))
 penUp
 right 90
 forward "${width}" # color = ffffff
 left 90
}

function forward() {
 local len=$1
 local color=$2

 # TODO: Check and revise the syntax of all expressions! 

 case ${color} in

  1)
    forward "${len}" # color = ffffff
  ;;

  2)
    forward "${len}" # color = ff8080
  ;;

  3)
    forward "${len}" # color = ffff80
  ;;

  4)
    forward "${len}" # color = 80ff80
  ;;

  5)
    forward "${len}" # color = 80ffff
  ;;

  6)
    forward "${len}" # color = 0080ff
  ;;

  7)
    forward "${len}" # color = ff80c0
  ;;

  8)
    forward "${len}" # color = c0c0c0
  ;;

  9)
    forward "${len}" # color = ff8000
  ;;

  10)
    forward "${len}" # color = 8080ff
  ;;
 esac

}

# Draws letter A in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterA() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local rotAngle
 local hypo
 width=$(( ${h}/2.0 ))
 hypo=$( sqrt $(( ${h}*${h} + ${width}*${width}/4.0 )) )
 rotAngle=$( toDegrees $( atan $(( ${width}/2.0/${h} )) ) )
 right "${rotAngle}"
 forward $(( ${hypo}/2.0 )) "${colorNo}"
 right $(( 90 - ${rotAngle} ))
 forward $(( ${width}/2.0 )) "${colorNo}"
 penUp
 backward $(( ${width}/2.0 )) # color = ffffff
 penDown
 left $(( 90 - ${rotAngle} ))
 forward $(( ${hypo}/2.0 )) "${colorNo}"
 left $(( 2*${rotAngle} ))
 forward $(( -${hypo} )) "${colorNo}"
 right "${rotAngle}"
}

# Draws letter E in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterE() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 width=$(( ${h}/2.0 ))
 forward "${h}" "${colorNo}"
 right 90
 forward "${width}" "${colorNo}"
 right 90
 penUp
 forward $(( ${h}/2.0 )) # color = ffffff
 right 90
 penDown
 forward "${width}" "${colorNo}"
 left 90
 penUp
 forward $(( ${h}/2.0 )) # color = ffffff
 left 90
 penDown
 forward "${width}" "${colorNo}"
 left 90
}

# Draws letter F in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterF() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 width=$(( ${h}/2.0 ))
 forward "${h}" "${colorNo}"
 right 90
 forward "${width}" "${colorNo}"
 right 90
 penUp
 forward $(( ${h}/2.0 )) # color = ffffff
 right 90
 penDown
 forward "${width}" "${colorNo}"
 left 90
 penUp
 forward $(( ${h}/2.0 )) # color = ffffff
 left 90
 forward "${width}" # color = ffffff
 penDown
 left 90
}

# Draws letter H in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterH() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 width=$(( ${h}/2.0 ))
 forward "${h}" "${colorNo}"
 penUp
 right 90
 forward "${width}" # color = ffffff
 right 90
 penDown
 forward $(( ${h}/2.0 )) "${colorNo}"
 right 90
 forward "${width}" "${colorNo}"
 penUp
 backward "${width}" # color = ffffff
 left 90
 penDown
 forward $(( ${h}/2.0 )) "${colorNo}"
 left 180
}

# Draws letter I in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterI() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local c
 local b
 # Octagon edge length 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Cathetus of the corner triangle outside the octagon 
 c=$(( ${b} / sqrt(2.0) ))
 penUp
 right 90
 forward "${c}" # color = ffffff
 penDown
 forward "${b}" "${colorNo}"
 penUp
 backward $(( ${b}/2.0 )) # color = ffffff
 left 90
 penDown
 forward "${h}" "${colorNo}"
 penUp
 right 90
 backward $(( ${b}/2.0 )) # color = ffffff
 penDown
 forward "${b}" "${colorNo}"
 penUp
 forward $(( ${b}/2 + ${c} )) # color = ffffff
 left 90
 backward "${h}" # color = ffffff
 penDown
}

# Draws letter K in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterK() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local diag
 width=$(( ${h}/2.0 ))
 diag=$(( ${h}/sqrt(2.0) ))
 forward "${h}" "${colorNo}"
 penUp
 right 90
 forward "${width}" # color = ffffff
 right 135
 penDown
 forward "${diag}" "${colorNo}"
 left 90
 forward "${diag}" "${colorNo}"
 left 135
}

# Draws letter L in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterL() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 width=$(( ${h}/2.0 ))
 forward "${h}" "${colorNo}"
 penUp
 backward "${h}" # color = ffffff
 right 90
 penDown
 forward "${width}" "${colorNo}"
 left 90
}

# Draws letter M in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterM() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local rotAngle
 local hypo
 width=$(( ${h}/2.0 ))
 hypo=$(( sqrt(${width}*${width} + ${h}*${h})/2.0 ))
 rotAngle=$( toDegrees $( atan $(( ${width}/${h} )) ) )
 forward "${h}" "${colorNo}"
 left "${rotAngle}"
 forward $(( -${hypo} )) "${colorNo}"
 right $(( 2*${rotAngle} ))
 forward "${hypo}" "${colorNo}"
 left "${rotAngle}"
 forward $(( -${h} )) "${colorNo}"
}

# Draws letter N in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterN() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local rotAngle
 local hypo
 width=$(( ${h}/2.0 ))
 hypo=$( sqrt $(( ${width}*${width} + ${h}*${h} )) )
 rotAngle=$( toDegrees $( atan $(( ${width}/${h} )) ) )
 forward "${h}" "${colorNo}"
 left "${rotAngle}"
 forward $(( -${hypo} )) "${colorNo}"
 right "${rotAngle}"
 forward "${h}" "${colorNo}"
 penUp
 backward "${h}" # color = ffffff
 penDown
}

# Draws letter T in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterT() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 width=$(( ${h}/2.0 ))
 penUp
 forward "${h}" # color = ffffff
 penDown
 right 90
 forward "${width}" "${colorNo}"
 penUp
 backward $(( ${width}/2.0 )) # color = ffffff
 penDown
 right 90
 forward "${h}" "${colorNo}"
 left 90
 penUp
 forward $(( ${width}/2.0 )) # color = ffffff
 penDown
 left 90
}

# Draws letter V in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterV() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local rotAngle
 local hypo
 width=$(( ${h}/2.0 ))
 hypo=$( sqrt $(( ${h}*${h} + ${width}*${width}/4.0 )) )
 rotAngle=$( toDegrees $( atan $(( ${width}/2.0/${h} )) ) )
 penUp
 forward "${h}" # color = ffffff
 left "${rotAngle}"
 penDown
 forward $(( -${hypo} )) "${colorNo}"
 right $(( 2*${rotAngle} ))
 forward "${hypo}" "${colorNo}"
 penUp
 left "${rotAngle}"
 backward "${h}" # color = ffffff
 penDown
}

# Draws letter W in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterW() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width_3
 local width
 local rotAngle
 local hypo
 width=$(( ${h}/2.0 ))
 width_3=$(( ${width}/3.0 ))
 hypo=$( sqrt $(( ${width_3}*${width_3} + ${h}*${h} )) )
 rotAngle=$( toDegrees $( atan $(( ${width_3}/${h} )) ) )
 penUp
 forward "${h}" # color = ffffff
 left "${rotAngle}"
 penDown
 forward $(( -${hypo} )) "${colorNo}"
 right $(( 2*${rotAngle} ))
 forward "${hypo}" "${colorNo}"
 penUp
 left $(( 90+${rotAngle} ))
 forward "${width_3}" # color = ffffff
 right $(( 90-${rotAngle} ))
 penDown
 forward $(( -${hypo} )) "${colorNo}"
 right $(( 2*${rotAngle} ))
 forward "${hypo}" "${colorNo}"
 penUp
 left "${rotAngle}"
 backward "${h}" # color = ffffff
 penDown
}

# Draws letter X in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterX() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local rotAngle
 local hypo
 width=$(( ${h}/2.0 ))
 hypo=$( sqrt $(( ${width}*${width} + ${h}*${h} )) )
 rotAngle=$( toDegrees $( atan $(( ${width}/${h} )) ) )
 right "${rotAngle}"
 forward "${hypo}" "${colorNo}"
 penUp
 left $(( 90+${rotAngle} ))
 forward "${width}" # color = ffffff
 right $(( 90-${rotAngle} ))
 penDown
 forward $(( -${hypo} )) "${colorNo}"
 right "${rotAngle}"
}

# Draws letter Y in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterY() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local rotAngle
 local hypo
 width=$(( ${h}/2.0 ))
 hypo=$(( sqrt(${width}*${width} + ${h}*${h})/2.0 ))
 rotAngle=$( toDegrees $( atan $(( ${width}/${h} )) ) )
 penUp
 forward "${h}" # color = ffffff
 left "${rotAngle}"
 penDown
 forward $(( -${hypo} )) "${colorNo}"
 right "${rotAngle}"
 penUp
 backward $(( ${h}/2.0 )) # color = ffffff
 penDown
 forward $(( ${h}/2.0 )) "${colorNo}"
 right "${rotAngle}"
 forward "${hypo}" "${colorNo}"
 left "${rotAngle}"
 penUp
 backward "${h}" # color = ffffff
 penDown
}

# Draws letter Z in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterZ() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local rotAngle
 local hypo
 width=$(( ${h}/2.0 ))
 hypo=$( sqrt $(( ${width}*${width} + ${h}*${h} )) )
 rotAngle=$( toDegrees $( atan $(( ${width}/${h} )) ) )
 penUp
 forward "${h}" # color = ffffff
 right 90
 penDown
 forward "${width}" "${colorNo}"
 left $(( 90-${rotAngle} ))
 forward $(( -${hypo} )) "${colorNo}"
 right $(( 90-${rotAngle} ))
 forward "${width}" "${colorNo}"
 left 90
}

# Draws nEdges edges of a regular n-polygon with edge length a 
# counter-clockwise, if ctrclkws is true, or clockwise if ctrclkws is false. 
function polygonPart() {
 local a=$1
 declare -i n=$2
 local ctrclkws=$3
 declare -i nEdges=$4
 declare -i color=$5

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local k
 rotAngle=$(( 360.0/${n} ))

 if [[ ${ctrclkws} ]]
 then
  rotAngle=$(( -${rotAngle} ))
 fi

 for (( k=1; k<=${nEdges}; k++ ))
 do
  right "${rotAngle}"
  forward "${a}" "${color}"
 done

}

# Draws a dummy character (small centered square) with font height h and 
# the colour encoded by colorNo 
function charDummy() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local d
 local c
 local b
 width=$(( ${h} / 2.0 ))
 # Octagon edge length (here: edge lengzh of the square) 
 b=$(( ${width} / (sqrt(2.0) + 1) ))
 # Cathetus of the corner triangle outside the octagon 
 c=$(( (${width} - ${b}) / 2.0 ))
 d=$(( ${b} / sqrt(2.0) ))
 penUp
 forward $(( ${h}/2.0-${b}/2.0 )) # color = ffffff
 right 90
 forward "${c}" # color = ffffff
 right 90
 penDown
 # Draws the square with edge length b 
 polygonPart "${b}" 4 1 4 "${colorNo}"
 penUp
 left 90
 forward $(( ${b} + ${c} )) # color = ffffff
 left 90
 backward $(( ${h}/2.0-${b}/2.0 )) # color = ffffff
 penDown
}

# Draws a comma in colour specified by colorNo with font height h 
# from the current turtle position. 
function comma() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local hypo
 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 rotAngle=$( toDegrees $( atan 0.5 ) )
 hypo=$(( ${c} * sqrt(1.25) ))
 penUp
 right 90
 forward $(( (${c}+${b})/2.0 + ${c} )) # color = ffffff
 penDown
 # Counterclockwise draw 3 edges of a square with edge length c 
 # in the colour endcoded by colorNo 
 polygonPart "${c}" 4 1 3 "${colorNo}"
 left 90
 forward $(( ${c}/2.0 )) "${colorNo}"
 right 90
 forward "${c}" "${colorNo}"
 left $(( 180 - ${rotAngle} ))
 forward "${hypo}" "${colorNo}"
 penUp
 right $(( 90 - ${rotAngle} ))
 forward $(( (${c} + ${b})/2.0 )) # color = ffffff
 left 90
 penDown
}

# Draws an exclamation mark in the colour encoded by colorNo with font height h 
# from the current turtle position. 
function exclMk() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local width
 local rotAngle2
 local rotAngle
 local length2
 local length1
 local hypo
 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 width=$(( ${h}/2.0 ))
 length1=$(( ${h} - (${b}+${c})/2.0 ))
 length2=$(( ${length1} - 2*${c} ))
 hypo=$( sqrt $(( ${width}*${width}/16.0 + ${length2}*${length2} )) )
 # 360°/8 
 rotAngle=45
 rotAngle2=$( toDegrees $( atan $(( ${width}/4.0/${length2} )) ) )
 penUp
 forward "${length1}" # color = ffffff
 right 90
 forward $(( ${width}/2.0 )) # color = ffffff
 left $(( 90 + ${rotAngle} ))
 penDown
 # Clockwise draw 5 edges of an octagon with edge length b/2 
 # in the colour endcoded by colorNo 
 polygonPart $(( ${b}/2.0 )) 8 0 5 "${colorNo}"
 right "${rotAngle2}"
 forward "${hypo}" "${colorNo}"
 left $(( 2*${rotAngle2} ))
 forward $(( -${hypo} )) "${colorNo}"
 penUp
 forward "${hypo}" # color = ffffff
 right "${rotAngle2}"
 forward "${c}" # color = ffffff
 left 90
 forward $(( ${c}/2.0 )) # color = ffffff
 penDown
 # Counterclockwise draw all 4 edges of a squarfe with edge length c 
 # in the colour endcoded by colorNo 
 polygonPart "${c}" 4 0 4 "${colorNo}"
 penUp
 forward $(( (${c} + ${b})/2.0 )) # color = ffffff
 left 90
 backward "${c}" # color = ffffff
 penDown
}

# Draws a full stop in colour specified by colorNo with font height h 
# from the current turtle position. 
function fullSt() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 penUp
 right 90
 forward $(( (${c}+${b})/2.0 + ${c} )) # color = ffffff
 penDown
 # Counterclockwise draw all 4 edges of a squarfe with edge length c 
 # in the colour endcoded by colorNo 
 polygonPart "${c}" 4 1 4 "${colorNo}"
 penUp
 forward $(( (${c} + ${b})/2.0 )) # color = ffffff
 left 90
 penDown
}

# Draws letter B in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterB() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local c
 local b
 # Octagon edge length 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Cathetus of the outer corner triangle of the octagon 
 c=$(( ${b} / sqrt(2.0) ))
 forward "${h}" "${colorNo}"
 right 90
 forward $(( ${c}+${b} )) "${colorNo}"
 # Clockwise draw 4 edges of an octagon with edge length b 
 polygonPart "${b}" 8 0 4 "${colorNo}"
 forward "${c}" "${colorNo}"
 penUp
 left 180
 forward $(( ${b} + ${c} )) # color = ffffff
 penDown
 # Clockwise draw 4 edges of an octagon with edge length b 
 polygonPart "${b}" 8 0 4 "${colorNo}"
 forward "${c}" "${colorNo}"
 penUp
 left 180
 forward $(( ${b} + 2*${c} )) # color = ffffff
 penDown
 left 90
}

# Draws letter C in the colour encoded by colorNo with font height h 
# from the current turtle position. 
function letterC() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local c
 local b
 # Octagon edge length 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Cathetus of the outer trinagle at the octagon corner 
 c=$(( ${b} / sqrt(2.0) ))
 # 360°/8 
 rotAngle=45
 penUp
 forward "${c}" # color = ffffff
 penDown
 right 180
 # Clockwise draws 3 edges of an octagon with edge length b in the colour 
 # encoded by colorNo 
 polygonPart "${b}" 8 1 3 "${colorNo}"
 left "${rotAngle}"
 penUp
 forward $(( 2*${b} + 2*${c} )) # color = ffffff
 penDown
 # Counterclockwise draws 4 edges of an octagon with edge length b 
 # iin the colour encoded by colorNo 
 polygonPart "${b}" 8 1 4 "${colorNo}"
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 penUp
 forward "${c}" # color = ffffff
 left 90
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 penDown
 left 90
}

# Draws letter D in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterD() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 forward "${h}" "${colorNo}"
 right 90
 forward $(( ${c}+${b} )) "${colorNo}"
 # Clockwise draw 2 edges of an octagon with edge length b in the colour 
 # encoded by colorNo 
 polygonPart "${b}" 8 0 2 "${colorNo}"
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 # Clockwise draw 2 edges of an octagon with edge length b in the colour 
 # encoded by colorNo 
 polygonPart "${b}" 8 0 2 "${colorNo}"
 forward "${c}" "${colorNo}"
 penUp
 left 180
 forward $(( ${b} + 2*${c} )) # color = ffffff
 penDown
 left 90
}

# Draws letter G in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterG() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local c
 local b
 # Octagon edge length 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Cathetus of the corner triangle outside the octagon. 
 c=$(( ${b} / sqrt(2.0) ))
 penUp
 forward "${c}" # color = ffffff
 penDown
 right 180
 # Counterclockwise draw 4 edges of an octagon with edge length b in 
 # the colour encoded by colorNo 
 polygonPart "${b}" 8 1 4 "${colorNo}"
 forward "${c}" "${colorNo}"
 left 90
 forward $(( ${b}/2.0 + ${c} )) "${colorNo}"
 penUp
 backward $(( ${b}/2.0 + ${c} )) # color = ffffff
 right 90
 forward $(( ${b} + ${c} )) # color = ffffff
 penDown
 # Counterclockwise draw 4 edges of an octagon with edge length b in 
 # the colour encoded by colorNo 
 polygonPart "${b}" 8 1 4 "${colorNo}"
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 penUp
 forward "${c}" # color = ffffff
 left 90
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 penDown
 left 90
}

# Draws letter J in colour encoded by colorNo with font height h 
# from the current turtle position. 
function letterJ() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 # 360°/8 
 rotAngle=45
 penUp
 forward "${c}" # color = ffffff
 penDown
 right 180
 # Counterclockwise draw 3 edges of an octagon with edge length b in 
 # the colour encoded by colorNo 
 polygonPart "${b}" 8 1 3 "${colorNo}"
 left "${rotAngle}"
 forward $(( ${h} - ${c} )) "${colorNo}"
 penUp
 backward "${h}" # color = ffffff
 penDown
}

# Draws letter O in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterO() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local c
 local b
 # Octagon edge length 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Cathetus of the corner triangle outside the octagon 
 c=$(( ${b} / sqrt(2.0) ))
 penUp
 forward "${c}" # color = ffffff
 penDown
 right 180
 # Counterclockwise draw 4 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 1 4 "${colorNo}"
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 # Counterclockwise draw 4 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 1 4 "${colorNo}"
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 penUp
 forward "${c}" # color = ffffff
 left 90
 forward $(( ${b} + 2*${c} )) # color = ffffff
 penDown
 left 90
}

# Draws letter P in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterP() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local c
 local b
 # Octagon edge length 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Cathetus of the corner triangle outside the octagon 
 c=$(( ${b} / sqrt(2.0) ))
 forward "${h}" "${colorNo}"
 right 90
 forward $(( ${c}+${b} )) "${colorNo}"
 # Clockwise draw 4 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 0 4 "${colorNo}"
 forward "${c}" "${colorNo}"
 penUp
 backward $(( ${b} + 2*${c} )) # color = ffffff
 left 90
 forward $(( ${b} + 2*${c} )) # color = ffffff
 penDown
 left 180
}

# Draws letter Q in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterQ() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 # 360°/8 
 rotAngle=45
 penUp
 forward "${c}" # color = ffffff
 penDown
 right 180
 # Counterclockwise draw 4 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 1 4 "${colorNo}"
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 # Counterclockwise draw 4 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 1 4 "${colorNo}"
 forward $(( ${b} + 2*${c} )) "${colorNo}"
 penUp
 forward "${c}" # color = ffffff
 left 90
 forward $(( ${b} + 2*${c} )) # color = ffffff
 right "${rotAngle}"
 backward "${b}" # color = ffffff
 penDown
 forward "${b}" "${colorNo}"
 left $(( 90 + ${rotAngle} ))
}

# Zeichnet den Buchstaben R von der Turtleposition aus 
# mit Zeilenhöhe h 
function letterR() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 # 360°/8 
 rotAngle=45
 forward "${h}" "${colorNo}"
 right 90
 forward $(( ${c}+${b} )) "${colorNo}"
 # Clockwise draw 4 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 0 4 "${colorNo}"
 forward "${c}" "${colorNo}"
 left $(( 90 + ${rotAngle} ))
 forward $(( sqrt(2.0)*(${b} + 2*${c}) )) "${colorNo}"
 left $(( 90 + ${rotAngle} ))
}

# Draws letter S in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterS() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 # 360°/8 
 rotAngle=45
 penUp
 forward "${c}" # color = ffffff
 penDown
 right 180
 # Counterclockwise draw 6 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 1 6 "${colorNo}"
 # Clockwise draw 5 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 0 5 "${colorNo}"
 right "${rotAngle}"
 penUp
 forward $(( 2*${b} + 3*${c} )) # color = ffffff
 penDown
 left 180
}

# Draws letter U in colour specified by colorNo with font height h 
# from the current turtle position. 
function letterU() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local c
 local b
 # edge length of a regular octagon 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 # 360°/8 
 rotAngle=45
 penUp
 forward "${c}" # color = ffffff
 penDown
 forward $(( ${h} - ${c} )) "${colorNo}"
 penUp
 backward $(( ${h}-${c} )) # color = ffffff
 penDown
 right 180
 # Counterclockwise draw 3 edges of an octagoin with edge length b in colour specified by colorNo 
 polygonPart "${b}" 8 1 3 "${colorNo}"
 left "${rotAngle}"
 forward $(( ${h} - ${c} )) "${colorNo}"
 penUp
 backward "${h}" # color = ffffff
 penDown
}

# Draws a question mark in colour specified by colorNo with font height h 
# from the current turtle position. 
function qstnMk() {
 local h=$1
 local colorNo=$2

 # TODO: Check and revise the syntax of all expressions! 

 local rotAngle
 local c
 local b
 # Achteck-Kantenlänge 
 b=$(( ${h} * 0.5 / (sqrt(2.0) + 1) ))
 # Eckenlänge außen am Achteck 
 c=$(( ${b} / sqrt(2.0) ))
 # 360°/8 
 rotAngle=45
 penUp
 forward $(( ${h}-${c} )) # color = ffffff
 penDown
 # Counterclockwise draw 5 edges of an octagon with edge length b 
 # in the colour endcoded by colorNo 
 polygonPart "${b}" 8 0 5 "${colorNo}"
 forward "${c}" "${colorNo}"
 left "${rotAngle}"
 forward $(( ${b}/2.0 )) "${colorNo}"
 penUp
 forward "${c}" # color = ffffff
 left 90
 forward $(( ${c}/2.0 )) # color = ffffff
 penDown
 # Counterclockwise draw all 4 edges of a squarfe with edge length c 
 # in the colour endcoded by colorNo 
 polygonPart "${c}" 4 0 4 "${colorNo}"
 penUp
 forward $(( (${c} + ${b})/2.0 )) # color = ffffff
 left 90
 backward "${c}" # color = ffffff
 penDown
}

# Has the turtle draw the given string 'text´ with font height 'h´ (in 
# pixels) and the colour coded by integer 'c´ from the current Turtle 
# position to the Turtle canvas. If the turtle looks North then 
# the text will be written rightwards. In the event, the turtle will be 
# placed behind the text in original orientation (such that the next text 
# would be written like a continuation. Colour codes: 
# 1 = black 
# 2 = red 
# 3 = yellow 
# 4 = green 
# 5 = cyan 
# 6 = blue 
# 7 = pink 
# 8 = grey 
# 9 = orange 
# 10 = violet 
# All letters (ASCII) will be converted to uppercase, digits cannot 
# be represented, the set of representable special characters is: 
# '.', ',', '!', '?'. Other characters will be shown as a small 
# centred square (dummy character). 
function drawText() {
 local text=$1
 declare -i h=$2
 declare -i c=$3

 # TODO: Check and revise the syntax of all expressions! 

 local letter
 local k
 local gap
 gap=$(( ${h}/10.0 ))

 for (( k=1; k<=length "${text}"; k++ ))
 do
  letter=$( uppercase $( copy "${text}" "${k}" 1 ) )

  if [[ ${letter} == "," ]]
  then
   comma "${h}" "${c}"

  else

   # "," cannot be chacked against because the comma is misinterpreted 
   # as selector list separator. 
   case ${letter} in

    "A")
      letterA "${h}" "${c}"
    ;;

    "B")
      letterB "${h}" "${c}"
    ;;

    "C")
      letterC "${h}" "${c}"
    ;;

    "D")
      letterD "${h}" "${c}"
    ;;

    "E")
      letterE "${h}" "${c}"
    ;;

    "F")
      letterF "${h}" "${c}"
    ;;

    "G")
      letterG "${h}" "${c}"
    ;;

    "H")
      letterH "${h}" "${c}"
    ;;

    "I")
      letterI "${h}" "${c}"
    ;;

    "J")
      letterJ "${h}" "${c}"
    ;;

    "K")
      letterK "${h}" "${c}"
    ;;

    "L")
      letterL "${h}" "${c}"
    ;;

    "M")
      letterM "${h}" "${c}"
    ;;

    "N")
      letterN "${h}" "${c}"
    ;;

    "O")
      letterO "${h}" "${c}"
    ;;

    "P")
      letterP "${h}" "${c}"
    ;;

    "Q")
      letterQ "${h}" "${c}"
    ;;

    "R")
      letterR "${h}" "${c}"
    ;;

    "S")
      letterS "${h}" "${c}"
    ;;

    "T")
      letterT "${h}" "${c}"
    ;;

    "U")
      letterU "${h}" "${c}"
    ;;

    "V")
      letterV "${h}" "${c}"
    ;;

    "W")
      letterW "${h}" "${c}"
    ;;

    "X")
      letterX "${h}" "${c}"
    ;;

    "Y")
      letterY "${h}" "${c}"
    ;;

    "Z")
      letterZ "${h}" "${c}"
    ;;

    " ")
      blank "${h}" "${c}"
    ;;

    "!")
      exclMk "${h}" "${c}"
    ;;

    "?")
      qstnMk "${h}" "${c}"
    ;;

    ".")
      fullSt "${h}" "${c}"
    ;;

    *)
     charDummy "${h}" "${c}"
    ;;
   esac

  fi

  right 90
  penUp
  forward "${gap}" # color = ffffff
  penDown
  left 90
 done

}


# TODO: Check and revise the syntax of all expressions! 

echo "This is a demo program for text writing with Turleizer."
showTurtle
penDown
y=0

# NOTE: Represents a REPEAT UNTIL loop, see conditional break at the end. 
while :
do
 echo -n "Enter some text (empty string to exit)" ; read text
 # Make sure the content is interpreted as string 
 text=$(( "" + ${text} ))

 if [[ ${text} != "" ]]
 then

  # NOTE: Represents a REPEAT UNTIL loop, see conditional break at the end. 
  while :
  do
   echo -n "Height of the text (pixels)" ; read height
   (( ! (${height} >= 5) )) || break
  done

  # NOTE: Represents a REPEAT UNTIL loop, see conditional break at the end. 
  while :
  do
   echo -n "Colour (1=black, 2=red, 3=yellow, 4=green, 5=cyan, 6=blue, 7=pink, 8=gray, 9=orange, 10=violet)" ; read colour
   (( ! (${colour} >= 1 && ${colour} <= 10) )) || break
  done

  y=$(( ${y} + ${height} + 2 ))
  gotoXY 0 $(( ${y} - 2 ))
  drawText "${text}" "${height}" "${colour}"
 fi

 [[ ! (${text} == "") ]] || break
done

gotoXY 0 $(( ${y} + 15 ))
drawText "Thank you, bye." 10 4
hideTurtle
