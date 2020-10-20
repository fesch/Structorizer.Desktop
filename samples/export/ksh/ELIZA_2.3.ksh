#!/usr/bin/ksh
# Generated by Structorizer 3.30-11 

# Copyright (C) 2018-05-14 Kay Gürtzig 
# License: GPLv3-link 
# GNU General Public License (V 3) 
# https://www.gnu.org/licenses/gpl.html 
# http://www.gnu.de/documents/gpl.de.html 

# Cares for correct letter case among others 
function adjustSpelling {
 typeset sentence=$1
# TODO: Check and revise the syntax of all expressions! 

 typeset word
 typeset start
 typeset result
 typeset -i position
 result=${sentence}
 position=1

 while (( (${position} <= length(${sentence})) && (copy(${sentence}, ${position}, 1) == " ") ))
 do
  position=$(( ${position} + 1 ))
 done

 if (( ${position} <= length(${sentence}) ))
 then
  start=$( copy "${sentence}" 1 "${position}" )
  delete "${result}" 1 "${position}"
  insert $( uppercase "${start}" ) "${result}" 1
 fi

 for word in " i " " i\'"
 do
  position=$( pos "${word}" "${result}" )

  while (( ${position} > 0 ))
  do
   delete "${result}" $(( ${position}+1 )) 1
   insert "I" "${result}" $(( ${position}+1 ))
   position=$( pos "${word}" "${result}" )
  done

 done

 result2205f10e=${result}
}

# Checks whether the given text contains some kind of 
# good-bye phrase inducing the end of the conversation 
# and if so writes a correspding good-bye message and 
# returns true, otherwise false 
function checkGoodBye {
 typeset text=$1
 typeset -n phrases=$2
# TODO: Check and revise the syntax of all expressions! 

 typeset pair

 for pair in "${phrases[@]}"
 do

  if [[ pos(${pair[0]}, ${text}) > 0 ]]
  then
   echo ${pair[1]}
   result27225003=1
   return 0
  fi

 done

 result27225003=0
}

# Checks whether newInput has occurred among the recently cached 
# input strings in the histArray component of history and updates the history. 
function checkRepetition {
 typeset -n history=$1
 typeset newInput=$2
# TODO: Check and revise the syntax of all expressions! 

 typeset -i i
 typeset -i histDepth
 typeset hasOccurred
 hasOccurred=0

 if [[ length(${newInput}) > 4 ]]
 then
  histDepth=$( length ${history[histArray]} )

  for (( i=0; i<=(( ${histDepth}-1 )); i++ ))
  do

   if [[ ${newInput} == ${history[histArray]}[${i}] ]]
   then
    hasOccurred=1
   fi

  done

  history[histArray][history[histIndex]]=${newInput}
  history[histIndex]=(${history[histIndex]} + 1) % (${histDepth})
 fi

 resultdd307267=${hasOccurred}
}

function conjugateStrings {
 typeset sentence=$1
 typeset key=$2
 typeset -i keyPos=$3
 typeset -n flexions=$4
# TODO: Check and revise the syntax of all expressions! 

 typeset right
 typeset result
 typeset -i position
 typeset pair
 typeset left
 result=" " + copy(${sentence}, ${keyPos} + length(${key}), length(${sentence})) + " "

 for pair in "${flexions[@]}"
 do
  left=""
  right=${result}
  position=$( pos ${pair[0]} "${right}" )

  while (( ${position} > 0 ))
  do
   left=$(( ${left} + copy(${right}, 1, ${position}-1) + ${pair[1]} ))
   right=$( copy "${right}" $(( ${position} + length(${pair[0]}) )) $( length "${right}" ) )
   position=$( pos ${pair[0]} "${right}" )
  done

  result=$(( ${left} + ${right} ))
 done

 # Eliminate multiple spaces 
 position=$( pos "  " "${result}" )

 while (( ${position} > 0 ))
 do
  result=$(( copy(${result}, 1, ${position}-1) + copy(${result}, ${position}+1, length(${result})) ))
  position=$( pos "  " "${result}" )
 done

 resultd5edbe8b=${result}
}

# Looks for the occurrence of the first of the strings 
# contained in keywords within the given sentence (in 
# array order). 
# Returns an array of 
# 0: the index of the first identified keyword (if any, otherwise -1), 
# 1: the position inside sentence (0 if not found) 
function findKeyword {
 typeset -r keyMap=$1
 typeset sentence=$2
# TODO: Check and revise the syntax of all expressions! 

 set -A result
 typeset -i position
 typeset -i i
 typeset -A entry
 # Contains the index of the keyword and its position in sentence 
 set -A result $(( -1 )) 0
 i=0

 while (( (${result[0]} < 0) && (${i} < length(${keyMap})) ))
 do
  entry=${keyMap[${i}]}
  position=$( pos ${entry[keyword]} "${sentence}" )

  if (( ${position} > 0 ))
  then
   result[0]=${i}
   result[1]=${position}
  fi

  i=$(( ${i}+1 ))
 done

 set -A resulta4ac2a5c "${result[@]}"; export resulta4ac2a5c
}

