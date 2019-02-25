#!/bin/bash
cd `dirname $0`
APP_NAME="%APP_NAME%"
KILL_WAIT_COUNT=120
MODULE_PATH=$(cd `dirname $0`;pwd)
LOGS_DIR="$MODULE_PATH/log"
STDOUT_FILE=$LOGS_DIR/stdout.log
APP_PID=`ps -ef|grep -w "name=${APP_NAME} "|grep -v grep|awk '{print $2}'`
APP=0
echoRed() { echo $'\e[0;31m'$1$'\e[0m'; }
echoGreen() { echo $'\e[0;32m'$1$'\e[0m'; }
echoYellow() { echo $'\e[0;33m'$1$'\e[0m'; }
log(){
    now=`date "+%Y-%m-%d %H:%M:%S"`
    echo "${now}    $@" >> STDOUT_FILE
    echoGreen "$@"
}
isRunning() {
  ps -p $1 &> /dev/null
}
stop(){
    pid=$1;
    log "stopping ${APP_NAME}@${pid}"
    kill $pid > /dev/null 2>&1
    COUNT=0
    while [ $COUNT -lt ${KILL_WAIT_COUNT} ]; do
        echo -e ".\c"
        sleep 1
        let COUNT=$COUNT+1
        PID_EXIST=`ps -f -p $pid | grep java`
        if [ -z "$PID_EXIST" ]; then
            echo -e "\n"
            log "stop ${APP_NAME}@${pid} success."
            exit 0;
        fi
    done

    log "stop ${APP_NAME}@${pid} failure,dump and kill -9 it."
    kill -9 $pid > /dev/null 2>&1
}

#if [ x$1 = x ]; then
#      echoRed "Usage: $0 appName";
#      exit 1;
#fi
APP=`ps -ef|grep -w "name=${APP_NAME} "|grep -v grep|wc -l`
if [[ $APP -eq 1 ]]; then
    PID_EXIST=`ps -f -p ${APP_PID} | grep java`
    if [ ! -z "$PID_EXIST" ]; then
        stop ${APP_PID}
    else
        echoRed "${APP_NAME} is not running"
    fi
elif [[ $APP -eq 0 ]]; then
    echoRed "${APP_NAME} is not running"
else
    echoRed "${APP_NAME} is exception"
fi
