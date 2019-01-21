#!/bin/sh

SOURCE="$0"
MODULE="$1"
SERVER_HOME="$( cd -P "$( dirname "$SOURCE"  )" && cd .. && pwd  )"

export logdir=${SERVER_HOME}/logs/${MODULE}

CLASSPATH=${SERVER_HOME}
# add conf to classpath
if [ -d ${SERVER_HOME}/resources/${MODULE} ]; then
  CLASSPATH=${CLASSPATH}:${SERVER_HOME}/resources/${MODULE}
fi

# add libs to CLASSPATH
for file in ${SERVER_HOME}/libs/*.jar; do
  CLASSPATH=${CLASSPATH}:${file};
done

# Get standard environment variables
JAVA_OPTS="-Dfile.encoding=UTF-8 -server"

#MAIN_CLASS=io.nuls.block.BlockBootstrap
MAIN_JAR=${MODULE}-1.0-SNAPSHOT.jar

if [ ! -d ${logdir} ]; then
  mkdir ${logdir}
fi

JAVA_BIN=`which java`
# try to use JAVA_HOME jre
if [ -x ${JAVA_BIN} ]; then
  #nohup ${JAVA_BIN} ${JAVA_OPTS} -classpath ${CLASSPATH} ${MAIN_CLASS} 1>${SERVER_HOME}/logs/stdout.log 0>${SERVER_HOME}/logs/stderr.log 2>&1 &
  ${JAVA_BIN} ${JAVA_OPTS} -classpath ${CLASSPATH} -jar ${MAIN_JAR}
  exit 0
fi

echo "The JDK required was not found."
exit 1
