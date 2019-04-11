@echo off
if "%OS%" == "Windows_NT" setlocal
set APP_NAME="%APP_NAME%"
jps | find "%MAIN_CLASS_NAME%" > temp
for /f %%a in (temp) do (
   set pid=%%a
   goto killAPP
)
ECHO %APP_NAME% is not running
goto end
:killAPP
taskkill /F /PID %pid%
goto end
:end
