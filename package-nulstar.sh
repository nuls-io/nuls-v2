#!/bin/bash

help()
{
    cat <<- EOF
    Desc: 使用此脚本将生成符合NULSTAR规范的可执行子模块，
    	  所有子模块按照module.ncf配置，使用mvn命令进行打包，并生成启动、停止脚本
    Usage: ./package.sh 
    		-b <branch> 打包前同步最新代码 参数为同步的远程分支名称
    		-p 打包前同步最新代码 从master分支拉取
    		-o <目录>  指定输出目录
    		-h 查看帮助
    		-j JAVA_HOME
    		-J 输出的jvm虚拟机目录，脚本将会把这个目录复制到程序依赖中
    		-i 跳过mvn打包
    		-z 生成压缩包
    Author: zlj
EOF
    exit 0
}

#NULSTAR download url
NULSTAR_URL="http://pub-readingpal.oss-cn-hangzhou.aliyuncs.com/nulstar.tar.gz"
#获取参数
#输出目录
MODULES_PATH="./NULS-Wallet-linux64-alpha1"
#RELEASE_OUT_PATH="./NULS-Walltet-linux64-alpha1"
#是否马上更新代码
DOPULL=
#是否生成mykernel模块
DOMOCK=0
#更新代码的 git 分支
GIT_BRANCH=
while getopts phb:o:j:iJ:zmN name
do
            case $name in
            p)	   DOPULL=1
            	   GIT_BRANCH="master";;
            b)     DOPULL=1
				   GIT_BRANCH="$OPTARG"	 
					;;
            m)     DOMOCK=1;;
			o)	   MODULES_PATH="$OPTARG";;
			h)     help ;;
			j)     JAVA_HOME="$OPTARG";;
			i)     IGNROEMVN="1";;
			J)     JRE_HOME="$OPTARG";;
			z)     BUILDTAR="1";;
			N)     BUILD_NULSTAR="1";;
            ?)     exit 2;;
           esac
done
#日志打印函数
echoRed() { echo $'\e[0;31m'$1$'\e[0m'; } #print red
echoGreen() { echo $'\e[0;32m'$1$'\e[0m'; } #print green
echoYellow() { echo $'\e[0;33m'$1$'\e[0m'; } #print yellow
log(){ #print date prefix and green
    now=`date "+%Y-%m-%d %H:%M:%S"`
    echoGreen "$now $@"
}

# 检查java版本 must be 11
checkJavaVersion(){
    JAVA="$JAVA_HOME/bin/java"
    if [ ! -r "$JAVA" ]; then
        JAVA='java'
    fi

    JAVA_EXIST=`${JAVA} -version 2>&1 |grep 11`
    if [ ! -n "$JAVA_EXIST" ]; then
            log "JDK version is not 11"
            ${JAVA} -version
            exit 0
    fi
}

checkJavaVersion

#执行mvn函数打包java工程  $1 命令 $2 模块名称
doMvn(){
    if [ -n "$IGNROEMVN" ]; then
        log "skip mvn package";
        return ;
    fi
	log "mvn $1 $2"
	moduleLogDir="${BUILD_PATH}/tmp/$2";
	if [ ! -d ${moduleLogDir} ]; then
		mkdir ${moduleLogDir}
	fi
	installLog="${moduleLogDir}/log.log";
	mvn clean $1 -Dmaven.test.skip=true > "${installLog}" 2>&1
	mvnSuccess=`grep "BUILD SUCCESS" ${installLog}`
	if [ ! -n "$mvnSuccess" ]; then 
		echoRed "$1 $2 FAIL"
		echoRed "日志文件:${installLog}"
		cd ..
		exit 0
	fi	
	# rm $installLog;
	log "$1 $2 success"
}

#项目根目录
cd `dirname $0`
PROJECT_PATH=`pwd`;
cd $PROJECT_PATH;
log "working path is $PROJECT_PATH"; 
#打包工作目录
BUILD_PATH="${PROJECT_PATH}/build-nulstar";
if [ ! -d "${BUILD_PATH}/tmp" ]; then 
	mkdir "${BUILD_PATH}/tmp"
fi

