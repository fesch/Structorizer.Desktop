<?php
// program DateTests563 (generated by Structorizer 3.32-26) 

// Copyright (C) 2017-09-18 Kay Gürtzig 
// License: GPLv3-link 
// GNU General Public License (V 3) 
// https://www.gnu.org/licenses/gpl.html 
// http://www.gnu.de/documents/gpl.de.html 

// BEGIN initialization for "CommonTypes423" 
// type Date = record{year: int; month, day: short} 
$today = array();
// END initialization for "CommonTypes423" 

// function isLeapYear 
// Detects whether the given year is a leap year in the Gregorian calendar 
// (extrapolated backwards beyonds its inauguration) 
function isLeapYear($year)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	// Most years aren't leap years... 
	$isLeapYear = false;
	if (($year % 4 == 0) && ($year % 100 != 0))
	{
		// This is a standard leap year 
		$isLeapYear = true;
	}
	else if $year % 400 == 0
	{
		// One of the rare leap years 
		// occurring every 400 years 
		$isLeapYear = true;
	}

	return $isLeapYear;
}

// function daysInMonth423 
// Computes the number of days the given month (1..12) 
// has in the the given year 
function daysInMonth423($aDate)
{

	// TODO Establish sensible web formulars to get the $_GET input working. 

	// select the case where illegal values are also considered 
	switch ($aDate['month']) 
	{
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			$days = 31;
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			$days = 30;
			break;
		case 2:
			// Default value for February 
			$days = 28;
			// To make the call work it has to be done in 
			// a separate element (cannot be performed 
			// as part of the condition of an Alternative) 
			$isLeap = isLeapYear($aDate['year']);
			if ($isLeap)
			{
				$days = 29;
			}
			break;
		default:
			// This is the return value for illegal months. 
			// It is easy to check 
			$days = 0;
	}
	return $days;
}
// = = = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 

// program DateTests563 
// Several declaration and initialisation variants for test of Analyser, Executor, and Generators 

$someDay = array("day" => 24,"month" => 2,"year" => 2017);
$nDays = daysInMonth423($someDay);
$today = array("year" => 2018,"month" => 7,"day" => 20);
// type Person = record { name: string; birth: Date; test: array[3] of int;} 
$me = array("name" => "roger","birth" => array("year" => 1985,"month" => 3,"day" => 6),"test" => array(0,8,15));
$declArray = array(9.0,7.5,-6.4,1.7,0.0);
$explArray = array(7.1,0.5,-1.5);
$doof = array(0.4);
$dull = array(-12.7,96.03);
$values = array(47,11);

?>
