#!/bin/bash
modules=(%MODULES%)

if [ ! -d "$1" ]; then
    echo "必须指定logs目录: ./check-status.sh <log path>"
    exit 0;
fi

echoRed() { echo $'\e[0;31m'$1$'\e[0m'; }
echoGreen() { echo $'\e[0;32m'$1$'\e[0m'; }
echo "==================RPC REDAY MODULE=================="
for module in ${modules[@]}
do
	#echo ${module}
	#grep -n 'RMB:module rpc is ready' Modules/Nuls/${module}/1.0.0/log/stdout.log
	if [ -n "`grep -n 'RMB:module rpc is ready' $1/${module}/stdout.log`" ];
	then
		echoGreen "${module} RPC READY"
		else
		echoRed "${module} RPC NOT READY"	
	fi
done

echo "==================REDAY MODULE=================="
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module is READY' $1/${module}/stdout.log`" ];
	then
		echoGreen "${module} STATE IS READY"
		else
		echoRed "${module} STATE NOT READY"	
	fi
done

echo "==================TRY RUNNING MODULE=================="
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module try running' $1/${module}/stdout.log`" ];
	then
		echoGreen "${module} TRY RUNNING"
		else
		echoRed "${module} NOT TRY RUNNING"	
	fi
done

echo "==================RUNNING MODULE=================="
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module state : Running' $1/${module}/stdout.log`" ];
	then
		echoGreen "${module} STATE IS RUNNING"
		else
		echoRed "${module} STATE NOT RUNNING"	
	fi
done



