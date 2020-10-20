{
  Concept and lisp implementation published by Joseph Weizenbaum (MIT):
  "ELIZA - A Computer Program For the Study of Natural Language Communication Between Man and Machine" - In:
  Computational Linguistis 1(1966)9, pp. 36-45
  Revision history:
  2016-10-06 Initial version
  2017-03-29 Two diagrams updated (comments translated to English)
  2017-03-29 More keywords and replies added
  2019-03-14 Replies and mapping reorganised for easier maintenance
  2019-03-15 key map joined from keyword array and index map
  2019-03-28 Keyword "bot" inserted (same reply ring as "computer")
}
program ELIZA;
{ Generated by Structorizer 3.30-11 }

{ Copyright (C) 2018-05-14 ??? }
{ License: GPLv3-link }
{
  GNU General Public License (V 3)
  https://www.gnu.org/licenses/gpl.html
  http://www.gnu.de/documents/gpl.de.html
}

type
  KeyMapEntry = RECORD
      keyword:	string;
      index:	Longint;
    END;

const
  replies = setupReplies();
  
  reflexions = setupReflexions();
  
  byePhrases = setupGoodByePhrases();
  
  keyMap = setupKeywords();

var
  index4aaad9a6: 1..5;
  array4aaad9a6: array [1..5] of string;
  indexe973e8e3: 1..2;
  arraye973e8e3: array [1..2] of string;
  varPart: String;
  {
    Converts the input to lowercase, cuts out interpunctation
    and pads the string
  }
  userInput: string;
  replyRing: ???;	{ FIXME! }
  reply: String;
  posAster: Longint;
  offsets: array [0..49] of Longint;
  { Should never happen... }
  keyIndex: ???;	{ FIXME! }
  isRepeated: boolean;
  isGone: boolean;
  {
    Stores the last five inputs of the user in a ring buffer,
    the first element is the current insertion index
  }
  history: array [0..5] of ???;	{ FIXME! }
  findInfo: array [0..1] of Longint;
  entry: KeyMapEntry;

{ Cares for correct letter case among others }
function adjustSpelling(sentence: string): string;

var
  word: String;
  start: string;
  result: string;
  position: Longint;

begin
  result := sentence;
  position := 1;
  while (position <= length(sentence)) and (copy(sentence, position, 1) = ' ') do
  begin
    position := position + 1;
  end;
  if position <= length(sentence) then
  begin
    start := copy(sentence, 1, position);
    delete(result, 1, position);
    insert(uppercase(start), result, 1);
  end;
  arraye973e8e3[1] := ' i ';
  arraye973e8e3[2] := ' i\''';
  for indexe973e8e3 := 1 to 2 do
  begin
    word := arraye973e8e3[indexe973e8e3];
    position := pos(word, result);
    while (position > 0) do
    begin
      delete(result, position+1, 1);
      insert('I', result, position+1);
      position := pos(word, result);
    end;
  end;

{ Automatically inserted to ensure Pascal value return. May be dropped on Structorizer reimport. }
  adjustSpelling := result;

end;

{
* Checks whether the given text contains some kind of
* good-bye phrase inducing the end of the conversation
* and if so writes a correspding good-bye message and
* returns true, otherwise false
}
function checkGoodBye(text: string; phrases: array [0..49] of array [0..1] of string): boolean;

var
  saidBye: boolean;
  pair: array [-1..49] of string;

begin
  { TODO: Rewrite this loop (there was no way to convert this automatically) }
  for pair in phrases do
  begin
    if (pos(pair[0], text) > 0) then
    begin
      saidBye := true;
      writeln(pair[1]);
      checkGoodBye := true;
      exit;
    end;
  end;
  checkGoodBye := false;

end;

{
* Checks whether newInput has occurred among the last
* length(history) - 1 input strings and updates the history
}
function checkRepetition(history: array; newInput: string): boolean;

var
  i: Longint;
  hasOccurred: boolean;
  currentIndex: ???;	{ FIXME! }

