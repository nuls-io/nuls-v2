# NULS2.0 SDK-Provider

**NULS为合作伙伴定制了对接需要的NULS2.0钱包版本，对接钱包内嵌`NULS-SDK-Provider`模块，模块内封装了NULS-SDK的功能，用HTTP协议访问接口，支持`JSON—RPC`和`Restful`两种格式。**

[测试版钱包下载地址](http://nuls-usa-west.oss-us-west-1.aliyuncs.com/2.0/NULS-Wallet-linux64-beta1.1.tar.gz)

[NULS-SDK-Provider离线操作工具下载地址](http://nuls-usa-west.oss-us-west-1.aliyuncs.com/2.0/nuls-sdk-provider-offline-beta1.1.tar.gz)

## 设置

​	`NULS-SDK-Provider`模块默认访问的端口号是18004，可以在nuls.ncf配置文件中做修改，如下：

```
[nuls-sdk-provider]
#httpServer的启动port
server_port=18004
```

## 说明

​	为了更好的理解NULS2.0的相关业务，和接口返回值的含义，提前在这里做一些说明。

#### 在线与离线

`NULS-SDK-Provider`模块提供了若干在线接口和离线接口。

在线接口：钱包必须正常运行，且能够连接网络中的其他节点，能够正常同步区块和广播数据。在调用在线接口之前，最好是已经同步到最新区块。接口所产生的数据都会保存在钱包中。例如创建账户、修改密码、转账、获取区块头等。

离线接口：NULS2.0提供了一个专门用于[离线操作的NULS-SDK-Provider工具](http://nuls-usa-west.oss-us-west-1.aliyuncs.com/2.0/nuls-sdk-provider-offline-beta1.1.tar.gz)。无需安装钱包，可独立运行在一台没有连接网络的服务器上。用户通过调用离线接口，传入相关的参数，获取返回值，相应数据不会存入钱包。例如离线创建账户、离线组装转账交易、离线签名等。

#### 字段描述

**链的chainId:**

​	NULS2.0支持多链并行和跨链转账，每条链通过链ID来区分，NULS主网的链ID为1，NULS测试网的链ID为2。

**链的资产：**

​	NULS2.0支持每条链除了默认的资产外，可根据业务需要，动态添加资产。每条链的每种资产通过链ID和资产ID的复合主键来区分。例如NULS主网的NULS，chainId=1,assetId=1

**交易的type值：**

​	NULS2.0默认有多种交易，每种交易的功能不同，调用接口查询交易详情时，可通过type字段来区分不同交易类型，以下是交易类型的枚举值：

```
int COIN_BASE = 1;						// coinBase出块奖励
int TRANSFER = 2;						// 转账
int ACCOUNT_ALIAS = 3;					// 设置账户别名
int REGISTER_AGENT = 4;					// 新建共识节点
int DEPOSIT = 5;						// 委托参与共识
int CANCEL_DEPOSIT = 6;					// 取消委托共识
int YELLOW_PUNISH = 7;					// 黄牌
int RED_PUNISH = 8;						// 红牌
int STOP_AGENT = 9;						// 注销共识节点
int CROSS_CHAIN = 10;					// 跨链转账
int REGISTER_CHAIN_AND_ASSET = 11;		// 注册链
int DESTROY_CHAIN_AND_ASSET = 12;		// 注销链
int ADD_ASSET_TO_CHAIN = 13;			// 为链新增一种资产
int REMOVE_ASSET_FROM_CHAIN = 14;		// 删除链上资产
int CREATE_CONTRACT = 15;				// 创建智能合约
int CALL_CONTRACT = 16;					// 调用智能合约
int DELETE_CONTRACT = 17;				// 删除智能合约
int CONTRACT_TRANSFER = 18;				// 合约内部转账
int CONTRACT_RETURN_GAS = 19;			// 合约执行手续费返还
int CONTRACT_CREATE_AGENT = 20;			// 合约新建共识节点
int CONTRACT_DEPOSIT = 21;				// 合约委托参与共识
int CONTRACT_CANCEL_DEPOSIT = 22;		// 合约取消委托共识
int CONTRACT_STOP_AGENT = 23;			// 合约注销共识节点
int VERIFIER_CHANGE = 24;				// 验证人变更
```

**交易的from和to：**

用转账交易为例：tx.type = 2

​	from为转账交易的转出方，每一个from视为一个转账人的某一种资产转出多少数量，其中nonce值每次转账后都会改变，可通过调用查询账户余额接口获取当前最新nonce值。

​	to为转账交易的接收方，每一个to视为接收人接收到某一种资产多少数量，其中lockTime为锁定时间。当锁定时间大于0时，表示现实时间超过这个值之后，这笔资产才能正常使用；当lockTime =-1时，表示永久锁定中，需要特殊的交易才能解除锁定，例如参与委托共识和取消委托共识。

​	交易的手续费 = from里本链主资产之和 - to里本链主资产之和


## 访问方式

- **`JSON-RPC`访问方式**

     添加请求头 Content-Type: application/json;charset=UTF-8
     
     HttpMethod: POST
     
     URL: http://${ip}:${port}/jsonrpc 
     
        示例: http://127.0.0.1:18004/jsonrpc
     
     请求数据格式: 
     
     ```json
     {
       "jsonrpc":"2.0",
       "method":"methodCMD", //接口名称
       "params":[],          //所有接口的参数，都以数组方式传递，且参数顺序不能变，若参数是非必填，也必须填入null占位
       "id":1234
     }
     ```

- **`RESTFUL`访问方式**

     添加请求头 Content-Type: application/json;charset=UTF-8
     
     其余请参考 [RESTFUL 接口文档](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_RESTFUL.md)


## 接口文档

我们对外提供的API接口，分为`JSON-RPC`和`Restful`两种风格，用户可根据需要选择不通过的对接方式，接口文档详见以下: 

[JSON-RPC 接口文档](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_JSONRPC.md)

[RESTFUL 接口文档](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_RESTFUL.md)

_**附：**_ 官方已提供NULS-SDK-4J工具，有使用JAVA做对接的合作伙伴，可使用工具对接`NULS-SDK-Provider`模块，详见：[NULS-SDK-4J使用说明](https://github.com/nuls-io/nuls-v2-sdk4j/blob/master/README.md)

## 接口调试

我们提供了`Postman`接口调式工具的导入文件(`JSON-RPC`和`RESTFUL`)，导入后，即可调试接口

[JSON-PRC 接口调试-POSTMAN导入文件](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_Postman_JSONRPC.json)

[RESTFUL 接口调试-POSTMAN导入文件](https://github.com/nuls-io/nuls-sdk-provider/blob/master/documents/nuls-sdk-provider_Postman_RESTFUL.json)



0.1 获取本链相关信息
============
Cmd: /api/info
--------------
_**详细描述: 获取本链相关信息**_
### HttpMethod: GET

参数列表
----
无参数

返回值
---
| 字段名             |  字段类型  | 参数描述         |
| --------------- |:------:| ------------ |
| chainId         | string | 本链的ID        |
| assetId         | string | 本链默认主资产的ID   |
| inflationAmount | string | 本链默认主资产的初始数量 |
| agentChainId    | string | 本链共识资产的链ID   |
| agentAssetId    | string | 本链共识资产的ID    |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

1.1 批量创建账户
==========
Cmd: /api/account
-----------------
_**详细描述: 创建的账户存在于本地钱包内**_
### HttpMethod: POST

### Form json data: 
```json
{
  "count" : 0,
  "prefix" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                      |       参数类型        | 参数描述     | 是否必填 |
| -------------------------------------------------------- |:-----------------:| -------- |:----:|
| 批量创建账户                                                   | accountcreateform | 批量创建账户表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;count    |        int        | 新建账户数量   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;prefix   |      string       | 地址前缀     |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |      string       | 账户密码     |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述 |
| ---- |:---------------:| ---- |
| list | list&lt;string> | 账户地址 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "count" : 1,
  "password" : "abcd1234"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "list" : [ "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG" ]
  }
}
```

1.2 修改账户密码
==========
Cmd: /api/account/password/{address}
------------------------------------
_**详细描述: 修改账户密码**_
### HttpMethod: PUT

### Form json data: 
```json
{
  "password" : null,
  "newPassword" : null
}
```

参数列表
----
| 参数名                                                         |           参数类型            | 参数描述     | 是否必填 |
| ----------------------------------------------------------- |:-------------------------:| -------- |:----:|
| address                                                     |          string           | 账户地址     |  是   |
| 账户密码信息                                                      | accountupdatepasswordform | 账户密码信息表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password    |          string           | 原始密码     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newPassword |          string           | 新密码      |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否修改成功 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

1.3 导出账户私钥
==========
Cmd: /api/account/prikey/{address}
----------------------------------
_**详细描述: 只能导出本地钱包已存在账户的私钥**_
### HttpMethod: POST

### Form json data: 
```json
{
  "password" : null
}
```

参数列表
----
| 参数名                                                      |        参数类型         | 参数描述     | 是否必填 |
| -------------------------------------------------------- |:-------------------:| -------- |:----:|
| address                                                  |       string        | 账户地址     |  是   |
| 账户密码信息                                                   | accountpasswordform | 账户密码信息表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |       string        | 密码       |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 私钥   |
### Example request data: 

_**request path:**_
http://localhost:9898/api/account/prikey/tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG

_**request form data:**_
```json
{
  "password" : "abcd1111"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "c55c80b0afcbebea36bc2cc1f07a1946935fe578c0c8c35190180f99619d5f48"
  }
}
```

1.4 根据私钥导入账户
============
Cmd: /api/account/import/pri
----------------------------
_**详细描述: 导入私钥时，需要输入密码给明文私钥加密**_
### HttpMethod: POST

### Form json data: 
```json
{
  "priKey" : null,
  "password" : null,
  "overwrite" : false
}
```

参数列表
----
| 参数名                                                       |           参数类型            | 参数描述                           | 是否必填 |
| --------------------------------------------------------- |:-------------------------:| ------------------------------ |:----:|
| 根据私钥导入账户                                                  | accountprikeypasswordform | 根据私钥导入账户表单                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;priKey    |          string           | 私钥                             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password  |          string           | 密码                             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;overwrite |          boolean          | 是否覆盖账户: false:不覆盖导入, true:覆盖导入 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 账户地址 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "priKey" : "c55c80b0afcbebea36bc2cc1f07a1946935fe578c0c8c35190180f99619d5f48",
  "password" : "abcd1234",
  "overwrite" : true
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG"
  }
}
```

1.5 根据keyStore导入账户
==================
Cmd: /api/account/import/keystore
---------------------------------
_**详细描述: 根据keyStore导入账户**_
### HttpMethod: POST

参数列表
----
| 参数名                                                      |    参数类型     | 参数描述       | 是否必填 |
| -------------------------------------------------------- |:-----------:| ---------- |:----:|
| 根据私钥导入账户                                                 | inputstream | 根据私钥导入账户表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;根据私钥导入账户 | inputstream | 根据私钥导入账户表单 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 账户地址 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

1.6 根据keystore文件路径导入账户
======================
Cmd: /api/account/import/keystore/path
--------------------------------------
_**详细描述: 根据keystore文件路径导入账户**_
### HttpMethod: POST

### Form json data: 
```json
{
  "path" : null,
  "password" : null,
  "overwrite" : false
}
```

参数列表
----
| 参数名                                                       |           参数类型            | 参数描述                           | 是否必填 |
| --------------------------------------------------------- |:-------------------------:| ------------------------------ |:----:|
| 根据keystore文件路径导入账户                                        | accountkeystoreimportform | 根据keystore文件路径导入账户表单           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;path      |          string           | 本地keystore文件路径                 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password  |          string           | 密码                             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;overwrite |          boolean          | 是否覆盖账户: false:不覆盖导入, true:覆盖导入 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 账户地址 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "path" : "e:\\tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG.keystore",
  "password" : "abcd1234",
  "overwrite" : true
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG"
  }
}
```

1.7 根据keystore字符串导入账户
=====================
Cmd: /api/account/import/keystore/json
--------------------------------------
_**详细描述: 根据keystore字符串导入账户**_
### HttpMethod: POST

### Form json data: 
```json
{
  "keystore" : {
    "address" : null,
    "encryptedPrivateKey" : null,
    "pubKey" : null,
    "prikey" : null
  },
  "password" : null,
  "overwrite" : false
}
```

参数列表
----
| 参数名                                                                                                                 |             参数类型              | 参数描述                           | 是否必填 |
| ------------------------------------------------------------------------------------------------------------------- |:-----------------------------:| ------------------------------ |:----:|
| 根据keystore字符串导入账户                                                                                                   | accountkeystorejsonimportform | 根据keystore字符串导入账户表单            |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;keystore                                                            |            object             | keystore字符串                    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address             |            string             | 账户地址                           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPrivateKey |            string             | 加密后的私钥                         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKey              |            string             | 公钥                             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;prikey              |            string             | 私钥                             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password                                                            |            string             | 密码                             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;overwrite                                                           |            boolean            | 是否覆盖账户: false:不覆盖导入, true:覆盖导入 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 账户地址 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "keystore" : {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "encryptedPrivateKey" : "54793157409d0414248ef290eac96270c1a0115d712e845f0eb372bb977cbc0cafe39d598175473fa1bd5329dd1fae95",
    "pubKey" : "023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e6",
    "prikey" : null
  },
  "password" : "abcd1234",
  "overwrite" : true
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG"
  }
}
```

1.8 账户备份，导出AccountKeyStore文件到指定目录
=================================
Cmd: /api/account/export/{address}
----------------------------------
_**详细描述: 账户备份，导出AccountKeyStore文件到指定目录**_
### HttpMethod: POST

### Form json data: 
```json
{
  "password" : null,
  "path" : null
}
```

参数列表
----
| 参数名                                                      |         参数类型          | 参数描述           | 是否必填 |
| -------------------------------------------------------- |:---------------------:| -------------- |:----:|
| address                                                  |        string         | 账户地址           |  是   |
| keystone导出信息                                             | accountkeystorebackup | keystone导出信息表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |        string         | 密码             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;path     |        string         | 文件路径           |  是   |

返回值
---
| 字段名  |  字段类型  | 参数描述    |
| ---- |:------:| ------- |
| path | string | 导出的文件路径 |
### Example request data: 

_**request path:**_
http://localhost:9898/api/account/export/tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG

_**request form data:**_
```json
{
  "password" : "abcd1234",
  "path" : "e:/"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "path" : "e:\\\\tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG.keystore"
  }
}
```

1.9 账户设置别名
==========
Cmd: /api/account/alias
-----------------------
_**详细描述: 别名格式为1-20位小写字母和数字的组合，设置别名会销毁1个NULS**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "alias" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                      |     参数类型     | 参数描述     | 是否必填 |
| -------------------------------------------------------- |:------------:| -------- |:----:|
| 账户设置别名                                                   | setaliasform | 账户设置别名表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address  |    string    | 账户地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias    |    string    | 别名       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |    string    | 账户密码     |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述        |
| ----- |:------:| ----------- |
| value | string | 设置别名交易的hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

1.10 查询账户余额
===========
Cmd: /api/accountledger/balance/{address}
-----------------------------------------
_**详细描述: 根据资产链ID和资产ID，查询本链账户对应资产的余额与nonce值**_
### HttpMethod: POST

### Form json data: 
```json
{
  "assetChainId" : 0,
  "assetId" : 0
}
```

参数列表
----
| 参数名                                                          |    参数类型     | 参数描述   | 是否必填 |
| ------------------------------------------------------------ |:-----------:| ------ |:----:|
| balanceDto                                                   | balanceform | 账户余额表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |     int     | 资产的链ID |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |     int     | 资产ID   |  是   |

返回值
---
| 字段名           |  字段类型  | 参数描述                      |
| ------------- |:------:| ------------------------- |
| total         | string | 总余额                       |
| freeze        | string | 锁定金额                      |
| available     | string | 可用余额                      |
| timeLock      | string | 时间锁定金额                    |
| consensusLock | string |  共识锁定金额                   |
| nonce         | string | 账户资产nonce值                |
| nonceType     |  int   | 1：已确认的nonce值,0：未确认的nonce值 |
### Example request data: 

_**request path:**_
http://localhost:9898/api/accountledger/balance/tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG

_**request form data:**_
```json
{
  "assetChainId" : 2,
  "assetId" : 1
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "total" : "10000000000000",
    "freeze" : "0",
    "available" : "10000000000000",
    "timeLock" : "0",
    "consensusLock" : "0",
    "nonce" : "0000000000000000",
    "nonceType" : 1
  }
}
```

1.11 离线 - 批量创建账户
================
Cmd: /api/account/offline
-------------------------
_**详细描述: 创建的账户不会保存到钱包中,接口直接返回账户的keystore信息**_
### HttpMethod: POST

### Form json data: 
```json
{
  "count" : 0,
  "prefix" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                      |       参数类型        | 参数描述       | 是否必填 |
| -------------------------------------------------------- |:-----------------:| ---------- |:----:|
| 离线批量创建账户                                                 | accountcreateform | 离线批量创建账户表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;count    |        int        | 新建账户数量     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;prefix   |      string       | 地址前缀       |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |      string       | 账户密码       |  是   |

返回值
---
| 字段名                                                                 |      字段类型       | 参数描述         |
| ------------------------------------------------------------------- |:---------------:| ------------ |
| list                                                                | list&lt;object> | 账户keystore列表 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address             |     string      | 账户地址         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKey              |     string      | 公钥           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;prikey              |     string      | 明文私钥         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPrivateKey |     string      | 加密后的私钥       |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "count" : 1,
  "prefix" : "tNULS",
  "password" : "abcd1234"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : [ {
    "address" : "tNULSeBaMoS1x2VryPZGyaVSfbaqcLfhqhbXit",
    "pubKey" : "03a299ec3c3bbb3da290a10c1deafae08f1f630e5edab89cde65f4dc0c42537c42",
    "prikey" : "",
    "encryptedPrivateKey" : "56070f74ebbcbf0097d5ceca5fc075b76f5f59bd3851be02cab08d953330c327267a2406bc6173e3093520744219c491"
  } ]
}
```

1.12 离线获取账户明文私钥
===============
Cmd: /api/account/priKey/offline
--------------------------------
_**详细描述: 离线获取账户明文私钥**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "encryptedPriKey" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                             |     参数类型      | 参数描述         | 是否必填 |
| --------------------------------------------------------------- |:-------------:| ------------ |:----:|
| 离线获取账户明文私钥                                                      | getprikeyform | 离线获取账户明文私钥表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address         |    string     | 账户地址         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPriKey |    string     | 账户密文私钥       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password        |    string     | 账户密码         |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 明文私钥 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "encryptedPriKey" : "54793157409d0414248ef290eac96270c1a0115d712e845f0eb372bb977cbc0cafe39d598175473fa1bd5329dd1fae95",
  "password" : "abcd1234"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "priKey" : "c55c80b0afcbebea36bc2cc1f07a1946935fe578c0c8c35190180f99619d5f48"
  }
}
```

1.13 离线修改账户密码
=============
Cmd: /api/account/password/offline/
-----------------------------------
_**详细描述: 离线修改账户密码**_
### HttpMethod: PUT

### Form json data: 
```json
{
  "address" : null,
  "encryptedPriKey" : null,
  "oldPassword" : null,
  "newPassword" : null
}
```

参数列表
----
| 参数名                                                             |       参数类型        | 参数描述       | 是否必填 |
| --------------------------------------------------------------- |:-----------------:| ---------- |:----:|
| 离线修改账户密码                                                        | resetpasswordform | 离线修改账户密码表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address         |      string       | 账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPriKey |      string       | 账户密文私钥     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;oldPassword     |      string       | 账户原密码      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newPassword     |      string       | 账户新密码      |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述       |
| ----- |:------:| ---------- |
| value | string | 重置密码后的加密私钥 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

1.14 多账户摘要签名
============
Cmd: /api/account/multi/sign
----------------------------
_**详细描述: 用于签名离线组装的多账户转账交易，调用接口时，参数可以传地址和私钥，或者传地址和加密私钥和加密密码**_
### HttpMethod: POST

### Form json data: 
```json
{
  "dtoList" : [ {
    "address" : null,
    "priKey" : null,
    "encryptedPrivateKey" : null,
    "password" : null
  } ],
  "txHex" : null
}
```

参数列表
----
| 参数名                                                                                                                 |      参数类型       | 参数描述        | 是否必填 |
| ------------------------------------------------------------------------------------------------------------------- |:---------------:| ----------- |:----:|
| 多账户摘要签名                                                                                                             |  multisignform  | 多账户摘要签名表单   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;dtoList                                                             | list&lt;object> | keystore集合  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address             |     string      | 地址          |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;priKey              |     string      | 明文私钥        |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPrivateKey |     string      | 加密私钥        |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password            |     string      | 密码          |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHex                                                               |     string      | 交易序列化Hex字符串 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述          |
| ----- |:------:| ------------- |
| hash  | string | 交易hash        |
| txHex | string | 签名后的交易16进制字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "dtoList" : [ {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "priKey" : "c55c80b0afcbebea36bc2cc1f07a1946935fe578c0c8c35190180f99619d5f48",
    "encryptedPrivateKey" : null,
    "password" : null
  } ],
  "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f0000000000000000000000000000000000000000000000000000000000000000000000000000"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed",
    "hash" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

1.15 明文私钥摘要签名
=============
Cmd: /api/account/priKey/sign
-----------------------------
_**详细描述: 明文私钥摘要签名**_
### HttpMethod: POST

### Form json data: 
```json
{
  "txHex" : null,
  "address" : null,
  "priKey" : null
}
```

参数列表
----
| 参数名                                                     |      参数类型      | 参数描述        | 是否必填 |
| ------------------------------------------------------- |:--------------:| ----------- |:----:|
| 明文私钥摘要签名                                                | prikeysignform | 明文私钥摘要签名表单  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHex   |     string     | 交易序列化Hex字符串 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |     string     | 账户地址        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;priKey  |     string     | 账户明文私钥      |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述          |
| ----- |:------:| ------------- |
| hash  | string | 交易hash        |
| txHex | string | 签名后的交易16进制字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f0000000000000000000000000000000000000000000000000000000000000000000000000000",
  "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "priKey" : "c55c80b0afcbebea36bc2cc1f07a1946935fe578c0c8c35190180f99619d5f48"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed",
    "hash" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

1.16 密文私钥摘要签名
=============
Cmd: /api/account/encryptedPriKey/sign
--------------------------------------
_**详细描述: 密文私钥摘要签名**_
### HttpMethod: POST