# Converts the sentence to lowercase, eliminates all 
# interpunction (i.e. ',', '.', ';'), and pads the 
# sentence among blanks 
function normalizeInput {
 typeset sentence=$1
# TODO: Check and revise the syntax of all expressions! 

 typeset symbol
 typeset result
 typeset -i position
 sentence=$( lowercase "${sentence}" )

 for symbol in '.' ',' ';' '!' '?'
 do
  position=$( pos "${symbol}" "${sentence}" )

  while (( ${position} > 0 ))
  do
   sentence=$(( copy(${sentence}, 1, ${position}-1) + copy(${sentence}, ${position}+1, length(${sentence})) ))
   position=$( pos "${symbol}" "${sentence}" )
  done

 done

 result=" " + ${sentence} + " "
 result7814f7b4=${result}
}

function setupGoodByePhrases {
# TODO: Check and revise the syntax of all expressions! 

 set -A phrases
 set -A phrases[0] " shut" "Okay. If you feel that way I\'ll shut up. ... Your choice."
 set -A phrases[1] "bye" "Well, let\'s end our talk for now. See you later. Bye."
 set -A result5fab96ca "${phrases[@]}"; export result5fab96ca
}

# The lower the index the higher the rank of the keyword (search is sequential). 
# The index of the first keyword found in a user sentence maps to a respective 
# reply ring as defined in `setupReplies()´. 
function setupKeywords {
# TODO: Check and revise the syntax of all expressions! 

 set -A keywords
 # The empty key string (last entry) is the default clause - will always be found 
 typeset -A keywords[39]=([keyword]="" [index]=29)
 typeset -A keywords[0]=([keyword]="can you " [index]=0)
 typeset -A keywords[1]=([keyword]="can i " [index]=1)
 typeset -A keywords[2]=([keyword]="you are " [index]=2)
 typeset -A keywords[3]=([keyword]="you\'re " [index]=2)
 typeset -A keywords[4]=([keyword]="i don't " [index]=3)
 typeset -A keywords[5]=([keyword]="i feel " [index]=4)
 typeset -A keywords[6]=([keyword]="why don\'t you " [index]=5)
 typeset -A keywords[7]=([keyword]="why can\'t i " [index]=6)
 typeset -A keywords[8]=([keyword]="are you " [index]=7)
 typeset -A keywords[9]=([keyword]="i can\'t " [index]=8)
 typeset -A keywords[10]=([keyword]="i am " [index]=9)
 typeset -A keywords[11]=([keyword]="i\'m " [index]=9)
 typeset -A keywords[12]=([keyword]="you " [index]=10)
 typeset -A keywords[13]=([keyword]="i want " [index]=11)
 typeset -A keywords[14]=([keyword]="what " [index]=12)
 typeset -A keywords[15]=([keyword]="how " [index]=12)
 typeset -A keywords[16]=([keyword]="who " [index]=12)
 typeset -A keywords[17]=([keyword]="where " [index]=12)
 typeset -A keywords[18]=([keyword]="when " [index]=12)
 typeset -A keywords[19]=([keyword]="why " [index]=12)
 typeset -A keywords[20]=([keyword]="name " [index]=13)
 typeset -A keywords[21]=([keyword]="cause " [index]=14)
 typeset -A keywords[22]=([keyword]="sorry " [index]=15)
 typeset -A keywords[23]=([keyword]="dream " [index]=16)
 typeset -A keywords[24]=([keyword]="hello " [index]=17)
 typeset -A keywords[25]=([keyword]="hi " [index]=17)
 typeset -A keywords[26]=([keyword]="maybe " [index]=18)
 typeset -A keywords[27]=([keyword]=" no" [index]=19)
 typeset -A keywords[28]=([keyword]="your " [index]=20)
 typeset -A keywords[29]=([keyword]="always " [index]=21)
 typeset -A keywords[30]=([keyword]="think " [index]=22)
 typeset -A keywords[31]=([keyword]="alike " [index]=23)
 typeset -A keywords[32]=([keyword]="yes " [index]=24)
 typeset -A keywords[33]=([keyword]="friend " [index]=25)
 typeset -A keywords[34]=([keyword]="computer" [index]=26)
 typeset -A keywords[35]=([keyword]="bot " [index]=26)
 typeset -A keywords[36]=([keyword]="smartphone" [index]=27)
 typeset -A keywords[37]=([keyword]="father " [index]=28)
 typeset -A keywords[38]=([keyword]="mother " [index]=28)
 set -A result28fbd817 "${keywords[@]}"; export result28fbd817
}

