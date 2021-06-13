::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::
::      Author:        Kay Gürtzig
::
::      Description:   Arranger start script for Windows
::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::
::      Revision List
::
::      Author                        Date          Description
::      ------                        ----          -----------
::      Kay Gürtzig                   2018-06-10    First issue
::      Kay Gürtzig                   2018-06-12    Drive and path variables inserted
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
java -cp "%~d0%~p0Structorizer.app/Contents/Java/Structorizer.jar" lu.fisch.structorizer.arranger.Arranger
