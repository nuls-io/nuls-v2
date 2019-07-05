#!/bin/bash
cd `dirname $0`
./package -a smart-contract
./package -a chain-manager
./package -a cross-chain
if [ -n "$1" ];
then
./package -Nb beta-1 -J $1
else
./package -Nb beta-1
fi
#mv NULS-Wallet-linux64-beta1 NULS-Wallet-linux64-
cp -f ./nuls-main.ncf ./NULS-Wallet-linux64-beta1/nuls.ncf
tar -zcPf NULS-Wallet-linux64-beta1-main.tar.gz ./NULS-Wallet-linux64-beta1
rm -rf NULS-Wallet-linux64-beta1
./package -r chain-manager
if [ -n "$1" ];
then
./package -Nb beta-1 -J $1
else
./package -Nb beta-1
fi
cp -f ./nuls-chain-10.ncf ./NULS-Wallet-linux64-beta1/nuls.ncf
cp -f ./genesis-block-10.json ./NULS-Wallet-linux64-beta1/genesis-block.json
tar -zcPf NULS-Wallet-linux64-beta1-NBTC.tar.gz ./NULS-Wallet-linux64-beta1
#cp -f ./nuls-chain-11.ncf ./NULS-Wallet-linux64-alpha3/nuls.ncf
#cp -f ./genesis-block-11.json ./NULS-Wallet-linux64-alpha3/genesis-block.json
#cp -f ./README-ALPAH3.md ./NULS-Wallet-linux64-alpha3/README.md
#tar -zcPf NULS-Wallet-linux64-alpha3-NETH.tar.gz ./NULS-Wallet-linux64-alpha3
echo "out of"
echo "NULS-Wallet-linux64-beta1-main.tar.gz"
echo "NULS-Wallet-linux64-beta1-NBTC.tar.gz"