/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#include "FileAPI.h"
#define _CRT_SECURE_NO_WARNINGS
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    File API for the CGenerator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016-12-22      First Issue
 *      Kay Gürtzig     2020-03-31      Several comments corrected
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - The API is not exactly equivalent to the Structorizer File API used in Executor.
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

#define FILE_API_MAX_HANDLES 20
/* Entry type for the file handle map */
struct StructorizerFileAPI_Entry {
	int fileNo;
	FILE* hFile;
};
struct StructorizerFileAPI_Map {
	unsigned int nHandles;
	int nextFileNo;
	struct StructorizerFileAPI_Entry handles[FILE_API_MAX_HANDLES];
} structorizerFileAPI_map = { 0, 1 };

/* Global buffer variable for File-API-internal purposes */
#define FILE_API_BUFFER_SIZE 1024
char structorizerFileAPI_buffer[FILE_API_BUFFER_SIZE];

/* Internal helper function of Structorizer File API, not for customer use! */
int structorizerFileAPI_getHandleIndex(int fileNo)
{
	int index = -1;
	int low = 0, high = structorizerFileAPI_map.nHandles;
	while (index < 0 && low < high) {
		int peek = (low + high) / 2;
		int refNo = structorizerFileAPI_map.handles[peek].fileNo;
		if (fileNo < refNo) { high = peek; }
		else if (fileNo > refNo) { low = peek + 1; }
		else { index = peek; }
	}
	return index;
}

/**
 * Tries to open a text file with given filePath for reading. File must exist.
 * A NULL return value indicates failure.
 * @param filePath - the path of the file (may be absolute or relative to the current directory)
 * @return a valid file handle (> 0) on success or a (negative) error code otherwise
 */
int fileOpen(char* filePath)
{
	struct StructorizerFileAPI_Entry entry = { 0, NULL };
	if (structorizerFileAPI_map.nHandles < FILE_API_MAX_HANDLES
		&& (entry.hFile = fopen(filePath, "r")) != NULL)
	{
		entry.fileNo = structorizerFileAPI_map.nextFileNo++;
		structorizerFileAPI_map.handles[structorizerFileAPI_map.nHandles++] = entry;
	}
	else {
		entry.fileNo = -errno;
	}
	return entry.fileNo;
}

/**
 * Tries to create a text file with given filePath for writing. If the file exists then it will
 * be cleared (without warning!).
 * @param filePath - the path of the file (may be absolute or relative to the current directory)
 * @return a valid file handle (> 0) on success or a (negative) error code otherwise
 */
int fileCreate(char* filePath)
{
	struct StructorizerFileAPI_Entry entry = { 0, NULL };
	if (structorizerFileAPI_map.nHandles < FILE_API_MAX_HANDLES
		&& (entry.hFile = fopen(filePath, "w")) != NULL)
	{
		entry.fileNo = structorizerFileAPI_map.nextFileNo++;
		structorizerFileAPI_map.handles[structorizerFileAPI_map.nHandles++] = entry;
	}
	else {
		entry.fileNo = -errno;
	}
	return entry.fileNo;
}

/**
 * Tries to create or open a text file with given filePath for writing. If the file exists then
 * it will not be cleared but writing starts at previous end.
 * @param filePath - the path of the file (may be absolute or relative to the current directory)
 * @return a valid file handle (> 0) on success or a (negative) error code otherwise
 */
int fileAppend(char* filePath)
{
	struct StructorizerFileAPI_Entry entry = { 0, NULL };
	if (structorizerFileAPI_map.nHandles < FILE_API_MAX_HANDLES
		&& (entry.hFile = fopen(filePath, "a")) != NULL)
	{
		entry.fileNo = structorizerFileAPI_map.nextFileNo++;
		structorizerFileAPI_map.handles[structorizerFileAPI_map.nHandles++] = entry;
	}
	else {
		entry.fileNo = -errno;
	}
	return entry.fileNo;
}

/**
 * Closes the file with given fileNo handle. If fileNo is not associated with an open file
 * then an IOException will be thrown.
 * @param fileNo - file handle as obtained by fileOpen, fileCreate or fileAppend before
 */
void fileClose(int fileNo)
{
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		fclose(structorizerFileAPI_map.handles[index].hFile);
		/* FIXME: The following should be performed as critical section */
		structorizerFileAPI_map.nHandles--;
		for (int i = index; i < structorizerFileAPI_map.nHandles; i++) {
			structorizerFileAPI_map.handles[i] = structorizerFileAPI_map.handles[i + 1];
		}
	}
	else {
		fprintf(stderr, "fileClose: Invalid file number or file not open for reading.");
		exit(-5);
	}
}

