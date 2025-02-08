Rem Concept and lisp implementation published by Joseph Weizenbaum (MIT): 
Rem "ELIZA - A Computer Program For the Study of Natural Language Communication Between Man and Machine" - In: 
Rem Computational Linguistis 1(1966)9, pp. 36-45 
Rem Revision history: 
Rem 2016-10-06 Initial version 
Rem 2017-03-29 Two diagrams updated (comments translated to English) 
Rem 2017-03-29 More keywords and replies added 
Rem 2019-03-14 Replies and mapping reorganised for easier maintenance 
Rem 2019-03-15 key map joined from keyword array and index map 
Rem 2019-03-28 Keyword "bot" inserted (same reply ring as "computer") 
Rem Generated by Structorizer 3.32-26 

Rem Copyright (C) 2018-05-14 ??? 
Rem License: GPLv3-link 
Rem GNU General Public License (V 3) 
Rem https://www.gnu.org/licenses/gpl.html 
Rem http://www.gnu.de/documents/gpl.de.html 

Rem  
Rem program ELIZA
Rem TODO: Check and accomplish your variable declarations here: 

Structure KeyMapEntry
  Dim keyword As String
  Dim index As Integer
End Structure

Dim replies(,) As String
Dim reflexions(,1) As String
Dim byePhrases(,1) As String
Dim keyMap() As KeyMapEntry
Dim varPart As String
Dim userInput As String
Dim replyRing() As String
Dim reply As String
Dim posAster As Integer
Dim offsets() As Integer
Dim keyIndex As Integer
Dim isRepeated As boolean
Dim isGone As boolean
Dim history(5) As ???
Dim findInfo(1) As integer
Dim entry As KeyMapEntry
Rem  
Rem Title information 
PRINT "************* ELIZA **************"
PRINT "* Original design by J. Weizenbaum"
PRINT "**********************************"
PRINT "* Adapted for Basic on IBM PC by"
PRINT "* - Patricia Danielson"
PRINT "* - Paul Hashfield"
PRINT "**********************************"
PRINT "* Adapted for Structorizer by"
PRINT "* - Kay Gürtzig / FH Erfurt 2016"
PRINT "* Version: 2.2 (2019-03-28)"
PRINT "**********************************"
Rem Stores the last five inputs of the user in a ring buffer, 
Rem the first element is the current insertion index 
history = Array(0, "", "", "", "", "")
Const replies = setupReplies()
Const reflexions = setupReflexions()
Const byePhrases = setupGoodByePhrases()
Const keyMap = setupKeywords()
offsets(length(keyMap)-1) = 0
isGone = false
Rem Starter 
PRINT "Hi! I\'m your new therapist. My name is Eliza. What\'s your problem?"
Do
  INPUT userInput
  Rem Converts the input to lowercase, cuts out interpunctation 
  Rem and pads the string 
  userInput = normalizeInput(userInput)
  isGone = checkGoodBye(userInput, byePhrases)
  If NOT isGone Then
    reply = "Please don\'t repeat yourself!"
    isRepeated = checkRepetition(history, userInput)
    If NOT isRepeated Then
      findInfo = findKeyword(keyMap, userInput)
      keyIndex = findInfo(0)
      If keyIndex < 0 Then
        Rem Should never happen... 
        keyIndex = length(keyMap)-1
      End If
      entry = keyMap(keyIndex)
      Rem Variable part of the reply 
      varPart = ""
      If length(entry.keyword) > 0 Then
        varPart = conjugateStrings(userInput, entry.keyword, findInfo(1), reflexions)
      End If
      replyRing = replies(entry.index)
      reply = replyRing(offsets(keyIndex))
      offsets(keyIndex) = (offsets(keyIndex) + 1) % length(replyRing)
      posAster = pos("*", reply)
      If posAster > 0 Then
        If varPart = " " Then
          reply = "You will have to elaborate more for me to help you."
        Else
          delete(reply, posAster, 1)
          insert(varPart, reply, posAster)
        End If
      End If
      reply = adjustSpelling(reply)
    End If
    PRINT reply
  End If
Loop Until isGone
End
Rem  
Rem Cares for correct letter case among others 
Rem TODO: Check (and specify if needed) the argument and result types! 
Function adjustSpelling(sentence As String) As String
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim word As String
  Dim start As String
  Dim result As String
  Dim position As Integer
  Rem  
  result = sentence
  position = 1
  Do While (position <= length(sentence)) AND (copy(sentence, position, 1) = " ")
    position = position + 1
  Loop
  If position <= length(sentence) Then
    start = copy(sentence, 1, position)
    delete(result, 1, position)
    insert(uppercase(start), result, 1)
  End If
  Dim array2de1525b() As String = {" i ", " i\'"}
  For Each word In array2de1525b
    position = pos(word, result)
    Do While position > 0
      delete(result, position+1, 1)
      insert("I", result, position+1)
      position = pos(word, result)
    Loop
  Next word
  Return result
