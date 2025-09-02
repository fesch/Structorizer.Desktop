<?php
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

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    File API for the PHPGenerator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.01.03      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - The API is not exactly equivalent to the Structorizer File API used in Executor but tries
 *        to get at close to it as possible
 *      - The Structorizer functions are replaced by static functions of PHP class StructorizerFileAPI
 *      - error codes returned by functions fileOpen, fileCreate, and fileAppend differ from those in
 *        Structorizer Executor
 *      - The function reactions to invalid $fileNo arguments may differ from those in Structorizer 
 *
 ******************************************************************************************************///


class StructorizerFileAPI {

	private static $fileMap = array();
	private static $fileLines = array();
	private static $nextFileNo = 0;
	
	/**
	 * Tries to open a text file with given $filepath for reading. File must exist.
	 * A negative or zero return value indicates failure.
	 * @param $filepath - the path of the file (may be absolute or relative to the current directory)
	 * @return an int to be used as file handle for this API if > 0 or as error code otherwise.
	 */
	public static function fileOpen($filepath)
	{
		$fileNo = -1;
		$file = fopen($filepath, "r");
		if ($file != NULL) {
			$fileNo = self::$nextFileNo++;
			self::$fileMap[$fileNo] = &$file;
			self::$fileLines[$fileNo] = "";
		}
		return $fileNo;
	}

	/**
	 * Tries to create a new text file with given $filepath for writing. If the file exists then it
	 * will be overwritten.
	 * A negative or zero return value indicates failure.
	 * @param $filepath - the path of the file (may be absolute or relative to the current directory)
	 * @return an int to be used as file handle for this API if > 0 or as error code otherwise.
	 */
	public static function fileCreate($filepath)
	{
		$fileNo = -1;
		$file = fopen($filepath, "w");
		if ($file != NULL) {
			$fileNo = self::$nextFileNo++;
			self::$fileMap[$fileNo] = &$file;
		}
		return $fileNo;
	}

	/**
	 * Tries to open a text file with given $filepath for appending text. If the file hasn't existed
	 * then it will be created.
	 * A negative or zero return value indicates failure.
	 * @param filePath - the path of the file (may be absolute or relative to the current directory)
	 * @return an int to be used as file handle for this API if > 0 or as error code otherwise.
	 */
	public static function fileAppend($filepath)
	{
		$fileNo = -1;
		$file = fopen($filepath, "a");
		if ($file != NULL) {
			$fileNo = self::$nextFileNo++;
			self::fileMap$[$fileNo] = &$file;
		}
		return $fileNo;
	}

	/**
	 * Closes the file with given $fileNo handle. If fileNo is not associated with an open file
	 * then an IOException will be thrown.
	 * @param $fileNo - file handle as obtained by fileOpen, fileCreate or fileAppend before
	 */
	public static function fileClose($fileNo)
	{
		if ($fileNo >= 0 && $fileNo < self::$nextFileNo) {
			fclose(self::$fileMap[$fileNo]);
			unset(self::$fileMap[$fileNo]);
		} 
	}

	/**
	 * Checks whether the input file with given $fileNo handle is exhausted i.e. provides no
	 * readable content beyond the current reading position.
	 * If $fileNo is not associated to any opened readable file then an IOException will be thrown.
	 * @param $fileNo - file handle as obtained by fileOpen before.
	 * @return true iff end of file has been reached.
	 */
	public static fileEOF($fileNo)
	{
		// FIXME: retrospective only?
		return feof(self::fileMap[$fileNo]);
	}

	/**
	 * Reads the next char from the text file given by $fileNo handle and returns it.
	 * Throws an error if the given handle is not associated to an open text input file.
	 * @param $fileNo - file handle as obtained by fileOpen before
	 * @return the current char from file input char sequence or NULL.
	*/
	public static function fileReadChar($fileNo)
	{
		$result = NULL;
		$line = self::$fileGetLine($fleNo);
		if ($line != false && strlen($line) > 0) {
			$result = $line[0];
			self::$fileLines[$fileNo] = substr($line, 1);
		}
		return $result;
	}