/**
 * Checks whether the input file with given hFie handle is exhausted i.e. provides no
 * readable content beyond the current reading position.
 * @param fileNo - file handle as obtained by fileOpen before.
 * @return true iff end of file has been reached.
 */
int fileEOF(int fileNo)
{
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index < 0) {
		fprintf(stderr, "fileEOF: Invalid file number or file not open for reading.");
		exit(-5);
	}
	else {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		int isEOF = feof(hFile);
		if (!isEOF) {
			int ch = getc(hFile);
			if (ch == EOF) {
				isEOF = 1;
			}
			else {
				ungetc(ch, hFile);
			}
		}
		return isEOF;
	}
	return 0;
}

/* Internal helper function of Structorizer File API, not for customer use! */
void* structorizerFileAPI_concatenateTokens(FILE* hFile, char rDelim, int length)
{
	void* pResult = structorizerFileAPI_buffer;
	char formatString[13];
	while (structorizerFileAPI_buffer[length - 1] != rDelim && length + 1 < FILE_API_BUFFER_SIZE) {
		structorizerFileAPI_buffer[length] = getc(hFile);
		if (structorizerFileAPI_buffer[length] == EOF || structorizerFileAPI_buffer[length] == rDelim) {
			structorizerFileAPI_buffer[length++] = rDelim;
			structorizerFileAPI_buffer[length] = '\0';
		}
		else {
			sprintf(formatString, "%%%ds", FILE_API_BUFFER_SIZE - (length + 1));
			if (fscanf(hFile, formatString, structorizerFileAPI_buffer + length + 1) > 0) {
				length = strlen(structorizerFileAPI_buffer);
			}
			else {
				structorizerFileAPI_buffer[length++] = rDelim;
				structorizerFileAPI_buffer[length] = '\0';
			}
		}
	}
	if (rDelim != '}') {
		pResult = structorizerFileAPI_buffer + 1;
		structorizerFileAPI_buffer[length - 1] = '\0';
	}

	return pResult;
}

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
void* fileRead(int fileNo)
{
	void* pResult = NULL;
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		char formatString[13];
		int nObtained = 0;
		sprintf(formatString, "%%%ds", FILE_API_BUFFER_SIZE);
		nObtained = fscanf(hFile, formatString, structorizerFileAPI_buffer);
		if (nObtained > 0) {
			/* Try to convert the obtained string... */
			int length = strlen(structorizerFileAPI_buffer);
			int iResult = 0;
			double dResult = 0.0;
			char cTest = '\0';
			int obtainedDbl = 0;
			int obtainedInt = 0;
			pResult = structorizerFileAPI_buffer;
			obtainedInt = sscanf(structorizerFileAPI_buffer, "%d", &iResult);
			if (obtainedInt > 0 && length > 1) {
				char lastCh = structorizerFileAPI_buffer[length - 1];
				structorizerFileAPI_buffer[length - 1] = '\0';
				if (atoi(structorizerFileAPI_buffer) == iResult) {
					// If the last digit doesn't play a role then it is hardly an integer
					obtainedInt = 0;
				}
				structorizerFileAPI_buffer[length - 1] = lastCh;
			}
			if (obtainedInt == 0 && (obtainedDbl = sscanf(pResult, "%lg%1c", &dResult, &cTest)) != 1) {
				obtainedDbl = 0;
			}
			pResult = structorizerFileAPI_buffer;
			if (obtainedInt == 1) {
				memcpy(pResult, &iResult, sizeof(int));
				structorizerFileAPI_buffer[sizeof(int)] = '\0';
			}
			else if (obtainedDbl == 1) {
				memcpy(pResult, &dResult, sizeof(double));
				structorizerFileAPI_buffer[sizeof(double)] = '\0';
			}
			else if (structorizerFileAPI_buffer[0] == '"') {
				pResult = structorizerFileAPI_concatenateTokens(hFile, '"', length);
			}
			else if (structorizerFileAPI_buffer[0] == '\'') {
				pResult = structorizerFileAPI_concatenateTokens(hFile, '\'', length);
			}
			else if (structorizerFileAPI_buffer[0] == '{') {
				pResult = structorizerFileAPI_concatenateTokens(hFile, '}', length);
			}
		}
	}
	else {
		fprintf(stderr, "fileRead: Invalid file number or file not open for reading.");
		exit(-5);
	}
	return pResult;
}

/**
 * Reads the next character from the text file given by fileNo handle and returns it.
 * Throws an error if the given handle is not associated to an open text input file.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return the current character from file input character sequence or '\0'.
 */
int fileReadChar(int fileNo)
{
	char ch = '\0';
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		ch = getc(hFile);
		if (ch == EOF) {
			ch = '\0';
		}
	}
	else {
		fprintf(stderr, "fileReadChar: Invalid file number or file not open for reading.");
		exit(-5);
	}
	return ch;
}

