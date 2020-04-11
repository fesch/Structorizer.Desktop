// ======= 8< ======= FileApiGroupTest_arrz.js ========================== 

<script>
// includable FileApiGroupTest_arrz 
// Generated by Structorizer 3.30-07 

// Copyright (C) 2020-04-10 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

// NOTE: 
// This first module of the file is a library module providing common resources 
// for the following modules, which are separated by comment lines like 
// "======= 8< =======...". 
// You may have to cut this file apart at these lines in order to get the parts 
// running, since the following modules may form sort of mutually independent 
// applications or programs the coexistence of which in a single file might not 
// be sensible. 

//
//Flag ensures that initialisation function FileApiGroupTest_arrz() runs just one time.
var initDone_FileApiGroupTest_arrz = false;

export function FileApiGroupTest_arrz() {

	if (!initDone_FileApiGroupTest_arrz) {
		
		initDone_FileApiGroupTest_arrz = true;
	}
}

// function drawBarChart(values: array of double; nValues) 
// Draws a bar chart from the array "values" of size nValues. 
// Turtleizer must be activated and will scale the chart into a square of 
// 500 x 500 pixels 
// Note: The function is not robust against empty array or totally equal values. 
export function drawBarChart(values, nValues) {
	// Used range of the Turtleizer screen 
	const xSize = 500;
	const ySize = 500;
	var yScale;
	var yAxis;
	var valMin;
	var valMax;
	var stripeWidth;
	var stripeHeight;
	var kMin;
	var kMax;
	var k;

	kMin = 0;
	kMax = 0;
	for (k = 1; k <= nValues-1; k += (1)) {
		if (values[k] > values[kMax]) {
			kMax = k;
		}
		else {
			if (values[k] < values[kMin]) {
				kMin = k;
			}
		}
	}
	valMin = values[kMin];
	valMax = values[kMax];
	yScale = valMax * 1.0 / (ySize - 1);
	yAxis = ySize - 1;
	if (valMin < 0) {
		if (valMax > 0) {
			yAxis = valMax * ySize * 1.0 / (valMax - valMin);
			yScale = (valMax - valMin) * 1.0 / (ySize - 1);
		}
		else {
			yAxis = 1;
			yScale = valMin * 1.0 / (ySize - 1);
		}
	}
	// draw coordinate axes 
	gotoXY(1, ySize - 1);
	forward(ySize -1); // color = ffffff
	penUp();
	backward(yAxis); // color = ffffff
	right(90);
	penDown();
	forward(xSize -1); // color = ffffff
	penUp();
	backward(xSize-1); // color = ffffff
	stripeWidth = xSize / nValues;
	for (k = 0; k <= nValues-1; k += (1)) {
		stripeHeight = values[k] * 1.0 / yScale;
		switch (k % 3) {
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
		fd(stripeHeight); // color = ffffff
		right(90);
		fd(stripeWidth - 1); // color = ffffff
		right(90);
		forward(stripeHeight); // color = ffffff
		left(90);
		penUp();
	}
}

// function generateRandomNumberFile(fileName: string; count: int; minVal, maxVal: integer): integer 
// Writes a text file with name fileName, consisting of count lines, each containing 
// a random number in the range from minVal to maxVal (both inclusive). 
// Returns either the number of written number if all went well or some potential 
// error code if something went wrong. 
export function generateRandomNumberFile(fileName, count, minVal, maxVal) {
	var number;
	var k;
	var fileNo;

	fileNo = fileCreate(fileName);
	if (fileNo <= 0) {
		return fileNo;
	}
	try {
		for (k = 1; k <= count; k += (1)) {
			number = minVal + random(maxVal - minVal + 1);
			fileWriteLine(fileNo, number);
		}
	}
	catch (exee853b33) {
		error = exee853b33.message
		document.write((error) + "<br/>");
		return -7;
	}
	finally {
		fileClose(fileNo);
	}
	return count;
}

// function readNumbers(fileName: string; numbers: array of integer; maxNumbers: integer): integer 
// Tries to read as many integer values as possible upto maxNumbers 
// from file fileName into the given array numbers. 
// Returns the number of the actually read numbers. May cause an exception. 
export function readNumbers(fileName, numbers, maxNumbers) {
	var number;
	var nNumbers;
	var fileNo;

	nNumbers = 0;
	fileNo = fileOpen(fileName);
	if (fileNo <= 0) {
		throw "File could not be opened!";
	}
	try {
		while (! fileEOF(fileNo) && nNumbers < maxNumbers) {
			number = fileReadInt(fileNo);
			numbers[nNumbers] = number;
			nNumbers = nNumbers + 1;
		}
	}
	catch (exe2a9676a) {
		error = exe2a9676a.message
		throw exe2a9676a;
	}
	finally {
		fileClose(fileNo);
	}
	return nNumbers;
}
</script>

// ======= 8< =========================================================== 

<script>
// program ComputeSum 
// Generated by Structorizer 3.30-07 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

import ./FileApiGroupTest_arrz.js;

// Computes the sum and average of the numbers read from a user-specified 
// text file (which might have been created via generateRandomNumberFile(4)). 
//  
// This program is part of an arrangement used to test group code export (issue 
// #828) with FileAPI dependency. 
// The input check loop has been disabled (replaced by a simple unchecked input 
// instruction) in order to test the effect of indirect FileAPI dependency (only the 
// called subroutine directly requires FileAPI now). 
FileApiGroupTest_arrz();
var values;
var sum;
var nValues;
var k;
// Disable this if you enable the loop below! 
var file_name;
var fileNo;

fileNo = 1000;
// Disable this if you enable the loop below! 
file_name = prompt("Name/path of the number file");
// If you enable this loop, then the preceding input instruction is to be disabled 
// and the fileClose instruction in the alternative below is to be enabled. 
// do { 
// 	file_name = prompt("Name/path of the number file"); 
// 	fileNo = fileOpen(file_name); 
// } while (! (fileNo > 0 || file_name == "")); 
if (fileNo > 0) {
	// This should be enabled if the input check loop above gets enabled. 
// 	fileClose(fileNo); 
	values = [];
	nValues = 0;
	try {
		nValues = readNumbers(file_name, values, 1000);
	}
	catch (ex72c31b7f) {
		failure = ex72c31b7f.message
		document.write((failure) + "<br/>");
		exit(-7);
	}
	sum = 0.0;
	for (k = 0; k <= nValues-1; k += (1)) {
		sum = sum + values[k];
	}
	document.write(("sum = ", sum) + "<br/>");
	document.write(("average = ", sum / nValues) + "<br/>");
}
</script>

// ======= 8< =========================================================== 

<script>
// program DrawRandomHistogram 
// Generated by Structorizer 3.30-07 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

import ./FileApiGroupTest_arrz.js;

// Reads a random number file and draws a histogram accotrding to the 
// user specifications 
FileApiGroupTest_arrz();
// Interval width 
var width;
var value;
var numberArray;
var nObtained;
var nIntervals;
var min;
var max;
var kMaxCount;
var k;
var i;
var file_name;
var fileNo;
var count;

fileNo = -10;
do {
	file_name = prompt("Name/path of the number file");
	fileNo = fileOpen(file_name);
} while (! (fileNo > 0 || file_name == ""));
if (fileNo > 0) {
	fileClose(fileNo);
	nIntervals = prompt("number of intervals");
	// Initialize the interval counters 
	for (k = 0; k <= nIntervals-1; k += (1)) {
		count[k] = 0;
	}
	// Index of the most populated interval 
	kMaxCount = 0;
	numberArray = [];
	nObtained = 0;
	try {
		nObtained = readNumbers(file_name, numberArray, 10000);
	}
	catch (ex32f18dfb) {
		failure = ex32f18dfb.message
		document.write((failure) + "<br/>");
	}
	if (nObtained > 0) {
		min = numberArray[0];
		max = numberArray[0];
		for (i = 1; i <= nObtained-1; i += (1)) {
			if (numberArray[i] < min) {
				min = numberArray[i];
			}
			else {
				if (numberArray[i] > max) {
					max = numberArray[i];
				}
			}
		}
		// Interval width 
		width = (max - min) * 1.0 / nIntervals;
		for (i = 0; i <= nObtained - 1; i += (1)) {
			value = numberArray[i];
			k = 1;
			while (k < nIntervals && value > min + k * width) {
				k = k + 1;
			}
			count[k-1] = count[k-1] + 1;
			if (count[k-1] > count[kMaxCount]) {
				kMaxCount = k-1;
			}
		}
		drawBarChart(count, nIntervals);
		document.write(("Interval with max count: ", kMaxCount, " (", count[kMaxCount], ")") + "<br/>");
		for (k = 0; k <= nIntervals-1; k += (1)) {
			document.write((count[k], " numbers in interval ", k, " (", min + k * width, " ... ", min + (k+1) * width, ")") + "<br/>");
		}
	}
	else {
		document.write(("No numbers read.") + "<br/>");
	}
}
</script>
