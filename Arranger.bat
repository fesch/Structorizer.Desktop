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
::      Kay Gürtzig                   2018.06.10    First issue
::      Kay Gürtzig                   2018.06.12    Drive and path variables inserted
::      Kay Gürtzig                   2018.11.27    Precaution against installation path with blanks
::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
java -cp "%~d0%~p0Structorizer.app/Contents/Java/Structorizer.jar" lu.fisch.structorizer.arranger.Arranger
