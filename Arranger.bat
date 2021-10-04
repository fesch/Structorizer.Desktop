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
::      Kay Gürtzig                   2021-06-13    Issue #944: Java version check (against 11) inserted
::      Kay Gürtzig                   2021-10-04    Bugfix #988: Syntax error in nested "if" statement mended
::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: Check for the correct Java version
@echo off
setlocal
set REQVERSION=11
for /f "delims=" %%a in (' java -version ^2^>^&^1 ^| find "version" ') do set JAVAVER=%%a
set JAVAVER=%JAVAVER:"=_%
for /f "tokens=2 delims=_" %%a in ("%JAVAVER%") do set JAVAVER=%%a
for /f "tokens=1,2 delims=." %%a in ("%JAVAVER%") do (
	set VERSION=%%a
	if %%VERSION%% equ 1 set VERSION=%%b
)
if %VERSION% lss %REQVERSION% (
	echo on
	echo "Your Java version is %VERSION%, but version %REQVERSION% is required. Please update."
	@goto :exit
)
:: Actual start (Java version is fine)
echo on
java -cp "%~d0%~p0Structorizer.app/Contents/Java/Structorizer.jar" lu.fisch.structorizer.arranger.Arranger
:exit
