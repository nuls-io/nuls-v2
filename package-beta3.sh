#!/bin/bash
cd `dirname $0`
version=beta3
OUT_DIR=NULS_Wallet_$version
./package -a smart-contract
./package -a chain-manager
./package -a cross-chain
TARGET_OS="linux"
if [ -n "$1" ]; then
    TARGET_OS=$1
fi
if [ -n "$2" ];
then
./package -Nb $version -J $2 -O $TARGET_OS  -o ./$OUT_DIR
else
./package  -O $TARGET_OS -Nb $version -o ./$OUT_DIR
fi
#mv NULS-Wallet-linux64-beta1 NULS-Wallet-linux64-
cp -f ./nuls-main-$version.ncf ./${OUT_DIR}/nuls.ncf
cp -f ./genesis-block-main-$version.json ./${OUT_DIR}/genesis-block.json
if [ "$TARGET_OS" == "window" ];
then
    zip -r ${OUT_DIR}-main-${TARGET_OS}.zip ./${OUT_DIR}
else
    tar -zcPf ${OUT_DIR}-main-${TARGET_OS}.tar.gz ./${OUT_DIR}
fi
rm -rf ${OUT_DIR}
./package -r chain-manager
if [ -n "$2" ];
then
./package -O $TARGET_OS -Nb $version -J $2 -o ./$OUT_DIR
else
./package -O $TARGET_OS -Nb $version -o ./$OUT_DIR
fi
cp -f ./nuls-chain-10-$version.ncf ./${OUT_DIR}/nuls.ncf
cp -f ./genesis-block-10-$version.json ./${OUT_DIR}/genesis-block.json
if [ "$TARGET_OS" == "window" ];
then
    zip -r ${OUT_DIR}-NBTC-${TARGET_OS}.zip ./${OUT_DIR}
else
    tar -zcPf ${OUT_DIR}-NBTC-${TARGET_OS}.tar.gz ./${OUT_DIR}
fi
#cp -f ./nuls-chain-11.ncf ./NULS-Wallet-linux64-alpha3/nuls.ncf
#cp -f ./genesis-block-11.json ./NULS-Wallet-linux64-alpha3/genesis-block.json
#cp -f ./README-ALPAH3.md ./NULS-Wallet-linux64-alpha3/README.md
#tar -zcPf NULS-Wallet-linux64-alpha3-NETH.tar.gz ./NULS-Wallet-linux64-alpha3
echo "======================== out of ======================== "
if [ "$TARGET_OS" == "window" ];
then
    echo "${OUT_DIR}-main-${TARGET_OS}.zip"
    echo "${OUT_DIR}-NBTC-${TARGET_OS}.zip"
else
    echo "${OUT_DIR}-main-${TARGET_OS}.tar.gz"
    echo "${OUT_DIR}-NBTC-${TARGET_OS}.tar.gz"
fi
echo "======================================================== "
