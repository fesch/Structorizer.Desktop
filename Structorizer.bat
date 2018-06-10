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
::      Kay Gürtzig                   2018.06.10    Direct3D switch inserted
::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
java -Dsun.java2d.d3d=false -jar %~d0%~p0Structorizer.app\Contents\Java\Structorizer.jar %*