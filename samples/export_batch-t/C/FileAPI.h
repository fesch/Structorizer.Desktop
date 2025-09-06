#ifndef FILEAPI_H
#define FILEAPI_H
/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#define _CRT_SECURE_NO_WARNINGS

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    File API header for the CGenerator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-03-23      First Issue (for enh. #828, group export)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - The API is not exactly equivalent to the Structorizer File API used in Executor
 *      - First, the fileRead function returns a pointer rather than object references. This
 *        pointer is of void* type such that it must first be casted to some meaningful
 *        pointer type in order to fetch the variable value. Unfortunately, there is no
 *        safe way to find out its type. So the user should not make assumptions here but
 *        have a look into the available files and adhere to their structure.
 *      - Next, the simple fileWrite and fileWriteLine routines had to be split into several
 *        argument-type-specific file output functions (e.g. fileWriteInt, fileWriteDouble),
 *        whereas the functions fileWrite and fileWriteLine are solely reserved for C-string
 *        output.
 *
 ******************************************************************************************************///

/**
 * Tries to open a text file with given filePath for reading. File must exist.
 * A NULL return value indicates failure.
 * @param filePath - the path of the file (may be absolute or relative to the current directory)
 * @return a valid file handle (> 0) on success or a (negative) error code otherwise
 */
int fileOpen(char* filePath);

/**
 * Tries to create a text file with given filePath for writing. Is file exists then it will
 * be cleared (without warning!).
 * A NULL return value indicates failure.
 * @param filePath - the path of the file (may be absolute or relative to the current directory)
 * @return a valid file handle (> 0) on success or a (negative) error code otherwise
 */
int fileCreate(char* filePath);

/**
 * Tries to create or open a text file with given filePath for writing. If the file exists then
 * it will not be cleared but writing starts at previous end.
 * A NULL return value indicates failure.
 * @param filePath - the path of the file (may be absolute or relative to the current directory
 * @return a valid file handle (> 0) on success or a (negative) error code otherwise
 */
int fileAppend(char* filePath);

/**
 * Closes the file with given fileNo handle. If fileNo is not associated with an open file
 * then an IOException will be thrown.
 * @param fileNo - file handle as obtained by fileOpen, fileCreate or fileAppend before
 */
void fileClose(int fileNo);

/**
 * Checks whether the input file with given hFie handle is exhausted i.e. provides no
 * readable content beyond the current reading position.
 * @param fileNo - file handle as obtained by fileOpen before.
 * @return true iff end of file has been reached.
 */
int fileEOF(int fileNo);

/**
 * CAUTION: This function returns either NULL or a pointer to the value read from file,
 * never the value itself!
 * In theory, this function should read the next token from the text file given by
 * fileNo handle and return it in the appropriate type.
 * Unfortunately, such a behaviour isn't possible in C. We cannot even return a
 * C-String without being provided with a buffer as argument (unless we were allowed
 * to allocate dynamic memory here).
 * So, this functions scans the file for a token and reads it into a fixed global
 * buffer. Then it tries to convert the content into int and double. If any of these
 * conversions succeeds and is unique then the converted value will be placed at the
 * buffer beginning such that dereferencing the returned pointer will provide the
 * value. It must immediately be copied to a variable because the buffer is volatile.
 * Throws an error if the given handle is not associated to an open text input file.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return POINTER TO an int or double variable or a C-string - to be casted appropriately -, or NULL
 */
void* fileRead(int fileNo);

/**
 * Reads the next character from the text file given by fileNo handle and returns it.
 * Throws an error if the given handle is not associated to an open text input file.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return the current character from file input character sequence or '\0'.
 */
int fileReadChar(int fileNo);

/**
 * Reads the next integer value from the text file given by fileNo handle and returns it.
 * Throws an error if the given handle is not associated to an open text input file.
 * If the file input stream was exhausted (was at end of file) or if the token at reading
 * was not interpretable as integral literal, then the program will be aborted with code EOF.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return the current int number as interpreted from file input character sequence.
 */
int fileReadInt(int fileNo);

/**
 * Reads the next floating-point value from the text file given by fileNo handle and
 * returns it as double.
 * Throws an error if the given handle is not associated to an open text input file.
 *  If the file input stream was exhausted (was at end of file) or if the token at reading
 * was not interpretable as double literal, then the program will be aborted with code EOF.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return the current floating point-value from file input character sequence.
 */
double fileReadDouble(int fileNo);

/**
 * Reads the next text line (or the rest of the current text line) from the text file
 * given by fileNo handle and returns it.
 * Throws an error if the given handle is not associated to an open text input file.
 * If the file input stream was exhausted (was at end of file) or if the token at reading
 * was not interpretable as integral literal then null will be returned.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return the current line from file or NULL.
 */
char* fileReadLine(int fileNo);

/**
 * Writes the given C-string to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param str the string to be written to file.
 */
void fileWrite(int fileNo, char* str);

/**
 * Writes the given value as character sequence to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param value the integer value to be written to file.
 */
void fileWriteInt(int fileNo, int value);

/**
 * Writes the given value as character sequence to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param value the double value to be written to file.
 */
void fileWriteDouble(int fileNo, double value);

/**
 * Writes the given character sequence to the file given by handle fileNo
 * and appends a newline character or sequence as value separator.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param str the C-String to be written to file.
 */
void fileWriteLine(int fileNo, char* str);

/**
 * Writes the given value as character sequence to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param value the integer value to be written to file.
 */
void fileWriteLineInt(int fileNo, int value);

/**
 * Writes the given value as character sequence to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param value the double value to be written to file.
 */
void fileWriteLineDouble(int fileNo, double value);
#endif /*FILEAPI_H*/
