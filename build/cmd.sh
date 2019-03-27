#!/bin/sh

help()
{
    cat <<- EOF
    Desc: 启动NULS 2.0钱包命令行，
    Usage: ./cmd.sh
    		[-l] <log level> 输出的日志级别 默认ERROR
    		-h help
    Author: zlj
EOF
    exit 0
}
BIN_PATH=$(cd $(dirname $0); pwd);
cd $BIN_PATH;
if [[ -d ../Libraries/JAVA/11.0.2 ]]; then
    export JAVA_HOME="$(cd $(dirname "../Libraries/JAVA/11.0.2"); pwd)/11.0.2"
    export PATH=${PATH}:${JAVA_HOME}/bin
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
echo "JAVA_HOME:${JAVA_HOME}"
echo `java -version`
cd ../Modules/Nuls/cmdclient/1.0.0
LOGLEVEL="ERROR"
while getopts hl: name
do
            case $name in
            l)     LOGLEVEL="$OPTARG";;
            h)     help ;;
            ?)     exit 2;;
           esac
done
sh ./cmd.sh ${LOGLEVEL}