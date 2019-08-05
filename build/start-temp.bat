@echo off
if "%OS%" == "Windows_NT" SETLOCAL ENABLEDELAYEDEXPANSION
set APP_NAME="%APP_NAME%"
set MODULE_PATH=%~dp0
echo %MODULE_PATH%
cd /d %MODULE_PATH%
REM SET START_DATE=%date% %time:~0,2%-%time:~3,2%
SET START_date=%date:~0,4%%date:~5,2%%date:~8,2%%hour%%time:~3,2%%time:~6,2%
SET LOG_PATH=..\..\..\..\Logs
SET CONFIG_FILE=..\..\..\..\nuls.ncf
SET DATA_PATH=..\..\..\..\data
if exist "%LOG_PATH%" goto logDirOk
echo create log dir
mkdir "%MODULE_PATH%\log"
:logDirOk
:GETPARAM
   SET pn=%1
   if %pn%! == ! goto ENDGETPARAM
   if %pn% == --jre GOTO SETJAVA_HOME
   if %pn% == --managerurl GOTO SETMANAGERURL
   if %pn% == --config GOTO SETCONFIG
   shift
   GOTO GETPARAM
:SETCONFIG
SET CONFIG_FILE=%2
shift
GOTO :GETPARAM
:SETJAVA_HOME
SET JRE_HOME=%2
shift
GOTO :GETPARAM
:SETMANAGERURL
SET NulstarUrl=%2
shift
GOTO GETPARAM
:ENDGETPARAM
ECHO JRE_HOME: %JRE_HOME%
ECHO NulstarUrl: %NulstarUrl%
if %JRE_HOME%! == ! SET JRE_HOME=%JAVA_HOME%
if %NulstarUrl%! == ! goto PARAMERROR
GOTO PARAMOK
:PARAMERROR
ECHO param error. require --jre and --managerurl
goto end
:PARAMOK
:CHECKISRUNING
for %%a in (%CONFIG_FILE%) do SET CONFIG_FILE=%%~fa
echo %CONFIG_FILE%
for /f %%a in ("%CONFIG_FILE%") do (
	set CONFIG_PATH=%%~dpa
)
for /f "tokens=1,2 delims== eol=#" %%i in (%CONFIG_FILE%) do (
     if %%i == logPath (
		SET LOG_PATH=%%j
	 )
	 if %%i == dataPath (
		SET DATA_PATH=%%j
	 )
	 if %%i == logLevel (
		SET LOG_LEVEL=%%j
	 )
)

set LOG_PATH=%CONFIG_PATH%%LOG_PATH%\%APP_NAME%
SET DATA_PATH=%CONFIG_PATH%%DATA_PATH%
SET LOG_LEVEL=%LOG_LEVEL%

ECHO "CONFIG:=====>%CONFIG_FILE%"
ECHO "LOG_PATH:===>%LOG_PATH%"
ECHO "LOG_LEVEL:==>%LOG_LEVEL%"
ECHO "DATA_PATH:==>%DATA_PATH%"


rem %Version reject
SET VERSION=%VERSION%
rem %MAIN_CLASS reject
SET MAIN_CLASS=%MAIN_CLASS%
REM # JOPT_XMS reject
SET JOPT_XMS=%JOPT_XMS%
rem JOPT_XMX reject
SET JOPT_XMX=%JOPT_XMX%
rem # %JOPT_METASPACESIZE reject
SET JOPT_METASPACESIZE=%JOPT_METASPACESIZE%
rem # %JOPT_MAXMETASPACESIZE reject
SET JOPT_MAXMETASPACESIZE=%JOPT_MAXMETASPACESIZE%
rem  # %JAVA_OPTS reject
SET JAVA_OPT=%JAVA_OPTS%
set JAR_FILE=%MODULE_PATH%%APP_NAME%-%VERSION%.jar

REM echo "%JAR_FILE% > %LOG_PATH%log.log"
SET CLASSPATH=""
for /f %%i in (dependent.conf) do (
    SET CLASSPATH=!CLASSPATH!..\..\libs\%%i;
)

SET CLASSPATH=-classpath %CLASSPATH%%JAR_FILE%
REM ;%JAR_FILE%
REM SET CPOPT=-cp "%JAR_FILE%"
SET JAVA_OPT=-server -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -Xms256m -Xmx256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+ParallelRefProcEnabled -XX:+TieredCompilation -XX:+ExplicitGCInvokesConcurrent
SET JAVA_OOM_DUMP=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_PATH%\oom-%START_DATE%.hprof -Dlog.path=%LOG_PATH% -DdataPath=%DATA_PATH% -Dlog.level=%LOG_LEVEL% -Dactive.config=%CONFIG_FILE%
SET JAVA_OPT=%JAVA_OPT% %JAVA_GC_LOG% %JAVA_OOM_DUMP%  -Dapp.name=%APP_NAME%


REM ECHO "account IS STARTING"
REM ECHO "account START CMD: %JRE_HOME%\bin\java %JAVA_OPT% %CLASSPATH% %MAIN_CLASS% %NulstarUrl% > %LOG_PATH%log.log"
REM ECHO "account log file : %LOG_PATH%log.log"

REM "%JRE_HOME%bin\java" %JAVA_OPT% %CLASSPATH% %MAIN_CLASS% %NulstarUrl%

"%JRE_HOME%\bin\java" %JAVA_OPT% %CLASSPATH% %MAIN_CLASS% %NulstarUrl%


:end
