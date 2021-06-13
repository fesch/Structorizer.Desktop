::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::
::      Author:        Kay Gürtzig
::
::      Description:   Structorizer start script for Windows
::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::
::      Revision List
::
::      Author                        Date          Description
::      ------                        ----          -----------
::      Kay Gürtzig                   2016-05-03    First Issue
::      Kay Gürtzig                   2017-07-04    Drive variable added to path
::      Kay Gürtzig                   2018-11-27    Precaution against installation path with blanks
::      Kay Gürtzig                   2021-06-13    issue #944: Java version check (against 11) inserted
::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: Check for the correct Java version
@echo off
for /f "delims=" %%a in (' java -version ^2^>^&^1 ^| find "version" ') do set JAVAVER=%%a
set JAVAVER=%JAVAVER:"=_%
for /f "tokens=2 delims=_" %%a in ("%JAVAVER%") do set JAVAVER=%%a
for /f "tokens=1,2 delims=." %%a in ("%JAVAVER%") do (
	set VERSION=%%a
	set MINOR=%%b
)
if %VERSION% equ 1 set VERSION=%MINOR%
if %VERSION% lss 11 (
	echo on
	echo "Your Java version is %VERSION%, but version 11 is required. Please update."
	@goto :exit
)
:: Actual start (Java version is fine)
java -jar "%~d0%~p0Structorizer.app\Contents\Java\Structorizer.jar" %*
:exit
