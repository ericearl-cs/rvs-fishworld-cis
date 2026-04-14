@echo off
cd /d "%~dp0"
call tools\server\import_cis_data_server.bat %*
