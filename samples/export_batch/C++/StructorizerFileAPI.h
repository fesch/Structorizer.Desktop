#pragma once
#ifndef STRUCTORIZERFILEAPI_HPP
#define STRUCTORIZERFILEAPI_HPP
/*
   Structorizer
   A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

   Copyright (C) 2009  Bob Fisch

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or any
   later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <string>
#include <fstream>
#include <vector>

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    File API header for the CPlusPlusGenerator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016-12-23      First Issue
 *      Kay Gürtzig     2020-03-23      Include guard symbol name modified from "..._H" to "..._HPP"
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - The File API of Structorizer is implemented as static methods on this non-instantiable class
 *
 ******************************************************************************************************///
class StructorizerFileAPI
{
public:
	class FileAPI_exception : public std::runtime_error {
	public:
		FileAPI_exception(const char description[])
		: runtime_error(description)
		{}
	};
	// Adapter class for auto-typed values read from a file. It implements automatic conversion
	// to int, double, string or array values like in Structorizer.
	// It also implements test methods in order to find out what structure the value actually
	// represents
	class FileAPI_value {
		friend class StructorizerFileAPI;
	public:
		// Converts the content into an int value if possible or 0 otherwise
		operator int() const;
		// Converts the content into a double value if possible or 0.0 otherwise
		operator double() const;
		// Converts the content into a string
		operator const std::string() const;
		// Returns true if this represents any value at all (i.e. unless nothing was obtained from file)
		operator bool() const;
		// Returns true iff the value represents an integral number
		bool isInt() const;
		// Returns true iff the value represents a floating-point number
		bool isDouble() const;
		// Returns true if the read character sequence may only be interpreted as string
		bool isString() const;
	private:
		double valDbl;
		int valInt;
		enum Type { FAV_INT, FAV_DBL, FAV_STR, FAV_VOID } type;
		std::string valStr;
		FileAPI_value();
		FileAPI_value(std::string value);
		bool append(std::string next);
		bool isComplete() const;
	};

	~StructorizerFileAPI();

	/**
	* Tries to open a text file with given filePath for reading. File must exist.
	* A negative or zero return value indicates failure.
	* @param filePath - the path of the file (may be absolute or relative to the current directory)
	* @return an int to be used as file handle for this API if > 0 or as error code otherwise.
	*/
	static int fileOpen(const std::string& filePath);

	/**
	* Tries to create a text file with given filePath for writing. Is file exists then it will
	* be cleared (without warning!).
	* A negative or zero return value indicates failure.
	* @param filePath - the path of the file (may be absolute or relative to the current directory)
	* @return an int to be used as file handle for this API if > 0 or as error code otherwise.
	*/
	static int fileCreate(const std::string& filePath);

	/**
	* Tries to create or open a text file with given filePath for writing. If the file had existed
	* then it will not be cleared but writing starts at previous end.
	* A negative or zero return value indicates failure.
	* @param filePath - the path of the file (may be absolute or relative to the current directory)
	* @return an int to be used as file handle for this API if > 0 or as error code otherwise.
	*/
	static int fileAppend(const std::string& filePath);

	/**
	* Closes the file with given fileNo handle. If fileNo is not associated with an open file
	* then an IOException will be thrown.
	* @param fileNo - file handle as obtained by fileOpen, fileCreate or fileAppend before
	*/
	static void fileClose(int fileNo);

	/**
	* Checks whether the input file with given fileNo handle is exhausted i.e. provides no
	* readable content beyond the current reading position.
	* If fileNo is not associated to any opened readable file then an IOException will be thrown.
	* @param fileNo - file handle as obtained by fileOpen before.
	* @return true iff end of file has been reached.
	*/
	static bool fileEOF(int fileNo);

	/**
	* Reads the next token from the text file given by fileNo handle and returns it as an
	* appropriate FileAPI_value implicitly convertible to int, double, or std::string.
	* Throws an error if the given handle is not associated to an open text input file.
	* The result may also be interpreted as bool value in which case false means that the
	* value could not be interpreted as any of the known types.
	* @param fileNo - file handle as obtained by fileOpen before
	* @return wrapper object for the current value as interpreted from file input.
	*/
	static FileAPI_value fileRead(int fileNo);

