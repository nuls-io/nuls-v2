#!/bin/sh
PRG="$0"
APP_PID=`ps -ef|grep -w "name=test "|grep -v grep|awk '{print $2}'`
PID_EXIST=`ps -f -p ${APP_PID} | grep java`
if [ ! -z "$PID_EXIST" ]; then
    echo "test module is running. please stop test module";
    exit 0
fi
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

#SOURCE="$0"
#while [ -h "$SOURCE"  ]; do # resolve $SOURCE until the file is no longer a symlink
#    DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd  )"
#    SOURCE="$(readlink "$SOURCE")"
#    [[ $SOURCE != /*  ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
#done
#SERVER_HOME="$( cd -P "$( dirname "$SOURCE"  )" && pwd  )"
#echo $SERVER_HOME
#export logdir=${SERVER_HOME}/logs
#if [ ! -d ${logdir} ]; then
#  mkdir ${logdir}
#fi
SERVER_HOME="../../"
LIBS=$SERVER_HOME/libs
PUB_LIB=""
MAIN_CLASS=io.nuls.test.TestModuleBootstrap

for jar in `find $LIBS -name "*.jar"`

do
 PUB_LIB="$PUB_LIB:""$jar"
done
PUB_LIB="${PUB_LIB}:./test-1.0.0.jar"
# Get standard environment variables
JAVA_OPTS="-Xms128m -Xmx128m -DtestNodeType=master -Dapp.name=test "

CONF_PATH=$SERVER_HOME/conf
CLASSPATH=$CLASSPATH:$CONF_PATH:$PUB_LIB:.
echo $CLASSPATH
if  [ -x ${SERVER_HOME}/jre/bin/java ]; then
  ${SERVER_HOME}/jre/bin/java $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS
  exit 0
fi

JAVA="${JAVA_HOME}/bin/java"
# try to use JAVA_HOME jre
if [ -x ${JAVA_BIN} ]; then
  ${JAVA_BIN} $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS
  exit 0
fi

 $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS

echo "The JAVA_HOME environment variable is not defined"
echo "This environment variable is needed to run this program"
exit 1