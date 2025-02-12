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
110 REM Generated by Structorizer 3.32-26 
120 
130 REM Copyright (C) 2018-05-14 ??? 
140 REM License: GPLv3-link 
150 REM GNU General Public License (V 3) 
160 REM https://www.gnu.org/licenses/gpl.html 
170 REM http://www.gnu.de/documents/gpl.de.html 
180 
190 REM  
200 REM program ELIZA
210 REM TODO: add the respective type suffixes to your variable names if required 
220 
230 TYPE KeyMapEntry
240   keyword AS String
250   index AS Integer
260 END TYPE
270 
280 DIM replies(,) AS String
290 DIM reflexions(,1) AS String
300 DIM byePhrases(,1) AS String
310 DIM keyMap() AS KeyMapEntry
320 DIM offsets() AS Integer
330 DIM history(5) AS ???
340 DIM findInfo(1) AS integer
350 DIM entry AS KeyMapEntry
360 REM  
370 REM Title information 
380 PRINT "************* ELIZA **************"
390 PRINT "* Original design by J. Weizenbaum"
400 PRINT "**********************************"
410 PRINT "* Adapted for Basic on IBM PC by"
420 PRINT "* - Patricia Danielson"
430 PRINT "* - Paul Hashfield"
440 PRINT "**********************************"
450 PRINT "* Adapted for Structorizer by"
460 PRINT "* - Kay Gürtzig / FH Erfurt 2016"
470 PRINT "* Version: 2.2 (2019-03-28)"
480 PRINT "**********************************"
490 REM Stores the last five inputs of the user in a ring buffer, 
500 REM the first element is the current insertion index 
510 LET history(0) = 0
520 LET history(1) = ""
530 LET history(2) = ""
540 LET history(3) = ""
550 LET history(4) = ""
560 LET history(5) = ""
570 LET replies = setupReplies()
580 LET reflexions = setupReflexions()
590 LET byePhrases = setupGoodByePhrases()
600 LET keyMap = setupKeywords()
610 LET offsets(length(keyMap)-1) = 0
620 LET isGone = false
630 REM Starter 
640 PRINT "Hi! I\'m your new therapist. My name is Eliza. What\'s your problem?"
650 DO
660   INPUT userInput
670   REM Converts the input to lowercase, cuts out interpunctation 
680   REM and pads the string 
690   LET userInput = normalizeInput(userInput)
700   LET isGone = checkGoodBye(userInput, byePhrases)
710   IF NOT isGone THEN
720     LET reply = "Please don\'t repeat yourself!"
730     LET isRepeated = checkRepetition(history, userInput)
740     IF NOT isRepeated THEN
750       LET findInfo = findKeyword(keyMap, userInput)
760       LET keyIndex = findInfo(0)
770       IF keyIndex < 0 THEN
780         REM Should never happen... 
790         LET keyIndex = length(keyMap)-1
800       END IF
810       LET entry = keyMap(keyIndex)
820       REM Variable part of the reply 
830       LET varPart = ""
840       IF length(entry.keyword) > 0 THEN
850         LET varPart = conjugateStrings(userInput, entry.keyword, findInfo(1), reflexions)
860       END IF
870       LET replyRing = replies(entry.index)
880       LET reply = replyRing(offsets(keyIndex))
890       LET offsets(keyIndex) = (offsets(keyIndex) + 1) % length(replyRing)
900       LET posAster = pos("*", reply)
910       IF posAster > 0 THEN
920         IF varPart = " " THEN
930           LET reply = "You will have to elaborate more for me to help you."
940         ELSE
950           delete(reply, posAster, 1)
960           insert(varPart, reply, posAster)
970         END IF
980       END IF
990       LET reply = adjustSpelling(reply)
1000     END IF
1010     PRINT reply
1020   END IF
1030 LOOP UNTIL isGone
1040 END
1050 REM  
1060 REM Cares for correct letter case among others 
1070 REM TODO: Add type-specific suffixes where necessary! 
1080 FUNCTION adjustSpelling(sentence AS String) AS String
1090   REM TODO: add the respective type suffixes to your variable names if required 
1100   REM  
1110   REM  
1120   LET result = sentence
1130   LET position = 1
1140   DO WHILE (position <= length(sentence)) AND (copy(sentence, position, 1) = " ")
1150     LET position = position + 1
1160   LOOP
1170   IF position <= length(sentence) THEN
1180     LET start = copy(sentence, 1, position)
1190     delete(result, 1, position)
1200     insert(uppercase(start), result, 1)
1210   END IF
1220   DIM array2de1525b() AS String = {" i ", " i\'"}
1230   FOR EACH word IN array2de1525b
1240     LET position = pos(word, result)
1250     DO WHILE position > 0
1260       delete(result, position+1, 1)
1270       insert("I", result, position+1)
1280       LET position = pos(word, result)
1290     LOOP
1300   NEXT word
1310   RETURN result
1320 END FUNCTION
1330 REM  
1340 REM Checks whether the given text contains some kind of 
1350 REM good-bye phrase inducing the end of the conversation 
1360 REM and if so writes a correspding good-bye message and 
1370 REM returns true, otherwise false 
1380 REM TODO: Add type-specific suffixes where necessary! 
1390 FUNCTION checkGoodBye(text AS String, phrases AS String(50,0 TO 1)) AS boolean
1400   REM TODO: add the respective type suffixes to your variable names if required 
1410   REM  
1420   REM  
1430   FOR EACH pair IN phrases
1440     IF pos(pair(0), text) > 0 THEN
1450       LET saidBye = true
1460       PRINT pair(1)
1470       RETURN true
1480     END IF
1490   NEXT pair
1500   return false
1510 END FUNCTION
1520 REM  
1530 REM Checks whether newInput has occurred among the last 
1540 REM length(history) - 1 input strings and updates the history 
1550 REM TODO: Add type-specific suffixes where necessary! 
1560 FUNCTION checkRepetition(history AS array, newInput AS String) AS boolean
1570   REM TODO: add the respective type suffixes to your variable names if required 
1580   REM  
1590   REM  
1600   LET hasOccurred = false
1610   IF length(newInput) > 4 THEN
1620     LET currentIndex = history(0);
1630     FOR i = 1 TO length(history)-1
1640       IF newInput = history(i) THEN
1650         LET hasOccurred = true
1660       END IF
1670     NEXT i
1680     LET history(history(0)+1) = newInput
1690     LET history(0) = (history(0) + 1) % (length(history) - 1)
1700   END IF
1710   return hasOccurred
1720 END FUNCTION
1730 REM  
1740 REM TODO: Add type-specific suffixes where necessary! 
1750 FUNCTION conjugateStrings(sentence AS String, key AS String, keyPos AS integer, flexions AS String(50,0 TO 1)) AS String
1760   REM TODO: add the respective type suffixes to your variable names if required 
1770   REM  
1780   REM  
1790   LET result = " " + copy(sentence, keyPos + length(key), length(sentence)) + " "
1800   FOR EACH pair IN flexions
1810     LET left = ""
1820     LET right = result
1830     LET position = pos(pair(0), right)
1840     DO WHILE position > 0
1850       LET left = left + copy(right, 1, position-1) + pair(1)
1860       LET right = copy(right, position + length(pair(0)), length(right))
1870       LET position = pos(pair(0), right)
1880     LOOP
1890     LET result = left + right
1900   NEXT pair
1910   REM Eliminate multiple spaces 
1920   LET position = pos("  ", result)
1930   DO WHILE position > 0
1940     LET result = copy(result, 1, position-1) + copy(result, position+1, length(result))
1950     LET position = pos("  ", result)
1960   LOOP
1970   RETURN result
1980 END FUNCTION
1990 REM  
2000 REM Looks for the occurrence of the first of the strings 
2010 REM contained in keywords within the given sentence (in 
2020 REM array order). 
2030 REM Returns an array of 
2040 REM 0: the index of the first identified keyword (if any, otherwise -1), 
2050 REM 1: the position inside sentence (0 if not found) 
2060 REM TODO: Add type-specific suffixes where necessary! 
2070 FUNCTION findKeyword(CONST keyMap AS KeyMapEntry(50), sentence AS String) AS integer(0 TO 1)
2080   REM TODO: add the respective type suffixes to your variable names if required 
2090   REM  
2100   DIM result(1) AS Integer
2110   DIM entry AS KeyMapEntry
2120   REM  
2130   REM Contains the index of the keyword and its position in sentence 
2140   LET result(0) = -1
2150   LET result(1) = 0
2160   LET i = 0
2170   DO WHILE (result(0) < 0) AND (i < length(keyMap))
2180     LET entry = keyMap(i)
2190     LET position = pos(entry.keyword, sentence)
2200     IF position > 0 THEN
2210       LET result(0) = i
2220       LET result(1) = position
2230     END IF
2240     LET i = i+1
2250   LOOP
2260   RETURN result
2270 END FUNCTION
2280 REM  
2290 REM Converts the sentence to lowercase, eliminates all 
2300 REM interpunction (i.e. ',', '.', ';'), and pads the 
2310 REM sentence among blanks 
2320 REM TODO: Add type-specific suffixes where necessary! 
2330 FUNCTION normalizeInput(sentence AS String) AS String
2340   REM TODO: add the respective type suffixes to your variable names if required 
2350   REM  
2360   REM  
2370   LET sentence = lowercase(sentence)
2380   REM TODO: Specify an appropriate element type for the array! 
2390   DIM array65006eba() AS FIXME_65006eba = {'.', ',', ';', '!', '?'}
2400   FOR EACH symbol IN array65006eba
2410     LET position = pos(symbol, sentence)
2420     DO WHILE position > 0
2430       LET sentence = copy(sentence, 1, position-1) + copy(sentence, position+1, length(sentence))
2440       LET position = pos(symbol, sentence)
2450     LOOP
2460   NEXT symbol
2470   LET result = " " + sentence + " "
2480   RETURN result
2490 END FUNCTION
2500 REM  
2510 REM TODO: Add type-specific suffixes where necessary! 
2520 FUNCTION setupGoodByePhrases() AS String(50,0 TO 1)
2530   REM TODO: add the respective type suffixes to your variable names if required 
2540   REM  
2550   DIM phrases(,1) AS String
2560   REM  
2570   LET phrases(0)(0) = " shut"
2580   LET phrases(0)(1) = "Okay. If you feel that way I\'ll shut up. ... Your choice."
2590   LET phrases(1)(0) = "bye"
2600   LET phrases(1)(1) = "Well, let\'s end our talk for now. See you later. Bye."
2610   return phrases
2620 END FUNCTION
2630 REM  
2640 REM The lower the index the higher the rank of the keyword (search is sequential). 
2650 REM The index of the first keyword found in a user sentence maps to a respective 
2660 REM reply ring as defined in `setupReplies()´. 
2670 REM TODO: Add type-specific suffixes where necessary! 
2680 FUNCTION setupKeywords() AS KeyMapEntry(50)
2690   REM TODO: add the respective type suffixes to your variable names if required 
2700   REM  
2710   DIM keywords() AS KeyMapEntry
2720   REM  
2730   REM The empty key string (last entry) is the default clause - will always be found 
2740   LET keywords(39).keyword = ""
2750   LET keywords(39).index = 29
2760   LET keywords(0).keyword = "can you "
2770   LET keywords(0).index = 0
2780   LET keywords(1).keyword = "can i "
2790   LET keywords(1).index = 1
2800   LET keywords(2).keyword = "you are "
2810   LET keywords(2).index = 2
2820   LET keywords(3).keyword = "you\'re "
2830   LET keywords(3).index = 2
2840   LET keywords(4).keyword = "i don't "
2850   LET keywords(4).index = 3
2860   LET keywords(5).keyword = "i feel "
2870   LET keywords(5).index = 4
2880   LET keywords(6).keyword = "why don\'t you "
2890   LET keywords(6).index = 5
2900   LET keywords(7).keyword = "why can\'t i "
2910   LET keywords(7).index = 6
2920   LET keywords(8).keyword = "are you "
2930   LET keywords(8).index = 7
2940   LET keywords(9).keyword = "i can\'t "
2950   LET keywords(9).index = 8
2960   LET keywords(10).keyword = "i am "
2970   LET keywords(10).index = 9
2980   LET keywords(11).keyword = "i\'m "
2990   LET keywords(11).index = 9
3000   LET keywords(12).keyword = "you "
3010   LET keywords(12).index = 10
3020   LET keywords(13).keyword = "i want "
3030   LET keywords(13).index = 11
3040   LET keywords(14).keyword = "what "
3050   LET keywords(14).index = 12
3060   LET keywords(15).keyword = "how "
3070   LET keywords(15).index = 12
3080   LET keywords(16).keyword = "who "
3090   LET keywords(16).index = 12
3100   LET keywords(17).keyword = "where "
3110   LET keywords(17).index = 12
3120   LET keywords(18).keyword = "when "
3130   LET keywords(18).index = 12
3140   LET keywords(19).keyword = "why "
3150   LET keywords(19).index = 12
3160   LET keywords(20).keyword = "name "
3170   LET keywords(20).index = 13
3180   LET keywords(21).keyword = "cause "
3190   LET keywords(21).index = 14
3200   LET keywords(22).keyword = "sorry "
3210   LET keywords(22).index = 15
3220   LET keywords(23).keyword = "dream "
3230   LET keywords(23).index = 16
3240   LET keywords(24).keyword = "hello "
3250   LET keywords(24).index = 17
3260   LET keywords(25).keyword = "hi "
3270   LET keywords(25).index = 17
3280   LET keywords(26).keyword = "maybe "
3290   LET keywords(26).index = 18
3300   LET keywords(27).keyword = " no"
3310   LET keywords(27).index = 19
3320   LET keywords(28).keyword = "your "
3330   LET keywords(28).index = 20
3340   LET keywords(29).keyword = "always "
3350   LET keywords(29).index = 21
3360   LET keywords(30).keyword = "think "
3370   LET keywords(30).index = 22
3380   LET keywords(31).keyword = "alike "
3390   LET keywords(31).index = 23
3400   LET keywords(32).keyword = "yes "
3410   LET keywords(32).index = 24
3420   LET keywords(33).keyword = "friend "
3430   LET keywords(33).index = 25
3440   LET keywords(34).keyword = "computer"
3450   LET keywords(34).index = 26
3460   LET keywords(35).keyword = "bot "
3470   LET keywords(35).index = 26
3480   LET keywords(36).keyword = "smartphone"
3490   LET keywords(36).index = 27
3500   LET keywords(37).keyword = "father "
3510   LET keywords(37).index = 28
3520   LET keywords(38).keyword = "mother "
3530   LET keywords(38).index = 28
3540   return keywords
3550 END FUNCTION
3560 REM  
3570 REM Returns an array of pairs of mutualy substitutable  
3580 REM TODO: Add type-specific suffixes where necessary! 
3590 FUNCTION setupReflexions() AS String(50,0 TO 1)
3600   REM TODO: add the respective type suffixes to your variable names if required 
3610   REM  
3620   DIM reflexions(,1) AS String
3630   REM  
3640   LET reflexions(0)(0) = " are "
3650   LET reflexions(0)(1) = " am "
3660   LET reflexions(1)(0) = " were "
3670   LET reflexions(1)(1) = " was "
3680   LET reflexions(2)(0) = " you "
3690   LET reflexions(2)(1) = " I "
3700   LET reflexions(3)(0) = " your"
3710   LET reflexions(3)(1) = " my"
3720   LET reflexions(4)(0) = " i\'ve "
3730   LET reflexions(4)(1) = " you\'ve "
3740   LET reflexions(5)(0) = " i\'m "
3750   LET reflexions(5)(1) = " you\'re "
3760   LET reflexions(6)(0) = " me "
3770   LET reflexions(6)(1) = " you "
3780   LET reflexions(7)(0) = " my "
3790   LET reflexions(7)(1) = " your "
3800   LET reflexions(8)(0) = " i "
3810   LET reflexions(8)(1) = " you "
3820   LET reflexions(9)(0) = " am "
3830   LET reflexions(9)(1) = " are "
3840   return reflexions
3850 END FUNCTION
3860 REM  
3870 REM This routine sets up the reply rings addressed by the key words defined in 
3880 REM routine `setupKeywords()´ and mapped hitherto by the cross table defined 
3890 REM in `setupMapping()´ 
3900 REM TODO: Add type-specific suffixes where necessary! 
3910 FUNCTION setupReplies() AS String(50,50)
3920   REM TODO: add the respective type suffixes to your variable names if required 
3930   REM  
3940   DIM setupReplies(,) AS String
3950   DIM replies(,) AS String
3960   REM  
3970   REM We start with the highest index for performance reasons 
3980   REM (is to avoid frequent array resizing) 
3990   LET replies(29)(0) = "Say, do you have any psychological problems?"
4000   LET replies(29)(1) = "What does that suggest to you?"
4010   LET replies(29)(2) = "I see."
4020   LET replies(29)(3) = "I'm not sure I understand you fully."
4030   LET replies(29)(4) = "Come come elucidate your thoughts."
4040   LET replies(29)(5) = "Can you elaborate on that?"
4050   LET replies(29)(6) = "That is quite interesting."
4060   LET replies(0)(0) = "Don't you believe that I can*?"
4070   LET replies(0)(1) = "Perhaps you would like to be like me?"
4080   LET replies(0)(2) = "You want me to be able to*?"
4090   LET replies(1)(0) = "Perhaps you don't want to*?"
4100   LET replies(1)(1) = "Do you want to be able to*?"
4110   LET replies(2)(0) = "What makes you think I am*?"
4120   LET replies(2)(1) = "Does it please you to believe I am*?"
4130   LET replies(2)(2) = "Perhaps you would like to be*?"
4140   LET replies(2)(3) = "Do you sometimes wish you were*?"
4150   LET replies(3)(0) = "Don't you really*?"
4160   LET replies(3)(1) = "Why don't you*?"
4170   LET replies(3)(2) = "Do you wish to be able to*?"
4180   LET replies(3)(3) = "Does that trouble you*?"
4190   LET replies(4)(0) = "Do you often feel*?"
4200   LET replies(4)(1) = "Are you afraid of feeling*?"
4210   LET replies(4)(2) = "Do you enjoy feeling*?"
4220   LET replies(5)(0) = "Do you really believe I don't*?"
4230   LET replies(5)(1) = "Perhaps in good time I will*."
4240   LET replies(5)(2) = "Do you want me to*?"
4250   LET replies(6)(0) = "Do you think you should be able to*?"
4260   LET replies(6)(1) = "Why can't you*?"
4270   LET replies(7)(0) = "Why are you interested in whether or not I am*?"
4280   LET replies(7)(1) = "Would you prefer if I were not*?"
4290   LET replies(7)(2) = "Perhaps in your fantasies I am*?"
4300   LET replies(8)(0) = "How do you know you can't*?"
4310   LET replies(8)(1) = "Have you tried?"
4320   LET replies(8)(2) = "Perhaps you can now*."
4330   LET replies(9)(0) = "Did you come to me because you are*?"
4340   LET replies(9)(1) = "How long have you been*?"
4350   LET replies(9)(2) = "Do you believe it is normal to be*?"
4360   LET replies(9)(3) = "Do you enjoy being*?"
4370   LET replies(10)(0) = "We were discussing you--not me."
4380   LET replies(10)(1) = "Oh, I*."
4390   LET replies(10)(2) = "You're not really talking about me, are you?"
4400   LET replies(11)(0) = "What would it mean to you if you got*?"
4410   LET replies(11)(1) = "Why do you want*?"
4420   LET replies(11)(2) = "Suppose you soon got*..."
4430   LET replies(11)(3) = "What if you never got*?"
4440   LET replies(11)(4) = "I sometimes also want*."
4450   LET replies(12)(0) = "Why do you ask?"
4460   LET replies(12)(1) = "Does that question interest you?"
4470   LET replies(12)(2) = "What answer would please you the most?"
4480   LET replies(12)(3) = "What do you think?"
4490   LET replies(12)(4) = "Are such questions on your mind often?"
4500   LET replies(12)(5) = "What is it that you really want to know?"
4510   LET replies(12)(6) = "Have you asked anyone else?"
4520   LET replies(12)(7) = "Have you asked such questions before?"
4530   LET replies(12)(8) = "What else comes to mind when you ask that?"
4540   LET replies(13)(0) = "Names don't interest me."
4550   LET replies(13)(1) = "I don't care about names -- please go on."
4560   LET replies(14)(0) = "Is that the real reason?"
4570   LET replies(14)(1) = "Don't any other reasons come to mind?"
4580   LET replies(14)(2) = "Does that reason explain anything else?"
4590   LET replies(14)(3) = "What other reasons might there be?"
4600   LET replies(15)(0) = "Please don't apologize!"
4610   LET replies(15)(1) = "Apologies are not necessary."
4620   LET replies(15)(2) = "What feelings do you have when you apologize?"
4630   LET replies(15)(3) = "Don't be so defensive!"
4640   LET replies(16)(0) = "What does that dream suggest to you?"
4650   LET replies(16)(1) = "Do you dream often?"
4660   LET replies(16)(2) = "What persons appear in your dreams?"
4670   LET replies(16)(3) = "Are you disturbed by your dreams?"
4680   LET replies(17)(0) = "How do you do ...please state your problem."
4690   LET replies(18)(0) = "You don't seem quite certain."
4700   LET replies(18)(1) = "Why the uncertain tone?"
4710   LET replies(18)(2) = "Can't you be more positive?"
4720   LET replies(18)(3) = "You aren't sure?"
4730   LET replies(18)(4) = "Don't you know?"
4740   LET replies(19)(0) = "Are you saying no just to be negative?"
4750   LET replies(19)(1) = "You are being a bit negative."
4760   LET replies(19)(2) = "Why not?"
4770   LET replies(19)(3) = "Are you sure?"
4780   LET replies(19)(4) = "Why no?"
4790   LET replies(20)(0) = "Why are you concerned about my*?"
4800   LET replies(20)(1) = "What about your own*?"
4810   LET replies(21)(0) = "Can you think of a specific example?"
4820   LET replies(21)(1) = "When?"
4830   LET replies(21)(2) = "What are you thinking of?"
4840   LET replies(21)(3) = "Really, always?"
4850   LET replies(22)(0) = "Do you really think so?"
4860   LET replies(22)(1) = "But you are not sure you*?"
4870   LET replies(22)(2) = "Do you doubt you*?"
4880   LET replies(23)(0) = "In what way?"
4890   LET replies(23)(1) = "What resemblance do you see?"
4900   LET replies(23)(2) = "What does the similarity suggest to you?"
4910   LET replies(23)(3) = "What other connections do you see?"
4920   LET replies(23)(4) = "Could there really be some connection?"
4930   LET replies(23)(5) = "How?"
4940   LET replies(23)(6) = "You seem quite positive."
4950   LET replies(24)(0) = "Are you sure?"
4960   LET replies(24)(1) = "I see."
4970   LET replies(24)(2) = "I understand."
4980   LET replies(25)(0) = "Why do you bring up the topic of friends?"
4990   LET replies(25)(1) = "Do your friends worry you?"
5000   LET replies(25)(2) = "Do your friends pick on you?"
5010   LET replies(25)(3) = "Are you sure you have any friends?"
5020   LET replies(25)(4) = "Do you impose on your friends?"
5030   LET replies(25)(5) = "Perhaps your love for friends worries you."
5040   LET replies(26)(0) = "Do computers worry you?"
5050   LET replies(26)(1) = "Are you talking about me in particular?"
5060   LET replies(26)(2) = "Are you frightened by machines?"
5070   LET replies(26)(3) = "Why do you mention computers?"
5080   LET replies(26)(4) = "What do you think machines have to do with your problem?"
5090   LET replies(26)(5) = "Don't you think computers can help people?"
5100   LET replies(26)(6) = "What is it about machines that worries you?"
5110   LET replies(27)(0) = "Do you sometimes feel uneasy without a smartphone?"
5120   LET replies(27)(1) = "Have you had these phantasies before?"
5130   LET replies(27)(2) = "Does the world seem more real for you via apps?"
5140   LET replies(28)(0) = "Tell me more about your family."
5150   LET replies(28)(1) = "Who else in your family*?"
5160   LET replies(28)(2) = "What does family relations mean for you?"
5170   LET replies(28)(3) = "Come on, How old are you?"
5180   LET setupReplies = replies
5190   RETURN setupReplies
5200 END FUNCTION
