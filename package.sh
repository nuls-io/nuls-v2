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
    		-m 生成mykernel模块以及启动脚本
    		-h 查看帮助
    Author: zlj
EOF
    exit 0
}


#获取参数
#输出目录
MODULES_PATH="./Modules"
#是否马上更新代码
DOPULL=
#是否生成mykernel模块
DOMOCK=
#更新代码的 git 分支
GIT_BRANCH=
while getopts pmhb:o: name
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
    if [[ ! -r "$JAVA" ]]; then
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
	log "$1 $2"
	moduleLogDir="${BUILD_PATH}/tmp/$2";
	if [ ! -d ${moduleLogDir} ]; then
		mkdir ${moduleLogDir}
	fi
	installLog="${moduleLogDir}/log.log";
	mvn clean $1 -Dmaven.test.skip=true > "${installLog}" 2>&1
	mvnSuccess=$(grep "BUILD SUCCESS" ${installLog})
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
PROJECT_PATH=$(cd $(dirname $0); pwd);
cd $PROJECT_PATH;
log "working path is $PROJECT_PATH"; 
#打包工作目录
BUILD_PATH="${PROJECT_PATH}/build"; 
if [ ! -d "${BUILD_PATH}/tmp" ]; then 
	mkdir "${BUILD_PATH}/tmp"
fi

if [ ! -d "${MODULES_PATH}" ]; then
	mkdir "${MODULES_PATH}"
fi
MODULES_PATH=$(cd "$MODULES_PATH"; pwd)
echoYellow "Modules Path $MODULES_PATH"''
log "==================BEGIN PACKAGE MODULES=============================="
#创建NULS_2.0公共模块目录
if [ ! -d "$MODULES_PATH/Nuls" ]; then
	mkdir $MODULES_PATH/Nuls
fi
MODULES_PATH=$MODULES_PATH/Nuls
#模块公共依赖jar存放目录
COMMON_LIBS_PATH=$MODULES_PATH/libs
if [ ! -d ${COMMON_LIBS_PATH} ]; then
	mkdir ${COMMON_LIBS_PATH}
fi

#0.更新代码
if [ -n "${DOPULL}" ];then
	log "git pull origin $GIT_BRANCH"
	git pull origin "$GIT_BRANCH"
fi

#1.install nuls-tools
cd $(pwd)/tools/nuls-tools
if [ ! -n `ls |grep pom.xml` ]; then
	echoRed "not found pom.xml"
	exit 0
fi
doMvn "install" "nuls-tools"

cd ../../


#检查module.ncf指定配置项是否存在
checkModuleItem(){
	if [ ! -f "$(pwd)/module.ncf" ]; then
		return 0
	fi
	if [ -z "$1" ]; then
		echoRed "getModuleItem 必须传入配置项名称"
		exit 1
	fi
	for line in `cat "$(pwd)/module.ncf"`
	do
		pname=$(echo $line | awk -F '=' '{print $1}')
		if [ ${pname} == $1 ]; then
			return 1;
		fi
	done
	#if [ "$2" == "1" ]; then
		echoRed "$2 module.ncf 必须配置 $1"
		exit 0
	#fi
}

