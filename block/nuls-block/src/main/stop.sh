#!/bin/bash
MODULE="$1"
echo "beginning to stop the ${MODULE} module..."
pid=`ps -ef|grep -i io.nuls.block.BlockBootstrap |grep java|awk '{print $2}'`
status=$?
if [ $status -ne 0 ]; then
    echo "stop the ${MODULE} module failed."
    exit $status
fi

if [ -z $pid ]; then
    echo "${MODULE} module is not running."
    exit 0
fi

echo "the ${MODULE} module's pid is $pid"
kill -9 $pid
status=$?
if [ $status -ne 0 ]; then
    echo "stop the ${MODULE} module failed."
    exit $status
fi
echo "stop the ${MODULE} module completed."

exit 0