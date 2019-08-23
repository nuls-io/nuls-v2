#!/bin/bash
MODULE_PATH=$(cd `dirname $0`;pwd)
cd "${MODULE_PATH}"

echo "MODULE_PATH is ${MODULE_PATH}"

LOGS_DIR="${MODULE_PATH}/log"

APP_NAME="nuls-sdk-provider" # %APP_NAME 注入

if [ -d ./JAVA/JRE/11.0.2 ]; then
    JAVA_HOME=`dirname "./JAVA/JRE/11.0.2/bin"`;
    JAVA_HOME=`cd $JAVA_HOME; pwd`
    JAVA="${JAVA_HOME}/bin/java"
else
    JAVA='java'
fi

JAVA_EXIST=`${JAVA} -version 2>&1 |grep 11`
if [ ! -n "$JAVA_EXIST" ]; then
    echo "JDK version is not 11"
    ${JAVA} -version
    exit 0;
fi

MAIN_CLASS="io.nuls.NulsModuleBootstrap" # MAIN_CLASS 注入
JOPT_XMS="64"  # JOPT_XMS 注入
JOPT_XMX="128"    # JOPT_XMX 注入
JOPT_METASPACESIZE="32"  # %JOPT_METASPACESIZE 注入
JOPT_MAXMETASPACESIZE="64"  # %JOPT_MAXMETASPACESIZE 注入
JAVA_OPTS=""  # %JAVA_OPTS 注入

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
		if [ "${pname}" == $2 ]; then
			echo ${pvalue};
			return 1;
		fi
	done < $1
	return 0
}

#获取绝对路径
function get_fullpath()
{
    if [ -f "$1" ];
    then
        tempDir=`dirname $1`;
        fileName=$1
        echo "`cd $tempDir; pwd`/${fileName##*/}";
    else
        echo `cd $1; pwd`;
    fi
}


echoRed() { echo -e $'\e[0;31m'$1$'\e[0m'; }
echoGreen() { echo -e $'\e[0;32m'$1$'\e[0m'; }
echoYellow() { echo -e $'\e[0;33m'$1$'\e[0m'; }
log(){
    now=`date "+%Y-%m-%d %H:%M:%S"`
    echo "${now}    $@" >> ${STDOUT_FILE}
    echoGreen "$@"
}

if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi
START_DATE=`date +%Y%m%d%H%M%S`
STDOUT_FILE=$LOGS_DIR/stdout.log

logpath="-Dlog.path=$LOGS_DIR";

cd $MODULE_PATH

checkLogDir(){
    if [ ! -d ${LOGS_DIR} ]; then
        mkdir ${LOGS_DIR}
    fi
}


checkIsRunning(){
    if [ ! -z "`ps -ef|grep -w "name=${APP_NAME} "|grep -v grep|awk '{print $2}'`" ]; then
        pid=`ps -ef|grep -w "name=${APP_NAME} "|grep -v grep|awk '{print $2}'`

        if [ -n "${RESTART}" ];
        then
            log "$APP_NAME Already running pid=$pid";
            log "do restart ${APP_NAM}"
            log "stop ${APP_NAME}@${pid} failure,dump and kill it."
            kill $pid > /dev/null 2>&1
        else
            echoRed "$APP_NAME Already running pid=$pid";
            exit 0;
        fi
    fi
}


# 检查java版本
checkJavaVersion(){
    JAVA="$JAVA_HOME/bin/java"
    if [ ! -r "$JAVA" ]; then
        JAVA='java'
    fi

    JAVA_EXIST=`${JAVA} -version 2>&1 |grep 11`
    if [ ! -n "$JAVA_EXIST" ]; then
            log "JDK version is not 11"
            ${JAVA} -version
            exit 0
    fi
}

checkJavaVersion
checkLogDir
checkIsRunning

CLASSPATH=""
# add conf to classpath
if [ -d ${MODULE_PATH}/conf ]; then
  CLASSPATH=${MODULE_PATH}/conf
fi

# add libs to CLASSPATH
for file in ${MODULE_PATH}/libs/*.jar; do
  CLASSPATH=${CLASSPATH}:${file};
done

CLASSPATH="-classpath ${CLASSPATH}"

JAVA_OPTS=" -server -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -Xms${JOPT_XMS}m -Xmx${JOPT_XMX}m -XX:MetaspaceSize=${JOPT_METASPACESIZE}m -XX:MaxMetaspaceSize=${JOPT_MAXMETASPACESIZE}m -XX:+ParallelRefProcEnabled -XX:+TieredCompilation -XX:+ExplicitGCInvokesConcurrent $JAVA_OPTS"
JAVA_OOM_DUMP="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGS_DIR}/oom-${START_DATE}.hprof"
JAVA_OPTS="$JAVA_OPTS $JAVA_OOM_DUMP  -Dapp.name=$APP_NAME ${logpath}"
#echo "${JAVA} ${JAVA_OPTS} ${CLASSPATH} ${MAIN_CLASS} offline"
CMD="${JAVA} ${JAVA_OPTS} ${CLASSPATH} ${MAIN_CLASS} offline"
CMD="${CMD} > ${STDOUT_FILE}"
CMD="$CMD 2>&1 & ";
eval $CMD
#nohup ${JAVA} ${JAVA_OPTS} ${CLASSPATH} ${MAIN_CLASS} ${NulstarUrl} > ${STDOUT_FILE} 2>&1 &

log "${APP_NAME} IS STARTING \n ${APP_NAME} 日志文件: ${STDOUT_FILE}"
# echo "${APP_NAME} start cmd:" $'\e[0;31m'${JAVA} ${JAVA_OPTS} ${CLASSPATH} ${MAIN_CLASS} ${NulstarUrl}$'\e[0m'
# echo "${APP_NAME} 日志文件: " $'\e[0;31m'${STDOUT_FILE}$'\e[0m'
