#!/bin/bash
#PRG="$0"
#
#while [ -h "$PRG" ]; do
#  ls=`ls -ld "$PRG"`
#  link=`expr "$ls" : '.*-> \(.*\)$'`
#  if expr "$link" : '.*/.*' > /dev/null; then
#    PRG="$link"
#  else
#    PRG=`dirname "$PRG"`/"$link"
#  fi
#done

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
JAVA_HOME=$1
logLevel=$2
if [ -z "$logLevel" ]; then
    logLevel="ERROR"
fi
NULSTAR_URL=$3
config=$4
SERVER_HOME="../../"
LIBS=$SERVER_HOME/libs
PUB_LIB=""
if [ "$4" == "address" ];
then
    MAIN_CLASS="io.nuls.cmd.client.Tools address 1 "
    else
    MAIN_CLASS=io.nuls.cmd.client.CmdClientBootstrap
fi

JAVA=${JAVA_HOME}/bin/java
for jar in `find $LIBS -name "*.jar"`
do
 PUB_LIB="$PUB_LIB:""$jar"
done
PUB_LIB="${PUB_LIB}:./cmdclient-1.0.0.jar"
# Get standard environment variables

JAVA_OPTS="-Xms128m -Xmx128m -Dapp.name=cmd-client --illegal-access=warn -Dlog.level=${logLevel} -Dactive.module=${config} "

CONF_PATH=$SERVER_HOME/conf
CLASSPATH=$CLASSPATH:$CONF_PATH:$PUB_LIB:.
if [ -x ${JAVA} ]; then
  ${JAVA} $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS $NULSTAR_URL
  exit 0
fi

# $JAVA_OPTS -classpath $CLASSPATH $MAIN_CLASS

echo "The JAVA_HOME environment variable is not defined"
echo "This environment variable is needed to run this program"
exit 1