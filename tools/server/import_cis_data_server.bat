@echo off
cd /d "%~dp0\..\.."
if not exist config.server.properties (
    echo config.server.properties not found.
    echo Copy docs\setup\templates\config.server.properties.example to config.server.properties and fill the real server values first.
    exit /b 1
)
java -Drvs.config=config.server.properties -cp "out;lib\mysql-connector-j-9.6.0.jar" com.rvsfishworld.tools.CisDataImporter --mode=full-mirror