getModuleItem(){
	for line in `cat "$(pwd)/module.ncf"`
	do
		pname=$(echo $line | awk -F '=' '{print $1}')
		pvalue=$(awk -v a="$line" '
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
					')
		if [ ${pname} == $1 ]; then
			echo ${pvalue};
			return 1;
		fi
	done
	return 0
}

#拷贝打好的jar包到Moules/Nuls/<Module Name>/<Version> 下
copyJarToModules(){
	moduleName=$(getModuleItem "APP_NAME");
	version=$(getModuleItem "VERSION");
	if [ ! -d "${MODULES_PATH}/${moduleName}" ];then
		mkdir ${MODULES_PATH}/${moduleName}
	fi
	if [ -d "${MODULES_PATH}/${moduleName}/${version}" ]; then 
		rm -r "${MODULES_PATH}/${moduleName}/${version}"
	fi	
	mkdir "${MODULES_PATH}/${moduleName}/${version}"
	jarName=`ls target |grep .jar`
	echo "拷贝 $(pwd)/target/${moduleName}-${version}.jar to ${MODULES_PATH}/${moduleName}/${version}/${moduleName}-${version}.jar"
	cp ./target/${jarName} "${MODULES_PATH}/${moduleName}/${version}/${moduleName}-${version}.jar"
	if [ -d ./target/libs ]; then
		for jar in `ls ./target/libs`; do
			#statements
			cp "./target/libs/${jar}" "${COMMON_LIBS_PATH}"
		done
	fi
}


copyModuleNcfToModules(){
	moduleName=$(getModuleItem "APP_NAME");
	version=$(getModuleItem "VERSION");
	mainClass=$(getModuleItem "MAIN_CLASS");
	mainClassName=$(awk -v s="${mainClass}" 'BEGIN{ len = split(s,ary,"."); print ary[len]}')
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
	for line in `cat ./module.ncf`
	do
		TEMP=$(echo $line|grep -Eo '\[.+\]')
		if [ -n "$TEMP" ]; then
		  #echo "set cfg domain ${TEMP}"
		  cfgDomain=$TEMP
		fi
		if [ "${cfgDomain}" == "[JAVA]" -a ! -n "$TEMP" ]; 
		then
			pname=$(echo $line | awk -F '=' '{print $1}')
			#pvalue=$(echo $line | awk -F '=' '{print $2}')
			pvalue=$(awk -v a="$line" '
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
					')
			sedCommand+=" -e 's/%${pname}%/${pvalue}/g' "
		else
			if [ "${cfgDomain}" != "[JAVA]" ]; then
				echo $line >> $moduleNcf
			fi
		fi
	done
	# merge common module.ncf and private module.ncf to module.tmep.ncf
	sh "${PROJECT_PATH}/build/merge-ncf.sh" "${PROJECT_PATH}/module.ncf" $moduleNcf
	rm $moduleNcf 
	sedCommand+=" -e 's/%MAIN_CLASS_NAME%/${mainClassName}/g' "

	if [ -z $(echo "${sedCommand}" | grep -o "%JOPT_XMS%") ]; then
		sedCommand="${sedCommand}  -e 's/%JOPT_XMS%/256/g' "
	fi
	if [ -z $(echo "${sedCommand}" | grep -o "%JAVA_OPTS%") ]; then
		sedCommand="${sedCommand}  -e 's/%JAVA_OPTS%//g' "
	fi
	if [ -z $(echo "${sedCommand}" | grep -o "%JOPT_XMX%") ]; then
		sedCommand="${sedCommand}  -e 's/%JOPT_XMX%/256/g' "
	fi
	if [ -z $(echo "${sedCommand}" | grep -o "%JOPT_METASPACESIZE%") ]; then
		sedCommand="${sedCommand}  -e 's/%JOPT_METASPACESIZE%/128/g' "
	fi
	if [ -z $(echo "${sedCommand}" | grep -o "%JOPT_MAXMETASPACESIZE%") ]; then
		sedCommand="${sedCommand}  -e 's/%JOPT_MAXMETASPACESIZE%/256/g' "
	fi
	# echo $sedCommand
	eval "${sedCommand}  ${BUILD_PATH}/start-temp.sh > ${moduleBuildPath}/start.sh"
	cp "${moduleBuildPath}/start.sh" "${MODULES_PATH}/${moduleName}/${version}/start.sh"
	chmod +x "${MODULES_PATH}/${moduleName}/${version}/start.sh"
	echo "拷贝 ${moduleBuildPath}/start.sh 到 ${MODULES_PATH}/${moduleName}/${version}/start.sh"

	eval "${sedCommand}  ${BUILD_PATH}/start-temp.bat > ${moduleBuildPath}/start.bat"
	cp "${moduleBuildPath}/start.bat" "${MODULES_PATH}/${moduleName}/${version}/start.bat"
    #cp "${moduleBuildPath}/start.bat" "/Volumes/share/start.bat"
	echo "拷贝 ${moduleBuildPath}/start.bat 到 ${MODULES_PATH}/${moduleName}/${version}/start.bat"

	eval "${sedCommand}  ${BUILD_PATH}/stop-temp.sh > ${moduleBuildPath}/stop.sh"
	cp "${moduleBuildPath}/stop.sh" "${MODULES_PATH}/${moduleName}/${version}/stop.sh"
	chmod +x "${MODULES_PATH}/${moduleName}/${version}/stop.sh"
	echo "拷贝 ${moduleBuildPath}/stop.sh 到 ${MODULES_PATH}/${moduleName}/${version}/stop.sh"

	eval "${sedCommand}  ${BUILD_PATH}/stop-temp.bat > ${moduleBuildPath}/stop.bat"
	cp "${moduleBuildPath}/stop.bat" "${MODULES_PATH}/${moduleName}/${version}/stop.bat"
	#cp "${moduleBuildPath}/stop.bat" "/Volumes/share/stop.bat"
	echo "拷贝 ${moduleBuildPath}/stop.bat 到 ${MODULES_PATH}/${moduleName}/${version}/stop.bat"


	cp "${moduleBuildPath}/module.temp.ncf" "${MODULES_PATH}/${moduleName}/${version}/Module.ncf"
	echo "拷贝 ${moduleBuildPath}/module.temp.ncf 到 ${MODULES_PATH}/${moduleName}/${version}/odule.ncf"
}

#2.遍历文件夹，检查第一个pom 发现pom文件后通过mvn进行打包，完成后把文件jar文件和module.ncf文件复制到Modules文件夹下
packageModule() {
	if [ ! -d $(pwd)/$1 ]; then
		return 0
	fi
	if [ $(pwd) == "${PROJECT_PATH}/Modules" ]; then
		return 0;
	fi
	cd $(pwd)/$1
	if [ -f "./module.ncf" ]; then
		echo "find module.ncf in $(pwd)"
		if [ ! -f "./pom.xml" ]; then
			echoRed "模块配置文件必须与pom.xml在同一个目录 : $(pwd)"
			exit 0;
		fi
		doMvn "package" $1
		checkModuleItem "APP_NAME" "$1"
		checkModuleItem "VERSION" "$1"
		checkModuleItem "MAIN_CLASS" "$1"
		copyJarToModules $1
		copyModuleNcfToModules $1
		log "$1 SUCCESS"
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
log "============ PACKAGE DONE ==============="

if [ -n "${DOMOCK}" ]; then
	log "BUILD start-mykernel script"
	cp "${BUILD_PATH}/start-mykernel.sh" "${MODULES_PATH}/start.sh"
	chmod u+x "${MODULES_PATH}/start.sh"
fi



