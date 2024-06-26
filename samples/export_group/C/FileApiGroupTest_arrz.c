// ======= 8< ======= FileApiGroupTest_arrz.h =========================== 

#ifndef FILEAPIGROUPTEST_ARRZ_H
#define FILEAPIGROUPTEST_ARRZ_H
// includable FileApiGroupTest_arrz 
// Generated by Structorizer 3.32-20 

// Copyright (C) 2024-04-03 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#define _CRT_SECURE_NO_WARNINGS
#include FileAPI.h
#include <stdio.h>
#include <stdbool.h>


// NOTE: 
// This first module of the file is a library module providing common resources 
// for the following modules, which are separated by comment lines like 
// "======= 8< =======...". 
// You may have to cut this file apart at these lines in order to get the parts 
// running, since the following modules may form sort of mutually independent 
// applications or programs the coexistence of which in a single file might not 
// be sensible. 

// Initialisation function for this library. 
void FileApiGroupTest_arrz(void);

// Draws a bar chart from the array "values" of size nValues. 
// Turtleizer must be activated and will scale the chart into a square of 
// 500 x 500 pixels 
// Note: The function is not robust against empty array or totally equal values. 
// TODO: Revise the return type and declare the parameters. 
void drawBarChart(double values[50], ??? nValues);

// Writes a text file with name fileName, consisting of count lines, each containing 
// a random number in the range from minVal to maxVal (both inclusive). 
// Returns either the number of written number if all went well or some potential 
// error code if something went wrong. 
// TODO: Revise the return type and declare the parameters. 
int generateRandomNumberFile(char* fileName, int count, int minVal, int maxVal);

// Tries to read as many integer values as possible upto maxNumbers 
// from file fileName into the given array numbers. 
// Returns the number of the actually read numbers. May cause an exception. 
// TODO: Revise the return type and declare the parameters. 
int readNumbers(char* fileName, int numbers[50], int maxNumbers);

#endif /*FILEAPIGROUPTEST_ARRZ_H*/

// ======= 8< ======= FileApiGroupTest_arrz.c =========================== 

// Generated by Structorizer 3.32-20 

// Copyright (C) 2024-04-03 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#include "FileApiGroupTest_arrz.h"


void FileApiGroupTest_arrz(void)
{
	// TODO: Check and accomplish variable declarations: 

	static bool initDone_FileApiGroupTest_arrz = false;
	if (!initDone_FileApiGroupTest_arrz) {
		
		initDone_FileApiGroupTest_arrz = true;
	}
}

// function drawBarChart(values: array of double; nValues) 

// Copyright (C) 2020-03-22 Kay Gürtzig 
// License: GPLv3-link 