	/**
	 * Reads the next int value from the text file given by $fileNo handle and returns it.
	 * Throws an error if the given handle is not associated to an open text input file.
	 * If the file input stream was exhausted (was at end of file) or if the token at reading
	 * position was not interpretable as integral literal, then null will be returned.
	 * @param $fileNo - file handle as obtained by fileOpen before
	 * @return the current int number as interpreted from file input char sequence or null.
	 */
	public static function fileReadInt($fileNo)
	{
		$result = NULL;
		$line = self::$fileGetLine($fileNo);
		if ($line != false) {
			if (sscanf($line, "%d %[^\\n]", $val, $tail) > 0) {
				$result = $val;
				self::$fileLines[$fileNo] = $tail."\n";
			}
		}
		return $result;
	}

	/**
	 * Reads the next floating-point value from the text file given by $fileNo handle and
	 * returns it as double.
	 * Throws an error if the given handle is not associated to an open text input file.
	 * If the file input stream was exhausted (was at end of file) or if the token at reading
	 * position was not interpretable as floating-point literal then null will be returned.
	 * @param $fileNo - file handle as obtained by fileOpen before
	 * @return the current floating point-value from file input char sequence or null.
	 */
	public static function fileReadDouble($fileNo)
	{
		$result = NULL;
		$line = self::$fileGetLine($fileNo);
		if ($line != false) {
			if (sscanf($line, "%f %[^\\n]", $val, $tail) > 0) {
				$result = $val;
				self::$fileLines[$fileNo] = $tail."\n";
			}
		}
		return $result;
	}

	/**
	* Reads the next token from the text file given by $fileNo handle and returns it as an
	* appropriate value of one of the types int, double, or string.
	* May raise an error if the given handle is not associated to an open text input file.
	* @param fileNo - file handle as obtained by fileOpen before
	* @return the current object as interpreted from file input char sequence or NULL.
	*/
	public static function fileRead($fileNo)
	{
		$result = NULL;
		$line = self::$fileGetLine($fileNo);
		if ($line != false) {
			if (sscanf($line, "%s %[^\\n]", $val, $tail) > 0) {
				if (is_numeric($val)) {
					$result = doubleval($val);
					$intRes = intval($val);
					if ($intRes == $result) {
						$result = $intRes;
					}
				}
				// FIXME: Try to identify and compose quoted strings and arrays
				else {
					$result = $val;
				}
				self::$fileLines[$fileNo] = $tail."\n";
			}
		}
		return $result;
	}
	
	/**
	 * Reads the next text line (or the rest of the current text line) from the text file
	 * given by $fileNo handle and returns it.
	 * May raise an error if the given handle is not associated to an open text input file.
	 * If the file input stream was exhausted (was at end of file) then false will be returned.
	 * @param $fileNo - file handle as obtained by fileOpen before
	 * @return the current (remaining) line from file or false.
	 */
	public static function fileReadLine($fileNo)
	{
		$line = self::$fileGetLine($fileNo);
		if ($line != false) {
			$len = strlen($line);
			if ($len > 0 && $line[$len-1] == '\n') {
				$line = substr($line, 0, $len-1);
			}
			// get next line
			self::fileGetLine($fileNo);  
		}
		return $line;
	}
	
	/* Internal helper function to provide a non-empty line, not for customer use! */
	private static function fileGetLine($fileNo)
	{
		$line = self::$fileLines[$fileNo];
		
		while ($line != false && strlen($line) == 0 && !feof($fileMap[$fileNo])) {
			$line = fgets($fileMap[$fileNo]);
		}
		self::$fileLines[$fileNo] = $line;
		return $line; 
	}
	
	/**
	 * Writes the given value as char sequence to the file given by handle $fileNo.
	 * Raises an error if the given handle is not associated to an open text output file.
	 * @param $fileNo - file handle as obtained by fileCreate or fileAppend before
	 * @param $value the value to be written to file.
	 */
	public static function fileWrite($fileNo, $value)
	{
		fwrite(self::$fileMap[$fileNo], strval($value));	
	}

	/**
	 * Writes the given value as char sequence to the file given by handle $fileNo
	 * and appends a newline char or sequence as value separator.
	 * May raise an error if the given handle is not associated to an open text output file.
	 * @param $fileNo - file handle as obtained by fileCreate or fileAppend before
	 * @param $value the value to be written to file.
	 */
	public static function fileWriteLine($fileNo, $value)
	{
		fwrite(self::$fileMap[$fileNo], strval($value)."\n");
	}

}

?>
