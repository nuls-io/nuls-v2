#!/bin/bash

checkModuleItem(){
	for line in `cat "$1"`
	do
		# echo "line=>$line  $2"
		pname=$(echo $line | awk -F '=' '{print $1}')
		if [ ${pname} == $2 ]; then
			return 1;
		fi
	done
	return 0
}

getModuleItem(){
	for line in `cat "$1"`
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
		if [ ${pname} == $2 ]; then
			echo ${pvalue};
			return 1;
		fi
	done
	return 0
}
moduleFile=$2
moduleTmepFile=$1
ncfPath=$(dirname $2)
moduleNcf="${ncfPath}/module.temp.ncf"
echo "# module.ncf" > $moduleNcf
declare -a titleList
for line in `cat $moduleTmepFile`
do
	TEMP=$(echo $line|grep -Eo '\[.+\]')
	if [ -n "$TEMP" ]; then
		titleList[${#titleList[@]}]="$TEMP"
	fi
done
#echo ${titleList[@]}
for title in ${titleList[@]}
do
	#echo $title
	declare -a itemName=()
	titleTemp=""
	for line in `cat $moduleTmepFile`
	do
	TEMP=$(echo $line|grep -Eo '\[.+\]')
	if [ -n "$TEMP" ]; then
		titleTemp=$TEMP
	fi
	if [ "${titleTemp}" == "${title}" -a ! -n "$TEMP"  ]; then
		pname=$(echo $line | awk -F '=' '{print $1}')
		itemName[${#itemName[@]}]=$pname
	fi
	done
	titleTemp=""
	for line in `cat $moduleFile`
	do
		TEMP=$(echo $line|grep -Eo '\[.+\]')
		if [ -n "$TEMP" ]; then
			titleTemp=$TEMP
		fi
		if [ "${titleTemp}" == "${title}" -a ! -n "$TEMP"  ]; then
			pname=$(echo $line | awk -F '=' '{print $1}')
			if ! echo "${itemName[@]}" | grep -w "${pname}" &>/dev/null ; then
	    		itemName[${#itemName[@]}]=$pname
			fi
		fi
	done
	# echo ${itemName[@]}
	echo $title >> $moduleNcf
	for name in ${itemName[@]}
	do
		#echo $name
		checkModuleItem "$moduleFile" ${name}
		if [ $? == 1 ]; 
		then
			value=$(getModuleItem "$moduleFile" $name)
			echo "${name}=${value}" >> $moduleNcf
		else 
			value=$(getModuleItem "$moduleTmepFile" $name)
			echo "${name}=${value}" >> $moduleNcf			
		fi
	done		
	echo "" >> $moduleNcf
done
