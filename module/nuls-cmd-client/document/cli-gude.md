
# Nuls_2.0-alpha-3 CLI Usage Guide
## introduce

This document is forNULS2.0 alpha3Version Testing NetworkLinuxThe user guide for the full node wallet version requires understanding before reading this documentLinuxThe basic operation and usage of the system are introduced in this articleLinuxHow to utilize in the systemNULSWallet creation account、Import account、Transfer、Establish nodes、Entrust and other operations. We suggest that users utilizeLinuxEstablish a stable system serverNULSNode.

## Version update records

|  version  |  Update date  |        content        |
| :----: | :--------: | :----------------: |
| V1.0.0 | 2019-03-18 | alphaVersion function |
| V1.0.1 | 2019-05-13|alpha3Version function|

## prepare

### Server hardware configuration


**establishNULSThe server of the node should not be lower than the following configuration：**

|     CPU     | Memory |   Hard disk   |  broadband   |
| :---------: | :---: | :------: | :-----: |
| Tetranuclear 3.0GHz | 16G  | 128GHard disk | 20Mupstream |


**Recommended configuration：**

|     CPU     | Memory |   Hard disk   |   broadband   |
| :---------: | :---: | :------: | :------: |
| Eight core 3.0GHz | 32G  | 256GHard disk | 100Mupstream |



### System and kernel versions

**Linuxsystem**

- CentOS 6,7
- Ubuntu 14 +

LinuxRecommended kernel version for use 2.6.32And above

## start

### download

- The latest version of the full node walletNULSOfficial website download address：http://nuls.io/wallet；GitHubaddress：https://github.com/nuls-io/nuls-wallet-release

