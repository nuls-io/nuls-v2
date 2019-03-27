#!/bin/sh
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
cd ../Modules/Nuls/test/1.0.0
APP_PID=`ps -ef|grep -w "name=test "|grep -v grep|awk '{print $2}'`
PID_EXIST=`ps -f -p ${APP_PID} | grep java`
if [ ! -z "$PID_EXIST" ]; then
    echo "test module is running. please stop test module";
    exit 0
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
JAVA_OPTS="-Xms128m -Xmx128m -DtestNodeType=master -Dapp.name=test "
CLASSPATH=$CLASSPATH:$PUB_LIB:.
if [ -x ${JAVA} ]; then
  ${JAVA} $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS
  exit 0
fi
echo "The JAVA_HOME environment variable is not defined"
echo "This environment variable is needed to run this program"
exit 1