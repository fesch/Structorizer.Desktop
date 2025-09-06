#!/bin/sh
set -e
################################################################################
#
#      Author:        Kay Gürtzig
#
#      Description:   start script for Translator component of Structorizer
#
################################################################################
#
#      Revision List
#
#      Author                        Date          Description
#      ------                        ----          -----------
#      Kay Gürtzig                   2025-08-18    First version derived from structorizer.sh
#
################################################################################

# get dir of symbolic link
DIR="$(dirname "$(readlink -f "$0")")"

# check if JAVA binary is found
java 2>/dev/null 1>&2 || (rc=$? && if test $rc -gt 1; then (echo 'JAVA not found in $PATH' && exit $rc); fi)

# check for jar in PATH
if [ ! -f "$DIR/Structorizer.app/Contents/Java/Structorizer.jar" ]
then
	echo "Error: $DIR/Structorizer.app/Contents/Java/Structorizer.jar not found."
	exit
fi

# check for correct Java version
REQVERSION=11
JAVAVER=$(java -version 2>&1)

# Try new version scheme VER.MINOR.PATCHLEVEL first
VERSION=$(echo $JAVAVER | head -1 | cut -d. -f 1 | cut -d'"' -f 2)
if [ $VERSION -eq 1 ]
then
  # Fallback for old 1.VER.BUILD
  VERSION=$(echo $JAVAVER | head -1 | cut -d. -f 2 )
fi

if [ $VERSION -lt $REQVERSION ]
then
  echo "Your Java version is $VERSION, but version $REQVERSION is required. Please update."
  exit 1
fi

# actual start
#echo "Your Java version is $VERSION, all fine."
java -jar "$DIR/Structorizer.app/Contents/Java/Structorizer.jar" lu.fisch.structorizer.locales.Translator -test
