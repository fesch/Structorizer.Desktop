﻿/*
	Structorizer
	A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

	Copyright (C) 2009, 2020  Bob Fisch

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

using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
 
/******************************************************************************************************
 *
 *	  Author:		Kay Gürtzig
 *
 *	  Description:	File API for the CSharpGenerator.
 *
 ******************************************************************************************************
 *
 *	  Revision List
 *
 *	  Author          Date        Description
 *	  ------          ----        -----------
 *	  Kay Gürtzig     2017-01-04  First Issue
 *    Kay Gürtzig     2017-01-05  Functional improvements (in StructorizerFileAPI class)
 *
 ******************************************************************************************************
 *
 *	  Comment:
 *	  
 *
 ******************************************************************************************************/// 

namespace FileAPI.CS
{
	class StructorizerFileAPI
	{
		// Map of open stream readers
		private static Dictionary<int, StreamReader> readers = new Dictionary<int, StreamReader>();
		// Map of open stream writers
		private static Dictionary<int, StreamWriter> writers = new Dictionary<int, StreamWriter>();
		// Map of the current lines of the respective open stream readers
		private static Dictionary<int, string> lines = new Dictionary<int, string>();
		// Next usable file number (handle)
		private static int nextFileNo = 1;

		/**
		 * Tries to open a text file with given filePath for reading. File must exist.
		 * A negative or zero return value indicates failure.
		 * @param filePath - the path of the file (may be absolute or relative to the current directory)
		 * @return an int to be used as file handle for this API if > 0 or as error code otherwise.
		 */
		public static int fileOpen(string filePath)
		{
			int fileNo = 0;
			try {
				StreamReader reader = new StreamReader(filePath);
				fileNo = nextFileNo;
				readers.Add(nextFileNo++, reader);
			}
			catch (FileNotFoundException) {
				fileNo = -2;
			}
			catch (IOException) {
				fileNo = -1;
			} 
			return fileNo;
		}

		/**
		 * Tries to create a text file with given filePath for writing. Is file exists then it will
		 * be cleared (without warning!).
		 * A negative or zero return value indicates failure.
		 * @param filePath - the path of the file (may be absolute or relative to the current directory)
		 * @return an int to be used as file handle for this API if > 0 or as error code otherwise.
		 */
		public static int fileCreate(string filePath)
		{
			int fileNo = 0;
			try {
				StreamWriter writer = new StreamWriter(filePath);
				fileNo = nextFileNo;
				writers.Add(nextFileNo++, writer);
			}
			catch (UnauthorizedAccessException) {
				fileNo = -3;
			}
			catch (DirectoryNotFoundException) {
				fileNo = -2;
			}
			catch (IOException) {
				fileNo = -1;
			} 
			return fileNo;
		}

		/**
		 * Tries to create or open a text file with given filePath for writing. If the file had existed
		 * then it will not be cleared but writing starts at previous end.
		 * A negative or zero return value indicates failure.
		 * @param filePath - the path of the file (may be absolute or relative to the current directory)
		 * @return an int to be used as file handle for this API if > 0 or as error code otherwise.
		 */
		public static int fileAppend(string filePath)
		{
			int fileNo = 0;
			try {
				StreamWriter writer = new StreamWriter(filePath, true);
				fileNo = nextFileNo;
				writers.Add(nextFileNo++, writer);
			}
			catch (UnauthorizedAccessException) {
				fileNo = -3;
			}
			catch (DirectoryNotFoundException) {
				fileNo = -2;
			}
			catch (IOException) {
				fileNo = -1;
			} 
			return fileNo;
		}

		/**
		 * Closes the file with given fileNo handle. If fileNo is not associated with an open file
		 * then an IOException will be thrown.
		 * @param fileNo - file handle as obtained by fileOpen, fileCreate or fileAppend before
		 */
		public static void fileClose(int fileNo)
		{
			if (readers.ContainsKey(fileNo)) {
				readers[fileNo].Close();
				readers.Remove(fileNo);
			}
			else if (writers.ContainsKey(fileNo)) {
				writers[fileNo].Close();
				writers.Remove(fileNo);
			}
		}

