<?php
// program DrawRandomHistogram (generated by Structorizer 3.32-20) 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

/*===== STRUCTORIZER FILE API START =====*/

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
/*===== STRUCTORIZER FILE API END =====*/

// function drawBarChart 
// Draws a bar chart from the array "values" of size nValues. 
// Turtleizer must be activated and will scale the chart into a square of 
// 500 x 500 pixels 
// Note: The function is not robust against empty array or totally equal values. 
function drawBarChart($values, $nValues)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	// Used range of the Turtleizer screen 
	const xSize = 500;
	const ySize = 500;
	$kMin = 0;
	$kMax = 0;
	for ($k = 1; $k <= $nValues-1; $k += (1))
	{
		if ($values[$k] > $values[$kMax])
		{
			$kMax = $k;
		}
		else if $values[$k] < $values[$kMin]
		{
			$kMin = $k;
		}
	}
	$valMin = $values[$kMin];
	$valMax = $values[$kMax];
	$yScale = $valMax * 1.0 / (ySize - 1);
	$yAxis = ySize - 1;
	if ($valMin < 0)
	{
		if ($valMax > 0)
		{
			$yAxis = $valMax * ySize * 1.0 / ($valMax - $valMin);
			$yScale = ($valMax - $valMin) * 1.0 / (ySize - 1);
		}
		else
		{
			$yAxis = 1;
			$yScale = $valMin * 1.0 / (ySize - 1);
		}
	}
	// draw coordinate axes 
	gotoXY(1, ySize - 1);
	forward(ySize -1); // color = ffffff
	penUp();
	backward($yAxis); // color = ffffff
	right(90);
	penDown();
	forward(xSize -1); // color = ffffff
	penUp();
	backward(xSize-1); // color = ffffff
	$stripeWidth = xSize / $nValues;
	for ($k = 0; $k <= $nValues-1; $k += (1))
	{
		$stripeHeight = $values[$k] * 1.0 / $yScale;
		switch ($k % 3) 
		{
			case 0:
				setPenColor(255,0,0);
				break;
			case 1:
				setPenColor(0, 255,0);
				break;
			case 2:
				setPenColor(0, 0, 255);
				break;
		}
		fd(1); // color = ffffff
		left(90);
		penDown();
		fd($stripeHeight); // color = ffffff
		right(90);
		fd($stripeWidth - 1); // color = ffffff
		right(90);
		forward($stripeHeight); // color = ffffff
		left(90);
		penUp();
	}
}

// function readNumbers 
// Tries to read as many integer values as possible upto maxNumbers 
// from file fileName into the given array numbers. 
// Returns the number of the actually read numbers. May cause an exception. 
function readNumbers($fileName, $numbers, $maxNumbers)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	$nNumbers = 0;
	$fileNo = StructorizerFileAPI::fileOpen($fileName);
	if ($fileNo <= 0)
	{
		throw new Exception("File could not be opened!");
	}
	try {
		while (! StructorizerFileAPI::fileEOF($fileNo) && $nNumbers < $maxNumbers) 
		{
			$number = StructorizerFileAPI::fileReadInt($fileNo);
			$numbers[$nNumbers] = $number;
			$nNumbers = $nNumbers + 1;
		}
	} catch (Exception $ex296eb024) {
		$error = $ex296eb024->getMessage();
		throw $ex296eb024;
	} finally {
		StructorizerFileAPI::fileClose($fileNo);
	}
	return $nNumbers;
}
// program DrawRandomHistogram 
// Reads a random number file and draws a histogram accotrding to the 
// user specifications 

// TODO Establish sensible web formulars to get the $_GET input working. 

$fileNo = -10;
do
{
	$file_name = $_REQUEST["Name/path of the number file"];	// TODO form a sensible input opportunity;
	$fileNo = StructorizerFileAPI::fileOpen($file_name);
} while (!( $fileNo > 0 || $file_name == "" ));
if ($fileNo > 0)
{
	StructorizerFileAPI::fileClose($fileNo);
	$nIntervals = $_REQUEST["number of intervals"];	// TODO form a sensible input opportunity;
	// Initialize the interval counters 
	for ($k = 0; $k <= $nIntervals-1; $k += (1))
	{
		$count[$k] = 0;
	}
	// Index of the most populated interval 
	$kMaxCount = 0;
	$numberArray = array();
	$nObtained = 0;
	try {
		$nObtained = readNumbers($file_name, $numberArray, 10000);
	} catch (Exception $ex6d5a8719) {
		$failure = $ex6d5a8719->getMessage();
		echo failure;
	}
	if ($nObtained > 0)
	{
		$min = $numberArray[0];
		$max = $numberArray[0];
		for ($i = 1; $i <= $nObtained-1; $i += (1))
		{
			if ($numberArray[$i] < $min)
			{
				$min = $numberArray[$i];
			}
			else if $numberArray[$i] > $max
			{
				$max = $numberArray[$i];
			}
		}
		// Interval width 
		$width = ($max - $min) * 1.0 / $nIntervals;
		for ($i = 0; $i <= $nObtained - 1; $i += (1))
		{
			$value = $numberArray[$i];
			$k = 1;
			while ($k < $nIntervals && $value > $min + $k * $width) 
			{
				$k = $k + 1;
			}
			$count[$k-1] = $count[$k-1] + 1;
			if ($count[$k-1] > $count[$kMaxCount])
			{
				$kMaxCount = $k-1;
			}
		}
		drawBarChart($count, $nIntervals);
		echo "Interval with max count: ", $kMaxCount, " (", $count[$kMaxCount], ")";
		for ($k = 0; $k <= $nIntervals-1; $k += (1))
		{
			echo $count[$k], " numbers in interval ", $k, " (", $min + $k * $width, " ... ", $min + ($k+1) * $width, ")";
		}
	}
	else
	{
		echo "No numbers read.";
	}
}

?>
