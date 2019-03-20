#!/bin/bash
#cd ./mykernel/1.0.0
function get_fullpath()
{
   _PWD=`pwd`
   if [ -d $1 ]; then
      cd $1
   elif [ -f $1 ]; then
      cd `dirname $1`
   else
      cd
   fi
   echo $(cd ..; cd -)
   cd ${_PWD} >/dev/null
}

RUNBLOCK=
while getopts bj:c: name
do
            case $name in
            b)     RUNBLOCK="1";;
            j)     JAVA_HOME="$OPTARG";;
            c)     CONFIG="`get_fullpath $OPTARG`/${OPTARG##*/}";;
            ?)     exit 2;;
           esac
done
echo $CONFIG
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
#echo "jdk version : `$JAVA -version `"
MODULE_PATH=$(cd `dirname $0`;pwd)

if [ -z "${RUNBLOCK}" ];
then
    ${JAVA} -server -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH $CONFIG
else
    nohup ${JAVA} -server -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH $CONFIG > mykernel.log 2>&1 &
fi


