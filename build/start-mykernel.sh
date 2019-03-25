#!/bin/bash
#cd ./mykernel/1.0.0

help()
{
    cat <<- EOF
    Desc: 启动NULS 2.0钱包，
    Usage: ./start.sh
    		-c <module.json> 使用指定配置文件 如果不配置将使用./default-config.json
    		-b 后台运行
    		-l <logs path> 输出的日志目录
    		-d <data path> 数据存储目录
    		-j JAVA_HOME
    		-D debug模式，在logs目录下输出名为stdut.log的全日志文件
    		-h help
    Author: zlj
EOF
    exit 0
}

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
while getopts bj:c:l:d:Dh name
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
            l)
                   if [ ! -d "$OPTARG" ]; then
                       mkdir $OPTARG
                       if [ ! -d "$OPTARG" ]; then
                          echo "$OPTARG not a folder"
                       exit 0 ;
                       fi
                   fi
                   LOGPATH="`get_fullpath $OPTARG`";;
            d)
                   if [ ! -d "$OPTARG" ]; then
                       mkdir $OPTARG
                       if [ ! -d "$OPTARG" ]; then
                          echo "$OPTARG not a folder"
                       exit 0 ;
                       fi
                   fi
                   DATAPATH="`get_fullpath $OPTARG`";;
            D)     DEBUG="1";;
            h)     help ;;
            ?)     exit 2;;
           esac
done
if [ ! -f "$CONFIG" ]; then
    CONFIG="${BIN_PATH}/default-config.json"
fi
if [ -n "$LOGPATH" ];
then
    LOGPATH="-Dlog.path=${LOGPATH}"
    else
    mkdir ../logs
    LOGPATH="-Dlog.path=`get_fullpath ../logs`"
fi
if [ -n "$DATAPATH" ];
then
    DATAPATH="-DDataPath=${DATAPATH}"
    else
    mkdir ../Modules/Nuls/data
    DATAPATH="-DDataPath=`get_fullpath ../Modules/Nuls/data`"
fi
echo "log path : ${LOGPATH}"
echo "data path : ${DATAPATH}"
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
    ${JAVA} -server -Ddebug=${DEBUG} -Dapp.name=mykernel ${LOGPATH} ${DATAPATH} -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH $CONFIG
else
    nohup ${JAVA} -server -Ddebug=${DEBUG} -Dapp.name=mykernel ${LOGPATH} ${DATAPATH}  -classpath ./libs/*:./mykernel/1.0.0/mykernel-1.0.0.jar io.nuls.mykernel.MyKernelBootstrap startModule $MODULE_PATH $CONFIG > mykernel.log 2>&1 &
fi