### Form json data: 
```json
{
  "txHex" : null,
  "address" : null,
  "encryptedPriKey" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                             |          参数类型           | 参数描述        | 是否必填 |
| --------------------------------------------------------------- |:-----------------------:| ----------- |:----:|
| 密文私钥摘要签名                                                        | encryptedprikeysignform | 密文私钥摘要签名表单  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHex           |         string          | 交易序列化Hex字符串 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address         |         string          | 账户地址        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPriKey |         string          | 账户密文私钥      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password        |         string          | 账户密码        |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述          |
| ----- |:------:| ------------- |
| hash  | string | 交易hash        |
| txHex | string | 签名后的交易16进制字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f0000000000000000000000000000000000000000000000000000000000000000000000000000",
  "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "encryptedPriKey" : "54793157409d0414248ef290eac96270c1a0115d712e845f0eb372bb977cbc0cafe39d598175473fa1bd5329dd1fae95",
  "password" : "abcd1234"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed",
    "hash" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

1.17 创建多签账户
===========
Cmd: /api/account/multiSign/create
----------------------------------
_**详细描述: 根据多个账户的公钥创建多签账户，minSigns为多签账户创建交易时需要的最小签名数**_
### HttpMethod: POST

参数列表
----
| 参数名                                                      |            参数类型            | 参数描述     | 是否必填 |
| -------------------------------------------------------- |:--------------------------:| -------- |:----:|
| 创建多签账户                                                   | multisignaccountcreateform | 创建多签账户表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys  |            list            | 账户公钥集合   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns |            int             | 最小签名数    |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述  |
| ----- |:------:| ----- |
| value | string | 账户的地址 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

1.18 离线创建设置别名交易
===============
Cmd: /api/account/aliasTx/create
--------------------------------
_**详细描述: 根据多个账户的公钥创建多签账户，minSigns为多签账户创建交易时需要的最小签名数**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "alias" : null,
  "nonce" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                     |   参数类型   | 参数描述     | 是否必填 |
| ------------------------------------------------------- |:--------:| -------- |:----:|
| 创建多签账户                                                  | aliasdto | 创建多签账户表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |  string  | 账户地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias   |  string  | 别名       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce   |  string  | 资产nonce值 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark  |  string  | 交易备注     |  否   |

返回值
---
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

1.19 多签账户离线创建设置别名交易
===================
Cmd: /api/account/multiSign/aliasTx/create
------------------------------------------
_**详细描述: 多签账户离线创建设置别名交易**_
### HttpMethod: POST

参数列表
----
| 参数名                                                      |       参数类型        | 参数描述     | 是否必填 |
| -------------------------------------------------------- |:-----------------:| -------- |:----:|
| 多签账户离线创建设置别名交易                                           | multisignaliasdto | 创建别名交易表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address  |      string       | 账户地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias    |      string       | 别名       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce    |      string       | 资产nonce值 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark   |      string       | 交易备注     |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys  |  list&lt;string>  | 公钥集合     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns |        int        | 最小签名数    |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

2.1 根据区块高度查询区块头
===============
Cmd: /api/block/header/height/{height}
--------------------------------------
_**详细描述: 根据区块高度查询区块头**_
### HttpMethod: GET

参数列表
----
| 参数名    | 参数类型 | 参数描述 | 是否必填 |
| ------ |:----:| ---- |:----:|
| height | long | 区块高度 |  是   |

返回值
---
| 字段名                  |  字段类型  | 参数描述                 |
| -------------------- |:------:| -------------------- |
| hash                 | string | 区块的hash值             |
| preHash              | string | 上一个区块的hash值          |
| merkleHash           | string | 梅克尔hash              |
| time                 | string | 区块生成时间               |
| height               |  long  | 区块高度                 |
| txCount              |  int   | 区块打包交易数量             |
| blockSignature       | string | 签名Hex.encode(byte[]) |
| size                 |  int   | 大小                   |
| packingAddress       | string | 打包地址                 |
| roundIndex           |  long  | 共识轮次                 |
| consensusMemberCount |  int   | 参与共识成员数量             |
| roundStartTime       | string | 当前共识轮开始时间            |
| packingIndexOfRound  |  int   | 当前轮次打包出块的名次          |
| mainVersion          | short  | 主网当前生效的版本            |
| blockVersion         | short  | 区块的版本，可以理解为本地钱包的版本   |
| stateRoot            | string | 智能合约世界状态根            |
### Example request data: 

_**request path:**_
http://localhost:9898/api/block/header/height/1

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "hash" : "0b21cc1e77865f3e414e69ccb63d65c2bdedd98f2aa3d6e414d4791ee897190f",
    "preHash" : "d8880f913c984e4dece5cfb3f5f1d96d6ee923ffb0b47be0079fe84472ddda83",
    "merkleHash" : "bace93bafd0834437019ad402bbcdc274b6c29c806d72135adbed9e46c7a4450",
    "time" : "1970-01-19 10:14:32.032",
    "height" : 1,
    "txCount" : 1,
    "blockSignature" : "473045022100a6a41777c78a3faafb7735d3b28a8bdb2501601bb4953fbbdcd48e892415fb3f02204c72100178b85d9ae4486808d0fa404e63f54912eea27bfd931da558fc3b8599",
    "size" : 247,
    "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
    "roundIndex" : 156327293,
    "consensusMemberCount" : 1,
    "roundStartTime" : "1970-01-19 10:14:32.032",
    "packingIndexOfRound" : 1,
    "mainVersion" : 1,
    "blockVersion" : 1,
    "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
  }
}
```

2.2 根据区块hash查询区块头
=================
Cmd: /api/block/header/hash/{hash}
----------------------------------
_**详细描述: 根据区块hash查询区块头**_
### HttpMethod: GET

参数列表
----
| 参数名  |  参数类型  | 参数描述   | 是否必填 |
| ---- |:------:| ------ |:----:|
| hash | string | 区块hash |  是   |

返回值
---
| 字段名                  |  字段类型  | 参数描述                 |
| -------------------- |:------:| -------------------- |
| hash                 | string | 区块的hash值             |
| preHash              | string | 上一个区块的hash值          |
| merkleHash           | string | 梅克尔hash              |
| time                 | string | 区块生成时间               |
| height               |  long  | 区块高度                 |
| txCount              |  int   | 区块打包交易数量             |
| blockSignature       | string | 签名Hex.encode(byte[]) |
| size                 |  int   | 大小                   |
| packingAddress       | string | 打包地址                 |
| roundIndex           |  long  | 共识轮次                 |
| consensusMemberCount |  int   | 参与共识成员数量             |
| roundStartTime       | string | 当前共识轮开始时间            |
| packingIndexOfRound  |  int   | 当前轮次打包出块的名次          |
| mainVersion          | short  | 主网当前生效的版本            |
| blockVersion         | short  | 区块的版本，可以理解为本地钱包的版本   |
| stateRoot            | string | 智能合约世界状态根            |
### Example request data: 

_**request path:**_
http://localhost:9898/api/block/header/hash/0b21cc1e77865f3e414e69ccb63d65c2bdedd98f2aa3d6e414d4791ee897190f

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "hash" : "0b21cc1e77865f3e414e69ccb63d65c2bdedd98f2aa3d6e414d4791ee897190f",
    "preHash" : "d8880f913c984e4dece5cfb3f5f1d96d6ee923ffb0b47be0079fe84472ddda83",
    "merkleHash" : "bace93bafd0834437019ad402bbcdc274b6c29c806d72135adbed9e46c7a4450",
    "time" : "1970-01-19 10:14:32.032",
    "height" : 1,
    "txCount" : 1,
    "blockSignature" : "473045022100a6a41777c78a3faafb7735d3b28a8bdb2501601bb4953fbbdcd48e892415fb3f02204c72100178b85d9ae4486808d0fa404e63f54912eea27bfd931da558fc3b8599",
    "size" : 247,
    "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
    "roundIndex" : 156327293,
    "consensusMemberCount" : 1,
    "roundStartTime" : "1970-01-19 10:14:32.032",
    "packingIndexOfRound" : 1,
    "mainVersion" : 1,
    "blockVersion" : 1,
    "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
  }
}
```

2.3 查询最新区块头信息
=============
Cmd: /api/block/header/newest
-----------------------------
_**详细描述: 查询最新区块头信息**_
### HttpMethod: GET

参数列表
----
无参数

返回值
---
| 字段名                  |  字段类型  | 参数描述                 |
| -------------------- |:------:| -------------------- |
| hash                 | string | 区块的hash值             |
| preHash              | string | 上一个区块的hash值          |
| merkleHash           | string | 梅克尔hash              |
| time                 | string | 区块生成时间               |
| height               |  long  | 区块高度                 |
| txCount              |  int   | 区块打包交易数量             |
| blockSignature       | string | 签名Hex.encode(byte[]) |
| size                 |  int   | 大小                   |
| packingAddress       | string | 打包地址                 |
| roundIndex           |  long  | 共识轮次                 |
| consensusMemberCount |  int   | 参与共识成员数量             |
| roundStartTime       | string | 当前共识轮开始时间            |
| packingIndexOfRound  |  int   | 当前轮次打包出块的名次          |
| mainVersion          | short  | 主网当前生效的版本            |
| blockVersion         | short  | 区块的版本，可以理解为本地钱包的版本   |
| stateRoot            | string | 智能合约世界状态根            |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "hash" : "0b21cc1e77865f3e414e69ccb63d65c2bdedd98f2aa3d6e414d4791ee897190f",
    "preHash" : "d8880f913c984e4dece5cfb3f5f1d96d6ee923ffb0b47be0079fe84472ddda83",
    "merkleHash" : "bace93bafd0834437019ad402bbcdc274b6c29c806d72135adbed9e46c7a4450",
    "time" : "1970-01-19 10:14:32.032",
    "height" : 1,
    "txCount" : 1,
    "blockSignature" : "473045022100a6a41777c78a3faafb7735d3b28a8bdb2501601bb4953fbbdcd48e892415fb3f02204c72100178b85d9ae4486808d0fa404e63f54912eea27bfd931da558fc3b8599",
    "size" : 247,
    "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
    "roundIndex" : 156327293,
    "consensusMemberCount" : 1,
    "roundStartTime" : "1970-01-19 10:14:32.032",
    "packingIndexOfRound" : 1,
    "mainVersion" : 1,
    "blockVersion" : 1,
    "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
  }
}
```

2.4 查询最新区块
==========
Cmd: /api/block/newest
----------------------
_**详细描述: 包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用**_
### HttpMethod: GET

参数列表
----
无参数

返回值
---
| 字段名                                                                                                           |      字段类型       | 参数描述                                      |
| ------------------------------------------------------------------------------------------------------------- |:---------------:| ----------------------------------------- |
| header                                                                                                        |     object      | 区块头信息, 只返回对应的部分数据                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hash                                                          |     string      | 区块的hash值                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;preHash                                                       |     string      | 上一个区块的hash值                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;merkleHash                                                    |     string      | 梅克尔hash                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time                                                          |     string      | 区块生成时间                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;height                                                        |      long       | 区块高度                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txCount                                                       |       int       | 区块打包交易数量                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockSignature                                                |     string      | 签名Hex.encode(byte[])                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;size                                                          |       int       | 大小                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress                                                |     string      | 打包地址                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundIndex                                                    |      long       | 共识轮次                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;consensusMemberCount                                          |       int       | 参与共识成员数量                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundStartTime                                                |     string      | 当前共识轮开始时间                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingIndexOfRound                                           |       int       | 当前轮次打包出块的名次                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mainVersion                                                   |      short      | 主网当前生效的版本                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockVersion                                                  |      short      | 区块的版本，可以理解为本地钱包的版本                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;stateRoot                                                     |     string      | 智能合约世界状态根                                 |
| txs                                                                                                           | list&lt;object> | 交易列表                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hash                                                          |     string      | 交易的hash值                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type                                                          |       int       | 交易类型                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time                                                          |     string      | 交易时间                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight                                                   |      long       | 区块高度                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                        |     string      | 交易备注                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transactionSignature                                          |     string      | 交易签名                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txDataHex                                                     |     string      | 交易业务数据序列化字符串                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status                                                        |       int       | 交易状态 0:unConfirm(待确认), 1:confirm(已确认)     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;size                                                          |       int       | 交易大小                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inBlockIndex                                                  |       int       | 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;form                                                          | list&lt;object> | 输入                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |     string      | 账户地址                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |       int       | 资产发行链的id                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |       int       | 资产id                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        |     string      | 数量                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce         |     string      | 账户nonce值的Hex字符串，防止双花交易，取上一笔交易hash的最后8个字节  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;locked        |      byte       | 0普通交易，-1解锁金额交易（退出共识，退出委托）                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                            | list&lt;object> | 输出                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |     string      | 账户地址                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |       int       | 资产发行链的id                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |       int       | 资产id                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        |     string      | 数量                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime      |      long       | 解锁时间，-1为永久锁定                              |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "header" : {
      "hash" : "92285f81a649a7c65b1fe9e52738bb95c4aac6a7f4ab4b0b971c09662a9433ad",
      "preHash" : "c9d0d84c47455e8dc0ccc328133c1e2bbb31d74b9f6ac99c14cc4f2d7663d4cc",
      "merkleHash" : "646a2bea27384ca31c45acd9980c7adec2ba8cfa95477c74cbca93db9f966caa",
      "time" : "1970-01-19 10:14:33.033",
      "height" : 9,
      "txCount" : 2,
      "blockSignature" : "463044022024e463c5dcb039f40e3ff2f733c294f5e705e38aa4caebbea6c14a100f39dbe30220222c673b226fc6c6c9cb535ff4440728ecf00968114798be40499e16b12b1709",
      "size" : 234,
      "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
      "roundIndex" : 156327301,
      "consensusMemberCount" : 1,
      "roundStartTime" : "1970-01-19 10:14:33.033",
      "packingIndexOfRound" : 1,
      "mainVersion" : 1,
      "blockVersion" : 1,
      "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
    },
    "txs" : [ {
      "hash" : "c418229126d1c2246828f99752bbffcb5d5a6fef552d64275482f80f79690fe6",
      "type" : 1,
      "time" : "2019-07-16 18:30:11.011",
      "blockHeight" : 9,
      "remark" : null,
      "transactionSignature" : null,
      "status" : 0,
      "size" : 80,
      "inBlockIndex" : 0,
      "form" : [ ],
      "to" : [ {
        "address" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000",
        "lockTime" : 0
      } ]
    }, {
      "hash" : "247a026d48f6be0c358423898e38a50ac0c2c1a851419b1ec843a667bab90df9",
      "type" : 2,
      "time" : "2019-07-16 18:30:03.003",
      "blockHeight" : 9,
      "remark" : "remark",
      "transactionSignature" : "2103958b790c331954ed367d37bac901de5c2f06ac8368b37d7bd6cd5ae143c1d7e34630440220084da59fca5edc6ed047c1360bb45d3e7ec297c367b8c2810421b2a43d1eabba02201f9e499fe63ad2dbbd83c1dafcb8437f5aba1c61fd0e5c9075a80b50820ca3ac",
      "status" : 0,
      "size" : 261,
      "inBlockIndex" : 0,
      "form" : [ {
        "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000100000",
        "nonce" : "0000000000000000",
        "locked" : 0
      } ],
      "to" : [ {
        "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000000000",
        "lockTime" : 0
      } ]
    } ]
  }
}
```

2.5 根据区块高度查询区块
==============
Cmd: /api/block/height/{height}
-------------------------------
_**详细描述: 包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用**_
### HttpMethod: GET

参数列表
----
| 参数名    | 参数类型 | 参数描述 | 是否必填 |
| ------ |:----:| ---- |:----:|
| height | long | 区块高度 |  是   |

返回值
---
| 字段名                  |  字段类型  | 参数描述                 |
| -------------------- |:------:| -------------------- |
| hash                 | string | 区块的hash值             |
| preHash              | string | 上一个区块的hash值          |
| merkleHash           | string | 梅克尔hash              |
| time                 | string | 区块生成时间               |
| height               |  long  | 区块高度                 |
| txCount              |  int   | 区块打包交易数量             |
| blockSignature       | string | 签名Hex.encode(byte[]) |
| size                 |  int   | 大小                   |
| packingAddress       | string | 打包地址                 |
| roundIndex           |  long  | 共识轮次                 |
| consensusMemberCount |  int   | 参与共识成员数量             |
| roundStartTime       | string | 当前共识轮开始时间            |
| packingIndexOfRound  |  int   | 当前轮次打包出块的名次          |
| mainVersion          | short  | 主网当前生效的版本            |
| blockVersion         | short  | 区块的版本，可以理解为本地钱包的版本   |
| stateRoot            | string | 智能合约世界状态根            |
### Example request data: 

_**request path:**_
http://localhost:9898//api/block/height/9

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "header" : {
      "hash" : "92285f81a649a7c65b1fe9e52738bb95c4aac6a7f4ab4b0b971c09662a9433ad",
      "preHash" : "c9d0d84c47455e8dc0ccc328133c1e2bbb31d74b9f6ac99c14cc4f2d7663d4cc",
      "merkleHash" : "646a2bea27384ca31c45acd9980c7adec2ba8cfa95477c74cbca93db9f966caa",
      "time" : "1970-01-19 10:14:33.033",
      "height" : 9,
      "txCount" : 2,
      "blockSignature" : "463044022024e463c5dcb039f40e3ff2f733c294f5e705e38aa4caebbea6c14a100f39dbe30220222c673b226fc6c6c9cb535ff4440728ecf00968114798be40499e16b12b1709",
      "size" : 234,
      "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
      "roundIndex" : 156327301,
      "consensusMemberCount" : 1,
      "roundStartTime" : "1970-01-19 10:14:33.033",
      "packingIndexOfRound" : 1,
      "mainVersion" : 1,
      "blockVersion" : 1,
      "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
    },
    "txs" : [ {
      "hash" : "c418229126d1c2246828f99752bbffcb5d5a6fef552d64275482f80f79690fe6",
      "type" : 1,
      "time" : "2019-07-16 18:30:11.011",
      "blockHeight" : 9,
      "remark" : null,
      "transactionSignature" : null,
      "status" : 0,
      "size" : 80,
      "inBlockIndex" : 0,
      "form" : [ ],
      "to" : [ {
        "address" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000",
        "lockTime" : 0
      } ]
    }, {
      "hash" : "247a026d48f6be0c358423898e38a50ac0c2c1a851419b1ec843a667bab90df9",
      "type" : 2,
      "time" : "2019-07-16 18:30:03.003",
      "blockHeight" : 9,
      "remark" : "remark",
      "transactionSignature" : "2103958b790c331954ed367d37bac901de5c2f06ac8368b37d7bd6cd5ae143c1d7e34630440220084da59fca5edc6ed047c1360bb45d3e7ec297c367b8c2810421b2a43d1eabba02201f9e499fe63ad2dbbd83c1dafcb8437f5aba1c61fd0e5c9075a80b50820ca3ac",
      "status" : 0,
      "size" : 261,
      "inBlockIndex" : 0,
      "form" : [ {
        "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000100000",
        "nonce" : "0000000000000000",
        "locked" : 0
      } ],
      "to" : [ {
        "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000000000",
        "lockTime" : 0
      } ]
    } ]
  }
}
```

2.6 根据区块hash查询区块
================
Cmd: /api/block/hash/{hash}
---------------------------
_**详细描述: 包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用**_
### HttpMethod: GET

参数列表
----
| 参数名  |  参数类型  | 参数描述   | 是否必填 |
| ---- |:------:| ------ |:----:|
| hash | string | 区块hash |  是   |

返回值
---
| 字段名                  |  字段类型  | 参数描述                 |
| -------------------- |:------:| -------------------- |
| hash                 | string | 区块的hash值             |
| preHash              | string | 上一个区块的hash值          |
| merkleHash           | string | 梅克尔hash              |
| time                 | string | 区块生成时间               |
| height               |  long  | 区块高度                 |
| txCount              |  int   | 区块打包交易数量             |
| blockSignature       | string | 签名Hex.encode(byte[]) |
| size                 |  int   | 大小                   |
| packingAddress       | string | 打包地址                 |
| roundIndex           |  long  | 共识轮次                 |
| consensusMemberCount |  int   | 参与共识成员数量             |
| roundStartTime       | string | 当前共识轮开始时间            |
| packingIndexOfRound  |  int   | 当前轮次打包出块的名次          |
| mainVersion          | short  | 主网当前生效的版本            |
| blockVersion         | short  | 区块的版本，可以理解为本地钱包的版本   |
| stateRoot            | string | 智能合约世界状态根            |
### Example request data: 

_**request path:**_
http://localhost:9898//api/block/hash/92285f81a649a7c65b1fe9e52738bb95c4aac6a7f4ab4b0b971c09662a9433ad

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "header" : {
      "hash" : "92285f81a649a7c65b1fe9e52738bb95c4aac6a7f4ab4b0b971c09662a9433ad",
      "preHash" : "c9d0d84c47455e8dc0ccc328133c1e2bbb31d74b9f6ac99c14cc4f2d7663d4cc",
      "merkleHash" : "646a2bea27384ca31c45acd9980c7adec2ba8cfa95477c74cbca93db9f966caa",
      "time" : "1970-01-19 10:14:33.033",
      "height" : 9,
      "txCount" : 2,
      "blockSignature" : "463044022024e463c5dcb039f40e3ff2f733c294f5e705e38aa4caebbea6c14a100f39dbe30220222c673b226fc6c6c9cb535ff4440728ecf00968114798be40499e16b12b1709",
      "size" : 234,
      "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
      "roundIndex" : 156327301,
      "consensusMemberCount" : 1,
      "roundStartTime" : "1970-01-19 10:14:33.033",
      "packingIndexOfRound" : 1,
      "mainVersion" : 1,
      "blockVersion" : 1,
      "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
    },
    "txs" : [ {
      "hash" : "c418229126d1c2246828f99752bbffcb5d5a6fef552d64275482f80f79690fe6",
      "type" : 1,
      "time" : "2019-07-16 18:30:11.011",
      "blockHeight" : 9,
      "remark" : null,
      "transactionSignature" : null,
      "status" : 0,
      "size" : 80,
      "inBlockIndex" : 0,
      "form" : [ ],
      "to" : [ {
        "address" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000",
        "lockTime" : 0
      } ]
    }, {
      "hash" : "247a026d48f6be0c358423898e38a50ac0c2c1a851419b1ec843a667bab90df9",
      "type" : 2,
      "time" : "2019-07-16 18:30:03.003",
      "blockHeight" : 9,
      "remark" : "remark",
      "transactionSignature" : "2103958b790c331954ed367d37bac901de5c2f06ac8368b37d7bd6cd5ae143c1d7e34630440220084da59fca5edc6ed047c1360bb45d3e7ec297c367b8c2810421b2a43d1eabba02201f9e499fe63ad2dbbd83c1dafcb8437f5aba1c61fd0e5c9075a80b50820ca3ac",
      "status" : 0,
      "size" : 261,
      "inBlockIndex" : 0,
      "form" : [ {
        "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000100000",
        "nonce" : "0000000000000000",
        "locked" : 0
      } ],
      "to" : [ {
        "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
        "assetsChainId" : 2,
        "assetsId" : 1,
        "amount" : "100000000000",
        "lockTime" : 0
      } ]
    } ]
  }
}
```

3.1 根据hash获取交易
==============
Cmd: /api/tx/{hash}
-------------------
_**详细描述: 根据hash获取交易**_
### HttpMethod: GET

参数列表
----
| 参数名  |  参数类型  | 参数描述   | 是否必填 |
| ---- |:------:| ------ |:----:|
| hash | string | 交易hash |  是   |

返回值
---
| 字段名                                                           |      字段类型       | 参数描述                                      |
| ------------------------------------------------------------- |:---------------:| ----------------------------------------- |
| hash                                                          |     string      | 交易的hash值                                  |
| type                                                          |       int       | 交易类型                                      |
| time                                                          |     string      | 交易时间                                      |
| blockHeight                                                   |      long       | 区块高度                                      |
| remark                                                        |     string      | 交易备注                                      |
| transactionSignature                                          |     string      | 交易签名                                      |
| txDataHex                                                     |     string      | 交易业务数据序列化字符串                              |
| status                                                        |       int       | 交易状态 0:unConfirm(待确认), 1:confirm(已确认)     |
| size                                                          |       int       | 交易大小                                      |
| inBlockIndex                                                  |       int       | 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序 |
| form                                                          | list&lt;object> | 输入                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |     string      | 账户地址                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |       int       | 资产发行链的id                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |       int       | 资产id                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        |     string      | 数量                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce         |     string      | 账户nonce值的Hex字符串，防止双花交易，取上一笔交易hash的最后8个字节  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;locked        |      byte       | 0普通交易，-1解锁金额交易（退出共识，退出委托）                 |
| to                                                            | list&lt;object> | 输出                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |     string      | 账户地址                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |       int       | 资产发行链的id                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |       int       | 资产id                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        |     string      | 数量                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime      |      long       | 解锁时间，-1为永久锁定                              |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

3.2 验证交易
========
Cmd: /api/accountledger/transaction/validate
--------------------------------------------
_**详细描述: 验证离线组装的交易,验证成功返回交易hash值,失败返回错误提示信息**_
### HttpMethod: POST

### Form json data: 
```json
{
  "txHex" : null
}
```

参数列表
----
| 参数名                                                   |  参数类型  | 参数描述         | 是否必填 |
| ----------------------------------------------------- |:------:| ------------ |:----:|
| 验证交易是否正确                                              | txform | 验证交易是否正确表单   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHex | string | 交易序列化16进制字符串 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

3.3 广播交易
========
Cmd: /api/accountledger/transaction/broadcast
---------------------------------------------
_**详细描述: 广播离线组装的交易,成功返回true,失败返回错误提示信息**_
### HttpMethod: POST

### Form json data: 
```json
{
  "txHex" : null
}
```

参数列表
----
| 参数名                                                   |  参数类型  | 参数描述         | 是否必填 |
| ----------------------------------------------------- |:------:| ------------ |:----:|
| 广播交易                                                  | txform | 广播交易表单       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHex | string | 交易序列化16进制字符串 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否成功   |
| hash  | string  | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : true,
    "hash" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

3.4 单笔转账
========
Cmd: /api/accountledger/transfer
--------------------------------
_**详细描述: 发起单账户单资产的转账交易**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "toAddress" : null,
  "password" : null,
  "amount" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                       |     参数类型     | 参数描述   | 是否必填 |
| --------------------------------------------------------- |:------------:| ------ |:----:|
| 单笔转账                                                      | transferform | 单笔转账表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address   |    string    | 账户地址   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toAddress |    string    | 账户地址   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password  |    string    | 账户密码   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount    |  biginteger  | 金额     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark    |    string    | 备注     |  否   |

返回值
---
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "toAddress" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "password" : "nuls123456",
  "amount" : 10000000000,
  "remark" : "remark"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "89368623898cde34fe81b5ede6fe5bed75ffb61021ec6caa01a9a5dcd9262d69"
  }
}
```