/**
 * Reads the next integer value from the text file given by fileNo handle and returns it.
 * Throws an error if the given handle is not associated to an open text input file.
 * If the file input stream was exhausted (was at end of file) or if the token at reading
 * was not interpretable as integral literal, then the program will be aborted with code EOF.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return the current int number as interpreted from the file input character sequence.
 */
int fileReadInt(int fileNo)
{
	int result = 0;
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		int nObtained = fscanf(hFile, "%d", &result);
		if (nObtained < 1) {
			fprintf(stderr, "fileReadInt: No int value readable from file!");
			exit(EOF);
		}
	}
	else {
		fprintf(stderr, "fileReadInt: Invalid file number or file not open for reading.");
		exit(-5);
	}
	return result;
}

/**
 * Reads the next floating-point value from the text file given by fileNo handle and
 * returns it as double.
 * Throws an error if the given handle is not associated to an open text input file.
 *  If the file input stream was exhausted (was at end of file) or if the token at reading
 * was not interpretable as double literal, then the program will be aborted with code EOF.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return the current floating-point value from the file input character sequence.
 */
double fileReadDouble(int fileNo)
{
	double result = 0;
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		int nObtained = fscanf(hFile, "%lg", &result);
		if (nObtained < 1) {
			fprintf(stderr, "fileReadDouble: No double value readable from file!");
			exit(EOF);
		}
	}
	else {
		fprintf(stderr, "fileReadDouble: Invalid file number or file not open for reading.");
		exit(-5);
	}
	return result;
}

/**
 * Reads the next text line (or the rest of the current text line) from the text file
 * given by fileNo handle and returns it.
 * Throws an error if the given handle is not associated to an open text input file.
 * If the file input stream was exhausted (was at end of file) or if the token at reading
 * was not interpretable as integral literal then null will be returned.
 * @param fileNo - file handle as obtained by fileOpen before
 * @return the current line from file or NULL.
 */
char* fileReadLine(int fileNo)
{
	char* pResult = NULL;
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		pResult = fgets(structorizerFileAPI_buffer, FILE_API_BUFFER_SIZE, hFile);
	}
	else {
		fprintf(stderr, "fileReadLine: Invalid file number or file not open for reading.");
		exit(-5);
	}
	return pResult;
}

/**
 * Writes the given C-string to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param str the string to be written to file.
 */
void fileWrite(int fileNo, char* str)
{
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		fprintf(hFile, "%s", str);
	}
	else {
		fprintf(stderr, "fileWrite: Invalid file number or file not open for writing.");
		exit(-5);
	}
}

/**
 * Writes the given value as character sequence to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param value the integer value to be written to file.
 */
void fileWriteInt(int fileNo, int value)
{
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		fprintf(hFile, "%d", value);
	}
	else {
		fprintf(stderr, "fileWriteInt: Invalid file number or file not open for writing.");
		exit(-5);
	}
}

/**
 * Writes the given value as character sequence to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param value the double value to be written to file.
 */
void fileWriteDouble(int fileNo, double value)
{
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		fprintf(hFile, "%g", value);
	}
	else {
		fprintf(stderr, "fileWriteDouble: Invalid file number or file not open for writing.");
		exit(-5);
	}
}

/**
 * Writes the given character sequence to the file given by handle fileNo
 * and appends a newline character or sequence as value separator.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param str the C-String to be written to file.
 */
void fileWriteLine(int fileNo, char* str)
{
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		fprintf(hFile, "%s\n", str);
	}
	else {
		fprintf(stderr, "fileWriteLine: Invalid file number or file not open for writing.");
		exit(-5);
	}
}

/**
 * Writes the given value as character sequence to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param value the integer value to be written to file.
 */
void fileWriteLineInt(int fileNo, int value)
{
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		fprintf(hFile, "%d\n", value);
	}
	else {
		fprintf(stderr, "fileWriteLineInt: Invalid file number or file not open for writing.");
		exit(-5);
	}
}

/**
 * Writes the given value as character sequence to the file given by handle fileNo.
 * Throws an error if the given handle is not associated to an open text output file.
 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
 * @param value the double value to be written to file.
 */
void fileWriteLineDouble(int fileNo, double value)
{
	int index = structorizerFileAPI_getHandleIndex(fileNo);
	if (index >= 0) {
		FILE* hFile = structorizerFileAPI_map.handles[index].hFile;
		fprintf(hFile, "%g\n", value);
	}
	else {
		fprintf(stderr, "fileWriteLineDouble: Invalid file number or file not open for writing.");
		exit(-5);
	}
}