		/**
		 * Checks whether the input file with given fileNo handle is exhausted i.e. provides no
		 * readable content beyond the current reading position.
		 * If fileNo is not associated to any opened readable file then an IOException will be thrown.
		 * @param fileNo - file handle as obtained by fileOpen before.
		 * @return true iff end of file has been reached.
		 */
		public static bool fileEOF(int fileNo)
		{
			bool isEOF = true;
			if (readers.ContainsKey(fileNo)) {
				isEOF = fetchLine(fileNo, true) == null;
			}
			return isEOF;
		}

		/**
		 * Internal helper method.
		 * Provides the current text line (or the rest of the current text line) from the
		 * text file given by fileNo handle without "consuming" it.
		 * May throw an error if the given handle is not associated to an open text input file.
		 * If the file input stream was exhausted (i.e. is at end of file) then null will
		 * be returned.
		 * @param fileNo - file handle as obtained by fileOpen before
		 * @return the current line from file input or null.
		 */
		private static string fetchLine(int fileNo, bool yieldEmptyLine)
		{
			string line = null;
			if (readers.ContainsKey(fileNo))
			{
				if (lines.ContainsKey(fileNo))
				{
					line = lines[fileNo];
				}
				if (line == null || line.Length == 0 && !yieldEmptyLine)
				{
					if (readers[fileNo].EndOfStream)
					{
						line = null;
						lines.Remove(fileNo);
					}
					else
					{
						try
						{
							line = readers[fileNo].ReadLine();
							lines[fileNo] = line;
						}
						catch (IOException)
						{
							line = null;
						}
					}
				}
			}
			return line;
		}

		/**
		 * Reads the next token from the text file given by fileNo handle and returns it as
		 * boxed value of one of the types int, double, or string.
		 * Throws an error if the given handle is not associated to an open text input file.
		 * The result may also be interpreted as bool value in which case false means that the
		 * value could not be interpreted as any of the known types.
		 * @param fileNo - file handle as obtained by fileOpen before
		 * @return wrapper object for the current value as interpreted from file input.
		 */
		public static object fileRead(int fileNo)
		{
			object value = null;
			string line = null;
			do
			{
				line = fetchLine(fileNo, false);
			} while (line != null && line.Trim().Length == 0);
			if (line != null)
			{
				line = line.TrimStart();
				string[] tokens = line.Split(null);
				int intVal;
				double dblVal;
				if (Int32.TryParse(tokens[0], out intVal)) {
					value = intVal;
					line = line.Substring(tokens[0].Length);
				}
				else if (Double.TryParse(tokens[0], out dblVal)) {
					value = dblVal;
					line = line.Substring(tokens[0].Length);
				}
				else {
					string strVal = tokens[0];
					line = line.Substring(tokens[0].Length);
					if (strVal.StartsWith("\"") || strVal.StartsWith("'")) {
						string delimiter = strVal.Substring(0,1);
						for (int i = 1; i < tokens.Length && !(strVal.Length > 1 && strVal.EndsWith(delimiter)); i++) {
							int start = line.IndexOf(tokens[i]);
							strVal += line.Substring(0, start) + tokens[i];
							line = line.Substring(start + tokens[i].Length);
						}
						strVal = strVal.Substring(1);
						if (strVal.EndsWith(delimiter)) {
							strVal = strVal.Substring(0, strVal.Length-1);
						}
					}
					value = strVal;
				}
				if (value != null)
				{
					lines[fileNo] = line;
				}
			}
			return value;
		}

		/**
		 * Reads the next char from the text file given by fileNo handle and returns it.
		 * Throws an error if the given handle is not associated to an open text input file.
		 * @param fileNo - file handle as obtained by fileOpen before
		 * @return the current char from file input char sequence or '\0'.
		 */
		public static char fileReadChar(int fileNo)
		{
			char ch = '\0';
			string line = fetchLine(fileNo, true);
			if (line != null)
			{
				if (line.Length > 0)
				{
					ch = line[0];
					lines[fileNo] = line.Substring(1);
				}
				else if ((line = fetchLine(fileNo, false)) != null)
				{
					ch = '\n';
				}
			}
			return ch;
		}