3.5 离线组装转账交易
============
Cmd: /api/accountledger/createTransferTxOffline
-----------------------------------------------
_**详细描述: 根据inputs和outputs离线组装转账交易，用于单账户或多账户的转账交易。交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和**_
### HttpMethod: POST

### Form json data: 
```json
{
  "inputs" : [ {
    "address" : null,
    "assetChainId" : 0,
    "assetId" : 0,
    "amount" : null,
    "nonce" : null
  } ],
  "outputs" : [ {
    "address" : null,
    "assetChainId" : 0,
    "assetId" : 0,
    "amount" : null,
    "lockTime" : 0
  } ],
  "remark" : null
}
```

参数列表
----
| 参数名                                                                                                          |      参数类型       | 参数描述     | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:---------------:| -------- |:----:|
| transferDto                                                                                                  |   transferdto   | 转账交易表单   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inputs                                                       | list&lt;object> | 转账交易输入列表 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |     string      | 账户地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 资产的链id   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |       int       | 资产id     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |   biginteger    | 资产金额     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |     string      | 资产nonce值 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                                      | list&lt;object> | 转账交易输出列表 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |     string      | 账户地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 资产的链id   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |       int       | 资产id     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |   biginteger    | 资产金额     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime     |      long       | 锁定时间     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                       |     string      | 交易备注     |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "inputs" : [ {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : 1100000,
    "nonce" : "0000000000000000"
  } ],
  "outputs" : [ {
    "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "1000000",
    "lockTime" : 0
  } ],
  "remark" : null
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f0000000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

3.6 计算离线创建转账交易所需手续费
===================
Cmd: /api/accountledger/calcTransferTxFee
-----------------------------------------
_**详细描述: 计算离线创建转账交易所需手续费**_
### HttpMethod: POST

### Form json data: 
```json
{
  "addressCount" : 0,
  "fromLength" : 0,
  "toLength" : 0,
  "remark" : null,
  "price" : null
}
```

参数列表
----
| 参数名                                                          |       参数类型       | 参数描述    | 是否必填 |
| ------------------------------------------------------------ |:----------------:| ------- |:----:|
| TransferTxFeeDto                                             | transfertxfeedto | 转账交易手续费 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addressCount |       int        | 转账地址数量  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromLength   |       int        | 转账输入长度  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toLength     |       int        | 转账输出长度  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark       |      string      | 交易备注    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price        |    biginteger    | 手续费单价   |  否   |

返回值
---
| 字段名   |  字段类型  | 参数描述  |
| ----- |:------:| ----- |
| value | string | 交易手续费 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "addressCount" : 6,
  "fromLength" : 6,
  "toLength" : 2,
  "remark" : "remark",
  "price" : "100000"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : 200000
  }
}
```

3.7 多签账户离线组装转账交易
================
Cmd: /api/accountledger/createMultiSignTransferTxOffline
--------------------------------------------------------
_**详细描述: 根据inputs和outputs离线组装转账交易，用于单账户或多账户的转账交易。交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和**_
### HttpMethod: POST

参数列表
----
| 参数名                                                                                                          |         参数类型         | 参数描述       | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:--------------------:| ---------- |:----:|
| transferDto                                                                                                  | multisigntransferdto | 多签账户转账交易表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys                                                      |   list&lt;string>    | 公钥集合       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns                                                     |         int          | 最小签名数      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inputs                                                       |   list&lt;object>    | 转账交易输入列表   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |        string        | 账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |         int          | 资产的链id     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |         int          | 资产id       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |      biginteger      | 资产金额       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |        string        | 资产nonce值   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                                      |   list&lt;object>    | 转账交易输出列表   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |        string        | 账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |         int          | 资产的链id     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |         int          | 资产id       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |      biginteger      | 资产金额       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime     |         long         | 锁定时间       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                       |        string        | 交易备注       |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

3.8 计算离线创建多签账户转账交易所需手续费
=======================
Cmd: /api/accountledger/calcMultiSignTransferTxFee
--------------------------------------------------
_**详细描述: 计算离线创建多签账户转账交易所需手续费**_
### HttpMethod: POST

### Form json data: 
```json
{
  "pubKeyCount" : 0,
  "fromLength" : 0,
  "toLength" : 0,
  "remark" : null,
  "price" : null
}
```

参数列表
----
| 参数名                                                         |           参数类型            | 参数描述          | 是否必填 |
| ----------------------------------------------------------- |:-------------------------:| ------------- |:----:|
| MultiSignTransferTxFeeDto                                   | multisigntransfertxfeedto | 多签账户转账交易手续费表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeyCount |            int            | 多签地址对应公钥数量    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromLength  |            int            | 转账输入长度        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toLength    |            int            | 转账输出长度        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark      |          string           | 交易备注          |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price       |        biginteger         | 手续费单价         |  否   |

返回值
---
| 字段名   |  字段类型  | 参数描述  |
| ----- |:------:| ----- |
| value | string | 交易手续费 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

4.1 发布合约
========
Cmd: /api/contract/create
-------------------------
_**详细描述: 发布合约**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "gasLimit" : 0,
  "price" : 0,
  "password" : null,
  "remark" : null,
  "contractCode" : null,
  "alias" : null,
  "args" : null
}
```

参数列表
----
| 参数名                                                          |      参数类型      | 参数描述                 | 是否必填 |
| ------------------------------------------------------------ |:--------------:| -------------------- |:----:|
| 发布合约                                                         | contractcreate | 发布合约表单               |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender       |     string     | 交易创建者                |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit     |      long      | 最大gas消耗              |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price        |      long      | 执行合约单价               |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password     |     string     | 交易创建者账户密码            |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark       |     string     | 备注                   |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractCode |     string     | 智能合约代码(字节码的Hex编码字符串) |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias        |     string     | 合约别名                 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args         |    object[]    | 参数列表                 |  否   |

返回值
---
| 字段名             |  字段类型  | 参数描述        |
| --------------- |:------:| ----------- |
| txHash          | string | 发布合约的交易hash |
| contractAddress | string | 生成的合约地址     |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "gasLimit" : 20000,
  "price" : 25,
  "password" : "nuls123456",
  "remark" : "restful-nrc20-remark",
  "contractCode" : "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbacf782dd58a888e49983a72c8e83821c367b8a1c382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe274703aa254cbe6691d5fe0bc8ea36087270e4f9a11551d916cb9588a8c999109f6312f864869d2c845f2850819dfc8f078e4f67e394e289db32c389413c98fdb36fac5c52f79e32c7cafb216aae57260ecb899a2f6b535fb6bb7f3a261cd7c5153772c0b2e706ce8d001be1c1edacf712a7f527a6bb82e6f86b2e54cc9529f59fdf55ab0ded9d69a78f749eb34149d5b29c22b7f812bcc3f55a6735bbdf7be1d916db7cef4dd3aa5a1bda68123ac4e524ec0cc5a54df5587bd544dd93d3f6172df861a306a1a0d16ec0f025ae83d26e71b53f9ec9451607ee761337e04f9c9868c7b85097943f71eabc51de8e637750c3ec49140039a842af8e3a049d8423d49187cfa8433d493c400d2b492b7f2ef2ecffb003fd699616ca3bc9d3349be697c2e8a5e8676416df90ec70635b9163b38eaf6067c173bf91c745677f1a911422d45beb98ad62fa8682376d750e4ff688ea2be05156dc6f76a280acc45945c50d110f6388afecd1d8bf834a3335476090d571088de40f0e80c4257d05815c3d1d81568d1f815f8a2cd812bf0471397b0b8397015fa552c915397d1780d4b05d0752cf3e3be1b587e54736666b0e21a9a2eba40fbb158fd060c30658bb11a2d6865296cc606a639c6246f645a0799d6434c729a491687b6d8401d8744ea57711049cac5a7a4ef739f5f491b95febb29b705b86d58797eeb4892e50189c51754212692441c8e5dc74a1fbec4aafd89af1064602e24e2ce54f340a02df015429c1449c21f505e75f2f72a887529313611650bc3bd8ec9bf8d798b33e4552f92ae1749ecc53e276dfb29f978ba1b07e8bf9f6797e0204f04f00327a1f6da56ae1de2cc52f86ea22588919b680ee2f0ac248bdf4b7c737c3d825127ef670823c0674f34c6142566d0128d715c1d8d736c95d4c54492ac05aa85daa2c0df8b46825e46b0cd84bc8e80c4b1a8adcf75ac07f7398ef5e087ca31910eaaf408b895fe9ae9e0bde5a4e3491e914cf6c4fe8960e01c02fe6b68932aaba4a6795fdc4ecdbeb8e4c0ef817884d11a453b2174d2f83aea1488115b9f07a21dfb7684713f81f9545417c31fdaa9dd446bad90063517e98ff0632798166d8ae6ae2863199728c6e24e143921f29c3836a9583d80101e64120d92d1980760970bb04b454e53d24105b0665a7fe22279d041d21e750cb7df404705c49ad92096aa9d134ce624bbcff200687701b4ab9ad494340f80075c00136c0151d15d01b0b602404d5c45671d1c59e2c8d1cdbc0747b78ba3dbc5d14d1febe2305c1c978943623c142586ae8aedfdf1c45544ce62859a60e53040e7a057b0b6cc83d5efe9f022a35c22ee32ab6d9a55f4302faf47d8e7a73d8d30e4621f72b10f31cd5b5487df89946a04a9b746e9e21e165ba896533ef914a42c4e3de1907584f545740381685b40bc4bc8108bb705e85d375daaf2acdd0d8fb2d01ea3d5c7b1923abad85955268ab83823187770469846bb61234ec3865821f72826f2c907af83e78c83a7d38327d1b46e06eb2b687a66a1b1cbfe296a7b1a2bf00ccbfe590f924e1749a78ba4d345d2e922697191582e927f7045740cd8b1f0f2b74dd66dce453610508b6dce1dd51688276c49507a59fc39ea7b5efe958836bc40067991fcfc126fa2973d88075cc4032ee20117f1808378156fb5e3e4f32a8bdb335b3923f90f816493248bf3147f42381e9de6aa448ba1bc8a6fed8fc69b36cce0b68a7b8db10423de5d85ee4df72b64ac577975bcc680bf4ee86f78ca72bd0b793d5b6e9ff30563b776031372bf82ec931f340e90a203a4cb01928837f54a862b207ae682b0b3fc16d59d65cdbdcd78bee301e025b7ec1c726b60ac2b00f8b3c701708c6f12cfd5bc075648d3b285dfacde0817dde60cab6def320aef29832df631d7e06a654653415f045fd34e4dd979c8b133e5dc371dd19804597843cabab62ddbc973d4f33eed7e407afcd0435c1daecd0e14944d918aee1543eb2d8ef59263dd74acb7d6f0327e0d7d73ddfc98ee7ce231d9ea9a6c75dc14a9acbe8be6383ced96984f79b1a68ec38959666d8f2f52d12542f894fe5cf6a4758d6b7e8debf11ac7639104887fb6ef2755c53c8c5f2ae53e2c278b3e4b36ece0f35534fe17504b0708ec308779cb09000028180000504b0304140008080800aa7b564e00000000000000000000000022000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373a552d14e1341143d03b5cb16aa28a8a0284a08697970131ff481a76a24694282a19507dfa6bb431d989da933b325fc9a0f7e801f65bc534ba5b6a5896eb23b3be79e7bcebd33f7c7cf6fdf01bcc64e84e711b623bc60a8367a3d6bfa5c7de80bed19569a5a0bfb5e71e7848bf092186dcbb53b1376c828699e0b86f55afde89cf779a2b8ee262d6fa5ee1e306c9e14dacb5c9c4a273b4a34b4369e7b69b463d83b9226d18572496ab4b73cf589cb2e123ee224a7525c9248d95de51da318963291ca9c2b4a5eacd59b0ccb9ea8aa55f47aea8a61e3ba869cfb2fc93bd96d6a2fbac29244dce154582a8ecf18ded4a61b37b2cc0ae70e668aec8c37f3915b6add0b3bd6d5fedcae4ec4d7425a9191e2921f1e26c3db39554d2faafe99eee85ae4d09a9ce1d33ca17fb489f86034e8ae63ae94b90cc7c9d0fe2fb799475d6999c2a6e2502af2a8b4cd85d0af0295616b42d0876832e030acfe19c2e3ceb9486942f76fcbd8fd6be06f278fcd7e99816101e1894b749788695fa15d99d66580b0952958157727b07b589dc0eee301a9dfc0b0466f144cd70784877844ff01ac2efc0647a1c7d81886d6166f864684cdd9b94ff0748aef388d085b83ef33dca135141a7c4ad46e488a10ff02504b070868fe421cca0100005e040000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c6173738d565b6f1b4514fec6bbde75b69bbb49d224b46929e05b6a2ee19a4b73218140d2943835b4406163af926d9cdd74771d90a0129540ea033c801020242e2f252f790089b80824c45390f82ffc03049c995dbb89e3243cf8ccd93367e67cdf7c6746fef39f5f7e03f018ae6988e34213148c7333c1cd248f4da998d63083e735bcc0cd0c66b937abe2450d2770218697f838c7cd7c0c176358e0ee256e5e56b1a822c71075deb64d97e1d49ce564ed72c9cb161cdb778d829ff58a6bd98962d1353d6f9841f5364cbbc853a39b46a96c3274cd5d37368decbae1af6627ad9559db37574c97529511cbb6fc3186cb89a3773d66b6e1f6c93c833ce5140940eb9c659b17cbebcba6bb642c9728d231e7148c52de702dfe1d06657fd5f2189a2736365c87b04f6f9ab6cfa0cfdac47caa64789e49d39983607c67cdb4b34bdc9edbb798389e5d2cdbbeb56ee62dcfa22a970cd758377dd39db06dc7377ccbb169cf546382462d27bb68de285bae59a41d632ba6bf10883190481e2747ccab659f39e694f98969b479ae2aa0e6edf9e065f381a03d54f6104979b930eb64e2706114a2639488f97d6152c9b057b20bcbd7cd823f9cbccac01c2ed28129219241a234e77ca3b0366f6c08eda8c1a9f4aae1ad06824b89e42c152156e5122533fa88f94ece772d7b85215e852f760ea2b4b39673ca6ec19cb1782f6842cdf33c4d471f967474a15b470f3727d1abe332f22a5e21e9fe7f37a87855c7237854c5151d4fe16986ee7a189365ab244e3bbe6fe5bbe2f28deab88ad7b8799d006606c28b364ae43203e2ae898c37740c6384a1adfef4187a1bea1ff6391d5a3e9059f43a43e7be7312413aa6fea308379aded362c4ab514b9054c60627c330983828cd41b5c2631aaecbaff6cf11f9fd89a9c3a771869ecc38a82b2121c215a78735c2451723e94ea346f37de8a7effbe9ab44a34c633c75172c95de412495d981941adc81fca358758a6c17a2646f50a68b267868818f4e94719aa2a9603d06f000203c5e97098f578e088fd796708efc4e89261f2467bf7d080f93e590b234f2e5d1d44f88fc50c3a088e03ba2a61e248435191248868bc7289b575404977b0c34117d8fd6dc143b74055935d44a889a2e444320523d90f71b0249370622d503f980d67c7808909e104806830d80c8f5406e3704725e2c3a0044ae07f211adf9f810205c335e98ae7cb8d75f9423d1782b95fe1e51793bbd8b965405d134fdee202a6da77f8732cf75dba58ea22143bf0ad4afd0ba45375ec4298b9f679020514cda9b205513e45db4f381627205b12db472ef0fa8f21664699b404882462f01053e818a4fe9e43e23989f13e52ff026be14b40602c0355ab7e89fc6e3824e1143148be009d1a0badac2fe46bf8a2715d6adb0b836cea9d32b1752ff393cc6e9805d734a306be26046da4ef7ad069c78580ac36f55e37210271a5a10efbb07bf43dc9eafe9367d43cdf32da6f0dd1e35a643d89d04fa193c4b5038d84128ede3ec5fbae511824b30c947e84718cbefffa445f49c862caed1b6bc51867ec5892b77a1773457484141a5b5a32df025eeb7073e616e6dbb594147059df58d77674fe30d8540231815760c67c3a746c173f42c35fd07504b0708ea7bbc798f040000e6090000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c6173738d56df531b5514fe6e76b31bb60be19740015b5aab8624b06ad156c36f84160da5051a0bd5da25d9c296b08bbb1b5e1c1ffa6ff8aebcf0a033923a3ae3f88433fe2ffe078e7aeedd854212c0879c7bf6dc73eef9cef9cebd933ffff9e53700efe189864e4c3441c12417535c4c73db8c8a590d73b8a3e12e177398e7dabc8a4f345cc244029ff235cfc54202f71258e4ea7d2e1ea85852b1cc203ff3dc6d862b79db359c4ad9378aae1378663130fcd29631552a7996efe7186281cb10df35cb158ba12bffdcdc358d6d33d834a6ed8d7927b0362c8fbc9451dbb183718687a9f30fbc60b7e1f18305823be3960840326f3bd6bdcaf6bae5ad98eb65b2b4e7dda2592e989ecdbf23a31c6cda3e43f38a673afe33cb9bddb59c80419f771ccb9b299bbe6fd176b61e4ce06e598eb1c2e58d53c154e3f5a58a13d8db56c1f66dca72dff4cc6d2bb0bc29c7710333b05d87ce4c372ed03cf63196acaf2ab66795e84475c30ae6040d03a9c18b8850fd23e76b17f498f72b4e47af70e6fc704dd0772124b187729d4163c23ff6ba9c3a9b0c854a30cb54ed6b9153d974368cc5f5e75631c80dae3130971353b72588318988e6e5c02c6e2d983b822f1a674abd69fa9b21c9526a709e92502d95323933fa4804ee72e0d9ce0643e7117c717268a593b565b7e215ad399bf3af090687b99b8e3eace8e842b78e1e2e2ea357c74314547c4674fdff0950f148c73b7857c5aa8e5bb8cdd05d0b63ba62974b96c7d0712af26b7ed5c674ace131179f132bd981c01da3b2b203e26689cd2f74e430cad05adb3786de867c47534ded2a84048bc9a6eca73a248cd4a0fef34a6db47d62a4a8eb8d8681483277762ca7c43094aa27a59ea7a841b91affa3c939c7bf3f3573f636aed1d3d8099a47488871aee9018d71bac54a8cd3aad17e1ffae9fb75fa2ad32ad3da997e0996ce1c2096ce1e404a0f1d40fe51445d21d98538c92c790ea109c36881810e1a82ab644d87f118c01b80d0785e26349e3926349e5bc20dd293120986374fc8b7f036490ec7a09587c6d33f21f6c3717e45186f8a7c7ae810e5634861300a1e276f9e4d1175bc42af09eb2d8ab92d4ee80abd8e112b1162ba060d8148b540720d81641a03916a814c50cce419407a2220596a743d10b916c84c4320c322a80e885c0be40ec5dc3d0308e78b27a68b1e9df517f970ea5ea433df232eef670ed192ae229ea1df77884bfb99dfa12c70de0e699a68c9d2af0af55b24f7a065859dbc783f4307896cd24907e9c8413e441b5fc8265791d843926b7f4095f7204bfb04421265f41250200f150bd4b94582f9804a5ec2975816650d84808fcb7a41ff266e8a724a18215b0cef8be1d4d516f637fa557ca0b06e85756a93bc747adba2d27f8eda381b56d79c1695357130a3ad57fb36c39ab8598acc4f8fec7268a732b4d0def70a7ebbb8398fe826add2f0ac119b8f4fb0311bc1ee20d01fe22382c2c10e41699b64ffd20d8f115c82493a223dc658e1f42705d1531a55f1848ee58332f22b2eadbe84dede5c25064529c9f6d65097b8de16ea8439d9fa4d15ed5574d40eded313833712018d614cc8715c8f9e19051fd393d4f41f504b0708826261e37e040000ca090000504b01020a000a0000080000aa7b564e000000000000000000000000030004000000000000000000000000000000696f2ffeca0000504b01020a000a0000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e00000000000000000000000011000000000000000000000000004b000000696f2f6e756c732f636f6e74726163742f504b01020a000a0000080000aa7b564e00000000000000000000000017000000000000000000000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000",
  "alias" : "restful_nrc20",
  "args" : [ "io", "IO", 80000, 1 ]
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHash" : "98dad7871ef9c02f19ba15929e2620e9465a410904ed8960b5893c9f3c4eb8fe",
    "contractAddress" : "tNULSeBaMx7J2im9edmmyZofHoTWW6nCTbvy3K"
  }
}
```

4.2 调用合约
========
Cmd: /api/contract/call
-----------------------
_**详细描述: 调用合约**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "gasLimit" : 0,
  "price" : 0,
  "password" : null,
  "remark" : null,
  "contractAddress" : null,
  "value" : null,
  "methodName" : null,
  "methodDesc" : null,
  "args" : null
}
```

参数列表
----
| 参数名                                                             |     参数类型     | 参数描述                       | 是否必填 |
| --------------------------------------------------------------- |:------------:| -------------------------- |:----:|
| 调用合约                                                            | contractcall | 调用合约表单                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |    string    | 交易创建者                      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit        |     long     | 最大gas消耗                    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price           |     long     | 执行合约单价                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password        |    string    | 交易创建者账户密码                  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark          |    string    | 备注                         |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |    string    | 智能合约地址                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value           |  biginteger  | 调用者向合约地址转入的主网资产金额，没有此业务时填0 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |    string    | 方法名                        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |    string    | 方法描述，若合约内方法没有重载，则此参数可以为空   |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |   object[]   | 参数列表                       |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述        |
| ------ |:------:| ----------- |
| txHash | string | 调用合约的交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "gasLimit" : 20000,
  "price" : 25,
  "password" : "nuls123456",
  "remark" : null,
  "contractAddress" : "tNULSeBaMx7J2im9edmmyZofHoTWW6nCTbvy3K",
  "value" : 0,
  "methodName" : "transfer",
  "methodDesc" : null,
  "args" : [ "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", 990 ]
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHash" : "f7b04c3d0863d79b08d6bd2758899fce8b5a4f09d2142a12bf2545ff978e0250"
  }
}
```

4.3 删除合约
========
Cmd: /api/contract/delete
-------------------------
_**详细描述: 删除合约**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "contractAddress" : null,
  "password" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                             |      参数类型      | 参数描述      | 是否必填 |
| --------------------------------------------------------------- |:--------------:| --------- |:----:|
| 删除合约                                                            | contractdelete | 删除合约表单    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |     string     | 交易创建者     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string     | 智能合约地址    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password        |     string     | 交易创建者账户密码 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark          |     string     | 备注        |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述        |
| ------ |:------:| ----------- |
| txHash | string | 删除合约的交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "contractAddress" : "tNULSeBaMx7J2im9edmmyZofHoTWW6nCTbvy3K",
  "password" : "nuls123456",
  "remark" : "delete-remark"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHash" : "c1ddf2254adf571ea441406bd0593d5fbc809e1e8aa8e5064fb3885fd7536f87"
  }
}
```

4.4 合约token转账
=============
Cmd: /api/contract/tokentransfer
--------------------------------
_**详细描述: 合约token转账**_
### HttpMethod: POST

### Form json data: 
```json
{
  "fromAddress" : null,
  "password" : null,
  "toAddress" : null,
  "contractAddress" : null,
  "amount" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                             |         参数类型          | 参数描述         | 是否必填 |
| --------------------------------------------------------------- |:---------------------:| ------------ |:----:|
| token转账                                                         | contracttokentransfer | token转账表单    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromAddress     |        string         | 转出者账户地址      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password        |        string         | 转出者账户地址密码    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toAddress       |        string         | 转入者账户地址      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |        string         | 合约地址         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount          |      biginteger       | 转出的token资产金额 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark          |        string         | 备注           |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述   |
| ------ |:------:| ------ |
| txHash | string | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "fromAddress" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "password" : "nuls123456",
  "toAddress" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
  "contractAddress" : "tNULSeBaNAKfKnLMR5XG5qtwXt5JS1b3QosZxg",
  "amount" : 8000,
  "remark" : "800个"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHash" : "a53fd2bff66a8e7ea243691afb95832d95cb2206c34684e233042ee3f399db5d"
  }
}
```

4.5 从账户地址向合约地址转账(主链资产)的合约交易
===========================
Cmd: /api/contract/transfer2contract
------------------------------------
_**详细描述: 从账户地址向合约地址转账(主链资产)的合约交易**_
### HttpMethod: POST

### Form json data: 
```json
{
  "fromAddress" : null,
  "password" : null,
  "toAddress" : null,
  "amount" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                         |       参数类型       | 参数描述      | 是否必填 |
| ----------------------------------------------------------- |:----------------:| --------- |:----:|
| 向合约地址转账                                                     | contracttransfer | 向合约地址转账表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromAddress |      string      | 转出者账户地址   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password    |      string      | 转出者账户地址密码 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toAddress   |      string      | 转入的合约地址   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount      |    biginteger    | 转出的主链资产金额 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark      |      string      | 备注        |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述   |
| ------ |:------:| ------ |
| txHash | string | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "fromAddress" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "password" : "nuls123456",
  "toAddress" : "tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu",
  "amount" : "400000000",
  "remark" : "向合约转账"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHash" : "e04fcdbfd571754dac48d7c3cd8f3b6e9830e9ce00292fad0ec601ad50bb8d5e"
  }
}
```

4.6 获取账户地址的指定合约的token余额
=======================
Cmd: /api/contract/balance/token/{contractAddress}/{address}
------------------------------------------------------------
_**详细描述: 获取账户地址的指定合约的token余额**_
### HttpMethod: GET

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否必填 |
| --------------- |:------:| ---- |:----:|
| contractAddress | string | 合约地址 |  是   |
| address         | string | 账户地址 |  是   |

返回值
---
| 字段名             |  字段类型  | 参数描述                    |
| --------------- |:------:| ----------------------- |
| contractAddress | string | 合约地址                    |
| name            | string | token名称                 |
| symbol          | string | token符号                 |
| amount          | string | token数量                 |
| decimals        |  long  | token支持的小数位数            |
| blockHeight     |  long  | 合约创建时的区块高度              |
| status          |  int   | 合约状态(0-不存在, 1-正常, 2-终止) |
### Example request data: 

_**request path:**_
http://localhost:9898/api/contract/balance/token/tNULSeBaNAKfKnLMR5XG5qtwXt5JS1b3QosZxg/tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "contractAddress" : "tNULSeBaNAKfKnLMR5XG5qtwXt5JS1b3QosZxg",
    "name" : "io",
    "symbol" : "IO",
    "amount" : "8000",
    "decimals" : 1,
    "blockHeight" : 719,
    "status" : 1
  }
}
```

4.7 获取智能合约详细信息
==============
Cmd: /api/contract/info/{address}
---------------------------------
_**详细描述: 获取智能合约详细信息**_
### HttpMethod: GET

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否必填 |
| ------- |:------:| ---- |:----:|
| address | string | 合约地址 |  是   |

返回值
---
| 字段名                                                                                                      |      字段类型       | 参数描述                          |
| -------------------------------------------------------------------------------------------------------- |:---------------:| ----------------------------- |
| createTxHash                                                                                             |     string      | 发布合约的交易hash                   |
| address                                                                                                  |     string      | 合约地址                          |
| creater                                                                                                  |     string      | 合约创建者地址                       |
| alias                                                                                                    |     string      | 合约别名                          |
| createTime                                                                                               |      long       | 合约创建时间（单位：秒）                  |
| blockHeight                                                                                              |      long       | 合约创建时的区块高度                    |
| isDirectPayable                                                                                          |     boolean     | 是否接受直接转账                      |
| isNrc20                                                                                                  |     boolean     | 是否是NRC20合约                    |
| nrc20TokenName                                                                                           |     string      | NRC20-token名称                 |
| nrc20TokenSymbol                                                                                         |     string      | NRC20-token符号                 |
| decimals                                                                                                 |      long       | NRC20-token支持的小数位数            |
| totalSupply                                                                                              |     string      | NRC20-token发行总量               |
| status                                                                                                   |     string      | 合约状态（not_found, normal, stop） |
| method                                                                                                   | list&lt;object> | 合约方法列表                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | 方法名称                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | 方法描述                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | 方法参数列表                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | 参数类型                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | 参数名称                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | 是否必填                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | 返回值类型                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | 是否视图方法（调用此方法数据不上链）            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | 是否是事件                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | 是否是可接受主链资产转账的方法               |
### Example request data: 

_**request path:**_
http://localhost:9898/api/contract/info/tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "createTxHash" : "de8825c942f50896f65c3c7c9ab18e388218568c7da64e09420a106b02edd81f",
    "address" : "tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu",
    "creater" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
    "alias" : "rf_innercall_offline",
    "createTime" : 1563285762,
    "blockHeight" : 743,
    "directPayable" : true,
    "nrc20" : false,
    "nrc20TokenName" : null,
    "nrc20TokenSymbol" : null,
    "decimals" : 0,
    "totalSupply" : null,
    "status" : "normal",
    "method" : [ {
      "name" : "_payable",
      "desc" : "() return void",
      "args" : [ ],
      "returnArg" : "void",
      "view" : false,
      "event" : false,
      "payable" : true
    }, {
      "name" : "<init>",
      "desc" : "() return void",
      "args" : [ ],
      "returnArg" : "void",
      "view" : false,
      "event" : false,
      "payable" : false
    }, {
      "name" : "getName",
      "desc" : "() return String",
      "args" : [ ],
      "returnArg" : "String",
      "view" : false,
      "event" : false,
      "payable" : false
    }, {
      "name" : "getSymbol",
      "desc" : "() return String",
      "args" : [ ],
      "returnArg" : "String",
      "view" : false,
      "event" : false,
      "payable" : false
    }, {
      "name" : "getDecimals",
      "desc" : "() return int",
      "args" : [ ],
      "returnArg" : "int",
      "view" : false,
      "event" : false,
      "payable" : false
    }, {
      "name" : "balance",
      "desc" : "() return String",
      "args" : [ ],
      "returnArg" : "String",
      "view" : false,
      "event" : false,
      "payable" : false
    }, {
      "name" : "single",
      "desc" : "() return String",
      "args" : [ ],
      "returnArg" : "String",
      "view" : false,
      "event" : false,
      "payable" : false
    }, {
      "name" : "multy",
      "desc" : "() return String",
      "args" : [ ],
      "returnArg" : "String",
      "view" : false,
      "event" : false,
      "payable" : true
    }, {
      "name" : "multyForAddress",
      "desc" : "(Address add1, BigInteger add1_na, Address add2, BigInteger add2_na, String add3ForString, BigInteger add3_na) return String",
      "args" : [ {
        "type" : "Address",
        "name" : "add1",
        "required" : false
      }, {
        "type" : "BigInteger",
        "name" : "add1_na",
        "required" : false
      }, {
        "type" : "Address",
        "name" : "add2",
        "required" : false
      }, {
        "type" : "BigInteger",
        "name" : "add2_na",
        "required" : false
      }, {
        "type" : "String",
        "name" : "add3ForString",
        "required" : false
      }, {
        "type" : "BigInteger",
        "name" : "add3_na",
        "required" : false
      } ],
      "returnArg" : "String",
      "view" : false,
      "event" : false,
      "payable" : true
    }, {
      "name" : "allInfo",
      "desc" : "() return String",
      "args" : [ ],
      "returnArg" : "String",
      "view" : false,
      "event" : false,
      "payable" : false
    } ]
  }
}
```

4.8 获取智能合约执行结果
==============
Cmd: /api/contract/result/{hash}
--------------------------------
_**详细描述: 获取智能合约执行结果**_
### HttpMethod: GET

参数列表
----
| 参数名  |  参数类型  | 参数描述   | 是否必填 |
| ---- |:------:| ------ |:----:|
| hash | string | 交易hash |  是   |

返回值
---
| 字段名                                                                                                   |      字段类型       | 参数描述                                        |
| ----------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------- |
| success                                                                                               |     boolean     | 合约执行是否成功                                    |
| errorMessage                                                                                          |     string      | 执行失败信息                                      |
| contractAddress                                                                                       |     string      | 合约地址                                        |
| result                                                                                                |     string      | 合约执行结果                                      |
| gasLimit                                                                                              |      long       | GAS限制                                       |
| gasUsed                                                                                               |      long       | 已使用GAS                                      |
| price                                                                                                 |      long       | GAS单价                                       |
| totalFee                                                                                              |     string      | 交易总手续费                                      |
| txSizeFee                                                                                             |     string      | 交易大小手续费                                     |
| actualContractFee                                                                                     |     string      | 实际执行合约手续费                                   |
| refundFee                                                                                             |     string      | 合约返回的手续费                                    |
| value                                                                                                 |     string      | 调用者向合约地址转入的主网资产金额，没有此业务时则为0                 |
| stackTrace                                                                                            |     string      | 异常堆栈踪迹                                      |
| transfers                                                                                             | list&lt;object> | 合约转账列表（从合约转出）                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                |     string      | 合约生成交易：合约转账交易hash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | 转出的合约地址                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                               | list&lt;object> | 转入的地址列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to    |     string      | 转入地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value |     string      | 转入金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                           |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）         |
| events                                                                                                | list&lt;string> | 合约事件列表                                      |
| tokenTransfers                                                                                        | list&lt;object> | 合约token转账列表                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                       |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | 付款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                    |     string      | 收款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                  |     string      | token名称                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                |     string      | token符号                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                              |      long       | token支持的小数位数                                |
| invokeRegisterCmds                                                                                    | list&lt;object> | 合约调用外部命令的调用记录列表                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                               |     string      | 命令名称                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                  |       map       | 命令参数，参数不固定，依据不同的命令而来，故此处不作描述，结构为 {参数名称=参数值} |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                       |     string      | 注册的命令模式（QUERY\_DATA or NEW\_TX）             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                             |     string      | 生成的交易hash（当调用的命令模式是 NEW\_TX 时，会生成交易）        |
| contractTxList                                                                                        | list&lt;string> | 合约生成交易的序列化字符串列表                             |
| remark                                                                                                |     string      | 备注                                          |
### Example request data: 

_**request path:**_
http://localhost:9898/api/contract/result/f0a5fc5d20c39355e35f1fe8011b1a28e7c65d8566ae8d76b297a22d1110851d

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "flag" : true,
    "data" : {
      "success" : true,
      "errorMessage" : null,
      "contractAddress" : "tNULSeBaN1rhd9k9eqNkvwC9HXBWLQ79dRuy81",
      "result" : "multyForAddress: 888634777633",
      "gasLimit" : 200000,
      "gasUsed" : 20038,
      "price" : 25,
      "totalFee" : "5100000",
      "txSizeFee" : "100000",
      "actualContractFee" : "500950",
      "refundFee" : "4499050",
      "value" : 10000000000,
      "stackTrace" : null,
      "transfers" : [ {
        "txHash" : "4877f6a865dea5b4ac82a8370d73e62da15bc7acb2145a03822dddfdab329d2b",
        "from" : "tNULSeBaN1rhd9k9eqNkvwC9HXBWLQ79dRuy81",
        "value" : "200000000",
        "outputs" : [ {
          "to" : "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL",
          "value" : "100000000"
        }, {
          "to" : "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL",
          "value" : "100000000"
        } ],
        "orginTxHash" : "b5473eefecd1c70ac4276f70062a92bdbfe8f779cbe48de2d0315686cc7e6789"
      } ],
      "events" : [ "{\"contractAddress\":\"TTb1LZLo6izPGmXa9dGPmb5D2vpLpNqA\",\"blockNumber\":1343847,\"event\":\"TransferEvent\",\"payload\":{\"from\":\"TTasNs8MGGGaFT9hd9DLmkammYYv69vs\",\"to\":\"TTau7kAxyhc4yMomVJ2QkMVECKKZK1uG\",\"value\":\"1000\"}}" ],
      "tokenTransfers" : [ {
        "contractAddress" : "TTb1LZLo6izPGmXa9dGPmb5D2vpLpNqA",
        "from" : "TTasNs8MGGGaFT9hd9DLmkammYYv69vs",
        "to" : "TTau7kAxyhc4yMomVJ2QkMVECKKZK1uG",
        "value" : "1000",
        "name" : "a",
        "symbol" : "a",
        "decimals" : 8
      } ],
      "invokeRegisterCmds" : [ {
        "cmdName" : "cs_createContractAgent",
        "args" : {
          "contractBalance" : "2030000000000",
          "commissionRate" : "100",
          "chainId" : 2,
          "deposit" : "2000000000000",
          "contractAddress" : "tNULSeBaMzZedU4D3xym1JcyNa5sqtuFku8AKm",
          "contractNonce" : "0000000000000000",
          "blockTime" : 1562564381,
          "packingAddress" : "tNULSeBaMtEPLXxUgyfnBt9bpb5Xv84dyJV98p",
          "contractSender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"
        },
        "cmdRegisterMode" : "NEW_TX",
        "newTxHash" : "a8eae11b52990e39c9d3233ba1d2c8827336d261c0f14aca43dd4f06435dfaba"
      } ],
      "contractTxList" : [ "12002fbb225d0037b5473eefecd1c70ac4276f70062a92bdbfe8f779cbe48de2d0315686cc7e678902000253472f4702eb83b71871a4c4e0c71526bb86b8afd0011702000253472f4702eb83b71871a4c4e0c71526bb86b8af0200010000c2eb0b0000000000000000000000000000000000000000000000000000000008000000000000000000021702000194f6239c075d184e265eaea97a67eeced51725160200010000e1f50500000000000000000000000000000000000000000000000000000000000000000000000017020001ce8ffa95606f0bfd2778cff2eff8fe8999e20c440200010000e1f50500000000000000000000000000000000000000000000000000000000000000000000000000", "1400bf6b285d006600204aa9d1010000000000000000000000000000000000000000000000000000020002f246b18e8c697f00ed9bd22696998e469d3f824b020001d7424d91c83566eb94233b5416f2aa77709c03e1020002f246b18e8c697f00ed9bd22696998e469d3f824b648c0117020002f246b18e8c697f00ed9bd22696998e469d3f824b0200010000204aa9d1010000000000000000000000000000000000000000000000000000080000000000000000000117020002f246b18e8c697f00ed9bd22696998e469d3f824b0200010000204aa9d1010000000000000000000000000000000000000000000000000000ffffffffffffffff00" ],
      "remark" : "call"
    }
  }
}
```

4.9 获取合约代码构造函数
==============
Cmd: /api/contract/constructor
------------------------------
_**详细描述: 获取合约代码构造函数**_
### HttpMethod: POST

### Form json data: 
```json
{
  "contractCode" : null
}
```

参数列表
----
| 参数名                                                          |     参数类型     | 参数描述                 | 是否必填 |
| ------------------------------------------------------------ |:------------:| -------------------- |:----:|
| 获取合约代码构造函数                                                   | contractcode | 获取合约代码构造函数表单         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractCode |    string    | 智能合约代码(字节码的Hex编码字符串) |  是   |

返回值
---
| 字段名                                                                                                      |      字段类型       | 参数描述               |
| -------------------------------------------------------------------------------------------------------- |:---------------:| ------------------ |
| constructor                                                                                              |     object      | 合约构造函数详情           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | 方法名称               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | 方法描述               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | 方法参数列表             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | 参数类型               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | 参数名称               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | 是否必填               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | 返回值类型              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | 是否视图方法（调用此方法数据不上链） |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | 是否是事件              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | 是否是可接受主链资产转账的方法    |
| isNrc20                                                                                                  |     boolean     | 是否是NRC20合约         |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "contractCode" : "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbacf782dd58a888e49983a72c8e83821c367b8a1c382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe274703aa254cbe6691d5fe0bc8ea36087270e4f9a11551d916cb9588a8c999109f6312f864869d2c845f2850819dfc8f078e4f67e394e289db32c389413c98fdb36fac5c52f79e32c7cafb216aae57260ecb899a2f6b535fb6bb7f3a261cd7c5153772c0b2e706ce8d001be1c1edacf712a7f527a6bb82e6f86b2e54cc9529f59fdf55ab0ded9d69a78f749eb34149d5b29c22b7f812bcc3f55a6735bbdf7be1d916db7cef4dd3aa5a1bda68123ac4e524ec0cc5a54df5587bd544dd93d3f6172df861a306a1a0d16ec0f025ae83d26e71b53f9ec9451607ee761337e04f9c9868c7b85097943f71eabc51de8e637750c3ec49140039a842af8e3a049d8423d49187cfa8433d493c400d2b492b7f2ef2ecffb003fd699616ca3bc9d3349be697c2e8a5e8676416df90ec70635b9163b38eaf6067c173bf91c745677f1a911422d45beb98ad62fa8682376d750e4ff688ea2be05156dc6f76a280acc45945c50d110f6388afecd1d8bf834a3335476090d571088de40f0e80c4257d05815c3d1d81568d1f815f8a2cd812bf0471397b0b8397015fa552c915397d1780d4b05d0752cf3e3be1b587e54736666b0e21a9a2eba40fbb158fd060c30658bb11a2d6865296cc606a639c6246f645a0799d6434c729a491687b6d8401d8744ea57711049cac5a7a4ef739f5f491b95febb29b705b86d58797eeb4892e50189c51754212692441c8e5dc74a1fbec4aafd89af1064602e24e2ce54f340a02df015429c1449c21f505e75f2f72a887529313611650bc3bd8ec9bf8d798b33e4552f92ae1749ecc53e276dfb29f978ba1b07e8bf9f6797e0204f04f00327a1f6da56ae1de2cc52f86ea22588919b680ee2f0ac248bdf4b7c737c3d825127ef670823c0674f34c6142566d0128d715c1d8d736c95d4c54492ac05aa85daa2c0df8b46825e46b0cd84bc8e80c4b1a8adcf75ac07f7398ef5e087ca31910eaaf408b895fe9ae9e0bde5a4e3491e914cf6c4fe8960e01c02fe6b68932aaba4a6795fdc4ecdbeb8e4c0ef817884d11a453b2174d2f83aea1488115b9f07a21dfb7684713f81f9545417c31fdaa9dd446bad90063517e98ff0632798166d8ae6ae2863199728c6e24e143921f29c3836a9583d80101e64120d92d1980760970bb04b454e53d24105b0665a7fe22279d041d21e750cb7df404705c49ad92096aa9d134ce624bbcff200687701b4ab9ad494340f80075c00136c0151d15d01b0b602404d5c45671d1c59e2c8d1cdbc0747b78ba3dbc5d14d1febe2305c1c978943623c142586ae8aedfdf1c45544ce62859a60e53040e7a057b0b6cc83d5efe9f022a35c22ee32ab6d9a55f4302faf47d8e7a73d8d30e4621f72b10f31cd5b5487df89946a04a9b746e9e21e165ba896533ef914a42c4e3de1907584f545740381685b40bc4bc8108bb705e85d375daaf2acdd0d8fb2d01ea3d5c7b1923abad85955268ab83823187770469846bb61234ec3865821f72826f2c907af83e78c83a7d38327d1b46e06eb2b687a66a1b1cbfe296a7b1a2bf00ccbfe590f924e1749a78ba4d345d2e922697191582e927f7045740cd8b1f0f2b74dd66dce453610508b6dce1dd51688276c49507a59fc39ea7b5efe958836bc40067991fcfc126fa2973d88075cc4032ee20117f1808378156fb5e3e4f32a8bdb335b3923f90f816493248bf3147f42381e9de6aa448ba1bc8a6fed8fc69b36cce0b68a7b8db10423de5d85ee4df72b64ac577975bcc680bf4ee86f78ca72bd0b793d5b6e9ff30563b776031372bf82ec931f340e90a203a4cb01928837f54a862b207ae682b0b3fc16d59d65cdbdcd78bee301e025b7ec1c726b60ac2b00f8b3c701708c6f12cfd5bc075648d3b285dfacde0817dde60cab6def320aef29832df631d7e06a654653415f045fd34e4dd979c8b133e5dc371dd19804597843cabab62ddbc973d4f33eed7e407afcd0435c1daecd0e14944d918aee1543eb2d8ef59263dd74acb7d6f0327e0d7d73ddfc98ee7ce231d9ea9a6c75dc14a9acbe8be6383ced96984f79b1a68ec38959666d8f2f52d12542f894fe5cf6a4758d6b7e8debf11ac7639104887fb6ef2755c53c8c5f2ae53e2c278b3e4b36ece0f35534fe17504b0708ec308779cb09000028180000504b0304140008080800aa7b564e00000000000000000000000022000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373a552d14e1341143d03b5cb16aa28a8a0284a08697970131ff481a76a24694282a19507dfa6bb431d989da933b325fc9a0f7e801f65bc534ba5b6a5896eb23b3be79e7bcebd33f7c7cf6fdf01bcc64e84e711b623bc60a8367a3d6bfa5c7de80bed19569a5a0bfb5e71e7848bf092186dcbb53b1376c828699e0b86f55afde89cf779a2b8ee262d6fa5ee1e306c9e14dacb5c9c4a273b4a34b4369e7b69b463d83b9226d18572496ab4b73cf589cb2e123ee224a7525c9248d95de51da318963291ca9c2b4a5eacd59b0ccb9ea8aa55f47aea8a61e3ba869cfb2fc93bd96d6a2fbac29244dce154582a8ecf18ded4a61b37b2cc0ae70e668aec8c37f3915b6add0b3bd6d5fedcae4ec4d7425a9191e2921f1e26c3db39554d2faafe99eee85ae4d09a9ce1d33ca17fb489f86034e8ae63ae94b90cc7c9d0fe2fb799475d6999c2a6e2502af2a8b4cd85d0af0295616b42d0876832e030acfe19c2e3ceb9486942f76fcbd8fd6be06f278fcd7e99816101e1894b749788695fa15d99d66580b0952958157727b07b589dc0eee301a9dfc0b0466f144cd70784877844ff01ac2efc0647a1c7d81886d6166f864684cdd9b94ff0748aef388d085b83ef33dca135141a7c4ad46e488a10ff02504b070868fe421cca0100005e040000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c6173738d565b6f1b4514fec6bbde75b69bbb49d224b46929e05b6a2ee19a4b73218140d2943835b4406163af926d9cdd74771d90a0129540ea033c801020242e2f252f790089b80824c45390f82ffc03049c995dbb89e3243cf8ccd93367e67cdf7c6746fef39f5f7e03f018ae6988e34213148c7333c1cd248f4da998d63083e735bcc0cd0c66b937abe2450d2770218697f838c7cd7c0c176358e0ee256e5e56b1a822c71075deb64d97e1d49ce564ed72c9cb161cdb778d829ff58a6bd98962d1353d6f9841f5364cbbc853a39b46a96c3274cd5d37368decbae1af6627ad9559db37574c97529511cbb6fc3186cb89a3773d66b6e1f6c93c833ce5140940eb9c659b17cbebcba6bb642c9728d231e7148c52de702dfe1d06657fd5f2189a2736365c87b04f6f9ab6cfa0cfdac47caa64789e49d39983607c67cdb4b34bdc9edbb798389e5d2cdbbeb56ee62dcfa22a970cd758377dd39db06dc7377ccbb169cf546382462d27bb68de285bae59a41d632ba6bf10883190481e2747ccab659f39e694f98969b479ae2aa0e6edf9e065f381a03d54f6104979b930eb64e2706114a2639488f97d6152c9b057b20bcbd7cd823f9cbccac01c2ed28129219241a234e77ca3b0366f6c08eda8c1a9f4aae1ad06824b89e42c152156e5122533fa88f94ece772d7b85215e852f760ea2b4b39673ca6ec19cb1782f6842cdf33c4d471f967474a15b470f3727d1abe332f22a5e21e9fe7f37a87855c7237854c5151d4fe16986ee7a189365ab244e3bbe6fe5bbe2f28deab88ad7b8799d006606c28b364ae43203e2ae898c37740c6384a1adfef4187a1bea1ff6391d5a3e9059f43a43e7be7312413aa6fea308379aded362c4ab514b9054c60627c330983828cd41b5c2631aaecbaff6cf11f9fd89a9c3a771869ecc38a82b2121c215a78735c2451723e94ea346f37de8a7effbe9ab44a34c633c75172c95de412495d981941adc81fca358758a6c17a2646f50a68b267868818f4e94719aa2a9603d06f000203c5e97098f578e088fd796708efc4e89261f2467bf7d080f93e590b234f2e5d1d44f88fc50c3a088e03ba2a61e248435191248868bc7289b575404977b0c34117d8fd6dc143b74055935d44a889a2e444320523d90f71b0249370622d503f980d67c7808909e104806830d80c8f5406e3704725e2c3a0044ae07f211adf9f810205c335e98ae7cb8d75f9423d1782b95fe1e51793bbd8b965405d134fdee202a6da77f8732cf75dba58ea22143bf0ad4afd0ba45375ec4298b9f679020514cda9b205513e45db4f381627205b12db472ef0fa8f21664699b404882462f01053e818a4fe9e43e23989f13e52ff026be14b40602c0355ab7e89fc6e3824e1143148be009d1a0badac2fe46bf8a2715d6adb0b836cea9d32b1752ff393cc6e9805d734a306be26046da4ef7ad069c78580ac36f55e37210271a5a10efbb07bf43dc9eafe9367d43cdf32da6f0dd1e35a643d89d04fa193c4b5038d84128ede3ec5fbae511824b30c947e84718cbefffa445f49c862caed1b6bc51867ec5892b77a1773457484141a5b5a32df025eeb7073e616e6dbb594147059df58d77674fe30d8540231815760c67c3a746c173f42c35fd07504b0708ea7bbc798f040000e6090000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c6173738d56df531b5514fe6e76b31bb60be19740015b5aab8624b06ad156c36f84160da5051a0bd5da25d9c296b08bbb1b5e1c1ffa6ff8aebcf0a033923a3ae3f88433fe2ffe078e7aeedd854212c0879c7bf6dc73eef9cef9cebd933ffff9e53700efe189864e4c3441c12417535c4c73db8c8a590d73b8a3e12e177398e7dabc8a4f345cc244029ff235cfc54202f71258e4ea7d2e1ea85852b1cc203ff3dc6d862b79db359c4ad9378aae1378663130fcd29631552a7996efe7186281cb10df35cb158ba12bffdcdc358d6d33d834a6ed8d7927b0362c8fbc9451dbb183718687a9f30fbc60b7e1f18305823be3960840326f3bd6bdcaf6bae5ad98eb65b2b4e7dda2592e989ecdbf23a31c6cda3e43f38a673afe33cb9bddb59c80419f771ccb9b299bbe6fd176b61e4ce06e598eb1c2e58d53c154e3f5a58a13d8db56c1f66dca72dff4cc6d2bb0bc29c7710333b05d87ce4c372ed03cf63196acaf2ab66795e84475c30ae6040d03a9c18b8850fd23e76b17f498f72b4e47af70e6fc704dd0772124b187729d4163c23ff6ba9c3a9b0c854a30cb54ed6b9153d974368cc5f5e75631c80dae3130971353b72588318988e6e5c02c6e2d983b822f1a674abd69fa9b21c9526a709e92502d95323933fa4804ee72e0d9ce0643e7117c717268a593b565b7e215ad399bf3af090687b99b8e3eace8e842b78e1e2e2ea357c74314547c4674fdff0950f148c73b7857c5aa8e5bb8cdd05d0b63ba62974b96c7d0712af26b7ed5c674ace131179f132bd981c01da3b2b203e26689cd2f74e430cad05adb3786de867c47534ded2a84048bc9a6eca73a248cd4a0fef34a6db47d62a4a8eb8d8681483277762ca7c43094aa27a59ea7a841b91affa3c939c7bf3f3573f636aed1d3d8099a47488871aee9018d71bac54a8cd3aad17e1ffae9fb75fa2ad32ad3da997e0996ce1c2096ce1e404a0f1d40fe51445d21d98538c92c790ea109c36881810e1a82ab644d87f118c01b80d0785e26349e3926349e5bc20dd293120986374fc8b7f036490ec7a09587c6d33f21f6c3717e45186f8a7c7ae810e5634861300a1e276f9e4d1175bc42af09eb2d8ab92d4ee80abd8e112b1162ba060d8148b540720d81641a03916a814c50cce419407a2220596a743d10b916c84c4320c322a80e885c0be40ec5dc3d0308e78b27a68b1e9df517f970ea5ea433df232eef670ed192ae229ea1df77884bfb99dfa12c70de0e699a68c9d2af0af55b24f7a065859dbc783f4307896cd24907e9c8413e441b5fc8265791d843926b7f4095f7204bfb04421265f41250200f150bd4b94582f9804a5ec2975816650d84808fcb7a41ff266e8a724a18215b0cef8be1d4d516f637fa557ca0b06e85756a93bc747adba2d27f8eda381b56d79c1695357130a3ad57fb36c39ab8598acc4f8fec7268a732b4d0def70a7ebbb8398fe826add2f0ac119b8f4fb0311bc1ee20d01fe22382c2c10e41699b64ffd20d8f115c82493a223dc658e1f42705d1531a55f1848ee58332f22b2eadbe84dede5c25064529c9f6d65097b8de16ea8439d9fa4d15ed5574d40eded313833712018d614cc8715c8f9e19051fd393d4f41f504b0708826261e37e040000ca090000504b01020a000a0000080000aa7b564e000000000000000000000000030004000000000000000000000000000000696f2ffeca0000504b01020a000a0000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e00000000000000000000000011000000000000000000000000004b000000696f2f6e756c732f636f6e74726163742f504b01020a000a0000080000aa7b564e00000000000000000000000017000000000000000000000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
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
    "nrc20" : true
  }
}
```

4.10 获取已发布合约指定函数的信息
===================
Cmd: /api/contract/method
-------------------------
_**详细描述: 获取已发布合约指定函数的信息**_
### HttpMethod: POST

### Form json data: 
```json
{
  "contractAddress" : null,
  "methodName" : null,
  "methodDesc" : null
}
```

参数列表
----
| 参数名                                                             |        参数类型        | 参数描述                     | 是否必填 |
| --------------------------------------------------------------- |:------------------:| ------------------------ |:----:|
| 获取已发布合约指定函数的信息                                                  | contractmethodform | 获取已发布合约指定函数的信息表单         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |       string       | 智能合约地址                   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |       string       | 方法名                      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |       string       | 方法描述，若合约内方法没有重载，则此参数可以为空 |  否   |

返回值
---
| 字段名                                                      |      字段类型       | 参数描述               |
| -------------------------------------------------------- |:---------------:| ------------------ |
| name                                                     |     string      | 方法名称               |
| desc                                                     |     string      | 方法描述               |
| args                                                     | list&lt;object> | 方法参数列表             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | 参数类型               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | 参数名称               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | 是否必填               |
| returnArg                                                |     string      | 返回值类型              |
| view                                                     |     boolean     | 是否视图方法（调用此方法数据不上链） |
| event                                                    |     boolean     | 是否是事件              |
| payable                                                  |     boolean     | 是否是可接受主链资产转账的方法    |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "contractAddress" : "tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu",
  "methodName" : "multyForAddress",
  "methodDesc" : null
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "name" : "multyForAddress",
    "desc" : "(Address add1, BigInteger add1_na, Address add2, BigInteger add2_na, String add3ForString, BigInteger add3_na) return String",
    "args" : [ {
      "type" : "Address",
      "name" : "add1",
      "required" : false
    }, {
      "type" : "BigInteger",
      "name" : "add1_na",
      "required" : false
    }, {
      "type" : "Address",
      "name" : "add2",
      "required" : false
    }, {
      "type" : "BigInteger",
      "name" : "add2_na",
      "required" : false
    }, {
      "type" : "String",
      "name" : "add3ForString",
      "required" : false
    }, {
      "type" : "BigInteger",
      "name" : "add3_na",
      "required" : false
    } ],
    "returnArg" : "String",
    "view" : false,
    "event" : false,
    "payable" : true
  }
}
```

4.11 获取已发布合约指定函数的参数类型列表
=======================
Cmd: /api/contract/method/argstypes
-----------------------------------
_**详细描述: 获取已发布合约指定函数的参数类型列表**_
### HttpMethod: POST

### Form json data: 
```json
{
  "contractAddress" : null,
  "methodName" : null,
  "methodDesc" : null
}
```

参数列表
----
| 参数名                                                             |        参数类型        | 参数描述                     | 是否必填 |
| --------------------------------------------------------------- |:------------------:| ------------------------ |:----:|
| 获取已发布合约指定函数的参数类型列表                                              | contractmethodform | 获取已发布合约指定函数的参数类型表单       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |       string       | 智能合约地址                   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |       string       | 方法名                      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |       string       | 方法描述，若合约内方法没有重载，则此参数可以为空 |  否   |

返回值
---
| 字段名 |      字段类型       | 参数描述 |
| --- |:---------------:| ---- |
| 返回值 | list&lt;string> |      |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "contractAddress" : "tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu",
  "methodName" : "multyForAddress",
  "methodDesc" : null
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : [ "Address", "BigInteger", "Address", "BigInteger", "String", "BigInteger" ]
}
```

4.12 验证发布合约
===========
Cmd: /api/contract/validate/create
----------------------------------
_**详细描述: 验证发布合约**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "gasLimit" : 0,
  "price" : 0,
  "contractCode" : null,
  "args" : null
}
```

参数列表
----
| 参数名                                                          |          参数类型          | 参数描述                 | 是否必填 |
| ------------------------------------------------------------ |:----------------------:| -------------------- |:----:|
| 验证发布合约                                                       | contractvalidatecreate | 验证发布合约表单             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender       |         string         | 交易创建者                |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit     |          long          | 最大gas消耗              |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price        |          long          | 执行合约单价               |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractCode |         string         | 智能合约代码(字节码的Hex编码字符串) |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args         |        object[]        | 参数列表                 |  否   |

返回值
---
| 字段名     |  字段类型   | 参数描述      |
| ------- |:-------:| --------- |
| success | boolean | 验证成功与否    |
| code    | string  | 验证失败的错误码  |
| msg     | string  | 验证失败的错误信息 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "gasLimit" : 20000,
  "price" : 25,
  "contractCode" : "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbacf782dd58a888e49983a72c8e83821c367b8a1c382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe274703aa254cbe6691d5fe0bc8ea36087270e4f9a11551d916cb9588a8c999109f6312f864869d2c845f2850819dfc8f078e4f67e394e289db32c389413c98fdb36fac5c52f79e32c7cafb216aae57260ecb899a2f6b535fb6bb7f3a261cd7c5153772c0b2e706ce8d001be1c1edacf712a7f527a6bb82e6f86b2e54cc9529f59fdf55ab0ded9d69a78f749eb34149d5b29c22b7f812bcc3f55a6735bbdf7be1d916db7cef4dd3aa5a1bda68123ac4e524ec0cc5a54df5587bd544dd93d3f6172df861a306a1a0d16ec0f025ae83d26e71b53f9ec9451607ee761337e04f9c9868c7b85097943f71eabc51de8e637750c3ec49140039a842af8e3a049d8423d49187cfa8433d493c400d2b492b7f2ef2ecffb003fd699616ca3bc9d3349be697c2e8a5e8676416df90ec70635b9163b38eaf6067c173bf91c745677f1a911422d45beb98ad62fa8682376d750e4ff688ea2be05156dc6f76a280acc45945c50d110f6388afecd1d8bf834a3335476090d571088de40f0e80c4257d05815c3d1d81568d1f815f8a2cd812bf0471397b0b8397015fa552c915397d1780d4b05d0752cf3e3be1b587e54736666b0e21a9a2eba40fbb158fd060c30658bb11a2d6865296cc606a639c6246f645a0799d6434c729a491687b6d8401d8744ea57711049cac5a7a4ef739f5f491b95febb29b705b86d58797eeb4892e50189c51754212692441c8e5dc74a1fbec4aafd89af1064602e24e2ce54f340a02df015429c1449c21f505e75f2f72a887529313611650bc3bd8ec9bf8d798b33e4552f92ae1749ecc53e276dfb29f978ba1b07e8bf9f6797e0204f04f00327a1f6da56ae1de2cc52f86ea22588919b680ee2f0ac248bdf4b7c737c3d825127ef670823c0674f34c6142566d0128d715c1d8d736c95d4c54492ac05aa85daa2c0df8b46825e46b0cd84bc8e80c4b1a8adcf75ac07f7398ef5e087ca31910eaaf408b895fe9ae9e0bde5a4e3491e914cf6c4fe8960e01c02fe6b68932aaba4a6795fdc4ecdbeb8e4c0ef817884d11a453b2174d2f83aea1488115b9f07a21dfb7684713f81f9545417c31fdaa9dd446bad90063517e98ff0632798166d8ae6ae2863199728c6e24e143921f29c3836a9583d80101e64120d92d1980760970bb04b454e53d24105b0665a7fe22279d041d21e750cb7df404705c49ad92096aa9d134ce624bbcff200687701b4ab9ad494340f80075c00136c0151d15d01b0b602404d5c45671d1c59e2c8d1cdbc0747b78ba3dbc5d14d1febe2305c1c978943623c142586ae8aedfdf1c45544ce62859a60e53040e7a057b0b6cc83d5efe9f022a35c22ee32ab6d9a55f4302faf47d8e7a73d8d30e4621f72b10f31cd5b5487df89946a04a9b746e9e21e165ba896533ef914a42c4e3de1907584f545740381685b40bc4bc8108bb705e85d375daaf2acdd0d8fb2d01ea3d5c7b1923abad85955268ab83823187770469846bb61234ec3865821f72826f2c907af83e78c83a7d38327d1b46e06eb2b687a66a1b1cbfe296a7b1a2bf00ccbfe590f924e1749a78ba4d345d2e922697191582e927f7045740cd8b1f0f2b74dd66dce453610508b6dce1dd51688276c49507a59fc39ea7b5efe958836bc40067991fcfc126fa2973d88075cc4032ee20117f1808378156fb5e3e4f32a8bdb335b3923f90f816493248bf3147f42381e9de6aa448ba1bc8a6fed8fc69b36cce0b68a7b8db10423de5d85ee4df72b64ac577975bcc680bf4ee86f78ca72bd0b793d5b6e9ff30563b776031372bf82ec931f340e90a203a4cb01928837f54a862b207ae682b0b3fc16d59d65cdbdcd78bee301e025b7ec1c726b60ac2b00f8b3c701708c6f12cfd5bc075648d3b285dfacde0817dde60cab6def320aef29832df631d7e06a654653415f045fd34e4dd979c8b133e5dc371dd19804597843cabab62ddbc973d4f33eed7e407afcd0435c1daecd0e14944d918aee1543eb2d8ef59263dd74acb7d6f0327e0d7d73ddfc98ee7ce231d9ea9a6c75dc14a9acbe8be6383ced96984f79b1a68ec38959666d8f2f52d12542f894fe5cf6a4758d6b7e8debf11ac7639104887fb6ef2755c53c8c5f2ae53e2c278b3e4b36ece0f35534fe17504b0708ec308779cb09000028180000504b0304140008080800aa7b564e00000000000000000000000022000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373a552d14e1341143d03b5cb16aa28a8a0284a08697970131ff481a76a24694282a19507dfa6bb431d989da933b325fc9a0f7e801f65bc534ba5b6a5896eb23b3be79e7bcebd33f7c7cf6fdf01bcc64e84e711b623bc60a8367a3d6bfa5c7de80bed19569a5a0bfb5e71e7848bf092186dcbb53b1376c828699e0b86f55afde89cf779a2b8ee262d6fa5ee1e306c9e14dacb5c9c4a273b4a34b4369e7b69b463d83b9226d18572496ab4b73cf589cb2e123ee224a7525c9248d95de51da318963291ca9c2b4a5eacd59b0ccb9ea8aa55f47aea8a61e3ba869cfb2fc93bd96d6a2fbac29244dce154582a8ecf18ded4a61b37b2cc0ae70e668aec8c37f3915b6add0b3bd6d5fedcae4ec4d7425a9191e2921f1e26c3db39554d2faafe99eee85ae4d09a9ce1d33ca17fb489f86034e8ae63ae94b90cc7c9d0fe2fb799475d6999c2a6e2502af2a8b4cd85d0af0295616b42d0876832e030acfe19c2e3ceb9486942f76fcbd8fd6be06f278fcd7e99816101e1894b749788695fa15d99d66580b0952958157727b07b589dc0eee301a9dfc0b0466f144cd70784877844ff01ac2efc0647a1c7d81886d6166f864684cdd9b94ff0748aef388d085b83ef33dca135141a7c4ad46e488a10ff02504b070868fe421cca0100005e040000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c6173738d565b6f1b4514fec6bbde75b69bbb49d224b46929e05b6a2ee19a4b73218140d2943835b4406163af926d9cdd74771d90a0129540ea033c801020242e2f252f790089b80824c45390f82ffc03049c995dbb89e3243cf8ccd93367e67cdf7c6746fef39f5f7e03f018ae6988e34213148c7333c1cd248f4da998d63083e735bcc0cd0c66b937abe2450d2770218697f838c7cd7c0c176358e0ee256e5e56b1a822c71075deb64d97e1d49ce564ed72c9cb161cdb778d829ff58a6bd98962d1353d6f9841f5364cbbc853a39b46a96c3274cd5d37368decbae1af6627ad9559db37574c97529511cbb6fc3186cb89a3773d66b6e1f6c93c833ce5140940eb9c659b17cbebcba6bb642c9728d231e7148c52de702dfe1d06657fd5f2189a2736365c87b04f6f9ab6cfa0cfdac47caa64789e49d39983607c67cdb4b34bdc9edbb798389e5d2cdbbeb56ee62dcfa22a970cd758377dd39db06dc7377ccbb169cf546382462d27bb68de285bae59a41d632ba6bf10883190481e2747ccab659f39e694f98969b479ae2aa0e6edf9e065f381a03d54f6104979b930eb64e2706114a2639488f97d6152c9b057b20bcbd7cd823f9cbccac01c2ed28129219241a234e77ca3b0366f6c08eda8c1a9f4aae1ad06824b89e42c152156e5122533fa88f94ece772d7b85215e852f760ea2b4b39673ca6ec19cb1782f6842cdf33c4d471f967474a15b470f3727d1abe332f22a5e21e9fe7f37a87855c7237854c5151d4fe16986ee7a189365ab244e3bbe6fe5bbe2f28deab88ad7b8799d006606c28b364ae43203e2ae898c37740c6384a1adfef4187a1bea1ff6391d5a3e9059f43a43e7be7312413aa6fea308379aded362c4ab514b9054c60627c330983828cd41b5c2631aaecbaff6cf11f9fd89a9c3a771869ecc38a82b2121c215a78735c2451723e94ea346f37de8a7effbe9ab44a34c633c75172c95de412495d981941adc81fca358758a6c17a2646f50a68b267868818f4e94719aa2a9603d06f000203c5e97098f578e088fd796708efc4e89261f2467bf7d080f93e590b234f2e5d1d44f88fc50c3a088e03ba2a61e248435191248868bc7289b575404977b0c34117d8fd6dc143b74055935d44a889a2e444320523d90f71b0249370622d503f980d67c7808909e104806830d80c8f5406e3704725e2c3a0044ae07f211adf9f810205c335e98ae7cb8d75f9423d1782b95fe1e51793bbd8b965405d134fdee202a6da77f8732cf75dba58ea22143bf0ad4afd0ba45375ec4298b9f679020514cda9b205513e45db4f381627205b12db472ef0fa8f21664699b404882462f01053e818a4fe9e43e23989f13e52ff026be14b40602c0355ab7e89fc6e3824e1143148be009d1a0badac2fe46bf8a2715d6adb0b836cea9d32b1752ff393cc6e9805d734a306be26046da4ef7ad069c78580ac36f55e37210271a5a10efbb07bf43dc9eafe9367d43cdf32da6f0dd1e35a643d89d04fa193c4b5038d84128ede3ec5fbae511824b30c947e84718cbefffa445f49c862caed1b6bc51867ec5892b77a1773457484141a5b5a32df025eeb7073e616e6dbb594147059df58d77674fe30d8540231815760c67c3a746c173f42c35fd07504b0708ea7bbc798f040000e6090000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c6173738d56df531b5514fe6e76b31bb60be19740015b5aab8624b06ad156c36f84160da5051a0bd5da25d9c296b08bbb1b5e1c1ffa6ff8aebcf0a033923a3ae3f88433fe2ffe078e7aeedd854212c0879c7bf6dc73eef9cef9cebd933ffff9e53700efe189864e4c3441c12417535c4c73db8c8a590d73b8a3e12e177398e7dabc8a4f345cc244029ff235cfc54202f71258e4ea7d2e1ea85852b1cc203ff3dc6d862b79db359c4ad9378aae1378663130fcd29631552a7996efe7186281cb10df35cb158ba12bffdcdc358d6d33d834a6ed8d7927b0362c8fbc9451dbb183718687a9f30fbc60b7e1f18305823be3960840326f3bd6bdcaf6bae5ad98eb65b2b4e7dda2592e989ecdbf23a31c6cda3e43f38a673afe33cb9bddb59c80419f771ccb9b299bbe6fd176b61e4ce06e598eb1c2e58d53c154e3f5a58a13d8db56c1f66dca72dff4cc6d2bb0bc29c7710333b05d87ce4c372ed03cf63196acaf2ab66795e84475c30ae6040d03a9c18b8850fd23e76b17f498f72b4e47af70e6fc704dd0772124b187729d4163c23ff6ba9c3a9b0c854a30cb54ed6b9153d974368cc5f5e75631c80dae3130971353b72588318988e6e5c02c6e2d983b822f1a674abd69fa9b21c9526a709e92502d95323933fa4804ee72e0d9ce0643e7117c717268a593b565b7e215ad399bf3af090687b99b8e3eace8e842b78e1e2e2ea357c74314547c4674fdff0950f148c73b7857c5aa8e5bb8cdd05d0b63ba62974b96c7d0712af26b7ed5c674ace131179f132bd981c01da3b2b203e26689cd2f74e430cad05adb3786de867c47534ded2a84048bc9a6eca73a248cd4a0fef34a6db47d62a4a8eb8d8681483277762ca7c43094aa27a59ea7a841b91affa3c939c7bf3f3573f636aed1d3d8099a47488871aee9018d71bac54a8cd3aad17e1ffae9fb75fa2ad32ad3da997e0996ce1c2096ce1e404a0f1d40fe51445d21d98538c92c790ea109c36881810e1a82ab644d87f118c01b80d0785e26349e3926349e5bc20dd293120986374fc8b7f036490ec7a09587c6d33f21f6c3717e45186f8a7c7ae810e5634861300a1e276f9e4d1175bc42af09eb2d8ab92d4ee80abd8e112b1162ba060d8148b540720d81641a03916a814c50cce419407a2220596a743d10b916c84c4320c322a80e885c0be40ec5dc3d0308e78b27a68b1e9df517f970ea5ea433df232eef670ed192ae229ea1df77884bfb99dfa12c70de0e699a68c9d2af0af55b24f7a065859dbc783f4307896cd24907e9c8413e441b5fc8265791d843926b7f4095f7204bfb04421265f41250200f150bd4b94582f9804a5ec2975816650d84808fcb7a41ff266e8a724a18215b0cef8be1d4d516f637fa557ca0b06e85756a93bc747adba2d27f8eda381b56d79c1695357130a3ad57fb36c39ab8598acc4f8fec7268a732b4d0def70a7ebbb8398fe826add2f0ac119b8f4fb0311bc1ee20d01fe22382c2c10e41699b64ffd20d8f115c82493a223dc658e1f42705d1531a55f1848ee58332f22b2eadbe84dede5c25064529c9f6d65097b8de16ea8439d9fa4d15ed5574d40eded313833712018d614cc8715c8f9e19051fd393d4f41f504b0708826261e37e040000ca090000504b01020a000a0000080000aa7b564e000000000000000000000000030004000000000000000000000000000000696f2ffeca0000504b01020a000a0000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e00000000000000000000000011000000000000000000000000004b000000696f2f6e756c732f636f6e74726163742f504b01020a000a0000080000aa7b564e00000000000000000000000017000000000000000000000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000",
  "args" : [ "io", "IO", 80000, 1 ]
}
```

### Example response data: 
```json
[ {
  "success" : true,
  "data" : {
    "success" : true
  }
}, "校验失败示例请参考[/api/contract/validate/call] - 验证调用合约" ]
```

4.13 验证调用合约
===========
Cmd: /api/contract/validate/call
--------------------------------
_**详细描述: 验证调用合约**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "value" : 0,
  "gasLimit" : 0,
  "price" : 0,
  "contractAddress" : null,
  "methodName" : null,
  "methodDesc" : null,
  "args" : null
}
```

参数列表
----
| 参数名                                                             |         参数类型         | 参数描述                       | 是否必填 |
| --------------------------------------------------------------- |:--------------------:| -------------------------- |:----:|
| 验证调用合约                                                          | contractvalidatecall | 验证调用合约表单                   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |        string        | 交易创建者                      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value           |         long         | 调用者向合约地址转入的主网资产金额，没有此业务时填0 |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit        |         long         | 最大gas消耗                    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price           |         long         | 执行合约单价                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |        string        | 智能合约地址                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |        string        | 方法名称                       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |        string        | 方法描述，若合约内方法没有重载，则此参数可以为空   |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |       object[]       | 参数列表                       |  否   |

返回值
---
| 字段名     |  字段类型   | 参数描述      |
| ------- |:-------:| --------- |
| success | boolean | 验证成功与否    |
| code    | string  | 验证失败的错误码  |
| msg     | string  | 验证失败的错误信息 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "value" : 0,
  "gasLimit" : 20000,
  "price" : 25,
  "contractAddress" : "tNULSeBaMx7J2im9edmmyZofHoTWW6nCTbvy3K",
  "methodName" : "transfer",
  "methodDesc" : null,
  "args" : [ "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", 990 ]
}
```

### Example response data: 
```json
[ {
  "success" : true,
  "data" : {
    "success" : true
  }
}, {
  "success" : true,
  "data" : {
    "msg" : "Data error;contract error - contract[tNULSeBaMx7J2im9edmmyZofHoTWW6nCTbvy3K] has stopped",
    "success" : false,
    "code" : "err_0014"
  }
} ]
```

4.14 验证删除合约
===========
Cmd: /api/contract/validate/delete
----------------------------------
_**详细描述: 验证删除合约**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "contractAddress" : null
}
```

参数列表
----
| 参数名                                                             |          参数类型          | 参数描述     | 是否必填 |
| --------------------------------------------------------------- |:----------------------:| -------- |:----:|
| 验证删除合约                                                          | contractvalidatedelete | 验证删除合约表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |         string         | 交易创建者    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |         string         | 智能合约地址   |  是   |

返回值
---
| 字段名     |  字段类型   | 参数描述      |
| ------- |:-------:| --------- |
| success | boolean | 验证成功与否    |
| code    | string  | 验证失败的错误码  |
| msg     | string  | 验证失败的错误信息 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "contractAddress" : "tNULSeBaNAKfKnLMR5XG5qtwXt5JS1b3QosZxg"
}
```

### Example response data: 
```json
[ {
  "success" : true,
  "data" : {
    "success" : true
  }
}, "校验失败示例请参考[/api/contract/validate/call] - 验证调用合约" ]
```

4.15 估算发布合约交易的GAS
=================
Cmd: /api/contract/imputedgas/create
------------------------------------
_**详细描述: 估算发布合约交易的GAS**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "contractCode" : null,
  "args" : null
}
```

参数列表
----
| 参数名                                                          |           参数类型           | 参数描述                 | 是否必填 |
| ------------------------------------------------------------ |:------------------------:| -------------------- |:----:|
| 估算发布合约交易的GAS                                                 | imputedgascontractcreate | 估算发布合约交易的GAS表单       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender       |          string          | 交易创建者                |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractCode |          string          | 智能合约代码(字节码的Hex编码字符串) |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args         |         object[]         | 参数列表                 |  否   |

返回值
---
| 字段名      | 字段类型 | 参数描述              |
| -------- |:----:| ----------------- |
| gasLimit | long | 消耗的gas值，执行失败返回数值1 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "contractCode" : "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbacf782dd58a888e49983a72c8e83821c367b8a1c382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe274703aa254cbe6691d5fe0bc8ea36087270e4f9a11551d916cb9588a8c999109f6312f864869d2c845f2850819dfc8f078e4f67e394e289db32c389413c98fdb36fac5c52f79e32c7cafb216aae57260ecb899a2f6b535fb6bb7f3a261cd7c5153772c0b2e706ce8d001be1c1edacf712a7f527a6bb82e6f86b2e54cc9529f59fdf55ab0ded9d69a78f749eb34149d5b29c22b7f812bcc3f55a6735bbdf7be1d916db7cef4dd3aa5a1bda68123ac4e524ec0cc5a54df5587bd544dd93d3f6172df861a306a1a0d16ec0f025ae83d26e71b53f9ec9451607ee761337e04f9c9868c7b85097943f71eabc51de8e637750c3ec49140039a842af8e3a049d8423d49187cfa8433d493c400d2b492b7f2ef2ecffb003fd699616ca3bc9d3349be697c2e8a5e8676416df90ec70635b9163b38eaf6067c173bf91c745677f1a911422d45beb98ad62fa8682376d750e4ff688ea2be05156dc6f76a280acc45945c50d110f6388afecd1d8bf834a3335476090d571088de40f0e80c4257d05815c3d1d81568d1f815f8a2cd812bf0471397b0b8397015fa552c915397d1780d4b05d0752cf3e3be1b587e54736666b0e21a9a2eba40fbb158fd060c30658bb11a2d6865296cc606a639c6246f645a0799d6434c729a491687b6d8401d8744ea57711049cac5a7a4ef739f5f491b95febb29b705b86d58797eeb4892e50189c51754212692441c8e5dc74a1fbec4aafd89af1064602e24e2ce54f340a02df015429c1449c21f505e75f2f72a887529313611650bc3bd8ec9bf8d798b33e4552f92ae1749ecc53e276dfb29f978ba1b07e8bf9f6797e0204f04f00327a1f6da56ae1de2cc52f86ea22588919b680ee2f0ac248bdf4b7c737c3d825127ef670823c0674f34c6142566d0128d715c1d8d736c95d4c54492ac05aa85daa2c0df8b46825e46b0cd84bc8e80c4b1a8adcf75ac07f7398ef5e087ca31910eaaf408b895fe9ae9e0bde5a4e3491e914cf6c4fe8960e01c02fe6b68932aaba4a6795fdc4ecdbeb8e4c0ef817884d11a453b2174d2f83aea1488115b9f07a21dfb7684713f81f9545417c31fdaa9dd446bad90063517e98ff0632798166d8ae6ae2863199728c6e24e143921f29c3836a9583d80101e64120d92d1980760970bb04b454e53d24105b0665a7fe22279d041d21e750cb7df404705c49ad92096aa9d134ce624bbcff200687701b4ab9ad494340f80075c00136c0151d15d01b0b602404d5c45671d1c59e2c8d1cdbc0747b78ba3dbc5d14d1febe2305c1c978943623c142586ae8aedfdf1c45544ce62859a60e53040e7a057b0b6cc83d5efe9f022a35c22ee32ab6d9a55f4302faf47d8e7a73d8d30e4621f72b10f31cd5b5487df89946a04a9b746e9e21e165ba896533ef914a42c4e3de1907584f545740381685b40bc4bc8108bb705e85d375daaf2acdd0d8fb2d01ea3d5c7b1923abad85955268ab83823187770469846bb61234ec3865821f72826f2c907af83e78c83a7d38327d1b46e06eb2b687a66a1b1cbfe296a7b1a2bf00ccbfe590f924e1749a78ba4d345d2e922697191582e927f7045740cd8b1f0f2b74dd66dce453610508b6dce1dd51688276c49507a59fc39ea7b5efe958836bc40067991fcfc126fa2973d88075cc4032ee20117f1808378156fb5e3e4f32a8bdb335b3923f90f816493248bf3147f42381e9de6aa448ba1bc8a6fed8fc69b36cce0b68a7b8db10423de5d85ee4df72b64ac577975bcc680bf4ee86f78ca72bd0b793d5b6e9ff30563b776031372bf82ec931f340e90a203a4cb01928837f54a862b207ae682b0b3fc16d59d65cdbdcd78bee301e025b7ec1c726b60ac2b00f8b3c701708c6f12cfd5bc075648d3b285dfacde0817dde60cab6def320aef29832df631d7e06a654653415f045fd34e4dd979c8b133e5dc371dd19804597843cabab62ddbc973d4f33eed7e407afcd0435c1daecd0e14944d918aee1543eb2d8ef59263dd74acb7d6f0327e0d7d73ddfc98ee7ce231d9ea9a6c75dc14a9acbe8be6383ced96984f79b1a68ec38959666d8f2f52d12542f894fe5cf6a4758d6b7e8debf11ac7639104887fb6ef2755c53c8c5f2ae53e2c278b3e4b36ece0f35534fe17504b0708ec308779cb09000028180000504b0304140008080800aa7b564e00000000000000000000000022000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373a552d14e1341143d03b5cb16aa28a8a0284a08697970131ff481a76a24694282a19507dfa6bb431d989da933b325fc9a0f7e801f65bc534ba5b6a5896eb23b3be79e7bcebd33f7c7cf6fdf01bcc64e84e711b623bc60a8367a3d6bfa5c7de80bed19569a5a0bfb5e71e7848bf092186dcbb53b1376c828699e0b86f55afde89cf779a2b8ee262d6fa5ee1e306c9e14dacb5c9c4a273b4a34b4369e7b69b463d83b9226d18572496ab4b73cf589cb2e123ee224a7525c9248d95de51da318963291ca9c2b4a5eacd59b0ccb9ea8aa55f47aea8a61e3ba869cfb2fc93bd96d6a2fbac29244dce154582a8ecf18ded4a61b37b2cc0ae70e668aec8c37f3915b6add0b3bd6d5fedcae4ec4d7425a9191e2921f1e26c3db39554d2faafe99eee85ae4d09a9ce1d33ca17fb489f86034e8ae63ae94b90cc7c9d0fe2fb799475d6999c2a6e2502af2a8b4cd85d0af0295616b42d0876832e030acfe19c2e3ceb9486942f76fcbd8fd6be06f278fcd7e99816101e1894b749788695fa15d99d66580b0952958157727b07b589dc0eee301a9dfc0b0466f144cd70784877844ff01ac2efc0647a1c7d81886d6166f864684cdd9b94ff0748aef388d085b83ef33dca135141a7c4ad46e488a10ff02504b070868fe421cca0100005e040000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c6173738d565b6f1b4514fec6bbde75b69bbb49d224b46929e05b6a2ee19a4b73218140d2943835b4406163af926d9cdd74771d90a0129540ea033c801020242e2f252f790089b80824c45390f82ffc03049c995dbb89e3243cf8ccd93367e67cdf7c6746fef39f5f7e03f018ae6988e34213148c7333c1cd248f4da998d63083e735bcc0cd0c66b937abe2450d2770218697f838c7cd7c0c176358e0ee256e5e56b1a822c71075deb64d97e1d49ce564ed72c9cb161cdb778d829ff58a6bd98962d1353d6f9841f5364cbbc853a39b46a96c3274cd5d37368decbae1af6627ad9559db37574c97529511cbb6fc3186cb89a3773d66b6e1f6c93c833ce5140940eb9c659b17cbebcba6bb642c9728d231e7148c52de702dfe1d06657fd5f2189a2736365c87b04f6f9ab6cfa0cfdac47caa64789e49d39983607c67cdb4b34bdc9edbb798389e5d2cdbbeb56ee62dcfa22a970cd758377dd39db06dc7377ccbb169cf546382462d27bb68de285bae59a41d632ba6bf10883190481e2747ccab659f39e694f98969b479ae2aa0e6edf9e065f381a03d54f6104979b930eb64e2706114a2639488f97d6152c9b057b20bcbd7cd823f9cbccac01c2ed28129219241a234e77ca3b0366f6c08eda8c1a9f4aae1ad06824b89e42c152156e5122533fa88f94ece772d7b85215e852f760ea2b4b39673ca6ec19cb1782f6842cdf33c4d471f967474a15b470f3727d1abe332f22a5e21e9fe7f37a87855c7237854c5151d4fe16986ee7a189365ab244e3bbe6fe5bbe2f28deab88ad7b8799d006606c28b364ae43203e2ae898c37740c6384a1adfef4187a1bea1ff6391d5a3e9059f43a43e7be7312413aa6fea308379aded362c4ab514b9054c60627c330983828cd41b5c2631aaecbaff6cf11f9fd89a9c3a771869ecc38a82b2121c215a78735c2451723e94ea346f37de8a7effbe9ab44a34c633c75172c95de412495d981941adc81fca358758a6c17a2646f50a68b267868818f4e94719aa2a9603d06f000203c5e97098f578e088fd796708efc4e89261f2467bf7d080f93e590b234f2e5d1d44f88fc50c3a088e03ba2a61e248435191248868bc7289b575404977b0c34117d8fd6dc143b74055935d44a889a2e444320523d90f71b0249370622d503f980d67c7808909e104806830d80c8f5406e3704725e2c3a0044ae07f211adf9f810205c335e98ae7cb8d75f9423d1782b95fe1e51793bbd8b965405d134fdee202a6da77f8732cf75dba58ea22143bf0ad4afd0ba45375ec4298b9f679020514cda9b205513e45db4f381627205b12db472ef0fa8f21664699b404882462f01053e818a4fe9e43e23989f13e52ff026be14b40602c0355ab7e89fc6e3824e1143148be009d1a0badac2fe46bf8a2715d6adb0b836cea9d32b1752ff393cc6e9805d734a306be26046da4ef7ad069c78580ac36f55e37210271a5a10efbb07bf43dc9eafe9367d43cdf32da6f0dd1e35a643d89d04fa193c4b5038d84128ede3ec5fbae511824b30c947e84718cbefffa445f49c862caed1b6bc51867ec5892b77a1773457484141a5b5a32df025eeb7073e616e6dbb594147059df58d77674fe30d8540231815760c67c3a746c173f42c35fd07504b0708ea7bbc798f040000e6090000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c6173738d56df531b5514fe6e76b31bb60be19740015b5aab8624b06ad156c36f84160da5051a0bd5da25d9c296b08bbb1b5e1c1ffa6ff8aebcf0a033923a3ae3f88433fe2ffe078e7aeedd854212c0879c7bf6dc73eef9cef9cebd933ffff9e53700efe189864e4c3441c12417535c4c73db8c8a590d73b8a3e12e177398e7dabc8a4f345cc244029ff235cfc54202f71258e4ea7d2e1ea85852b1cc203ff3dc6d862b79db359c4ad9378aae1378663130fcd29631552a7996efe7186281cb10df35cb158ba12bffdcdc358d6d33d834a6ed8d7927b0362c8fbc9451dbb183718687a9f30fbc60b7e1f18305823be3960840326f3bd6bdcaf6bae5ad98eb65b2b4e7dda2592e989ecdbf23a31c6cda3e43f38a673afe33cb9bddb59c80419f771ccb9b299bbe6fd176b61e4ce06e598eb1c2e58d53c154e3f5a58a13d8db56c1f66dca72dff4cc6d2bb0bc29c7710333b05d87ce4c372ed03cf63196acaf2ab66795e84475c30ae6040d03a9c18b8850fd23e76b17f498f72b4e47af70e6fc704dd0772124b187729d4163c23ff6ba9c3a9b0c854a30cb54ed6b9153d974368cc5f5e75631c80dae3130971353b72588318988e6e5c02c6e2d983b822f1a674abd69fa9b21c9526a709e92502d95323933fa4804ee72e0d9ce0643e7117c717268a593b565b7e215ad399bf3af090687b99b8e3eace8e842b78e1e2e2ea357c74314547c4674fdff0950f148c73b7857c5aa8e5bb8cdd05d0b63ba62974b96c7d0712af26b7ed5c674ace131179f132bd981c01da3b2b203e26689cd2f74e430cad05adb3786de867c47534ded2a84048bc9a6eca73a248cd4a0fef34a6db47d62a4a8eb8d8681483277762ca7c43094aa27a59ea7a841b91affa3c939c7bf3f3573f636aed1d3d8099a47488871aee9018d71bac54a8cd3aad17e1ffae9fb75fa2ad32ad3da997e0996ce1c2096ce1e404a0f1d40fe51445d21d98538c92c790ea109c36881810e1a82ab644d87f118c01b80d0785e26349e3926349e5bc20dd293120986374fc8b7f036490ec7a09587c6d33f21f6c3717e45186f8a7c7ae810e5634861300a1e276f9e4d1175bc42af09eb2d8ab92d4ee80abd8e112b1162ba060d8148b540720d81641a03916a814c50cce419407a2220596a743d10b916c84c4320c322a80e885c0be40ec5dc3d0308e78b27a68b1e9df517f970ea5ea433df232eef670ed192ae229ea1df77884bfb99dfa12c70de0e699a68c9d2af0af55b24f7a065859dbc783f4307896cd24907e9c8413e441b5fc8265791d843926b7f4095f7204bfb04421265f41250200f150bd4b94582f9804a5ec2975816650d84808fcb7a41ff266e8a724a18215b0cef8be1d4d516f637fa557ca0b06e85756a93bc747adba2d27f8eda381b56d79c1695357130a3ad57fb36c39ab8598acc4f8fec7268a732b4d0def70a7ebbb8398fe826add2f0ac119b8f4fb0311bc1ee20d01fe22382c2c10e41699b64ffd20d8f115c82493a223dc658e1f42705d1531a55f1848ee58332f22b2eadbe84dede5c25064529c9f6d65097b8de16ea8439d9fa4d15ed5574d40eded313833712018d614cc8715c8f9e19051fd393d4f41f504b0708826261e37e040000ca090000504b01020a000a0000080000aa7b564e000000000000000000000000030004000000000000000000000000000000696f2ffeca0000504b01020a000a0000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e00000000000000000000000011000000000000000000000000004b000000696f2f6e756c732f636f6e74726163742f504b01020a000a0000080000aa7b564e00000000000000000000000017000000000000000000000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000",
  "args" : [ "io", "IO", 80000, 1 ]
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "gasLimit" : 20143
  }
}
```

4.16 估算调用合约交易的GAS
=================
Cmd: /api/contract/imputedgas/call
----------------------------------
_**详细描述: 估算调用合约交易的GAS**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "value" : null,
  "contractAddress" : null,
  "methodName" : null,
  "methodDesc" : null,
  "args" : null
}
```

参数列表
----
| 参数名                                                             |          参数类型          | 参数描述                       | 是否必填 |
| --------------------------------------------------------------- |:----------------------:| -------------------------- |:----:|
| 估算调用合约交易的GAS                                                    | imputedgascontractcall | 估算调用合约交易的GAS表单             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |         string         | 交易创建者                      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value           |       biginteger       | 调用者向合约地址转入的主网资产金额，没有此业务时填0 |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |         string         | 智能合约地址                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |         string         | 方法名称                       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |         string         | 方法描述，若合约内方法没有重载，则此参数可以为空   |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |        object[]        | 参数列表                       |  否   |

返回值
---
| 字段名      | 字段类型 | 参数描述              |
| -------- |:----:| ----------------- |
| gasLimit | long | 消耗的gas值，执行失败返回数值1 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "value" : 0,
  "contractAddress" : "tNULSeBaNAKfKnLMR5XG5qtwXt5JS1b3QosZxg",
  "methodName" : "transfer",
  "methodDesc" : null,
  "args" : [ "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", 990 ]
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "gasLimit" : 17538
  }
}
```

4.17 调用合约不上链方法
==============
Cmd: /api/contract/view
-----------------------
_**详细描述: 调用合约不上链方法**_
### HttpMethod: POST

### Form json data: 
```json
{
  "contractAddress" : null,
  "methodName" : null,
  "methodDesc" : null,
  "args" : null
}
```

参数列表
----
| 参数名                                                             |       参数类型       | 参数描述                     | 是否必填 |
| --------------------------------------------------------------- |:----------------:| ------------------------ |:----:|
| 调用合约不上链方法                                                       | contractviewcall | 调用合约不上链方法表单              |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |      string      | 智能合约地址                   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |      string      | 方法名称                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |      string      | 方法描述，若合约内方法没有重载，则此参数可以为空 |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |     object[]     | 参数列表                     |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述      |
| ------ |:------:| --------- |
| result | string | 视图方法的调用结果 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "contractAddress" : "tNULSeBaNAKfKnLMR5XG5qtwXt5JS1b3QosZxg",
  "methodName" : "balanceOf",
  "methodDesc" : null,
  "args" : [ "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD" ]
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "result" : "8000"
  }
}
```

4.18 离线组装 - 发布合约的交易
===================
Cmd: /api/contract/create/offline
---------------------------------
_**详细描述: 离线组装 - 发布合约的交易**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "senderBalance" : null,
  "nonce" : null,
  "alias" : null,
  "contractCode" : null,
  "gasLimit" : 0,
  "args" : null,
  "argsType" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                           |         参数类型          | 参数描述                 | 是否必填 |
| ------------------------------------------------------------- |:---------------------:| -------------------- |:----:|
| 发布合约离线交易                                                      | contractcreateoffline | 发布合约离线交易表单           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender        |        string         | 交易创建者                |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;senderBalance |      biginteger       | 账户余额                 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce         |        string         | 账户nonce值             |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias         |        string         | 合约别名                 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractCode  |        string         | 智能合约代码(字节码的Hex编码字符串) |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit      |         long          | GAS限制                |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args          |       object[]        | 参数列表                 |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;argsType      |       string[]        | 参数类型列表               |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark        |        string         | 备注                   |  否   |

返回值
---
| 字段名             |  字段类型  | 参数描述     |
| --------------- |:------:| -------- |
| hash            | string | 交易hash   |
| txHex           | string | 交易序列化字符串 |
| contractAddress | string | 生成的合约地址  |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "alias" : "rf_nrc20_offline",
  "senderBalance" : "999999998523475",
  "nonce" : "9c0aea02bed90ddd",
  "contractCode" : "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbacf782dd58a888e49983a72c8e83821c367b8a1c382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe274703aa254cbe6691d5fe0bc8ea36087270e4f9a11551d916cb9588a8c999109f6312f864869d2c845f2850819dfc8f078e4f67e394e289db32c389413c98fdb36fac5c52f79e32c7cafb216aae57260ecb899a2f6b535fb6bb7f3a261cd7c5153772c0b2e706ce8d001be1c1edacf712a7f527a6bb82e6f86b2e54cc9529f59fdf55ab0ded9d69a78f749eb34149d5b29c22b7f812bcc3f55a6735bbdf7be1d916db7cef4dd3aa5a1bda68123ac4e524ec0cc5a54df5587bd544dd93d3f6172df861a306a1a0d16ec0f025ae83d26e71b53f9ec9451607ee761337e04f9c9868c7b85097943f71eabc51de8e637750c3ec49140039a842af8e3a049d8423d49187cfa8433d493c400d2b492b7f2ef2ecffb003fd699616ca3bc9d3349be697c2e8a5e8676416df90ec70635b9163b38eaf6067c173bf91c745677f1a911422d45beb98ad62fa8682376d750e4ff688ea2be05156dc6f76a280acc45945c50d110f6388afecd1d8bf834a3335476090d571088de40f0e80c4257d05815c3d1d81568d1f815f8a2cd812bf0471397b0b8397015fa552c915397d1780d4b05d0752cf3e3be1b587e54736666b0e21a9a2eba40fbb158fd060c30658bb11a2d6865296cc606a639c6246f645a0799d6434c729a491687b6d8401d8744ea57711049cac5a7a4ef739f5f491b95febb29b705b86d58797eeb4892e50189c51754212692441c8e5dc74a1fbec4aafd89af1064602e24e2ce54f340a02df015429c1449c21f505e75f2f72a887529313611650bc3bd8ec9bf8d798b33e4552f92ae1749ecc53e276dfb29f978ba1b07e8bf9f6797e0204f04f00327a1f6da56ae1de2cc52f86ea22588919b680ee2f0ac248bdf4b7c737c3d825127ef670823c0674f34c6142566d0128d715c1d8d736c95d4c54492ac05aa85daa2c0df8b46825e46b0cd84bc8e80c4b1a8adcf75ac07f7398ef5e087ca31910eaaf408b895fe9ae9e0bde5a4e3491e914cf6c4fe8960e01c02fe6b68932aaba4a6795fdc4ecdbeb8e4c0ef817884d11a453b2174d2f83aea1488115b9f07a21dfb7684713f81f9545417c31fdaa9dd446bad90063517e98ff0632798166d8ae6ae2863199728c6e24e143921f29c3836a9583d80101e64120d92d1980760970bb04b454e53d24105b0665a7fe22279d041d21e750cb7df404705c49ad92096aa9d134ce624bbcff200687701b4ab9ad494340f80075c00136c0151d15d01b0b602404d5c45671d1c59e2c8d1cdbc0747b78ba3dbc5d14d1febe2305c1c978943623c142586ae8aedfdf1c45544ce62859a60e53040e7a057b0b6cc83d5efe9f022a35c22ee32ab6d9a55f4302faf47d8e7a73d8d30e4621f72b10f31cd5b5487df89946a04a9b746e9e21e165ba896533ef914a42c4e3de1907584f545740381685b40bc4bc8108bb705e85d375daaf2acdd0d8fb2d01ea3d5c7b1923abad85955268ab83823187770469846bb61234ec3865821f72826f2c907af83e78c83a7d38327d1b46e06eb2b687a66a1b1cbfe296a7b1a2bf00ccbfe590f924e1749a78ba4d345d2e922697191582e927f7045740cd8b1f0f2b74dd66dce453610508b6dce1dd51688276c49507a59fc39ea7b5efe958836bc40067991fcfc126fa2973d88075cc4032ee20117f1808378156fb5e3e4f32a8bdb335b3923f90f816493248bf3147f42381e9de6aa448ba1bc8a6fed8fc69b36cce0b68a7b8db10423de5d85ee4df72b64ac577975bcc680bf4ee86f78ca72bd0b793d5b6e9ff30563b776031372bf82ec931f340e90a203a4cb01928837f54a862b207ae682b0b3fc16d59d65cdbdcd78bee301e025b7ec1c726b60ac2b00f8b3c701708c6f12cfd5bc075648d3b285dfacde0817dde60cab6def320aef29832df631d7e06a654653415f045fd34e4dd979c8b133e5dc371dd19804597843cabab62ddbc973d4f33eed7e407afcd0435c1daecd0e14944d918aee1543eb2d8ef59263dd74acb7d6f0327e0d7d73ddfc98ee7ce231d9ea9a6c75dc14a9acbe8be6383ced96984f79b1a68ec38959666d8f2f52d12542f894fe5cf6a4758d6b7e8debf11ac7639104887fb6ef2755c53c8c5f2ae53e2c278b3e4b36ece0f35534fe17504b0708ec308779cb09000028180000504b0304140008080800aa7b564e00000000000000000000000022000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373a552d14e1341143d03b5cb16aa28a8a0284a08697970131ff481a76a24694282a19507dfa6bb431d989da933b325fc9a0f7e801f65bc534ba5b6a5896eb23b3be79e7bcebd33f7c7cf6fdf01bcc64e84e711b623bc60a8367a3d6bfa5c7de80bed19569a5a0bfb5e71e7848bf092186dcbb53b1376c828699e0b86f55afde89cf779a2b8ee262d6fa5ee1e306c9e14dacb5c9c4a273b4a34b4369e7b69b463d83b9226d18572496ab4b73cf589cb2e123ee224a7525c9248d95de51da318963291ca9c2b4a5eacd59b0ccb9ea8aa55f47aea8a61e3ba869cfb2fc93bd96d6a2fbac29244dce154582a8ecf18ded4a61b37b2cc0ae70e668aec8c37f3915b6add0b3bd6d5fedcae4ec4d7425a9191e2921f1e26c3db39554d2faafe99eee85ae4d09a9ce1d33ca17fb489f86034e8ae63ae94b90cc7c9d0fe2fb799475d6999c2a6e2502af2a8b4cd85d0af0295616b42d0876832e030acfe19c2e3ceb9486942f76fcbd8fd6be06f278fcd7e99816101e1894b749788695fa15d99d66580b0952958157727b07b589dc0eee301a9dfc0b0466f144cd70784877844ff01ac2efc0647a1c7d81886d6166f864684cdd9b94ff0748aef388d085b83ef33dca135141a7c4ad46e488a10ff02504b070868fe421cca0100005e040000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c6173738d565b6f1b4514fec6bbde75b69bbb49d224b46929e05b6a2ee19a4b73218140d2943835b4406163af926d9cdd74771d90a0129540ea033c801020242e2f252f790089b80824c45390f82ffc03049c995dbb89e3243cf8ccd93367e67cdf7c6746fef39f5f7e03f018ae6988e34213148c7333c1cd248f4da998d63083e735bcc0cd0c66b937abe2450d2770218697f838c7cd7c0c176358e0ee256e5e56b1a822c71075deb64d97e1d49ce564ed72c9cb161cdb778d829ff58a6bd98962d1353d6f9841f5364cbbc853a39b46a96c3274cd5d37368decbae1af6627ad9559db37574c97529511cbb6fc3186cb89a3773d66b6e1f6c93c833ce5140940eb9c659b17cbebcba6bb642c9728d231e7148c52de702dfe1d06657fd5f2189a2736365c87b04f6f9ab6cfa0cfdac47caa64789e49d39983607c67cdb4b34bdc9edbb798389e5d2cdbbeb56ee62dcfa22a970cd758377dd39db06dc7377ccbb169cf546382462d27bb68de285bae59a41d632ba6bf10883190481e2747ccab659f39e694f98969b479ae2aa0e6edf9e065f381a03d54f6104979b930eb64e2706114a2639488f97d6152c9b057b20bcbd7cd823f9cbccac01c2ed28129219241a234e77ca3b0366f6c08eda8c1a9f4aae1ad06824b89e42c152156e5122533fa88f94ece772d7b85215e852f760ea2b4b39673ca6ec19cb1782f6842cdf33c4d471f967474a15b470f3727d1abe332f22a5e21e9fe7f37a87855c7237854c5151d4fe16986ee7a189365ab244e3bbe6fe5bbe2f28deab88ad7b8799d006606c28b364ae43203e2ae898c37740c6384a1adfef4187a1bea1ff6391d5a3e9059f43a43e7be7312413aa6fea308379aded362c4ab514b9054c60627c330983828cd41b5c2631aaecbaff6cf11f9fd89a9c3a771869ecc38a82b2121c215a78735c2451723e94ea346f37de8a7effbe9ab44a34c633c75172c95de412495d981941adc81fca358758a6c17a2646f50a68b267868818f4e94719aa2a9603d06f000203c5e97098f578e088fd796708efc4e89261f2467bf7d080f93e590b234f2e5d1d44f88fc50c3a088e03ba2a61e248435191248868bc7289b575404977b0c34117d8fd6dc143b74055935d44a889a2e444320523d90f71b0249370622d503f980d67c7808909e104806830d80c8f5406e3704725e2c3a0044ae07f211adf9f810205c335e98ae7cb8d75f9423d1782b95fe1e51793bbd8b965405d134fdee202a6da77f8732cf75dba58ea22143bf0ad4afd0ba45375ec4298b9f679020514cda9b205513e45db4f381627205b12db472ef0fa8f21664699b404882462f01053e818a4fe9e43e23989f13e52ff026be14b40602c0355ab7e89fc6e3824e1143148be009d1a0badac2fe46bf8a2715d6adb0b836cea9d32b1752ff393cc6e9805d734a306be26046da4ef7ad069c78580ac36f55e37210271a5a10efbb07bf43dc9eafe9367d43cdf32da6f0dd1e35a643d89d04fa193c4b5038d84128ede3ec5fbae511824b30c947e84718cbefffa445f49c862caed1b6bc51867ec5892b77a1773457484141a5b5a32df025eeb7073e616e6dbb594147059df58d77674fe30d8540231815760c67c3a746c173f42c35fd07504b0708ea7bbc798f040000e6090000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c6173738d56df531b5514fe6e76b31bb60be19740015b5aab8624b06ad156c36f84160da5051a0bd5da25d9c296b08bbb1b5e1c1ffa6ff8aebcf0a033923a3ae3f88433fe2ffe078e7aeedd854212c0879c7bf6dc73eef9cef9cebd933ffff9e53700efe189864e4c3441c12417535c4c73db8c8a590d73b8a3e12e177398e7dabc8a4f345cc244029ff235cfc54202f71258e4ea7d2e1ea85852b1cc203ff3dc6d862b79db359c4ad9378aae1378663130fcd29631552a7996efe7186281cb10df35cb158ba12bffdcdc358d6d33d834a6ed8d7927b0362c8fbc9451dbb183718687a9f30fbc60b7e1f18305823be3960840326f3bd6bdcaf6bae5ad98eb65b2b4e7dda2592e989ecdbf23a31c6cda3e43f38a673afe33cb9bddb59c80419f771ccb9b299bbe6fd176b61e4ce06e598eb1c2e58d53c154e3f5a58a13d8db56c1f66dca72dff4cc6d2bb0bc29c7710333b05d87ce4c372ed03cf63196acaf2ab66795e84475c30ae6040d03a9c18b8850fd23e76b17f498f72b4e47af70e6fc704dd0772124b187729d4163c23ff6ba9c3a9b0c854a30cb54ed6b9153d974368cc5f5e75631c80dae3130971353b72588318988e6e5c02c6e2d983b822f1a674abd69fa9b21c9526a709e92502d95323933fa4804ee72e0d9ce0643e7117c717268a593b565b7e215ad399bf3af090687b99b8e3eace8e842b78e1e2e2ea357c74314547c4674fdff0950f148c73b7857c5aa8e5bb8cdd05d0b63ba62974b96c7d0712af26b7ed5c674ace131179f132bd981c01da3b2b203e26689cd2f74e430cad05adb3786de867c47534ded2a84048bc9a6eca73a248cd4a0fef34a6db47d62a4a8eb8d8681483277762ca7c43094aa27a59ea7a841b91affa3c939c7bf3f3573f636aed1d3d8099a47488871aee9018d71bac54a8cd3aad17e1ffae9fb75fa2ad32ad3da997e0996ce1c2096ce1e404a0f1d40fe51445d21d98538c92c790ea109c36881810e1a82ab644d87f118c01b80d0785e26349e3926349e5bc20dd293120986374fc8b7f036490ec7a09587c6d33f21f6c3717e45186f8a7c7ae810e5634861300a1e276f9e4d1175bc42af09eb2d8ab92d4ee80abd8e112b1162ba060d8148b540720d81641a03916a814c50cce419407a2220596a743d10b916c84c4320c322a80e885c0be40ec5dc3d0308e78b27a68b1e9df517f970ea5ea433df232eef670ed192ae229ea1df77884bfb99dfa12c70de0e699a68c9d2af0af55b24f7a065859dbc783f4307896cd24907e9c8413e441b5fc8265791d843926b7f4095f7204bfb04421265f41250200f150bd4b94582f9804a5ec2975816650d84808fcb7a41ff266e8a724a18215b0cef8be1d4d516f637fa557ca0b06e85756a93bc747adba2d27f8eda381b56d79c1695357130a3ad57fb36c39ab8598acc4f8fec7268a732b4d0def70a7ebbb8398fe826add2f0ac119b8f4fb0311bc1ee20d01fe22382c2c10e41699b64ffd20d8f115c82493a223dc658e1f42705d1531a55f1848ee58332f22b2eadbe84dede5c25064529c9f6d65097b8de16ea8439d9fa4d15ed5574d40eded313833712018d614cc8715c8f9e19051fd393d4f41f504b0708826261e37e040000ca090000504b01020a000a0000080000aa7b564e000000000000000000000000030004000000000000000000000000000000696f2ffeca0000504b01020a000a0000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e00000000000000000000000011000000000000000000000000004b000000696f2f6e756c732f636f6e74726163742f504b01020a000a0000080000aa7b564e00000000000000000000000017000000000000000000000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000",
  "gasLimit" : 20245,
  "args" : [ "air", "AIR", 10000, 2 ],
  "argsType" : [ "String", "String", "BigInteger", "int" ],
  "remark" : "() return void"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "0f008629375d0e28292072657475726e20766f6964fd8119020001f7ec6473df12e751d64cf20a8baa7edd50810f810200020d2f73cb93099a8cfd0cbdd060155abfe2f50917fd1a19504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbacf782dd58a888e49983a72c8e83821c367b8a1c382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe274703aa254cbe6691d5fe0bc8ea36087270e4f9a11551d916cb9588a8c999109f6312f864869d2c845f2850819dfc8f078e4f67e394e289db32c389413c98fdb36fac5c52f79e32c7cafb216aae57260ecb899a2f6b535fb6bb7f3a261cd7c5153772c0b2e706ce8d001be1c1edacf712a7f527a6bb82e6f86b2e54cc9529f59fdf55ab0ded9d69a78f749eb34149d5b29c22b7f812bcc3f55a6735bbdf7be1d916db7cef4dd3aa5a1bda68123ac4e524ec0cc5a54df5587bd544dd93d3f6172df861a306a1a0d16ec0f025ae83d26e71b53f9ec9451607ee761337e04f9c9868c7b85097943f71eabc51de8e637750c3ec49140039a842af8e3a049d8423d49187cfa8433d493c400d2b492b7f2ef2ecffb003fd699616ca3bc9d3349be697c2e8a5e8676416df90ec70635b9163b38eaf6067c173bf91c745677f1a911422d45beb98ad62fa8682376d750e4ff688ea2be05156dc6f76a280acc45945c50d110f6388afecd1d8bf834a3335476090d571088de40f0e80c4257d05815c3d1d81568d1f815f8a2cd812bf0471397b0b8397015fa552c915397d1780d4b05d0752cf3e3be1b587e54736666b0e21a9a2eba40fbb158fd060c30658bb11a2d6865296cc606a639c6246f645a0799d6434c729a491687b6d8401d8744ea57711049cac5a7a4ef739f5f491b95febb29b705b86d58797eeb4892e50189c51754212692441c8e5dc74a1fbec4aafd89af1064602e24e2ce54f340a02df015429c1449c21f505e75f2f72a887529313611650bc3bd8ec9bf8d798b33e4552f92ae1749ecc53e276dfb29f978ba1b07e8bf9f6797e0204f04f00327a1f6da56ae1de2cc52f86ea22588919b680ee2f0ac248bdf4b7c737c3d825127ef670823c0674f34c6142566d0128d715c1d8d736c95d4c54492ac05aa85daa2c0df8b46825e46b0cd84bc8e80c4b1a8adcf75ac07f7398ef5e087ca31910eaaf408b895fe9ae9e0bde5a4e3491e914cf6c4fe8960e01c02fe6b68932aaba4a6795fdc4ecdbeb8e4c0ef817884d11a453b2174d2f83aea1488115b9f07a21dfb7684713f81f9545417c31fdaa9dd446bad90063517e98ff0632798166d8ae6ae2863199728c6e24e143921f29c3836a9583d80101e64120d92d1980760970bb04b454e53d24105b0665a7fe22279d041d21e750cb7df404705c49ad92096aa9d134ce624bbcff200687701b4ab9ad494340f80075c00136c0151d15d01b0b602404d5c45671d1c59e2c8d1cdbc0747b78ba3dbc5d14d1febe2305c1c978943623c142586ae8aedfdf1c45544ce62859a60e53040e7a057b0b6cc83d5efe9f022a35c22ee32ab6d9a55f4302faf47d8e7a73d8d30e4621f72b10f31cd5b5487df89946a04a9b746e9e21e165ba896533ef914a42c4e3de1907584f545740381685b40bc4bc8108bb705e85d375daaf2acdd0d8fb2d01ea3d5c7b1923abad85955268ab83823187770469846bb61234ec3865821f72826f2c907af83e78c83a7d38327d1b46e06eb2b687a66a1b1cbfe296a7b1a2bf00ccbfe590f924e1749a78ba4d345d2e922697191582e927f7045740cd8b1f0f2b74dd66dce453610508b6dce1dd51688276c49507a59fc39ea7b5efe958836bc40067991fcfc126fa2973d88075cc4032ee20117f1808378156fb5e3e4f32a8bdb335b3923f90f816493248bf3147f42381e9de6aa448ba1bc8a6fed8fc69b36cce0b68a7b8db10423de5d85ee4df72b64ac577975bcc680bf4ee86f78ca72bd0b793d5b6e9ff30563b776031372bf82ec931f340e90a203a4cb01928837f54a862b207ae682b0b3fc16d59d65cdbdcd78bee301e025b7ec1c726b60ac2b00f8b3c701708c6f12cfd5bc075648d3b285dfacde0817dde60cab6def320aef29832df631d7e06a654653415f045fd34e4dd979c8b133e5dc371dd19804597843cabab62ddbc973d4f33eed7e407afcd0435c1daecd0e14944d918aee1543eb2d8ef59263dd74acb7d6f0327e0d7d73ddfc98ee7ce231d9ea9a6c75dc14a9acbe8be6383ced96984f79b1a68ec38959666d8f2f52d12542f894fe5cf6a4758d6b7e8debf11ac7639104887fb6ef2755c53c8c5f2ae53e2c278b3e4b36ece0f35534fe17504b0708ec308779cb09000028180000504b0304140008080800aa7b564e00000000000000000000000022000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373a552d14e1341143d03b5cb16aa28a8a0284a08697970131ff481a76a24694282a19507dfa6bb431d989da933b325fc9a0f7e801f65bc534ba5b6a5896eb23b3be79e7bcebd33f7c7cf6fdf01bcc64e84e711b623bc60a8367a3d6bfa5c7de80bed19569a5a0bfb5e71e7848bf092186dcbb53b1376c828699e0b86f55afde89cf779a2b8ee262d6fa5ee1e306c9e14dacb5c9c4a273b4a34b4369e7b69b463d83b9226d18572496ab4b73cf589cb2e123ee224a7525c9248d95de51da318963291ca9c2b4a5eacd59b0ccb9ea8aa55f47aea8a61e3ba869cfb2fc93bd96d6a2fbac29244dce154582a8ecf18ded4a61b37b2cc0ae70e668aec8c37f3915b6add0b3bd6d5fedcae4ec4d7425a9191e2921f1e26c3db39554d2faafe99eee85ae4d09a9ce1d33ca17fb489f86034e8ae63ae94b90cc7c9d0fe2fb799475d6999c2a6e2502af2a8b4cd85d0af0295616b42d0876832e030acfe19c2e3ceb9486942f76fcbd8fd6be06f278fcd7e99816101e1894b749788695fa15d99d66580b0952958157727b07b589dc0eee301a9dfc0b0466f144cd70784877844ff01ac2efc0647a1c7d81886d6166f864684cdd9b94ff0748aef388d085b83ef33dca135141a7c4ad46e488a10ff02504b070868fe421cca0100005e040000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c6173738d565b6f1b4514fec6bbde75b69bbb49d224b46929e05b6a2ee19a4b73218140d2943835b4406163af926d9cdd74771d90a0129540ea033c801020242e2f252f790089b80824c45390f82ffc03049c995dbb89e3243cf8ccd93367e67cdf7c6746fef39f5f7e03f018ae6988e34213148c7333c1cd248f4da998d63083e735bcc0cd0c66b937abe2450d2770218697f838c7cd7c0c176358e0ee256e5e56b1a822c71075deb64d97e1d49ce564ed72c9cb161cdb778d829ff58a6bd98962d1353d6f9841f5364cbbc853a39b46a96c3274cd5d37368decbae1af6627ad9559db37574c97529511cbb6fc3186cb89a3773d66b6e1f6c93c833ce5140940eb9c659b17cbebcba6bb642c9728d231e7148c52de702dfe1d06657fd5f2189a2736365c87b04f6f9ab6cfa0cfdac47caa64789e49d39983607c67cdb4b34bdc9edbb798389e5d2cdbbeb56ee62dcfa22a970cd758377dd39db06dc7377ccbb169cf546382462d27bb68de285bae59a41d632ba6bf10883190481e2747ccab659f39e694f98969b479ae2aa0e6edf9e065f381a03d54f6104979b930eb64e2706114a2639488f97d6152c9b057b20bcbd7cd823f9cbccac01c2ed28129219241a234e77ca3b0366f6c08eda8c1a9f4aae1ad06824b89e42c152156e5122533fa88f94ece772d7b85215e852f760ea2b4b39673ca6ec19cb1782f6842cdf33c4d471f967474a15b470f3727d1abe332f22a5e21e9fe7f37a87855c7237854c5151d4fe16986ee7a189365ab244e3bbe6fe5bbe2f28deab88ad7b8799d006606c28b364ae43203e2ae898c37740c6384a1adfef4187a1bea1ff6391d5a3e9059f43a43e7be7312413aa6fea308379aded362c4ab514b9054c60627c330983828cd41b5c2631aaecbaff6cf11f9fd89a9c3a771869ecc38a82b2121c215a78735c2451723e94ea346f37de8a7effbe9ab44a34c633c75172c95de412495d981941adc81fca358758a6c17a2646f50a68b267868818f4e94719aa2a9603d06f000203c5e97098f578e088fd796708efc4e89261f2467bf7d080f93e590b234f2e5d1d44f88fc50c3a088e03ba2a61e248435191248868bc7289b575404977b0c34117d8fd6dc143b74055935d44a889a2e444320523d90f71b0249370622d503f980d67c7808909e104806830d80c8f5406e3704725e2c3a0044ae07f211adf9f810205c335e98ae7cb8d75f9423d1782b95fe1e51793bbd8b965405d134fdee202a6da77f8732cf75dba58ea22143bf0ad4afd0ba45375ec4298b9f679020514cda9b205513e45db4f381627205b12db472ef0fa8f21664699b404882462f01053e818a4fe9e43e23989f13e52ff026be14b40602c0355ab7e89fc6e3824e1143148be009d1a0badac2fe46bf8a2715d6adb0b836cea9d32b1752ff393cc6e9805d734a306be26046da4ef7ad069c78580ac36f55e37210271a5a10efbb07bf43dc9eafe9367d43cdf32da6f0dd1e35a643d89d04fa193c4b5038d84128ede3ec5fbae511824b30c947e84718cbefffa445f49c862caed1b6bc51867ec5892b77a1773457484141a5b5a32df025eeb7073e616e6dbb594147059df58d77674fe30d8540231815760c67c3a746c173f42c35fd07504b0708ea7bbc798f040000e6090000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c6173738d56df531b5514fe6e76b31bb60be19740015b5aab8624b06ad156c36f84160da5051a0bd5da25d9c296b08bbb1b5e1c1ffa6ff8aebcf0a033923a3ae3f88433fe2ffe078e7aeedd854212c0879c7bf6dc73eef9cef9cebd933ffff9e53700efe189864e4c3441c12417535c4c73db8c8a590d73b8a3e12e177398e7dabc8a4f345cc244029ff235cfc54202f71258e4ea7d2e1ea85852b1cc203ff3dc6d862b79db359c4ad9378aae1378663130fcd29631552a7996efe7186281cb10df35cb158ba12bffdcdc358d6d33d834a6ed8d7927b0362c8fbc9451dbb183718687a9f30fbc60b7e1f18305823be3960840326f3bd6bdcaf6bae5ad98eb65b2b4e7dda2592e989ecdbf23a31c6cda3e43f38a673afe33cb9bddb59c80419f771ccb9b299bbe6fd176b61e4ce06e598eb1c2e58d53c154e3f5a58a13d8db56c1f66dca72dff4cc6d2bb0bc29c7710333b05d87ce4c372ed03cf63196acaf2ab66795e84475c30ae6040d03a9c18b8850fd23e76b17f498f72b4e47af70e6fc704dd0772124b187729d4163c23ff6ba9c3a9b0c854a30cb54ed6b9153d974368cc5f5e75631c80dae3130971353b72588318988e6e5c02c6e2d983b822f1a674abd69fa9b21c9526a709e92502d95323933fa4804ee72e0d9ce0643e7117c717268a593b565b7e215ad399bf3af090687b99b8e3eace8e842b78e1e2e2ea357c74314547c4674fdff0950f148c73b7857c5aa8e5bb8cdd05d0b63ba62974b96c7d0712af26b7ed5c674ace131179f132bd981c01da3b2b203e26689cd2f74e430cad05adb3786de867c47534ded2a84048bc9a6eca73a248cd4a0fef34a6db47d62a4a8eb8d8681483277762ca7c43094aa27a59ea7a841b91affa3c939c7bf3f3573f636aed1d3d8099a47488871aee9018d71bac54a8cd3aad17e1ffae9fb75fa2ad32ad3da997e0996ce1c2096ce1e404a0f1d40fe51445d21d98538c92c790ea109c36881810e1a82ab644d87f118c01b80d0785e26349e3926349e5bc20dd293120986374fc8b7f036490ec7a09587c6d33f21f6c3717e45186f8a7c7ae810e5634861300a1e276f9e4d1175bc42af09eb2d8ab92d4ee80abd8e112b1162ba060d8148b540720d81641a03916a814c50cce419407a2220596a743d10b916c84c4320c322a80e885c0be40ec5dc3d0308e78b27a68b1e9df517f970ea5ea433df232eef670ed192ae229ea1df77884bfb99dfa12c70de0e699a68c9d2af0af55b24f7a065859dbc783f4307896cd24907e9c8413e441b5fc8265791d843926b7f4095f7204bfb04421265f41250200f150bd4b94582f9804a5ec2975816650d84808fcb7a41ff266e8a724a18215b0cef8be1d4d516f637fa557ca0b06e85756a93bc747adba2d27f8eda381b56d79c1695357130a3ad57fb36c39ab8598acc4f8fec7268a732b4d0def70a7ebbb8398fe826add2f0ac119b8f4fb0311bc1ee20d01fe22382c2c10e41699b64ffd20d8f115c82493a223dc658e1f42705d1531a55f1848ee58332f22b2eadbe84dede5c25064529c9f6d65097b8de16ea8439d9fa4d15ed5574d40eded313833712018d614cc8715c8f9e19051fd393d4f41f504b0708826261e37e040000ca090000504b01020a000a0000080000aa7b564e000000000000000000000000030004000000000000000000000000000000696f2ffeca0000504b01020a000a0000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e00000000000000000000000011000000000000000000000000004b000000696f2f6e756c732f636f6e74726163742f504b01020a000a0000080000aa7b564e00000000000000000000000017000000000000000000000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b316000000001072665f6e726332305f6f66666c696e65154f0000000000001900000000000000040103616972010341495201053130303030010132480117020001f7ec6473df12e751d64cf20a8baa7edd50810f81020001006d67120000000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000000",
    "contractAddress" : "tNULSeBaMwYiR4p1X9xNJPiyJfrXjr4KgkcFjG",
    "hash" : "9443656bab59f52441286e1d859855be28cbe155973c712c07385a21b7212152"
  }
}
```

4.19 离线组装 - 调用合约的交易
===================
Cmd: /api/contract/call/offline
-------------------------------
_**详细描述: 离线组装 - 调用合约的交易**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "senderBalance" : null,
  "nonce" : null,
  "contractAddress" : null,
  "gasLimit" : 0,
  "value" : null,
  "methodName" : null,
  "methodDesc" : null,
  "args" : null,
  "argsType" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                             |        参数类型         | 参数描述                       | 是否必填 |
| --------------------------------------------------------------- |:-------------------:| -------------------------- |:----:|
| 调用合约离线交易                                                        | contractcalloffline | 调用合约离线交易表单                 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |       string        | 交易创建者                      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;senderBalance   |     biginteger      | 账户余额                       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce           |       string        | 账户nonce值                   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |       string        | 智能合约地址                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit        |        long         | GAS限制                      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value           |     biginteger      | 调用者向合约地址转入的主网资产金额，没有此业务时填0 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |       string        | 方法名                        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |       string        | 方法描述，若合约内方法没有重载，则此参数可以为空   |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |      object[]       | 参数列表                       |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;argsType        |      string[]       | 参数类型列表                     |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark          |       string        | 备注                         |  否   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "senderBalance" : "999999998523475",
  "nonce" : "9c0aea02bed90ddd",
  "contractAddress" : "tNULSeBaMwYiR4p1X9xNJPiyJfrXjr4KgkcFjG",
  "gasLimit" : 14166,
  "value" : 0,
  "methodName" : "transfer",
  "methodDesc" : null,
  "args" : [ "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", 3800 ],
  "argsType" : [ "Address", "BigInteger" ],
  "remark" : "remark_call_test"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "1000be2a375d1072656d61726b5f63616c6c5f7465737497020001f7ec6473df12e751d64cf20a8baa7edd50810f810200020d2f73cb93099a8cfd0cbdd060155abfe2f50917000000000000000000000000000000000000000000000000000000000000000056370000000000001900000000000000087472616e7366657200020126744e554c536542614d6e7273364a4b724379365451647a594a5a6b4d5a4a446e673751417344010433383030480117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010006ee060000000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000000",
    "hash" : "aa69824582c6a3c1a4d486bbd38377a4c4a0ec4ea75a898fc70d109364a41bbf"
  }
}
```

4.20 离线组装 - 删除合约交易
==================
Cmd: /api/contract/delete/offline
---------------------------------
_**详细描述: 离线组装 - 删除合约交易**_
### HttpMethod: POST

### Form json data: 
```json
{
  "sender" : null,
  "senderBalance" : null,
  "nonce" : null,
  "contractAddress" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                             |         参数类型          | 参数描述       | 是否必填 |
| --------------------------------------------------------------- |:---------------------:| ---------- |:----:|
| 删除合约离线交易                                                        | contractdeleteoffline | 删除合约离线交易表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |        string         | 交易创建者      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;senderBalance   |      biginteger       | 账户余额       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce           |        string         | 账户nonce值   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |        string         | 智能合约地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark          |        string         | 备注         |  否   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "sender" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "senderBalance" : "999999998523475",
  "nonce" : "9c0aea02bed90ddd",
  "contractAddress" : "tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu",
  "remark" : "delete contract"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "11004f2c375d0f64656c65746520636f6e74726163742e020001f7ec6473df12e751d64cf20a8baa7edd50810f81020002245bcd36879bc30bfc719a417939b3aa924247ca480117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a086010000000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000000",
    "hash" : "780cd742592e16e9062f5a04f72273b1c92f8f130e2c93bdb25662fa4ad7aa50"
  }
}
```

4.21 离线组装 - 合约token转账交易
=======================
Cmd: /api/contract/tokentransfer/offline
----------------------------------------
_**详细描述: 离线组装 - 合约token转账交易**_
### HttpMethod: POST

### Form json data: 
```json
{
  "fromAddress" : null,
  "senderBalance" : null,
  "nonce" : null,
  "toAddress" : null,
  "contractAddress" : null,
  "gasLimit" : 0,
  "amount" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                             |             参数类型             | 参数描述          | 是否必填 |
| --------------------------------------------------------------- |:----------------------------:| ------------- |:----:|
| token转账离线交易                                                     | contracttokentransferoffline | token转账离线交易表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromAddress     |            string            | 转出者账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;senderBalance   |          biginteger          | 转出者账户余额       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce           |            string            | 转出者账户nonce值   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toAddress       |            string            | 转入者账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |            string            | 合约地址          |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit        |             long             | GAS限制         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount          |          biginteger          | 转出的token资产金额  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark          |            string            | 备注            |  否   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "fromAddress" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "senderBalance" : "999999998523475",
  "nonce" : "9c0aea02bed90ddd",
  "toAddress" : "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD",
  "contractAddress" : "tNULSeBaN3MH7HX8kXzKw4X9tLKQ991X1GiAbK",
  "gasLimit" : 14166,
  "amount" : 10,
  "remark" : "1个"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "1000632b375d0431e4b8aa95020001f7ec6473df12e751d64cf20a8baa7edd50810f810200026b8d9b09ed5c1a692a6109c5ee99ccb6177b13a1000000000000000000000000000000000000000000000000000000000000000056370000000000001900000000000000087472616e7366657200020126744e554c536542614d6e7273364a4b724379365451647a594a5a6b4d5a4a446e67375141734401023130480117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010006ee060000000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000000",
    "hash" : "4eb36b1fb31b0888895c0cdcab39c80ac986b18f7aef721a390ff1727c77ef10"
  }
}
```

4.22 离线组装 - 从账户地址向合约地址转账(主链资产)的合约交易
===================================
Cmd: /api/contract/transfer2contract/offline
--------------------------------------------
_**详细描述: 离线组装 - 从账户地址向合约地址转账(主链资产)的合约交易**_
### HttpMethod: POST

### Form json data: 
```json
{
  "fromAddress" : null,
  "senderBalance" : null,
  "nonce" : null,
  "toAddress" : null,
  "gasLimit" : 0,
  "amount" : null,
  "remark" : null
}
```

参数列表
----
| 参数名                                                           |          参数类型           | 参数描述          | 是否必填 |
| ------------------------------------------------------------- |:-----------------------:| ------------- |:----:|
| 向合约地址转账离线交易                                                   | contracttransferoffline | 向合约地址转账离线交易表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromAddress   |         string          | 转出者账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;senderBalance |       biginteger        | 转出者账户余额       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce         |         string          | 转出者账户nonce值   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toAddress     |         string          | 转入的合约地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit      |          long           | GAS限制         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        |       biginteger        | 转出的主链资产金额     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark        |         string          | 备注            |  否   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "fromAddress" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "senderBalance" : "999999998523475",
  "nonce" : "9c0aea02bed90ddd",
  "toAddress" : "tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu",
  "gasLimit" : 25896,
  "amount" : "400000000",
  "remark" : "离线向合约转账"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "1000e82b375d15e7a6bbe7babfe59091e59088e7baa6e8bdace8b4a677020001f7ec6473df12e751d64cf20a8baa7edd50810f81020002245bcd36879bc30bfc719a417939b3aa924247ca0084d7170000000000000000000000000000000000000000000000000000000028650000000000001900000000000000085f70617961626c650e28292072657475726e20766f6964008c0117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010088ebe21700000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000117020002245bcd36879bc30bfc719a417939b3aa924247ca020001000084d71700000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "4ed64d90abf420beba1baf68399c85d290347dc41de2c49384a2f8c895d4addf"
  }
}
```

5.1 创建共识节点
==========
Cmd: /api/consensus/agent
-------------------------
_**详细描述: 创建共识节点**_
### HttpMethod: POST

### Form json data: 
```json
{
  "agentAddress" : null,
  "packingAddress" : null,
  "rewardAddress" : null,
  "commissionRate" : 0,
  "deposit" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                            |      参数类型       | 参数描述        | 是否必填 |
| -------------------------------------------------------------- |:---------------:| ----------- |:----:|
| CreateAgentForm                                                | createagentform | 创建共识节点表单    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress   |     string      | 节点地址        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress |     string      | 节点出块地址      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rewardAddress  |     string      | 奖励地址，默认节点地址 |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;commissionRate |       int       | 佣金比例        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |     string      | 抵押金额        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password       |     string      | 密码          |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "agentAddress" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "packingAddress" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "rewardAddress" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "commissionRate" : 10,
  "deposit" : "2000000000000",
  "password" : "abcd1234"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "52456e830fa389c72c4a71e4224db5aa869d0fbfd0cb2175096e6e5fb6a5c6ee"
  }
}
```

5.2 注销共识节点
==========
Cmd: /api/consensus/agent/stop
------------------------------
_**详细描述: 注销共识节点**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                      |     参数类型      | 参数描述     | 是否必填 |
| -------------------------------------------------------- |:-------------:| -------- |:----:|
| StopAgentForm                                            | stopagentform | 注销共识节点表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address  |    string     | 共识节点地址   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |    string     | 密码       |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "password" : "abcd1234"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "fcaf8c92a0eafd2ca57744c165e1a955edcbfde98248494937200cc30d524e2e"
  }
}
```

5.3 委托参与共识
==========
Cmd: /api/consensus/deposit
---------------------------
_**详细描述: 委托参与共识**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "agentHash" : null,
  "deposit" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                       |    参数类型     | 参数描述     | 是否必填 |
| --------------------------------------------------------- |:-----------:| -------- |:----:|
| DepositForm                                               | depositform | 委托参与共识表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address   |   string    | 参与共识账户地址 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash |   string    | 共识节点hash |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit   |   string    | 参与共识的金额  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password  |   string    | 密码       |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "agentHash" : "52456e830fa389c72c4a71e4224db5aa869d0fbfd0cb2175096e6e5fb6a5c6ee",
  "deposit" : "200000000000",
  "password" : "abcd1234"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "4ae333f8bf821884d0f589f35516c8bdd9661dbd8a7009b063ac862eeefc10f6"
  }
}
```

5.4 退出共识
========
Cmd: /api/consensus/withdraw
----------------------------
_**详细描述: 退出共识**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "txHash" : null,
  "password" : null
}
```

参数列表
----
| 参数名                                                      |     参数类型     | 参数描述         | 是否必填 |
| -------------------------------------------------------- |:------------:| ------------ |:----:|
| 退出共识                                                     | withdrawform | 退出共识表单       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address  |    string    | 节点地址         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash   |    string    | 加入共识时的交易hash |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |    string    | 密码           |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "txHash" : "4ae333f8bf821884d0f589f35516c8bdd9661dbd8a7009b063ac862eeefc10f6",
  "password" : "abcd1234"
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "value" : "13a0e252bf05ec02f3ae0a84fc3b8183dbfc0e16c562b20b8e28b73b139f2c0e"
  }
}
```

5.5 查询节点的委托共识列表
===============
Cmd: /api/consensus/list/deposit/{agentHash}
--------------------------------------------
_**详细描述: 查询节点的委托共识列表**_
### HttpMethod: GET

参数列表
----
| 参数名       |  参数类型  | 参数描述          | 是否必填 |
| --------- |:------:| ------------- |:----:|
| agentHash | string | 创建共识节点的交易hash |  是   |

返回值
---
| 字段名         |  字段类型  | 参数描述      |
| ----------- |:------:| --------- |
| deposit     | string | 委托金额      |
| agentHash   | string | 节点hash    |
| address     | string | 账户地址      |
| time        |  long  | 委托时间      |
| txHash      | string | 委托交易hash  |
| blockHeight |  long  | 委托时的区块高度  |
| delHeight   |  long  | 退出委托的区块高度 |
### Example request data: 

_**request path:**_
http://localhost:9898/api/consensus/list/deposit/786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd

_**request form data:**_
无

### Example response data: 
```json
{
  "success" : true,
  "data" : [ {
    "deposit" : "200000000000",
    "agentHash" : "786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd",
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "time" : 1563277510,
    "txHash" : "bd93cf73331c0d9986cb90922d2eec785ea9eda3da85cd9d629b5a4c7f36c452",
    "blockHeight" : 462,
    "delHeight" : -1
  }, {
    "deposit" : "200000000000",
    "agentHash" : "786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd",
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "time" : 1563277712,
    "txHash" : "be5257bc0814cbda61378ff2afa81e98cae0018cd7d78b8d1ca9812c66d27e84",
    "blockHeight" : 482,
    "delHeight" : -1
  } ]
}
```

5.6 离线组装 - 创建共识节点交易
===================
Cmd: /api/consensus/agent/offline
---------------------------------
_**详细描述: 参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)**_
### HttpMethod: POST

### Form json data: 
```json
{
  "agentAddress" : null,
  "packingAddress" : null,
  "rewardAddress" : null,
  "commissionRate" : 0,
  "deposit" : null,
  "input" : {
    "address" : null,
    "assetChainId" : 0,
    "assetId" : 0,
    "amount" : null,
    "nonce" : null
  }
}
```

参数列表
----
| 参数名                                                                                                          |     参数类型     | 参数描述       | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:------------:| ---------- |:----:|
| ConsensusDto                                                                                                 | consensusdto | 离线创建共识节点表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress                                                 |    string    | 节点创建地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress                                               |    string    | 节点出块地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rewardAddress                                                |    string    | 获取共识奖励地址   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;commissionRate                                               |     int      | 节点佣金比例     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit                                                      |  biginteger  | 创建节点保证金    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;input                                                        |    object    | 交易输入信息     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |    string    | 账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |     int      | 资产的链id     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |     int      | 资产id       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |  biginteger  | 资产金额       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |    string    | 资产nonce值   |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "agentAddress" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "packingAddress" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
  "rewardAddress" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "commissionRate" : 10,
  "deposit" : "2000000000000",
  "input" : {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "2000001000000",
    "nonce" : "63ac862eeefc10f6"
  }
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "040019b72d5d006600204aa9d1010000000000000000000000000000000000000000000000000000020001efa328e600912da9872390a675486ab9e8ec2114020001f7ec6473df12e751d64cf20a8baa7edd50810f81020001efa328e600912da9872390a675486ab9e8ec21140a8c0117020001efa328e600912da9872390a675486ab9e8ec211402000100406259a9d10100000000000000000000000000000000000000000000000000000863ac862eeefc10f6000117020001efa328e600912da9872390a675486ab9e8ec21140200010000204aa9d1010000000000000000000000000000000000000000000000000000ffffffffffffffff00",
    "hash" : "786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd"
  }
}
```

5.7 离线组装 - 注销共识节点交易
===================
Cmd: /api/consensus/agent/stop/offline
--------------------------------------
_**详细描述: 组装交易的StopDepositDto信息，可通过查询节点的委托共识列表获取，input的nonce值可为空**_
### HttpMethod: POST

### Form json data: 
```json
{
  "agentHash" : null,
  "agentAddress" : null,
  "deposit" : null,
  "price" : null,
  "depositList" : [ {
    "depositHash" : null,
    "input" : {
      "address" : null,
      "assetChainId" : 0,
      "assetId" : 0,
      "amount" : null,
      "nonce" : null
    }
  } ]
}
```

参数列表
----
| 参数名                                                                                                                                                          |       参数类型       | 参数描述        | 是否必填 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------ |:----------------:| ----------- |:----:|
| StopConsensusDto                                                                                                                                             | stopconsensusdto | 离线注销共识节点表单  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash                                                                                                    |      string      | 创建节点的交易hash |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress                                                                                                 |      string      | 节点地址        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit                                                                                                      |    biginteger    | 创建节点的保证金    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                                                                        |    biginteger    | 手续费单价       |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositList                                                                                                  | list&lt;object>  | 停止委托列表      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositHash                                                  |      string      | 委托共识的交易hash |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;input                                                        |      object      | 交易输入信息      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |      string      | 账户地址        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int        | 资产的链id      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |       int        | 资产id        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |    biginteger    | 资产金额        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |      string      | 资产nonce值    |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "agentHash" : "786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd",
  "agentAddress" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "deposit" : "2000000000000",
  "price" : "100000",
  "depositList" : [ {
    "depositHash" : "bd93cf73331c0d9986cb90922d2eec785ea9eda3da85cd9d629b5a4c7f36c452",
    "input" : {
      "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
      "assetChainId" : 2,
      "assetId" : 1,
      "amount" : "200000000000",
      "nonce" : ""
    }
  }, {
    "depositHash" : "be5257bc0814cbda61378ff2afa81e98cae0018cd7d78b8d1ca9812c66d27e84",
    "input" : {
      "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
      "assetChainId" : 2,
      "assetId" : 1,
      "amount" : "200000000000",
      "nonce" : ""
    }
  } ]
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "0900e1bc2d5d0020786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcdfd5c010317020001efa328e600912da9872390a675486ab9e8ec21140200010000204aa9d1010000000000000000000000000000000000000000000000000000087a1f080d3dd30dcdff17020001efa328e600912da9872390a675486ab9e8ec21140200010000d0ed902e00000000000000000000000000000000000000000000000000000008629b5a4c7f36c452ff17020001efa328e600912da9872390a675486ab9e8ec21140200010000d0ed902e000000000000000000000000000000000000000000000000000000081ca9812c66d27e84ff0217020001efa328e600912da9872390a675486ab9e8ec211402000100609948a9d1010000000000000000000000000000000000000000000000000000f1ca2d5d0000000017020001efa328e600912da9872390a675486ab9e8ec21140200010000a0db215d000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "c07b40a70858b262a39b55deb08c9d505384c017580f91976979e8984a096eaf"
  }
}
```

5.8 离线组装 - 委托参与共识交易
===================
Cmd: /api/consensus/deposit/offline
-----------------------------------
_**详细描述: 参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "deposit" : null,
  "agentHash" : null,
  "input" : {
    "address" : null,
    "assetChainId" : 0,
    "assetId" : 0,
    "amount" : null,
    "nonce" : null
  }
}
```

参数列表
----
| 参数名                                                                                                          |    参数类型    | 参数描述       | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:----------:| ---------- |:----:|
| DepositDto                                                                                                   | depositdto | 离线委托参与共识表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address                                                      |   string   | 账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit                                                      | biginteger | 委托金额       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash                                                    |   string   | 共识节点hash   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;input                                                        |   object   | 交易输入信息     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |   string   | 账户地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |    int     | 资产的链id     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |    int     | 资产id       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       | biginteger | 资产金额       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |   string   | 资产nonce值   |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "deposit" : "200000000000",
  "agentHash" : "786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd",
  "input" : {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "200010000000",
    "nonce" : "629b5a4c7f36c452"
  }
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "050090b92d5d005700d0ed902e000000000000000000000000000000000000000000000000000000020001efa328e600912da9872390a675486ab9e8ec2114786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd8c0117020001efa328e600912da9872390a675486ab9e8ec211402000100806686912e00000000000000000000000000000000000000000000000000000008629b5a4c7f36c452000117020001efa328e600912da9872390a675486ab9e8ec21140200010000d0ed902e000000000000000000000000000000000000000000000000000000ffffffffffffffff00",
    "hash" : "be5257bc0814cbda61378ff2afa81e98cae0018cd7d78b8d1ca9812c66d27e84"
  }
}
```

5.9 离线组装 - 退出共识交易
=================
Cmd: /api/consensus/withdraw/offline
------------------------------------
_**详细描述: 接口的input数据，则是委托共识交易的output数据，nonce值可为空**_
### HttpMethod: POST

### Form json data: 
```json
{
  "address" : null,
  "depositHash" : null,
  "price" : null,
  "input" : {
    "address" : null,
    "assetChainId" : 0,
    "assetId" : 0,
    "amount" : null,
    "nonce" : null
  }
}
```

参数列表
----
| 参数名                                                                                                          |    参数类型     | 参数描述        | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:-----------:| ----------- |:----:|
| WithDrawDto                                                                                                  | withdrawdto | 离线退出共识表单    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address                                                      |   string    | 地址          |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositHash                                                  |   string    | 委托共识交易的hash |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                        | biginteger  | 手续费单价       |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;input                                                        |   object    | 交易输入信息      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |   string    | 账户地址        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |     int     | 资产的链id      |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |     int     | 资产id        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       | biginteger  | 资产金额        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |   string    | 资产nonce值    |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
```json
{
  "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
  "depositHash" : "be5257bc0814cbda61378ff2afa81e98cae0018cd7d78b8d1ca9812c66d27e84",
  "price" : "1000000",
  "input" : {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : 200000000000,
    "nonce" : ""
  }
}
```

### Example response data: 
```json
{
  "success" : true,
  "data" : {
    "txHex" : "060090ba2d5d0020be5257bc0814cbda61378ff2afa81e98cae0018cd7d78b8d1ca9812c66d27e848c0117020001efa328e600912da9872390a675486ab9e8ec21140200010000d0ed902e000000000000000000000000000000000000000000000000000000081ca9812c66d27e84ff0117020001efa328e600912da9872390a675486ab9e8ec211402000100c08dde902e000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "d1a054a1bc5d20bab53235993a5a2aee6f3c644e67e0044c60a08c7c49bb0ff2"
  }
}
```

5.10 多签账户离线组装 - 创建共识节点交易
========================
Cmd: /api/consensus/multiSign/agent/offline
-------------------------------------------
_**详细描述: 参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)**_
### HttpMethod: POST

参数列表
----
| 参数名                                                                                                          |         参数类型          | 参数描述           | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:---------------------:| -------------- |:----:|
| MultiSignConsensusDto                                                                                        | multisignconsensusdto | 多签账户离线创建共识节点表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress                                                 |        string         | 节点创建地址         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress                                               |        string         | 节点出块地址         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rewardAddress                                                |        string         | 获取共识奖励地址       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;commissionRate                                               |          int          | 节点佣金比例         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit                                                      |      biginteger       | 创建节点保证金        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;input                                                        |        object         | 交易输入信息         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |        string         | 账户地址           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |          int          | 资产的链id         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |          int          | 资产id           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |      biginteger       | 资产金额           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |        string         | 资产nonce值       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys                                                      |    list&lt;string>    | 公钥集合           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns                                                     |          int          | 最小签名数          |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

5.11 离线组装 - 多签账户委托参与共识交易
========================
Cmd: /api/consensus/multiSign/deposit/offline
---------------------------------------------
_**详细描述: 参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)**_
### HttpMethod: POST

参数列表
----
| 参数名                                                                                                          |        参数类型         | 参数描述           | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:-------------------:| -------------- |:----:|
| MultiSignDepositDto                                                                                          | multisigndepositdto | 多签账户离线委托参与共识表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address                                                      |       string        | 账户地址           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit                                                      |     biginteger      | 委托金额           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash                                                    |       string        | 共识节点hash       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;input                                                        |       object        | 交易输入信息         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |       string        | 账户地址           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |         int         | 资产的链id         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |         int         | 资产id           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |     biginteger      | 资产金额           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |       string        | 资产nonce值       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys                                                      |   list&lt;string>   | 公钥集合           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns                                                     |         int         | 最小签名数          |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

5.12 离线组装 - 多签账户退出共识交易
======================
Cmd: /api/consensus/multiSign/withdraw/offline
----------------------------------------------
_**详细描述: 接口的input数据，则是委托共识交易的output数据，nonce值可为空**_
### HttpMethod: POST

参数列表
----
| 参数名                                                                                                          |         参数类型         | 参数描述         | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:--------------------:| ------------ |:----:|
| WithDrawDto                                                                                                  | multisignwithdrawdto | 多签账户离线退出共识表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address                                                      |        string        | 地址           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositHash                                                  |        string        | 委托共识交易的hash  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                        |      biginteger      | 手续费单价        |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;input                                                        |        object        | 交易输入信息       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |        string        | 账户地址         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |         int          | 资产的链id       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |         int          | 资产id         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |      biginteger      | 资产金额         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |        string        | 资产nonce值     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys                                                      |   list&lt;string>    | 公钥集合         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns                                                     |         int          | 最小签名数        |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

5.13 离线组装 - 多签账户注销共识节点交易
========================
Cmd: /api/consensus/multiSign/agent/stop/offline
------------------------------------------------
_**详细描述: 组装交易的StopDepositDto信息，可通过查询节点的委托共识列表获取，input的nonce值可为空**_
### HttpMethod: POST

参数列表
----
| 参数名                                                                                                                                                          |           参数类型            | 参数描述           | 是否必填 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------ |:-------------------------:| -------------- |:----:|
| StopConsensusDto                                                                                                                                             | multisignstopconsensusdto | 多签账户离线注销共识节点表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash                                                                                                    |          string           | 创建节点的交易hash    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress                                                                                                 |          string           | 节点地址           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit                                                                                                      |        biginteger         | 创建节点的保证金       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                                                                        |        biginteger         | 手续费单价          |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositList                                                                                                  |      list&lt;object>      | 停止委托列表         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositHash                                                  |          string           | 委托共识的交易hash    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;input                                                        |          object           | 交易输入信息         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address      |          string           | 账户地址           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |            int            | 资产的链id         |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |            int            | 资产id           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount       |        biginteger         | 资产金额           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce        |          string           | 资产nonce值       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys                                                                                                      |      list&lt;string>      | 公钥集合           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns                                                                                                     |            int            | 最小签名数          |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
### Example request data: 

_**request path:**_
略

_**request form data:**_
无

### Example response data: 
略