if [ ! -d "${MODULES_PATH}" ]; then
	mkdir "${MODULES_PATH}"
fi
MODULES_PATH=`cd "$MODULES_PATH"; pwd`
RELEASE_PATH=$MODULES_PATH
echoYellow "Modules Path $MODULES_PATH"''
log "==================BEGIN PACKAGE MODULES=============================="
declare -a managedModules
#if [ ! -d "$MODULES_PATH/bin" ]; then
#	mkdir $MODULES_PATH/bin
#fi
#存放脚本目录
MODULES_BIN_PATH=$MODULES_PATH
if [ ! -d "$MODULES_PATH/Modules" ]; then
	#statements
	mkdir $MODULES_PATH/Modules
fi
#默认日志目录
MODULES_LOGS_PATH=${MODULES_PATH}/logs
if [ ! -d "$MODULES_LOGS_PATH" ]; then
	#statements
	mkdir $MODULES_LOGS_PATH
fi
MODULES_PATH=$MODULES_PATH/Modules
#创建NULS_2.0公共模块目录
if [ ! -d "$MODULES_PATH/Nuls" ]; then
	mkdir $MODULES_PATH/Nuls
fi
MODULES_PATH=$MODULES_PATH/Nuls
#模块公共依赖jar存放目录
COMMON_LIBS_PATH=$MODULES_PATH/libs
if [ -z "${IGNROEMVN}" ]; then
    if [ -d ${COMMON_LIBS_PATH} ]; then
        rm -r ${COMMON_LIBS_PATH}
    fi
    mkdir ${COMMON_LIBS_PATH}
fi

#模块数据库文件存放位置
COMMON_DATA_PATH=$MODULES_PATH/data
if [ ! -d ${COMMON_DATA_PATH} ]; then
    mkdir ${COMMON_DATA_PATH}
fi

#0.更新代码
if [ -n "${DOPULL}" ];then
	log "git pull origin $GIT_BRANCH"
	git pull origin "$GIT_BRANCH"
fi

#0.download Nulstar
if [ -n  "${BUILD_NULSTAR}" ]; then
    log "download Nulstar"
    wget $NULSTAR_URL
    if [ -f "./nulstar.tar.gz" ]; then
        tar -xvf "./nulstar.tar.gz" -C "${BUILD_PATH}/tmp"
        /bin/cp -Rf "${BUILD_PATH}/tmp/nulstar/" ${RELEASE_PATH}
        rm "./nulstar.tar.gz"
    fi
    log "build Nulstar done"
fi

#1.install nuls-tools
cd ./tools/nuls-tools
if [ ! -n `ls |grep pom.xml` ]; then
	echoRed "not found pom.xml"
	exit 0
fi
doMvn "install" "nuls-tools"

cd ../../


#检查module.ncf指定配置项是否存在
checkModuleItem(){
	if [ ! -f "./module.ncf" ]; then
		return 0
	fi
	if [ -z "$1" ]; then
		echoRed "getModuleItem 必须传入配置项名称"
		exit 1
	fi
    while read line
	do
		pname=`echo $line | awk -F '=' '{print $1}'`
		if [ "${pname}" == "$1" ]; then
			return 1;
		fi
	done < "$(pwd)/module.ncf"
	echoRed "$2 module.ncf 必须配置 $1"
	exit 0
}

getModuleItem(){
    while read line
	do
		pname=`echo $line | awk -F '=' '{print $1}'`
		pvalue=`awk -v a="$line" '
						BEGIN{
							len = split(a,ary,"=")
							r=""
							for ( i = 2; i <= len; i++ ){
								if(r != ""){
									r = (r"=")
								}
								r=(r""ary[i])
					 		}
							print r
						}
					'`
		if [ "${pname}" == $1 ]; then
			echo ${pvalue};
			return 1;
		fi
	done < "./module.ncf"
	return 0
}

