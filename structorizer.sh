VERSION=$(java -version 2>&1 | head -1 | cut -d. -f 2 )
if [ $VERSION -ne 8 ]
then
  echo "Your Java Version is $VERSION, but version 8 is required. Please update."
  exit 1
else
  java -jar $(dirname "$0")/Structorizer.app/Contents/Java/Structorizer.jar "$@"
fi