begin
  hasOccurred := false;
  if (length(newInput) > 4) then
  begin
    currentIndex := history[0];;
    for i := 1 to length(history)-1 do
    begin
      if (newInput = history[i]) then
      begin
        hasOccurred := true;
      end;
    end;
    history[history[0]+1] := newInput;
    history[0] := (history[0] + 1) mod (length(history) - 1);
  end;
  checkRepetition := hasOccurred;

end;

function conjugateStrings(sentence: string; key: string; keyPos: Longint; flexions: array [0..49] of array [0..1] of string): string;

var
  right: String;
  result: String;
  position: Longint;
  pair: array [-1..49] of string;
  left: String;

begin
  result := ' ' + copy(sentence, keyPos + length(key), length(sentence)) + ' ';
  { TODO: Rewrite this loop (there was no way to convert this automatically) }
  for pair in flexions do
  begin
    left := '';
    right := result;
    position := pos(pair[0], right);
    while (position > 0) do
    begin
      left := left + copy(right, 1, position-1) + pair[1];
      right := copy(right, position + length(pair[0]), length(right));
      position := pos(pair[0], right);
    end;
    result := left + right;
  end;
  { Eliminate multiple spaces }
  position := pos(' ', result);
  while (position > 0) do
  begin
    result := copy(result, 1, position-1) + copy(result, position+1, length(result));
    position := pos(' ', result);
  end;

{ Automatically inserted to ensure Pascal value return. May be dropped on Structorizer reimport. }
  conjugateStrings := result;

end;

{
* Looks for the occurrence of the first of the strings
* contained in keywords within the given sentence (in
* array order).
* Returns an array of
* 0: the index of the first identified keyword (if any, otherwise -1),
* 1: the position inside sentence (0 if not found)
}
function findKeyword(keyMap: const array of KeyMapEntry; sentence: string): array [0..1] of Longint;

const
  keyMap = null;

var
  result: array [0..1] of Longint;
  position: Longint;
  i: Longint;
  entry: KeyMapEntry;

begin
  { Contains the index of the keyword and its position in sentence }
  { Hint: Automatically decomposed array initialization }
  result[0] := -1;
  result[1] := 0;
  i := 0;
  while (result[0] < 0) and (i < length(keyMap)) do
  begin
    entry := keyMap[i];
    position := pos(entry.keyword, sentence);
    if (position > 0) then
    begin
      result[0] := i;
      result[1] := position;
    end;
    i := i+1;
  end;

{ Automatically inserted to ensure Pascal value return. May be dropped on Structorizer reimport. }
  findKeyword := result;

end;

{
* Converts the sentence to lowercase, eliminates all
* interpunction (i.e. ',', '.', ';'), and pads the
* sentence among blanks
}
function normalizeInput(sentence: string): string;

var
  symbol: char;
  result: String;
  position: Longint;

begin
  sentence := lowercase(sentence);
  array4aaad9a6[1] := '.';
  array4aaad9a6[2] := ',';
  array4aaad9a6[3] := ';';
  array4aaad9a6[4] := '!';
  array4aaad9a6[5] := '?';
  for index4aaad9a6 := 1 to 5 do
  begin
    symbol := array4aaad9a6[index4aaad9a6];
    position := pos(symbol, sentence);
    while (position > 0) do
    begin
      sentence := copy(sentence, 1, position-1) + copy(sentence, position+1, length(sentence));
      position := pos(symbol, sentence);
    end;
  end;
  result := ' ' + sentence + ' ';

{ Automatically inserted to ensure Pascal value return. May be dropped on Structorizer reimport. }
  normalizeInput := result;

end;

function setupGoodByePhrases(): array [0..49] of array [0..1] of string;

var
  phrases: array [0..49] of array [0..1] of String;

begin
  { Hint: Automatically decomposed array initialization }
  phrases[0, 0] := ' shut';
  phrases[0, 1] := 'Okay. If you feel that way I\''ll shut up. ... Your choice.';
  { Hint: Automatically decomposed array initialization }
  phrases[1, 0] := 'bye';
  phrases[1, 1] := 'Well, let\''s end our talk for now. See you later. Bye.';
  setupGoodByePhrases := phrases;

end;

