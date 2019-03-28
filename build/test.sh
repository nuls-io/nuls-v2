#!/bin/sh
cd `dirname $0`;
if [ -d ../Libraries/JAVA/11.0.2 ]; then
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
LOGLEVEL="ERROR"
while getopts hl: name
do
            case $name in
            l)     LOGLEVEL="$OPTARG";;
            h)     help ;;
            ?)     exit 2;;
           esac
done
cd ../Modules/Nuls/test/1.0.0
APP_PID=`ps -ef|grep -w "name=test "|grep -v grep|awk '{print $2}'`
APP=`ps -ef|grep -w "name=test "|grep -v grep|wc -l`
if [ $APP -eq 1 ]; then
    PID_EXIST=`ps -f -p ${APP_PID} | grep java`
    if [ ! -z "$PID_EXIST" ]; then
        echo "test module is running. please stop test module";
        exit 0
    fi
fi
LIBS="../../libs"
PUB_LIB=""
MAIN_CLASS=io.nuls.test.TestModuleBootstrap
for jar in `find $LIBS -name "*.jar"`
do
 PUB_LIB="$PUB_LIB:""$jar"
done
PUB_LIB="${PUB_LIB}:./test-1.0.0.jar"
# Get standard environment variables
JAVA_OPTS="-Xms128m -Xmx128m -DtestNodeType=master -Dapp.name=test -Dlog.level=${LOGLEVEL} "
CLASSPATH=$CLASSPATH:$PUB_LIB:.
${JAVA} $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS  > "../logs/test/test-case.log" 2>&1 &