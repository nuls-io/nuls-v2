#!/bin/bash
cd `dirname $0`

echo "do stop" >> ./Logs/stop
KILL_WAIT_COUNT=120
stop(){
    pid=$1;
    kill $pid > /dev/null 2>&1
    COUNT=0
    while [ $COUNT -lt ${KILL_WAIT_COUNT} ]; do
        echo -e ".\c"
        sleep 1
        let COUNT=$COUNT+1
        PID_EXIST=`ps -f -p $pid | grep -w $2`
        if [ -z "$PID_EXIST" ]; then
#            echo -e "\n"
#            echo "stop ${pid} success."
            return 0;
        fi
    done

    echo "stop ${pid} failure,dump and kill -9 it."
    kill -9 $pid > /dev/null 2>&1
}
BIN_PATH=`pwd`
APP_PID=`ps -ef|grep -w "${BIN_PATH}/Modules/Nulstar/Nulstar/0.1.0/Nulstar"|grep -v grep|awk '{print $2}'`
if [ -z "${APP_PID}" ]; then
 echo "Nuls wallet not running"
        exit 0
fi
echo "stoping"
for pid in $APP_PID
do
   stop $pid "`pwd`/Modules/Nulstar/Nulstar/0.1.0/Nulstar"
done
echo ""
echo "shutdown success"
