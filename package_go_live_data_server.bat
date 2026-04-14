@echo off
cd /d "%~dp0"
call tools\server\package_go_live_data_server.bat %*
