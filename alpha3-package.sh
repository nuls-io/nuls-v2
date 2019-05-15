#!/bin/bash
cd `dirname $0`
if [ -n "$1"];
then
./package -Nb alpha3 -J $1
else
./package -Nb alpha3
fi
mv NULS-Wallet-linux64-alpha2 NULS-Wallet-linux64-alpha3
cp -f ./nuls-main.ncf ./NULS-Wallet-linux64-alpha3/nuls.ncf
tar -zcPf NULS-Wallet-linux64-alpha3-main.tar.gz ./NULS-Wallet-linux64-alpha3
cp -f ./nuls-chain-10.ncf ./NULS-Wallet-linux64-alpha3/nuls.ncf
cp -f ./genesis-block-10.json ./NULS-Wallet-linux64-alpha3/genesis-block.json
cp -f ./README-ALPAH3.md ./NULS-Wallet-linux64-alpha3/README.md
tar -zcPf NULS-Wallet-linux64-alpha3-NBTC.tar.gz ./NULS-Wallet-linux64-alpha3
cp -f ./nuls-chain-11.ncf ./NULS-Wallet-linux64-alpha3/nuls.ncf
cp -f ./genesis-block-11.json ./NULS-Wallet-linux64-alpha3/genesis-block.json
cp -f ./README-ALPAH3.md ./NULS-Wallet-linux64-alpha3/README.md
tar -zcPf NULS-Wallet-linux64-alpha3-NETH.tar.gz ./NULS-Wallet-linux64-alpha3
echo "out of"
echo "NULS-Wallet-linux64-alpha3-main.tar.gz"
echo "NULS-Wallet-linux64-alpha3-NBTC.tar.gz"
echo "NULS-Wallet-linux64-alpha3-NETH.tar.gz"
