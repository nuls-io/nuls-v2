#!/bin/bash
#cd ./mykernel/1.0.0
BIN_PATH=$(cd $(dirname $0); pwd);
cd $BIN_PATH;
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
            c)     
                    CONFIG="`get_fullpath $OPTARG`/${OPTARG##*/}"
#                    if [ "${CONFIG##*.}"x != "properties"x ]; then
#                        echo "-c setting config file must be *.ncf"
#                        exit 1;
#                    fi
                    ;;
            ?)     exit 2;;
           esac
done
if [ ! -f "$CONFIG" ]; then
    CONFIG="${BIN_PATH}/default-config.json"
fi
#
#if [ -f "$CONFIG" ]; then
#    if [ -f "./config.temp.properties" ]; then
#        rm ./config.temp.properties
#    fi
#    touch ./config.temp.properties
#    while read line
#	do
#	    pname=$(echo $line | awk -F '=' '{print $1}')
#        pvalue=$(awk -v a="$line" '
#                            BEGIN{
#                                len = split(a,ary,"=")
#                                r=""
#                                for ( i = 2; i <= len; i++ ){
#                                    if(r != ""){
#                                        r = (r"=")
#                                    }
#                                    r=(r""ary[i])
#                                }
#                                print r
#                            }
#                        ')
#        if [ -d "$pvalue" ]; then
#            pvalue=$(`dirname $pvalue`)
#        fi
#        if [ -f "$pvalue" ]; then
#            pvalue="`get_fullpath $pvalue`/${pvalue##*/}"
#        fi
#		echo "${pname}=${pvalue}" >> ./config.temp.properties
#	done < $CONFIG
#fi
#exit 0
JAVA="$JAVA_HOME/bin/java"
if [[ ! -r "$JAVA" ]]; then
    JAVA='java'
fi

JAVA_EXIST=`${JAVA} -version 2>&1 |grep 11`
if [ ! -n "$JAVA_EXIST" ]; then
    echo "JDK version is not 11"
    ${JAVA} -version
    exit 0;
fi
#echo "jdk version : `$JAVA -version `"
MODULE_PATH=$(cd `dirname $0`;pwd)
cd ../Modules/Nuls
MODULE_PATH=$(pwd)
if [ -z "${RUNBLOCK}" ];
then
    ${JAVA} -server -Dapp.name=mykernel  -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH $CONFIG
else
    nohup ${JAVA} -server -Dapp.name=mykernel  -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH $CONFIG > mykernel.log 2>&1 &
fi


