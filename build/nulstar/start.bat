@echo off
SET CONFIG="nuls.ncf"
if exist "%CONFIG%"  goto start
attrib .default-config.ncf -h
ren .default-config.ncf nuls.ncf
echo "nuls.ncf is created by default-config.ncf."
echo "Please re-excute the startup program."
goto end
:start
@echo off
SET ROOT_PATH=%~dp0
SET PATH=%ROOT_PATH%Libraries\CPP\Nulstar\0.1.0;%ROOT_PATH%Libraries\CPP\Qt\5.12.3;%PATH%
call %ROOT_PATH%Modules\Nulstar\Nulstar\0.1.0\Nulstar.exe
:end
