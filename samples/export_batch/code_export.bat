@echo off
rem Automated code export (test) script for Windows 2021-06-09
rem 1. Set the path of the Structorizer.bat file in the environment variable STRUCTORIZER.
rem 2. Make sure that diagrdir is correctly defined.
rem 3. Specify the appropriate inifile (if the given ini files are not appropriate)
rem 4. The resulting files will occur in the diagrdir as well and ought to be moved to the
rem    appropriate target directory (i.e. .\export_batch
set diagrdir=..\arrz
rem set inifile=%USER_PROFILE%\.structorizer\structorizer.ini
set inifile=export_basic0.ini
echo %diagrdir%
rem Arrangement archive loop (all available source archives)
for %%f in (%diagrdir%\*.arrz) do (
	echo %%f
	rem target generator loop - iterates over all available languages
	for %%l in (Pascal Oberon StrukTeX Perl ksh bash C CSharpGenerator CPlusPlusGenerator Java Javascript PHP Python Basic) do (
		echo Doing %STRUCTORIZER% -x %%l %%f ...
		%STRUCTORIZER% -x %%l -s %inifile% %%f
	)
	rem Now export as modern Basic (in the above loop it was ancient BASIC)
	echo Doing %STRUCTORIZER% -x Basic %%f ...
	%STRUCTORIZER% -x Basic -s %inifile:0=1% %%f -o %%f1.bas
	rem Now export the files to all different LaTeX algorithm variants
	for %%p in (algorithmicx algorithmic algorithm2e pseudocode) do (
		echo Doing %STRUCTORIZER% -x LaTeX -s export_%%p.ini %%f ...
		%STRUCTORIZER% -x LaTeX -s export_%%p.ini %%f -o %%f_%%p.tex
	)
)
