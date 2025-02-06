<script>
// program DateTests563 
// Generated by Structorizer 3.32-26 

// Copyright (C) 2017-09-18 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

var initDone_CommonTypes423 = false;
var today;

// function initialize_CommonTypes423() 
// Automatically created initialization procedure for CommonTypes423 
function initialize_CommonTypes423() {
	if (! initDone_CommonTypes423) {
		initDone_CommonTypes423 = true;
	}
}

// function isLeapYear(year): boolean 
// Detects whether the given year is a leap year in the Gregorian calendar 
// (extrapolated backwards beyonds its inauguration) 
function isLeapYear(year) {
	var isLeapYear;

	// Most years aren't leap years... 
	isLeapYear = false;
	if ((year % 4 == 0) && (year % 100 != 0)) {
		// This is a standard leap year 
		isLeapYear = true;
	}
	else if (year % 400 == 0) {
		// One of the rare leap years 
		// occurring every 400 years 
		isLeapYear = true;
	}

	return isLeapYear;
}

// function int daysInMonth423(Date aDate) 
// Computes the number of days the given month (1..12) 
// has in the the given year 
function daysInMonth423(aDate) {
	initialize_CommonTypes423();
	
	var isLeap;
	var days;

	// select the case where illegal values are also considered 
	switch (aDate.month) {
	case 1:
	case 3:
	case 5:
	case 7:
	case 8:
	case 10:
	case 12:
		days = 31;
		break;
	case 4:
	case 6:
	case 9:
	case 11:
		days = 30;
		break;
	case 2:
		// Default value for February 
		days = 28;
		// To make the call work it has to be done in 
		// a separate element (cannot be performed 
		// as part of the condition of an Alternative) 
		isLeap = isLeapYear(aDate.year);
		if (isLeap) {
			days = 29;
		}
		break;
	default:
		// This is the return value for illegal months. 
		// It is easy to check 
		days = 0;
	}
	return days;
}
// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// Several declaration and initialisation variants for test of Analyser, Executor, and Generators 
initialize_CommonTypes423();

var values;
var someDay;
var nDays;
var me;
var explArray;
var dull;
var doof;
var declArray;

someDay = {year: 2017, month: 2, day: 24};
nDays = daysInMonth423(someDay);
today = {year: 2018, month: 7, day: 20}
me = {name: "roger", birth: {year: 1985, month: 3, day: 6}, test: [0, 8, 15]};
var declArray = [9.0, 7.5, -6.4, 1.7, 0.0];
var explArray = [7.1, 0.5, -1.5];
var doof = [0.4];
var dull = [-12.7, 96.03];
values = [47, 11];
</script>
