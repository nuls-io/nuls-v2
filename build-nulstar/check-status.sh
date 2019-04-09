#!/bin/bash
modules=(%MODULES%)

getModuleItem(){
    while read line
	do
		pname=`echo $line | awk -F '=' '{print $1}'`
		pvalue=`awk -v a="$line" '
						BEGIN{
							len = split(a,ary,"=")
							r=""
							for ( i = 2; i <= len; i++ ){
								if(r != ""){
									r = (r"=")
								}
								r=(r""ary[i])
					 		}
							print r
						}
					'`
		if [ "${pname}" == $1 ]; then
			echo ${pvalue};
			return 1;
		fi
	done < "./nuls.ncf"
	return 0
}

logPath=`getModuleItem "logPath"`

#if [ ! -d "$1" ]; then
#    echo "必须指定logs目录: ./check-status.sh <log path>"
#    exit 0;
#fi

echoRed() { echo $'\e[0;31m'$1$'\e[0m'; }
echoGreen() { echo $'\e[0;32m'$1$'\e[0m'; }
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
for module in ${modules[@]}
do
	if [ -n "`grep -n 'RMB:module state : Running' $logPath/${module}/stdout.log`" ];
	then
		echoGreen "${module} STATE IS RUNNING"
		else
		echoRed "${module} STATE NOT RUNNING"	
	fi
done



