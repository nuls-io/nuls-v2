#!/bin/bash
modules=("network" "ledger" "account" "block" "poc-consensus" "protocol" "transaction" "chain")

echoRed() { echo $'\e[0;31m'$1$'\e[0m'; }
echoGreen() { echo $'\e[0;32m'$1$'\e[0m'; }
echo "==================RPC REDAY MODULE=================="
for module in ${modules[@]}
do
	#echo ${module}
	#grep -n 'RMB:module rpc is ready' Modules/Nuls/${module}/1.0.0/log/stdout.log
	if [ -n "`grep -n 'RMB:module rpc is ready' ${module}/1.0.0/log/stdout.log`" ]; 
	then
		echoGreen "${module} RPC READY"
		else
		echoRed "${module} RPC NOT READY"	
	fi
done

echo "==================REDAY MODULE=================="
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module is READY' ${module}/1.0.0/log/stdout.log`" ]; 
	then
		echoGreen "${module} STATE IS READY"
		else
		echoRed "${module} STATE NOT READY"	
	fi
done

echo "==================TRY RUNNING MODULE=================="
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module try running' ${module}/1.0.0/log/stdout.log`" ]; 
	then
		echoGreen "${module} TRY RUNNING"
		else
		echoRed "${module} NOT TRY RUNNING"	
	fi
done

echo "==================RUNNING MODULE=================="
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module state : Running' ${module}/1.0.0/log/stdout.log`" ]; 
	then
		echoGreen "${module} STATE IS RUNNING"
		else
		echoRed "${module} STATE NOT RUNNING"	
	fi
done



