#!/bin/bash
modules=(%MODULES%)

. func.sh

logPath=`getModuleItem "./nuls.ncf" "logPath"`

#if [ ! -d "$1" ]; then
#    echo "必须指定logs目录: ./check-status.sh <log path>"
#    exit 0;
#fi
clear
echoRed() { echo $'\e[0;31m'$1$'\e[0m'; }
echoGreen() { echo $'\e[0;32m'$1$'\e[0m'; }
while [ 1 == 1 ]
do
echo "==================RPC REDAY MODULE=================="
for module in ${modules[@]}
do
	#echo ${module}
	#grep -n 'RMB:module rpc is ready' Modules/Nuls/${module}/1.0.0/log/stdout.log
	if [ -n "`grep -n 'RMB:module rpc is ready' $logPath/${module}/stdout.log`" ];
	then
		echoGreen "${module} RPC READY"
		else
		echoRed "${module} RPC NOT READY"	
	fi
done

echo "==================REDAY MODULE=================="
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module is READY' $logPath/${module}/stdout.log`" ];
	then
		echoGreen "${module} STATE IS READY"
		else
		echoRed "${module} STATE NOT READY"	
	fi
done

echo "==================TRY RUNNING MODULE=================="
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module try running' $logPath/${module}/stdout.log`" ];
	then
		echoGreen "${module} TRY RUNNING"
		else
		echoRed "${module} NOT TRY RUNNING"	
	fi
done

echo "==================RUNNING MODULE=================="
isReady=1
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module state : Running' $logPath/${module}/stdout.log`" ];
	then
		echoGreen "${module} STATE IS RUNNING"
		else
		isReady=0
		echoRed "${module} STATE NOT RUNNING"	
	fi
done
echo "==================NULS WALLET STATE=================="
if [ $isReady == 1 ];
then
    echoGreen "=========================="
    echoGreen "NULS WALLET IS RUNNING"
    echoGreen "=========================="
    exit 0;
else
    echoRed "NULS WALLET NOT RUNNING"
    sleep 2
    clear;
fi
done



