UNIT StructorizerFileAPI;
{
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This unit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 }
 
{******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    File API for the PasGenerator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016-12-26      First Issue
 *      Kay Gürtzig     2016-12-27      fileRead function enhanced roughly to handle quoted strings
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      Parts of the API are directly transformed by the PasGenerator. To replace function calls by
 *      procedure calls, however, is not possible in general. So we provide adapters for the fileRead
 *      functions here. 
 *
 ******************************************************************************************************}

INTERFACE

TYPE
  LongIntFile = FILE OF LongInt;
  DoubleFile = FILE OF Double;

{ Implements File API base function fileRead by means of read in a Text file }
function fileRead(var file: Text): String;
{ Implements File API function fileReadChar by means of read in a Text file }
function fileReadChar(var file: Text): Char;
{ Implements File API function fileReadInt by means of read in a typed file }
function fileReadInt(var file: LongIntFile): LongInt;
{ Implements File API function fileReadDouble by means of read in a typed file }
function fileRead(var file: DoubleFile): Double;
{ Implements File API base function fileReadLine by means of readln in a Text file }
function fileReadLine(var file: Text): String;

IMPLEMENTATION


{ Approaches File API base function fileRead by means of read in a Text file }
function fileRead(var tFile: Text): String;

var
  String word, nextWord;
  Integer len; { String length }

begin
  word = '';
  read(tFile, word);
  { FIXME: Detection of array initializers? }
  len = length(word);
  if len > 0 then
  begin
    { test for double or single quote }
    if (word[1] = #34) or (word[1] = #39) then
    begin
      while not eof(tFile) and (word[len] <> word[1]) do
      begin
        read(tFile, nextWord);
        word = word + ' ' + nextWord;
        len = length(word);
      end;
      if word[len] = word[1] then
        word = copy(word, 2, len-2);
    end
  end;
  fileRead = word;
end;

{ Implements File API function fileReadChar by means of read in a Text file }
function fileReadChar(var tFile: Text): String;

var
  String word;

begin
  word = '';
  read(tFile, word);
  fileRead = word;
end;

{ Implements File API function fileReadInt by means of read in a typed file }
function fileReadInt(var iFile: LongIntFile): LongInt;

var
  LongInt value;

begin
  value = 0;
  read(iFile, value);
  fileReadInt = value;
end;

{ Implements File API function fileReadDouble by means of read in a typed file }
function fileReadDouble(var dFile: DoubleFile): Double;

var 
  Double value;

begin
  value = 0.0;
  read(dFile, value);
  fileReadDouble = value;
end;

{ Implements base function fileRead by means of readln in a Text file }
function fileReadLine(var tFile: Text): String

var
  String line;
  
begin
  line = '';
  readln(tFile, line);
  fileReadLine = line;
end;


BEGIN
END.
