@echo off
chcp 65001 > nul
cd /d "%~dp0"
if not exist out (
  echo Dang build...
  mkdir out
  dir /s /b src\*.java > sources.txt
  javac -encoding UTF-8 -d out @sources.txt
  del sources.txt
)
java -Dfile.encoding=UTF-8 -cp out com.minimart.Main
pause