		/**
		 * Reads the next int value from the text file given by fileNo handle and returns it.
		 * Throws an error if the given handle is not associated to an open text input file.
		 * If the file input stream was exhausted (was at end of file) or if the token at reading
		 * position was not interpretable as integral literal, then null will be returned.
		 * @param fileNo - file handle as obtained by fileOpen before
		 * @return the current int number as interpreted from file input char sequence or null.
		 */
		public static int fileReadInt(int fileNo)
		{
			int value = 0;
			string line = null;
			do
			{
				line = fetchLine(fileNo, false);
			} while (line != null && line.TrimStart().Length == 0);
			if (line != null)
			{
				line = line.TrimStart();
				string[] tokens = line.Split(null);
				value = Int32.Parse(tokens[0]);
				lines[fileNo] = line.Substring(tokens[0].Length);
			}
			return value;
		}

		/**
		 * Reads the next floating-point value from the text file given by fileNo handle and
		 * returns it as double.
		 * Throws an error if the given handle is not associated to an open text input file.
		 * If the file input stream was exhausted (was at end of file) or if the token at reading
		 * position was not interpretable as floating-point literal then null will be returned.
		 * @param fileNo - file handle as obtained by fileOpen before
		 * @return the current floating point-value from file input char sequence or null.
		 */
		public static double fileReadDouble(int fileNo)
		{
			double value = 0;
			string line = null;
			do
			{
				line = fetchLine(fileNo, false);
			} while (line != null && line.TrimStart().Length == 0);
			if (line != null)
			{
				line = line.TrimStart();
				string[] tokens = line.Split(null);
				value = Double.Parse(tokens[0]);
				lines[fileNo] = line.Substring(tokens[0].Length);
			}
			return value;
		}

		/**
		 * Reads the next text line (or the rest of the current text line) from the text file
		 * given by fileNo handle and returns it. This consumes the line.
		 * May throw an error if the given handle is not associated to an open text input file.
		 * If the file input stream had been exhausted (is at end of file) then null will be
		 * returned.
		 * @param fileNo - file handle as obtained by fileOpen before
		 * @return the current line from file input or null.
		 */
		public static string fileReadLine(int fileNo)
		{
			string line = fetchLine(fileNo, true);
			if (line != null)
			{
				// Line is consumed now
				lines.Remove(fileNo);
			}
			return line;
		}

		/**
		 * Writes the given value as char sequence to the file given by handle fileNo.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWrite(int fileNo, int data)
		{
			writers[fileNo].Write(data);
		}
		/**
		 * Writes the given value as char sequence to the file given by handle fileNo.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWrite(int fileNo, double data)
		{
			writers[fileNo].Write(data);
		}
		/**
		 * Writes the given value as char sequence to the file given by handle fileNo.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWrite(int fileNo, string data)
		{
			writers[fileNo].Write(data);
		}
		/**
		 * Writes the given value as char sequence to the file given by handle fileNo.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWrite(int fileNo, char data)
		{
			writers[fileNo].Write(data);
		}
		/**
		 * Writes the given value as char sequence to the file given by handle fileNo.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWrite(int fileNo, bool data)
		{
			writers[fileNo].Write(data);
		}

		/**
		 * Writes the given value as char sequence to the file given by handle fileNo
		 * and appends a newline char or sequence as value separator.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWriteLine(int fileNo, int data)
		{
			writers[fileNo].WriteLine(data);
		}
		/**
		 * Writes the given value as char sequence to the file given by handle fileNo
		 * and appends a newline char or sequence as value separator.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWriteLine(int fileNo, double data)
		{
			writers[fileNo].WriteLine(data);
		}
		/**
		 * Writes the given value as char sequence to the file given by handle fileNo
		 * and appends a newline char or sequence as value separator.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWriteLine(int fileNo, string data)
		{
			writers[fileNo].WriteLine(data);
		}
		/**
		 * Writes the given value as char sequence to the file given by handle fileNo
		 * and appends a newline char or sequence as value separator.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWriteLine(int fileNo, char data)
		{
			writers[fileNo].WriteLine(data);
		}
		/**
		 * Writes the given value as char sequence to the file given by handle fileNo
		 * and appends a newline char or sequence as value separator.
		 * Throws an error if the given handle is not associated to an open text output file.
		 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
		 * @param data the value to be written to file.
		 */
		public static void fileWriteLine(int fileNo, bool data)
		{
			writers[fileNo].WriteLine(data);
		}

	}

}
