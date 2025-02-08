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
120 REM Generated by Structorizer 3.32-26 
130 
140 REM Copyright (C) 2018-05-14 Kay Gürtzig 
150 REM License: GPLv3-link 
160 REM GNU General Public License (V 3) 
170 REM https://www.gnu.org/licenses/gpl.html 
180 REM http://www.gnu.de/documents/gpl.de.html 
190 
200 REM  
210 REM program ELIZA
220 REM TODO: add the respective type suffixes to your variable names if required 
230 
240 REM histArray contains the most recent user replies as ring buffer; 
250 REM histIndex is the index where the next reply is to be stored (= index of the oldest 
260 REM cached user reply). 
270 REM Note: The depth of the history is to be specified by initializing a variable of this type, 
280 REM e.g. for a history of depth 5: 
290 REM myhistory <- History{{"", "", "", "", ""}, 0} 
300 TYPE History
310   Dim histArray() AS String
320   histIndex AS Integer
330 END TYPE
340 REM Associates a key word in the text with an index in the reply ring array 
350 TYPE KeyMapEntry
360   keyword AS String
370   index AS Integer
380 END TYPE
390 
400 DIM replies(,) AS String
410 DIM reflexions(,1) AS String
420 DIM byePhrases(,1) AS String
430 DIM keyMap() AS KeyMapEntry
440 DIM offsets() AS Integer
450 DIM history AS History
460 DIM findInfo(1) AS integer
470 DIM entry AS KeyMapEntry
480 REM  
490 REM Title information 
500 PRINT "************* ELIZA **************"
510 PRINT "* Original design by J. Weizenbaum"
520 PRINT "**********************************"
530 PRINT "* Adapted for Basic on IBM PC by"
540 PRINT "* - Patricia Danielson"
550 PRINT "* - Paul Hashfield"
560 PRINT "**********************************"
570 PRINT "* Adapted for Structorizer by"
580 PRINT "* - Kay Gürtzig / FH Erfurt 2016"
590 PRINT "* Version: 2.3 (2020-02-24)"
600 PRINT "* (Requires at least Structorizer 3.30-03 to run)"
610 PRINT "**********************************"
620 REM Stores the last five inputs of the user in a ring buffer, 
630 REM the second component is the rolling (over-)write index. 
640 LET history.histArray(0) = ""
650 LET history.histArray(1) = ""
660 LET history.histArray(2) = ""
670 LET history.histArray(3) = ""
680 LET history.histArray(4) = ""
690 LET history.histIndex = 0
700 LET replies = setupReplies()
710 LET reflexions = setupReflexions()
720 LET byePhrases = setupGoodByePhrases()
730 LET keyMap = setupKeywords()
740 LET offsets(length(keyMap)-1) = 0
750 LET isGone = false
760 REM Starter 
770 PRINT "Hi! I\'m your new therapist. My name is Eliza. What\'s your problem?"
780 DO
790   INPUT userInput
800   REM Converts the input to lowercase, cuts out interpunctation 
810   REM and pads the string 
820   LET userInput = normalizeInput(userInput)
830   LET isGone = checkGoodBye(userInput, byePhrases)
840   IF NOT isGone THEN
850     LET reply = "Please don\'t repeat yourself!"
860     LET isRepeated = checkRepetition(history, userInput)
870     IF NOT isRepeated THEN
880       LET findInfo = findKeyword(keyMap, userInput)
890       LET keyIndex = findInfo(0)
900       IF keyIndex < 0 THEN
910         REM Should never happen... 
920         LET keyIndex = length(keyMap)-1
930       END IF
940       LET entry = keyMap(keyIndex)
950       REM Variable part of the reply 
960       LET varPart = ""
970       IF length(entry.keyword) > 0 THEN
980         LET varPart = conjugateStrings(userInput, entry.keyword, findInfo(1), reflexions)
990       END IF
1000       LET replyRing = replies(entry.index)
1010       LET reply = replyRing(offsets(keyIndex))
1020       LET offsets(keyIndex) = (offsets(keyIndex) + 1) % length(replyRing)
1030       LET posAster = pos("*", reply)
1040       IF posAster > 0 THEN
1050         IF varPart = " " THEN
1060           LET reply = "You will have to elaborate more for me to help you."
1070         ELSE
1080           delete(reply, posAster, 1)
1090           insert(varPart, reply, posAster)
1100         END IF
1110       END IF
1120       LET reply = adjustSpelling(reply)
1130     END IF
1140     PRINT reply
1150   END IF
1160 LOOP UNTIL isGone
1170 END
1180 REM  
1190 REM Cares for correct letter case among others 
1200 REM TODO: Add type-specific suffixes where necessary! 
1210 FUNCTION adjustSpelling(sentence AS String) AS String
1220   REM TODO: add the respective type suffixes to your variable names if required 
1230   REM  
1240   REM  
1250   LET result = sentence
1260   LET position = 1
1270   DO WHILE (position <= length(sentence)) AND (copy(sentence, position, 1) = " ")
1280     LET position = position + 1
1290   LOOP
1300   IF position <= length(sentence) THEN
1310     LET start = copy(sentence, 1, position)
1320     delete(result, 1, position)
1330     insert(uppercase(start), result, 1)
1340   END IF
1350   DIM array7f1a4809() AS String = {" i ", " i\'"}
1360   FOR EACH word IN array7f1a4809
1370     LET position = pos(word, result)
1380     DO WHILE position > 0
1390       delete(result, position+1, 1)
1400       insert("I", result, position+1)
1410       LET position = pos(word, result)
1420     LOOP
1430   NEXT word
1440   RETURN result
1450 END FUNCTION
1460 REM  
1470 REM Checks whether the given text contains some kind of 
1480 REM good-bye phrase inducing the end of the conversation 
1490 REM and if so writes a correspding good-bye message and 
1500 REM returns true, otherwise false 
1510 REM TODO: Add type-specific suffixes where necessary! 
1520 FUNCTION checkGoodBye(text AS String, phrases AS String(50,0 TO 1)) AS boolean
1530   REM TODO: add the respective type suffixes to your variable names if required 
1540   REM  
1550   REM  
1560   FOR EACH pair IN phrases
1570     IF pos(pair(0), text) > 0 THEN
1580       PRINT pair(1)
1590       RETURN true
1600     END IF
1610   NEXT pair
1620   return false
1630 END FUNCTION
1640 REM  
1650 REM Checks whether newInput has occurred among the recently cached 
1660 REM input strings in the histArray component of history and updates the history. 
1670 REM TODO: Add type-specific suffixes where necessary! 
1680 FUNCTION checkRepetition(history AS History, newInput AS String) AS boolean
1690   REM TODO: add the respective type suffixes to your variable names if required 
1700   REM  
1710   REM  
1720   LET hasOccurred = false
1730   IF length(newInput) > 4 THEN
1740     LET histDepth = length(history.histArray)
1750     FOR i = 0 TO histDepth-1
1760       IF newInput = history.histArray(i) THEN
1770         LET hasOccurred = true
1780       END IF
1790     NEXT i
1800     LET history.histArray(history.histIndex) = newInput
1810     LET history.histIndex = (history.histIndex + 1) % (histDepth)
1820   END IF
1830   return hasOccurred
1840 END FUNCTION
1850 REM  
1860 REM TODO: Add type-specific suffixes where necessary! 
1870 FUNCTION conjugateStrings(sentence AS String, key AS String, keyPos AS integer, flexions AS String(50,0 TO 1)) AS String
1880   REM TODO: add the respective type suffixes to your variable names if required 
1890   REM  
1900   REM  
1910   LET result = " " + copy(sentence, keyPos + length(key), length(sentence)) + " "
1920   FOR EACH pair IN flexions
1930     LET left = ""
1940     LET right = result
1950     LET position = pos(pair(0), right)
1960     DO WHILE position > 0
1970       LET left = left + copy(right, 1, position-1) + pair(1)
1980       LET right = copy(right, position + length(pair(0)), length(right))
1990       LET position = pos(pair(0), right)
2000     LOOP
2010     LET result = left + right
2020   NEXT pair
2030   REM Eliminate multiple spaces 
2040   LET position = pos("  ", result)
2050   DO WHILE position > 0
2060     LET result = copy(result, 1, position-1) + copy(result, position+1, length(result))
2070     LET position = pos("  ", result)
2080   LOOP
2090   RETURN result
2100 END FUNCTION
2110 REM  
2120 REM Looks for the occurrence of the first of the strings 
2130 REM contained in keywords within the given sentence (in 
2140 REM array order). 
2150 REM Returns an array of 
2160 REM 0: the index of the first identified keyword (if any, otherwise -1), 
2170 REM 1: the position inside sentence (0 if not found) 
2180 REM TODO: Add type-specific suffixes where necessary! 
2190 FUNCTION findKeyword(CONST keyMap AS KeyMapEntry(50), sentence AS String) AS integer(0 TO 1)
2200   REM TODO: add the respective type suffixes to your variable names if required 
2210   REM  
2220   DIM result(1) AS Integer
2230   DIM entry AS KeyMapEntry
2240   REM  
2250   REM Contains the index of the keyword and its position in sentence 
2260   LET result(0) = -1
2270   LET result(1) = 0
2280   LET i = 0
2290   DO WHILE (result(0) < 0) AND (i < length(keyMap))
2300     LET entry = keyMap(i)
2310     LET position = pos(entry.keyword, sentence)
2320     IF position > 0 THEN
2330       LET result(0) = i
2340       LET result(1) = position
2350     END IF
2360     LET i = i+1
2370   LOOP
2380   RETURN result
2390 END FUNCTION
2400 REM  
2410 REM Converts the sentence to lowercase, eliminates all 
2420 REM interpunction (i.e. ',', '.', ';'), and pads the 
2430 REM sentence among blanks 
2440 REM TODO: Add type-specific suffixes where necessary! 
2450 FUNCTION normalizeInput(sentence AS String) AS String
2460   REM TODO: add the respective type suffixes to your variable names if required 
2470   REM  
2480   REM  
2490   LET sentence = lowercase(sentence)
2500   REM TODO: Specify an appropriate element type for the array! 
2510   DIM array6aab8819() AS FIXME_6aab8819 = {'.', ',', ';', '!', '?'}
2520   FOR EACH symbol IN array6aab8819
2530     LET position = pos(symbol, sentence)
2540     DO WHILE position > 0
2550       LET sentence = copy(sentence, 1, position-1) + copy(sentence, position+1, length(sentence))
2560       LET position = pos(symbol, sentence)
2570     LOOP
2580   NEXT symbol
2590   LET result = " " + sentence + " "
2600   RETURN result
2610 END FUNCTION
2620 REM  
2630 REM TODO: Add type-specific suffixes where necessary! 
2640 FUNCTION setupGoodByePhrases() AS String(50,0 TO 1)
2650   REM TODO: add the respective type suffixes to your variable names if required 
2660   REM  
2670   DIM phrases(,1) AS String
2680   REM  
2690   LET phrases(0)(0) = " shut"
2700   LET phrases(0)(1) = "Okay. If you feel that way I\'ll shut up. ... Your choice."
2710   LET phrases(1)(0) = "bye"
2720   LET phrases(1)(1) = "Well, let\'s end our talk for now. See you later. Bye."
2730   return phrases
2740 END FUNCTION
2750 REM  
2760 REM The lower the index the higher the rank of the keyword (search is sequential). 
2770 REM The index of the first keyword found in a user sentence maps to a respective 
2780 REM reply ring as defined in `setupReplies()´. 
2790 REM TODO: Add type-specific suffixes where necessary! 
2800 FUNCTION setupKeywords() AS KeyMapEntry(50)
2810   REM TODO: add the respective type suffixes to your variable names if required 
2820   REM  
2830   DIM keywords() AS KeyMapEntry
2840   REM  
2850   REM The empty key string (last entry) is the default clause - will always be found 
2860   LET keywords(39).keyword = ""
2870   LET keywords(39).index = 29
2880   LET keywords(0).keyword = "can you "
2890   LET keywords(0).index = 0
2900   LET keywords(1).keyword = "can i "
2910   LET keywords(1).index = 1
2920   LET keywords(2).keyword = "you are "
2930   LET keywords(2).index = 2
2940   LET keywords(3).keyword = "you\'re "
2950   LET keywords(3).index = 2
2960   LET keywords(4).keyword = "i don't "
2970   LET keywords(4).index = 3
2980   LET keywords(5).keyword = "i feel "
2990   LET keywords(5).index = 4
3000   LET keywords(6).keyword = "why don\'t you "
3010   LET keywords(6).index = 5
3020   LET keywords(7).keyword = "why can\'t i "
3030   LET keywords(7).index = 6
3040   LET keywords(8).keyword = "are you "
3050   LET keywords(8).index = 7
3060   LET keywords(9).keyword = "i can\'t "
3070   LET keywords(9).index = 8
3080   LET keywords(10).keyword = "i am "
3090   LET keywords(10).index = 9
3100   LET keywords(11).keyword = "i\'m "
3110   LET keywords(11).index = 9
3120   LET keywords(12).keyword = "you "
3130   LET keywords(12).index = 10
3140   LET keywords(13).keyword = "i want "
3150   LET keywords(13).index = 11
3160   LET keywords(14).keyword = "what "
3170   LET keywords(14).index = 12
3180   LET keywords(15).keyword = "how "
3190   LET keywords(15).index = 12
3200   LET keywords(16).keyword = "who "
3210   LET keywords(16).index = 12
3220   LET keywords(17).keyword = "where "
3230   LET keywords(17).index = 12
3240   LET keywords(18).keyword = "when "
3250   LET keywords(18).index = 12
3260   LET keywords(19).keyword = "why "
3270   LET keywords(19).index = 12
3280   LET keywords(20).keyword = "name "
3290   LET keywords(20).index = 13
3300   LET keywords(21).keyword = "cause "
3310   LET keywords(21).index = 14
3320   LET keywords(22).keyword = "sorry "
3330   LET keywords(22).index = 15
3340   LET keywords(23).keyword = "dream "
3350   LET keywords(23).index = 16
3360   LET keywords(24).keyword = "hello "
3370   LET keywords(24).index = 17
3380   LET keywords(25).keyword = "hi "
3390   LET keywords(25).index = 17
3400   LET keywords(26).keyword = "maybe "
3410   LET keywords(26).index = 18
3420   LET keywords(27).keyword = " no"
3430   LET keywords(27).index = 19
3440   LET keywords(28).keyword = "your "
3450   LET keywords(28).index = 20
3460   LET keywords(29).keyword = "always "
3470   LET keywords(29).index = 21
3480   LET keywords(30).keyword = "think "
3490   LET keywords(30).index = 22
3500   LET keywords(31).keyword = "alike "
3510   LET keywords(31).index = 23
3520   LET keywords(32).keyword = "yes "
3530   LET keywords(32).index = 24
3540   LET keywords(33).keyword = "friend "
3550   LET keywords(33).index = 25
3560   LET keywords(34).keyword = "computer"
3570   LET keywords(34).index = 26
3580   LET keywords(35).keyword = "bot "
3590   LET keywords(35).index = 26
3600   LET keywords(36).keyword = "smartphone"
3610   LET keywords(36).index = 27
3620   LET keywords(37).keyword = "father "
3630   LET keywords(37).index = 28
3640   LET keywords(38).keyword = "mother "
3650   LET keywords(38).index = 28
3660   return keywords
3670 END FUNCTION
3680 REM  
3690 REM Returns an array of pairs of mutualy substitutable  
3700 REM TODO: Add type-specific suffixes where necessary! 
3710 FUNCTION setupReflexions() AS String(50,0 TO 1)
3720   REM TODO: add the respective type suffixes to your variable names if required 
3730   REM  
3740   DIM reflexions(,1) AS String
3750   REM  
3760   LET reflexions(0)(0) = " are "
3770   LET reflexions(0)(1) = " am "
3780   LET reflexions(1)(0) = " were "
3790   LET reflexions(1)(1) = " was "
3800   LET reflexions(2)(0) = " you "
3810   LET reflexions(2)(1) = " I "
3820   LET reflexions(3)(0) = " your"
3830   LET reflexions(3)(1) = " my"
3840   LET reflexions(4)(0) = " i\'ve "
3850   LET reflexions(4)(1) = " you\'ve "
3860   LET reflexions(5)(0) = " i\'m "
3870   LET reflexions(5)(1) = " you\'re "
3880   LET reflexions(6)(0) = " me "
3890   LET reflexions(6)(1) = " you "
3900   LET reflexions(7)(0) = " my "
3910   LET reflexions(7)(1) = " your "
3920   LET reflexions(8)(0) = " i "
3930   LET reflexions(8)(1) = " you "
3940   LET reflexions(9)(0) = " am "
3950   LET reflexions(9)(1) = " are "
3960   return reflexions
3970 END FUNCTION
3980 REM  
3990 REM This routine sets up the reply rings addressed by the key words defined in 
4000 REM routine `setupKeywords()´ and mapped hitherto by the cross table defined 
4010 REM in `setupMapping()´ 
4020 REM TODO: Add type-specific suffixes where necessary! 
4030 FUNCTION setupReplies() AS String(50,50)
4040   REM TODO: add the respective type suffixes to your variable names if required 
4050   REM  
4060   DIM setupReplies(,) AS String
4070   DIM replies(,) AS String
4080   REM  
4090   REM We start with the highest index for performance reasons 
4100   REM (is to avoid frequent array resizing) 
4110   LET replies(29)(0) = "Say, do you have any psychological problems?"
4120   LET replies(29)(1) = "What does that suggest to you?"
4130   LET replies(29)(2) = "I see."
4140   LET replies(29)(3) = "I'm not sure I understand you fully."
4150   LET replies(29)(4) = "Come come elucidate your thoughts."
4160   LET replies(29)(5) = "Can you elaborate on that?"
4170   LET replies(29)(6) = "That is quite interesting."
4180   LET replies(0)(0) = "Don't you believe that I can*?"
4190   LET replies(0)(1) = "Perhaps you would like to be like me?"
4200   LET replies(0)(2) = "You want me to be able to*?"
4210   LET replies(1)(0) = "Perhaps you don't want to*?"
4220   LET replies(1)(1) = "Do you want to be able to*?"
4230   LET replies(2)(0) = "What makes you think I am*?"
4240   LET replies(2)(1) = "Does it please you to believe I am*?"
4250   LET replies(2)(2) = "Perhaps you would like to be*?"
4260   LET replies(2)(3) = "Do you sometimes wish you were*?"
4270   LET replies(3)(0) = "Don't you really*?"
4280   LET replies(3)(1) = "Why don't you*?"
4290   LET replies(3)(2) = "Do you wish to be able to*?"
4300   LET replies(3)(3) = "Does that trouble you*?"
4310   LET replies(4)(0) = "Do you often feel*?"
4320   LET replies(4)(1) = "Are you afraid of feeling*?"
4330   LET replies(4)(2) = "Do you enjoy feeling*?"
4340   LET replies(5)(0) = "Do you really believe I don't*?"
4350   LET replies(5)(1) = "Perhaps in good time I will*."
4360   LET replies(5)(2) = "Do you want me to*?"
4370   LET replies(6)(0) = "Do you think you should be able to*?"
4380   LET replies(6)(1) = "Why can't you*?"
4390   LET replies(7)(0) = "Why are you interested in whether or not I am*?"
4400   LET replies(7)(1) = "Would you prefer if I were not*?"
4410   LET replies(7)(2) = "Perhaps in your fantasies I am*?"
4420   LET replies(8)(0) = "How do you know you can't*?"
4430   LET replies(8)(1) = "Have you tried?"
4440   LET replies(8)(2) = "Perhaps you can now*."
4450   LET replies(9)(0) = "Did you come to me because you are*?"
4460   LET replies(9)(1) = "How long have you been*?"
4470   LET replies(9)(2) = "Do you believe it is normal to be*?"
4480   LET replies(9)(3) = "Do you enjoy being*?"
4490   LET replies(10)(0) = "We were discussing you--not me."
4500   LET replies(10)(1) = "Oh, I*."
4510   LET replies(10)(2) = "You're not really talking about me, are you?"
4520   LET replies(11)(0) = "What would it mean to you if you got*?"
4530   LET replies(11)(1) = "Why do you want*?"
4540   LET replies(11)(2) = "Suppose you soon got*..."
4550   LET replies(11)(3) = "What if you never got*?"
4560   LET replies(11)(4) = "I sometimes also want*."
4570   LET replies(12)(0) = "Why do you ask?"
4580   LET replies(12)(1) = "Does that question interest you?"
4590   LET replies(12)(2) = "What answer would please you the most?"
4600   LET replies(12)(3) = "What do you think?"
4610   LET replies(12)(4) = "Are such questions on your mind often?"
4620   LET replies(12)(5) = "What is it that you really want to know?"
4630   LET replies(12)(6) = "Have you asked anyone else?"
4640   LET replies(12)(7) = "Have you asked such questions before?"
4650   LET replies(12)(8) = "What else comes to mind when you ask that?"
4660   LET replies(13)(0) = "Names don't interest me."
4670   LET replies(13)(1) = "I don't care about names -- please go on."
4680   LET replies(14)(0) = "Is that the real reason?"
4690   LET replies(14)(1) = "Don't any other reasons come to mind?"
4700   LET replies(14)(2) = "Does that reason explain anything else?"
4710   LET replies(14)(3) = "What other reasons might there be?"
4720   LET replies(15)(0) = "Please don't apologize!"
4730   LET replies(15)(1) = "Apologies are not necessary."
4740   LET replies(15)(2) = "What feelings do you have when you apologize?"
4750   LET replies(15)(3) = "Don't be so defensive!"
4760   LET replies(16)(0) = "What does that dream suggest to you?"
4770   LET replies(16)(1) = "Do you dream often?"
4780   LET replies(16)(2) = "What persons appear in your dreams?"
4790   LET replies(16)(3) = "Are you disturbed by your dreams?"
4800   LET replies(17)(0) = "How do you do ...please state your problem."
4810   LET replies(18)(0) = "You don't seem quite certain."
4820   LET replies(18)(1) = "Why the uncertain tone?"
4830   LET replies(18)(2) = "Can't you be more positive?"
4840   LET replies(18)(3) = "You aren't sure?"
4850   LET replies(18)(4) = "Don't you know?"
4860   LET replies(19)(0) = "Are you saying no just to be negative?"
4870   LET replies(19)(1) = "You are being a bit negative."
4880   LET replies(19)(2) = "Why not?"
4890   LET replies(19)(3) = "Are you sure?"
4900   LET replies(19)(4) = "Why no?"
4910   LET replies(20)(0) = "Why are you concerned about my*?"
4920   LET replies(20)(1) = "What about your own*?"
4930   LET replies(21)(0) = "Can you think of a specific example?"
4940   LET replies(21)(1) = "When?"
4950   LET replies(21)(2) = "What are you thinking of?"
4960   LET replies(21)(3) = "Really, always?"
4970   LET replies(22)(0) = "Do you really think so?"
4980   LET replies(22)(1) = "But you are not sure you*?"
4990   LET replies(22)(2) = "Do you doubt you*?"
5000   LET replies(23)(0) = "In what way?"
5010   LET replies(23)(1) = "What resemblance do you see?"
5020   LET replies(23)(2) = "What does the similarity suggest to you?"
5030   LET replies(23)(3) = "What other connections do you see?"
5040   LET replies(23)(4) = "Could there really be some connection?"
5050   LET replies(23)(5) = "How?"
5060   LET replies(23)(6) = "You seem quite positive."
5070   LET replies(24)(0) = "Are you sure?"
5080   LET replies(24)(1) = "I see."
5090   LET replies(24)(2) = "I understand."
5100   LET replies(25)(0) = "Why do you bring up the topic of friends?"
5110   LET replies(25)(1) = "Do your friends worry you?"
5120   LET replies(25)(2) = "Do your friends pick on you?"
5130   LET replies(25)(3) = "Are you sure you have any friends?"
5140   LET replies(25)(4) = "Do you impose on your friends?"
5150   LET replies(25)(5) = "Perhaps your love for friends worries you."
5160   LET replies(26)(0) = "Do computers worry you?"
5170   LET replies(26)(1) = "Are you talking about me in particular?"
5180   LET replies(26)(2) = "Are you frightened by machines?"
5190   LET replies(26)(3) = "Why do you mention computers?"
5200   LET replies(26)(4) = "What do you think machines have to do with your problem?"
5210   LET replies(26)(5) = "Don't you think computers can help people?"
5220   LET replies(26)(6) = "What is it about machines that worries you?"
5230   LET replies(27)(0) = "Do you sometimes feel uneasy without a smartphone?"
5240   LET replies(27)(1) = "Have you had these phantasies before?"
5250   LET replies(27)(2) = "Does the world seem more real for you via apps?"
5260   LET replies(28)(0) = "Tell me more about your family."
5270   LET replies(28)(1) = "Who else in your family*?"
5280   LET replies(28)(2) = "What does family relations mean for you?"
5290   LET replies(28)(3) = "Come on, How old are you?"
5300   LET setupReplies = replies
5310   RETURN setupReplies
5320 END FUNCTION