End Function
Rem  
Rem Checks whether the given text contains some kind of 
Rem good-bye phrase inducing the end of the conversation 
Rem and if so writes a correspding good-bye message and 
Rem returns true, otherwise false 
Rem TODO: Check (and specify if needed) the argument and result types! 
Function checkGoodBye(text As String, phrases As String(50,0 To 1)) As boolean
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim saidBye As boolean
  Dim pair() As String
  Rem  
  For Each pair In phrases
    If pos(pair(0), text) > 0 Then
      saidBye = true
      PRINT pair(1)
      Return true
    End If
  Next pair
  return false
End Function
Rem  
Rem Checks whether newInput has occurred among the last 
Rem length(history) - 1 input strings and updates the history 
Rem TODO: Check (and specify if needed) the argument and result types! 
Function checkRepetition(history As array, newInput As String) As boolean
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim i As Integer
  Dim hasOccurred As boolean
  Dim currentIndex As ???
  Rem  
  hasOccurred = false
  If length(newInput) > 4 Then
    currentIndex = history(0);
    For i = 1 To length(history)-1
      If newInput = history(i) Then
        hasOccurred = true
      End If
    Next i
    history(history(0)+1) = newInput
    history(0) = (history(0) + 1) % (length(history) - 1)
  End If
  return hasOccurred
End Function
Rem  
Rem TODO: Check (and specify if needed) the argument and result types! 
Function conjugateStrings(sentence As String, key As String, keyPos As integer, flexions As String(50,0 To 1)) As String
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim right As String
  Dim result As String
  Dim position As Integer
  Dim pair() As String
  Dim left As String
  Rem  
  result = " " + copy(sentence, keyPos + length(key), length(sentence)) + " "
  For Each pair In flexions
    left = ""
    right = result
    position = pos(pair(0), right)
    Do While position > 0
      left = left + copy(right, 1, position-1) + pair(1)
      right = copy(right, position + length(pair(0)), length(right))
      position = pos(pair(0), right)
    Loop
    result = left + right
  Next pair
  Rem Eliminate multiple spaces 
  position = pos("  ", result)
  Do While position > 0
    result = copy(result, 1, position-1) + copy(result, position+1, length(result))
    position = pos("  ", result)
  Loop
  Return result
End Function
Rem  
Rem Converts the sentence to lowercase, eliminates all 
Rem interpunction (i.e. ',', '.', ';'), and pads the 
Rem sentence among blanks 
Rem TODO: Check (and specify if needed) the argument and result types! 
Function normalizeInput(sentence As String) As String
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim symbol As char
  Dim result As String
  Dim position As Integer
  Rem  
  sentence = lowercase(sentence)
  Rem TODO: Specify an appropriate element type for the array! 
  Dim array65006eba() As FIXME_65006eba = {'.', ',', ';', '!', '?'}
  For Each symbol In array65006eba
    position = pos(symbol, sentence)
    Do While position > 0
      sentence = copy(sentence, 1, position-1) + copy(sentence, position+1, length(sentence))
      position = pos(symbol, sentence)
    Loop
  Next symbol
  result = " " + sentence + " "
  Return result
End Function
Rem  
Rem TODO: Check (and specify if needed) the argument and result types! 
Function setupGoodByePhrases() As String(50,0 To 1)
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim phrases(,1) As String
  Rem  
  phrases(0) = Array(" shut", "Okay. If you feel that way I\'ll shut up. ... Your choice.")
  phrases(1) = Array("bye", "Well, let\'s end our talk for now. See you later. Bye.")
  return phrases
End Function
Rem  
Rem Returns an array of pairs of mutualy substitutable  
Rem TODO: Check (and specify if needed) the argument and result types! 
Function setupReflexions() As String(50,0 To 1)
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim reflexions(,1) As String
  Rem  
  reflexions(0) = Array(" are ", " am ")
  reflexions(1) = Array(" were ", " was ")
  reflexions(2) = Array(" you ", " I ")
  reflexions(3) = Array(" your", " my")
  reflexions(4) = Array(" i\'ve ", " you\'ve ")
  reflexions(5) = Array(" i\'m ", " you\'re ")
  reflexions(6) = Array(" me ", " you ")
  reflexions(7) = Array(" my ", " your ")
  reflexions(8) = Array(" i ", " you ")
  reflexions(9) = Array(" am ", " are ")
  return reflexions