	/**
	* Reads the next char from the text file given by fileNo handle and returns it.
	* Throws an error if the given handle is not associated to an open text input file.
	* @param fileNo - file handle as obtained by fileOpen before
	* @return the current char from file input char sequence or '\0'.
	*/
	static char fileReadChar(int fileNo);

	/**
	* Reads the next int value from the text file given by fileNo handle and returns it.
	* Throws an error if the given handle is not associated to an open text input file.
	* If the file input stream was exhausted (was at end of file) or if the token at reading
	* position was not interpretable as integral literal, then null will be returned.
	* @param fileNo - file handle as obtained by fileOpen before
	* @return the current int number as interpreted from file input char sequence or null.
	*/
	static int fileReadInt(int fileNo);

	/**
	* Reads the next floating-point value from the text file given by fileNo handle and
	* returns it as double.
	* Throws an error if the given handle is not associated to an open text input file.
	* If the file input stream was exhausted (was at end of file) or if the token at reading
	* position was not interpretable as floating-point literal then null will be returned.
	* @param fileNo - file handle as obtained by fileOpen before
	* @return the current floating point-value from file input char sequence or null.
	*/
	static double fileReadDouble(int fileNo);

	/**
	* Reads the next text line (or the rest of the current text line) from the text file
	* given by fileNo handle and returns it.
	* Throws an error if the given handle is not associated to an open text input file.
	* If the file input stream was exhausted (was at end of file) or if the token at reading
	* position was not interpretable as integral literal then null will be returned.
	* @param fileNo - file handle as obtained by fileOpen before
	* @return the current line from file input or null.
	*/
	static std::string fileReadLine(int fileNo);

	/**
	* Writes the given value as char sequence to the file given by handle fileNo.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWrite(int fileNo, int data);
	/**
	* Writes the given value as char sequence to the file given by handle fileNo.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWrite(int fileNo, double data);
	/**
	* Writes the given value as char sequence to the file given by handle fileNo.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWrite(int fileNo, std::string data);
	/**
	* Writes the given value as char sequence to the file given by handle fileNo.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWrite(int fileNo, char data);
	/**
	* Writes the given value as char sequence to the file given by handle fileNo.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWrite(int fileNo, bool data);

	/**
	* Writes the given value as char sequence to the file given by handle fileNo
	* and appends a newline char or sequence as value separator.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWriteLine(int fileNo, int data);
	/**
	* Writes the given value as char sequence to the file given by handle fileNo
	* and appends a newline char or sequence as value separator.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWriteLine(int fileNo, double data);
	/**
	* Writes the given value as char sequence to the file given by handle fileNo
	* and appends a newline char or sequence as value separator.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWriteLine(int fileNo, std::string data);
	/**
	* Writes the given value as char sequence to the file given by handle fileNo
	* and appends a newline char or sequence as value separator.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWriteLine(int fileNo, char data);
	/**
	* Writes the given value as char sequence to the file given by handle fileNo
	* and appends a newline char or sequence as value separator.
	* Throws an error if the given handle is not associated to an open text output file.
	* @param fileNo - file handle as obtained by fileCreate or fileAppend before
	* @param data the value to be written to file.
	*/
	static void fileWriteLine(int fileNo, bool data);

private:
	StructorizerFileAPI();

	class FileMapEntry {
	public:
		FileMapEntry(int fileNo, const std::string& filePath, std::ios_base::openmode mode);
		int fileNo;
		std::fstream strm;
		bool forOutput;
	};
	class FileMap {
	public:
		FileMap();
		std::fstream& getFile(int fileNo, std::ios_base::openmode mode = 0) const;
		int addFile(const std::string& filePath, std::ios_base::openmode mode);
		bool removeFile(int fileNo);
	private:
		unsigned int nFiles;
		std::vector<FileMapEntry*> entries;
		std::vector<FileMapEntry*>::const_iterator getIterOf(int fileNo) const;
	};
	/**
	* Maps the file numbers to stream writers or scanners. For File-API-internal use only
	*/
	static FileMap fileMap;

};

template <class _Elem, class _Traits>
std::basic_ostream<_Elem, _Traits>& operator<<(std::basic_ostream<_Elem, _Traits>& stream, const StructorizerFileAPI::FileAPI_value& value)
{
	return stream << (const std::string)value;
}

#endif /*STRUCTORIZERFILEAPI_HPP*/
