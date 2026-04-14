@echo off
cd /d "%~dp0"
call tools\package_go_live_data.bat %*