# Returns an array of pairs of mutualy substitutable  
function setupReflexions {
# TODO: Check and revise the syntax of all expressions! 

 set -A reflexions
 set -A reflexions[0] " are " " am "
 set -A reflexions[1] " were " " was "
 set -A reflexions[2] " you " " I "
 set -A reflexions[3] " your" " my"
 set -A reflexions[4] " i\'ve " " you\'ve "
 set -A reflexions[5] " i\'m " " you\'re "
 set -A reflexions[6] " me " " you "
 set -A reflexions[7] " my " " your "
 set -A reflexions[8] " i " " you "
 set -A reflexions[9] " am " " are "
 set -A result51b8010 "${reflexions[@]}"; export result51b8010
}

# This routine sets up the reply rings addressed by the key words defined in 
# routine `setupKeywords()´ and mapped hitherto by the cross table defined 
# in `setupMapping()´ 
function setupReplies {
# TODO: Check and revise the syntax of all expressions! 

 set -A setupReplies
 set -A replies
 # We start with the highest index for performance reasons 
 # (is to avoid frequent array resizing) 
 set -A replies[29] "Say, do you have any psychological problems?" "What does that suggest to you?" "I see." "I'm not sure I understand you fully." "Come come elucidate your thoughts." "Can you elaborate on that?" "That is quite interesting."
 set -A replies[0] "Don't you believe that I can*?" "Perhaps you would like to be like me?" "You want me to be able to*?"
 set -A replies[1] "Perhaps you don't want to*?" "Do you want to be able to*?"
 set -A replies[2] "What makes you think I am*?" "Does it please you to believe I am*?" "Perhaps you would like to be*?" "Do you sometimes wish you were*?"
 set -A replies[3] "Don't you really*?" "Why don't you*?" "Do you wish to be able to*?" "Does that trouble you*?"
 set -A replies[4] "Do you often feel*?" "Are you afraid of feeling*?" "Do you enjoy feeling*?"
 set -A replies[5] "Do you really believe I don't*?" "Perhaps in good time I will*." "Do you want me to*?"
 set -A replies[6] "Do you think you should be able to*?" "Why can't you*?"
 set -A replies[7] "Why are you interested in whether or not I am*?" "Would you prefer if I were not*?" "Perhaps in your fantasies I am*?"
 set -A replies[8] "How do you know you can't*?" "Have you tried?" "Perhaps you can now*."
 set -A replies[9] "Did you come to me because you are*?" "How long have you been*?" "Do you believe it is normal to be*?" "Do you enjoy being*?"
 set -A replies[10] "We were discussing you--not me." "Oh, I*." "You're not really talking about me, are you?"
 set -A replies[11] "What would it mean to you if you got*?" "Why do you want*?" "Suppose you soon got*..." "What if you never got*?" "I sometimes also want*."
 set -A replies[12] "Why do you ask?" "Does that question interest you?" "What answer would please you the most?" "What do you think?" "Are such questions on your mind often?" "What is it that you really want to know?" "Have you asked anyone else?" "Have you asked such questions before?" "What else comes to mind when you ask that?"
 set -A replies[13] "Names don't interest me." "I don't care about names -- please go on."
 set -A replies[14] "Is that the real reason?" "Don't any other reasons come to mind?" "Does that reason explain anything else?" "What other reasons might there be?"
 set -A replies[15] "Please don't apologize!" "Apologies are not necessary." "What feelings do you have when you apologize?" "Don't be so defensive!"
 set -A replies[16] "What does that dream suggest to you?" "Do you dream often?" "What persons appear in your dreams?" "Are you disturbed by your dreams?"
 set -A replies[17] "How do you do ...please state your problem."
 set -A replies[18] "You don't seem quite certain." "Why the uncertain tone?" "Can't you be more positive?" "You aren't sure?" "Don't you know?"
 set -A replies[19] "Are you saying no just to be negative?" "You are being a bit negative." "Why not?" "Are you sure?" "Why no?"
 set -A replies[20] "Why are you concerned about my*?" "What about your own*?"
 set -A replies[21] "Can you think of a specific example?" "When?" "What are you thinking of?" "Really, always?"
 set -A replies[22] "Do you really think so?" "But you are not sure you*?" "Do you doubt you*?"
 set -A replies[23] "In what way?" "What resemblance do you see?" "What does the similarity suggest to you?" "What other connections do you see?" "Could there really be some connection?" "How?" "You seem quite positive."
 set -A replies[24] "Are you sure?" "I see." "I understand."
 set -A replies[25] "Why do you bring up the topic of friends?" "Do your friends worry you?" "Do your friends pick on you?" "Are you sure you have any friends?" "Do you impose on your friends?" "Perhaps your love for friends worries you."
 set -A replies[26] "Do computers worry you?" "Are you talking about me in particular?" "Are you frightened by machines?" "Why do you mention computers?" "What do you think machines have to do with your problem?" "Don't you think computers can help people?" "What is it about machines that worries you?"
 set -A replies[27] "Do you sometimes feel uneasy without a smartphone?" "Have you had these phantasies before?" "Does the world seem more real for you via apps?"
 set -A replies[28] "Tell me more about your family." "Who else in your family*?" "What does family relations mean for you?" "Come on, How old are you?"
 setupReplies="${replies[@]}"
 set -A result3af4e45f "${setupReplies[@]}"; export result3af4e45f
}
# auxCopyAssocArray() - copies an associative array via name references 
auxCopyAssocArray() {
 typeset -n target=$1
 typeset -n source=$2
 typeset key
 for key in "${!source[@]}"; do
  target[$key]="${source[$key]}"
 done
}