End Function
Rem  
Rem This routine sets up the reply rings addressed by the key words defined in 
Rem routine `setupKeywords()´ and mapped hitherto by the cross table defined 
Rem in `setupMapping()´ 
Rem TODO: Check (and specify if needed) the argument and result types! 
Function setupReplies() As String(50,50)
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim setupReplies(,) As String
  Dim replies(,) As String
  Rem  
  Rem We start with the highest index for performance reasons 
  Rem (is to avoid frequent array resizing) 
  replies(29) = Array( "Say, do you have any psychological problems?", "What does that suggest to you?", "I see.", "I'm not sure I understand you fully.", "Come come elucidate your thoughts.", "Can you elaborate on that?", "That is quite interesting.")
  replies(0) = Array( "Don't you believe that I can*?", "Perhaps you would like to be like me?", "You want me to be able to*?")
  replies(1) = Array( "Perhaps you don't want to*?", "Do you want to be able to*?")
  replies(2) = Array( "What makes you think I am*?", "Does it please you to believe I am*?", "Perhaps you would like to be*?", "Do you sometimes wish you were*?")
  replies(3) = Array( "Don't you really*?", "Why don't you*?", "Do you wish to be able to*?", "Does that trouble you*?")
  replies(4) = Array( "Do you often feel*?", "Are you afraid of feeling*?", "Do you enjoy feeling*?")
  replies(5) = Array( "Do you really believe I don't*?", "Perhaps in good time I will*.", "Do you want me to*?")
  replies(6) = Array( "Do you think you should be able to*?", "Why can't you*?")
  replies(7) = Array( "Why are you interested in whether or not I am*?", "Would you prefer if I were not*?", "Perhaps in your fantasies I am*?")
  replies(8) = Array( "How do you know you can't*?", "Have you tried?","Perhaps you can now*.")
  replies(9) = Array( "Did you come to me because you are*?", "How long have you been*?", "Do you believe it is normal to be*?", "Do you enjoy being*?")
  replies(10) = Array( "We were discussing you--not me.", "Oh, I*.", "You're not really talking about me, are you?")
  replies(11) = Array( "What would it mean to you if you got*?", "Why do you want*?", "Suppose you soon got*...", "What if you never got*?", "I sometimes also want*.")
  replies(12) = Array( "Why do you ask?", "Does that question interest you?", "What answer would please you the most?", "What do you think?", "Are such questions on your mind often?", "What is it that you really want to know?", "Have you asked anyone else?", "Have you asked such questions before?", "What else comes to mind when you ask that?")
  replies(13) = Array( "Names don't interest me.", "I don't care about names -- please go on.")
  replies(14) = Array( "Is that the real reason?", "Don't any other reasons come to mind?", "Does that reason explain anything else?", "What other reasons might there be?")
  replies(15) = Array( "Please don't apologize!", "Apologies are not necessary.", "What feelings do you have when you apologize?", "Don't be so defensive!")
  replies(16) = Array( "What does that dream suggest to you?", "Do you dream often?", "What persons appear in your dreams?", "Are you disturbed by your dreams?")
  replies(17) = Array( "How do you do ...please state your problem.")
  replies(18) = Array( "You don't seem quite certain.", "Why the uncertain tone?", "Can't you be more positive?", "You aren't sure?", "Don't you know?")
  replies(19) = Array( "Are you saying no just to be negative?", "You are being a bit negative.", "Why not?", "Are you sure?", "Why no?")
  replies(20) = Array( "Why are you concerned about my*?", "What about your own*?")
  replies(21) = Array( "Can you think of a specific example?", "When?", "What are you thinking of?", "Really, always?")
  replies(22) = Array( "Do you really think so?", "But you are not sure you*?", "Do you doubt you*?")
  replies(23) = Array( "In what way?", "What resemblance do you see?", "What does the similarity suggest to you?", "What other connections do you see?", "Could there really be some connection?", "How?", "You seem quite positive.")
  replies(24) = Array( "Are you sure?", "I see.", "I understand.")
  replies(25) = Array( "Why do you bring up the topic of friends?", "Do your friends worry you?", "Do your friends pick on you?", "Are you sure you have any friends?", "Do you impose on your friends?", "Perhaps your love for friends worries you.")
  replies(26) = Array( "Do computers worry you?", "Are you talking about me in particular?", "Are you frightened by machines?", "Why do you mention computers?", "What do you think machines have to do with your problem?", "Don't you think computers can help people?", "What is it about machines that worries you?")
  replies(27) = Array( "Do you sometimes feel uneasy without a smartphone?", "Have you had these phantasies before?", "Does the world seem more real for you via apps?")
  replies(28) = Array( "Tell me more about your family.", "Who else in your family*?", "What does family relations mean for you?", "Come on, How old are you?")
  setupReplies = replies
  Return setupReplies
