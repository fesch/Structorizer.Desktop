#!/bin/sh
set -e
################################################################################
#
#      Author:        Bob Fisch
#
#      Description:   start script for Structorizer
#
################################################################################
#
#      Revision List
#
#      Author                        Date          Description
#      ------                        ----          -----------
#      Bob Fisch                     20??.??.??    First Issue
#      Bob Fisch                     2017.??.??    Check for Java > 8
#      Rolf Schmidt                  2018.06.03    fixed version check for OpenJDK 10+, Java 8
#      Simon Sobisch                 2018.06.03    Check for jar, tweaked version checks
#      Bob Fisch                     2018.09.05    get correct dir if symlinked
#      Kay Gürtzig                   2018.09.19    Bugfix #604: Condition in jar test (line 31) corrected (#586)
#
################################################################################

# get dir of symblic
DIR="$(dirname "$(readlink -f "$0")")"

# check if JAVA binary is found
java 2>/dev/null 1>&2 || (rc=$? && if test $rc -gt 1; then (echo 'JAVA not found in $PATH' && exit $rc); fi)

# check for jar in PATH
if [ ! -f "$DIR/Structorizer.app/Contents/Java/Structorizer.jar" ]
then
	echo "$DIR/Structorizer.app/Contents/Java/Structorizer.jar not found."
	exit
fi

# check for correct Java version
JAVAVER=$(java -version 2>&1)

# Try new version scheme VER.MINOR.PATCHLEVEL first
VERSION=$(echo $JAVAVER | head -1 | cut -d. -f 1 | cut -d'"' -f 2)
if [ $VERSION -eq 1 ]
then
  # Fallback for old 1.VER.BUILD
  VERSION=$(echo $JAVAVER | head -1 | cut -d. -f 2 )
fi

if [ $VERSION -lt 8 ]
then
  echo "Your Java Version is $VERSION, but version 8 is required. Please update."
  exit 1
fi

# actual start
#echo "Your Java Version is $VERSION, all fine."
java -jar $DIR/Structorizer.app/Contents/Java/Structorizer.jar "$@"
