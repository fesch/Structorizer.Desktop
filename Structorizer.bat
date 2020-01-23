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
::      Kay Gürtzig                   2016.05.03    First Issue
::      Kay Gürtzig                   2017.07.04    Drive variable added to path
::      Kay Gürtzig                   2018.11.27    Precaution against installation path with blanks
::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
java -jar "%~d0%~p0Structorizer.app\Contents\Java\Structorizer.jar" %*
