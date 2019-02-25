#!/bin/bash
MODULE_PATH=$(cd `dirname $0`;pwd)
cd "${MODULE_PATH}"
LOGS_DIR="$MODULE_PATH/log"
if [ ! -d $LOGS_DIR ]; then 
    mkdir $LOGS_DIR
fi
START_DATE=`date +%Y%m%d%H%M%S`
STDOUT_FILE=$LOGS_DIR/stdout.log
echoRed() { echo $'\e[0;31m'$1$'\e[0m'; }
echoGreen() { echo $'\e[0;32m'$1$'\e[0m'; }
echoYellow() { echo $'\e[0;33m'$1$'\e[0m'; }
log(){
    now=`date "+%Y-%m-%d %H:%M:%S"`
    echo "${now}    $@" >> ${STDOUT_FILE}
    echoGreen "$@"
}
#获取参数
if [ ! -n "$1" ]; then 
    echo "args is error" 
    exit 0;
fi

while [ ! -z $1 ] ; do
    case "$1" in
        "--jre") 
            #log "jre path : $2"
            JAVA_HOME=$2
            shift 2 ;;
        "--managerurl") 
            #log "NulstarUrl is : $2"; 
            NulstarUrl=$2;    
            shift 2 ;;
        * ) shift
    esac
done  
APP_NAME="%APP_NAME%" # %APP_NAME 注入 
if [ -z "${APP_NAME}" ]; then
    echoRed "APP_NAME 未配置"
    exit 0;
fi
VERSION="%Version%"; # %Version 注入
JAR_FILE="${MODULE_PATH}/${APP_NAME}-${VERSION}.jar"
MAIN_CLASS="%MAIN_CLASS%" # MAIN_CLASS 注入
JOPT_XMS="%JOPT_XMS%"  # JOPT_XMS 注入
JOPT_XMX="%JOPT_XMX%"    # JOPT_XMX 注入
JOPT_METASPACESIZE="%JOPT_METASPACESIZE%"  # %JOPT_METASPACESIZE 注入
JOPT_MAXMETASPACESIZE="%JOPT_MAXMETASPACESIZE%"  # %JOPT_MAXMETASPACESIZE 注入
JAVA_OPTS="%JAVA_OPTS%"  # %JAVA_OPTS 注入


checkLogDir(){
    if [ ! -d ${LOGS_DIR} ]; then
        mkdir ${LOGS_DIR}
    fi
}

checkIsRunning(){
    if [ ! -z "`ps -ef|grep -w "name=${APP_NAME} "|grep -v grep|awk '{print $2}'`" ]; then
        pid=`ps -ef|grep -w "name=${APP_NAME} "|grep -v grep|awk '{print $2}'`
        log "$APP_NAME Already running pid=$pid";
        exit 0;
    fi
}


# 检查java版本
checkJavaVersion(){
    JAVA="$JAVA_HOME/bin/java"
    if [[ ! -r "$JAVA" ]]; then
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
CLASSPATH=" -classpath ../../libs/*:${JAR_FILE} "
JAVA_OPTS=" -server -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -Xms${JOPT_XMS}m -Xmx${JOPT_XMX}m -XX:MetaspaceSize=${JOPT_METASPACESIZE}m -XX:MaxMetaspaceSize=${JOPT_MAXMETASPACESIZE}m -XX:+ParallelRefProcEnabled -XX:+TieredCompilation -XX:+ExplicitGCInvokesConcurrent $JAVA_OPTS"
JAVA_OOM_DUMP="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGS_DIR}/oom-${START_DATE}.hprof"
JAVA_OPTS="$JAVA_OPTS $JAVA_GC_LOG $JAVA_OOM_DUMP  -Dsys.name=$APP_NAME "
# echo "${JAVA} ${JAVA_OPTS} ${CLASSPATH} ${MAIN_CLASS} ${NulstarUrl}"
nohup ${JAVA} ${JAVA_OPTS} ${CLASSPATH} ${MAIN_CLASS} ${NulstarUrl} > ${STDOUT_FILE} 2>&1 &

log "${APP_NAME} IS STARTING"
log "${APP_NAME} START CMD: ${JAVA} ${JAVA_OPTS} ${CLASSPATH} ${MAIN_CLASS} ${NulstarUrl}"
log "${APP_NAME} 日志文件: ${STDOUT_FILE}"
# echo "${APP_NAME} start cmd:" $'\e[0;31m'${JAVA} ${JAVA_OPTS} ${CLASSPATH} ${MAIN_CLASS} ${NulstarUrl}$'\e[0m'
# echo "${APP_NAME} 日志文件: " $'\e[0;31m'${STDOUT_FILE}$'\e[0m'

