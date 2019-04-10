#!/bin/bash
. func.sh
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

nulstarUrl=`getModuleItem "./nuls.ncf" "serviceManager"`
if [ -z "${nulstarUrl}" ]; then
    nulstarUrl="ws://127.0.0.1:7771"
fi

cd `dirname $0`;
if [ -d ../Libraries/JAVA/11.0.2 ]; then
    JAVA_HOME=`dirname "../Libraries/JAVA/11.0.2/bin"`;
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
echo "JAVA_HOME:${JAVA_HOME}"
echo `${JAVA} -version`
cd ./Modules/Nuls/cmdclient/1.0.0
LOGLEVEL="ERROR"
while getopts hl: name
do
            case $name in
            l)     LOGLEVEL="$OPTARG";;
            h)     help ;;
            ?)     exit 2;;
           esac
done
./cmd.sh ${JAVA_HOME} ${LOGLEVEL} ${nulstarUrl}
