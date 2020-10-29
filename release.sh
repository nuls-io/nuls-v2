#!/bin/bash
cd `dirname $0`
OS=$1
if [ -z "$1" ]; then
    cat <<- EOF
        Desc: need set target os;
        Usage: ./release.sh window
               ./release.sh linux
               ./release.sh macos
EOF
    exit 0
fi
VERSION=`cat version`
./package -a smart-contract
./package -a chain-manager
./package -a cross-chain
./package -a protocol-update
./package -a nuls-api
./package -O ${OS} -o NULS_Wallet
tar -czf NULS_Wallet_${OS}_v${VERSION}.tar.gz NULS_Wallet
#rm -rf NULS_Wallet
