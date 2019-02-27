@echo off
if "%OS%" == "Windows_NT" setlocal
set APP_NAME="%APP_NAME%"
set MODULE_PATH=%~dp0
ECHO %MODULE_PATH%
SET START_DATE=%date% %time:~0,2%-%time:~3,2%
SET LOG_PATH=%MODULE_PATH%\log
if exist "%LOG_PATH%" goto logDirOk
echo create log dir
mkdir "%MODULE_PATH%\log"
:logDirOk
:GETPARAM
   SET pn=%1
   if %pn%! == ! goto ENDGETPARAM
   if %pn% == --jre GOTO SETJAVA_HOME
   if %pn% == --managerurl GOTO SETMANAGERURL
   shift
   GOTO GETPARAM
:SETJAVA_HOME
SET JRE_HOME=%2
shift
GOTO :GETPARAM
:SETMANAGERURL
SET MANAGERURL=%2
shift
GOTO GETPARAM
:ENDGETPARAM
ECHO JRE_HOME: %JRE_HOME%
ECHO MANAGERURL: %MANAGERURL%   
if %JRE_HOME%! == ! GOTO PARAMERROR
if %MANAGERURL%! == ! GOTO PARAMERROR
GOTO PARAMOK
:PARAMERROR
ECHO param error. require --jre and --managerurl 
goto end
:PARAMOK
:CHECKISRUNING
%JRE_HOME%\bin\jps | find "%MAIN_CLASS_NAME%" > temp
for /f %%a in (temp) do (
   ECHO %APP_NAME% is running pid: %%a
   goto end
)
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

echo "%JAR_FILE%"

SET CLASSPATH=-classpath ..\..\libs\*;%JAR_FILE%
SET JAVA_OPTS=-server -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -Xms%JOPT_XMS%m -Xmx%JOPT_XMX%m -XX:MetaspaceSize=%JOPT_METASPACESIZE%m -XX:MaxMetaspaceSize=%JOPT_MAXMETASPACESIZE%m -XX:+ParallelRefProcEnabled -XX:+TieredCompilation -XX:+ExplicitGCInvokesConcurrent %JAVA_OPT%
SET JAVA_OOM_DUMP=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGS_DIR}/oom-${START_DATE}.hprof
SET JAVA_OPT=%JAVA_OPT% %JAVA_GC_LOG% %JAVA_OOM_DUMP%  -Dsys.name=%APP_NAME% 
REM echo %JRE_HOME%\bin\java %JAVA_OPTS% %CLASSPATH% %MAIN_CLASS% %NulstarUrl% > log.log
ECHO "%APP_NAME% IS STARTING"
ECHO "%APP_NAME% START CMD: %JRE_HOME%\bin\java %JAVA_OPTS% %CLASSPATH% %MAIN_CLASS% %NulstarUrl%"
ECHO "%APP_NAME% log file : %LOG_PATH%\log.log"

%JRE_HOME%\bin\java %JAVA_OPTS% %CLASSPATH% %MAIN_CLASS% %NulstarUrl% > %LOG_PATH%\log.log




:end