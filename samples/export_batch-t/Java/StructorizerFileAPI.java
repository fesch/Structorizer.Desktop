/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

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
 
 package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    File API for the JavaGenerator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     22.12.2016      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///
 
 public class StructorizerFileAPI 
 {

	/**
	 * Maps the file numbers to stream writers or scanners. For File-API-internal use only 
	 */
	private static java.util.Vector<java.io.Closeable> structorizerFileMap = new java.util.Vector<java.io.Closeable>(); 

	/**
	 * Tries to open a text file with given filePath for reading. File must exist.
	 * A negative or zero return value indicates failure. Failure code explanation:
	 * 0,-1: unspecific error
	 * -2: file not found
	 * -3: insufficient permissions
	 * @param filePath - the path of the file (may be absolute or relative to the current directory)
	 * @return an integer to be used as file handle for this API if > 0 or as error code otherwise.
	 */
	public static int fileOpen(String filePath)
	{
		int fileNo = 0; 
		java.io.File file = new java.io.File(filePath);
		try {
			java.io.FileInputStream fis = new java.io.FileInputStream(file);
			java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis, "UTF-8"));
			fileNo = structorizerFileMap.size() + 1;
			structorizerFileMap.add(new java.util.Scanner(reader));
		}
		catch (SecurityException e) { fileNo = -3; }
		catch (java.io.FileNotFoundException e) { fileNo = -2; }
		catch (java.io.IOException e) { fileNo = -1; }
		return fileNo;
	}

	/**
	 * Tries to create a text file with given filePath for writing. Is file exists then it will
	 * be cleared (without warning!).
	 * A negative or zero return value indicates failure. Failure code explanation:
	 * 0,-1: unspecific error
	 * -2: path to create the file within not found
	 * -3: insufficient permissions
	 * @param filePath - the path of the file (may be absolute or relative to the current directory)
	 * @return an integer to be used as file handle for this API if > 0 or as error code otherwise.
	 */
	public static int fileCreate(String filePath)
	{
		int fileNo = 0;
		java.io.File file = new java.io.File(filePath);
		try {
			java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
			java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, "UTF-8"));
			fileNo = structorizerFileMap.size() + 1;
			structorizerFileMap.add(writer);
		}
		catch (SecurityException e) { fileNo = -3; }
		catch (java.io.FileNotFoundException e) { fileNo = -2; }
		catch (java.io.IOException e) { fileNo = -1; }
		return fileNo;
	}

	/**
	 * Tries to create or open a text file with given filePath for writing. If the file exists then
	 * it will not be cleared but writing starts at previous end.
	 * A negative or zero return value indicates failure. Failure code explanation:
	 * 0,-1: unspecific error
	 * -2: path to create the file within not found
	 * -3: insufficient permissions
	 * @param filePath - the path of the file (may be absolute or relative to the current directory)
	 * @return an integer to be used as file handle for this API if > 0 or as error code otherwise.
	 */
	public static int fileAppend(String filePath)
	{
		int fileNo = 0;
		java.io.File file = new java.io.File(filePath);
		try {
			java.io.FileOutputStream fos = new java.io.FileOutputStream(file, true);
			java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, "UTF-8"));
			fileNo = structorizerFileMap.size() + 1;
			structorizerFileMap.add(writer);
		}
		catch (SecurityException e) { fileNo = -3; }
		catch (java.io.FileNotFoundException e) { fileNo = -2; }
		catch (java.io.IOException e) { fileNo = -1; }
		return fileNo;
	}

	/**
	 * Closes the file with given fileNo handle. If fileNo is not associated with an open file
	 * then an IOException will be thrown. 
	 * @param fileNo - file handle as obtained by fileOpen, fileCreate or fileAppend before
	 */
	public static void fileClose(int fileNo) throws java.io.IOException
	{
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable file = structorizerFileMap.get(fileNo - 1);
			if (file != null) {
				try { file.close(); }
				catch (java.io.IOException e) {}
				structorizerFileMap.set(fileNo - 1, null); }
		}
		else { throw new java.io.IOException("fileClose: Invalid file number or file not open for reading."); }
	}

	/**
	 * Checks whether the input file with given fileNo handle is exhausted i.e. provides no
	 * readable content beyond the current reading position. 
	 * If fileNo is not associated to any opened readable file then an IOException will be thrown. 
	 * @param fileNo - file handle as obtained by fileOpen before.
	 * @return true iff end of file has been reached.
	 */
	public static boolean fileEOF(int fileNo) throws java.io.IOException
	{
		boolean isEOF = true;
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable reader = structorizerFileMap.get(fileNo - 1);
			if (reader instanceof java.util.Scanner) {
				//try {
					isEOF = !((java.util.Scanner)reader).hasNext();
				//} catch (java.io.IOException e) {}
			}
		}
		else { throw new java.io.IOException("fileEOF: Invalid file number or file not open for reading."); }
		return isEOF;
	}

	/* Internal helper function for file reading */
	private static Object structorizerGetScannedObject(java.util.Scanner sc) {
		Object result = null;
		sc.useLocale(java.util.Locale.UK);
		if (sc.hasNextInt()) { result = sc.nextInt(); }
		else if (sc.hasNextDouble()) { result = sc.nextDouble(); }
		else if (sc.hasNext("\\\".*?\\\"")) {
			String str = sc.next("\\\".*?\\\"");
			result = str.substring(1, str.length() - 1);
		}
		else if (sc.hasNext("'.*?'")) {
			String str = sc.next("'.*?'");
			result = str.substring(1, str.length() - 1);
		}
		else if (sc.hasNext("\\{.*?\\}")) {
			String token = sc.next();
			result = new Object[]{token.substring(1, token.length()-1)};
		}
		else if (sc.hasNext("\\\".*")) {
			String str = sc.next();
			while (sc.hasNext() && !sc.hasNext(".*\\\"")) {
				str += " " + sc.next();
			}
			if (sc.hasNext()) { str += " " + sc.next(); }
			result = str.substring(1, str.length() - 1);
		}
		else if (sc.hasNext("'.*")) {
			String str = sc.next();
			while (sc.hasNext() && !sc.hasNext(".*'")) {
				str += " " + sc.next();
			}
			if (sc.hasNext()) { str += " " + sc.next(); }
			result = str.substring(1, str.length() - 1);
		}
		else if (sc.hasNext("\\{.*")) {
			java.util.regex.Pattern oldDelim = sc.delimiter();
			sc.useDelimiter("\\}");
			String content = sc.next().trim().substring(1);
			sc.useDelimiter(oldDelim);
			if (sc.hasNext("\\}")) { sc.next(); }
			String[] elements = {};
			if (!content.isEmpty()) {
				elements = cotent.split("\\p{javaWhitespace}*,\\p{javaWhitespace}*");
			}
			Object[] objects = new Object[elements.length];
			for (int i = 0; i < elements.length; i++) {
				java.util.Scanner sc0 = new java.util.Scanner(elements[i]);
				objects[i] = structorizerGetScannedObject(sc0);
				sc0.close();
			}
			result = objects;
		}
		else { result = sc.next(); }
		return result;
	}

	/**
	 * Reads the next token from the text file given by fileNo handle and returns it as an
	 * appropriate value of one of the classes Integer, Double, Object[], or String.  
	 * Throws an error if the given handle is not associated to an open text input file. 
	 * @param fileNo - file handle as obtained by fileOpen before
	 * @return the current object as interpreted from file input character sequence or null.
	 */
	public static Object fileRead(int fileNo) throws java.io.IOException
	{
		Object result = null;
		boolean ok = false;
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable reader = structorizerFileMap.get(fileNo - 1);
			if (reader instanceof java.util.Scanner) {
				result = structorizerGetScannedObject((java.util.Scanner)reader);
				ok = true;
			}
		}
		if (!ok) { throw new java.io.IOException("fileRead: Invalid file number or file not open for reading."); }
		return result;
	}

	/**
	 * Reads the next character from the text file given by fileNo handle and returns it.
	 * Throws an error if the given handle is not associated to an open text input file. 
	 * @param fileNo - file handle as obtained by fileOpen before
	 * @return the current character from file input character sequence or '\0'.
	 */
	public static Character fileReadChar(int fileNo) throws java.io.IOException {
		Character result = '\0';
		boolean ok = false;
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable reader = structorizerFileMap.get(fileNo - 1);
			if (reader instanceof java.util.Scanner) {
				java.util.Scanner sc = (java.util.Scanner)reader;
				java.util.regex.Pattern oldDelim = sc.delimiter();
				sc.useDelimiter("");
				try {
					if (!sc.hasNext(".") && sc.hasNextLine()) { sc.nextLine(); result = '\n'; }
					else if (sc.hasNext(".") {
						result = sc.next(".").charAt(0);
					}
				}
				finally { sc.useDelimiter(oldDelim); }
				ok = true;
			}
		}
		if (!ok) { throw new java.io.IOException("fileReadChar: Invalid file number or file not open for reading."); }
		return result;
	}

	/**
	 * Reads the next integer value from the text file given by fileNo handle and returns it.  
	 * Throws an error if the given handle is not associated to an open text input file.
	 * If the file input stream was exhausted (was at end of file) or if the token at reading
	 * was not interpretable as integral literal, then null will be returned. 
	 * @param fileNo - file handle as obtained by fileOpen before
	 * @return the current int number as interpreted from file input character sequence or null.
	 */
	public static Integer fileReadInt(int fileNo) throws java.io.IOException
	{
		Integer result = null;
		boolean ok = false;
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable reader = structorizerFileMap.get(fileNo - 1);
			if (reader instanceof java.util.Scanner) {
				if (((java.util.Scanner)reader).hasNextInt()) {
					result = ((java.util.Scanner)reader).nextInt();
				}
				ok = true;
			}
		}
		if (!ok) { throw new java.io.IOException("fileReadInt: Invalid file number or file not open for reading."); }
		return result;
	}

	/**
	 * Reads the next floating-point value from the text file given by fileNo handle and
	 * returns it as double.  
	 * Throws an error if the given handle is not associated to an open text input file.
	 * If the file input stream was exhausted (was at end of file) or if the token at reading
	 * was not interpretable as integral literal then null will be returned. 
	 * @param fileNo - file handle as obtained by fileOpen before
	 * @return the current floating point-value from file input character sequence or null.
	 */
	public static double fileReadDouble(int fileNo) throws java.io.IOException
	{ 
		Double result = null;
		boolean ok = false;
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable reader = structorizerFileMap.get(fileNo - 1);
			if (reader instanceof java.util.Scanner) {
				if (((java.util.Scanner)reader).hasNextDouble()) {
					result = ((java.util.Scanner)reader).nextDouble();
				}
				ok = true;
			}
		}
		if (!ok) { throw new java.io.IOException("fileReadDouble: Invalid file number or file not open for reading."); }
		return result;
	}

	/**
	 * Reads the next text line (or the rest of the current text line) from the text file
	 * given by fileNo handle and returns it.  
	 * Throws an error if the given handle is not associated to an open text input file.
	 * If the file input stream was exhausted (was at end of file) or if the token at reading
	 * was not interpretable as integral literal then null will be returned. 
	 * @param fileNo - file handle as obtained by fileOpen before
	 * @return the current object as interpreted from file input character sequence or null.
	 */
	public static String fileReadLine(int fileNo) throws java.io.IOException
	{
		String line = null;
		boolean ok = false;
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable reader = structorizerFileMap.get(fileNo - 1);
			if (reader instanceof java.util.Scanner) {
				if (((java.util.Scanner)reader).hasNextLine()) {
					line = ((java.util.Scanner)reader).nextLine();
				}
				ok = true;
			}
		}
		if (!ok) { throw new java.io.IOException("fileReadLine: Invalid file number or file not open for reading."); }
		return line;
	}

	/**
	 * Writes the given value as character sequence to the file given by handle fileNo.  
	 * Throws an error if the given handle is not associated to an open text output file.
	 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
	 * @param data the object or value to be written to file.
	 */
	public static void fileWrite(int fileNo, java.lang.Object data) throws java.io.IOException
	{
		boolean ok = false;
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable writer = structorizerFileMap.get(fileNo - 1);
			if (writer instanceof java.io.BufferedWriter) {
				((java.io.BufferedWriter)writer).write(data.toString());
				ok = true;
			}
		}
		if (!ok) { throw new java.io.IOException("fileWrite: Invalid file number or file not open for writing."); }
	}

	/**
	 * Writes the given value as character sequence to the file given by handle fileNo
	 * and appends a newline character or sequence as value separator.  
	 * Throws an error if the given handle is not associated to an open text output file.
	 * @param fileNo - file handle as obtained by fileCreate or fileAppend before
	 * @param data the object or value to be written to file.
	 */
	public static void fileWriteLine(int fileNo, java.lang.Object data) throws java.io.IOException
	{
		boolean ok = false;
		if (fileNo > 0 && fileNo <= structorizerFileMap.size()) {
			java.io.Closeable file = structorizerFileMap.get(fileNo - 1);
			if (file instanceof java.io.BufferedWriter) {
				((java.io.BufferedWriter)file).write(data.toString());
				((java.io.BufferedWriter)file).newLine();
				ok = true;
			}
		}
		if (!ok) { throw new java.io.IOException("fileWriteLine: Invalid file number or file not open for writing."); }
	}

}
