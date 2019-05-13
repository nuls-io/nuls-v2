
# Nuls_2.0-alpha-1 CLI 使用指南
## 介绍

本文档为NULS2.0 alpha版本测试网Linux版全节点钱包的使用指南，阅读本文档前用户需了解Linux系统的基本操作和使用方式，本文介绍了在Linux系统中如何利用NULS钱包创建账户、导入账户、转账、建立节点、委托等操作。我们建议用户利用Linux系统服务器建立稳定的NULS节点。

## 版本更新记录

|  版本  |  更新日期  |        内容        |
| :----: | :--------: | :----------------: |
| V1.0.0 | 2018-03-18 | alpha版功能 |

## 准备

### 服务器硬件配置


**建立NULS节点的服务器不低于如下配置：**

|     CPU     | 内存 |   硬盘   |  宽带   |
| :---------: | :---: | :------: | :-----: |
| 四核 3.0GHz | 16G  | 128G硬盘 | 20M上行 |


**推荐配置：**

|     CPU     | 内存 |   硬盘   |   宽带   |
| :---------: | :---: | :------: | :------: |
| 八核 3.0GHz | 32G  | 256G硬盘 | 100M上行 |



### 系统及内核版本

**Linux系统**

- CentOS 6,7

Linux内核版本推荐使用 2.6.32及以上

## 开始

### 下载

- 最新版本的全节点钱包NULS官网下载地址：http://nuls.io/wallet；GitHub地址：https://github.com/nuls-io/nuls-wallet-release

