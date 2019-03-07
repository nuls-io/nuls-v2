#!/bin/bash
#cd ./mykernel/1.0.0
JAVA="$JAVA_HOME/bin/java"
if [[ ! -r "$JAVA" ]]; then
    JAVA='java'
fi

JAVA_EXIST=`${JAVA} -version 2>&1 |grep 11`
if [ ! -n "$JAVA_EXIST" ]; then
    log "JDK version is not 11"
    ${JAVA} -version
    exit 0;
fi
MODULE_PATH=$(cd `dirname $0`;pwd)
RUNBLOCK=
while getopts b name
do
            case $name in
            b)	   RUNBLOCK="1";;
            ?)     exit 2;;
           esac
done
if [ -z "${RUNBLOCK}" ];
then
    ${JAVA} -server -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH
else
    nohup ${JAVA} -server -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH  > mykernel.log 2>&1 &
fi


