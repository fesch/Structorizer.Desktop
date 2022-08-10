10 REM Concept and lisp implementation published by Joseph Weizenbaum (MIT): 
20 REM "ELIZA - A Computer Program For the Study of Natural Language Communication Between Man and Machine" - In: 
30 REM Computational Linguistis 1(1966)9, pp. 36-45 
40 REM Revision history: 
50 REM 2016-10-06 Initial version 
60 REM 2017-03-29 Two diagrams updated (comments translated to English) 
70 REM 2017-03-29 More keywords and replies added 
80 REM 2019-03-14 Replies and mapping reorganised for easier maintenance 
90 REM 2019-03-15 key map joined from keyword array and index map 
100 REM 2019-03-28 Keyword "bot" inserted (same reply ring as "computer") 
110 REM 2019-11-28 New global type "History" (to ensure a homogenous array) 
120 REM 2022-01-11 Measures against substition inversions a -> b -> a in conjugateStrings, reflexions revised. 
130 REM Generated by Structorizer 3.32-10 
140 
150 REM Copyright (C) 2018-05-14 Kay Gürtzig 
160 REM License: GPLv3-link 
170 REM GNU General Public License (V 3) 
180 REM https://www.gnu.org/licenses/gpl.html 
190 REM http://www.gnu.de/documents/gpl.de.html 
200 
210 REM  
220 REM program ELIZA
230 REM TODO: add the respective type suffixes to your variable names if required 
240 REM Title information 
250 PRINT "************* ELIZA **************"
260 PRINT "* Original design by J. Weizenbaum"
270 PRINT "**********************************"
280 PRINT "* Adapted for Basic on IBM PC by"
290 PRINT "* - Patricia Danielson"
300 PRINT "* - Paul Hashfield"
310 PRINT "**********************************"
320 PRINT "* Adapted for Structorizer by"
330 PRINT "* - Kay Gürtzig / FH Erfurt 2016"
340 PRINT "* Version: 2.4 (2022-01-11)"
350 PRINT "* (Requires at least Structorizer 3.30-03 to run)"
360 PRINT "**********************************"
370 REM Stores the last five inputs of the user in a ring buffer, 
380 REM the second component is the rolling (over-)write index. 
390 DIM history AS History
400 LET history.histArray = {"", "", "", "", ""}
410 LET history.histIndex = 0
420 LET replies = setupReplies()
430 LET reflexions = setupReflexions()
440 LET byePhrases = setupGoodByePhrases()
450 LET keyMap = setupKeywords()
460 LET offsets(length(keyMap)-1) = 0
470 LET isGone = false
480 REM Starter 
490 PRINT "Hi! I\'m your new therapist. My name is Eliza. What\'s your problem?"
500 DO
510   INPUT userInput
520   REM Converts the input to lowercase, cuts out interpunctation 
530   REM and pads the string 
540   LET userInput = normalizeInput(userInput)
550   LET isGone = checkGoodBye(userInput, byePhrases)
560   IF NOT isGone THEN
570     LET reply = "Please don\'t repeat yourself!"
580     LET isRepeated = checkRepetition(history, userInput)
590     IF NOT isRepeated THEN
600       LET findInfo = findKeyword(keyMap, userInput)
610       LET keyIndex = findInfo(0)
620       IF keyIndex < 0 THEN
630         REM Should never happen... 
640         LET keyIndex = length(keyMap)-1
650       END IF
660       LET var entry: KeyMapEntry = keyMap(keyIndex)
670       REM Variable part of the reply 
680       LET varPart = ""
690       IF length(entry.keyword) > 0 THEN
700         LET varPart = conjugateStrings(userInput, entry.keyword, findInfo(1), reflexions)
710       END IF
720       LET replyRing = replies(entry.index)
730       LET reply = replyRing(offsets(keyIndex))
740       LET offsets(keyIndex) = (offsets(keyIndex) + 1) % length(replyRing)
750       LET posAster = pos("*", reply)
760       IF posAster > 0 THEN
770         IF varPart = " " THEN
780           LET reply = "You will have to elaborate more for me to help you."
790         ELSE
800           delete(reply, posAster, 1)
810           insert(varPart, reply, posAster)
820         END IF
830       END IF
840       LET reply = adjustSpelling(reply)
850     END IF
860     PRINT reply
870   END IF
880 LOOP UNTIL isGone
890 END
900 REM  
910 REM Cares for correct letter case among others 
920 REM TODO: Add type-specific suffixes where necessary! 
930 FUNCTION adjustSpelling(sentence AS String) AS String
940   REM TODO: add the respective type suffixes to your variable names if required 
950   LET result = sentence
960   LET position = 1
970   DO WHILE (position <= length(sentence)) AND (copy(sentence, position, 1) = " ")
980     LET position = position + 1
990   LOOP
1000   IF position <= length(sentence) THEN
1010     LET start = copy(sentence, 1, position)
1020     delete(result, 1, position)
1030     insert(uppercase(start), result, 1)
1040   END IF
1050   DIM array754d6492() AS String = {" i ", " i\'"}
1060   FOR EACH word IN array754d6492
1070     LET position = pos(word, result)
1080     DO WHILE position > 0
1090       delete(result, position+1, 1)
1100       insert("I", result, position+1)
1110       LET position = pos(word, result)
1120     LOOP
1130   NEXT word
1140   RETURN result
1150 END FUNCTION
1160 REM  
1170 REM Checks whether the given text contains some kind of 
1180 REM good-bye phrase inducing the end of the conversation 
1190 REM and if so writes a correspding good-bye message and 
1200 REM returns true, otherwise false 
1210 REM TODO: Add type-specific suffixes where necessary! 
1220 FUNCTION checkGoodBye(text AS String, phrases AS String(50,0 TO 1)) AS boolean
1230   REM TODO: add the respective type suffixes to your variable names if required 
1240   FOR EACH pair IN phrases
1250     IF pos(pair(0), text) > 0 THEN
1260       PRINT pair(1)
1270       RETURN true
1280     END IF
1290   NEXT pair
1300   return false
1310 END FUNCTION
1320 REM  
1330 REM Checks whether newInput has occurred among the recently cached 
1340 REM input strings in the histArray component of history and updates the history. 
1350 REM TODO: Add type-specific suffixes where necessary! 
1360 FUNCTION checkRepetition(history AS History, newInput AS String) AS boolean
1370   REM TODO: add the respective type suffixes to your variable names if required 
1380   LET hasOccurred = false
1390   IF length(newInput) > 4 THEN
1400     LET histDepth = length(history.histArray)
1410     FOR i = 0 TO histDepth-1
1420       IF newInput = history.histArray(i) THEN
1430         LET hasOccurred = true
1440       END IF
1450     NEXT i
1460     LET history.histArray(history.histIndex) = newInput
1470     LET history.histIndex = (history.histIndex + 1) % (histDepth)
1480   END IF
1490   return hasOccurred
1500 END FUNCTION
1510 REM  
1520 REM TODO: Add type-specific suffixes where necessary! 
1530 FUNCTION conjugateStrings(sentence AS String, key AS String, keyPos AS integer, flexions AS String(50,0 TO 1)) AS String
1540   REM TODO: add the respective type suffixes to your variable names if required 
1550   LET result = " " + copy(sentence, keyPos + length(key), length(sentence)) + " "
1560   FOR EACH pair IN flexions
1570     LET left = ""
1580     LET right = result
1590     LET pos0 = pos(pair(0), right)
1600     LET pos1 = pos(pair(1), right)
1610     DO WHILE pos0 > 0 OR pos1 > 0
1620       REM Detect which of the two words of the pair matches first (lest a substitution should be reverted) 
1630       LET which = 0
1640       LET position = pos0
1650       IF (pos0 = 0) OR ((pos1 > 0) AND (pos1 < pos0)) THEN
1660         LET which = 1
1670         LET position = pos1
1680       END IF
1690       LET left = left + copy(right, 1, position-1) + pair(1 - which)
1700       LET right = copy(right, position + length(pair(which)), length(right))
1710       LET pos0 = pos(pair(0), right)
1720       LET pos1 = pos(pair(1), right)
1730     LOOP
1740     LET result = left + right
1750   NEXT pair
1760   REM Eliminate multiple spaces (replaced by single ones) and vertical bars 
1770   DIM array364015e8() AS String = {"  ", "|"}
1780   FOR EACH str IN array364015e8
1790     LET position = pos(str, result)
1800     DO WHILE position > 0
1810       LET result = copy(result, 1, position-1) + copy(result, position+1, length(result))
1820       LET position = pos(str, result)
1830     LOOP
1840   NEXT str
1850   RETURN result
1860 END FUNCTION
1870 REM  
1880 REM Looks for the occurrence of the first of the strings 
1890 REM contained in keywords within the given sentence (in 
1900 REM array order). 
1910 REM Returns an array of 
1920 REM 0: the index of the first identified keyword (if any, otherwise -1), 
1930 REM 1: the position inside sentence (0 if not found) 
1940 REM TODO: Add type-specific suffixes where necessary! 
1950 FUNCTION findKeyword(CONST keyMap AS KeyMapEntry(50), sentence AS String) AS integer(0 TO 1)
1960   REM TODO: add the respective type suffixes to your variable names if required 
1970   REM Contains the index of the keyword and its position in sentence 
1980   REM TODO: Check indexBase value (automatically generated) 
1990   LET indexBase = 0
2000   LET result(indexBase + 0) = -1
2010   LET result(indexBase + 1) = 0
2020   LET i = 0
2030   DO WHILE (result(0) < 0) AND (i < length(keyMap))
2040     LET var entry: KeyMapEntry = keyMap(i)
2050     LET position = pos(entry.keyword, sentence)
2060     IF position > 0 THEN
2070       LET result(0) = i
2080       LET result(1) = position
2090     END IF
2100     LET i = i+1
2110   LOOP
2120   RETURN result
2130 END FUNCTION
2140 REM  
2150 REM Converts the sentence to lowercase, eliminates all 
2160 REM interpunction (i.e. ',', '.', ';'), and pads the 
2170 REM sentence among blanks 
2180 REM TODO: Add type-specific suffixes where necessary! 
2190 FUNCTION normalizeInput(sentence AS String) AS String
2200   REM TODO: add the respective type suffixes to your variable names if required 
2210   LET sentence = lowercase(sentence)
2220   REM TODO: Specify an appropriate element type for the array! 
2230   DIM arrayf8902ea6() AS FIXME_f8902ea6 = {'.', ',', ';', '!', '?'}
2240   FOR EACH symbol IN arrayf8902ea6
2250     LET position = pos(symbol, sentence)
2260     DO WHILE position > 0
2270       LET sentence = copy(sentence, 1, position-1) + copy(sentence, position+1, length(sentence))
2280       LET position = pos(symbol, sentence)
2290     LOOP
2300   NEXT symbol
2310   LET result = " " + sentence + " "
2320   RETURN result
2330 END FUNCTION
2340 REM  
2350 REM TODO: Add type-specific suffixes where necessary! 
2360 FUNCTION setupGoodByePhrases() AS String(50,0 TO 1)
2370   REM TODO: add the respective type suffixes to your variable names if required 
2380   REM TODO: Check indexBase value (automatically generated) 
2390   LET indexBase = 0
2400   LET phrases(0)(indexBase + 0) = " shut"
2410   LET phrases(0)(indexBase + 1) = "Okay. If you feel that way I\'ll shut up. ... Your choice."
2420   REM TODO: Check indexBase value (automatically generated) 
2430   LET indexBase = 0
2440   LET phrases(1)(indexBase + 0) = "bye"
2450   LET phrases(1)(indexBase + 1) = "Well, let\'s end our talk for now. See you later. Bye."
2460   return phrases
2470 END FUNCTION
2480 REM  
2490 REM The lower the index the higher the rank of the keyword (search is sequential). 
2500 REM The index of the first keyword found in a user sentence maps to a respective 
2510 REM reply ring as defined in `setupReplies()´. 
2520 REM TODO: Add type-specific suffixes where necessary! 
2530 FUNCTION setupKeywords() AS KeyMapEntry(50)
2540   REM TODO: add the respective type suffixes to your variable names if required 
2550   REM The empty key string (last entry) is the default clause - will always be found 
2560   LET keywords(39).keyword = ""
2570   LET keywords(39).index = 29
2580   LET keywords(0).keyword = "can you "
2590   LET keywords(0).index = 0
2600   LET keywords(1).keyword = "can i "
2610   LET keywords(1).index = 1
2620   LET keywords(2).keyword = "you are "
2630   LET keywords(2).index = 2
2640   LET keywords(3).keyword = "you\'re "
2650   LET keywords(3).index = 2
2660   LET keywords(4).keyword = "i don't "
2670   LET keywords(4).index = 3
2680   LET keywords(5).keyword = "i feel "
2690   LET keywords(5).index = 4
2700   LET keywords(6).keyword = "why don\'t you "
2710   LET keywords(6).index = 5
2720   LET keywords(7).keyword = "why can\'t i "
2730   LET keywords(7).index = 6
2740   LET keywords(8).keyword = "are you "
2750   LET keywords(8).index = 7
2760   LET keywords(9).keyword = "i can\'t "
2770   LET keywords(9).index = 8
2780   LET keywords(10).keyword = "i am "
2790   LET keywords(10).index = 9
2800   LET keywords(11).keyword = "i\'m "
2810   LET keywords(11).index = 9
2820   LET keywords(12).keyword = "you "
2830   LET keywords(12).index = 10
2840   LET keywords(13).keyword = "i want "
2850   LET keywords(13).index = 11
2860   LET keywords(14).keyword = "what "
2870   LET keywords(14).index = 12
2880   LET keywords(15).keyword = "how "
2890   LET keywords(15).index = 12
2900   LET keywords(16).keyword = "who "
2910   LET keywords(16).index = 12
2920   LET keywords(17).keyword = "where "
2930   LET keywords(17).index = 12
2940   LET keywords(18).keyword = "when "
2950   LET keywords(18).index = 12
2960   LET keywords(19).keyword = "why "
2970   LET keywords(19).index = 12
2980   LET keywords(20).keyword = "name "
2990   LET keywords(20).index = 13
3000   LET keywords(21).keyword = "cause "
3010   LET keywords(21).index = 14
3020   LET keywords(22).keyword = "sorry "
3030   LET keywords(22).index = 15
3040   LET keywords(23).keyword = "dream "
3050   LET keywords(23).index = 16
3060   LET keywords(24).keyword = "hello "
3070   LET keywords(24).index = 17
3080   LET keywords(25).keyword = "hi "
3090   LET keywords(25).index = 17
3100   LET keywords(26).keyword = "maybe "
3110   LET keywords(26).index = 18
3120   LET keywords(27).keyword = " no"
3130   LET keywords(27).index = 19
3140   LET keywords(28).keyword = "your "
3150   LET keywords(28).index = 20
3160   LET keywords(29).keyword = "always "
3170   LET keywords(29).index = 21
3180   LET keywords(30).keyword = "think "
3190   LET keywords(30).index = 22
3200   LET keywords(31).keyword = "alike "
3210   LET keywords(31).index = 23
3220   LET keywords(32).keyword = "yes "
3230   LET keywords(32).index = 24
3240   LET keywords(33).keyword = "friend "
3250   LET keywords(33).index = 25
3260   LET keywords(34).keyword = "computer"
3270   LET keywords(34).index = 26
3280   LET keywords(35).keyword = "bot "
3290   LET keywords(35).index = 26
3300   LET keywords(36).keyword = "smartphone"
3310   LET keywords(36).index = 27
3320   LET keywords(37).keyword = "father "
3330   LET keywords(37).index = 28
3340   LET keywords(38).keyword = "mother "
3350   LET keywords(38).index = 28
3360   return keywords
3370 END FUNCTION
3380 REM  
3390 REM Returns an array of pairs of mutually substitutable words 
3400 REM The second word may contain a '|' in order to prevent an inverse 
3410 REM replacement. 
3420 REM TODO: Add type-specific suffixes where necessary! 
3430 FUNCTION setupReflexions() AS String(50,0 TO 1)
3440   REM TODO: add the respective type suffixes to your variable names if required 
3450   REM TODO: Check indexBase value (automatically generated) 
3460   LET indexBase = 0
3470   LET reflexions(0)(indexBase + 0) = " are "
3480   LET reflexions(0)(indexBase + 1) = " am "
3490   REM This is not always helpful (e.g. if it relates to things or third persons) 
3500   REM TODO: Check indexBase value (automatically generated) 
3510   LET indexBase = 0
3520   LET reflexions(1)(indexBase + 0) = " were "
3530   LET reflexions(1)(indexBase + 1) = " was "
3540   REM TODO: Check indexBase value (automatically generated) 
3550   LET indexBase = 0
3560   LET reflexions(2)(indexBase + 0) = " you "
3570   LET reflexions(2)(indexBase + 1) = " i "
3580   REM TODO: Check indexBase value (automatically generated) 
3590   LET indexBase = 0
3600   LET reflexions(3)(indexBase + 0) = " yours "
3610   LET reflexions(3)(indexBase + 1) = " mine "
3620   REM TODO: Check indexBase value (automatically generated) 
3630   LET indexBase = 0
3640   LET reflexions(4)(indexBase + 0) = " yourself "
3650   LET reflexions(4)(indexBase + 1) = " myself "
3660   REM TODO: Check indexBase value (automatically generated) 
3670   LET indexBase = 0
3680   LET reflexions(5)(indexBase + 0) = " your "
3690   LET reflexions(5)(indexBase + 1) = " my "
3700   REM TODO: Check indexBase value (automatically generated) 
3710   LET indexBase = 0
3720   LET reflexions(6)(indexBase + 0) = " i\'ve "
3730   LET reflexions(6)(indexBase + 1) = " you\'ve "
3740   REM TODO: Check indexBase value (automatically generated) 
3750   LET indexBase = 0
3760   LET reflexions(7)(indexBase + 0) = " i\'m "
3770   LET reflexions(7)(indexBase + 1) = " you\'re "
3780   REM We must not replace "you" by "me", not in particular after "I" had been replaced by "you". 
3790   REM TODO: Check indexBase value (automatically generated) 
3800   LET indexBase = 0
3810   LET reflexions(8)(indexBase + 0) = " me "
3820   LET reflexions(8)(indexBase + 1) = " |you "
3830   return reflexions
3840 END FUNCTION
3850 REM  
3860 REM This routine sets up the reply rings addressed by the key words defined in 
3870 REM routine `setupKeywords()´ and mapped hitherto by the cross table defined 
3880 REM in `setupMapping()´ 
3890 REM TODO: Add type-specific suffixes where necessary! 
3900 FUNCTION setupReplies() AS String(50,50)
3910   REM TODO: add the respective type suffixes to your variable names if required 
3920   REM We start with the highest index for performance reasons 
3930   REM (is to avoid frequent array resizing) 
3940   REM TODO: Check indexBase value (automatically generated) 
3950   LET indexBase = 0
3960   LET replies(29)(indexBase + 0) = "Say, do you have any psychological problems?"
3970   LET replies(29)(indexBase + 1) = "What does that suggest to you?"
3980   LET replies(29)(indexBase + 2) = "I see."
3990   LET replies(29)(indexBase + 3) = "I'm not sure I understand you fully."
4000   LET replies(29)(indexBase + 4) = "Come come elucidate your thoughts."
4010   LET replies(29)(indexBase + 5) = "Can you elaborate on that?"
4020   LET replies(29)(indexBase + 6) = "That is quite interesting."
4030   REM TODO: Check indexBase value (automatically generated) 
4040   LET indexBase = 0
4050   LET replies(0)(indexBase + 0) = "Don't you believe that I can*?"
4060   LET replies(0)(indexBase + 1) = "Perhaps you would like to be like me?"
4070   LET replies(0)(indexBase + 2) = "You want me to be able to*?"
4080   REM TODO: Check indexBase value (automatically generated) 
4090   LET indexBase = 0
4100   LET replies(1)(indexBase + 0) = "Perhaps you don't want to*?"
4110   LET replies(1)(indexBase + 1) = "Do you want to be able to*?"
4120   REM TODO: Check indexBase value (automatically generated) 
4130   LET indexBase = 0
4140   LET replies(2)(indexBase + 0) = "What makes you think I am*?"
4150   LET replies(2)(indexBase + 1) = "Does it please you to believe I am*?"
4160   LET replies(2)(indexBase + 2) = "Perhaps you would like to be*?"
4170   LET replies(2)(indexBase + 3) = "Do you sometimes wish you were*?"
4180   REM TODO: Check indexBase value (automatically generated) 
4190   LET indexBase = 0
4200   LET replies(3)(indexBase + 0) = "Don't you really*?"
4210   LET replies(3)(indexBase + 1) = "Why don't you*?"
4220   LET replies(3)(indexBase + 2) = "Do you wish to be able to*?"
4230   LET replies(3)(indexBase + 3) = "Does that trouble you*?"
4240   REM TODO: Check indexBase value (automatically generated) 
4250   LET indexBase = 0
4260   LET replies(4)(indexBase + 0) = "Do you often feel*?"
4270   LET replies(4)(indexBase + 1) = "Are you afraid of feeling*?"
4280   LET replies(4)(indexBase + 2) = "Do you enjoy feeling*?"
4290   REM TODO: Check indexBase value (automatically generated) 
4300   LET indexBase = 0
4310   LET replies(5)(indexBase + 0) = "Do you really believe I don't*?"
4320   LET replies(5)(indexBase + 1) = "Perhaps in good time I will*."
4330   LET replies(5)(indexBase + 2) = "Do you want me to*?"
4340   REM TODO: Check indexBase value (automatically generated) 
4350   LET indexBase = 0
4360   LET replies(6)(indexBase + 0) = "Do you think you should be able to*?"
4370   LET replies(6)(indexBase + 1) = "Why can't you*?"
4380   REM TODO: Check indexBase value (automatically generated) 
4390   LET indexBase = 0
4400   LET replies(7)(indexBase + 0) = "Why are you interested in whether or not I am*?"
4410   LET replies(7)(indexBase + 1) = "Would you prefer if I were not*?"
4420   LET replies(7)(indexBase + 2) = "Perhaps in your fantasies I am*?"
4430   REM TODO: Check indexBase value (automatically generated) 
4440   LET indexBase = 0
4450   LET replies(8)(indexBase + 0) = "How do you know you can't*?"
4460   LET replies(8)(indexBase + 1) = "Have you tried?"
4470   LET replies(8)(indexBase + 2) = "Perhaps you can now*."
4480   REM TODO: Check indexBase value (automatically generated) 
4490   LET indexBase = 0
4500   LET replies(9)(indexBase + 0) = "Did you come to me because you are*?"
4510   LET replies(9)(indexBase + 1) = "How long have you been*?"
4520   LET replies(9)(indexBase + 2) = "Do you believe it is normal to be*?"
4530   LET replies(9)(indexBase + 3) = "Do you enjoy being*?"
4540   REM TODO: Check indexBase value (automatically generated) 
4550   LET indexBase = 0
4560   LET replies(10)(indexBase + 0) = "We were discussing you--not me."
4570   LET replies(10)(indexBase + 1) = "Oh, I*."
4580   LET replies(10)(indexBase + 2) = "You're not really talking about me, are you?"
4590   REM TODO: Check indexBase value (automatically generated) 
4600   LET indexBase = 0
4610   LET replies(11)(indexBase + 0) = "What would it mean to you if you got*?"
4620   LET replies(11)(indexBase + 1) = "Why do you want*?"
4630   LET replies(11)(indexBase + 2) = "Suppose you soon got*..."
4640   LET replies(11)(indexBase + 3) = "What if you never got*?"
4650   LET replies(11)(indexBase + 4) = "I sometimes also want*."
4660   REM TODO: Check indexBase value (automatically generated) 
4670   LET indexBase = 0
4680   LET replies(12)(indexBase + 0) = "Why do you ask?"
4690   LET replies(12)(indexBase + 1) = "Does that question interest you?"
4700   LET replies(12)(indexBase + 2) = "What answer would please you the most?"
4710   LET replies(12)(indexBase + 3) = "What do you think?"
4720   LET replies(12)(indexBase + 4) = "Are such questions on your mind often?"
4730   LET replies(12)(indexBase + 5) = "What is it that you really want to know?"
4740   LET replies(12)(indexBase + 6) = "Have you asked anyone else?"
4750   LET replies(12)(indexBase + 7) = "Have you asked such questions before?"
4760   LET replies(12)(indexBase + 8) = "What else comes to mind when you ask that?"
4770   REM TODO: Check indexBase value (automatically generated) 
4780   LET indexBase = 0
4790   LET replies(13)(indexBase + 0) = "Names don't interest me."
4800   LET replies(13)(indexBase + 1) = "I don't care about names -- please go on."
4810   REM TODO: Check indexBase value (automatically generated) 
4820   LET indexBase = 0
4830   LET replies(14)(indexBase + 0) = "Is that the real reason?"
4840   LET replies(14)(indexBase + 1) = "Don't any other reasons come to mind?"
4850   LET replies(14)(indexBase + 2) = "Does that reason explain anything else?"
4860   LET replies(14)(indexBase + 3) = "What other reasons might there be?"
4870   REM TODO: Check indexBase value (automatically generated) 
4880   LET indexBase = 0
4890   LET replies(15)(indexBase + 0) = "Please don't apologize!"
4900   LET replies(15)(indexBase + 1) = "Apologies are not necessary."
4910   LET replies(15)(indexBase + 2) = "What feelings do you have when you apologize?"
4920   LET replies(15)(indexBase + 3) = "Don't be so defensive!"
4930   REM TODO: Check indexBase value (automatically generated) 
4940   LET indexBase = 0
4950   LET replies(16)(indexBase + 0) = "What does that dream suggest to you?"
4960   LET replies(16)(indexBase + 1) = "Do you dream often?"
4970   LET replies(16)(indexBase + 2) = "What persons appear in your dreams?"
4980   LET replies(16)(indexBase + 3) = "Are you disturbed by your dreams?"
4990   REM TODO: Check indexBase value (automatically generated) 
5000   LET indexBase = 0
5010   LET replies(17)(indexBase + 0) = "How do you do ...please state your problem."
5020   REM TODO: Check indexBase value (automatically generated) 
5030   LET indexBase = 0
5040   LET replies(18)(indexBase + 0) = "You don't seem quite certain."
5050   LET replies(18)(indexBase + 1) = "Why the uncertain tone?"
5060   LET replies(18)(indexBase + 2) = "Can't you be more positive?"
5070   LET replies(18)(indexBase + 3) = "You aren't sure?"
5080   LET replies(18)(indexBase + 4) = "Don't you know?"
5090   REM TODO: Check indexBase value (automatically generated) 
5100   LET indexBase = 0
5110   LET replies(19)(indexBase + 0) = "Are you saying no just to be negative?"
5120   LET replies(19)(indexBase + 1) = "You are being a bit negative."
5130   LET replies(19)(indexBase + 2) = "Why not?"
5140   LET replies(19)(indexBase + 3) = "Are you sure?"
5150   LET replies(19)(indexBase + 4) = "Why no?"
5160   REM TODO: Check indexBase value (automatically generated) 
5170   LET indexBase = 0
5180   LET replies(20)(indexBase + 0) = "Why are you concerned about my*?"
5190   LET replies(20)(indexBase + 1) = "What about your own*?"
5200   REM TODO: Check indexBase value (automatically generated) 
5210   LET indexBase = 0
5220   LET replies(21)(indexBase + 0) = "Can you think of a specific example?"
5230   LET replies(21)(indexBase + 1) = "When?"
5240   LET replies(21)(indexBase + 2) = "What are you thinking of?"
5250   LET replies(21)(indexBase + 3) = "Really, always?"
5260   REM TODO: Check indexBase value (automatically generated) 
5270   LET indexBase = 0
5280   LET replies(22)(indexBase + 0) = "Do you really think so?"
5290   LET replies(22)(indexBase + 1) = "But you are not sure you*?"
5300   LET replies(22)(indexBase + 2) = "Do you doubt you*?"
5310   REM TODO: Check indexBase value (automatically generated) 
5320   LET indexBase = 0
5330   LET replies(23)(indexBase + 0) = "In what way?"
5340   LET replies(23)(indexBase + 1) = "What resemblance do you see?"
5350   LET replies(23)(indexBase + 2) = "What does the similarity suggest to you?"
5360   LET replies(23)(indexBase + 3) = "What other connections do you see?"
5370   LET replies(23)(indexBase + 4) = "Could there really be some connection?"
5380   LET replies(23)(indexBase + 5) = "How?"
5390   LET replies(23)(indexBase + 6) = "You seem quite positive."
5400   REM TODO: Check indexBase value (automatically generated) 
5410   LET indexBase = 0
5420   LET replies(24)(indexBase + 0) = "Are you sure?"
5430   LET replies(24)(indexBase + 1) = "I see."
5440   LET replies(24)(indexBase + 2) = "I understand."
5450   REM TODO: Check indexBase value (automatically generated) 
5460   LET indexBase = 0
5470   LET replies(25)(indexBase + 0) = "Why do you bring up the topic of friends?"
5480   LET replies(25)(indexBase + 1) = "Do your friends worry you?"
5490   LET replies(25)(indexBase + 2) = "Do your friends pick on you?"
5500   LET replies(25)(indexBase + 3) = "Are you sure you have any friends?"
5510   LET replies(25)(indexBase + 4) = "Do you impose on your friends?"
5520   LET replies(25)(indexBase + 5) = "Perhaps your love for friends worries you."
5530   REM TODO: Check indexBase value (automatically generated) 
5540   LET indexBase = 0
5550   LET replies(26)(indexBase + 0) = "Do computers worry you?"
5560   LET replies(26)(indexBase + 1) = "Are you talking about me in particular?"
5570   LET replies(26)(indexBase + 2) = "Are you frightened by machines?"
5580   LET replies(26)(indexBase + 3) = "Why do you mention computers?"
5590   LET replies(26)(indexBase + 4) = "What do you think machines have to do with your problem?"
5600   LET replies(26)(indexBase + 5) = "Don't you think computers can help people?"
5610   LET replies(26)(indexBase + 6) = "What is it about machines that worries you?"
5620   REM TODO: Check indexBase value (automatically generated) 
5630   LET indexBase = 0
5640   LET replies(27)(indexBase + 0) = "Do you sometimes feel uneasy without a smartphone?"
5650   LET replies(27)(indexBase + 1) = "Have you had these phantasies before?"
5660   LET replies(27)(indexBase + 2) = "Does the world seem more real for you via apps?"
5670   REM TODO: Check indexBase value (automatically generated) 
5680   LET indexBase = 0
5690   LET replies(28)(indexBase + 0) = "Tell me more about your family."
5700   LET replies(28)(indexBase + 1) = "Who else in your family*?"
5710   LET replies(28)(indexBase + 2) = "What does family relations mean for you?"
5720   LET replies(28)(indexBase + 3) = "Come on, How old are you?"
5730   LET setupReplies = replies
5740   RETURN setupReplies
5750 END FUNCTION

REM = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
