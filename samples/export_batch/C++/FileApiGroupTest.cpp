// ======= 8< ======= FileApiGroupTest.h ================================ 

#pragma once
#ifndef FILEAPIGROUPTEST_H
#define FILEAPIGROUPTEST_H
// Generated by Structorizer 3.32-13 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#include <string>
using std::string;

//===== STRUCTORIZER FILE API START =====
#include "StructorizerFileAPI.h"
//===== STRUCTORIZER FILE API END =====


// Tries to read as many integer values as possible upto maxNumbers 
// from file fileName into the given array numbers. 
// Returns the number of the actually read numbers. May cause an exception. 

// Initialisation function for this library. 
// TODO: Revise the return type and declare the parameters. 
int readNumbers(string fileName, int numbers[50], int maxNumbers);

#endif /*FILEAPIGROUPTEST_H*/

// ======= 8< ======= FileApiGroupTest.cpp ============================== 

// Generated by Structorizer 3.32-13 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#include "FileApiGroupTest.h"

// function drawBarChart(values: array of double; nValues) 
// TODO: Revise the return type and declare the parameters. 
void drawBarChart(double values[50], ??? nValues)
{
	// TODO: Check and accomplish variable declarations: 
	const int xSize = 500;
	const int ySize = 500;
	double valMin;
	double valMax;
	int kMin;
	int kMax;

	kMin = 0;
	kMax = 0;
	for (int k = 1; k <= nValues-1; k += (1)) {
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
	??? yScale = valMax * 1.0 / (ySize - 1);
	??? yAxis = ySize - 1;
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
	??? stripeWidth = xSize / nValues;
	for (int k = 0; k <= nValues-1; k += (1)) {
		??? stripeHeight = values[k] * 1.0 / yScale;
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
// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// function readNumbers(fileName: string; numbers: array of integer; maxNumbers: integer): integer 
// TODO: Revise the return type and declare the parameters. 
int readNumbers(string fileName, int numbers[50], int maxNumbers)
{
	// TODO: Check and accomplish variable declarations: 
	int number;
	int nNumbers;
	int fileNo;

	nNumbers = 0;
	fileNo = StructorizerFileAPI::fileOpen(fileName);
	if (fileNo <= 0) {
		throw "File could not be opened!";
	}
	try {
		while (! StructorizerFileAPI::fileEOF(fileNo) && nNumbers < maxNumbers) {
			number = StructorizerFileAPI::fileReadInt(fileNo);
			numbers[nNumbers] = number;
			nNumbers = nNumbers + 1;
		}
	}
	catch(string error) {
		throw;
	}
// 	finally { 
		StructorizerFileAPI::fileClose(fileNo);
// 	} 
	return nNumbers;
}

// ======= 8< =========================================================== 

// Generated by Structorizer 3.32-13 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#include "FileApiGroupTest.h"
#include <string>
#include <iostream>
using std::string;

// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// program ComputeSum 

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
	??? file_name;
	int fileNo;

	FileApiGroupTest();
	fileNo = 1000;
	// Disable this if you enable the loop below! 
	std::cout << "Name/path of the number file"; std::cin >> file_name;
	// If you enable this loop, then the preceding input instruction is to be disabled 
	// and the fileClose instruction in the alternative below is to be enabled. 
// 	do { 
// 		std::cout << "Name/path of the number file"; std::cin >> file_name; 
// 		fileNo = StructorizerFileAPI::fileOpen(file_name); 
// 	} while (! (fileNo > 0 || file_name == "")); 
	if (fileNo > 0) {
		// This should be enabled if the input check loop above gets enabled. 
// 		StructorizerFileAPI::fileClose(fileNo); 
		nValues = 0;
		try {
			nValues = readNumbers(file_name, values, 1000);
		}
		catch(string failure) {
			std::cout << failure << std::endl;
			exit(-7);
		}
		sum = 0.0;
		for (int k = 0; k <= nValues-1; k += (1)) {
			sum = sum + values[k];
		}
		std::cout << "sum = " << sum << std::endl;
		std::cout << "average = " << sum / nValues << std::endl;
	}

	return 0;
}

// ======= 8< =========================================================== 

// Generated by Structorizer 3.32-13 

// Copyright (C) 2020-03-21 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

#include "FileApiGroupTest.h"
#include <string>
#include <iostream>
using std::string;

//===== STRUCTORIZER FILE API START =====
#include "StructorizerFileAPI.h"
//===== STRUCTORIZER FILE API END =====

// function drawBarChart(values: array of double; nValues) 

// Draws a bar chart from the array "values" of size nValues. 
// Turtleizer must be activated and will scale the chart into a square of 
// 500 x 500 pixels 
// Note: The function is not robust against empty array or totally equal values. 
// TODO: Revise the return type and declare the parameters. 
void drawBarChart(double values[50], ??? nValues)
{
	// TODO: Check and accomplish variable declarations: 

	kMin = 0;
	kMax = 0;
	for (int k = 1; k <= nValues-1; k += (1)) {
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
	for (int k = 0; k <= nValues-1; k += (1)) {
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
// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// program DrawRandomHistogram 

// Reads a random number file and draws a histogram accotrding to the 
// user specifications 
int main(void)
{
	// TODO: Check and accomplish variable declarations: 
	??? numberArray[50];
	int nObtained;
	??? nIntervals;
	int kMaxCount;
	??? file_name;
	int fileNo;
	int count[50];

	FileApiGroupTest();
	fileNo = -10;
	do {
		std::cout << "Name/path of the number file"; std::cin >> file_name;
		fileNo = StructorizerFileAPI::fileOpen(file_name);
	} while (! (fileNo > 0 || file_name == ""));
	if (fileNo > 0) {
		StructorizerFileAPI::fileClose(fileNo);
		std::cout << "number of intervals"; std::cin >> nIntervals;
		// Initialize the interval counters 
		for (int k = 0; k <= nIntervals-1; k += (1)) {
			count[k] = 0;
		}
		// Index of the most populated interval 
		kMaxCount = 0;
		nObtained = 0;
		try {
			nObtained = readNumbers(file_name, numberArray, 10000);
		}
		catch(string failure) {
			std::cout << failure << std::endl;
		}
		if (nObtained > 0) {
			??? min = numberArray[0];
			??? max = numberArray[0];
			for (int i = 1; i <= nObtained-1; i += (1)) {
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
			??? width = (max - min) * 1.0 / nIntervals;
			for (int i = 0; i <= nObtained - 1; i += (1)) {
				??? value = numberArray[i];
				int k = 1;
				while (k < nIntervals && value > min + k * width) {
					k = k + 1;
				}
				count[k-1] = count[k-1] + 1;
				if (count[k-1] > count[kMaxCount]) {
					kMaxCount = k-1;
				}
			}
			drawBarChart(count, nIntervals);
			std::cout << "Interval with max count: " << kMaxCount << " (" << count[kMaxCount] << ")" << std::endl;
			for (k = 0; k <= nIntervals-1; k += (1)) {
				std::cout << count[k] << " numbers in interval " << k << " (" << min + k * width << " ... " << min + (k+1) * width << ")" << std::endl;
			}
		}
		else {
			std::cout << "No numbers read." << std::endl;
		}
	}

	return 0;
}