#拷贝打好的jar包到Moules/Nuls/<Module Name>/<Version> 下
copyJarToModules(){
    if [ -z "$IGNROEMVN" ]; then
       doMvn "clean package" $1
    fi
	moduleName=`getModuleItem "APP_NAME"`;
	version=`getModuleItem "VERSION"`;
	if [ ! -d "${MODULES_PATH}/${moduleName}" ];then
		mkdir ${MODULES_PATH}/${moduleName}
	fi
	if [ -d "${MODULES_PATH}/${moduleName}/${version}" ]; then 
		rm -r "${MODULES_PATH}/${moduleName}/${version}"
	fi	
	mkdir "${MODULES_PATH}/${moduleName}/${version}"
	jarName=`ls target |grep .jar`
	nowPath=`pwd`
	echo "copy ${nowPath}/target/${moduleName}-${version}.jar to ${MODULES_PATH}/${moduleName}/${version}/${moduleName}-${version}.jar"
	cp ./target/${jarName} "${MODULES_PATH}/${moduleName}/${version}/${moduleName}-${version}.jar"
	if [ -d ./target/libs ]; then
		for jar in `ls ./target/libs`; do
			#statements
			cp "./target/libs/${jar}" "${COMMON_LIBS_PATH}"
		done
	fi
}


copyModuleNcfToModules(){
	moduleName=`getModuleItem "APP_NAME"`;
	version=`getModuleItem "VERSION"`;
	mainClass=`getModuleItem "MAIN_CLASS"`;
	mainClassName=`awk -v s="${mainClass}" 'BEGIN{ len = split(s,ary,"."); print ary[len]}'`
	moduleBuildPath="${BUILD_PATH}/tmp/$1"
	if [ ! -d "${moduleBuildPath}" ]; then
		mkdir "${moduleBuildPath}"
	fi	
	moduleNcf="${moduleBuildPath}/module.1.ncf";
	if [ -f $moduleNcf ]; then
		rm $moduleNcf
	fi
	touch $moduleNcf
	cfgDomain=""
	sedCommand="sed "
	while read line
	do
		TEMP=`echo $line|grep -Eo '\[.+\]'`
		if [ -n "$TEMP" ]; then
#		  echo "set cfg domain ${TEMP}"
		  cfgDomain=$TEMP
		fi
		if [ "${cfgDomain}" == "[JAVA]" -a ! -n "$TEMP" ];
		then
			pname=`echo $line | awk -F '=' '{print $1}'`
			#pvalue=$(echo $line | awk -F '=' '{print $2}')
			pvalue=`awk -v a="$line" '
						BEGIN{
							len = split(a,ary,"=")
							r=""
							for ( i = 2; i <= len; i++ ){
								if(r != ""){
									r = (r"=")
								}
								r=(r""ary[i])
					 		}
							print r
						}
					'`
            if [ "${pname}" != "" ]; then
			    sedCommand+=" -e 's/%${pname}%/${pvalue}/g' "
			fi
			echo $line >> $moduleNcf
		else

			if [ "${cfgDomain}" != "[JAVA]" ]; then
				echo $line >> $moduleNcf
			fi
		fi
	done < ./module.ncf
#	 merge common module.ncf and private module.ncf to module.tmep.ncf
	"${PROJECT_PATH}/build/merge-ncf.sh" "${BUILD_PATH}/module-prod.ncf" $moduleNcf
#	rm $moduleNcf
	sedCommand+=" -e 's/%MAIN_CLASS_NAME%/${mainClassName}/g' "
#    echo $sedCommand
	if [ -z `echo "${sedCommand}" | grep -o "%JOPT_XMS%"` ]; then
		sedCommand="${sedCommand}  -e 's/%JOPT_XMS%/256/g' "
	fi
	if [ -z `echo "${sedCommand}" | grep -o "%JAVA_OPTS%"` ]; then
		sedCommand="${sedCommand}  -e 's/%JAVA_OPTS%//g' "
	fi
	if [ -z `echo "${sedCommand}" | grep -o "%JOPT_XMX%"` ]; then
		sedCommand="${sedCommand}  -e 's/%JOPT_XMX%/256/g' "
	fi
	if [ -z `echo "${sedCommand}" | grep -o "%JOPT_METASPACESIZE%"` ]; then
		sedCommand="${sedCommand}  -e 's/%JOPT_METASPACESIZE%/128/g' "
	fi
	if [ -z `echo "${sedCommand}" | grep -o "%JOPT_MAXMETASPACESIZE%"` ]; then
		sedCommand="${sedCommand}  -e 's/%JOPT_MAXMETASPACESIZE%/256/g' "
	fi
    if [ -d "./script" ];
    then
        for file in `ls ./script`
        do
            nowPath=`pwd`
            eval "${sedCommand}  ${nowPath}/script/${file} > ${moduleBuildPath}/${file}"
            cp "${moduleBuildPath}/${file}" "${MODULES_PATH}/${moduleName}/${version}/${file}"
            chmod u+x "${MODULES_PATH}/${moduleName}/${version}/${file}"
            echo "copy ${moduleBuildPath}/${file} to ${MODULES_PATH}/${moduleName}/${version}/${file}"
        done
    else
    	startSh="${BUILD_PATH}/start-temp.sh"
        startBat="${BUILD_PATH}/start-temp.bat"
        stopSh="${BUILD_PATH}/stop-temp.sh"
        stopBat="${BUILD_PATH}/stop-temp.bat"
        # echo $sedCommand
        eval "${sedCommand}  ${startSh} > ${moduleBuildPath}/start.sh"
        cp "${moduleBuildPath}/start.sh" "${MODULES_PATH}/${moduleName}/${version}/start.sh"
        chmod +x "${MODULES_PATH}/${moduleName}/${version}/start.sh"
        echo "copy ${moduleBuildPath}/start.sh to ${MODULES_PATH}/${moduleName}/${version}/start.sh"

        eval "${sedCommand}  ${startBat} > ${moduleBuildPath}/start.bat"
        cp "${moduleBuildPath}/start.bat" "${MODULES_PATH}/${moduleName}/${version}/start.bat"
    #    cp "${moduleBuildPath}/start.bat" "/Volumes/share/start.bat"
        echo "copy ${moduleBuildPath}/start.bat to ${MODULES_PATH}/${moduleName}/${version}/start.bat"

        eval "${sedCommand}  ${stopSh} > ${moduleBuildPath}/stop.sh"
        cp "${moduleBuildPath}/stop.sh" "${MODULES_PATH}/${moduleName}/${version}/stop.sh"
        chmod +x "${MODULES_PATH}/${moduleName}/${version}/stop.sh"
        echo "copy ${moduleBuildPath}/stop.sh to ${MODULES_PATH}/${moduleName}/${version}/stop.sh"

        eval "${sedCommand}  ${stopBat} > ${moduleBuildPath}/stop.bat"
        cp "${moduleBuildPath}/stop.bat" "${MODULES_PATH}/${moduleName}/${version}/stop.bat"
        #cp "${moduleBuildPath}/stop.bat" "/Volumes/share/stop.bat"
        echo "copy ${moduleBuildPath}/stop.bat to ${MODULES_PATH}/${moduleName}/${version}/stop.bat"

    fi
	cp "${moduleBuildPath}/module.temp.ncf" "${MODULES_PATH}/${moduleName}/${version}/Module.ncf"
	echo "copy ${moduleBuildPath}/module.temp.ncf to ${MODULES_PATH}/${moduleName}/${version}/Module.ncf"
}

