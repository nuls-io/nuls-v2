@echo off
if "%OS%" == "Windows_NT" SETLOCAL ENABLEDELAYEDEXPANSION
SET JAVA=%1
SET JAVA_OPTS=%2
SET NULSTAR_URL=%3
if "%4" == "address" (
	SET MAIN_CLASS=io.nuls.cmd.client.Tools address %5
)ELSE (
    SET MAIN_CLASS=io.nuls.cmd.client.CmdClientBootstrap %NULSTAR_URL% %5
)

ECHO %MAIN_CLASS%

SET CLASSPATH=
for /f %%i in (dependent.conf) do (
    SET CLASSPATH=!CLASSPATH!..\..\libs\%%i;
)

set a="aaaaa"
echo %JAVA_OPTS:"=%

SET CLASSPATH=-classpath %CLASSPATH%cmd-client-1.0.0.jar
SET JAVA_OPTS=-Xms128m -Xmx128m -Dapp.name=cmd-client --add-opens java.base/java.lang=ALL-UNNAMED --illegal-access=warn %JAVA_OPTS:"=%
%JAVA% %JAVA_OPTS% %CLASSPATH% %MAIN_CLASS%