{
* The lower the index the higher the rank of the keyword (search is sequential).
* The index of the first keyword found in a user sentence maps to a respective
* reply ring as defined in `setupReplies()´.
}
function setupKeywords(): array [0..49] of KeyMapEntry;

var
  keywords: array [0..49] of KeyMapEntry;

begin
  { The empty key string (last entry) is the default clause - will always be found }
  keywords[39].keyword := '';
  keywords[39].index := 29;
  keywords[0].keyword := 'can you ';
  keywords[0].index := 0;
  keywords[1].keyword := 'can i ';
  keywords[1].index := 1;
  keywords[2].keyword := 'you are ';
  keywords[2].index := 2;
  keywords[3].keyword := 'you\''re ';
  keywords[3].index := 2;
  keywords[4].keyword := 'i don''t ';
  keywords[4].index := 3;
  keywords[5].keyword := 'i feel ';
  keywords[5].index := 4;
  keywords[6].keyword := 'why don\''t you ';
  keywords[6].index := 5;
  keywords[7].keyword := 'why can\''t i ';
  keywords[7].index := 6;
  keywords[8].keyword := 'are you ';
  keywords[8].index := 7;
  keywords[9].keyword := 'i can\''t ';
  keywords[9].index := 8;
  keywords[10].keyword := 'i am ';
  keywords[10].index := 9;
  keywords[11].keyword := 'i\''m ';
  keywords[11].index := 9;
  keywords[12].keyword := 'you ';
  keywords[12].index := 10;
  keywords[13].keyword := 'i want ';
  keywords[13].index := 11;
  keywords[14].keyword := 'what ';
  keywords[14].index := 12;
  keywords[15].keyword := 'how ';
  keywords[15].index := 12;
  keywords[16].keyword := 'who ';
  keywords[16].index := 12;
  keywords[17].keyword := 'where ';
  keywords[17].index := 12;
  keywords[18].keyword := 'when ';
  keywords[18].index := 12;
  keywords[19].keyword := 'why ';
  keywords[19].index := 12;
  keywords[20].keyword := 'name ';
  keywords[20].index := 13;
  keywords[21].keyword := 'cause ';
  keywords[21].index := 14;
  keywords[22].keyword := 'sorry ';
  keywords[22].index := 15;
  keywords[23].keyword := 'dream ';
  keywords[23].index := 16;
  keywords[24].keyword := 'hello ';
  keywords[24].index := 17;
  keywords[25].keyword := 'hi ';
  keywords[25].index := 17;
  keywords[26].keyword := 'maybe ';
  keywords[26].index := 18;
  keywords[27].keyword := ' no';
  keywords[27].index := 19;
  keywords[28].keyword := 'your ';
  keywords[28].index := 20;
  keywords[29].keyword := 'always ';
  keywords[29].index := 21;
  keywords[30].keyword := 'think ';
  keywords[30].index := 22;
  keywords[31].keyword := 'alike ';
  keywords[31].index := 23;
  keywords[32].keyword := 'yes ';
  keywords[32].index := 24;
  keywords[33].keyword := 'friend ';
  keywords[33].index := 25;
  keywords[34].keyword := 'computer';
  keywords[34].index := 26;
  keywords[35].keyword := 'bot ';
  keywords[35].index := 26;
  keywords[36].keyword := 'smartphone';
  keywords[36].index := 27;
  keywords[37].keyword := 'father ';
  keywords[37].index := 28;
  keywords[38].keyword := 'mother ';
  keywords[38].index := 28;
  setupKeywords := keywords;

end;

{ Returns an array of pairs of mutualy substitutable  }
function setupReflexions(): array [0..49] of array [0..1] of string;

var
  reflexions: array [0..49] of array [0..1] of String;

begin
  { Hint: Automatically decomposed array initialization }
  reflexions[0, 0] := ' are ';
  reflexions[0, 1] := ' am ';
  { Hint: Automatically decomposed array initialization }
  reflexions[1, 0] := ' were ';
  reflexions[1, 1] := ' was ';
  { Hint: Automatically decomposed array initialization }
  reflexions[2, 0] := ' you ';
  reflexions[2, 1] := ' I ';
  { Hint: Automatically decomposed array initialization }
  reflexions[3, 0] := ' your';
  reflexions[3, 1] := ' my';
  { Hint: Automatically decomposed array initialization }
  reflexions[4, 0] := ' i\''ve ', ' you\''ve ';
  { Hint: Automatically decomposed array initialization }
  reflexions[5, 0] := ' i\''m ', ' you\''re ';
  { Hint: Automatically decomposed array initialization }
  reflexions[6, 0] := ' me ';
  reflexions[6, 1] := ' you ';
  { Hint: Automatically decomposed array initialization }
  reflexions[7, 0] := ' my ';
  reflexions[7, 1] := ' your ';
  { Hint: Automatically decomposed array initialization }
  reflexions[8, 0] := ' i ';
  reflexions[8, 1] := ' you ';
  { Hint: Automatically decomposed array initialization }
  reflexions[9, 0] := ' am ';
  reflexions[9, 1] := ' are ';
  setupReflexions := reflexions;

end;

{
* This routine sets up the reply rings addressed by the key words defined in
* routine `setupKeywords()´ and mapped hitherto by the cross table defined
* in `setupMapping()´
}
function setupReplies(): array [0..49] of array [0..49] of string;

var
  replies: array [0..49] of array [0..49] of String;

begin
  {
    We start with the highest index for performance reasons
    (is to avoid frequent array resizing)
  }
  { Hint: Automatically decomposed array initialization }
  replies[29, 0] := 'Say, do you have any psychological problems?';
  replies[29, 1] := 'What does that suggest to you?';
  replies[29, 2] := 'I see.';
  replies[29, 3] := 'I''m not sure I understand you fully.';
  replies[29, 4] := 'Come come elucidate your thoughts.';
  replies[29, 5] := 'Can you elaborate on that?';
  replies[29, 6] := 'That is quite interesting.';
  { Hint: Automatically decomposed array initialization }
  replies[0, 0] := 'Don''t you believe that I can*?';
  replies[0, 1] := 'Perhaps you would like to be like me?';
  replies[0, 2] := 'You want me to be able to*?';
  { Hint: Automatically decomposed array initialization }
  replies[1, 0] := 'Perhaps you don''t want to*?';
  replies[1, 1] := 'Do you want to be able to*?';
  { Hint: Automatically decomposed array initialization }
  replies[2, 0] := 'What makes you think I am*?';
  replies[2, 1] := 'Does it please you to believe I am*?';
  replies[2, 2] := 'Perhaps you would like to be*?';
  replies[2, 3] := 'Do you sometimes wish you were*?';
  { Hint: Automatically decomposed array initialization }
  replies[3, 0] := 'Don''t you really*?';
  replies[3, 1] := 'Why don''t you*?';
  replies[3, 2] := 'Do you wish to be able to*?';
  replies[3, 3] := 'Does that trouble you*?';
  { Hint: Automatically decomposed array initialization }
  replies[4, 0] := 'Do you often feel*?';
  replies[4, 1] := 'Are you afraid of feeling*?';
  replies[4, 2] := 'Do you enjoy feeling*?';
  { Hint: Automatically decomposed array initialization }
  replies[5, 0] := 'Do you really believe I don''t*?';
  replies[5, 1] := 'Perhaps in good time I will*.';
  replies[5, 2] := 'Do you want me to*?';
  { Hint: Automatically decomposed array initialization }
  replies[6, 0] := 'Do you think you should be able to*?';
  replies[6, 1] := 'Why can''t you*?';
  { Hint: Automatically decomposed array initialization }
  replies[7, 0] := 'Why are you interested in whether or not I am*?';
  replies[7, 1] := 'Would you prefer if I were not*?';
  replies[7, 2] := 'Perhaps in your fantasies I am*?';
  { Hint: Automatically decomposed array initialization }
  replies[8, 0] := 'How do you know you can''t*?';
  replies[8, 1] := 'Have you tried?';
  replies[8, 2] := 'Perhaps you can now*.';
  { Hint: Automatically decomposed array initialization }
  replies[9, 0] := 'Did you come to me because you are*?';
  replies[9, 1] := 'How long have you been*?';
  replies[9, 2] := 'Do you believe it is normal to be*?';
  replies[9, 3] := 'Do you enjoy being*?';
  { Hint: Automatically decomposed array initialization }
  replies[10, 0] := 'We were discussing you--not me.';
  replies[10, 1] := 'Oh, I*.';
  replies[10, 2] := 'You''re not really talking about me, are you?';
  { Hint: Automatically decomposed array initialization }
  replies[11, 0] := 'What would it mean to you if you got*?';
  replies[11, 1] := 'Why do you want*?';
  replies[11, 2] := 'Suppose you soon got*...';
  replies[11, 3] := 'What if you never got*?';
  replies[11, 4] := 'I sometimes also want*.';
  { Hint: Automatically decomposed array initialization }
  replies[12, 0] := 'Why do you ask?';
  replies[12, 1] := 'Does that question interest you?';
  replies[12, 2] := 'What answer would please you the most?';
  replies[12, 3] := 'What do you think?';
  replies[12, 4] := 'Are such questions on your mind often?';
  replies[12, 5] := 'What is it that you really want to know?';
  replies[12, 6] := 'Have you asked anyone else?';
  replies[12, 7] := 'Have you asked such questions before?';
  replies[12, 8] := 'What else comes to mind when you ask that?';
  { Hint: Automatically decomposed array initialization }
  replies[13, 0] := 'Names don''t interest me.';
  replies[13, 1] := 'I don''t care about names -- please go on.';
  { Hint: Automatically decomposed array initialization }
  replies[14, 0] := 'Is that the real reason?';
  replies[14, 1] := 'Don''t any other reasons come to mind?';
  replies[14, 2] := 'Does that reason explain anything else?';
  replies[14, 3] := 'What other reasons might there be?';
  { Hint: Automatically decomposed array initialization }
  replies[15, 0] := 'Please don''t apologize!';
  replies[15, 1] := 'Apologies are not necessary.';
  replies[15, 2] := 'What feelings do you have when you apologize?';
  replies[15, 3] := 'Don''t be so defensive!';
  { Hint: Automatically decomposed array initialization }
  replies[16, 0] := 'What does that dream suggest to you?';
  replies[16, 1] := 'Do you dream often?';
  replies[16, 2] := 'What persons appear in your dreams?';
  replies[16, 3] := 'Are you disturbed by your dreams?';
  { Hint: Automatically decomposed array initialization }
  replies[17, 0] := 'How do you do ...please state your problem.';
  { Hint: Automatically decomposed array initialization }
  replies[18, 0] := 'You don''t seem quite certain.';
  replies[18, 1] := 'Why the uncertain tone?';
  replies[18, 2] := 'Can''t you be more positive?';
  replies[18, 3] := 'You aren''t sure?';
  replies[18, 4] := 'Don''t you know?';
  { Hint: Automatically decomposed array initialization }
  replies[19, 0] := 'Are you saying no just to be negative?';
  replies[19, 1] := 'You are being a bit negative.';
  replies[19, 2] := 'Why not?';
  replies[19, 3] := 'Are you sure?';
  replies[19, 4] := 'Why no?';
  { Hint: Automatically decomposed array initialization }
  replies[20, 0] := 'Why are you concerned about my*?';
  replies[20, 1] := 'What about your own*?';
  { Hint: Automatically decomposed array initialization }
  replies[21, 0] := 'Can you think of a specific example?';
  replies[21, 1] := 'When?';
  replies[21, 2] := 'What are you thinking of?';
  replies[21, 3] := 'Really, always?';
  { Hint: Automatically decomposed array initialization }
  replies[22, 0] := 'Do you really think so?';
  replies[22, 1] := 'But you are not sure you*?';
  replies[22, 2] := 'Do you doubt you*?';
  { Hint: Automatically decomposed array initialization }
  replies[23, 0] := 'In what way?';
  replies[23, 1] := 'What resemblance do you see?';
  replies[23, 2] := 'What does the similarity suggest to you?';
  replies[23, 3] := 'What other connections do you see?';
  replies[23, 4] := 'Could there really be some connection?';
  replies[23, 5] := 'How?';
  replies[23, 6] := 'You seem quite positive.';
  { Hint: Automatically decomposed array initialization }
  replies[24, 0] := 'Are you sure?';
  replies[24, 1] := 'I see.';
  replies[24, 2] := 'I understand.';
  { Hint: Automatically decomposed array initialization }
  replies[25, 0] := 'Why do you bring up the topic of friends?';
  replies[25, 1] := 'Do your friends worry you?';
  replies[25, 2] := 'Do your friends pick on you?';
  replies[25, 3] := 'Are you sure you have any friends?';
  replies[25, 4] := 'Do you impose on your friends?';
  replies[25, 5] := 'Perhaps your love for friends worries you.';
  { Hint: Automatically decomposed array initialization }
  replies[26, 0] := 'Do computers worry you?';
  replies[26, 1] := 'Are you talking about me in particular?';
  replies[26, 2] := 'Are you frightened by machines?';
  replies[26, 3] := 'Why do you mention computers?';
  replies[26, 4] := 'What do you think machines have to do with your problem?';
  replies[26, 5] := 'Don''t you think computers can help people?';
  replies[26, 6] := 'What is it about machines that worries you?';
  { Hint: Automatically decomposed array initialization }
  replies[27, 0] := 'Do you sometimes feel uneasy without a smartphone?';
  replies[27, 1] := 'Have you had these phantasies before?';
  replies[27, 2] := 'Does the world seem more real for you via apps?';
  { Hint: Automatically decomposed array initialization }
  replies[28, 0] := 'Tell me more about your family.';
  replies[28, 1] := 'Who else in your family*?';
  replies[28, 2] := 'What does family relations mean for you?';
  replies[28, 3] := 'Come on, How old are you?';
  setupReplies := replies;

end;

{ = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = }


begin
  { Title information }
  writeln('************* ELIZA **************');
  writeln('* Original design by J. Weizenbaum');
  writeln('**********************************');
  writeln('* Adapted for Basic on IBM PC by');
  writeln('* - Patricia Danielson');
  writeln('* - Paul Hashfield');
  writeln('**********************************');
  writeln('* Adapted for Structorizer by');
  writeln('* - Kay Gürtzig / FH Erfurt 2016');
  writeln('* Version: 2.2 (2019-03-28)');
  writeln('**********************************');
  {
    Stores the last five inputs of the user in a ring buffer,
    the first element is the current insertion index
  }
  { Hint: Automatically decomposed array initialization }
  history[0] := 0;
  history[1] := '';
  history[2] := '';
  history[3] := '';
  history[4] := '';
  history[5] := '';
  replies := setupReplies();
  reflexions := setupReflexions();
  byePhrases := setupGoodByePhrases();
  keyMap := setupKeywords();
  offsets[length(keyMap)-1] := 0;
  isGone := false;
  { Starter }
  writeln('Hi! I\''m your new therapist. My name is Eliza. What\''s your problem?');
  repeat
    readln(userInput);
    {
      Converts the input to lowercase, cuts out interpunctation
      and pads the string
    }
    userInput := normalizeInput(userInput);
    isGone := checkGoodBye(userInput, byePhrases);
    if (not isGone) then
    begin
      reply := 'Please don\''t repeat yourself!';
      isRepeated := checkRepetition(history, userInput);
      if (not isRepeated) then
      begin
        findInfo := findKeyword(keyMap, userInput);
        keyIndex := findInfo[0];
        if (keyIndex < 0) then
        begin
          { Should never happen... }
          keyIndex := length(keyMap)-1;
        end;
        entry := keyMap[keyIndex];
        { Variable part of the reply }
        varPart := '';
        if (length(entry.keyword) > 0) then
        begin
          varPart := conjugateStrings(userInput, entry.keyword, findInfo[1], reflexions);
        end;
        replyRing := replies[entry.index];
        reply := replyRing[offsets[keyIndex]];
        offsets[keyIndex] := (offsets[keyIndex] + 1) mod length(replyRing);
        posAster := pos('*', reply);
        if (posAster > 0) then
        begin
          if (varPart = ' ') then
          begin
            reply := 'You will have to elaborate more for me to help you.';
          end
          else
          begin
            delete(reply, posAster, 1);
            insert(varPart, reply, posAster);
          end;
        end;
        reply := adjustSpelling(reply);
      end;
      writeln(reply);
    end;
    until (isGone);
end.
