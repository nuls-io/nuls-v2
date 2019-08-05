@echo off
if "%OS%" == "Windows_NT" SETLOCAL ENABLEDELAYEDEXPANSION

set MODULE_PATH=%~dp0
cd /d %MODULE_PATH%
set NULS_JAVA_HOME=Libraries\JAVA\JRE\11.0.2
if not exist "%NULS_JAVA_HOME%\bin\java.exe" goto noJavaHome
for %%a in (%NULS_JAVA_HOME%) do SET NULS_JAVA_HOME=%%~fa

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
if not exist "%CONFIG%" (
    set _CONFIG=nuls.ncf;
)
SET LOGLEVEL=ERROR
SET command=cmd
SET PARAM=
SET JAVAOPT=

:GETPARAM
   SET pn=%1
   if %pn%! == ! goto ENDGETPARAM
   if %pn% == -l GOTO SETLOGLEVEL
   if %pn% == -c GOTO SETCONFIG
   if %pn% == -h GOTO SETNULSTART_URL
   shift
   GOTO GETPARAM
:SETNULSTART_URL
SET NULSTART_URL=%2
shift
GOTO GETPARAM
:SETCONFIG
SET _CONFIG=%2
shift
GOTO GETPARAM
:SETLOGLEVEL
SET LOGLEVEL=%2
shift
GOTO GETPARAM
:ENDGETPARAM

SET JAVAOPT=%JAVAOPT% -Dlog.level=%LOGLEVEL%

for %%a in (%_CONFIG%) do SET CONFIG_FILE=%%~fa
ECHO USE CONFIG : %CONFIG_FILE%
SET JAVAOPT=%JAVAOPT% -Dactive.config=%CONFIG_FILE%
if "%NULSTART_URL%" == ""  SET NULSTART_URL=ws://127.0.0.1:7771
echo "Service Manager URL: %NULSTART_URL%"
cd .\Modules\Nuls\cmd-client\1.0.0
rem echo "cmd.bat %NULS_JAVA_HOME% %JAVAOPT% %NULSTART_URL% %command% %PARAM%"
cmd.bat "%NULS_JAVA_HOME%\bin\java" "%JAVAOPT%" "%NULSTART_URL%" "%command%" "%PARAM%"

:end