// TODO: Revise the return type and declare the parameters. 
void drawBarChart(double values[50], ??? nValues)
{
	// TODO: Check and accomplish variable declarations: 
	// Used range of the Turtleizer screen 
	const int xSize = 500;
	const int ySize = 500;
	??? yScale;
	??? yAxis;
	double valMin;
	double valMax;
	??? stripeWidth;
	??? stripeHeight;
	int kMin;
	int kMax;
	int k;

	// Used range of the Turtleizer screen 
	kMin = 0;
	kMax = 0;
	for (k = 1; k <= nValues-1; k += (1)) {
		if (values[k] > values[kMax]) {
			kMax = k;
		}
		else if (values[k] < values[kMin]) {
			kMin = k;
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

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 

// TODO: Revise the return type and declare the parameters. 
int generateRandomNumberFile(char* fileName, int count, int minVal, int maxVal)
{
	// TODO: Check and accomplish variable declarations: 
	??? number;
	int k;
	int fileNo;

	// TODO: 
	// For any output using the 'printf' function you need to fill the first argument: 
	// http://en.wikipedia.org/wiki/Printf#printf_format_placeholders 

	fileNo = fileCreate(fileName);
	if (fileNo <= 0) {
		return fileNo;
	}
	// TODO: Find an equivalent for this non-supported try / catch block! 
// 	try { 
		for (k = 1; k <= count; k += (1)) {
			number = minVal + (rand() % (maxVal - minVal + 1));
			fileWriteLine(fileNo, number);
		}
// 	} 
// 	catch(char error[]) { 
		// TODO: check format specifiers, replace all '?'! 
// 		printf("%?\n", error); 
// 		return -7; 
// 	} 
// 	finally { 
		fileClose(fileNo);
// 	} 
	return count;
}

// function readNumbers(fileName: string; numbers: array of integer; maxNumbers: integer): integer 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 

// TODO: Revise the return type and declare the parameters. 
int readNumbers(char* fileName, int numbers[50], int maxNumbers)
{
	// TODO: Check and accomplish variable declarations: 
	int number;
	int nNumbers;
	int fileNo;

	nNumbers = 0;
	fileNo = fileOpen(fileName);
	if (fileNo <= 0) {
		// FIXME: Structorizer detected this illegal jump attempt: 
		// throw "File could not be opened!" 
		goto __ERROR__;
	}
	// TODO: Find an equivalent for this non-supported try / catch block! 
// 	try { 
		while (! fileEOF(fileNo) && nNumbers < maxNumbers) {
			number = fileReadInt(fileNo);
			numbers[nNumbers] = number;
			nNumbers = nNumbers + 1;
		}
// 	} 
// 	catch(char error[]) { 
		// FIXME: jump/exit instruction of unrecognised kind! 
		// throw 
// 	} 
// 	finally { 
		fileClose(fileNo);
// 	} 
	return nNumbers;
}

// ======= 8< =========================================================== 

// program ComputeSum 
// Generated by Structorizer 3.32-20 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#define _CRT_SECURE_NO_WARNINGS
#include "FileApiGroupTest_arrz.h"
#include <stdio.h>
#include <stdbool.h>



// Computes the sum and average of the numbers read from a user-specified 
// text file (which might have been created via generateRandomNumberFile(4)). 
//  
// This program is part of an arrangement used to test group code export (issue 
// #828) with FileAPI dependency. 
// The input check loop has been disabled (replaced by a simple unchecked input 
// instruction) in order to test the effect of indirect FileAPI dependency (only the 
// called subroutine directly requires FileAPI now). 
int main(void)
{
	// TODO: Check and accomplish variable declarations: 
	??? values[50];
	double sum;
	int nValues;
	int k;
	??? file_name;
	int fileNo;

	// TODO: 
	// For any input using the 'scanf' function you need to fill the first argument. 
	// http://en.wikipedia.org/wiki/Scanf#Format_string_specifications 

	// TODO: 
	// For any output using the 'printf' function you need to fill the first argument: 
	// http://en.wikipedia.org/wiki/Printf#printf_format_placeholders 

	FileApiGroupTest_arrz();
	fileNo = 1000;
	// TODO: check format specifiers, replace all '?'! 
	// Disable this if you enable the loop below! 
	printf("Name/path of the number file"); scanf("%?", &file_name);
	// Disable this if you enable the loop below! 
	// If you enable this loop, then the preceding input instruction is to be disabled 
	// and the fileClose instruction in the alternative below is to be enabled. 
// 	do { 
		// TODO: check format specifiers, replace all '?'! 
// 		printf("Name/path of the number file"); scanf("%?", &file_name); 
// 		fileNo = fileOpen(file_name); 
// 	} while (! (fileNo > 0 || file_name == "")); 
	if (fileNo > 0) {
		// This should be enabled if the input check loop above gets enabled. 
// 		fileClose(fileNo); 
		nValues = 0;
		// TODO: Find an equivalent for this non-supported try / catch block! 
// 		try { 
			nValues = readNumbers(file_name, values, 1000);
// 		} 
// 		catch(char failure[]) { 
			// TODO: check format specifiers, replace all '?'! 
// 			printf("%?\n", failure); 
// 			exit(-7); 
// 		} 
		sum = 0.0;
		for (k = 0; k <= nValues-1; k += (1)) {
			sum = sum + values[k];
		}
		// TODO: check format specifiers, replace all '?'! 
		printf("%s%g\n", "sum = ", sum);
		// TODO: check format specifiers, replace all '?'! 
		printf("%s%?\n", "average = ", sum / nValues);
	}

	return 0;
}

// ======= 8< =========================================================== 

// program DrawRandomHistogram 
// Generated by Structorizer 3.32-20 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#define _CRT_SECURE_NO_WARNINGS
#include "FileApiGroupTest_arrz.h"
#include FileAPI.h
#include <stdio.h>
#include <stdbool.h>



// Reads a random number file and draws a histogram accotrding to the 
// user specifications 
int main(void)
{
	// TODO: Check and accomplish variable declarations: 
	??? width;
	??? value;
	??? numberArray[50];
	int nObtained;
	??? nIntervals;
	??? min;
	??? max;
	int kMaxCount;
	int k;
	int i;
	??? file_name;
	int fileNo;
	int count[50];

	// TODO: 
	// For any input using the 'scanf' function you need to fill the first argument. 
	// http://en.wikipedia.org/wiki/Scanf#Format_string_specifications 

	// TODO: 
	// For any output using the 'printf' function you need to fill the first argument: 
	// http://en.wikipedia.org/wiki/Printf#printf_format_placeholders 

	FileApiGroupTest_arrz();
	fileNo = -10;
	do {
		// TODO: check format specifiers, replace all '?'! 
		printf("Name/path of the number file"); scanf("%?", &file_name);
		fileNo = fileOpen(file_name);
	} while (! (fileNo > 0 || file_name == ""));
	if (fileNo > 0) {
		fileClose(fileNo);
		// TODO: check format specifiers, replace all '?'! 
		printf("number of intervals"); scanf("%?", &nIntervals);
		// Initialize the interval counters 
		for (k = 0; k <= nIntervals-1; k += (1)) {
			count[k] = 0;
		}
		// Index of the most populated interval 
		kMaxCount = 0;
		nObtained = 0;
		// TODO: Find an equivalent for this non-supported try / catch block! 
// 		try { 
			nObtained = readNumbers(file_name, numberArray, 10000);
// 		} 
// 		catch(char failure[]) { 
			// TODO: check format specifiers, replace all '?'! 
// 			printf("%?\n", failure); 
// 		} 
		if (nObtained > 0) {
			min = numberArray[0];
			max = numberArray[0];
			for (i = 1; i <= nObtained-1; i += (1)) {
				if (numberArray[i] < min) {
					min = numberArray[i];
				}
				else if (numberArray[i] > max) {
					max = numberArray[i];
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
			// TODO: check format specifiers, replace all '?'! 
			printf("%s%d%s%?%s\n", "Interval with max count: ", kMaxCount, " (", count[kMaxCount], ")");
			for (k = 0; k <= nIntervals-1; k += (1)) {
				// TODO: check format specifiers, replace all '?'! 
				printf("%?%s%d%s%?%s%?%s\n", count[k], " numbers in interval ", k, " (", min + k * width, " ... ", min + (k+1) * width, ")");
			}
		}
		else {
			// TODO: check format specifiers, replace all '?'! 
			printf("%s\n", "No numbers read.");
		}
	}

	return 0;
}