# Concept and lisp implementation published by Joseph Weizenbaum (MIT): 
# "ELIZA - A Computer Program For the Study of Natural Language Communication Between Man and Machine" - In: 
# Computational Linguistis 1(1966)9, pp. 36-45 
# Revision history: 
# 2016-10-06 Initial version 
# 2017-03-29 Two diagrams updated (comments translated to English) 
# 2017-03-29 More keywords and replies added 
# 2019-03-14 Replies and mapping reorganised for easier maintenance 
# 2019-03-15 key map joined from keyword array and index map 
# 2019-03-28 Keyword "bot" inserted (same reply ring as "computer") 
# 2019-11-28 New global type "History" (to ensure a homogenous array) 
# TODO: Check and revise the syntax of all expressions! 

typeset -A history
typeset -A entry
# Title information 
echo "************* ELIZA **************"
echo "* Original design by J. Weizenbaum"
echo "**********************************"
echo "* Adapted for Basic on IBM PC by"
echo "* - Patricia Danielson"
echo "* - Paul Hashfield"
echo "**********************************"
echo "* Adapted for Structorizer by"
echo "* - Kay Gürtzig / FH Erfurt 2016"
echo "* Version: 2.3 (2020-02-24)"
echo "* (Requires at least Structorizer 3.30-03 to run)"
echo "**********************************"
# Stores the last five inputs of the user in a ring buffer, 
# the second component is the rolling (over-)write index. 
typeset -A history=([histArray]={"", "", "", "", ""} [histIndex]=0)
setupReplies
set -A replies "${result3af4e45f[@]}"
setupReflexions
set -A reflexions "${result51b8010[@]}"
setupGoodByePhrases
set -A byePhrases "${result5fab96ca[@]}"
setupKeywords
set -A keyMap "${result28fbd817[@]}"
offsets[length(${keyMap})-1]=0
isGone=0
# Starter 
echo "Hi! I\'m your new therapist. My name is Eliza. What\'s your problem?"

# NOTE: Represents a REPEAT UNTIL loop, see conditional break at the end. 
while :
do
 read userInput
 # Converts the input to lowercase, cuts out interpunctation 
 # and pads the string 
 normalizeInput "${userInput}"
 userInput=${result7814f7b4}
 checkGoodBye "${userInput}" byePhrases
 isGone=${result27225003}

 if [[ ! ${isGone} ]]
 then
  reply="Please don\'t repeat yourself!"
  checkRepetition history "${userInput}"
  isRepeated=${resultdd307267}

  if [[ ! ${isRepeated} ]]
  then
   findKeyword keyMap "${userInput}"
   set -A findInfo "${resulta4ac2a5c[@]}"
   keyIndex=${findInfo[0]}

   if [[ ${keyIndex} < 0 ]]
   then
    # Should never happen... 
    keyIndex=$(( length(${keyMap})-1 ))
   fi

   entry=${keyMap[${keyIndex}]}
   # Variable part of the reply 
   varPart=""

   if (( length(${entry[keyword]}) > 0 ))
   then
    conjugateStrings "${userInput}" ${entry[keyword]} ${findInfo[1]} reflexions
    varPart=${resultd5edbe8b}
   fi

   replyRing=${replies[${entry[index]}]}
   reply=${replyRing[${offsets[${keyIndex}]}]}
   offsets[${keyIndex}]=(${offsets[${keyIndex}]} + 1) % length(${replyRing})
   posAster=$( pos "*" "${reply}" )

   if (( ${posAster} > 0 ))
   then

    if [[ ${varPart} == " " ]]
    then
     reply="You will have to elaborate more for me to help you."

    else
     delete "${reply}" "${posAster}" 1
     insert "${varPart}" "${reply}" "${posAster}"
    fi

   fi

   adjustSpelling "${reply}"
   reply=${result2205f10e}
  fi

  echo ${reply}
 fi

 [[ ! (${isGone}) ]] || break
done
