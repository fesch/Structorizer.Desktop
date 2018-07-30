# Structorizer

Structorizer is a little tool to create Nassi-Shneiderman Diagrams (NSD):

![newton](https://user-images.githubusercontent.com/15326471/43420482-1f1edaac-9444-11e8-8c36-ccb8e084c615.png)

Beyond mere editing, it even allows to execute and debug them (within certain restrictions), to control a painting turtle on a drawing canvas, and to export the formed algorithms to several programming languages (still requiring postprocessing, of course). You may also derive diagrams from source code (by now languages Pascal/Delphi, ANSI-C, and COBOL).

The debugging features include stepwise execution, highlighting, pausing, breakpoints, variable display (with value editing), and configurable running speed, and eventually the possibility to call other diagrams as subroutine.
An impressive feature is the "Runtime Analysis" collecting and visualising execution counts, operation loads, and test coverage.

The website can be found at https://structorizer.fisch.lu

You may have look at the elaborate [user guide](https://help.structorizer.fisch.lu/index.php) in particular.

# Why having started this project?

In fact, I was not satisfied by the result of other NSD-editors, so I started writing my own one. I think I started drawing the first schemes and thinking about its internal structure in July 2006. The first lines of code were written during the summer and for September a first more or less functional version was available.


# Project history

I will not recite the entire [changelog](https://github.com/fesch/Structorizer.Desktop/blob/master/src/lu/fisch/structorizer/gui/changelog.txt) here, but just a few lines that, I think so, could be of interest:

* The first version, called "Structorizer 2006", was written in "Delphi 6 PE".
* In January 2007 I decided to rename the project as "Structorizer", since "2006" did no longer apply and a lot of new features waited to be integrated.
* Somewhat later in 2007 I ported the project for a first time to "Lazarus". I published a Linux version as well as the source code under the terms of the GPL license and called this sub-project "openStructorizer".
* I got my first Mac in May 2007 and wanted to have the application run in native mode. I did a second and this time complete porting of the project to "Lazarus". Waiting for the next release to be published, I renamed the old project as "Structorizer (Delphi)" and stopped both of them.
* I intended to publish Windows, Linux, and Mac OSX (intel) portings of the new "Structorizer (Lazarus)" project. This time, it would be completely open-source!
* After the release of the Lazarus version, major problems were detected for the Windows port. This is why I came back to the Delphi code and continued developing "Structorizer (Delphi)", which would be published as freeware.
* Due to too many problems with Lazarus, I decided to implement a Structorizer version in Java in December 2007. This version, which has also been released under the terms of the GPL, was given the major release number 3 and became the basis for all the versions 3.x until now.
