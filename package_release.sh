#!/bin/bash
cd `dirname $0`
./package -a smart-contract
./package -a chain-manager
./package -a cross-chain
./package -a protocol-update
./package -a nuls-api
./package -a mykernel
./package
cp build/default-config.ncf NULS_Wallet/nuls.ncf
cp build/genesis-block.json NULS_Wallet/genesis-block.json