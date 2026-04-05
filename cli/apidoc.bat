@echo off
set SCRIPT_DIR=%~dp0
java -Djava.util.logging.config.file=%SCRIPT_DIR%\logging.properties -jar "%SCRIPT_DIR%\apidoc-2.0.0.jar" %*
