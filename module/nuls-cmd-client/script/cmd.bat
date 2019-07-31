@echo off
if "%OS%" == "Windows_NT" SETLOCAL ENABLEDELAYEDEXPANSION
SET JAVA=%1
SET JAVA_OPT=%2
SET NULSTAR_URL=%3
if "%4" == "address" (
	SET MAIN_CLASS=io.nuls.cmd.client.Tools address %5
)ELSE (
    SET MAIN_CLASS=io.nuls.cmd.client.CmdClientBootstrap %NULSTAR_URL% %5
)

SET CLASSPATH=
for /f %%i in (dependent.conf) do (
    SET CLASSPATH=!CLASSPATH!..\..\libs\%%i;
)

SET CLASSPATH=-classpath %CLASSPATH%cmd-client-1.0.0.jar
SET JAVA_OPT=-Xms128m -Xmx128m -Dapp.name=cmd-client --add-opens java.base/java.lang=ALL-UNNAMED --illegal-access=warn %JAVA_OPT:"=%
%JAVA% %JAVA_OPT% %CLASSPATH% %MAIN_CLASS%