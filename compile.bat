@echo off
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.6.0.jar" -d out @sources.txt

if exist sources.txt del sources.txt
