#!/bin/bash
cd `dirname $0`
./package -a smart-contract
./package -a chain-manager
./package -a cross-chain
./package -a protocol-update
./package -a nuls-api
./package -O linux -o NULS_Wallet
cp build/default-config.ncf NULS_Wallet/nuls.ncf
cp build/genesis-block.json NULS_Wallet/genesis-block.json
tar -czf NULS_Wallet_linux64_v2.1.0.tar NULS_Wallet
rm -rf NULS_Wallet
./package -O win64 -o NULS_Wallet
cp build/default-config.ncf NULS_Wallet/nuls.ncf
cp build/genesis-block.json NULS_Wallet/genesis-block.json
zip -r NULS_Wallet_win64_v2.1.0.zip NULS_Wallet
rm -rf NULS_Wallet