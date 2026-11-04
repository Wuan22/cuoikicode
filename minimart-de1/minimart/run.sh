#!/bin/sh
cd "$(dirname "$0")"
if [ ! -d out ]; then
  echo "Dang build..."
  mkdir out
  javac -encoding UTF-8 -d out $(find src -name '*.java') || exit 1
fi
java -Dfile.encoding=UTF-8 -cp out com.minimart.Main
