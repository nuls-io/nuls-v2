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
./package -a smart-contract
./package -a chain-manager
./package -a cross-chain
./package -a protocol-update
./package -a nuls-api
./package -O ${OS} -o NULS_Wallet
cp config/nuls.ncf NULS_Wallet/nuls.ncf
cp config/genesis-block.json NULS_Wallet/genesis-block.json
tar -czf NULS_Wallet_${OS}_v2.1.0.tar NULS_Wallet
rm -rf NULS_Wallet
