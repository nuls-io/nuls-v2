#!/bin/sh

SOURCE="$0"
while [ -h "$SOURCE"  ]; do # resolve $SOURCE until the file is no longer a symlink
    DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd  )"
    SOURCE="$(readlink "$SOURCE")"
    [[ ${SOURCE} != /*  ]] && SOURCE=${DIR}/${SOURCE} # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done

SERVER_HOME="$( cd -P "$( dirname "$SOURCE"  )" && cd .. && pwd  )"

export logdir=${SERVER_HOME}/logs

CLASSPATH=${SERVER_HOME}
# add conf to classpath
if [ -d ${SERVER_HOME}/conf ]; then
  CLASSPATH=${CLASSPATH}:${SERVER_HOME}/conf
fi

# add jar to CLASSPATH
for file in ${SERVER_HOME}/*.jar; do
  CLASSPATH=${CLASSPATH}:${file};
done

# add libs to CLASSPATH
for file in ${SERVER_HOME}/libs/*.jar; do
  CLASSPATH=${CLASSPATH}:${file};
done

# Get standard environment variables
JAVA_OPTS="-Dfile.encoding=UTF-8 -server"

MAIN_CLASS=io.nuls.block.BlockBootstrap

if [ ! -d ${logdir} ]; then
  mkdir ${logdir}
fi

JAVA_BIN=`which java`
# try to use JAVA_HOME jre
if [ -x ${JAVA_BIN} ]; then
  #nohup ${JAVA_BIN} ${JAVA_OPTS} -classpath ${CLASSPATH} ${MAIN_CLASS} 1>${SERVER_HOME}/logs/stdout.log 0>${SERVER_HOME}/logs/stderr.log 2>&1 &
  ${JAVA_BIN} ${JAVA_OPTS} -classpath ${CLASSPATH} ${MAIN_CLASS}
  exit 0
fi

echo "The JDK required was not found."
exit 1