- enter[NULSOfficial website wallet download](http://nuls.io/wallet)After the interface, selectLinux downloadWe provideMEGAThere are two download methods, Baidu Cloud Disk and Baidu Cloud Disk, which users can choose by themselves.

  LinuxDownload in the systemv2.0.0-alpha-1The version of the wallet can use the following commands：

  ```shell
  $ wget https://media.githubusercontent.com/media/nuls-io/nuls-wallet-release/master/NULS-Wallet-linux64-2.0.0-alpha-3.tar.gz
  ```

  notes：If there are other versions in the future, the download address may be different.

### install

- stayLinuxDecompression of downloaded files

  ```shell
  $ tar -zxf NULS-Wallet-linux64-2.0.0-alpha-3.tar.gz
  ```

### working

- Enter the decompressed directory and run the startup script to start the full node wallet

  ```shell
  $ cd NULS-Wallet-linux64-2.0.0-alpha-3
  $ ./start.sh
  ```

## Using a wallet

### quick get start

- After confirming that the wallet has been started, start the command line program of the wallet to operate it.

  Enter the root directory of the wallet and execute the following command：

  ```shell
  $ ./cmd
  ```

  Will appearNULSNamed input prompt`nuls>>>  ` , and then you can directly inputNULSThe wallet operation command is used to perform the operation.

  For example, an example of creating an account is as follows：

  ```shell
  nuls>>> create
  Please enter the password (password is between 8 and 20 inclusive of numbers and letters), If you do not want to set a password, return directly.
  Enter your password:*********
  Please confirm new password:*********
  [ "Nse9EtaRwgVgN42pxURgZjUR33LUx1j1" ]
  nuls>>>
  ```

  implement`create`The command indicates creating a single account, then entering the password, and confirming the password again. After successful creation, the account address will be returned.



## convention

- Set password rules：The password length is within8to20Bits must contain both letters and numbers.
- Command parameter description： &lt;parameter&gt; Indicates mandatory parameters；[parameter] Indicates optional parameters."|" In the parameter, represents or, indicating that only one of the preceding and following parameters can be selected.

## Wallet command

### Help Command

Output and print all commands,

- **command： help [-a]|[group]|[command]**

| parameter | explain                 |
| :--- | :------------------- |
| -a   | Format printing command, optional |
| command|View instructions for using specified commands|
| group|View all command usage instructions for the specified command group|

Return Information help

```json
getaccount <address> --get account information
```

Return Information help -a

```json
getaccount <address> --get account information
	OPTIONS:
	<address> the account address - Required
```

Example

```shell
nuls>>> help
nuls>>> help -a
nuls>>> help account
nuls>>> help create
```



### Create an account

Create an account and return a set of account addresses

- **command： create [number]**

| parameter     | explain                 |
| :------- | :------------------- |
| [number] | Number of accounts created, optional |

When creating an account, a password will be prompted. To ensure asset security, a password must be set for the account；

Return account collection

```json
[ "5MR_2CkDZtZRHGLD43JreUc8LsFBertsc9r", "5MR_2CXCCU89fj9RyQj9MgZVE7Pq3Mmk77p" ]
```

Example 

establish1Accounts


```shell
nuls>>> create 
Please enter the new password(8-20 characters, the combination of letters and numbers).
Enter your new password:**********
Please confirm new password:**********
[ "5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW" ]
```
Create multiple accounts at once

```
nuls>>> create 3
Please enter the new password(8-20 characters, the combination of letters and numbers).
Enter your new password:**********
Please confirm new password:**********
[ "5MR_2CWdfU2VDERgQbWS1quGYAGD1iDDM4N", "5MR_2CcYq7fqrvKagReBmzG3qEz8qGkifCr", "5MR_2Cd6E2vAGZQxkqeXbeqThRxDGTFiLei" ]
```




### Backup account

Backup account will generate an account address with an extension of.keystoreThe file is a backup file for the account

- **command：backup &lt;address&gt; [path]**

| parameter            | explain                                                 |
| --------------- | ---------------------------------------------------- |
| &lt;address&gt; | Account address, required                                       |
| [path]          | The target folder for generating backup files, default to the current folder, optional |

Return Information

```shell
The path to the backup file is /nuls/bin/NsdyM1Ls5qw8wutvAQsr93jxgq8qYAZy.keystore
```

Example Back up an account with a password

```shell
nuls>>> backup 5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW /Users/zlj
Enter account password
***************
The path to the backup file is /Users/zlj/5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW.keystore
```

### Remove account

To remove a local account based on the account address, a password needs to be entered

- **command：remove &lt;address&gt;**

| parameter            | explain             |
| --------------- | ---------------- |
| &lt;address&gt; | Account address, required |

Return Information

```json
Success
```

Example

```shell
nuls>>> remove 5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW
Enter your password for account**********
Success
```


### Change account password

Reset the new password based on the account address and password.

- **command：resetpwd &lt;address&gt;**

| parameter            | explain             |
| --------------- | ---------------- |
| &lt;address&gt; | Account address, required |

Return Information

```json
Success
```

Example

```shell
nuls>>> resetpwd 5MR_2CWdfU2VDERgQbWS1quGYAGD1iDDM4N
Enter your old password:**********
Enter new password**********
Please confirm new password:**********
Success
```



### Set alias

Set an alias for the account. If a node is created using this account, the alias will be displayed as the source of the node

- **command：setalias &lt;address&gt; &lt;alias&gt;**

| parameter            | explain             |
| --------------- | ---------------- |
| &lt;address&gt; | Account address, required |
| &lt;alias&gt;   | Alias name, required   |

Return Information transactionhash

```json
txHash:0020f94f36aefd59f9cca9bff3c018fc287dc6c0bcd7fbeb047133cadb5747e7d98d"
```

Example

```shell
nuls>>> setalias 5MR_2CXrzwoCoP4vnUxHJ5gdUUXZJhCpjq9 zlj
Enter your account password**********
txHash:0020830971e02527f18f8f9e32f974d8c73ce6bd249de859cae170476b87d0ec9582
```



### Import accountkeystore

Import accountkeystoreFile, generate a local account. If the account already exists locally, it cannot be imported.

- **command：importkeystore &lt;path&gt;**

| parameter         | explain                           |
| ------------ | ------------------------------ |
| &lt;path&gt; | To be importedkeystoreFile address, mandatory |

take care：ImportkeystoreWhen generating an account for files, the original password is required

Return Information Imported account address

```json
"NsdyM1Ls5qw8wutvAQsr93jxgq8qYAZy"
```

Example

```shell
nuls>>> importkeystore /Users/zhoulijun/5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW.keystore
Please enter the password (password is between 8 and 20 inclusive of numbers and letters), If you do not want to set a password, return directly.
Enter your password:**********
5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW
```



### Import account private key

Import the account private key and generate a local account. If the account already exists locally, it will be overwritten,When importing, a password needs to be set for the account. This function can be used to retrieve the account through the private key after forgetting the account password.

- **command：import &lt;privatekey&gt;**

| parameter               | explain             |
| ------------------ | ---------------- |
| &lt;privatekey&gt; | The private key of the account, required |


```json
"NsdyM1Ls5qw8wutvAQsr93jxgq8qYAZy"
```

Example

```shell
nuls>>> import 1c2b9fd4417c1aad8ae9f24c982ff294eb50a6462b873b79a879e805a9990346
Please enter the password (password is between 8 and 20 inclusive of numbers and letters), If you do not want to set a password, return directly.
Enter your password:**********
Please confirm new password:**********
5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu
```

### Query account information

Query account information based on account address

- **command：getaccount &lt;address&gt;**

| parameter            | explain           |
| --------------- | :------------- |
| &lt;address&gt; | Account address, required |

Return Information

```json
{
  "encryptedPrikeyHex" : "724d68268849f3680d480c9257f33229c0fac88890d5355c0e4d9c457af5c6e8b8f9f7ca9fd52fbd8079fbce1782052d",  //Encrypted private key
  "alias" : "zlj",  //alias
  "baglance" : {  
    "freeze" : 0,   //Number of frozen assets
    "total" : 997999999800000,     //Total number of assets
    "available" : 997999999800000  //Number of available assets
  },
  "address" : "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",  //Account address
  "pubkeyHex" : "0211c45f28710cd26a2c45fb790895a0ff2e095a290f1825b31d80ebc30913c486" //Public key
}
```

Example

```shell
nuls>>> getaccount 5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu
{
  "encryptedPrikeyHex" : "724d68268849f3680d480c9257f33229c0fac88890d5355c0e4d9c457af5c6e8b8f9f7ca9fd52fbd8079fbce1782052d",  //Encrypted private key
  "alias" : "zlj",  //alias
  "baglance" : {  
    "freeze" : 0,   //Number of frozen assets
    "total" : 997999999800000,     //Total number of assets
    "available" : 997999999800000  //Number of available assets
  },
  "address" : "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",  //Account address
  "pubkeyHex" : "0211c45f28710cd26a2c45fb790895a0ff2e095a290f1825b31d80ebc30913c486" //Encrypted public key
}
```



### Query account list

Query the account list based on pagination parameters, and output all accounts in reverse order of creation time.

- **command：getaccounts &lt;pageNumber&gt; &lt;pageSize&gt;**

| parameter               | explain                             |
| ------------------ | -------------------------------- |
| &lt;pageNumber&gt; | Page number, which page of data needs to be obtained, mandatory |
| &lt;pageSize&gt;   | The number of data displayed on each page is mandatory       |

Return information to output the account set

```json
[ {
  "address" : "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",  //address
  "alias" : null,  //alias
  "pubkeyHex" : "0211c45f28710cd26a2c45fb790895a0ff2e095a290f1825b31d80ebc30913c486",  //Public key
  "encryptedPrikeyHex" : "724d68268849f3680d480c9257f33229c0fac88890d5355c0e4d9c457af5c6e8b8f9f7ca9fd52fbd8079fbce1782052d"  //Private key
}, {
  "address" : "5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW",
  "alias" : null,
  "pubkeyHex" : "0205a70731e7653eca328ba7d71f0a789f8cfb1ced32f5a00d4fc3fb2ad8b9f7c1",
  "encryptedPrikeyHex" : "e38d2dd08154a0eedf8298f5fe50b86723e521522f38aba5c68072bad365c3e8c57d7ac3ae83f8d646a17f845a38bc57"
}, {
  "address" : "5MR_2CXrzwoCoP4vnUxHJ5gdUUXZJhCpjq9",
  "alias" : "zlj",
  "pubkeyHex" : "03021a46a7e5ea59ae8884340568e9e79511fbd352b4ba28db39f15856918cdbeb",
  "encryptedPrikeyHex" : "bfbfdad874f74215e241ad15152d8648925c497b6a826965f5c06c46fd9b008313e6918ebcfcb56f2cdf8d1b9f088f77"
} ]
```



Example Get account list

```shell
nuls>>> getaccounts
[ {
  "address" : "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",  //address
  "alias" : null,  //alias
  "pubkeyHex" : "0211c45f28710cd26a2c45fb790895a0ff2e095a290f1825b31d80ebc30913c486",  //Encrypted public key
  "encryptedPrikeyHex" : "724d68268849f3680d480c9257f33229c0fac88890d5355c0e4d9c457af5c6e8b8f9f7ca9fd52fbd8079fbce1782052d"  //Encrypted private key
}, {
  "address" : "5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW",
  "alias" : null,
  "pubkeyHex" : "0205a70731e7653eca328ba7d71f0a789f8cfb1ced32f5a00d4fc3fb2ad8b9f7c1",
  "encryptedPrikeyHex" : "e38d2dd08154a0eedf8298f5fe50b86723e521522f38aba5c68072bad365c3e8c57d7ac3ae83f8d646a17f845a38bc57"
}, {
  "address" : "5MR_2CXrzwoCoP4vnUxHJ5gdUUXZJhCpjq9",
  "alias" : "zlj",
  "pubkeyHex" : "03021a46a7e5ea59ae8884340568e9e79511fbd352b4ba28db39f15856918cdbeb",
  "encryptedPrikeyHex" : "bfbfdad874f74215e241ad15152d8648925c497b6a826965f5c06c46fd9b008313e6918ebcfcb56f2cdf8d1b9f088f77"
} ]
```



### Query account private key

Query account private key based on account address and password

- **command：getprikey &lt;address&gt;**

| parameter            | explain             |
| --------------- | ---------------- |
| &lt;address&gt; | Account address, required |

Return Information The private key of the imported account（unencrypted）

```json
00a166d10c2cc4cd8f76449ff699ab3eee44fe4f82b4bb866f7bba02751a6fd655
```

Example

```shell
nuls>>> getprikey 5MR_2CXrzwoCoP4vnUxHJ5gdUUXZJhCpjq9
Enter your account password**********
7b4d3ec971fc01ea813b52f6c35091d43beac4a68550bae2db63975149244678
```



### Query account balance

Query account balance based on account address

- **command：getbalance &lt;address&gt;**

| parameter            | explain             |
| --------------- | ---------------- |
| &lt;address&gt; | Account address, required |

Return Information Imported account address

```json
{
  "total" : "9999998.99",//balance
  "freeze" : "0",//Locked balance
  "available" : "9999998.99"//Available balance
}
```

Example

```shell
nuls>>> getbalance Nse2TpVsJd4gLoj79MAY8NHwEsYuXwtT
{
  "total" : "9999998.99",
  "freeze" : "0",
  "available" : "9999998.99"
}
```



### Transfer

According to the account address or aliasNULSTransfer to another account address or alias

- **command：transfer &lt;formAddress&gt;|<formAlias> &lt;toAddress&gt;|<toAlias> &lt;amount&gt; [remark] **

| parameter              | explain                                            |
| ----------------- | ----------------------------------------------- |
| &lt;formAddress&gt; | Transfer address(Related toformAliasChoose any one option）                                |
|<formAlias>|Transfer address alias(Related toformAddressChoose any one option）|
| &lt;toAddress&gt; | Receiving address(Related totoAliasChoose any one option）                               |
|<toAlias>|Receive address alias(Related totoAddressChoose any one option）|
| &lt;amount&gt;    | Transfer quantity, required |
| [remark]          | Note information, optional                                  |

Return Information Transfer transactionhash

```json
"00200bef73ad728c48146c8a5eb0d76fe7325b85803c61d8357c16dba09ea33b3596"
```

Example

```shell
nuls>>> transfer Nse2TpVsJd4gLoj79MAY8NHwEsYuXwtT NsdtmV5XkgSdpBXi65ueTsrv2W5beV2T 100 Transfer
Please enter the password.
Enter your password:**********
"00200bef73ad728c48146c8a5eb0d76fe7325b85803c61d8357c16dba09ea33b3596"
```



### Query transaction details

According to the transactionhashQuery transaction details

- **command：gettx &lt;hash&gt;**

| parameter         | explain           |
| ------------ | -------------- |
| &lt;hash&gt; | transactionhash, mandatory |

Return Information Transaction Details

```json
{
  "type" : 2,  //Transaction type（The enumeration instructions are shown in the table below【type Description of enumeration types】）
  "coinData" : "ARc5MAGYBT3XNVp+BIuhGvGcejuTev8DODkwAQCgZ/cFAAAAAAAAAAAAAAAACO/WnDT4pvmsAAEXOTABL/80LO1f8vxvfNXc5l9eeIDTGKM5MAEAAOH1BQAAAAAAAAAAAAAAAAA=",
  "txData" : null,
  "time" : 1552979783918,
  "transactionSignature" : "IQIRxF8ocQzSaixF+3kIlaD/LglaKQ8YJbMdgOvDCRPEhgBGMEQCICdnNr3HqEg/UZZ6RLBHyGuPChoLdMtcOHXT3Xlb5SC3AiBGAWSPGH3yjtEkaVbLsI5n9UcqDvOfG3Ui1jf672IDCg==",
  "remark" : "6L2s6LSm",
  "hash" : {
    "digestAlgType" : 0,
    "digestBytes" : "CivAIHpVyqNr/h87/FWk7vXsXqBekHJ+3kQc5mZp+H8=", 
    "digestHex" : "00200a2bc0207a55caa36bfe1f3bfc55a4eef5ec5ea05e90727ede441ce66669f87f" 
  },
  "blockHeight" : 341,   //block height
  "status" : "CONFIRMED",  //Confirm status
  "size" : 225,
  "inBlockIndex" : 0,
  "coinDataInstance" : {  
    "from" : [ {
      "address" : "OTABmAU91zVafgSLoRrxnHo7k3r/Azg=",
      "assetsChainId" : 12345,
      "assetId" : 1,
      "amount" : 100100000,
      "nonce" : "79acNPim+aw=",
      "locked" : 0
    } ],
    "to" : [ {
      "address" : "OTABL/80LO1f8vxvfNXc5l9eeIDTGKM=",
      "assetsChainId" : 12345,
      "assetId" : 1,
      "amount" : 100000000,
      "lockTime" : 0
    } ]
  },
  "fee" : 100000,  //Handling fees
  "multiSignTx" : false
}
```

Example Query transfer transactions

```shell
nuls>>> gettx 00200a2bc0207a55caa36bfe1f3bfc55a4eef5ec5ea05e90727ede441ce66669f87f
{
  "type" : 2,  //Transaction type（The enumeration instructions are shown in the table below【type Description of enumeration types】）
  "coinData" : "ARc5MAGYBT3XNVp+BIuhGvGcejuTev8DODkwAQCgZ/cFAAAAAAAAAAAAAAAACO/WnDT4pvmsAAEXOTABL/80LO1f8vxvfNXc5l9eeIDTGKM5MAEAAOH1BQAAAAAAAAAAAAAAAAA=",
  "txData" : null,
  "time" : 1552979783918,
  "transactionSignature" : "IQIRxF8ocQzSaixF+3kIlaD/LglaKQ8YJbMdgOvDCRPEhgBGMEQCICdnNr3HqEg/UZZ6RLBHyGuPChoLdMtcOHXT3Xlb5SC3AiBGAWSPGH3yjtEkaVbLsI5n9UcqDvOfG3Ui1jf672IDCg==",
  "remark" : "6L2s6LSm",
  "hash" : {
    "digestAlgType" : 0,
    "digestBytes" : "CivAIHpVyqNr/h87/FWk7vXsXqBekHJ+3kQc5mZp+H8=", 
    "digestHex" : "00200a2bc0207a55caa36bfe1f3bfc55a4eef5ec5ea05e90727ede441ce66669f87f" 
  },
  "blockHeight" : 341,   //block height
  "status" : "CONFIRMED",  //Confirm status
  "size" : 225,
  "inBlockIndex" : 0,
  "coinDataInstance" : {  
    "from" : [ {
      "address" : "OTABmAU91zVafgSLoRrxnHo7k3r/Azg=",
      "assetsChainId" : 12345,
      "assetId" : 1,
      "amount" : 100100000,
      "nonce" : "79acNPim+aw=",
      "locked" : 0
    } ],
    "to" : [ {
      "address" : "OTABL/80LO1f8vxvfNXc5l9eeIDTGKM=",
      "assetsChainId" : 12345,
      "assetId" : 1,
      "amount" : 100000000,
      "lockTime" : 0
    } ]
  },
  "fee" : 100000,  //Handling fees
  "multiSignTx" : false
}
```
#### type Description of enumeration types

```
/** coinbasetransaction*/
    int TX_TYPE_COINBASE = 1;
    /** Transfer transaction*/
    int TX_TYPE_TRANSFER = 2;
    /** Set alias*/
    int TX_TYPE_ALIAS = 3;
    /** Create consensus node transactions*/
    int TX_TYPE_REGISTER_AGENT = 4;
    /** Entrusted transaction(Join consensus)*/
    int TX_TYPE_JOIN_CONSENSUS = 5;
    /** Cancel entrusted transaction(Exit consensus)*/
    int TX_TYPE_CANCEL_DEPOSIT = 6;
    /** Yellow card punishment*/
    int TX_TYPE_YELLOW_PUNISH = 7;
    /** Red card punishment*/
    int TX_TYPE_RED_PUNISH = 8;
    /** Stop node(Delete consensus node)*/
    int TX_TYPE_STOP_AGENT = 9;
    /** Cross chain transfer transactions*/
    int TX_TYPE_CROSS_CHAIN_TRANSFER = 10;
    /** Registration Chain Transaction*/
    int TX_TYPE_REGISTER_CHAIN_AND_ASSET = 11;
    /** Destruction chain*/
    int TX_TYPE_DESTROY_CHAIN_AND_ASSET = 12;
    /** Add an asset to the chain*/
    int TX_TYPE_ADD_ASSET_TO_CHAIN = 13;
    /** Delete on chain assets*/
    int TX_TYPE_REMOVE_ASSET_FROM_CHAIN = 14;
    /** Create smart contract transactions*/
    int TX_TYPE_CREATE_CONTRACT = 100;
    /** Calling smart contract transactions*/
    int TX_TYPE_CALL_CONTRACT = 101;
    /** Delete smart contract transactions*/
    int TX_TYPE_DELETE_CONTRACT = 102;
```

### Create nodes

Create nodes based on account addresses,When creating a node, two addresses need to be provided. The first address is the node address, and the node address account password needs to be entered. The second address is the packaging address, and no password needs to be entered. At least20000NULSThe security deposit.

- **command：createagent &lt;agentAddress&gt; &lt;packingAddress&gt; &lt;commissionRate&gt; &lt;deposit&gt;**

| parameter                   | explain                                                         |
| ---------------------- | ------------------------------------------------------------ |
| &lt;agentAddress&gt;   | Account address for creating node, required                                     |
| &lt;packingAddress&gt; | Node packaging account address, mandatory（notes：This account cannot be set with a password, otherwise the node cannot package blocks） |
| &lt;commissionRate&gt; | Agency commission ratio, range：10~100, mandatory                             |
| &lt;deposit&gt;        | The margin for creating nodes cannot be lower than20000NULS, mandatory                    |

Return Information Return node'sagent hash

```json
"002006a5b7eb1d32ed6d7d54e24e219b112d4fdb8530db5506ee953b6f65a0fdb55e"
```

Example Create a node with a commission ratio of10%Deposit20000NULS.

```shell
nuls>>> createagent Nse2TpVsJd4gLoj79MAY8NHwEsYuXwtT NsdvAnqc8oEiNiGgcp6pEusfiRFZi4vt 10 20000
Please enter the password.
Enter your password:**********
"002006a5b7eb1d32ed6d7d54e24e219b112d4fdb8530db5506ee953b6f65a0fdb55e"
```
### Query consensus node information
according toagentHashQuery specified node information

-**command：getagent <agentHash>**
| parameter              | explain                                   |
| ----------------- | -------------------------------------- |
| &lt;agentHash&gt;   | nodehash                        |
Return value

```
slightly See example
```
Example

```
nuls>>> getagent 0020623133fe6e9a530b5658873439c6db3673cbeaa092d4a0c837ee00245f3b7eb7
{
  "agentAddress" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
  "agentId" : "5F3B7EB7",
  "commissionRate" : 10.0,
  "delHeight" : -1,
  "agentHash" : "0020623133fe6e9a530b5658873439c6db3673cbeaa092d4a0c837ee00245f3b7eb7",
  "totalDeposit" : "0",
  "memberCount" : 0,
  "agentName" : null,
  "packingAddress" : "tNULSeBaMnKhtrJpYY12S9wXGg2ASaTnk5km95",
  "version" : null,
  "blockHeight" : 2384,
  "rewardAddress" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
  "deposit" : "20000",
  "time" : "2019-03-26 16:06:55.055",
  "creditVal" : 0.0,
  "txHash" : "0020623133fe6e9a530b5658873439c6db3673cbeaa092d4a0c837ee00245f3b7eb7",
  "status" : "unconsensus"
}
```

### Query consensus node list
Query consensus node list
-**command：getagents [pageNumber] [pageSize] [keyWord]**
| parameter              | explain                                   |
| ----------------- | -------------------------------------- |
| [pageNumber];   | List page number position                      |
| [pageSize]; | Number of displayed items per page                  |
| [keyWord];   | Matching node alias keywords |
Return value

```
slightly See example
```
Example Get the number1Page of10Bar, alias bandnulsNode list for
```
nuls>>> getagents 1 10 nuls
[ {
  "agentAddress" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
  "agentId" : "5F3B7EB7",
  "commissionRate" : 10.0,
  "delHeight" : -1,
  "agentHash" : "0020623133fe6e9a530b5658873439c6db3673cbeaa092d4a0c837ee00245f3b7eb7",
  "totalDeposit" : "0",
  "memberCount" : 0,
  "agentName" : null,
  "packingAddress" : "tNULSeBaMnKhtrJpYY12S9wXGg2ASaTnk5km95",
  "version" : null,
  "blockHeight" : 2384,
  "rewardAddress" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
  "deposit" : "20000",
  "time" : "2019-03-26 16:06:55.055",
  "creditVal" : 0.0,
  "txHash" : "0020623133fe6e9a530b5658873439c6db3673cbeaa092d4a0c837ee00245f3b7eb7",
  "status" : "unconsensus"
} ]
```



### Join consensus（Delegate node）

Based on account address and nodeagentHashJoining consensus requires at least2000NULS

- **command：deposit &lt;address&gt; &lt;agentHash&gt; &lt;deposit&gt;**

| parameter              | explain                                   |
| ----------------- | -------------------------------------- |
| &lt;address&gt;   | Account address, required                         |
| &lt;agentHash&gt; | Node'sagentHash, mandatory                  |
| &lt;deposit&gt;   | Joining consensus margin, cannot be lower than2000NULS, mandatory |

Return Information Joining consensus on transactionshashIf you want to withdraw from this consensus, you need tohash.

```json
"0020d349b7ad322ff958e3abfa799d9ac76341afa6e1fb4d3857353a5adc74ba3fd0"
```

Example

```shell
nuls>>> deposit NsdtmV5XkgSdpBXi65ueTsrv2W5beV2T 002006a5b7eb1d32ed6d7d54e24e219b112d4fdb8530db5506ee953b6f65a0fdb55e 5000
"0020d349b7ad322ff958e3abfa799d9ac76341afa6e1fb4d3857353a5adc74ba3fd0"
```



### Exit consensus（Exit the commission）

Based on account address and transactions upon joining consensushashTo withdraw from the consensus(entrust)When a single account entrusts nodes multiple times, each transaction entrusted is independent, so when exiting, it also needs to go through the transaction during the single entrustmenthashExit the corresponding delegate instead of exiting all delegates at once.

- **command：withdraw &lt;address&gt; &lt;txHash&gt;**

| parameter            | explain                   |
| --------------- | ---------------------- |
| &lt;address&gt; | Account address, required         |
| &lt;txHash&gt;  | Transaction at the time of commissionhash, mandatory |

Return Information Exit consensus tradinghash

```json
"00201d70ac37b53d41c0e813ad245fc42e1d3a5d174d9148fbbbaed3c18d4d67bdbf"
```

Example

```shell
nuls>>> withdraw NsdtmV5XkgSdpBXi65ueTsrv2W5beV2T 0020d349b7ad322ff958e3abfa799d9ac76341afa6e1fb4d3857353a5adc74ba3fd0
"00201d70ac37b53d41c0e813ad245fc42e1d3a5d174d9148fbbbaed3c18d4d67bdbf"
```



### Stop node

Stop the node, all delegated tasks to the nodeNULSWill be refunded, and the deposit in the node creator's account will be locked72Hours.

- **command：stopagent &lt;address&gt;**

| parameter            | explain           |
| --------------- | -------------- |
| &lt;address&gt; | Account address, required |

Return Information Stop node transactionshash

```json
"0020f15eecd7c85be76521ed6af4d58a3810f7df58e536481cff4a96af6d4fddec5f"
```

Example

```shell
nuls>>> stopagent Nse2TpVsJd4gLoj79MAY8NHwEsYuXwtT
Please enter the password.
Enter your password:**********
"0020f15eecd7c85be76521ed6af4d58a3810f7df58e536481cff4a96af6d4fddec5f"
```


### Get the latest block header information

Get the latest block header information

- **command：getbestblockheader**

Return Information

```json
{
  "hash" : "0020b446a0244e4e46f8736f1ab56c33616facb836bc8344367f2f048b703f0c8f57",  //blockhash
  "preHash" : "0020c0dcf9209f66ee7e7778c817ba7c04d67b5e6a056b42dec7fbfe44eb5f91bdfc",  //Previous blockhash
  "merkleHash" : "00200511ced5779c54aa2170b941a1f9a7ae08dfd009b1dfaacc3679d15da9fb9c3e",  //merkle hash
  "time" : "2019-03-19 18:26:20.020",  //Packaging time
  "height" : 1479, //block height
  "txCount" : 1,   //Number of transactions included
  "blockSignature" : "00473045022100b1a07f6da3d4ce46cab278967d76875483527e3fc749a460afdf0c375f2ec2ae022053e40e8b4d8bf4e571284e45f18c46c31163ed640a2328f3ba90ac7708808365", //Block signature
  "size" : 0, //block size
  "packingAddress" : null,  //Packaging address
  "roundIndex" : 155299118, 
  "consensusMemberCount" : 100,
  "roundStartTime" : "2019-03-19 18:26:10.010",
  "packingIndexOfRound" : 1, 
  "mainVersion" : 1,
  "blockVersion" : 0,
  "stateRoot" : null
}
```

Example

```shell
nuls>>> getbestblockheader
{
  "hash" : "0020b446a0244e4e46f8736f1ab56c33616facb836bc8344367f2f048b703f0c8f57",
  "preHash" : "0020c0dcf9209f66ee7e7778c817ba7c04d67b5e6a056b42dec7fbfe44eb5f91bdfc",
  "merkleHash" : "00200511ced5779c54aa2170b941a1f9a7ae08dfd009b1dfaacc3679d15da9fb9c3e",
  "time" : "2019-03-19 18:26:20.020",
  "height" : 1479,
  "txCount" : 0,
  "blockSignature" : "00473045022100b1a07f6da3d4ce46cab278967d76875483527e3fc749a460afdf0c375f2ec2ae022053e40e8b4d8bf4e571284e45f18c46c31163ed640a2328f3ba90ac7708808365",
  "size" : 0,
  "packingAddress" : null,
  "roundIndex" : 155299118,
  "consensusMemberCount" : 100,
  "roundStartTime" : "2019-03-19 18:26:10.010",
  "packingIndexOfRound" : 1,
  "mainVersion" : 1,
  "blockVersion" : 0,
  "stateRoot" : null
}
```



### Query block header information

Based on block height or blockshashTo query block information, only one parameter must be selected as the query criterion.

- **command：getblock &lt;hash&gt; | &lt;height&gt;**

| parameter           | explain         |
| -------------- | ------------ |
| &lt;hash&gt;   | Blockedhashvalue |
| &lt;height&gt; | The height of the block   |

Return Information

```json
{
  "hash" : "0020b446a0244e4e46f8736f1ab56c33616facb836bc8344367f2f048b703f0c8f57",  //blockhash
  "preHash" : "0020c0dcf9209f66ee7e7778c817ba7c04d67b5e6a056b42dec7fbfe44eb5f91bdfc",  //Previous blockhash
  "merkleHash" : "00200511ced5779c54aa2170b941a1f9a7ae08dfd009b1dfaacc3679d15da9fb9c3e",  //merkle hash
  "time" : "2019-03-19 18:26:20.020",  //Packaging time
  "height" : 1479, //block height
  "txCount" : 1,   //Number of transactions included
  "blockSignature" : "00473045022100b1a07f6da3d4ce46cab278967d76875483527e3fc749a460afdf0c375f2ec2ae022053e40e8b4d8bf4e571284e45f18c46c31163ed640a2328f3ba90ac7708808365", //Block signature
  "size" : 0, //block size
  "packingAddress" : null,  //Packaging address
  "roundIndex" : 155299118, 
  "consensusMemberCount" : 100,
  "roundStartTime" : "2019-03-19 18:26:10.010",
  "packingIndexOfRound" : 1, 
  "mainVersion" : 1,
  "blockVersion" : 0,
  "stateRoot" : null
}
```

Example Obtain blocks based on height

```shell
nuls>>> getblock 28115
{
  "hash" : "0020b446a0244e4e46f8736f1ab56c33616facb836bc8344367f2f048b703f0c8f57",  //blockhash
  "preHash" : "0020c0dcf9209f66ee7e7778c817ba7c04d67b5e6a056b42dec7fbfe44eb5f91bdfc",  //Previous blockhash
  "merkleHash" : "00200511ced5779c54aa2170b941a1f9a7ae08dfd009b1dfaacc3679d15da9fb9c3e",  //merkle hash
  "time" : "2019-03-19 18:26:20.020",  //Packaging time
  "height" : 1479, //block height
  "txCount" : 1,   //Number of transactions included
  "blockSignature" : "00473045022100b1a07f6da3d4ce46cab278967d76875483527e3fc749a460afdf0c375f2ec2ae022053e40e8b4d8bf4e571284e45f18c46c31163ed640a2328f3ba90ac7708808365", //Block signature
  "size" : 0, //block size
  "packingAddress" : null,  //Packaging address
  "roundIndex" : 155299118, 
  "consensusMemberCount" : 100,
  "roundStartTime" : "2019-03-19 18:26:10.010",
  "packingIndexOfRound" : 1, 
  "mainVersion" : 1,
  "blockVersion" : 0,
  "stateRoot" : null
}
```



### Query block header information

Based on block height or blockshashTo query block header information, only one parameter must be selected as the query criterion.

- **command：getblockheader &lt;hash&gt; | &lt;height&gt;**

| parameter           | explain         |
| -------------- | ------------ |
| &lt;hash&gt;   | Blockedhashvalue |
| &lt;height&gt; | The height of the block   |

Return Information

```json
{
  "hash" : "0020c40f471756c88e7487fcc0d428545232120071b58f35e450891237d7b41eb817",//blockhash
  "preHash" : "0020fb1fd03cda7e2b6585256f4da85bdac7d8fc8bafa0740b8eb0ed577f3020b954",//Previous blockhash
  "merkleHash" : "0020474c5a353f235e8e8514328e1e98d6b653d4a5445473d160691e39121cd8b158",//Merkelhash
  "time" : "2018-07-16 16:29:30",//Block generation time
  "height" : 28115,//block height
  "txCount" : 2,//Number of block packaging transactions
  "packingAddress" : "NsdyF8gBxAfxCyiNbLzsENUvbJZ27mWw",//Packaging address
  "roundIndex" : 662578,//Consensus round
  "consensusMemberCount" : 1,//Number of members participating in consensus
  "roundStartTime" : "2018-07-16 16:29:20",//Current round start time
  "packingIndexOfRound" : 1,//The ranking of the blocks packaged in the current round
  "reward" : "0.001",//Consensus rewards
  "fee" : "0.001",//Packaging fees for blocks
  "confirmCount" : 6174,//Confirmation frequency
  "size" : 507,//block size
  "scriptSig" : "210381e44e0c2fffadc94603a41514f3e5b1c5fd53166be73eb8f49ce8c297059e5600473045022100d25b815fa30376247692fad856d3984acf45c9b49edd3d222e3afdab3169520c02200565a486e33358301848bf3d704c187ff8b2d1e859c93b704f713abb984584bf"//autograph
}
```

Example Obtain block heads based on height

```shell
nuls>>> getblockheader 28115
{
  "hash" : "0020c40f471756c88e7487fcc0d428545232120071b58f35e450891237d7b41eb817",
  "preHash" : "0020fb1fd03cda7e2b6585256f4da85bdac7d8fc8bafa0740b8eb0ed577f3020b954",
  "merkleHash" : "0020474c5a353f235e8e8514328e1e98d6b653d4a5445473d160691e39121cd8b158",
  "time" : "2018-07-16 16:29:30",
  "height" : 28115,
  "txCount" : 2,
  "packingAddress" : "NsdyF8gBxAfxCyiNbLzsENUvbJZ27mWw",
  "roundIndex" : 662578,
  "consensusMemberCount" : 1,
  "roundStartTime" : "2018-07-16 16:29:20",
  "packingIndexOfRound" : 1,
  "reward" : "0.001",
  "fee" : "0.001",
  "confirmCount" : 6280,
  "size" : 204,
  "scriptSig" : "210381e44e0c2fffadc94603a41514f3e5b1c5fd53166be73eb8f49ce8c297059e5600473045022100d25b815fa30376247692fad856d3984acf45c9b49edd3d222e3afdab3169520c02200565a486e33358301848bf3d704c187ff8b2d1e859c93b704f713abb984584bf"
}
```

### Create a smart contract
Call this interface to create a smart contract on the chain

- **command：createcontract &lt;sender> &lt;gaslimt> &lt;price> &lt;contractCode> [remark]**

| parameter           | explain         |
| -------------- | ------------ |
| &lt;sender&gt;   | Account address for creating smart contract |
| &lt;gaslimt&gt; | The maximum consumption for this contract creationGas   |
| &lt;price&gt; | Unit price, eachGasWhat is the valueNa,NayesNULSThe minimum unit of,1Nuls=1BillionNaThe minimum unit price of the system is25Na/Gas   |
| &lt;contractCode> | Contract codehexcoding |
| [remark]|Remarks|


Return Information Create transactions for contractshashAddress of the contract

```
{
  "txHash" : "00205fb44fd0924a57857e71d06ec0549366b5d879b2cbd68488ed88a2dbf96c130f",  //transactionhash
  "contractAddress" : "tNULSeBaN6ofkEqsPJmWVaeMpENTgmC5ifWtz9" //Contract address
}
```
Example Create a contract（contractCode Omit the middle part）

```
nuls>>> createcontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD 200000 25 504b03040a........000000800080051020000b31600000000 remarkdemo
The arguments structure: 
[ {
  "type" : "String",
  "name" : "name",
  "required" : true
}, {
  "type" : "String",
  "name" : "symbol",
  "required" : true
}, {
  "type" : "BigInteger",
  "name" : "initialAmount",
  "required" : true
}, {
  "type" : "int",
  "name" : "decimals",
  "required" : true
} ]
Please enter the arguments you want to fill in according to the arguments structure(eg. "a",2,["c",4],"","e" or "'a',2,['c',4],'','e'").
Enter the arguments:"KQB","KQB",10000,2
{
  "txHash" : "0020ec1d68eaed63e2db8649b0a39f16b7c5af24f86b787233f6ba6d577d7d090587",
  "contractAddress" : "tNULSeBaNBYK9MQcWWbfgFTHj2U4j8KQGDzzuK"
}
```
### Obtain basic contract information
Obtain the description information and constructor of the smart contract、List of parameters for calling methods

- **command：getcontractinfo &lt;contract address>**

| parameter           | explain         |
| -------------- | ------------ |
| &lt;contract address&gt;   | Contract address |


Return Information

```
slightly See example
```
Example

```

nuls>>> getcontractinfo tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L
getcontractinfo tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L
{
  "createTxHash" : "00203a48dcfc26426152805be49830c72005b4648d0182bbf6c2e8980380364eb59f",
  "address" : "tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L",
  "creater" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
  "createTime" : 1553563706022,
  "blockHeight" : 46,
  "isNrc20" : true,
  "nrc20TokenName" : "QKB",
  "nrc20TokenSymbol" : "QKB",
  "decimals" : 2,
  "totalSupply" : "200000000",
  "status" : "normal",
  "method" : [ {
    "name" : "<init>",
    "desc" : "(String name, String symbol, BigInteger initialAmount, int decimals) return void",
    "args" : [ {
      "type" : "String",
      "name" : "name",
      "required" : true
    }, {
      "type" : "String",
      "name" : "symbol",
      "required" : true
    }, {
      "type" : "BigInteger",
      "name" : "initialAmount",
      "required" : true
    }, {
      "type" : "int",
      "name" : "decimals",
      "required" : true
    } ],
    "returnArg" : "void",
    "view" : false,
    "event" : false,
    "payable" : false
  },{
    "name" : "transfer",
    "desc" : "(Address to, BigInteger value) return boolean",
    "args" : [ {
      "type" : "Address",
      "name" : "to",
      "required" : true
    }, {
      "type" : "BigInteger",
      "name" : "value",
      "required" : true
    } ],
    "returnArg" : "boolean",
    "view" : false,
    "event" : false,
    "payable" : false
  }]
}

```

### Calling smart contracts
Calling functions provided by smart contracts

- **command：callcontract &lt;sender> &lt;gasLimit> &lt;price> &lt;contractAddress> &lt;methodName> &lt;value> [-d methodDesc] [-r remark]**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;senderAddress&gt;   | Transfer the account address of the contract |
|&lt;gasLimit>|The maximum consumption during the execution of this contractGas|
|&lt;price>|Unit price, eachGasWhat is the valueNa,NayesNULSThe minimum unit of,1Nuls=1BillionNaThe minimum unit price of the system is25Na/Gas|
|&lt;contractAddress|Contract address called|
|&lt;methodName>|Method name of the contract|
|&lt;value>|If you want to transfer money to the contract, the number of transfers|
|[-d methodDesc]|If there is a method with the same name in the contract, use this method to describe the parameter list|
|[-r remark]|Remarks|

Return Information The transaction being called this timehash

```
"0020c9079e0f0454103adceed798d40171c41a8db04586dba966fbe7f2ab722583ad" //transactionhash
```
Example Invoke a specified contract'sNRC20-TokenTransfer function, In the example`tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L`byNRC20Contract address, input parameters are Receiving address and transfer quantity

```
nuls>>> callcontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD 200000 25 tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L transfer 0 -r call
callcontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD 200000 25 tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L transfer 0 -r call
Please Enter your account passwordzhoujun172
**********
Please enter the arguments according to the arguments structure(eg. "a",2,["c",4],"","e" or "'a',2,['c',4],'','e'"),
If this method has no arguments(Refer to the command named "getcontractinfo" for the arguments structure of the method.), return directly.
Enter the arguments:"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",2
"0020c9079e0f0454103adceed798d40171c41a8db04586dba966fbe7f2ab722583ad"
```

### Delete smart contract
Stop an available smart contract

- **command：deletecontract &lt;senderAddress> &lt;contractAddress>**

| parameter           | explain         |
| -------------- | ------------ |
| &lt;senderAddress&gt;   | Call the account address of the contract |
| &lt;contractAddress>|Contract address called|
Return value transactionhash

```
"0020c55e885dd910dad0b2c49f5b71b62691b57884ca21fd47668f1f5cadc84daad6" //transactionhash
```
Example

```
nuls>>> deletecontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L
deletecontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L
Please enter your account passwordzhoujun172
**********
"0020c55e885dd910dad0b2c49f5b71b62691b57884ca21fd47668f1f5cadc84daad6"
```

### Call Contract View Method
Calling the view method of the contract will immediately return the result without generating a transaction

- **command：deletecontractviewcontract &lt;contractAddress> &lt;methodName> [-d methodDesc] --view contract**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;contractAddress>|Contract address called|
|&lt;methodName>|Method called|
|[-d methodDesc]|If there is a method with the same name in the contract, use this method to describe the parameter list|
Return value

```
Depending on the specific function call, the return value may vary
```
Example callNRC20-TokenContract inquiryTokenThe balance function queries the specified addressTokenbalance

```
nuls>>> viewcontract tNULSeBaN6pwyVwXjfpm5BMH5eiirvthoZDVEc balanceOf
viewcontract tNULSeBaN6pwyVwXjfpm5BMH5eiirvthoZDVEc balanceOf
Please enter the arguments according to the arguments structure(eg. "a",2,["c",4],"","e" or "'a',2,['c',4],'','e'"),
If this method has no arguments(Refer to the command named "getcontractinfo" for the arguments structure of the method.), return directly.
Enter the arguments:"tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD"
"tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD"
{
  "result" : "20000000"
}
```

### Transfer to the contracted address

Transfer main network currency to the specified contract address

- **command：transfertocontract &lt;senderAddress> &lt;contractAddress> &lt;amount> [remark]**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;senderAddress>|Transfer account address|
|&lt;contractAddress|Transferred contract address|
|&lt;amount>|Transferred quantity|
|[remark]|Remarks|
Return value transactionhash

```
"0020f5d6b87c246595d1b060a3fa8bac6a2992490e38fdfcad40db2a3908297e7979"
```
Example Transfer to designated contract2individualNULS

```
nuls>>> transfertocontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD tNULSeBaN1NRtaj1ZPAmEmTqcpkyCLqv64PR7U 2 remark
Please enter your account password
**********
"0020f5d6b87c246595d1b060a3fa8bac6a2992490e38fdfcad40db2a3908297e7979"
```

### tokenTransfer

NRC20 tokenTransfer

- **command：tokentransfer &lt;formAddress> &lt;toAddress> &lt;contractAddress> &lt;amount> [remark]**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;formAddress>|Transfer account address|
|&lt;toAddress|Transferred account address|
|&lt;contractAddress>|Contract address|
|&lt;amount>|Transferred quantity|
|[remark]|Remarks|
Return value transactionhash

```
"002022dffd96026b493945d2cf9ad276c4bc9655c735b72e6fcc85a2d19f6cbe25e8"
```
Example tokenTransfer:

```
nuls>>> tokentransfer tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD  tNULSeBaNBh9RUsVrVmMy8NHcZJ2BhNVsM1Vta  tNULSeBaN6pwyVwXjfpm5BMH5eiirvthoZDVEc 200000 25 10000
Please enter your account password
**********
"002022dffd96026b493945d2cf9ad276c4bc9655c735b72e6fcc85a2d19f6cbe25e8"
```


### Obtaining contract transactions

Obtain transaction information for contracts, Including transaction details, contract call parameters, and contract execution results

- **command：getcontracttx &lt;hash>**

| parameter           | explain         |
| -------------- | ------------ |
| &lt;hash>|transactionhash|

Return value

```
slightly See example
```
Example

```
nuls>>> getcontracttx 00203a48dcfc26426152805be49830c72005b4648d0182bbf6c2e8980380364eb59f
getcontracttx 00203a48dcfc26426152805be49830c72005b4648d0182bbf6c2e8980380364eb59f
{
  "hash" : "00203a48dcfc26426152805be49830c72005b4648d0182bbf6c2e8980380364eb59f",
  "type" : "100",
  "time" : "2019-03-26 09:28:26",
  "blockHeight" : 46,
  "fee" : 0.0,
  "value" : 0.0,
  "remark" : null,
  "scriptSig" : "210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0004630440220112a446b2a684510b4016fa97b92d2f3fead03128f0f658c99a6a8d230d05d4e02201e23a2f6e68aacdff2d117bd5bbe7ce2440babfe4211168eafbae41acad5d505",
  "status" : "confirm",
  "confirmCount" : 0,
  "size" : 6686,
  "inputs" : [ {
    "address" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
    "assetsChainId" : 2,
    "assetId" : 1,
    "amount" : "5700000",
    "nonce" : "ffffffff",
    "locked" : 0,
    "value" : 0.0
  } ],
  "outputs" : [ ],
  "txData" : {
    "data" : {
      "sender" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
      "contractAddress" : "tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L",
      "value" : 0.0,
      "hexCode" : "504b03040a0000080...........31600000000",
      "gasLimit" : 200000,
      "price" : "0.00000025",
      "args" : [ [ "QKB" ], [ "QKB" ], [ "2000000" ], [ "2" ] ]
    }
  },
  "contractResult" : {
    "success" : true,
    "errorMessage" : null,
    "contractAddress" : "tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L",
    "result" : null,
    "gasLimit" : 200000,
    "gasUsed" : 14029,
    "price" : "0.00000025",
    "totalFee" : 0.0,
    "txSizeFee" : 0.0,
    "actualContractFee" : 0.0,
    "refundFee" : 0.0,
    "stateRoot" : "be76399c41a8cb4be5ecf80e04dab36830b124cb1c43fea6ca69ae62259899ba",
    "value" : 0.0,
    "stackTrace" : null,
    "balance" : 0.0,
    "transfers" : [ ],
    "events" : [ "{\"contractAddress\":\"tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L\",\"blockNumber\":46,\"event\":\"TransferEvent\",\"payload\":{\"from\":null,\"to\":\"tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD\",\"value\":\"200000000\"}}" ],
    "tokenTransfers" : [ {
      "contractAddress" : "tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L",
      "from" : null,
      "to" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
      "value" : "200000000",
      "name" : "QKB",
      "symbol" : "QKB",
      "decimals" : 2
    } ],
    "remark" : "create"
  }
}


```


### Obtain contract execution results

Obtain the execution result of a contract

- **command:getcontractresult &lt;hash>**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;hash>|transactionhash|

Return value

```
slightly See example
```
Example

```
nuls>>> getcontractresult 00203a48dcfc26426152805be49830c72005b4648d0182bbf6c2e8980380364eb59f
{
  "success" : true,
  "errorMessage" : null,
  "contractAddress" : "tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L",
  "result" : null,
  "gasLimit" : 200000,
  "gasUsed" : 14029,
  "price" : "0.00000025",
  "totalFee" : 0.0,
  "txSizeFee" : 0.0,
  "actualContractFee" : 0.0,
  "refundFee" : 0.0,
  "stateRoot" : "be76399c41a8cb4be5ecf80e04dab36830b124cb1c43fea6ca69ae62259899ba",
  "value" : 0.0,
  "stackTrace" : null,
  "balance" : 0.0,
  "transfers" : [ ],
  "events" : [ "{\"contractAddress\":\"tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L\",\"blockNumber\":46,\"event\":\"TransferEvent\",\"payload\":{\"from\":null,\"to\":\"tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD\",\"value\":\"200000000\"}}" ],
  "tokenTransfers" : [ {
    "contractAddress" : "tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L",
    "from" : null,
    "to" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
    "value" : "200000000",
    "name" : "QKB",
    "symbol" : "QKB",
    "decimals" : 2
  } ],
  "remark" : "create"
}

```


### Get contract constructor

Get the list of parameters that need to be passed in when creating a specified contract

- **command：getcontractcontructor &lt;contractCode>**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;contractCode>|Contract codehexcoding|

Return value

```
slightly See example
```
Example

```
nuls>>> getcontractcontructor 504b03040a000008000.........20000b31600000000
{
  "constructor" : {
    "name" : "<init>",
    "desc" : "(String name, String symbol, BigInteger initialAmount, int decimals) return void",
    "args" : [ {
      "type" : "String",
      "name" : "name",
      "required" : true
    }, {
      "type" : "String",
      "name" : "symbol",
      "required" : true
    }, {
      "type" : "BigInteger",
      "name" : "initialAmount",
      "required" : true
    }, {
      "type" : "int",
      "name" : "decimals",
      "required" : true
    } ],
    "returnArg" : "void",
    "view" : false,
    "event" : false,
    "payable" : false
  },
  "isNrc20" : true
}

```


### Obtain the list of contracts created by the specified account

Obtain the list of contracts created by the specified account address

- **command：getaccountcontracts &lt;createAddress>**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;createAddress>|Account address|

Return value

```
{
  "contractAddress" : "tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L",
  "createTime" : "2019-03-26 09:28:26.026",
  "height" : 46,
  "confirmCount" : 402,
  "remarkName" : null,
  "status" : 2,
  "msg" : null,
  "create" : true
}
```
Example

```
nuls>>> getaccountcontracts tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD
[ {
  "contractAddress" : "tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L",
  "createTime" : "2019-03-26 09:28:26.026",
  "height" : 46,
  "confirmCount" : 402,
  "remarkName" : null,
  "status" : 2,
  "msg" : null,
  "create" : true
}, {
  "contractAddress" : "tNULSeBaMzsHrbMy2VK23RzwjkXS1qo2ycG5Cg",
  "createTime" : "2019-03-25 16:08:25.025",
  "height" : 253,
  "confirmCount" : 195,
  "remarkName" : null,
  "status" : 0,
  "msg" : null,
  "create" : true
}, {
  "contractAddress" : "tNULSeBaNBYK9MQcWWbfgFTHj2U4j8KQGDzzuK",
  "createTime" : "2019-03-25 15:33:54.054",
  "height" : 46,
  "confirmCount" : 402,
  "remarkName" : null,
  "status" : 0,
  "msg" : null,
  "create" : true
} ]
```

### Query network information

Query basic network information

- **command：network info**

Return Information

```json
{
  "localBestHeight" : 35317,//Local latest block height
  "netBestHeight" : 35317,//The latest block height in the network
  "timeOffset" : "0ms",//Network time offset value
  "inCount" : 0,//Number of passive connection nodes
  "outCount" : 1//Number of active connection nodes
}
```

Example

```shell
nuls>>> network info
{
  "localBestHeight" : 35317,
  "netBestHeight" : 35317,
  "timeOffset" : "0ms",
  "inCount" : 0,
  "outCount" : 1
}
```



### Query network nodesIP

Query network nodesIP

- **command：network nodes**

Return Information

```json
[ "192.168.1.223" ]
```

Example Obtain blocks based on height

```shell
nuls>>> network nodes
[ "192.168.1.223" ]
```

### Register side chains on the main chain
The side chain needs to conduct cross chain transactions, and registration needs to be completed on the main chain first. This command needs to be run on the main network node
- **command： registercrosschain &lt;address> &lt;chainId> &lt;chainName> &lt;addressPrefix> &lt;magicNumber> &lt;maxSignatureCount> &lt;signatureBFTRatio>&lt;verifierList>&lt;assetId> &lt;symbol> &lt;assetName> &lt;initNumber> [decimalPlaces] [minAvailableNodeNum] [txConfirmedBlockNum]**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;address>|Register a cross chain fee payment account|
|&lt;chainId>|Registered Chainid|
|&lt;chainName>|Registered chain name|
|&lt;addressPrefix>|Address prefix|
|&lt;magicNumber>|The network magic parameters for the operation of the registration chain|
|&lt;maxSignatureCount>|Maximum number of signatures in the registration chain|
|&lt;signatureBFTRatio>|Byzantine proportion [67-100]|
|&lt;verifierList>|Initial Verifier List for Registration Chain|
|&lt;assetId>|Registered assetsid|
|&lt;symbol>|Asset abbreviation e.g. BTC|
|&lt;assetName>|Asset Name|
|&lt;initNumber>|Total discovered assets|
|[decimalPlaces]|Decimal places of assets default8|
|[minAvailableNodeNum]|Available conditions for cross chain transactions：Minimum number of available nodes, default5|
|[txConfirmedBlockNum]|Number of confirmation blocks for registration transactions, default30|
Return value

```
{
  "mainNetVerifierSeeds" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",#List of main network validators
  "txHash" : "25b3a57507086d5d895895b41ef744a160f3251f4e5db118b7ca833eb6c9fff3",#transactionhash
  "mainNetCrossConnectSeeds" : "192.168.1.192:8088"#Main network cross chain seed connection nodes
}

```
Example

```
nuls>>>registercrosschain tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn 12 nbtc btc 20197777 12  67 LJScusmPf5EfdEwbA8nRZEYqMbRXKp6y3oCb 1 btc bt 100000000 8 1

Please enter the password.
Enter your password:**********
{
  "mainNetVerifierSeeds" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
  "txHash" : "25b3a57507086d5d895895b41ef744a160f3251f4e5db118b7ca833eb6c9fff3",
  "mainNetCrossConnectSeeds" : "192.168.1.192:8088"
}

```




### Add side chain assets that require cross chain transactions to the main chain

Side chains are multi asset and require cross chain asset addition on existing chains. This command needs to be run on the main network node

- **command： addcrossasset &lt;address> &lt;chainId> &lt;assetId> &lt;symbol> &lt;assetName> &lt;initNumber> [decimalPlaces] **

| parameter            | explain                     |
| --------------- | ------------------------ |
| &lt;address>    | Add asset instruction fee payment account |
| &lt;chainId>    | Registered Chainid               |
| &lt;assetId>    | Registered assetsid             |
| &lt;symbol>     | Asset abbreviation e.g. BTC        |
| &lt;assetName>  | Asset Name                 |
| &lt;initNumber> | Total discovered assets             |
| [decimalPlaces] | Decimal places of assets default8       |

Return value

```
  "txHash" : "25b3a57507086d5d895895b41ef744a160f3251f4e5db118b7ca833eb6c9fff3",#transactionhash
```

Example

```
nuls>>>addcrossasset tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn  10 2 yuer CCY 300000000 8

Please enter the password.
Enter your password:**********
 "25b3a57507086d5d895895b41ef744a160f3251f4e5db118b7ca833eb6c9fff3"
  

```



### Remove side chain assets for cross chain transactions from the main chain

Side chains are multi asset, and when the cross chain transaction of that asset needs to be stopped on the main network, a removal instruction is used. If the last asset is left, the corresponding chain will also stop working after the instruction is executed. This command needs to be run on the main network node

- **command： disablecrossasset &lt;address> &lt;chainId> &lt;assetId>**

| parameter         | explain                   |
| ------------ | ---------------------- |
| &lt;address> | Address used when adding assets |
| &lt;chainId> | Remove registered chainsid         |
| &lt;assetId> | Remove registered assetsid       |

Return value

```
  "txHash" : "25b3a57507086d5d895895b41ef744a160f3251f4e5db118b7ca833eb6c9fff3",#transactionhash
```

Example

```
nuls>>>disablecrossasset tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn  10 3
Please enter the password.
Enter your password:**********
 "25b3a57507086d5d895895b41ef744a160f3251f4e5db118b7ca833eb6c9fff3"
  

```



### Restore side chains on the main chain

After registering and deleting the side chain on the main network, if it needs to be restored, the following command can be used to restore and update information. This command needs to be run on the main network node

- **command： updatecrosschain &lt;address> &lt;chainId> &lt;chainName> &lt;addressPrefix> &lt;magicNumber> &lt;maxSignatureCount> &lt;signatureBFTRatio>&lt;verifierList>&lt;assetId> &lt;symbol> &lt;assetName> &lt;initNumber> [decimalPlaces] [minAvailableNodeNum] [txConfirmedBlockNum]**

| parameter                   | explain                                      |
| ---------------------- | ----------------------------------------- |
| &lt;address>           | Register a cross chain fee payment account                      |
| &lt;chainId>           | Registered Chainid                                |
| &lt;chainName>         | Registered chain name                              |
| &lt;addressPrefix>     | Address prefix                                  |
| &lt;magicNumber>       | The network magic parameters for the operation of the registration chain                |
| &lt;maxSignatureCount> | Maximum number of signatures in the registration chain                        |
| &lt;signatureBFTRatio> | Byzantine proportion [67-100]                       |
| &lt;verifierList>      | Initial Verifier List for Registration Chain                    |
| &lt;assetId>           | Registered assetsid                              |
| &lt;symbol>            | Asset abbreviation e.g. BTC                         |
| &lt;assetName>         | Asset Name                                  |
| &lt;initNumber>        | Total discovered assets                              |
| [decimalPlaces]        | Decimal places of assets default8                        |
| [minAvailableNodeNum]  | Available conditions for cross chain transactions：Minimum number of available nodes, default5 |
| [txConfirmedBlockNum]  | Number of confirmation blocks for registration transactions, default30                |

Return value

```
{
  "mainNetVerifierSeeds" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",#List of main network validators
  "txHash" : "25b3a57507086d5d895895b41ef744a160f3251f4e5db118b7ca833eb6c9fff3",#transactionhash
  "mainNetCrossConnectSeeds" : "192.168.1.192:8088"#Main network cross chain seed connection nodes
}

```

Example

```
nuls>>>registercrosschain tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn 12 nbtc btc 20197777 12  67 LJScusmPf5EfdEwbA8nRZEYqMbRXKp6y3oCb 1 btc bt 100000000 8 1

Please enter the password.
Enter your password:**********
{
  "mainNetVerifierSeeds" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
  "txHash" : "25b3a57507086d5d895895b41ef744a160f3251f4e5db118b7ca833eb6c9fff3",
  "mainNetCrossConnectSeeds" : "192.168.1.192:8088"
}

```



### Query sidechain registration information

Query the registration information of a certain side chain on the main network

- **command：crosschaininfo &lt;chainId>**

| parameter         | explain       |
| ------------ | ---------- |
| &lt;chainId> | Registration chainid |

Return value

```{
 {
  "chainId" : 3,
  "chainName" : "testchain",
  "addressType" : "1",
  "addressPrefix" : "TBTC",
  "magicNumber" : 123456,
  "minAvailableNodeNum" : 5,
  "txConfirmedBlockNum" : 0,
  "regAddress" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
  "regTxHash" : "6c29d99c2b02cfc766ef25bee2ea619610a5fce1d778c3038885111f590ae312",
  "createTime" : 1557739548367,
  "verifierList" : [ "TBTCdusmPf5EfdEwbA8nRZEYqMbRXKp6y3oCb" ],
  "signatureByzantineRatio" : 67,
  "maxSignatureCount" : 12,
  "selfAssetKeyList" : [ "3-10" ],
  "totalAssetKeyList" : [ "3-10" ],
  "mainNetVerifierSeeds" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
  "mainNetCrossConnectSeeds" : "192.168.1.192:8088",
  "enable" : true
}
```

Return parameter description

| parameter                | required | type   | description                                |
| ------------------------ | -------- | ------ | ------------------------------------------ |
| chainId                  | true     | int    | Chain identification                                     |
| assetId                  | true     | int    | assetid                                     |
| chainName                | true     | string | Chain Name                                     |
| addressType              | true     | int    | The address type of the account created on the chain：1Within the ecosystem2Non ecological interior |
| addressPrefix            | true     | string | Address prefix 1-5individual Capital letters or numbers              |
| magicNumber              | true     | string | Network Magic Parameters                               |
| minAvailableNodeNum      | true     | int    | Minimum number of available nodes                           |
| txConfirmBlockNum        | true     | int    | Number of transaction confirmation blocks                               |
| regAddress               | true     | string | Registered payment address                               |
| regTxHash                | true     | string | transactionhash                                   |
| createTime               | true     | long   | Transaction submission time ,1970The number of seconds of difference              |
| verifierList             | true     | string | Verifier List                                 |
| signatureByzantineRatio  | true     | int    | Byzantine proportion [67-100]                        |
| maxSignatureCount        | true     | int    | Maximum number of signatures                                 |
| symbol                   | true     | string | Asset symbols                                   |
| assetName                | true     | string | Asset Name                                   |
| initNumber               | true     | string | Initial value of assets                                 |
| decimalPlaces            | true     | int    | Minimum divisible digits of assets                         |
| mainNetVerifierSeeds     | true     | string | Main network seed verifier address                         |
| mainNetCrossConnectSeeds | true     | string | Main network seed connection node address                       |
| enable                   | true     | string | Is it in use                                 |

Example

```
nuls>>> crosschaininfo 10
{
  "chainId" : 10,
  "chainName" : "nuls10",
  "addressType" : "1",
  "addressPrefix" : "LJS2",
  "magicNumber" : 2019888,
  "minAvailableNodeNum" : 1,
  "txConfirmedBlockNum" : 0,
  "regAddress" : "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn",
  "regTxHash" : "14539bbcb00b26e545168aa241c4484cf8aff42f373a2019959681e73f0acea8",
  "createTime" : 1565229647,
  "verifierList" : [ "LJS2dusmPf5EfdEwbA8nRZEYqMbRXKp6y3oCb" ],
  "signatureByzantineRatio" : 67,
  "maxSignatureCount" : 12,
  "selfAssetKeyList" : [ "10-1" ],
  "totalAssetKeyList" : [ "10-1" ],
  "mainNetVerifierSeeds" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
  "mainNetCrossConnectSeeds" : "192.168.1.192:8088",
  "enable" : true
}
```



### Query sidechain registered asset information

Query the registration information of a certain side chain asset on the main website

- **command：crosschaininfo &lt;chainId>**

| parameter         | explain       |
| ------------ | ---------- |
| &lt;chainId> | Registration chainid |
| &lt;assetId> | assetid     |

Return value

```{
 {
  "chainId" : 10,
  "assetId" : 2,
  "symbol" : "CCY",
  "assetName" : "yuer",
  "depositNuls" : "100000000000",
  "destroyNuls" : "20000000000",
  "initNumber" : "30000000000",
  "decimalPlaces" : 2,
  "enable" : false,
  "createTime" : 1565229429,
  "address" : "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn",
  "txHash" : "612eda872c6ca16c5a5f63cce70a64ac15852e2b3a403309b0d963d22d6391bc"
}
```

Return parameter description

| parameter     | required | type    | description                  |
| ------------- | -------- | ------- | ---------------------------- |
| chainId       | true     | int     | Chain identification                       |
| assetId       | true     | int     | assetid                       |
| &lt;symbol>   | true     | string  | Asset abbreviation e.g. BTC            |
| assetName     | true     | string  | Asset Name                     |
| depositNuls   | true     | long    | Number of main network assets mortgaged           |
| destroyNuls   | true     | long    | Number of main network assets destroyed           |
| initNumber    | true     | string  | Initial value of assets                   |
| decimalPlaces | true     | int     | Asset divisible digits               |
| enable        | true     | boolean | Is it available trueavailable,false Deactivate |
| createTime    | true     | long    | Transaction generation time                 |
| address       | true     | String  | Transaction payment address                 |
| txHash        | true     | String  | transactionhash                     |

Example

```

nuls>>> crossassetinfo 10 2
{
  "chainId" : 10,
  "assetId" : 2,
  "symbol" : "CCY",
  "assetName" : "yuer",
  "depositNuls" : "100000000000",
  "destroyNuls" : "20000000000",
  "initNumber" : "30000000000",
  "decimalPlaces" : 2,
  "enable" : false,
  "createTime" : 1565229429,
  "address" : "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn",
  "txHash" : "612eda872c6ca16c5a5f63cce70a64ac15852e2b3a403309b0d963d22d6391bc"
}

```












### Create cross chain transactions

- **command：createcrosstx &lt;formAddress> &lt;toAddress> &lt;assetChainId> &lt;assetId> &lt;amount> [remark]**


| parameter           | explain         |
| -------------- | ------------ |
|&lt;formAddress>|Transfer address|
|&lt;toAddress>|Transfer address|
|&lt;assetChainId>|Transfer of assetschainId|
|&lt;assetId>|Transfer assetsid|
|&lt;amount>|Number of transferred assets|
|&lt;remark>|Transfer remarks|
Return value:transactionhash
```
529bb34c0f4760fa55dd98b92d3e913ed2306b7ac1f93c4491007e266bb04ef5
```
Example

```
nuls>>> createcrosstx tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD M9busmFhQeu1Efn6rDyeQkFjHxv2dSzkuH8 2 1 1
Please enter the password.
Enter your password:**********
529bb34c0f4760fa55dd98b92d3e913ed2306b7ac1f93c4491007e266bb04ef5
```
### Query cross chain transaction confirmation status
- **command：getcrosstxstate  &lt;txHash>**

| parameter           | explain         |
| -------------- | ------------ |
|&lt;txHash>|transactionhash|

Return value

```
Confirmed | Unconfirmed
```
Example

```
nuls>>> getcrosstxstate 529bb34c0f4760fa55dd98b92d3e913ed2306b7ac1f93c4491007e266bb04ef5
Unconfirmed
```


### Exit wallet command program

Exiting the command line program for operating the wallet will not exit the launched wallet node.

- **command：exit**

Example

```shell
nuls>>> exit
```
