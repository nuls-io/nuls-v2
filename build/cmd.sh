#!/bin/sh
export JAVA_HOME="$(cd $(dirname "../Libraries/JAVA/11.0.2"); pwd)/11.0.2"
export PATH=${PATH}:${JAVA_HOME}/bin
echo "JAVA_HOME:${JAVA_HOME}"
echo `java -version`
cd ../Modules/Nuls/cmdclient/1.0.0
sh ./cmd.sh