#!/bin/bash

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
		if [ "${pname}" == $2 ]; then
			echo ${pvalue};
			return 1;
		fi
	done < $1
	return 0
}

#获取绝对路径
get_fullpath()
{
    if [ -f "$1" ];
    then
        tempDir=`dirname $1`;
        echo `cd $tempDir; pwd`;
    else
        echo `cd $1; pwd`;
    fi
}