- 进入[NULS官网钱包下载](http://nuls.io/wallet)界面后，选择Linux download，我们提供了MEGA和百度云盘两种下载方式，用户可自行选择。

  Linux系统中下载v2.0.0-alpha-1版的钱包可以使用如下命令：

  ```shell
  $ wget https://media.githubusercontent.com/media/nuls-io/nuls-wallet-release/master/NULS-Wallet-linux64-1.0.0.tar.gz
  ```

  注：如果后续有其他版本，下载地址可能会不同。

### 安装

- 在Linux中解压已下载的文件

  ```shell
  $ tar -zxf NULS-Wallet-linux64-2.0.0-alpha-1.tar.gz
  ```

### 运行

- 进入解压后的目录，并运行启动脚本，启动全节点钱包

  ```shell
  $ cd NULS-Wallet-linux64-2.0.0-alpha-1
  $ ./start.sh
  ```

## 使用钱包

### 快速入门

- 在确定钱包已经启动后，启动钱包的命令行程序，可对钱包进行操作。

  进入cmdclient/1.0.0目录，执行如下命令：

  ```shell
  $ cd cmdclient/1.0.0
  $ ./cmd.sh
  ```

  将会出现NULS命名输入提示符`nuls>>>  ` ，然后可直接输入NULS钱包操作命令，来进行操作。

  例如，创建账户的示例如下：

  ```shell
  nuls>>> create
  Please enter the password (password is between 8 and 20 inclusive of numbers and letters), If you do not want to set a password, return directly.
  Enter your password:*********
  Please confirm new password:*********
  [ "Nse9EtaRwgVgN42pxURgZjUR33LUx1j1" ]
  nuls>>>
  ```

  执行`create`命令表示创建单个账户，然后输入密码，以及再次确认输入的密码，创建成功后将会返回账户的地址。



## 约定

- 设置密码规则：密码长度在8至20位，必须同时包含字母和数字。
- 命令参数说明： &lt;parameter&gt; 表示必填参数；[parameter] 表示选填参数。"|" 在参数中表示或者，表示前后参数只能选其一。

## 钱包命令

### 帮助命令

输出打印所有的命令，

- **命令： help [-a]|[group]|[command]**

| 参数 | 说明                 |
| :--- | :------------------- |
| -a   | 格式化打印命令，选填 |
| command|查看指定命令使用说明|
| group|查看指定命令组的所有命令使用说明|

返回信息 help

```json
getaccount <address> --get account information
```

返回信息 help -a

```json
getaccount <address> --get account information
	OPTIONS:
	<address> the account address - Required
```

示例

```shell
nuls>>> help
nuls>>> help -a
nuls>>> help account
nuls>>> help create
```



### 创建账户

创建账户，返回账户地址集合

- **命令： create [number]**

| 参数     | 说明                 |
| :------- | :------------------- |
| [number] | 创建账户的数量，选填 |

创建账户时，将会提示输入密码，为了保证资产安全，必须给账户设置密码；

返回账户集合

```json
[ "5MR_2CkDZtZRHGLD43JreUc8LsFBertsc9r", "5MR_2CXCCU89fj9RyQj9MgZVE7Pq3Mmk77p" ]
```

示例 

创建1个账户


```shell
nuls>>> create 
Please enter the new password(8-20 characters, the combination of letters and numbers).
Enter your new password:**********
Please confirm new password:**********
[ "5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW" ]
```
一次创建多个账户

```
nuls>>> create 3
Please enter the new password(8-20 characters, the combination of letters and numbers).
Enter your new password:**********
Please confirm new password:**********
[ "5MR_2CWdfU2VDERgQbWS1quGYAGD1iDDM4N", "5MR_2CcYq7fqrvKagReBmzG3qEz8qGkifCr", "5MR_2Cd6E2vAGZQxkqeXbeqThRxDGTFiLei" ]
```




### 备份账户

备份账户，将生成一个名称为账户地址，扩展名为.keystore的文件，该文件为账户的备份文件

- **命令：backup &lt;address&gt; [path]**

| 参数            | 说明                                                 |
| --------------- | ---------------------------------------------------- |
| &lt;address&gt; | 账户地址，必填                                       |
| [path]          | 文件生成备份文件的目标文件夹，默认为当前文件夹，选填 |

返回信息

```shell
The path to the backup file is /nuls/bin/NsdyM1Ls5qw8wutvAQsr93jxgq8qYAZy.keystore
```

示例 备份一个有密码的账户

```shell
nuls>>> backup 5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW /Users/zlj
Enter account password
***************
The path to the backup file is /Users/zlj/5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW.keystore
```

### 移除账户

根据账户地址移除本地账户，需要输入密码

- **命令：remove &lt;address&gt;**

| 参数            | 说明             |
| --------------- | ---------------- |
| &lt;address&gt; | 账户的地址，必填 |

返回信息

```json
Success
```

示例

```shell
nuls>>> remove 5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW
Enter your password for account**********
Success
```


### 修改账户密码

根据账户地址和账户密码重新设置新密码。

- **命令：resetpwd &lt;address&gt;**

| 参数            | 说明             |
| --------------- | ---------------- |
| &lt;address&gt; | 账户的地址，必填 |

返回信息

```json
Success
```

示例

```shell
nuls>>> resetpwd 5MR_2CWdfU2VDERgQbWS1quGYAGD1iDDM4N
Enter your old password:**********
Enter new password**********
Please confirm new password:**********
Success
```



### 设置别名

给账户设置一个别名，如果用此账户建立节点，别名将作为节点来源显示

- **命令：setalias &lt;address&gt; &lt;alias&gt;**

| 参数            | 说明             |
| --------------- | ---------------- |
| &lt;address&gt; | 账户的地址，必填 |
| &lt;alias&gt;   | 别名名称，必填   |

返回信息 交易hash

```json
txHash:0020f94f36aefd59f9cca9bff3c018fc287dc6c0bcd7fbeb047133cadb5747e7d98d"
```

示例

```shell
nuls>>> setalias 5MR_2CXrzwoCoP4vnUxHJ5gdUUXZJhCpjq9 zlj
Enter your account password**********
txHash:0020830971e02527f18f8f9e32f974d8c73ce6bd249de859cae170476b87d0ec9582
```



### 导入账户keystore

导入账户keystore文件，生成本地账户，如果本地已有该账户将无法导入。

- **命令：importkeystore &lt;path&gt;**

| 参数         | 说明                           |
| ------------ | ------------------------------ |
| &lt;path&gt; | 待导入的keystore文件地址，必填 |

注意：导入keystore文件生成账户时，需要原始密码

返回信息 导入的账户地址

```json
"NsdyM1Ls5qw8wutvAQsr93jxgq8qYAZy"
```

示例

```shell
nuls>>> importkeystore /Users/zhoulijun/5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW.keystore
Please enter the password (password is between 8 and 20 inclusive of numbers and letters), If you do not want to set a password, return directly.
Enter your password:**********
5MR_2CetN1KeWAVsaUsqD7JwMBwjGuRGpGW
```



### 导入账户私钥

导入账户私钥，生成本地账户，如果本地已有该账户将覆盖,导入时需要给账户设置密码。此功能可以用于忘记账户密码后，通过私钥重新找回账户。

- **命令：import &lt;privatekey&gt;**

| 参数               | 说明             |
| ------------------ | ---------------- |
| &lt;privatekey&gt; | 账户的私钥，必填 |


```json
"NsdyM1Ls5qw8wutvAQsr93jxgq8qYAZy"
```

示例

```shell
nuls>>> import 1c2b9fd4417c1aad8ae9f24c982ff294eb50a6462b873b79a879e805a9990346
Please enter the password (password is between 8 and 20 inclusive of numbers and letters), If you do not want to set a password, return directly.
Enter your password:**********
Please confirm new password:**********
5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu
```

### 查询账户信息

根据账户地址查询账户信息

- **命令：getaccount &lt;address&gt;**

| 参数            | 说明           |
| --------------- | :------------- |
| &lt;address&gt; | 账户地址，必填 |

返回信息

```json
{
  "encryptedPrikeyHex" : "724d68268849f3680d480c9257f33229c0fac88890d5355c0e4d9c457af5c6e8b8f9f7ca9fd52fbd8079fbce1782052d",  //加密后的私钥
  "alias" : "zlj",  //别名
  "baglance" : {  
    "freeze" : 0,   //冻结的资产数量
    "total" : 997999999800000,     //总的资产数量
    "available" : 997999999800000  //可用的资产数量
  },
  "address" : "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",  //账户地址
  "pubkeyHex" : "0211c45f28710cd26a2c45fb790895a0ff2e095a290f1825b31d80ebc30913c486" //公钥
}
```

示例

```shell
nuls>>> getaccount 5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu
{
  "encryptedPrikeyHex" : "724d68268849f3680d480c9257f33229c0fac88890d5355c0e4d9c457af5c6e8b8f9f7ca9fd52fbd8079fbce1782052d",  //加密后的私钥
  "alias" : "zlj",  //别名
  "baglance" : {  
    "freeze" : 0,   //冻结的资产数量
    "total" : 997999999800000,     //总的资产数量
    "available" : 997999999800000  //可用的资产数量
  },
  "address" : "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",  //账户地址
  "pubkeyHex" : "0211c45f28710cd26a2c45fb790895a0ff2e095a290f1825b31d80ebc30913c486" //加密后的公钥
}
```



### 查询账户列表

根据分页参数查询账户列表，所有账户以创建时间倒序输出。

- **命令：getaccounts &lt;pageNumber&gt; &lt;pageSize&gt;**

| 参数               | 说明                             |
| ------------------ | -------------------------------- |
| &lt;pageNumber&gt; | 页数，需要获取第几页的数据，必填 |
| &lt;pageSize&gt;   | 每一页显示的数据条数，必填       |

返回信息，将输出账户集合

```json
[ {
  "address" : "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",  //地址
  "alias" : null,  //别名
  "pubkeyHex" : "0211c45f28710cd26a2c45fb790895a0ff2e095a290f1825b31d80ebc30913c486",  //公钥
  "encryptedPrikeyHex" : "724d68268849f3680d480c9257f33229c0fac88890d5355c0e4d9c457af5c6e8b8f9f7ca9fd52fbd8079fbce1782052d"  //私钥
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



示例 获取账户列表

```shell
nuls>>> getaccounts
[ {
  "address" : "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",  //地址
  "alias" : null,  //别名
  "pubkeyHex" : "0211c45f28710cd26a2c45fb790895a0ff2e095a290f1825b31d80ebc30913c486",  //加密后的公钥
  "encryptedPrikeyHex" : "724d68268849f3680d480c9257f33229c0fac88890d5355c0e4d9c457af5c6e8b8f9f7ca9fd52fbd8079fbce1782052d"  //加密后的私钥
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



### 查询账户私钥

根据账户地址个密码查询账户私钥

- **命令：getprikey &lt;address&gt;**

| 参数            | 说明             |
| --------------- | ---------------- |
| &lt;address&gt; | 账户的地址，必填 |

返回信息 导入的账户的私钥（未加密）

```json
00a166d10c2cc4cd8f76449ff699ab3eee44fe4f82b4bb866f7bba02751a6fd655
```

示例

```shell
nuls>>> getprikey 5MR_2CXrzwoCoP4vnUxHJ5gdUUXZJhCpjq9
Enter your account password**********
7b4d3ec971fc01ea813b52f6c35091d43beac4a68550bae2db63975149244678
```



### 查询账户余额

根据账户地址查询账户余额

- **命令：getbalance &lt;address&gt;**

| 参数            | 说明             |
| --------------- | ---------------- |
| &lt;address&gt; | 账户的地址，必填 |

返回信息 导入的账户地址

```json
{
  "total" : "9999998.99",//余额
  "freeze" : "0",//已锁定余额
  "available" : "9999998.99"//可用余额
}
```

示例

```shell
nuls>>> getbalance Nse2TpVsJd4gLoj79MAY8NHwEsYuXwtT
{
  "total" : "9999998.99",
  "freeze" : "0",
  "available" : "9999998.99"
}
```



### 转账

根据账户地址或别名将NULS转入另一账户地址或别名中

- **命令：transfer &lt;formAddress&gt;|<formAlias> &lt;toAddress&gt;|<toAlias> &lt;amount&gt; [remark] **

| 参数              | 说明                                            |
| ----------------- | ----------------------------------------------- |
| &lt;formAddress&gt; | 转出地址(与formAlias任选一项）                                |
|<formAlias>|转出地址别名(与formAddress任选一项）|
| &lt;toAddress&gt; | 接收地址(与toAlias任选一项）                               |
|<toAlias>|接收地址别名(与toAddress任选一项）|
| &lt;amount&gt;    | 转账数量，必填 |
| [remark]          | 备注信息，选填                                  |

返回信息 转账交易hash

```json
"00200bef73ad728c48146c8a5eb0d76fe7325b85803c61d8357c16dba09ea33b3596"
```

示例

```shell
nuls>>> transfer Nse2TpVsJd4gLoj79MAY8NHwEsYuXwtT NsdtmV5XkgSdpBXi65ueTsrv2W5beV2T 100 转账
Please enter the password.
Enter your password:**********
"00200bef73ad728c48146c8a5eb0d76fe7325b85803c61d8357c16dba09ea33b3596"
```



### 查询交易详情

根据交易hash查询交易详细信息

- **命令：gettx &lt;hash&gt;**

| 参数         | 说明           |
| ------------ | -------------- |
| &lt;hash&gt; | 交易hash，必填 |

返回信息 交易详细信息

```json
{
  "type" : 2,  //交易类型（枚举说明见下表【type 枚举类型说明】）
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
  "blockHeight" : 341,   //区块高度
  "status" : "CONFIRMED",  //确认状态
  "size" : 225,
  "inBlockIndex" : 0,
  "coinDataInstance" : {  
    "from" : [ {
      "address" : "OTABmAU91zVafgSLoRrxnHo7k3r/Azg=",
      "assetsChainId" : 12345,
      "assetsId" : 1,
      "amount" : 100100000,
      "nonce" : "79acNPim+aw=",
      "locked" : 0
    } ],
    "to" : [ {
      "address" : "OTABL/80LO1f8vxvfNXc5l9eeIDTGKM=",
      "assetsChainId" : 12345,
      "assetsId" : 1,
      "amount" : 100000000,
      "lockTime" : 0
    } ]
  },
  "fee" : 100000,  //手续费
  "multiSignTx" : false
}
```

示例 查询转账交易

```shell
nuls>>> gettx 00200a2bc0207a55caa36bfe1f3bfc55a4eef5ec5ea05e90727ede441ce66669f87f
{
  "type" : 2,  //交易类型（枚举说明见下表【type 枚举类型说明】）
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
  "blockHeight" : 341,   //区块高度
  "status" : "CONFIRMED",  //确认状态
  "size" : 225,
  "inBlockIndex" : 0,
  "coinDataInstance" : {  
    "from" : [ {
      "address" : "OTABmAU91zVafgSLoRrxnHo7k3r/Azg=",
      "assetsChainId" : 12345,
      "assetsId" : 1,
      "amount" : 100100000,
      "nonce" : "79acNPim+aw=",
      "locked" : 0
    } ],
    "to" : [ {
      "address" : "OTABL/80LO1f8vxvfNXc5l9eeIDTGKM=",
      "assetsChainId" : 12345,
      "assetsId" : 1,
      "amount" : 100000000,
      "lockTime" : 0
    } ]
  },
  "fee" : 100000,  //手续费
  "multiSignTx" : false
}
```
#### type 枚举类型说明

```
/** coinbase交易*/
    int TX_TYPE_COINBASE = 1;
    /** 转账交易*/
    int TX_TYPE_TRANSFER = 2;
    /** 设置别名*/
    int TX_TYPE_ALIAS = 3;
    /** 创建共识节点交易*/
    int TX_TYPE_REGISTER_AGENT = 4;
    /** 委托交易(加入共识)*/
    int TX_TYPE_JOIN_CONSENSUS = 5;
    /** 取消委托交易(退出共识)*/
    int TX_TYPE_CANCEL_DEPOSIT = 6;
    /** 黄牌惩罚*/
    int TX_TYPE_YELLOW_PUNISH = 7;
    /** 红牌惩罚*/
    int TX_TYPE_RED_PUNISH = 8;
    /** 停止节点(删除共识节点)*/
    int TX_TYPE_STOP_AGENT = 9;
    /** 跨链转账交易*/
    int TX_TYPE_CROSS_CHAIN_TRANSFER = 10;
    /** 注册链交易*/
    int TX_TYPE_REGISTER_CHAIN_AND_ASSET = 11;
    /** 销毁链*/
    int TX_TYPE_DESTROY_CHAIN_AND_ASSET = 12;
    /** 为链新增一种资产*/
    int TX_TYPE_ADD_ASSET_TO_CHAIN = 13;
    /** 删除链上资产*/
    int TX_TYPE_REMOVE_ASSET_FROM_CHAIN = 14;
    /** 创建智能合约交易*/
    int TX_TYPE_CREATE_CONTRACT = 100;
    /** 调用智能合约交易*/
    int TX_TYPE_CALL_CONTRACT = 101;
    /** 删除智能合约交易*/
    int TX_TYPE_DELETE_CONTRACT = 102;
```

### 创建节点

根据账户地址创建节点,创建节点时需要提供两个地址，第一个地址为节点地址，需要输入节点地址账户密码，第二地址为打包地址，不需要输入密码。同时需要至少20000NULS的保证金。

- **命令：createagent &lt;agentAddress&gt; &lt;packingAddress&gt; &lt;commissionRate&gt; &lt;deposit&gt;**

| 参数                   | 说明                                                         |
| ---------------------- | ------------------------------------------------------------ |
| &lt;agentAddress&gt;   | 创建节点的账户地址，必填                                     |
| &lt;packingAddress&gt; | 节点打包账户地址，必填（注：该账户不能设置密码，否则节点不能打包出块） |
| &lt;commissionRate&gt; | 代理佣金比例，范围：10~100，必填                             |
| &lt;deposit&gt;        | 创建节点的保证金，不能低于20000NULS，必填                    |

返回信息 返回节点的agent hash

```json
"002006a5b7eb1d32ed6d7d54e24e219b112d4fdb8530db5506ee953b6f65a0fdb55e"
```

示例 创建一个节点，佣金比例为10%，押金20000NULS。

```shell
nuls>>> createagent Nse2TpVsJd4gLoj79MAY8NHwEsYuXwtT NsdvAnqc8oEiNiGgcp6pEusfiRFZi4vt 10 20000
Please enter the password.
Enter your password:**********
"002006a5b7eb1d32ed6d7d54e24e219b112d4fdb8530db5506ee953b6f65a0fdb55e"
```
### 查询共识节点信息
根据agentHash查询指定节点信息

-**命令：getagent <agentHash>**
| 参数              | 说明                                   |
| ----------------- | -------------------------------------- |
| &lt;agentHash&gt;   | 节点hash                        |
返回值

```
略 见示例
```
示例

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

### 查询共识节点列表
查询共识节点列表
-**命令：getagents [pageNumber] [pageSize] [keyWord]**
| 参数              | 说明                                   |
| ----------------- | -------------------------------------- |
| [pageNumber];   | 列表页号位置                      |
| [pageSize]; | 每页显示条数                  |
| [keyWord];   | 匹配节点别名关键字 |
返回值

```
略 见示例
```
示例 获取第1页，共10条，别名带nuls的节点列表
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



### 加入共识（委托节点）

根据账户地址和节点agentHash，加入共识，至少需要2000NULS

- **命令：deposit &lt;address&gt; &lt;agentHash&gt; &lt;deposit&gt;**

| 参数              | 说明                                   |
| ----------------- | -------------------------------------- |
| &lt;address&gt;   | 账户地址，必填                         |
| &lt;agentHash&gt; | 节点的agentHash，必填                  |
| &lt;deposit&gt;   | 加入共识保证金，不能低于2000NULS，必填 |

返回信息 加入共识的交易hash，如果要退出这笔共识，则需要该hash。

```json
"0020d349b7ad322ff958e3abfa799d9ac76341afa6e1fb4d3857353a5adc74ba3fd0"
```

示例

```shell
nuls>>> deposit NsdtmV5XkgSdpBXi65ueTsrv2W5beV2T 002006a5b7eb1d32ed6d7d54e24e219b112d4fdb8530db5506ee953b6f65a0fdb55e 5000
"0020d349b7ad322ff958e3abfa799d9ac76341afa6e1fb4d3857353a5adc74ba3fd0"
```



### 退出共识（退出委托）

根据账户地址和加入共识时的交易hash来退出共识(委托)，单个账户多次委托节点时，每次委托的交易是独立的，所以退出时也要通过单次委托时的交易hash来退出对应的那一次委托，而不会一次退出所有委托。

- **命令：withdraw &lt;address&gt; &lt;txHash&gt;**

| 参数            | 说明                   |
| --------------- | ---------------------- |
| &lt;address&gt; | 账户地址，必填         |
| &lt;txHash&gt;  | 委托时的交易hash，必填 |

返回信息 退出共识交易hash

```json
"00201d70ac37b53d41c0e813ad245fc42e1d3a5d174d9148fbbbaed3c18d4d67bdbf"
```

示例

```shell
nuls>>> withdraw NsdtmV5XkgSdpBXi65ueTsrv2W5beV2T 0020d349b7ad322ff958e3abfa799d9ac76341afa6e1fb4d3857353a5adc74ba3fd0
"00201d70ac37b53d41c0e813ad245fc42e1d3a5d174d9148fbbbaed3c18d4d67bdbf"
```



### 停止节点

停止节点，所有委托给节点的NULS将被退回，节点创建者账户的保证金将会被锁定72小时。

- **命令：stopagent &lt;address&gt;**

| 参数            | 说明           |
| --------------- | -------------- |
| &lt;address&gt; | 账户地址，必填 |

返回信息 停止节点交易hash

```json
"0020f15eecd7c85be76521ed6af4d58a3810f7df58e536481cff4a96af6d4fddec5f"
```

示例

```shell
nuls>>> stopagent Nse2TpVsJd4gLoj79MAY8NHwEsYuXwtT
Please enter the password.
Enter your password:**********
"0020f15eecd7c85be76521ed6af4d58a3810f7df58e536481cff4a96af6d4fddec5f"
```


### 获取最新的区块头信息

获取最新的区块头信息

- **命令：getbestblockheader**

返回信息

```json
{
  "hash" : "0020b446a0244e4e46f8736f1ab56c33616facb836bc8344367f2f048b703f0c8f57",  //区块hash
  "preHash" : "0020c0dcf9209f66ee7e7778c817ba7c04d67b5e6a056b42dec7fbfe44eb5f91bdfc",  //上一个区块hash
  "merkleHash" : "00200511ced5779c54aa2170b941a1f9a7ae08dfd009b1dfaacc3679d15da9fb9c3e",  //merkle hash
  "time" : "2019-03-19 18:26:20.020",  //打包时间
  "height" : 1479, //区块高度
  "txCount" : 1,   //包含的交易数
  "blockSignature" : "00473045022100b1a07f6da3d4ce46cab278967d76875483527e3fc749a460afdf0c375f2ec2ae022053e40e8b4d8bf4e571284e45f18c46c31163ed640a2328f3ba90ac7708808365", //区块签名
  "size" : 0, //区块大小
  "packingAddress" : null,  //打包地址
  "roundIndex" : 155299118, 
  "consensusMemberCount" : 100,
  "roundStartTime" : "2019-03-19 18:26:10.010",
  "packingIndexOfRound" : 1, 
  "mainVersion" : 1,
  "blockVersion" : 0,
  "stateRoot" : null
}
```

示例

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



### 查询区块头信息

根据区块高度或者区块hash，查询区块信息，必须并且只能选择一种参数作为查询条件。

- **命令：getblock &lt;hash&gt; | &lt;height&gt;**

| 参数           | 说明         |
| -------------- | ------------ |
| &lt;hash&gt;   | 区块的hash值 |
| &lt;height&gt; | 区块的高度   |

返回信息

```json
{
  "hash" : "0020b446a0244e4e46f8736f1ab56c33616facb836bc8344367f2f048b703f0c8f57",  //区块hash
  "preHash" : "0020c0dcf9209f66ee7e7778c817ba7c04d67b5e6a056b42dec7fbfe44eb5f91bdfc",  //上一个区块hash
  "merkleHash" : "00200511ced5779c54aa2170b941a1f9a7ae08dfd009b1dfaacc3679d15da9fb9c3e",  //merkle hash
  "time" : "2019-03-19 18:26:20.020",  //打包时间
  "height" : 1479, //区块高度
  "txCount" : 1,   //包含的交易数
  "blockSignature" : "00473045022100b1a07f6da3d4ce46cab278967d76875483527e3fc749a460afdf0c375f2ec2ae022053e40e8b4d8bf4e571284e45f18c46c31163ed640a2328f3ba90ac7708808365", //区块签名
  "size" : 0, //区块大小
  "packingAddress" : null,  //打包地址
  "roundIndex" : 155299118, 
  "consensusMemberCount" : 100,
  "roundStartTime" : "2019-03-19 18:26:10.010",
  "packingIndexOfRound" : 1, 
  "mainVersion" : 1,
  "blockVersion" : 0,
  "stateRoot" : null
}
```

示例 根据高度获取区块

```shell
nuls>>> getblock 28115
{
  "hash" : "0020b446a0244e4e46f8736f1ab56c33616facb836bc8344367f2f048b703f0c8f57",  //区块hash
  "preHash" : "0020c0dcf9209f66ee7e7778c817ba7c04d67b5e6a056b42dec7fbfe44eb5f91bdfc",  //上一个区块hash
  "merkleHash" : "00200511ced5779c54aa2170b941a1f9a7ae08dfd009b1dfaacc3679d15da9fb9c3e",  //merkle hash
  "time" : "2019-03-19 18:26:20.020",  //打包时间
  "height" : 1479, //区块高度
  "txCount" : 1,   //包含的交易数
  "blockSignature" : "00473045022100b1a07f6da3d4ce46cab278967d76875483527e3fc749a460afdf0c375f2ec2ae022053e40e8b4d8bf4e571284e45f18c46c31163ed640a2328f3ba90ac7708808365", //区块签名
  "size" : 0, //区块大小
  "packingAddress" : null,  //打包地址
  "roundIndex" : 155299118, 
  "consensusMemberCount" : 100,
  "roundStartTime" : "2019-03-19 18:26:10.010",
  "packingIndexOfRound" : 1, 
  "mainVersion" : 1,
  "blockVersion" : 0,
  "stateRoot" : null
}
```



### 查询区块头信息

根据区块高度或者区块hash，查询区块头信息，必须并且只能选择一种参数作为查询条件。

- **命令：getblockheader &lt;hash&gt; | &lt;height&gt;**

| 参数           | 说明         |
| -------------- | ------------ |
| &lt;hash&gt;   | 区块的hash值 |
| &lt;height&gt; | 区块的高度   |

返回信息

```json
{
  "hash" : "0020c40f471756c88e7487fcc0d428545232120071b58f35e450891237d7b41eb817",//区块hash
  "preHash" : "0020fb1fd03cda7e2b6585256f4da85bdac7d8fc8bafa0740b8eb0ed577f3020b954",//前一区块hash
  "merkleHash" : "0020474c5a353f235e8e8514328e1e98d6b653d4a5445473d160691e39121cd8b158",//梅克尔hash
  "time" : "2018-07-16 16:29:30",//区块生成时间
  "height" : 28115,//区块高度
  "txCount" : 2,//区块打包交易数量
  "packingAddress" : "NsdyF8gBxAfxCyiNbLzsENUvbJZ27mWw",//打包地址
  "roundIndex" : 662578,//共识轮次
  "consensusMemberCount" : 1,//参与共识成员数量
  "roundStartTime" : "2018-07-16 16:29:20",//当前轮次开始时间
  "packingIndexOfRound" : 1,//当前轮次打包出块的名次
  "reward" : "0.001",//共识奖励
  "fee" : "0.001",//区块的打包手续费
  "confirmCount" : 6174,//确认次数
  "size" : 507,//区块大小
  "scriptSig" : "210381e44e0c2fffadc94603a41514f3e5b1c5fd53166be73eb8f49ce8c297059e5600473045022100d25b815fa30376247692fad856d3984acf45c9b49edd3d222e3afdab3169520c02200565a486e33358301848bf3d704c187ff8b2d1e859c93b704f713abb984584bf"//签名
}
```

示例 根据高度获取区块头

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

### 创建智能合约
调用此接口在链上创建一个智能合约

- **命令：createcontract &lt;sender> &lt;gaslimt> &lt;price> &lt;contractCode> [remark]**

| 参数           | 说明         |
| -------------- | ------------ |
| &lt;sender&gt;   | 创建智能合约的账户地址 |
| &lt;gaslimt&gt; | 本次创建合约最大消耗的Gas   |
| &lt;price&gt; | 单价，每一个Gas值多少Na，Na是NULS的最小单位，1Nuls=1亿Na，系统最小单价是25Na/Gas   |
| &lt;contractCode> | 合约代码的hex编码 |
| [remark]|备注|


返回信息 创建合约的交易hash和合约的地址

```
{
  "txHash" : "00205fb44fd0924a57857e71d06ec0549366b5d879b2cbd68488ed88a2dbf96c130f",  //交易hash
  "contractAddress" : "tNULSeBaN6ofkEqsPJmWVaeMpENTgmC5ifWtz9" //合约地址
}
```
示例 创建一个合约（contractCode 省略中间部分）

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
### 获取合约基本信息
获取智能合约的描述信息以及构造函数、调用方法的参数列表

- **命令：getcontractinfo &lt;contract address>**

| 参数           | 说明         |
| -------------- | ------------ |
| &lt;contract address&gt;   | 合约地址 |


返回信息

```
略 ，见示例
```
示例

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
  }，{
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

### 调用智能合约
调用智能合约提供的函数

- **命令：callcontract &lt;sender> &lt;gasLimit> &lt;price> &lt;contractAddress> &lt;methodName> &lt;value> [-d methodDesc] [-r remark]**

| 参数           | 说明         |
| -------------- | ------------ |
|&lt;senderAddress&gt;   | 调动合约的账户地址 |
|&lt;gasLimit>|本次合约执行最大消耗的Gas|
|&lt;price>|单价，每一个Gas值多少Na，Na是NULS的最小单位，1Nuls=1亿Na，系统最小单价是25Na/Gas|
|&lt;contractAddress|调用的合约地址|
|&lt;methodName>|合约的方法名|
|&lt;value>|如果要向合约转账，转账的数量|
|[-d methodDesc]|如果合约中有同名方法时，使用此方法来描述参数列表|
|[-r remark]|备注信息|

返回信息 本次调用的交易hash

```
"0020c9079e0f0454103adceed798d40171c41a8db04586dba966fbe7f2ab722583ad" //交易hash
```
示例 调用一个指定合约的NRC20-Token转账函数, 示例中`tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L`为NRC20合约地址，输入的参数为 接收地址和转账数量

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

### 删除智能合约
停止一个可用的智能合约

- **命令：deletecontract &lt;senderAddress> &lt;contractAddress>**

| 参数           | 说明         |
| -------------- | ------------ |
| &lt;senderAddress&gt;   | 调用合约的账户地址 |
| &lt;contractAddress>|调用的合约地址|
返回值 交易hash

```
"0020c55e885dd910dad0b2c49f5b71b62691b57884ca21fd47668f1f5cadc84daad6" //交易hash
```
示例

```
nuls>>> deletecontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L
deletecontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD tNULSeBaMz7vkyhgqLXVdcT75dC5udULVs1D2L
Please enter your account passwordzhoujun172
**********
"0020c55e885dd910dad0b2c49f5b71b62691b57884ca21fd47668f1f5cadc84daad6"
```

### 调用合约视图方法
调用合约的视图方法，会立即返回结果，不会产生交易

- **命令：deletecontractviewcontract &lt;contractAddress> &lt;methodName> [-d methodDesc] --view contract**

| 参数           | 说明         |
| -------------- | ------------ |
|&lt;contractAddress>|调用的合约地址|
|&lt;methodName>|调用的方法|
|[-d methodDesc]|如果合约中有同名方法时，使用此方法来描述参数列表|
返回值

```
根据具体调用函数返回值不同
```
示例 调用NRC20-Token合约的查询Token余额函数查询指定地址的Token余额

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

### 向合约地址转账

向指定的合约地址转入主网币

- **命令：transfertocontract &lt;senderAddress> &lt;contractAddress> &lt;amount> [remark]**

| 参数           | 说明         |
| -------------- | ------------ |
|&lt;senderAddress>|转出账户地址|
|&lt;contractAddress|转入的合约地址|
|&lt;amount>|转入数量|
|[remark]|备注|
返回值 交易hash

```
"0020f5d6b87c246595d1b060a3fa8bac6a2992490e38fdfcad40db2a3908297e7979"
```
示例 向指定合约转入2个NULS

```
nuls>>> transfertocontract tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD tNULSeBaN1NRtaj1ZPAmEmTqcpkyCLqv64PR7U 2 remark
Please enter your account password
**********
"0020f5d6b87c246595d1b060a3fa8bac6a2992490e38fdfcad40db2a3908297e7979"
```

### token转账

NRC20 token转账

- **命令：tokentransfer &lt;formAddress> &lt;toAddress> &lt;contractAddress> &lt;amount> [remark]**

| 参数           | 说明         |
| -------------- | ------------ |
|&lt;formAddress>|转出账户地址|
|&lt;toAddress|转入的账户地址|
|&lt;contractAddress>|合约地址|
|&lt;amount>|转入数量|
|[remark]|备注|
返回值 交易hash

```
"002022dffd96026b493945d2cf9ad276c4bc9655c735b72e6fcc85a2d19f6cbe25e8"
```
示例 token转账:

```
nuls>>> tokentransfer tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD  tNULSeBaNBh9RUsVrVmMy8NHcZJ2BhNVsM1Vta  tNULSeBaN6pwyVwXjfpm5BMH5eiirvthoZDVEc 200000 25 10000
Please enter your account password
**********
"002022dffd96026b493945d2cf9ad276c4bc9655c735b72e6fcc85a2d19f6cbe25e8"
```


### 获取合约交易

获取合约的交易信息, 包含交易详情，合约调用参数，合约执行结果

- **命令：getcontracttx &lt;hash>**

| 参数           | 说明         |
| -------------- | ------------ |
| &lt;hash>|交易hash|

返回值

```
略 见示例
```
示例

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
    "assetsId" : 1,
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


### 获取合约执行结果

获取一个合约的执行结果

- **命令:getcontractresult &lt;hash>**

| 参数           | 说明         |
| -------------- | ------------ |
|&lt;hash>|交易hash|

返回值

```
略 见示例
```
示例

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


### 获取合约构造函数

获取创建指定合约时需要传入的参数列表

- **命令：getcontractcontructor &lt;contractCode>**

| 参数           | 说明         |
| -------------- | ------------ |
|&lt;contractCode>|合约代码的hex编码|

返回值

```
略 见示例
```
示例

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


### 获取指定账户创建的合约列表

获取指定账户地址所创建的合约列表

- **命令：getaccountcontracts &lt;createAddress>**

| 参数           | 说明         |
| -------------- | ------------ |
|&lt;createAddress>|账户地址|

返回值

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
示例

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

### 查询网络信息

查询网络基本信息

- **命令：network info **

返回信息

```json
{
  "localBestHeight" : 35317,//本地最新区块高度
  "netBestHeight" : 35317,//网络最新区块高度
  "timeOffset" : "0ms",//网络时间偏移值
  "inCount" : 0,//被动连接节点数量
  "outCount" : 1//主动连接节点数量
}
```

示例

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



### 查询网络节点IP

查询网络节点IP

- **命令：network nodes **

返回信息

```json
[ "192.168.1.223" ]
```

示例 根据高度获取区块

```shell
nuls>>> network nodes
[ "192.168.1.223" ]
```




### 退出钱包命令程序

退出操作钱包的命令行程序，不会退出已启动的钱包节点。

- **命令：exit**

示例

```shell
nuls>>> exit
```