#2.遍历文件夹，检查第一个pom 发现pom文件后通过mvn进行打包，完成后把文件jar文件和module.ncf文件复制到Modules文件夹下
packageModule() {
	if [ ! -d "./$1" ]; then
		return 0
	fi
	if [ "$1" == "tmp" ]; then
	    return 0
	fi
	cd ./$1
#	echo `pwd`
#	echo ${RELEASE_PATH}
	if [ `pwd` == "${RELEASE_PATH}" ]; then
	    cd ..
		return 0;
	fi
	nowPath=`pwd`
	if [ -f "./module.ncf" ]; then
		echoYellow "find module.ncf in ${nowPath}"
		if [ ! -f "./pom.xml" ]; then
			echoRed "模块配置文件必须与pom.xml在同一个目录 : ${nowPath}"
			exit 0;
		fi
		managed=`getModuleItem "Managed"`;
		if [ "${managed}" != "-1" ];
		then
            checkModuleItem "APP_NAME" "$1"
            checkModuleItem "VERSION" "$1"
            checkModuleItem "MAIN_CLASS" "$1"
		    log "build $1"
            copyJarToModules $1
            copyModuleNcfToModules $1
            if [ "${managed}" == "1" ]; then
                moduleName=`getModuleItem "APP_NAME"`;
                managedModules[${#managedModules[@]}]="$moduleName"
            fi
            log "build $1 done"
        else
            echoYellow "$1 skip"
		fi
		cd ..
		return 0
	fi
    for f in `ls`
    do
        packageModule $f
    done
    cd ..
}

for fi in `ls`
do
    if [ "$fi"x != "tools"x ]; then
    	packageModule $fi
    fi
done
log "============ PACKAGE MODULES DONE ==============="
cd $PROJECT_PATH
if [ -n "${JRE_HOME}" ]; then
log "============ COPY JRE TO libs ==================="

    if [ ! -d "${JRE_HOME}" ];
    then
        echoRed "JRE_HOME 必须是文件夹"
        else
        log "JRE_HOME IS ${JRE_HOME}"
        LIBS_PATH="${RELEASE_PATH}/Libraries"
        if [ ! -d "${LIBS_PATH}" ]; then
            mkdir ${LIBS_PATH}
        fi
        if [ ! -d "${LIBS_PATH}/JAVA" ]; then
            mkdir "${LIBS_PATH}/JAVA"
        fi
        cp -r ${JRE_HOME} "${LIBS_PATH}/JAVA/11.0.2"
    fi
log "============ COPY JRE TO libs done ============"
fi
if [ -n "${DOMOCK}" ]; then
	log "============== BUILD start-mykernel script ====================="
	cp "${BUILD_PATH}/start-mykernel.sh" "${MODULES_BIN_PATH}/start.sh"
	chmod u+x "${MODULES_BIN_PATH}/start.sh"
	cp "${BUILD_PATH}/stop-mykernel.sh" "${MODULES_BIN_PATH}/stop.sh"
	chmod u+x "${MODULES_BIN_PATH}/stop.sh"
	cp "${BUILD_PATH}/default-config.ncf" "${MODULES_BIN_PATH}/nuls.ncf"
	chmod u+r "${MODULES_BIN_PATH}/nuls.ncf"
	cp "${BUILD_PATH}/cmd.sh" "${MODULES_BIN_PATH}/"
	chmod u+x "${MODULES_BIN_PATH}/cmd.sh"
	cp "${BUILD_PATH}/test.sh" "${MODULES_BIN_PATH}/"
	chmod u+x "${MODULES_BIN_PATH}/test.sh"
	cp "${BUILD_PATH}/func.sh" "${MODULES_BIN_PATH}/"
	chmod u+x "${MODULES_BIN_PATH}/func.sh"
	tempModuleList=
	for m in ${managedModules[@]}
	do
	    tempModuleList+=" \"${m}\""
	done
	eval "sed -e 's/%MODULES%/${tempModuleList}/g' ${BUILD_PATH}/check-status.sh > ${BUILD_PATH}/tmp/check-status-temp.sh"
	eval "sed -e 's/%MODULES%/${tempModuleList}/g' ${BUILD_PATH}/shutdown-nulstar.sh > ${BUILD_PATH}/tmp/shutdown-nulstar.sh"
    cp "${BUILD_PATH}/tmp/shutdown-nulstar.sh" "${MODULES_BIN_PATH}/shutdown.sh"
	chmod u+x "${MODULES_BIN_PATH}/shutdown.sh"
	cp "${BUILD_PATH}/tmp/check-status-temp.sh" "${MODULES_BIN_PATH}/check-status.sh"
	chmod u+x "${MODULES_BIN_PATH}/check-status.sh"
	log "============== BUILD start-mykernel script done ================"
fi

if [ -n "${BUILDTAR}" ]; then
    log "============ BUILD ${RELEASE_PATH}.tar.gz ==================="
    tar -zcPf "${RELEASE_PATH}.tar.gz" ${RELEASE_PATH}
    log "============ BUILD ${RELEASE_PATH}.tar.gz FINISH==================="
fi
log "============ ${RELEASE_PATH} PACKAGE FINISH 🍺🍺🍺🎉🎉🎉 ==============="