End Function
Rem  
Rem Looks for the occurrence of the first of the strings 
Rem contained in keywords within the given sentence (in 
Rem array order). 
Rem Returns an array of 
Rem 0: the index of the first identified keyword (if any, otherwise -1), 
Rem 1: the position inside sentence (0 if not found) 
Rem TODO: Check (and specify if needed) the argument and result types! 
Function findKeyword(Const keyMap As KeyMapEntry(50), sentence As String) As integer(0 To 1)
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim result(1) As Integer
  Dim position As Integer
  Dim i As Integer
  Dim entry As KeyMapEntry
  Rem  
  Rem Contains the index of the keyword and its position in sentence 
  result = Array(-1, 0)
  i = 0
  Do While (result(0) < 0) AND (i < length(keyMap))
    entry = keyMap(i)
    position = pos(entry.keyword, sentence)
    If position > 0 Then
      result(0) = i
      result(1) = position
    End If
    i = i+1
  Loop
  Return result
End Function
Rem  
Rem The lower the index the higher the rank of the keyword (search is sequential). 
Rem The index of the first keyword found in a user sentence maps to a respective 
Rem reply ring as defined in `setupReplies()´. 
Rem TODO: Check (and specify if needed) the argument and result types! 
Function setupKeywords() As KeyMapEntry(50)
  Rem TODO: Check and accomplish your variable declarations here: 
  Rem  
  Dim keywords() As KeyMapEntry
  Rem  
  Rem The empty key string (last entry) is the default clause - will always be found 
  keywords(39).keyword = ""
  keywords(39).index = 29
  keywords(0).keyword = "can you "
  keywords(0).index = 0
  keywords(1).keyword = "can i "
  keywords(1).index = 1
  keywords(2).keyword = "you are "
  keywords(2).index = 2
  keywords(3).keyword = "you\'re "
  keywords(3).index = 2
  keywords(4).keyword = "i don't "
  keywords(4).index = 3
  keywords(5).keyword = "i feel "
  keywords(5).index = 4
  keywords(6).keyword = "why don\'t you "
  keywords(6).index = 5
  keywords(7).keyword = "why can\'t i "
  keywords(7).index = 6
  keywords(8).keyword = "are you "
  keywords(8).index = 7
  keywords(9).keyword = "i can\'t "
  keywords(9).index = 8
  keywords(10).keyword = "i am "
  keywords(10).index = 9
  keywords(11).keyword = "i\'m "
  keywords(11).index = 9
  keywords(12).keyword = "you "
  keywords(12).index = 10
  keywords(13).keyword = "i want "
  keywords(13).index = 11
  keywords(14).keyword = "what "
  keywords(14).index = 12
  keywords(15).keyword = "how "
  keywords(15).index = 12
  keywords(16).keyword = "who "
  keywords(16).index = 12
  keywords(17).keyword = "where "
  keywords(17).index = 12
  keywords(18).keyword = "when "
  keywords(18).index = 12
  keywords(19).keyword = "why "
  keywords(19).index = 12
  keywords(20).keyword = "name "
  keywords(20).index = 13
  keywords(21).keyword = "cause "
  keywords(21).index = 14
  keywords(22).keyword = "sorry "
  keywords(22).index = 15
  keywords(23).keyword = "dream "
  keywords(23).index = 16
  keywords(24).keyword = "hello "
  keywords(24).index = 17
  keywords(25).keyword = "hi "
  keywords(25).index = 17
  keywords(26).keyword = "maybe "
  keywords(26).index = 18
  keywords(27).keyword = " no"
  keywords(27).index = 19
  keywords(28).keyword = "your "
  keywords(28).index = 20
  keywords(29).keyword = "always "
  keywords(29).index = 21
  keywords(30).keyword = "think "
  keywords(30).index = 22
  keywords(31).keyword = "alike "
  keywords(31).index = 23
  keywords(32).keyword = "yes "
  keywords(32).index = 24
  keywords(33).keyword = "friend "
  keywords(33).index = 25
  keywords(34).keyword = "computer"
  keywords(34).index = 26
  keywords(35).keyword = "bot "
  keywords(35).index = 26
  keywords(36).keyword = "smartphone"
  keywords(36).index = 27
  keywords(37).keyword = "father "
  keywords(37).index = 28
  keywords(38).keyword = "mother "
  keywords(38).index = 28
  return keywords
End Function
