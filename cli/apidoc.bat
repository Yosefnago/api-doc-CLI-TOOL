@echo off
setlocal

set SCRIPT_DIR=%~dp0
java -jar "%SCRIPT_DIR%\apidoc-1.0.0.jar" %*

endlocal