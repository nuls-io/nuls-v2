@echo off
if "%OS%" == "Windows_NT" SETLOCAL

set MODULE_PATH=%~dp0
echo %MODULE_PATH%
cd /d %MODULE_PATH%

set NULS_JAVA_HOME=./Libraries/JAVA/JRE/11.0.2
if not "%NULS_JAVA_HOME%" == "" goto gotJavaHome
echo The NULS_JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome
if not exist "%NULS_JAVA_HOME%\bin\java.exe" goto noJavaHome
goto okJavaHome
:noJavaHome
if not "%JAVA_HOME%" == "" goto useSysJavaHome
echo The NULS_JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:useSysJavaHome
set NULS_JAVA_HOME=%JAVA_HOME%
goto okJavaHome
:okJavaHome
"%NULS_JAVA_HOME%\bin\java" -version

SET LOGLEVEL=ERROR
SET command=cmd
SET PARAM=
SET JAVAOPT=

SET JAVAOPT=%JAVAOPT% -Dlog.level=%LOGLEVEL%
if not exist "%CONFIG%" (
    set CONFIG=nuls.ncf;
)
for %%a in (%CONFIG%) do SET CONFIG_FILE=%%~fa
ECHO %CONFIG_FILE%
SET JAVAOPT=%JAVAOPT% -Dactive.config=%CONFIG_FILE%

for /f "tokens=1,2 delims== eol=#" %%i in (%CONFIG_FILE%) do (
	 if %%i == logLevel (
		SET LOG_LEVEL=%%j
	 )
)
REM if %NULSTART_URL% == "" (
    set NULSTART_URL=ws://127.0.0.1:7771
REM )
echo "Service Manager URL: %NULSTART_URL%"
cd .\Modules\Nuls\cmd-client\1.0.0
echo "cmd.bat %JAVA_HOME% %JAVAOPT% %NULSTART_URL% %command% %PARAM%"
cmd.bat "%JAVA_HOME%bin\java" "%JAVAOPT%" "%NULSTART_URL%" "%command%" "%PARAM%"
