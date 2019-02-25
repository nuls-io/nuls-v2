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
${JAVA} -server -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH
