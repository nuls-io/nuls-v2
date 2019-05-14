#!/bin/bash
cd `dirname $0`
./package -Nb alpha3
mv NULS-Wallet-linux64-alpha2 NULS-Wallet-linux64-alpha3
cp -f ./nuls-main.ncf ./NULS-Wallet-linux64-alpha3/nuls.ncf
tar -cvf NULS-Wallet-linux64-alpha3-main.tar.gz ./NULS-Wallet-linux64-alpha3
cp -f ./nuls-chain-10.ncf ./NULS-Wallet-linux64-alpha3/nuls.ncf
cp -f ./genesis-block-10.json ./NULS-Wallet-linux64-alpha3/genesis-block.json
cp -f ./README-ALPAH3.md ./NULS-Wallet-linux64-alpha3/README.md
tar -cvf NULS-Wallet-linux64-alpha3-chain-10.tar.gz ./NULS-Wallet-linux64-alpha3
cp -f ./nuls-chain-11.ncf ./NULS-Wallet-linux64-alpha3/nuls.ncf
cp -f ./genesis-block-10.json ./NULS-Wallet-linux64-alpha3/genesis-block.json
cp -f ./README-ALPAH3.md ./NULS-Wallet-linux64-alpha3/README.md
tar -cvf NULS-Wallet-linux64-alpha3-chain-11.tar.gz ./NULS-Wallet-linux64-alpha3
echo "out of"
echo "NULS-Wallet-linux64-alpha3-main.tar.gz"
echo "NULS-Wallet-linux64-alpha3-chain-10.tar.gz"
echo "NULS-Wallet-linux64-alpha3-chain-11.tar.gz"
