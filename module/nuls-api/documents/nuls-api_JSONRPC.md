# NULS2.0 API

**NULS为合作伙伴定制了对接需要的NULS2.0钱包版本，对接钱包内嵌`NULS-API`模块，模块内封装了NULS-SDK的功能，用HTTP协议访问接口，支持`JSON—RPC`和`Restful`两种格式。**

[主网或测试版钱包下载地址](https://github.com/nuls-io/nuls-v2/releases)

[NULS-API离线操作工具下载地址](http://nuls-cn.oss-cn-hangzhou.aliyuncs.com/2.1/NULS_API-offline_v2.0.0.tar.gz)

## 设置

​	`NULS-API`模块默认访问的端口号是8004，可以在nuls.ncf配置文件中做修改，如下：

```
[nuls-API]
#httpServer的启动port
serverPort=8004
```

## 说明

​	为了更好的理解NULS2.0的相关业务，和接口返回值的含义，提前在这里做一些说明。

### 在线与离线

`NULS-API`模块提供了若干在线接口和离线接口。

#### 在线接口

即需要访问节点钱包API，才能得到正确的返回结果。访问在线接口有以下几点要求：

- 需安装节点钱包且必须正常运行。
- 节点钱包能够连接网络中的其他节点，能够正常同步区块和广播数据。
- 在调用在线接口之前，节点钱包最好是已经同步到最新区块。

在线接口所产生的数据都会保存在钱包中。例如创建账户、修改密码、转账交易、获取区块头等。

#### 离线接口

NULS2.0提供了一个专门用于[离线操作的NULS-API工具](http://nuls-cn.oss-cn-hangzhou.aliyuncs.com/2.1/NULS_API-offline_v2.0.0.tar.gz)。无需安装钱包，可独立运行在一台没有连接网络的服务器上。用户通过调用离线接口，传入相关的参数，获取返回值，相应数据不会存入钱包。例如离线创建账户、离线组装转账交易、离线签名等。

### 字段与业务描述

#### 链的chainId

​	NULS2.0支持多链并行和跨链转账，每条链通过链ID（chainId）来区分，NULS主网的链ID为1，NULS测试网的链ID为2。

#### 链的资产

​	NULS2.0还支持多资产，每条链除了默认的资产外，可根据业务需要，动态添加资产。每种资产通过链ID（chainId）和资产ID（assetId）的复合主键来区分。例如NULS主网的NULS资产（chainId=1,assetId=1）

​	**主资产：每条链的默认资产就是主资产，交易手续费只收取本链主资产。**

#### 合约资产

​	NULS2.0内置智能合约，用户可以通过NULS官方提供的标准NRC20合约模板发布合约资产(token)，每发布一个合约资产都有唯一合约地址作为标记。

​	这里需要注意的是合约资产并不等于链的资产，链的资产是会在链上分配有chainId和assetId属性的，而合约资产并没有。

#### 交易类型

​	NULS2.0默认有多种交易，每种交易的功能不同，调用接口查询交易详情时，可通过交易类型字段（type）来区分不同交易类型，以下是交易类型的枚举值：

```
int COIN_BASE = 1;						// coinBase出块奖励
int TRANSFER = 2;						// 普通转账
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
int VERIFIER_INIT = 25;                 // 验证人初始化
int CONTRACT_TOKEN_CROSS_TRANSFER = 26; // 合约跨链转账
```

#### 转账

​	转账交易分为4种：**普通转账、跨链转账、合约转账、合约跨链转账。**其中普通转账和跨链转账针对是链上资产（分配有chainId和assetId）。后两者针对合约token的转账。

​	**普通转账（type=2）：**也叫链内转账，交易支持多对多的转账操作。多对多有两层含义，一是资产可以是多个，二是转账地址可以是多个。转账人、转账资产、转账金额都体现在交易的from和to里，后面会详细说明。

​	**跨链转账（type=10）：**跨链转账是指将链上资产跨链转到别的NULS平行链上去。同普通转账一样，也支持多对多的转账操作，体现在交易的from和to里。

​	**合约转账（type=16）：**合约转账只支持合约token的链内转账，它实际上是通过调用智能合约完成的。交易的from和to里只包含手续费。

​	**合约跨链转账（type=26）：**合约的跨链转账，也是通过调用智能合约完成的。但是节点在打包区块解析智能合约后，系统内部生成一个type=10的跨链交易，一同打包到区块内。所以实际上合约跨链转账是通过两个交易来实现。

#### 交易的from和to

用转账交易为例：tx.type = 2

```
tx:{
	"hash": "9c10fdf7162b00ac9a0972fcdc81c68d4f41383f025196a4207372e78acc9a3f",				//交易ID
	"type": 2,
	"from": [
        {
        	"address": "NULSeBaMotPhSFTjU9UxmzS8uToKezBSSYUxS",
         	"assetsChainId": 1
            "assetsId": 1,
            "amount": "100000100000",
            "nonce": "86f0ae38296c6b9d",
            "locked": 0
        }
    ],
    "to": [
    	{
    		"address": "NULSeBaMnAh6nquK1PARuRzKVk5LcfmFEAu7X",
    		"assetsChainId": 1,
    		"assetsId": 1,
    		"amount": "100000000000",
    		"lockTime": 0
    	}
    ]
}
```

​	**from：**转账交易的转出方，每一个from视为一个地址的某一种资产转出多少数量。通过assetsChainId和assetsId确定某一个具体资产，address为转出地址，amount为转出数量。其中nonce值每次转账后都会改变，防止打包时出现双花，可通过调用查询账户余额接口获取当前最新nonce值。

​	**to：**转账交易的接收方，每一个to视为接收人接收到某一种资产多少数量，其中lockTime字段为锁定时间。当lockTime=0时，表示资产可以正常使用；当锁定时间大于0时，表示只有当现实时间（时间戳）超过这个值之后，这笔资产才能正常使用；当lockTime =-1时，表示永久锁定中，需要特殊的交易才能解除锁定，例如参与委托共识和取消委托共识。

#### 资产的永久锁定与解锁

​	NULS里支持特殊的交易将链上的资产永久锁定，通常这种永久锁定交易都是成对出现，再通过另一个交易解除锁定，从而实现一些复杂的业务逻辑。我们用委托参与共识交易（type=5）和退出取消委托共识（type=6）来举例说明。

**委托参与共识交易：**

```
tx:{
	"hash": "fce936c045d2f200598db030105ecce20fdf09f672897d4e0e18f6d8bd9dc023",				//交易ID
	"type": 5,
	"from": [
        {
        	"address": "NULSeBaMotPhSFTjU9UxmzS8uToKezBSSYUxS",
         	"assetsChainId": 1,
            "assetsId": 1,
            "amount": "100000100000",
            "nonce": "207372e78acc9a3f",
            "locked": 0
        }
    ],
    "to": [
    	{
    		"address": "NULSeBaMotPhSFTjU9UxmzS8uToKezBSSYUxS",
    		"assetsChainId": 1,
    		"assetsId": 1,
    		"amount": "100000000000",
    		"lockTime": -1
    	}
    ]
}
```

这个交易里type=5，from和to的地址相同，都是NULSeBaMotPhSFTjU9UxmzS8uToKezBSSYUxS，且to的lockTime = -1。表示自己通过委托共识交易永久锁定了部分资产,去参与共识获得出块奖励。

**取消委托共识交易：**

```
tx:{
	"hash": "ff6596e2489c591efa94eab27e082f902bc5fefb529416e00cd736a34029c08c",				//交易ID
	"type": 6,
	"from": [
        {
        	"address": "tNULSeBaMotPhSFTjU9UxmzS8uToKezBSSYUxS",
         	"assetsChainId": 2,
            "assetsId": 1,
            "amount": "100000000000",			//必须和锁定时的金额一致
            "nonce": "0e18f6d8bd9dc023",		//必须是锁定交易的hash后8位
            "locked": -1
        }
    ],
    "to": [
    	{
    		"address": "tNULSeBaMotPhSFTjU9UxmzS8uToKezBSSYUxS",
    		"assetsChainId": 2,
    		"assetsId": 1,
    		"amount": "99999900000",
    		"lockTime": 0
    	}
    ]
}
```

这个交易里from的locked = -1，表示为需要解锁一笔资产。需要注意的是，解锁交易的from必须和之前锁定交易的to保持一致，即assetsChainId、assetsId、amount值一样。nonce值则是之前锁定交易hash的后8位。

#### 交易手续费

​	交易手续费 ：from里本链主资产之和 - to里本链主资产之和。

​	手续费单价：

​		转账交易（type=2）：0.001NULS/KB

​		其他交易：0.01NULS/KB


## 访问方式

- **`JSON-RPC`访问方式**

     添加请求头 Content-Type: application/json;charset=UTF-8
     
     HttpMethod: POST
     
     URL: http://${ip}:${port}/jsonrpc 
     
        示例: http://127.0.0.1:8004/jsonrpc
     
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
     
     其余请参考 [RESTFUL 接口文档](https://github.com/nuls-io/nuls-v2/blob/master/module/nuls-api/documents/nuls-api_RESTFUL.md)


## 接口文档

我们对外提供的API接口，分为`JSON-RPC`和`Restful`两种风格，用户可根据需要选择不通过的对接方式，接口文档详见以下: 

[JSON-RPC 接口文档](https://github.com/nuls-io/nuls-v2/blob/master/module/nuls-api/documents/nuls-api_JSONRPC.md)

[RESTFUL 接口文档](https://github.com/nuls-io/nuls-v2/blob/master/module/nuls-api/documents/nuls-api_RESTFUL.md)

_**附：**_ 官方已提供NULS-SDK-4J工具，有使用JAVA做对接的合作伙伴，可使用工具对接`NULS-API`模块，详见：[NULS-SDK-4J使用说明](https://github.com/nuls-io/nuls-v2-sdk4j/blob/master/README.md)

## 接口调试

我们提供了`Postman`接口调式工具的导入文件(`JSON-RPC`和`RESTFUL`)，导入后，即可调试接口

[JSON-PRC 接口调试-POSTMAN导入文件](https://github.com/nuls-io/nuls-v2/blob/master/module/nuls-api/documents/nuls-api_Postman_JSONRPC.json)

[RESTFUL 接口调试-POSTMAN导入文件](https://github.com/nuls-io/nuls-v2/blob/master/module/nuls-api/documents/nuls-api_Postman_RESTFUL.json)



接口列表
----
### 0.1 获取本链相关信息,其中共识资产为本链创建共识节点交易和创建委托共识交易时，需要用到的资产
#### Cmd: info
_**详细描述: 获取本链相关信息,其中共识资产为本链创建共识节点交易和创建委托共识交易时，需要用到的资产**_

#### 参数列表
无参数

#### 返回值
| 字段名             |  字段类型  | 参数描述         |
| --------------- |:------:| ------------ |
| chainId         | string | 本链的ID        |
| assetId         | string | 本链默认主资产的ID   |
| inflationAmount | string | 本链默认主资产的初始数量 |
| agentChainId    | string | 本链共识资产的链ID   |
| agentAssetId    | string | 本链共识资产的ID    |
| addressPrefix   | string | 本链地址前缀       |
| symbol          | string | 本链主资产符号      |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "info",
  "params" : [ ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "agentChainId" : 2,
    "inflationAmount" : 41095890410959,
    "agentAssetId" : 1,
    "commissionMin" : 20000000000000,
    "chainId" : 2,
    "assetId" : 1,
    "addressPrefix" : "tNULS",
    "symbol" : "NULS"
  }
}
```

### 1.1 批量创建账户
#### Cmd: createAccount
_**详细描述: 创建的账户存在于本地钱包内**_

#### 参数列表
| 参数名   | 参数类型 | 参数描述                                                 | 是否必填 |
| -------- | :------: | -------------------------------------------------------- | :------: |
| chainId  |   int    | 链ID                                                     |    是    |
| count    |   int    | 创建数量                                                 |    是    |
| password |  string  | 8-20位长度的密码，字母与数字的组合，且密码首位必须是字母 |    是    |

#### 返回值
| 字段名 |      字段类型       | 参数描述     |
| --- |:---------------:| -------- |
| 返回值 | list&lt;string> | 返回账户地址集合 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createAccount",
  "params" : [ 2, 1, "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : [ "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk" ]
}
```

### 1.2 修改账户密码
#### Cmd: updatePassword
_**详细描述: 修改账户密码**_

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否必填 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| address     | string | 账户地址 |  是   |
| oldPassword | string | 原密码  |  是   |
| newPassword | string | 新密码  |  是   |

#### 返回值
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否修改成功 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "updatePassword",
  "params" : [ 2, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "abcd1234", "abcd1111" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : true
}
```

### 1.3 导出账户私钥
#### Cmd: getPriKey
_**详细描述: 导出本地钱包已存在账户的私钥**_

#### 参数列表
| 参数名      |  参数类型  | 参数描述 | 是否必填 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链ID  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 密码   |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 私钥   |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getPriKey",
  "params" : [ 2, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "abcd1111" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "53b02c91605451ea35175df894b4c47b7d1effbd05d6b269b3e7c785f3f6dc18"
}
```

### 1.4 根据私钥导入账户
#### Cmd: importPriKey
_**详细描述: 导入私钥时，需要输入密码给明文私钥加密**_

#### 参数列表
| 参数名   | 参数类型 | 参数描述             | 是否必填 |
| -------- | :------: | -------------------- | :------: |
| chainId  |   int    | 链ID                 |    是    |
| priKey   |  string  | 账户明文私钥         |    是    |
| password |  string  | 新密码，用于加密私钥 |    是    |

#### 返回值
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 账户地址 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "importPriKey",
  "params" : [ 2, "53b02c91605451ea35175df894b4c47b7d1effbd05d6b269b3e7c785f3f6dc18", "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk"
}
```

### 1.5 根据keystore导入账户
#### Cmd: importKeystore
_**详细描述: 根据keystore导入账户**_

#### 参数列表
| 参数名          |  参数类型  | 参数描述         | 是否必填 |
| ------------ |:------:| ------------ |:----:|
| chainId      |  int   | 链ID          |  是   |
| keyStoreJson |  map   | keyStoreJson |  是   |
| password     | string | keystore密码   |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 账户地址 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "importKeystore",
  "params" : [ 2, {
    "address" : "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk",
    "encryptedPrivateKey" : "8dbe5c1da7228c0a8b6a26c328231b8df2d4dbfd3f9b029557708d4560de9ecd53a353bb2d688d7c68bd11d741e5d3ed",
    "pubKey" : "024477033a4521efee5f90caf30f8eb3284e8d1bb7fef2923ae21617b24aacc8cb",
    "prikey" : null
  }, "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk"
}
```

### 1.6 账户备份，导出账户keystore信息
#### Cmd: exportKeystore
_**详细描述: 账户备份，导出账户keystore信息**_

#### 参数列表
| 参数名      |  参数类型  | 参数描述 | 是否必填 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链ID  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 账户密码 |  是   |

#### 返回值
| 字段名    |  字段类型  | 参数描述     |
| ------ |:------:| -------- |
| result | string | keystore |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "exportKeystore",
  "params" : [ 2, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "{\"address\":\"tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk\",\"encryptedPrivateKey\":\"8dbe5c1da7228c0a8b6a26c328231b8df2d4dbfd3f9b029557708d4560de9ecd53a353bb2d688d7c68bd11d741e5d3ed\",\"pubKey\":\"024477033a4521efee5f90caf30f8eb3284e8d1bb7fef2923ae21617b24aacc8cb\",\"prikey\":null}"
}
```

### 1.7 查询账户余额
#### Cmd: getAccountBalance
_**详细描述: 根据资产链ID和资产ID，查询本链账户对应资产的余额与nonce值**_

#### 参数列表
| 参数名          |  参数类型  | 参数描述   | 是否必填 |
| ------------ |:------:| ------ |:----:|
| chainId      |  int   | 链ID    |  是   |
| assetChainId |  int   | 资产的链ID |  是   |
| assetId      |  int   | 资产ID   |  是   |
| address      | string | 账户地址   |  是   |

#### 返回值
| 字段名           |  字段类型  | 参数描述                      |
| ------------- |:------:| ------------------------- |
| totalBalance  | string | 总余额                       |
| balance       | string | 可用余额                      |
| timeLock      | string | 时间锁定金额                    |
| consensusLock | string |  共识锁定金额                   |
| freeze        | string | 总锁定余额                     |
| nonce         | string | 账户资产nonce值                |
| nonceType     |  int   | 1：已确认的nonce值,0：未确认的nonce值 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getAccountBalance",
  "params" : [ 2, 2, 1, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "totalBalance" : "0",
    "balance" : "0",
    "timeLock" : "0",
    "consensusLock" : "0",
    "freeze" : "0",
    "nonce" : "0000000000000000",
    "nonceType" : 1
  }
}
```

### 1.8 设置账户别名
#### Cmd: setAlias
_**详细描述: 别名格式为1-20位小写字母和数字的组合，设置别名会销毁1个NULS**_

#### 参数列表
| 参数名      |  参数类型  | 参数描述 | 是否必填 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链ID  |  是   |
| address  | string | 账户地址 |  是   |
| alias    | string | 别名   |  是   |
| password | string | 账户密码 |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述        |
| ----- |:------:| ----------- |
| value | string | 设置别名交易的hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "setAlias",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "test", "nuls123456" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "b8e53f0b43bbb566bf48ba30a56dc935bba27873fb5e52d793b7dc564b71b81c"
}
```

### 1.9 验证地址是否正确
#### Cmd: validateAddress
_**详细描述: 验证地址是否正确**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述 | 是否必填 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链ID  |  是   |
| address | string | 账户地址 |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述    |
| ----- |:------:| ------- |
| value | string | boolean |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "validateAddress",
  "params" : [ 2, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : true
  }
}
```

### 1.10 根据账户公钥生成账户地址
#### Cmd: getAddressByPublicKey
_**详细描述: 根据账户公钥生成账户地址**_

#### 参数列表
| 参数名       |  参数类型  | 参数描述 | 是否必填 |
| --------- |:------:| ---- |:----:|
| chainId   |  int   | 链ID  |  是   |
| publicKey | string | 账户公钥 |  是   |

#### 返回值
| 字段名     |  字段类型  | 参数描述 |
| ------- |:------:| ---- |
| address | string | 账户地址 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getAddressByPublicKey",
  "params" : [ 2, "03958b790c331954ed367d37bac901de5c2f06ac8368b37d7bd6cd5ae143c1d7e3" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"
  }
}
```

### 1.11 离线 - 批量创建账户
#### Cmd: createAccountOffline
_**详细描述: 创建的账户不会保存到钱包中,接口直接返回账户的keystore信息**_

#### 参数列表
| 参数名      |  参数类型  | 参数描述 | 是否必填 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链ID  |  是   |
| count    |  int   | 创建数量 |  是   |
| prefix   | string | 地址前缀 |  否   |
| password | string | 密码   |  是   |

#### 返回值
| 字段名                 |  字段类型  | 参数描述   |
| ------------------- |:------:| ------ |
| address             | string | 账户地址   |
| pubKey              | string | 公钥     |
| prikey              | string | 明文私钥   |
| encryptedPrivateKey | string | 加密后的私钥 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createAccountOffline",
  "params" : [ 2, 1, "tNULS", "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : [ {
    "address" : "tNULSeBaMnS7et6FdFMMMLyK8Wvy1daVVeohfu",
    "pubKey" : "02756e0d0827df60f5806bc00c44f97a9f5c234f78502a314aa40bb0a0156cd9f0",
    "prikey" : "",
    "encryptedPrivateKey" : "720e9f7ac1ab2ee997bad249d1c42212a5c5c744358a7bc65f472a1fe61a87a8f0bc841fdc74c8313fe6c94f496f3676"
  } ]
}
```

### 1.12 离线获取账户明文私钥
#### Cmd: getPriKeyOffline
_**详细描述: 离线获取账户明文私钥**_

#### 参数列表
| 参数名                 |  参数类型  | 参数描述   | 是否必填 |
| ------------------- |:------:| ------ |:----:|
| chainId             |  int   | 链ID    |  是   |
| address             | string | 账户地址   |  是   |
| encryptedPrivateKey | string | 账户密文私钥 |  是   |
| password            | string | 密码     |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 明文私钥 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getPriKeyOffline",
  "params" : [ 2, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "8dbe5c1da7228c0a8b6a26c328231b8df2d4dbfd3f9b029557708d4560de9ecd53a353bb2d688d7c68bd11d741e5d3ed", "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "priKey" : "53b02c91605451ea35175df894b4c47b7d1effbd05d6b269b3e7c785f3f6dc18"
  }
}
```

### 1.13 离线修改账户密码
#### Cmd: resetPasswordOffline
_**详细描述: 离线修改账户密码**_

#### 参数列表
| 参数名                 |  参数类型  | 参数描述   | 是否必填 |
| ------------------- |:------:| ------ |:----:|
| chainId             |  int   | 链ID    |  是   |
| address             | string | 账户地址   |  是   |
| encryptedPrivateKey | string | 账户密文私钥 |  是   |
| oldPassword         | string | 原密码    |  是   |
| newPassword         | string | 新密码    |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述       |
| ----- |:------:| ---------- |
| value | string | 重置密码后的加密私钥 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "resetPasswordOffline",
  "params" : [ 2, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "8dbe5c1da7228c0a8b6a26c328231b8df2d4dbfd3f9b029557708d4560de9ecd53a353bb2d688d7c68bd11d741e5d3ed", "abcd1234", "abcd1111" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "newEncryptedPriKey" : "8dbe5c1da7228c0a8b6a26c328231b8df2d4dbfd3f9b029557708d4560de9ecd53a353bb2d688d7c68bd11d741e5d3ed"
  }
}
```

### 1.14 多账户摘要签名
#### Cmd: multiSign
_**详细描述: 用于签名离线组装的多账户转账交易,调用接口时，参数可以传地址和私钥，或者传地址和加密私钥和加密密码**_

#### 参数列表
| 参数名                                                                 |  参数类型  | 参数描述         | 是否必填 |
| ------------------------------------------------------------------- |:------:| ------------ |:----:|
| chainId                                                             |  int   | 链ID          |  是   |
| signDtoList                                                         |  list  | 摘要签名表单       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address             | string | 地址           |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;priKey              | string | 明文私钥         |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPrivateKey | string | 加密私钥         |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password            | string | 密码           |  否   |
| txHex                                                               | string | 交易序列化16进制字符串 |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述          |
| ----- |:------:| ------------- |
| hash  | string | 交易hash        |
| txHex | string | 签名后的交易16进制字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "multiSign",
  "params" : [ 2, [ {
    "address" : "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk",
    "priKey" : "53b02c91605451ea35175df894b4c47b7d1effbd05d6b269b3e7c785f3f6dc18"
  }, {
    "address" : "tNULSABFehEc2HgKhXFMtH3yGHpSStBthiuMfd",
    "encryptedPrivateKey" : "8dbe5c1da7228c0a8b6a26c328231b8df2d4dbfd3f9b029557708d4560de9ecd53a353bb2d688d7c68bd11d741e5d3ed",
    "password" : "abcd1234"
  } ], "0200b67f2d5d0672656d61726b008c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b02000100402a8648170000000000000000000000000000000000000000000000000000000800000000000000000001170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0200010000e8764817000000000000000000000000000000000000000000000000000000000000000000000000" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0200b67f2d5d0672656d61726b008c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b02000100402a8648170000000000000000000000000000000000000000000000000000000800000000000000000001170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0200010000e876481700000000000000000000000000000000000000000000000000000000000000000000006a21024477033a4521efee5f90caf30f8eb3284e8d1bb7fef2923ae21617b24aacc8cb473045022100a8b3d10dfdf4fb0c7c6ede1f5d216a631689fbbd0e9beb46cac1918a5e64ccbc02202a654c3d9a27a99e8458ac18a8b9bc460f520bff10e4592102ad04e22890b412",
    "hash" : "748184df91eda8d09be76e075d553313434c56bfeec3d449abc99ba6c430c00c"
  }
}
```

### 1.15 明文私钥摘要签名
#### Cmd: priKeySign
_**详细描述: 明文私钥摘要签名**_

#### 参数列表
| 参数名        |  参数类型  | 参数描述         | 是否必填 |
| ---------- |:------:| ------------ |:----:|
| chainId    |  int   | 链ID          |  是   |
| txHex      | string | 交易序列化16进制字符串 |  是   |
| address    | string | 账户地址         |  是   |
| privateKey | string | 账户明文私钥       |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述          |
| ----- |:------:| ------------- |
| hash  | string | 交易hash        |
| txHex | string | 签名后的交易16进制字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "priKeySign",
  "params" : [ 2, "0200b67f2d5d0672656d61726b008c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b02000100402a8648170000000000000000000000000000000000000000000000000000000800000000000000000001170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0200010000e8764817000000000000000000000000000000000000000000000000000000000000000000000000", "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "53b02c91605451ea35175df894b4c47b7d1effbd05d6b269b3e7c785f3f6dc18" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0200b67f2d5d0672656d61726b008c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b02000100402a8648170000000000000000000000000000000000000000000000000000000800000000000000000001170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0200010000e876481700000000000000000000000000000000000000000000000000000000000000000000006a21024477033a4521efee5f90caf30f8eb3284e8d1bb7fef2923ae21617b24aacc8cb473045022100a8b3d10dfdf4fb0c7c6ede1f5d216a631689fbbd0e9beb46cac1918a5e64ccbc02202a654c3d9a27a99e8458ac18a8b9bc460f520bff10e4592102ad04e22890b412",
    "hash" : "748184df91eda8d09be76e075d553313434c56bfeec3d449abc99ba6c430c00c"
  }
}
```

### 1.16 密文私钥摘要签名
#### Cmd: encryptedPriKeySign
_**详细描述: 密文私钥摘要签名**_

#### 参数列表
| 参数名                 |  参数类型  | 参数描述         | 是否必填 |
| ------------------- |:------:| ------------ |:----:|
| chainId             |  int   | 链ID          |  是   |
| txHex               | string | 交易序列化16进制字符串 |  是   |
| address             | string | 账户地址         |  是   |
| encryptedPrivateKey | string | 账户密文私钥       |  是   |
| password            | string | 密码           |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述          |
| ----- |:------:| ------------- |
| hash  | string | 交易hash        |
| txHex | string | 签名后的交易16进制字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "encryptedPriKeySign",
  "params" : [ 2, "0200b67f2d5d0672656d61726b008c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b02000100402a8648170000000000000000000000000000000000000000000000000000000800000000000000000001170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0200010000e8764817000000000000000000000000000000000000000000000000000000000000000000000000", "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "8dbe5c1da7228c0a8b6a26c328231b8df2d4dbfd3f9b029557708d4560de9ecd53a353bb2d688d7c68bd11d741e5d3ed", "abcd" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0200b67f2d5d0672656d61726b008c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b02000100402a8648170000000000000000000000000000000000000000000000000000000800000000000000000001170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0200010000e876481700000000000000000000000000000000000000000000000000000000000000000000006a21024477033a4521efee5f90caf30f8eb3284e8d1bb7fef2923ae21617b24aacc8cb473045022100a8b3d10dfdf4fb0c7c6ede1f5d216a631689fbbd0e9beb46cac1918a5e64ccbc02202a654c3d9a27a99e8458ac18a8b9bc460f520bff10e4592102ad04e22890b412",
    "hash" : "748184df91eda8d09be76e075d553313434c56bfeec3d449abc99ba6c430c00c"
  }
}
```

### 1.17 创建多签账户
#### Cmd: createMultiSignAccount
_**详细描述: 根据多个账户的公钥创建多签账户，minSigns为多签账户创建交易时需要的最小签名数**_

#### 参数列表
| 参数名      |      参数类型       | 参数描述   | 是否必填 |
| -------- |:---------------:| ------ |:----:|
| pubKeys  | list&lt;string> | 账户公钥集合 |  是   |
| minSigns |       int       | 最小签名数  |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述  |
| ----- |:------:| ----- |
| value | string | 账户的地址 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createMultiSignAccount",
  "params" : [ [ "03d0593e55a11e841e28c4288aa1181fb151f9d260bab0e006ca158095eb78bb35", "02ec141204330f1a028f4e4040582b9568db1f32bc8afc03c7ba6e84b78a72d979" ], 2 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : "tNULSeBaNNVVFWD1LjfT29s9BE4SqbzxonBejA"
  }
}
```

### 1.18 离线创建设置别名交易
#### Cmd: createAliasTx
_**详细描述: 离线创建设置别名交易**_

#### 参数列表
| 参数名                                                     |   参数类型   | 参数描述     | 是否必填 |
| ------------------------------------------------------- |:--------:| -------- |:----:|
| 创建别名交易                                                  | aliasdto | 创建别名交易表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |  string  | 账户地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias   |  string  | 别名       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce   |  string  | 资产nonce值 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark  |  string  | 交易备注     |  否   |

#### 返回值
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createAliasTx",
  "params" : [ "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "test2", "0000000000000000", "remark" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "03006621775d001e170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0574657374328c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b020001004023050600000000000000000000000000000000000000000000000000000000080000000000000000000117020001e2f297763765bc154afaac7aec5e7899a729fed20200010000e1f50500000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "170dc03089d5c721ce4f9794bb87ebf6c7553163622d57e7e8a22622ba02db5e"
  }
}
```

### 1.19 多签账户离线创建设置别名交易
#### Cmd: createMultiSignAliasTx
_**详细描述: 多签账户离线创建设置别名交易**_

#### 参数列表
| 参数名                                                      |       参数类型        | 参数描述     | 是否必填 |
| -------------------------------------------------------- |:-----------------:| -------- |:----:|
| 多签账户离线创建设置别名交易                                           | multisignaliasdto | 创建别名交易表单 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address  |      string       | 账户地址     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias    |      string       | 别名       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce    |      string       | 资产nonce值 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark   |      string       | 交易备注     |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys  |  list&lt;string>  | 公钥集合     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns |        int        | 最小签名数    |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createMultiSignAliasTx",
  "params" : [ "tNULSeBaNNVVFWD1LjfT29s9BE4SqbzxonBejA", "test2", "0000000000000000", "remark", [ "03d0593e55a11e841e28c4288aa1181fb151f9d260bab0e006ca158095eb78bb35", "02ec141204330f1a028f4e4040582b9568db1f32bc8afc03c7ba6e84b78a72d979" ], 2 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0300462e775d0672656d61726b1e17020003a2d7273f40c7e2d19ac53fd743bdcea92494a7c00574657374328c0117020003a2d7273f40c7e2d19ac53fd743bdcea92494a7c0020001004023050600000000000000000000000000000000000000000000000000000000080000000000000000000117020001e2f297763765bc154afaac7aec5e7899a729fed20200010000e1f5050000000000000000000000000000000000000000000000000000000000000000000000004602022103d0593e55a11e841e28c4288aa1181fb151f9d260bab0e006ca158095eb78bb352102ec141204330f1a028f4e4040582b9568db1f32bc8afc03c7ba6e84b78a72d979",
    "hash" : "e05e43886c8f9a95ef4c6dfb10f1964d507ac6b2d98079e4aa8d9d8d0572bdb2"
  }
}
```

### 1.20 根据私钥获取账户地址格式
#### Cmd: getAddressByPriKey
_**详细描述: 根据私钥获取账户地址格式**_

#### 参数列表
| 参数名                                                    |    参数类型    | 参数描述   | 是否必填 |
| ------------------------------------------------------ |:----------:| ------ |:----:|
| 原始私钥                                                   | prikeyform | 私钥表单   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;priKey |   string   | 账户明文私钥 |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| value | string | 账户地址 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getAddressByPriKey",
  "params" : [ "9ce21dad67e0f0af2599b41b515a7f7018059418bab892a7b68f283d489abc4b" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"
  }
}
```

### 1.21 查询钱包内创建的账户列表
#### Cmd: getAddressList
_**详细描述: 查询钱包内创建的账户列表**_

#### 参数列表
无参数

#### 返回值
无返回值
#### Example request data: 
```
{
    "jsonrpc":"2.0",
    "method":"getAddressList",
    "params":[],
    "id":1234
}
```

#### Example response data: 
```
{
    "jsonrpc": "2.0",
    "id": "1234",
    "result": [
        "tNULSeBaMhmHrnX4XJHbZxR4ypRun52s1uYnJB",
        "tNULSeBaMuJrfBuCWJn9t3WeKo8VGPvScftivi",
        "tNULSeBaMnrTyBNxbAnPgyihYLennQcYjh835H",
        "tNULSeBaMfRZXhSuWtka6RqmGhS8cfYk7wEtY3",
        "tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe"
    ]
}
```



### 2.1 获取本节点的网络状态信息
#### Cmd: getNetworkInfo
_**详细描述: 获取本节点的网络状态信息**_

#### 参数列表
无参数

#### 返回值
| 字段名 |    字段类型     | 参数描述   |
| --- |:-----------:| ------ |
| 返回值 | networkinfo | 返回网络状态 |
#### Example request data: 
```
{
"jsonrpc":"2.0",
"method":"getNetworkInfo",
"params":[],
"id":1234
}
```

#### Example response data: 
```
{
    "jsonrpc": "2.0",
    "id": "1234",
    "result": {
        "localBestHeight": 4624308,
        "netBestHeight": 4624309,
        "timeOffset": -17,
        "inCount": 0,
        "outCount": 12
    }
}
```



### 2.2 根据区块高度查询区块头
#### Cmd: getHeaderByHeight
_**详细描述: 根据区块高度查询区块头**_

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否必填 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| height  | long | 区块高度 |  是   |

#### 返回值
| 字段名                  |      字段类型       | 参数描述                 |
| -------------------- |:---------------:| -------------------- |
| hash                 |     string      | 区块的hash值             |
| preHash              |     string      | 上一个区块的hash值          |
| merkleHash           |     string      | 梅克尔hash              |
| time                 |     string      | 区块生成时间               |
| height               |      long       | 区块高度                 |
| txCount              |       int       | 区块打包交易数量             |
| blockSignature       |     string      | 签名Hex.encode(byte[]) |
| size                 |       int       | 大小                   |
| packingAddress       |     string      | 打包地址                 |
| roundIndex           |      long       | 共识轮次                 |
| consensusMemberCount |       int       | 参与共识成员数量             |
| roundStartTime       |     string      | 当前共识轮开始时间            |
| packingIndexOfRound  |       int       | 当前轮次打包出块的名次          |
| mainVersion          |      short      | 主网当前生效的版本            |
| blockVersion         |      short      | 区块的版本，可以理解为本地钱包的版本   |
| stateRoot            |     string      | 智能合约世界状态根            |
| txHashList           | list&lt;string> | 区块打包的交易hash集合        |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getHeaderByHeight",
  "params" : [ 2, 1 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "hash" : "0061dee8b289df58e3c820f38b31ce47d02993797f976eacd6020ced392a6b5b",
    "preHash" : "d8880f913c984e4dece5cfb3f5f1d96d6ee923ffb0b47be0079fe84472ddda83",
    "merkleHash" : "8930f7386e33eaf79c22025956820fa58f403b7dbdf3d39ca5f2be5776e8b8e5",
    "time" : "1970-01-19 10:14:08.008",
    "height" : 1,
    "txCount" : 1,
    "blockSignature" : "473045022100f2012721b3eef4bc052bcef76903cb4eab029020b09a300968f7dde6fb7c56be0220621774e67bc8b09440ab40273f64795d83394ec6ad3c9458801c36e9b0f29850",
    "size" : 247,
    "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
    "roundIndex" : 156324818,
    "consensusMemberCount" : 1,
    "roundStartTime" : "1970-01-19 10:14:08.008",
    "packingIndexOfRound" : 1,
    "mainVersion" : 1,
    "blockVersion" : 1,
    "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
  }
}
```

### 2.3 根据区块hash查询区块头
#### Cmd: getHeaderByHash
_**详细描述: 根据区块hash查询区块头**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述   | 是否必填 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| hash    | string | 区块hash |  是   |

#### 返回值
| 字段名                  |      字段类型       | 参数描述                 |
| -------------------- |:---------------:| -------------------- |
| hash                 |     string      | 区块的hash值             |
| preHash              |     string      | 上一个区块的hash值          |
| merkleHash           |     string      | 梅克尔hash              |
| time                 |     string      | 区块生成时间               |
| height               |      long       | 区块高度                 |
| txCount              |       int       | 区块打包交易数量             |
| blockSignature       |     string      | 签名Hex.encode(byte[]) |
| size                 |       int       | 大小                   |
| packingAddress       |     string      | 打包地址                 |
| roundIndex           |      long       | 共识轮次                 |
| consensusMemberCount |       int       | 参与共识成员数量             |
| roundStartTime       |     string      | 当前共识轮开始时间            |
| packingIndexOfRound  |       int       | 当前轮次打包出块的名次          |
| mainVersion          |      short      | 主网当前生效的版本            |
| blockVersion         |      short      | 区块的版本，可以理解为本地钱包的版本   |
| stateRoot            |     string      | 智能合约世界状态根            |
| txHashList           | list&lt;string> | 区块打包的交易hash集合        |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getHeaderByHash",
  "params" : [ 2, "0061dee8b289df58e3c820f38b31ce47d02993797f976eacd6020ced392a6b5b" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "hash" : "0061dee8b289df58e3c820f38b31ce47d02993797f976eacd6020ced392a6b5b",
    "preHash" : "d8880f913c984e4dece5cfb3f5f1d96d6ee923ffb0b47be0079fe84472ddda83",
    "merkleHash" : "8930f7386e33eaf79c22025956820fa58f403b7dbdf3d39ca5f2be5776e8b8e5",
    "time" : "1970-01-19 10:14:08.008",
    "height" : 1,
    "txCount" : 1,
    "blockSignature" : "473045022100f2012721b3eef4bc052bcef76903cb4eab029020b09a300968f7dde6fb7c56be0220621774e67bc8b09440ab40273f64795d83394ec6ad3c9458801c36e9b0f29850",
    "size" : 247,
    "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
    "roundIndex" : 156324818,
    "consensusMemberCount" : 1,
    "roundStartTime" : "1970-01-19 10:14:08.008",
    "packingIndexOfRound" : 1,
    "mainVersion" : 1,
    "blockVersion" : 1,
    "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
  }
}
```

### 2.4 查询最新区块头信息
#### Cmd: getBestBlockHeader
_**详细描述: 查询最新区块头信息**_

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否必填 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

#### 返回值
| 字段名                  |      字段类型       | 参数描述                 |
| -------------------- |:---------------:| -------------------- |
| hash                 |     string      | 区块的hash值             |
| preHash              |     string      | 上一个区块的hash值          |
| merkleHash           |     string      | 梅克尔hash              |
| time                 |     string      | 区块生成时间               |
| height               |      long       | 区块高度                 |
| txCount              |       int       | 区块打包交易数量             |
| blockSignature       |     string      | 签名Hex.encode(byte[]) |
| size                 |       int       | 大小                   |
| packingAddress       |     string      | 打包地址                 |
| roundIndex           |      long       | 共识轮次                 |
| consensusMemberCount |       int       | 参与共识成员数量             |
| roundStartTime       |     string      | 当前共识轮开始时间            |
| packingIndexOfRound  |       int       | 当前轮次打包出块的名次          |
| mainVersion          |      short      | 主网当前生效的版本            |
| blockVersion         |      short      | 区块的版本，可以理解为本地钱包的版本   |
| stateRoot            |     string      | 智能合约世界状态根            |
| txHashList           | list&lt;string> | 区块打包的交易hash集合        |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getBestBlockHeader",
  "params" : [ 2 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "hash" : "f1003ee7c46ee33c5d6c518342c993cad7d202767cb4b7b5ddb69ce19d8899ea",
    "preHash" : "8edfb6610be130020c3815915e81eccaa4c3c426362d1239030119b3a2941923",
    "merkleHash" : "4b4564bff52373d698dbb4d95ea66d23b18a2ae09079a9e62b8f4d7ddf8bdb5c",
    "time" : "1970-01-19 10:14:18.018",
    "height" : 1000,
    "txCount" : 1,
    "blockSignature" : "4730450221009d13cd79b918fba44b4ca549a37dc715e368ac55fe80170f54f52c2742da0ed802207312ee6d38b95a28feaca40ed9c91fba4d47fe5efa1940ecd4fe63e7b9cb5533",
    "size" : 247,
    "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
    "roundIndex" : 156325817,
    "consensusMemberCount" : 1,
    "roundStartTime" : "1970-01-19 10:14:18.018",
    "packingIndexOfRound" : 1,
    "mainVersion" : 1,
    "blockVersion" : 1,
    "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
  }
}
```

### 2.5 查询最新区块
#### Cmd: getBestBlock
_**详细描述: 包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用**_

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否必填 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

#### 返回值
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
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHashList                                                    | list&lt;string> | 区块打包的交易hash集合                             |
| txs                                                                                                           | list&lt;object> | 交易列表                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hash                                                          |     string      | 交易的hash值                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type                                                          |       int       | 交易类型                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time                                                          |     string      | 交易时间                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;timestamp                                                     |      long       | 交易时间戳                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight                                                   |      long       | 区块高度                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHash                                                     |     string      | 区块hash                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                        |     string      | 交易备注                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transactionSignature                                          |     string      | 交易签名                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txDataHex                                                     |     string      | 交易业务数据序列化字符串                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status                                                        |       int       | 交易状态 0:unConfirm(待确认), 1:confirm(已确认)     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;size                                                          |       int       | 交易大小                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inBlockIndex                                                  |       int       | 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                          | list&lt;object> | 输入                                        |
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
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getBestBlock",
  "params" : [ 2 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "header" : {
      "hash" : "55ff1491334a3e636e504f1bc12ba04fa0c582381a0b8e0c3f7aaa12a27fabb5",
      "preHash" : "97bb75f9d12e945396ffb386373941c05d9671770bd4639554e5ed948e775f8c",
      "merkleHash" : "0ecd099ee9c5955588516a6f619d9bef6406a7d2aa31eec592df2c6cb19e326d",
      "time" : "1970-01-19 10:14:21.021",
      "height" : 1348,
      "txCount" : 1,
      "blockSignature" : "463044022046aa28d324da4ec487829fcc8901e351eb13a0290bdd05c084d5e42a876ab6a1022024aa4386081787506771f5e8ddbe7a625d6f4aff67e5c10818fbd4f98ccf264e",
      "size" : 234,
      "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
      "roundIndex" : 156326165,
      "consensusMemberCount" : 1,
      "roundStartTime" : "1970-01-19 10:14:21.021",
      "packingIndexOfRound" : 1,
      "mainVersion" : 1,
      "blockVersion" : 1,
      "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
    },
    "txs" : [ {
      "type" : 1,
      "coinData" : "AAA=",
      "txData" : null,
      "time" : 1563261651,
      "transactionSignature" : null,
      "remark" : null,
      "hash" : {
        "bytes" : "Ds0JnunFlVWIUWpvYZ2b72QGp9KqMe7Fkt8sbLGeMm0="
      },
      "blockHeight" : 1348,
      "status" : "UNCONFIRM",
      "size" : 12,
      "inBlockIndex" : 0,
      "coinDataInstance" : {
        "from" : [ ],
        "to" : [ ],
        "fromAddressCount" : 0
      },
      "fee" : 0,
      "multiSignTx" : false
    } ]
  }
}
```

### 2.6 根据区块高度查询区块
#### Cmd: getBlockByHeight
_**详细描述: 包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用**_

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否必填 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| height  | long | 区块高度 |  是   |

#### 返回值
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
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHashList                                                    | list&lt;string> | 区块打包的交易hash集合                             |
| txs                                                                                                           | list&lt;object> | 交易列表                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hash                                                          |     string      | 交易的hash值                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type                                                          |       int       | 交易类型                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time                                                          |     string      | 交易时间                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;timestamp                                                     |      long       | 交易时间戳                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight                                                   |      long       | 区块高度                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHash                                                     |     string      | 区块hash                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                        |     string      | 交易备注                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transactionSignature                                          |     string      | 交易签名                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txDataHex                                                     |     string      | 交易业务数据序列化字符串                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status                                                        |       int       | 交易状态 0:unConfirm(待确认), 1:confirm(已确认)     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;size                                                          |       int       | 交易大小                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inBlockIndex                                                  |       int       | 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                          | list&lt;object> | 输入                                        |
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
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getBlockByHeight",
  "params" : [ 2, 100 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "header" : {
      "hash" : "0620b926b2f09921315cc251bd65fe803cfa9e2259275900f7f7509cd0dac6d3",
      "preHash" : "975c90cbc8dedc577ebf315be4d11b4153c2bbb1b9704484c45752215717aa1d",
      "merkleHash" : "c9144c126f64f2e79d11879af9f4c94839202c464bb854dae17d89800de30fc6",
      "time" : "1970-01-19 10:14:09.009",
      "height" : 100,
      "txCount" : 1,
      "blockSignature" : "463044022060286d182fb808bb24543730a0316688b2c02f8378f112bca15d0860288dc5340220566b867e1813ed57c79b5b6ed9baf1f07e29afa8b445a842120c5407557a7363",
      "size" : 234,
      "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
      "roundIndex" : 156324917,
      "consensusMemberCount" : 1,
      "roundStartTime" : "1970-01-19 10:14:09.009",
      "packingIndexOfRound" : 1,
      "mainVersion" : 1,
      "blockVersion" : 1,
      "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
    },
    "txs" : [ {
      "type" : 1,
      "coinData" : "AAA=",
      "txData" : null,
      "time" : 1563249171,
      "transactionSignature" : null,
      "remark" : null,
      "hash" : {
        "bytes" : "yRRMEm9k8uedEYea+fTJSDkgLEZLuFTa4X2JgA3jD8Y="
      },
      "blockHeight" : 100,
      "status" : "UNCONFIRM",
      "size" : 12,
      "inBlockIndex" : 0,
      "coinDataInstance" : {
        "from" : [ ],
        "to" : [ ],
        "fromAddressCount" : 0
      },
      "fee" : 0,
      "multiSignTx" : false
    } ]
  }
}
```

### 2.7 根据区块hash查询区块
#### Cmd: getBlockByHash
_**详细描述: 包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述   | 是否必填 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| hash    | string | 区块hash |  是   |

#### 返回值
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
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHashList                                                    | list&lt;string> | 区块打包的交易hash集合                             |
| txs                                                                                                           | list&lt;object> | 交易列表                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hash                                                          |     string      | 交易的hash值                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type                                                          |       int       | 交易类型                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time                                                          |     string      | 交易时间                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;timestamp                                                     |      long       | 交易时间戳                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight                                                   |      long       | 区块高度                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHash                                                     |     string      | 区块hash                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                        |     string      | 交易备注                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transactionSignature                                          |     string      | 交易签名                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txDataHex                                                     |     string      | 交易业务数据序列化字符串                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status                                                        |       int       | 交易状态 0:unConfirm(待确认), 1:confirm(已确认)     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;size                                                          |       int       | 交易大小                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inBlockIndex                                                  |       int       | 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                          | list&lt;object> | 输入                                        |
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
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getBlockByHash",
  "params" : [ 2, "0620b926b2f09921315cc251bd65fe803cfa9e2259275900f7f7509cd0dac6d3" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "header" : {
      "hash" : "0620b926b2f09921315cc251bd65fe803cfa9e2259275900f7f7509cd0dac6d3",
      "preHash" : "975c90cbc8dedc577ebf315be4d11b4153c2bbb1b9704484c45752215717aa1d",
      "merkleHash" : "c9144c126f64f2e79d11879af9f4c94839202c464bb854dae17d89800de30fc6",
      "time" : "1970-01-19 10:14:09.009",
      "height" : 100,
      "txCount" : 1,
      "blockSignature" : "463044022060286d182fb808bb24543730a0316688b2c02f8378f112bca15d0860288dc5340220566b867e1813ed57c79b5b6ed9baf1f07e29afa8b445a842120c5407557a7363",
      "size" : 234,
      "packingAddress" : "tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp",
      "roundIndex" : 156324917,
      "consensusMemberCount" : 1,
      "roundStartTime" : "1970-01-19 10:14:09.009",
      "packingIndexOfRound" : 1,
      "mainVersion" : 1,
      "blockVersion" : 1,
      "stateRoot" : "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421"
    },
    "txs" : [ {
      "type" : 1,
      "coinData" : "AAA=",
      "txData" : null,
      "time" : 1563249171,
      "transactionSignature" : null,
      "remark" : null,
      "hash" : {
        "bytes" : "yRRMEm9k8uedEYea+fTJSDkgLEZLuFTa4X2JgA3jD8Y="
      },
      "blockHeight" : 100,
      "status" : "UNCONFIRM",
      "size" : 12,
      "inBlockIndex" : 0,
      "coinDataInstance" : {
        "from" : [ ],
        "to" : [ ],
        "fromAddressCount" : 0
      },
      "fee" : 0,
      "multiSignTx" : false
    } ]
  }
}
```

### 2.8 根据区块高度查询区块序列化字符串
#### Cmd: getBlockSerializationByHeight
_**详细描述: 包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用**_

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否必填 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| height  | long | 区块高度 |  是   |

#### 返回值
| 字段名 |  字段类型  | 参数描述            |
| --- |:------:| --------------- |
| 返回值 | string | 返回区块序列化后的HEX字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getBlockSerializationByHeight",
  "params" : [ 2, 1 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "772f158614cefd4f4e0a7ef1cd442f4de7439c10b5642afe582ed09b585d9b1e37d371e184142ebb1d46f4160a18a1e27d51c23dd66c0ccc607044821ae7fff24ddc4c5d01000000010000005c6e7c5409010043dc4c5d0100010001005064002056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b42100000000000000000000000000000000000000000000000000000000000000000f2517abe887d67e21037fae74d15153c3b55857ca0abd5c34c865dfa1c0d0232997c545bae5541a0863473045022100c6515c296a80ae8ef48713cae87b693003fb57cc41ce2af4dcc93d32e3cb382502201b84db49946fee5fd57edb350fe0f4c78cac3a503cfb11cbb3a4f6082ffe26cb01004ddc4c5d000002000000"
}
```

### 2.9 根据区块hash查询区块序列化字符串
#### Cmd: getBlockSerializationByHash
_**详细描述: 包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述   | 是否必填 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| hash    | string | 区块hash |  是   |

#### 返回值
| 字段名 |  字段类型  | 参数描述            |
| --- |:------:| --------------- |
| 返回值 | string | 返回区块序列化后的HEX字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getBlockSerializationByHeight",
  "params" : [ 2, "5ce81f9a470459276b633465f2572862aa7156a42220d29d724ced9bf9d723f9" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "772f158614cefd4f4e0a7ef1cd442f4de7439c10b5642afe582ed09b585d9b1e37d371e184142ebb1d46f4160a18a1e27d51c23dd66c0ccc607044821ae7fff24ddc4c5d01000000010000005c6e7c5409010043dc4c5d0100010001005064002056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b42100000000000000000000000000000000000000000000000000000000000000000f2517abe887d67e21037fae74d15153c3b55857ca0abd5c34c865dfa1c0d0232997c545bae5541a0863473045022100c6515c296a80ae8ef48713cae87b693003fb57cc41ce2af4dcc93d32e3cb382502201b84db49946fee5fd57edb350fe0f4c78cac3a503cfb11cbb3a4f6082ffe26cb01004ddc4c5d000002000000"
}
```

### 2.10 获取最新主链高度
#### Cmd: getLatestHeight
_**详细描述: 获取最新主链高度**_

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否必填 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述     |
| --- |:----:| -------- |
| 返回值 | long | 获取最新主链高度 |
#### Example request data: 
无

#### Example response data: 
略

### 3.1 根据hash获取交易
#### Cmd: getTx
_**详细描述: 根据hash获取交易**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述   | 是否必填 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链id    |  是   |
| hash    | string | 交易hash |  是   |

#### 返回值
| 字段名                                                           |      字段类型       | 参数描述                                      |
| ------------------------------------------------------------- |:---------------:| ----------------------------------------- |
| hash                                                          |     string      | 交易的hash值                                  |
| type                                                          |       int       | 交易类型                                      |
| time                                                          |     string      | 交易时间                                      |
| timestamp                                                     |      long       | 交易时间戳                                     |
| blockHeight                                                   |      long       | 区块高度                                      |
| blockHash                                                     |     string      | 区块hash                                    |
| remark                                                        |     string      | 交易备注                                      |
| transactionSignature                                          |     string      | 交易签名                                      |
| txDataHex                                                     |     string      | 交易业务数据序列化字符串                              |
| status                                                        |       int       | 交易状态 0:unConfirm(待确认), 1:confirm(已确认)     |
| size                                                          |       int       | 交易大小                                      |
| inBlockIndex                                                  |       int       | 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序 |
| from                                                          | list&lt;object> | 输入                                        |
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
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getTx",
  "params" : [ 2, "3d05d84f7d537b70fe4bce6ec81904018e482461a831b6a7a69756225876293f" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "hash" : "3d05d84f7d537b70fe4bce6ec81904018e482461a831b6a7a69756225876293f",
    "type" : 16,
    "time" : "2019-12-18 14:35:04.004",
    "blockHeight" : 172,
    "blockHash" : "d7412d925da4eef1f1d7fdf2e19c24d1d2616e9ae3d75b405ee9e69b51bf0491",
    "remark" : "call contract test",
    "transactionSignature" : "2103958b790c331954ed367d37bac901de5c2f06ac8368b37d7bd6cd5ae143c1d7e3473045022100fa7c1987316b16fbc156173d2419591e4bc0df15835c096eae5d38f24c34ae7802201ca68cf83b13811f5e4cbd09bd03a53394ef0e90d20cd4a1bb43eb13a6fa441e",
    "txDataHex" : "020001f7ec6473df12e751d64cf20a8baa7edd50810f810200029fef190beb3651234855ec4348471180ae1881b1000000000000000000000000000000000000000000000000000000000000000080841e00000000001900000000000000087472616e7366657200020126744e554c536542614d72624d52694641556565417436737762347856424e79693831594c32340103383030",
    "status" : 1,
    "size" : 374,
    "inBlockIndex" : 0,
    "from" : [ {
      "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
      "assetsChainId" : 2,
      "assetsId" : 1,
      "amount" : "50100000",
      "nonce" : "ef3247392e9a8d99",
      "locked" : 0
    } ],
    "to" : [ ]
  }
}
```

### 3.2 验证交易
#### Cmd: validateTx
_**详细描述: 验证离线组装的交易,验证成功返回交易hash值,失败返回错误提示信息**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述     | 是否必填 |
| ------- |:------:| -------- |:----:|
| chainId |  int   | 链id      |  是   |
| tx      | string | 交易序列化字符串 |  是   |

#### 返回值
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "validateTx",
  "params" : [ 2, "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

### 3.3 广播交易
#### Cmd: broadcastTx
_**详细描述: 广播离线组装的交易,成功返回true,失败返回错误提示信息**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述         | 是否必填 |
| ------- |:------:| ------------ |:----:|
| chainId |  int   | 链id          |  是   |
| tx      | string | 交易序列化16进制字符串 |  是   |

#### 返回值
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否成功   |
| hash  | string  | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "broadcastTx",
  "params" : [ 2, "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : true,
    "hash" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

### 3.4 广播交易(不验证合约)
#### Cmd: broadcastTxWithNoContractValidation
_**详细描述: 广播离线组装的交易(不验证合约),成功返回true,失败返回错误提示信息**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述         | 是否必填 |
| ------- |:------:| ------------ |:----:|
| chainId |  int   | 链id          |  是   |
| tx      | string | 交易序列化16进制字符串 |  是   |

#### 返回值
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否成功   |
| hash  | string  | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "broadcastTxWithNoContractValidation",
  "params" : [ 2, "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : true,
    "hash" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

### 3.5 广播交易(不验证)
#### Cmd: broadcastTxWithoutAnyValidation
_**详细描述: 广播离线组装的交易(不验证),成功返回true,失败返回错误提示信息**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述         | 是否必填 |
| ------- |:------:| ------------ |:----:|
| chainId |  int   | 链id          |  是   |
| tx      | string | 交易序列化16进制字符串 |  是   |

#### 返回值
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否成功   |
| hash  | string  | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "broadcastTxWithoutAnyValidation",
  "params" : [ 2, "02003fac2d5d00008c0117020001efa328e600912da9872390a675486ab9e8ec211402000100e0c8100000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010040420f000000000000000000000000000000000000000000000000000000000000000000000000006921023cee1aa6158ee640c8f48f9a9fa9735c8ed5426f2c353b0ed65e123033d820e646304402203c376fd0121fce6228516c011126a8526c5bc543afb7e4272c0de708a55d834f02204ebcd942e019b77bbec37f7e2b77b591ba4ce0fbc5fe9335ab91ae925ded6bed" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : true,
    "hash" : "5a91b75e6a6d1f415638375627933b42ce7179b4c6390ca0dcc5a0c2c74bd34a"
  }
}
```

### 3.6 单笔链内转账
#### Cmd: transfer
_**详细描述: 发起单账户单资产的转账交易**_

#### 参数列表
| 参数名          |  参数类型  | 参数描述   | 是否必填 |
| ------------ |:------:| ------ |:----:|
| assetChainId |  int   | 资产链id  |  是   |
| assetId      |  int   | 资产id   |  是   |
| address      | string | 转出账户地址 |  是   |
| toAddress    | string | 转入账户地址 |  是   |
| password     | string | 转出账户密码 |  是   |
| amount       | string | 转出金额   |  是   |
| remark       | string | 备注     |  是   |

#### 返回值
| 字段名  |  字段类型  | 参数描述   |
| ---- |:------:| ------ |
| hash | string | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "transfer",
  "params" : [ 2, 1, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "nuls123456", "10000000000000", "transfer tx" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "hash" : "40acabd7e7b7643aa545f2b74d09f8d65eecf885919d968d263a7a24255f8698"
  }
}
```

### 3.7 单笔转账
#### Cmd: transferOtherChainAsset
_**详细描述: 发起单账户单资产的转账交易,转账资产为链内的其他平行链资产**_

#### 参数列表
| 参数名          |  参数类型  | 参数描述   | 是否必填 |
| ------------ |:------:| ------ |:----:|
| chainId      |  int   | 链id    |  是   |
| assetChainId |  int   | 资产链id  |  是   |
| assetId      |  int   | 资产id   |  是   |
| address      | string | 转出账户地址 |  是   |
| toAddress    | string | 转入账户地址 |  是   |
| password     | string | 转出账户密码 |  是   |
| amount       | string | 转出金额   |  是   |
| remark       | string | 备注     |  是   |

#### 返回值
| 字段名  |  字段类型  | 参数描述   |
| ---- |:------:| ------ |
| hash | string | 交易hash |
#### Example request data: 
```
{
  "jsonrpc" : "2.0",
  "method" : "transferOtherChainAsset",
  "params" : [ 5, 1, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "nuls123456", "10000000000000", "transfer tx" ],
  "id" : 1234
}
```

#### Example response data: 
```
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "hash" : "40acabd7e7b7643aa545f2b74d09f8d65eecf885919d968d263a7a24255f8698"
  }
}
```

### 3.8 单笔跨链转账
#### Cmd: crossTransfer
_**详细描述: 发起单账户单资产的跨链转账交易**_

#### 参数列表
| 参数名          |  参数类型  | 参数描述   | 是否必填 |
| ------------ |:------:| ------ |:----:|
| assetChainId |  int   | 资产链id  |  是   |
| assetId      |  int   | 资产id   |  是   |
| address      | string | 转出账户地址 |  是   |
| toAddress    | string | 转入账户地址 |  是   |
| password     | string | 转出账户密码 |  是   |
| amount       | string | 转出金额   |  是   |
| remark       | string | 备注     |  是   |

#### 返回值
| 字段名  |  字段类型  | 参数描述   |
| ---- |:------:| ------ |
| hash | string | 交易hash |
#### Example request data: 
```
{
  "jsonrpc" : "2.0",
  "method" : "crossTransfer",
  "params" : [ 2, 1, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "TNVTseBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "nuls123456", "10000000000000", "cross transfer tx" ],
  "id" : 1234
}
```

#### Example response data: 
```
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "hash" : "40acabd7e7b7643aa545f2b74d09f8d65eecf885919d968d263a7a24255f8698"
  }
}
```



### 3.9 离线组装转账交易
#### Cmd: createTransferTxOffline
_**详细描述: 根据inputs和outputs离线组装转账交易，用于单账户或多账户的转账交易。交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和**_

#### 参数列表
| 参数名                                                       |    参数类型     | 参数描述         | 是否必填 |
| ------------------------------------------------------------ | :-------------: | ---------------- | :------: |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inputs       | list&lt;object> | 转账交易输入列表 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |     string      | 账户地址         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 资产的链id       |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId |       int       | 资产id           |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount |   biginteger    | 资产金额         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce |     string      | 资产nonce值      |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs      | list&lt;object> | 转账交易输出列表 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |     string      | 账户地址         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 资产的链id       |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId |       int       | 资产id           |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount |   biginteger    | 资产金额         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime |      long       | 锁定时间         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time         |      long       | 创建时间         |    否    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark       |     string      | 交易备注         |    否    |

#### 返回值
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createTransferTxOffline",
  "params" : [ [ {
    "address" : "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "100001000000",
    "nonce" : "0000000000000000"
  } ], [ {
    "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "100000000000",
    "lockTime" : 0
  } ], "remark" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0200b67f2d5d0672656d61726b008c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b02000100402a8648170000000000000000000000000000000000000000000000000000000800000000000000000001170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0200010000e8764817000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "748184df91eda8d09be76e075d553313434c56bfeec3d449abc99ba6c430c00c"
  }
}
```

### 3.10 离线组装转账交易
#### Cmd: createCrossTxOffline
_**详细描述: 根据inputs和outputs离线组装跨链转账交易，用于单账户或多账户的跨链转账交易。交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和，加上跨链转账手续费（NULS）**_

#### 参数列表
| 参数名                                                       |    参数类型     | 参数描述         | 是否必填 |
| ------------------------------------------------------------ | :-------------: | ---------------- | :------: |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inputs       | list&lt;object> | 转账交易输入列表 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |     string      | 账户地址         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 资产的链id       |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId |       int       | 资产id           |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount |   biginteger    | 资产金额         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce |     string      | 资产nonce值      |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs      | list&lt;object> | 转账交易输出列表 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |     string      | 账户地址         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 资产的链id       |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId |       int       | 资产id           |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount |   biginteger    | 资产金额         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime |      long       | 锁定时间         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time         |      long       | 创建时间         |    否    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark       |     string      | 交易备注         |    否    |

#### 返回值
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
#### Example request data: 
```
{
  "jsonrpc" : "2.0",
  "method" : "createCrossTxOffline",
  "params" : [ [ {
    "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "100001000000",
    "nonce" : "0000000000000000"
  } ], [ {
    "address" : "TNVTeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "100000000000",
    "lockTime" : 0
  } ], "remark" ],
  "id" : 1234
}
```

#### Example response data: 
```
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0200b67f2d5d0672656d61726b008c01170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b02000100402a8648170000000000000000000000000000000000000000000000000000000800000000000000000001170200012a9af4ee49f4cb1ee84eafd42aec41bc04b28f7b0200010000e8764817000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "748184df91eda8d09be76e075d553313434c56bfeec3d449abc99ba6c430c00c"
  }
}
```



### 3.11 计算离线创建转账交易所需手续费
#### Cmd: calcTransferTxFee
_**详细描述: 计算离线创建转账交易所需手续费，其中手续费单价不填写，默认为0.001NULS**_

#### 参数列表
| 参数名                                                       |  参数类型  | 参数描述     | 是否必填 |
| ------------------------------------------------------------ | :--------: | ------------ | :------: |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addressCount |    int     | 转账地址数量 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromLength   |    int     | 转账输入长度 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toLength     |    int     | 转账输出长度 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark       |   string   | 交易备注     |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price        | biginteger | 手续费单价   |    否    |

#### 返回值
| 字段名   |  字段类型  | 参数描述  |
| ----- |:------:| ----- |
| value | string | 交易手续费 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "calcTransferTxFee",
  "params" : [ 6, 6, 2, "remark", "1000000" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : 2000000
  }
}
```

### 3.12 计算离线创建跨链转账交易所需手续费
#### Cmd: calcCrossTxFee
_**详细描述: 计算离线创建跨链转账交易所需手续费**_

#### 参数列表
| 参数名                                                       | 参数类型 | 参数描述     | 是否必填 |
| ------------------------------------------------------------ | :------: | ------------ | :------: |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addressCount |   int    | 转账地址数量 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromLength   |   int    | 转账输入长度 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toLength     |   int    | 转账输出长度 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark       |  string  | 交易备注     |    是    |

#### 返回值
| 字段名   |  字段类型  | 参数描述  |
| ----- |:------:| ----- |
| value | string | 交易手续费 |
#### Example request data: 
```
{
"jsonrpc":"2.0",
"method":"calcCrossTxFee",
"params":[1, 1, 1,"remark"],
"id":1234
}
```

#### Example response data: 
```
{
    "jsonrpc": "2.0",
    "id": "1234",
    "result": {
        "value": "1000000"
    }
}
```

### 3.13 离线组装转账交易
#### Cmd: createMultiSignTransferTxOffline
_**详细描述: 根据inputs和outputs离线组装转账交易，用于单账户或多账户的转账交易。交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和**_

#### 参数列表
| 参数名                                                       |    参数类型     | 参数描述         | 是否必填 |
| ------------------------------------------------------------ | :-------------: | ---------------- | :------: |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeys      | list&lt;string> | 公钥集合         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minSigns     |       int       | 最小签名数       |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;inputs       | list&lt;object> | 转账交易输入列表 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |     string      | 账户地址         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 资产的链id       |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId |       int       | 资产id           |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount |   biginteger    | 资产金额         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce |     string      | 资产nonce值      |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs      | list&lt;object> | 转账交易输出列表 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address |     string      | 账户地址         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 资产的链id       |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId |       int       | 资产id           |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount |   biginteger    | 资产金额         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime |      long       | 锁定时间         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark       |     string      | 交易备注         |    是    |

#### 返回值
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| hash  | string | 交易hash       |
| txHex | string | 交易序列化16进制字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createMultiSignTransferTxOffline",
  "params" : [ [ "0377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db", "03f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1" ], 2, [ {
    "address" : "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : 11000000,
    "nonce" : "0000000000000000"
  } ], [ {
    "address" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : 10000000,
    "lockTime" : 0
  } ], "remark" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "02008854775d0672656d61726b008c0117020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe202000100c0d8a70000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010080969800000000000000000000000000000000000000000000000000000000000000000000000000460202210377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db2103f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1",
    "hash" : "f72a8240924380d3b2499ed8859f192b3097c0d19dbdbd879093fa2d974352c7"
  }
}
```

### 3.14 计算离线创建转账交易所需手续费
#### Cmd: calcMultiSignTransferTxFee
_**详细描述: 计算离线创建转账交易所需手续费**_

#### 参数列表
| 参数名                                                      |  参数类型  | 参数描述             | 是否必填 |
| ----------------------------------------------------------- | :--------: | -------------------- | :------: |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKeyCount |    int     | 多签地址对应公钥数量 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;fromLength  |    int     | 转账输入长度         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;toLength    |    int     | 转账输出长度         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark      |   string   | 交易备注             |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price       | biginteger | 手续费单价           |    否    |

#### 返回值
| 字段名   |  字段类型  | 参数描述  |
| ----- |:------:| ----- |
| value | string | 交易手续费 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "calcMultiSignTransferTxFee",
  "params" : [ 2, 2, 1, "remark", "1000000" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "value" : 1000000
  }
}
```

### 4.1 发布合约
#### Cmd: contractCreate
_**详细描述: 发布智能合约**_

#### 参数列表
| 参数名          |   参数类型   | 参数描述                 | 是否必填 |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | 链id                  |  是   |
| sender       |  string  | 交易创建者账户地址            |  是   |
| password     |  string  | 账户密码                 |  是   |
| alias        |  string  | 合约别名                 |  是   |
| gasLimit     |   long   | GAS限制                |  是   |
| price        |   long   | GAS单价                |  是   |
| contractCode |  string  | 智能合约代码(字节码的Hex编码字符串) |  是   |
| args         | object[] | 参数列表                 |  否   |
| remark       |  string  | 交易备注                 |  否   |

#### 返回值
| 字段名             |  字段类型  | 参数描述        |
| --------------- |:------:| ----------- |
| txHash          | string | 发布合约的交易hash |
| contractAddress | string | 生成的合约地址     |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "contractCreate",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "nuls123456", "jsonrpc_nrc20", 20000, 30, "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbacf782dd58a888e49983a72c8e83821c367b8a1c382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe274703aa254cbe6691d5fe0bc8ea36087270e4f9a11551d916cb9588a8c999109f6312f864869d2c845f2850819dfc8f078e4f67e394e289db32c389413c98fdb36fac5c52f79e32c7cafb216aae57260ecb899a2f6b535fb6bb7f3a261cd7c5153772c0b2e706ce8d001be1c1edacf712a7f527a6bb82e6f86e5cf6a4758d6b7e8debf11ac7639104887fb6ef2755c53c8c5f2ae53e2c278b3e4b36ece0f35534fe17504b0708ec308779cb09000028180000504b0304140008080800aa7b564e00000000000000000000000022000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373a552d14e1341143d03b5cb16aa28a8a0284a08697970131ff481a76a24694282a19507dfa6bb431d989da933b325fc9a0f7e801f65bc534ba5b6a5896eb23b3be79e7bcebd33f7c7cf6fdf01bcc64e84e711b623bc60a8367a3d6bfa5c7de80bed19569a5a0bfb5e71e7848bf092186dcbb53b1376c828699e0b86f55afde89cf779a2b8ee262d6fa5ee1e306c9e14dacb5c9c4a273b4a34b4369e7b69b463d83b9226d18572496ab4b73cf589cb2e123ee224a7525c9248d95de51da318963291ca9c2b4a5eacd59b0ccb9ea8aa55f47aea8a61e3ba869cfb2fc93bd96d6a2fbac29244dce154582a8ecf18ded4a61b37b2cc0ae70e668aec8c37f3915b6add0b3bd6d5fedcae4ec4d7425a9191e2921f1e26c3db39554d2faafe99eee85ae4d09a9ce1d33ca17fb489f86034e8ae63ae94b90cc7c9d0fe2fb799475d6999c2a6e2502af2a8b4cd85d0af0295616b42d0876832e030acfe19c2e3ceb9486942f76fcbd8fd6be06f278fcd7e99816101e1894b749788695fa15d99d66580b0952958157727b07b589dc0eee301a9dfc0b0466f144cd70784877844ff01ac2efc0647a1c7d81886d6166f864684cdd9b94ff0748aef388d085b83ef33dca135141a7c4ad46e488a10ff02504b070868fe421cca0100005e040000504b0304140008080800aa7b564e00000000000000000000000030000000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c6173738d565b6f1b4514fec6bbde75b69bbb49d224b46929e05b6a2ee19a4b73218140d2943835b4406163af926d9cdd74771d90a0129540ea033c801020242e2f252f790089b80824c45390f82ffc03049c995dbb89e3243cf8ccd93367e67cdf7c6746fef39f5f7e03f018ae6988e34213148c7333c1cd248f4da998d63083e735bcc0cd0c66b937abe2450d2770218697f838c7cd7c0c176358e0ee256e5e56b1a822c71075deb64d97e1d49ce564ed72c9cb161cdb778d829ff58a6bd98962d1353d6f9841f5364cbbc853a39b46a96c3274cd5d37368decbae1af6627ad9559db37574c97529511cbb6fc3186cb89a3773d66b6e1f6c93c833ce5140940eb9c659b17cbebcba6bb642c9728d231e7148c52de702dfe1d06657fd5f2189a2736365c87b0404806830d80c8f5406e3704725e2c3a0044ae07f211adf9f810205c335e98ae7cb8d75f9423d1782b95fe1e51793bbd8b965405d134fdee202a6da77f8732cf75dba58ea22143bf0ad4afd0ba45375ec4298b9f679020514cda9b205513e45db4f381627205b12db472ef0fa8f21664699b404882462f01053e818a4fe9e43e23989f13e52ff026be14b40602c0355ab7e89fc6e3824e1143148be009d1a0badac2fe46bf8a2715d6adb0b836cea9d32b1752ff393cc6e9805d734a306be26046da4ef7ad069c78580ac36f55e37210271a5a10efbb07bf43dc9eafe9367d43cdf32da6f0dd1e35a643d89d04fa193c4b5038d84128ede3ec5fbae511824b30c947e84718cbeff", [ "io", "IO", 80000, 1 ], null ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHash" : "0b8a56835e47ab67c57d1a3e7c9102315b0b219af4c3a7c0fa820d16f09a12f3",
    "contractAddress" : "tNULSeBaMx2zjgThursB6k9XsST7VvZmr2vB3J"
  }
}
```

### 4.2 调用合约
#### Cmd: contractCall
_**详细描述: 调用合约**_

#### 参数列表
| 参数名              |    参数类型    | 参数描述                                                                      | 是否必填 |
| ---------------- |:----------:| ------------------------------------------------------------------------- |:----:|
| chainId          |    int     | 链id                                                                       |  是   |
| sender           |   string   | 交易创建者账户地址                                                                 |  是   |
| password         |   string   | 调用者账户密码                                                                   |  是   |
| value            | biginteger | 调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO                                  |  是   |
| gasLimit         |    long    | GAS限制                                                                     |  是   |
| price            |    long    | GAS单价                                                                     |  是   |
| contractAddress  |   string   | 合约地址                                                                      |  是   |
| methodName       |   string   | 合约方法                                                                      |  是   |
| methodDesc       |   string   | 合约方法描述，若合约内方法没有重载，则此参数可以为空                                                |  否   |
| args             |  object[]  | 参数列表                                                                      |  否   |
| remark           |   string   | 交易备注                                                                      |  否   |
| multyAssetValues | string[][] | 调用者向合约地址转入的其他资产金额，没有此业务时填空，规则: [[\<value\>,\<assetChainId\>,\<assetId\>]] |  否   |

#### 返回值
| 字段名    |  字段类型  | 参数描述        |
| ------ |:------:| ----------- |
| txHash | string | 调用合约的交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "contractCall",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "nuls123456", 0, 200000, 30, "tNULSeBaMx2zjgThursB6k9XsST7VvZmr2vB3J", "transfer", null, [ "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM", "4000" ], "remark-jsonrpc-call", null ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHash" : "1e9d4676e09a7ead64a400971625c0cc0cd0991086bc969fe27a433bb6e6efb9"
  }
}
```

### 4.3 删除合约
#### Cmd: contractDelete
_**详细描述: 删除合约**_

#### 参数列表
| 参数名             |  参数类型  | 参数描述      | 是否必填 |
| --------------- |:------:| --------- |:----:|
| chainId         |  int   | 链id       |  是   |
| sender          | string | 交易创建者账户地址 |  是   |
| password        | string | 交易账户密码    |  是   |
| contractAddress | string | 合约地址      |  是   |
| remark          | string | 交易备注      |  否   |

#### 返回值
| 字段名    |  字段类型  | 参数描述        |
| ------ |:------:| ----------- |
| txHash | string | 删除合约的交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "contractDelete",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "nuls123456", "tNULSeBaMx2zjgThursB6k9XsST7VvZmr2vB3J", "delete-remark" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHash" : "12f521540bc9dba75b4c8fb605bb7387681891748265c0d4b869c64389008fca"
  }
}
```

### 4.4 合约token转账
#### Cmd: tokentransfer
_**详细描述: 合约token转账**_

#### 参数列表
| 参数名             |    参数类型    | 参数描述         | 是否必填 |
| --------------- |:----------:| ------------ |:----:|
| chainId         |    int     | 链id          |  是   |
| fromAddress     |   string   | 转出者账户地址      |  是   |
| password        |   string   | 转出者账户密码      |  是   |
| toAddress       |   string   | 转入者账户地址      |  是   |
| contractAddress |   string   | token合约地址    |  是   |
| amount          | biginteger | 转出的token资产金额 |  是   |
| remark          |   string   | 交易备注         |  否   |

#### 返回值
| 字段名    |  字段类型  | 参数描述   |
| ------ |:------:| ------ |
| txHash | string | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "tokentransfer",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "nuls123456", "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", "tNULSeBaN7GnASHHfknQ1a9ywiLsNsuim6ASwi", 80, "8个" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHash" : "f628c9787830ff67fe9a3e423899225d7b975add83f9ee505d4ce9d75a79b1fb"
  }
}
```

### 4.5 从账户地址向合约地址转账(主链资产)的合约交易
#### Cmd: transfer2contract
_**详细描述: 从账户地址向合约地址转账(主链资产)的合约交易**_

#### 参数列表
| 参数名         |    参数类型    | 参数描述      | 是否必填 |
| ----------- |:----------:| --------- |:----:|
| chainId     |    int     | 链id       |  是   |
| fromAddress |   string   | 转出者账户地址   |  是   |
| password    |   string   | 转出者账户密码   |  是   |
| toAddress   |   string   | 转入者账户地址   |  是   |
| amount      | biginteger | 转出的主链资产金额 |  是   |
| remark      |   string   | 交易备注      |  否   |

#### 返回值
| 字段名    |  字段类型  | 参数描述   |
| ------ |:------:| ------ |
| txHash | string | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "transfer2contract",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "nuls123456", "tNULSeBaNA4yaXmfaQVXpX3QWPcUaHRRryoXHa", 900000000, "向合约转账" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHash" : "6a10dea1845b189ff5542939df0ababa5c478fb9414884937212549af1603ad4"
  }
}
```

### 4.6 获取账户地址的指定合约的token余额
#### Cmd: getTokenBalance
_**详细描述: 获取账户地址的指定合约的token余额**_

#### 参数列表
| 参数名             |  参数类型  | 参数描述 | 是否必填 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | 链id  |  是   |
| contractAddress | string | 合约地址 |  是   |
| address         | string | 账户地址 |  是   |

#### 返回值
| 字段名             |  字段类型  | 参数描述                    |
| --------------- |:------:| ----------------------- |
| contractAddress | string | 合约地址                    |
| name            | string | token名称                 |
| symbol          | string | token符号                 |
| amount          | string | token数量                 |
| decimals        |  long  | token支持的小数位数            |
| blockHeight     |  long  | 合约创建时的区块高度              |
| status          |  int   | 合约状态(0-不存在, 1-正常, 2-终止) |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getTokenBalance",
  "params" : [ 2, "tNULSeBaMvkanBqyWF1h7MuQ22Pq3JkL8FTfsL", "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "contractAddress" : "tNULSeBaMvkanBqyWF1h7MuQ22Pq3JkL8FTfsL",
    "name" : "io",
    "symbol" : "IO",
    "amount" : "80",
    "decimals" : 1,
    "blockHeight" : 505,
    "status" : 1
  }
}
```

### 4.7 获取智能合约详细信息
#### Cmd: getContract
_**详细描述: 获取智能合约详细信息**_

#### 参数列表
| 参数名             |  参数类型  | 参数描述 | 是否必填 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | 链ID  |  是   |
| contractAddress | string | 合约地址 |  是   |

#### 返回值
| 字段名                                                                                                      |      字段类型       | 参数描述                                       |
| -------------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------ |
| createTxHash                                                                                             |     string      | 发布合约的交易hash                                |
| address                                                                                                  |     string      | 合约地址                                       |
| creater                                                                                                  |     string      | 合约创建者地址                                    |
| alias                                                                                                    |     string      | 合约别名                                       |
| createTime                                                                                               |      long       | 合约创建时间（单位：秒）                               |
| blockHeight                                                                                              |      long       | 合约创建时的区块高度                                 |
| isDirectPayable                                                                                          |     boolean     | 是否接受直接转账                                   |
| tokenType                                                                                                |       int       | token类型, 0 - 非token, 1 - NRC20, 2 - NRC721 |
| isNrc20                                                                                                  |     boolean     | 是否是NRC20合约                                 |
| nrc20TokenName                                                                                           |     string      | NRC20-token名称                              |
| nrc20TokenSymbol                                                                                         |     string      | NRC20-token符号                              |
| decimals                                                                                                 |      long       | NRC20-token支持的小数位数                         |
| totalSupply                                                                                              |     string      | NRC20-token发行总量                            |
| status                                                                                                   |     string      | 合约状态（not_found, normal, stop）              |
| method                                                                                                   | list&lt;object> | 合约方法列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | 方法名称                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | 方法描述                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | 方法参数列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | 参数类型                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | 参数名称                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | 是否必填                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | 返回值类型                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | 是否视图方法（调用此方法数据不上链）                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | 是否是事件                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | 是否是可接受主链资产转账的方法                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jsonSerializable                                         |     boolean     | 方法返回值是否JSON序列化                             |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getContract",
  "params" : [ 2, "tNULSeBaNA4yaXmfaQVXpX3QWPcUaHRRryoXHa" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "createTxHash" : "3a932a0bae1cd78c9e56264824a1ff9c96baf857bd799fe6941d0c4e98c19cf0",
    "address" : "tNULSeBaNA4yaXmfaQVXpX3QWPcUaHRRryoXHa",
    "creater" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
    "alias" : "offline_payable_jr",
    "createTime" : 1563270456,
    "blockHeight" : 427,
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
    }, 
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

### 4.8 获取智能合约执行结果
#### Cmd: getContractTxResult
_**详细描述: 获取智能合约执行结果**_

#### 参数列表
| 参数名     |  参数类型  | 参数描述   | 是否必填 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| hash    | string | 交易hash |  是   |

#### 返回值
| 字段名                                                                                                          |      字段类型       | 参数描述                                        |
| ------------------------------------------------------------------------------------------------------------ |:---------------:| ------------------------------------------- |
| success                                                                                                      |     boolean     | 合约执行是否成功                                    |
| errorMessage                                                                                                 |     string      | 执行失败信息                                      |
| contractAddress                                                                                              |     string      | 合约地址                                        |
| result                                                                                                       |     string      | 合约执行结果                                      |
| gasLimit                                                                                                     |      long       | GAS限制                                       |
| gasUsed                                                                                                      |      long       | 已使用GAS                                      |
| price                                                                                                        |      long       | GAS单价                                       |
| totalFee                                                                                                     |     string      | 交易总手续费                                      |
| txSizeFee                                                                                                    |     string      | 交易大小手续费                                     |
| actualContractFee                                                                                            |     string      | 实际执行合约手续费                                   |
| refundFee                                                                                                    |     string      | 合约返回的手续费                                    |
| value                                                                                                        |     string      | 调用者向合约地址转入的主网资产金额，没有此业务时则为0                 |
| stackTrace                                                                                                   |     string      | 异常堆栈踪迹                                      |
| transfers                                                                                                    | list&lt;object> | 合约转账列表（从合约转出主资产）                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                       |     string      | 合约生成交易：合约转账交易hash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                         |     string      | 转出的合约地址                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                        |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                                      | list&lt;object> | 转入的地址列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to           |     string      | 转入地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value        |     string      | 转入金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                                  |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）         |
| multyAssetTransfers                                                                                          | list&lt;object> | 合约转账列表（从合约转出其他资产）                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                       |     string      | 合约生成交易：合约转账交易hash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                         |     string      | 转出的合约地址                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                        |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId                                                 |       int       | 转账金额资产链ID                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId                                                      |       int       | 转账金额资产ID                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                                      | list&lt;object> | 转入的地址列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to           |     string      | 转入地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value        |     string      | 转入金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 转入金额资产链ID                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |       int       | 转入金额资产ID                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime     |      long       | 转入金额锁定时间                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                                  |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）         |
| events                                                                                                       | list&lt;string> | 合约事件列表                                      |
| debugEvents                                                                                                  | list&lt;string> | 调式合约事件列表                                    |
| tokenTransfers                                                                                               | list&lt;object> | 合约token转账列表                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                              |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                         |     string      | 付款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                           |     string      | 收款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                        |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                         |     string      | token名称                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                       |     string      | token符号                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                                     |      long       | token支持的小数位数                                |
| token721Transfers                                                                                            | list&lt;object> | 合约NRC721-token转账列表                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                              |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                         |     string      | 付款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                           |     string      | 收款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tokenId                                                      |     string      | tokenId                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                         |     string      | token名称                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                       |     string      | token符号                                     |
| invokeRegisterCmds                                                                                           | list&lt;object> | 合约调用外部命令的调用记录列表                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                                      |     string      | 命令名称                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                         |       map       | 命令参数，参数不固定，依据不同的命令而来，故此处不作描述，结构为 {参数名称=参数值} |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                              |     string      | 注册的命令模式（QUERY\_DATA or NEW\_TX）             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                                    |     string      | 生成的交易hash（当调用的命令模式是 NEW\_TX 时，会生成交易）        |
| contractTxList                                                                                               | list&lt;string> | 合约生成交易的序列化字符串列表                             |
| remark                                                                                                       |     string      | 备注                                          |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getContractTxResult",
  "params" : [ 2, "b8db1792fbfb6630a5106a1a92ee182aaa694aa1142454e81e4d8286e19ffc11" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
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
    "multyAssetTransfers" : [ {
      "txHash" : "21c7af81c5130f43a363152d3b81f96004fbaaeaeab8e50c988c04015f78770b",
      "from" : "tNULSeBaN31HBrLhXsWDkSz1bjhw5qGBcjafVJ",
      "value" : "200000000",
      "assetChainId" : 5,
      "assetId" : 1,
      "outputs" : [ {
        "to" : "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24",
        "value" : "200000000",
        "assetChainId" : 5,
        "assetId" : 1,
        "lockTime" : 0
      } ],
      "orginTxHash" : "755cdeabb704a77038d44c741b6c2b5635a60ffa58f652162559763f63623176"
    } ],
    "events" : [ "{\"contractAddress\":\"TTb1LZLo6izPGmXa9dGPmb5D2vpLpNqA\",\"blockNumber\":1343847,\"event\":\"TransferEvent\",\"payload\":{\"from\":\"TTasNs8MGGGaFT9hd9DLmkammYYv69vs\",\"to\":\"TTau7kAxyhc4yMomVJ2QkMVECKKZK1uG\",\"value\":\"1000\"}}" ],
    "debugEvents" : [ ],
    "tokenTransfers" : [ {
      "contractAddress" : "TTb1LZLo6izPGmXa9dGPmb5D2vpLpNqA",
      "from" : "TTasNs8MGGGaFT9hd9DLmkammYYv69vs",
      "to" : "TTau7kAxyhc4yMomVJ2QkMVECKKZK1uG",
      "value" : "1000",
      "name" : "a",
      "symbol" : "a",
      "decimals" : 8
    } ],
    "token721Transfers" : [ {
      "contractAddress" : "NULSd6Hgrsk44itdzFqjgkgAF6nFM82WdpqrQ",
      "from" : "NULSd6Hgd3ACi95QvpLBfp3jgJP3YFmEpbgoG",
      "to" : "NULSd6HgcbwRjN8AxpPK8TvJWtzBzMQ1zDhVd",
      "tokenId" : "13450",
      "name" : "nft",
      "symbol" : "NFT"
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
    "contractTxList" : [ "12002fbb225d0037b5473eefecd1c70ac4276f70062a92bdbfe8f779cbe48de2d0315686cc7e678902000253472f4702eb83b71871a4c4e0c71526bb86b8afd0011702000253472f4702eb83b71871a4c4e0c71526bb86b8af0200010000c2eb0b0000000000000000000000000000000000000000000000000000000008000000000000000000021702000194f6239c075d184e265eaea97a67eeced51725160200010000e1f50500000000000000000000000000000000000000000000000000000000000000000000000017020001ce8ffa95606f0bfd2778cff2eff8fe8999e20c440200010000e1f50500000000000000000000000000000000000000000000000000000000000000000000000000", "12009cbbf25f0037755cdeabb704a77038d44c741b6c2b5635a60ffa58f652162559763f6362317602000265f22046ba64eb216854390877d0f52348ded8be8c011702000265f22046ba64eb216854390877d0f52348ded8be0500010000c2eb0b00000000000000000000000000000000000000000000000000000000080000000000000000000117020001bc9cf2a09f0d1dbe7ab0a7dca2ccb87d12da6a990500010000c2eb0b00000000000000000000000000000000000000000000000000000000000000000000000000", "1400bf6b285d006600204aa9d1010000000000000000000000000000000000000000000000000000020002f246b18e8c697f00ed9bd22696998e469d3f824b020001d7424d91c83566eb94233b5416f2aa77709c03e1020002f246b18e8c697f00ed9bd22696998e469d3f824b648c0117020002f246b18e8c697f00ed9bd22696998e469d3f824b0200010000204aa9d1010000000000000000000000000000000000000000000000000000080000000000000000000117020002f246b18e8c697f00ed9bd22696998e469d3f824b0200010000204aa9d1010000000000000000000000000000000000000000000000000000ffffffffffffffff00" ],
    "remark" : "call"
  }
}
```

### 4.9 获取智能合约执行结果列表
#### Cmd: getContractTxResultList
_**详细描述: 获取智能合约执行结果列表**_

#### 参数列表
| 参数名      |      参数类型       | 参数描述     | 是否必填 |
| -------- |:---------------:| -------- |:----:|
| chainId  |       int       | 链ID      |  是   |
| hashList | list&lt;string> | 交易hash列表 |  是   |

#### 返回值
| 字段名                                                                                                                                                          |      字段类型       | 参数描述                                        |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------ |:---------------:| ------------------------------------------- |
| hash1 or hash2 or hash3...                                                                                                                                   |     object      | 以交易hash列表中的hash值作为key，这里的key name是动态的       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;success                                                                                                      |     boolean     | 合约执行是否成功                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;errorMessage                                                                                                 |     string      | 执行失败信息                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                                                                              |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;result                                                                                                       |     string      | 合约执行结果                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit                                                                                                     |      long       | GAS限制                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasUsed                                                                                                      |      long       | 已使用GAS                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                                                                        |      long       | GAS单价                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalFee                                                                                                     |     string      | 交易总手续费                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txSizeFee                                                                                                    |     string      | 交易大小手续费                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;actualContractFee                                                                                            |     string      | 实际执行合约手续费                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;refundFee                                                                                                    |     string      | 合约返回的手续费                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                                                                        |     string      | 调用者向合约地址转入的主网资产金额，没有此业务时则为0                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;stackTrace                                                                                                   |     string      | 异常堆栈踪迹                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transfers                                                                                                    | list&lt;object> | 合约转账列表（从合约转出主资产）                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                       |     string      | 合约生成交易：合约转账交易hash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                         |     string      | 转出的合约地址                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                        |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                                      | list&lt;object> | 转入的地址列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to           |     string      | 转入地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value        |     string      | 转入金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                                  |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;multyAssetTransfers                                                                                          | list&lt;object> | 合约转账列表（从合约转出其他资产）                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                       |     string      | 合约生成交易：合约转账交易hash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                         |     string      | 转出的合约地址                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                        |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId                                                 |       int       | 转账金额资产链ID                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId                                                      |       int       | 转账金额资产ID                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                                      | list&lt;object> | 转入的地址列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to           |     string      | 转入地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value        |     string      | 转入金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetChainId |       int       | 转入金额资产链ID                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId      |       int       | 转入金额资产ID                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime     |      long       | 转入金额锁定时间                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                                  |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;events                                                                                                       | list&lt;string> | 合约事件列表                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;debugEvents                                                                                                  | list&lt;string> | 调式合约事件列表                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tokenTransfers                                                                                               | list&lt;object> | 合约token转账列表                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                              |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                         |     string      | 付款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                           |     string      | 收款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                        |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                         |     string      | token名称                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                       |     string      | token符号                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                                     |      long       | token支持的小数位数                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;token721Transfers                                                                                            | list&lt;object> | 合约NRC721-token转账列表                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                              |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                         |     string      | 付款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                           |     string      | 收款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tokenId                                                      |     string      | tokenId                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                         |     string      | token名称                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                       |     string      | token符号                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;invokeRegisterCmds                                                                                           | list&lt;object> | 合约调用外部命令的调用记录列表                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                                      |     string      | 命令名称                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                         |       map       | 命令参数，参数不固定，依据不同的命令而来，故此处不作描述，结构为 {参数名称=参数值} |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                              |     string      | 注册的命令模式（QUERY\_DATA or NEW\_TX）             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                                    |     string      | 生成的交易hash（当调用的命令模式是 NEW\_TX 时，会生成交易）        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractTxList                                                                                               | list&lt;string> | 合约生成交易的序列化字符串列表                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                                                                       |     string      | 备注                                          |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getContractTxResultList",
  "params" : [ 2, [ "c2460b94430074dd98e497ed9d48afb8f44d1323b73ca2086f5abaa0684b760d", "48b2f348f201f9d10848f4031a746919470b679f621327b0e0edf50a339f2e87", "2e99610b7d295790b636fcdb8acf72d70fcae61c873df0984ef248bbbaa6daa2" ] ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "c2460b94430074dd98e497ed9d48afb8f44d1323b73ca2086f5abaa0684b760d" : {
      "success" : true,
      "errorMessage" : null,
      "contractAddress" : "tNULSeBaN5Y2gRias1NMNVmsmXqJbu5Bcp3ZPL",
      "result" : null,
      "gasLimit" : 20000,
      "gasUsed" : 13429,
      "price" : 30,
      "totalFee" : "1300000",
      "txSizeFee" : "700000",
      "actualContractFee" : "402870",
      "refundFee" : "197130",
      "value" : "0",
      "stackTrace" : null,
      "transfers" : [ ],
      "multyAssetTransfers" : [ ],
      "events" : [ "{\"contractAddress\":\"tNULSeBaN5Y2gRias1NMNVmsmXqJbu5Bcp3ZPL\",\"blockNumber\":68,\"event\":\"TransferEvent\",\"payload\":{\"from\":null,\"to\":\"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG\",\"value\":\"800000\"}}" ],
      "debugEvents" : [ ],
      "tokenTransfers" : [ {
        "contractAddress" : "tNULSeBaN5Y2gRias1NMNVmsmXqJbu5Bcp3ZPL",
        "from" : null,
        "to" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
        "value" : "800000",
        "name" : "io",
        "symbol" : "IO",
        "decimals" : 1
      } ],
      "token721Transfers" : [ ],
      "invokeRegisterCmds" : [ ],
      "contractTxList" : [ ],
      "remark" : "create"
    },
    "48b2f348f201f9d10848f4031a746919470b679f621327b0e0edf50a339f2e87" : {
      "success" : true,
      "errorMessage" : null,
      "contractAddress" : "tNULSeBaN5Y2gRias1NMNVmsmXqJbu5Bcp3ZPL",
      "result" : "true",
      "gasLimit" : 200000,
      "gasUsed" : 9444,
      "price" : 30,
      "totalFee" : "6100000",
      "txSizeFee" : "100000",
      "actualContractFee" : "283320",
      "refundFee" : "5716680",
      "value" : "0",
      "stackTrace" : null,
      "transfers" : [ ],
      "multyAssetTransfers" : [ ],
      "events" : [ "{\"contractAddress\":\"tNULSeBaN5Y2gRias1NMNVmsmXqJbu5Bcp3ZPL\",\"blockNumber\":71,\"event\":\"TransferEvent\",\"payload\":{\"from\":\"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG\",\"to\":\"tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM\",\"value\":\"4000\"}}" ],
      "debugEvents" : [ ],
      "tokenTransfers" : [ {
        "contractAddress" : "tNULSeBaN5Y2gRias1NMNVmsmXqJbu5Bcp3ZPL",
        "from" : "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
        "to" : "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM",
        "value" : "4000",
        "name" : "io",
        "symbol" : "IO",
        "decimals" : 1
      } ],
      "token721Transfers" : [ ],
      "invokeRegisterCmds" : [ ],
      "contractTxList" : [ ],
      "remark" : "call"
    },
    "2e99610b7d295790b636fcdb8acf72d70fcae61c873df0984ef248bbbaa6daa2" : {
      "success" : true,
      "errorMessage" : null,
      "contractAddress" : "tNULSeBaN5Y2gRias1NMNVmsmXqJbu5Bcp3ZPL",
      "result" : "true",
      "gasLimit" : 200000,
      "gasUsed" : 5836,
      "price" : 30,
      "totalFee" : "6100000",
      "txSizeFee" : "100000",
      "actualContractFee" : "175080",
      "refundFee" : "5824920",
      "value" : "0",
      "stackTrace" : null,
      "transfers" : [ ],
      "multyAssetTransfers" : [ ],
      "events" : [ "{\"contractAddress\":\"tNULSeBaN5Y2gRias1NMNVmsmXqJbu5Bcp3ZPL\",\"blockNumber\":72,\"event\":\"ApprovalEvent\",\"payload\":{\"owner\":\"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG\",\"spender\":\"tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM\",\"value\":\"4000\"}}" ],
      "debugEvents" : [ ],
      "tokenTransfers" : [ ],
      "token721Transfers" : [ ],
      "invokeRegisterCmds" : [ ],
      "contractTxList" : [ ],
      "remark" : "call"
    }
  }
}
```

### 4.10 获取合约代码构造函数
#### Cmd: getContractConstructor
_**详细描述: 获取合约代码构造函数**_

#### 参数列表
| 参数名          |  参数类型  | 参数描述                 | 是否必填 |
| ------------ |:------:| -------------------- |:----:|
| chainId      |  int   | 链ID                  |  是   |
| contractCode | string | 智能合约代码(字节码的Hex编码字符串) |  是   |

#### 返回值
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
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jsonSerializable                                         |     boolean     | 方法返回值是否JSON序列化     |
| isNrc20                                                                                                  |     boolean     | 是否是NRC20合约         |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getContractConstructor",
  "params" : [ 2, "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbac199728c6e24e143921f29c3836a9583d80101e64120d92d1980760970bb04b454e53d24105b0665a7fe22279d041d21e750cb7df404705c49ad92096aa9d134ce624bbcff200687701b4ab9ad494340f80075c00136c0151d15d01b0b602404d5c45671d1c59e2c8d1cdbc0747b78ba3dbc5d14d1febe2305c1c978943623c142586ae8aedfdf1c45544ce62859a60e53040e7a057b0b6cc83d5efe9f022a35c223083e735bcc0cd0c66b937abe2450d2770218697f838c7cd7c0c176358e0ee256e5e56b1a822c71075deb64d97e1d49ce564ed72c9cb161cdb778d829ff58a6bd98962d1353d6f9841f5364cbbc853a39b46a96c3274cd5d37368decbae1af6627ad9559db37574c97529511cbb6fc3186cb89a3773d66b6e1f6c93c833ce5140940eb9c659b17cbebcba6bb642c9728d231e7148c52de702dfe42af09eb2d8ab92d4ee80abd8e112b1162ba060d8148b540720d81641a03916a814c50cce419407a2220596a743d10b916c84c4320c322a80e885c0be40ec5dc3d0308e78b27a68b1e9df517f970ea5ea433df232eef670ed192ae229ea1df77884bfb99dfa12c70de0e699a68c9d2af0af55b24f7a065859dbc783f4307896cd24907e9c8413e441b5fc8265791d843926b7f4095f7204bfb04421265f41250200f150bd4b94582f9804a5ec2975816650d84808fcb7a41ff266e8a724a18215b0cef8be1d4d516f637fa557ca0b06e85756a93bc747adba2d27f8eda381b56d79c1695357130a3ad57fb36c39ab8598acc4f8fec7268a732b4d0def70a7ebbb8398fe826add2f0ac119b8f4fb0311bc1ee20d01fe22382c2c10e41699b64ffd20d8f115c82493a223dc658e1f42705d1531a55f1848ee58332f22b2eadbe84dede5c25064529c9f6d65097b8de16ea8439d9fa4d15ed5574d40eded313833712018d614cc8715c8f9e19051fd393d4f41f504b0708826261e37e040000ca090000504b01020a000a0000080000aa7b564e000000000000000000000000030004000000000000000000000000000000696f2ffeca0000504b01020a000a0000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e00000000000000000000000011000000000000000000000000004b000000696f2f6e756c732f636f6e74726163742f504b01020a000a0000080000aa7b564e00000000000000000000000017000000000000000000000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
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

### 4.11 获取合约方法信息
#### Cmd: getContractMethod
_**详细描述: 获取合约方法信息**_

#### 参数列表
| 参数名             |  参数类型  | 参数描述 | 是否必填 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | 链ID  |  是   |
| contractAddress | string | 合约地址 |  是   |
| methodName      | string | 方法名称 |  是   |
| methodDesc      | string | 方法描述 |  否   |

#### 返回值
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
| jsonSerializable                                         |     boolean     | 方法返回值是否JSON序列化     |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getContractMethod",
  "params" : [ 2, "tNULSeBaMvkanBqyWF1h7MuQ22Pq3JkL8FTfsL", "transfer", null ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
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
  }
}
```

### 4.12 获取合约方法参数类型
#### Cmd: getContractMethodArgsTypes
_**详细描述: 获取合约方法参数类型**_

#### 参数列表
| 参数名             |  参数类型  | 参数描述 | 是否必填 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | 链ID  |  是   |
| contractAddress | string | 合约地址 |  是   |
| methodName      | string | 方法名称 |  是   |
| methodDesc      | string | 方法描述 |  否   |

#### 返回值
| 字段名 |      字段类型       | 参数描述 |
| --- |:---------------:| ---- |
| 返回值 | list&lt;string> |      |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getContractMethodArgsTypes",
  "params" : [ 2, "tNULSeBaMvkanBqyWF1h7MuQ22Pq3JkL8FTfsL", "transfer", null ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : [ "Address", "BigInteger" ]
}
```

### 4.13 验证发布合约
#### Cmd: validateContractCreate
_**详细描述: 验证发布合约**_

#### 参数列表
| 参数名          |   参数类型   | 参数描述                 | 是否必填 |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | 链id                  |  是   |
| sender       |  string  | 交易创建者账户地址            |  是   |
| gasLimit     |   long   | GAS限制                |  是   |
| price        |   long   | GAS单价                |  是   |
| contractCode |  string  | 智能合约代码(字节码的Hex编码字符串) |  是   |
| args         | object[] | 参数列表                 |  否   |

#### 返回值
| 字段名     |  字段类型   | 参数描述      |
| ------- |:-------:| --------- |
| success | boolean | 验证成功与否    |
| code    | string  | 验证失败的错误码  |
| msg     | string  | 验证失败的错误信息 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "validateContractCreate",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", 20000, 30, "504b0304140008080800038b2d4d000000000000000000000000090004004d4554412d494e462ffeca00000300504b0708000000000200000000000000504b0304140008080800038b2d4d000000000000000000000000140000004d4554412d494e462f4d414e49464553542e4d46f34dcccb4c4b2d2ed10d4b2d2acecccfb35230d433e0e5722e4a4d2c494dd175aa040958e819c41b9a982868f8172526e7a42a38e71715e417259600d56bf272f1720100504b07089e7c76534400000045000000504b03040a0000080000fb8a2d4d0000000000000000000000001b00000074657374636f6e74726163742f6d756c74797472616e736665722f504b0304140008080800fb8a2d4d0000000000000000000000003200000074657374636f6e74726163742f6d756c74797472616e736665722f546573744d756c74795472616e736665722e636c6173739d56eb7313d715ffad257997454e8cea00f210aa262d91658cb00c989a94c498180cb6f143d8d86d4256d25a5a4bda95b52b83d31749daa4e933e9234ddf8f90a68fb40d2d96dd64265f3ac34cfb0ff463ff857eea97cc747acedd95ac48824e6a8ff69e7b9ebf73ef3967f7efff79e73d00c7f08e8a53585590df85fb505050e4d5e48725a3a442c62a3fd6149479b515382cacc85857710dd75584b0a1e219d8323eabe073bcff7c00fcf7daab3758f005667d51c18d1af75905cf0a5a1a1c8082e75cfa6f7fbda1e079155fc29715bcc0be5f64435ba5685f51f09282af32f36b8ce2eb0abec19b6ff2e65b0a5ee6cd2b0abeade03b2abe8beff1e35505dfe7f535053f50f043267fa4e0c7327e22e3a712fca656d425842657b5752d5ed0cc6c7cde291b66f694844e7ba398b20a12bac62cd37634d359d00a15d256327ada286a055b8234e123d82109b256284c982b1649af96b40d2d5520455fb46f81628c5919dadc3f6998fa74a598d2cb49571c9ab4d25a61412b1bbcf7987e276790e323938e6e3b69cb74ca5ada89172b05678348d35ed1cbf12489a69893f43884363c57311da3a82f18b6418e464dd37234c720e412fa260d2b6e560a76bceed0cee4e35a5d273ee342e6ac1f354cc3394d196575675a9c4e4fb4afddf9ec228579ef8876137db67e2a94f7043948696490d6f924c98293dba5653265ddb6af0e4a38d81ed4a8ab41fe0322e906a344033d44072ae4e356d9339170337a6f9f6e1645cdc9c5cf18d909d3d1b37476ff974dcb79b4d76b7b707e4a62904b8696aba626616f7b635733e16a26846617514394b2ebcb950cb99279474be7a7b49257479d5aa9a49b19090f445b3170592aae825e96a0ce5b95725a1f37d8706f4b751d61fb20a6312e614f8b34884f81caa523990ce2319c0e62146724ec6b8e79a6621432acade367748f9181c8e8e2e0f1939783f83966242088b3ece6f087297b19bf08e297783d88246eca7843c2817bdda584c8b49d71b2b3e746af5f99c91d1fb296af8f2eae2e94ccf3e6c5e162e5d8b525ce7225885fe1cd207e8ddf1050af884722c25a3fbe9c3766edc1d9c152e60967eee2b52bf913d3f91396b554494c26bc001b99e4c2a29d9f4a0f9fcb2f3f93b2d712e3d6586eede2f8b5a91509fb3d3491b4e8bf484a8fe8c592b37144c66f83f81ddea2a0aa2d6e662412c4ef393d1d64a8aa5ef13316455de761c41a7fc04dd6f8236b6435bba0af38cc7e1bb798fd276a4e6697ca465aa8ff19b7a843555595d0ada6e848cf14ac74febc66e74622326e07b1c986556cb161da32cc94660bc36dbcced072ba91cd398c21a8f2c0a1c9582cb1fc2fec5862b73b777f29b5aaa71d090fb6bd97316f2361a05d91b6b0bc1aa2c6501cabd602fbdbba9eb2bdeef02e3e7a9719b8336ef6d7a65c6b1bf6b4e3937b710597e86ac2d10b7735566af5cb6a779912d48ebd6de15d760c1ea872595fab1865eecee872fb76eeb4bd660eacbb6fa90f1ca97b0ff73c52d9ab1d31be2f502bd5736a7f03b59aba1b74515684cb14af3cee2466e4a8ce7826b5f1cd4e6bf546eaf5dac2c7e8e57f0a121e851f1d3c6df895cbb386788f13dd41fc108f9dfa7e8c7e6785de6ee23d8171da9fa35d3f7f63f0ef96503b4fcf4ec18863023c838410177091d6494f3a257834fde8c94e36e0137ac3b12d48b150c7267cb1907f1381587768139def425eda82125bbe0d5f15bb422a3daad82db4829be8da097d90be5b804142ff085444d18d3eec450247318413f455b60369d88324e112663c18715a59168851a0b79bf2196e300ed48d67ebc6039eb12fe46f363dd560eaab9bce61bed9b43bf45693e9636d4d93f5a8c73c53751bf757d15dc59ee6e0630d1ed4ba87cb750f05f2db41ebd177115a0a7d640b3d93fd3d78601bfbaad8ef9d7c284c275e8fe09efe4e9cfbc05f6ce7c8fb79f450348eb7d7f5e9c55371080b581458af5064b774b6c9ce4feb950f44eead4526de01e24d1deec1830dbc8f126f7aa007910f81d02d8b4b84638610ced17e9e8a23498571995a6051208eb958ea88a73dc407318225a23a48fb109689f235d5f1a7f1192fa37f924ca175a6ff0eba07ee20180edc81ec7f137e5fe8a16d3cdc7f98100f84fd5e2ee100e5322287e570e7ffcec42732e9a5b8a078fbf0240ee0293c84ab543e4f531d68228b3937be9705534f8a2c987a8a743b04f5b4c882298d787e41a5a8b903824a13af93bc3e2cb295911115dfb1e771a929f1b3b5229266c90907793f56cbe2e3228b4f5471c84d610b8fd465d15a866d647d4216aba2bf4970580806aa38d224880bc1d11d4168b0519c10e2a16d1c6b0d765cc84eb4061b768d5a839d14824f36041be1e76d74ed545b82ba0d58a1fbc9e2247234230cacd2fff3c8e325eab73750c47b30f10f58f8174af837d61a7af47defee3af813c5ebd139af477b1b26611721e9af0dc28611a80acd0ac2586fe8c4de7a45f412aad3a21373c2c6f82f504b07087c257e1c05070000b30e0000504b01021400140008080800038b2d4d0000000002000000000000000900040000000000000000000000000000004d4554412d494e462ffeca0000504b01021400140008080800038b2d4d9e7c7653440000004500000014000000000000000000000000003d0000004d4554412d494e462f4d414e49464553542e4d46504b01020a000a0000080000fb8a2d4d0000000000000000000000001b00000000000000000000000000c300000074657374636f6e74726163742f6d756c74797472616e736665722f504b01021400140008080800fb8a2d4d7c257e1c05070000b30e00003200000000000000000000000000fc00000074657374636f6e74726163742f6d756c74797472616e736665722f546573744d756c74795472616e736665722e636c617373504b0506000000000400040026010000610800000000", [ ] ],
  "id" : 1234
}
```

#### Example response data: 

```json
[ {
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "success" : true
  }
}, "校验失败示例请参考[validateContractDelete] - 验证删除合约" ]
```

### 4.14 验证调用合约
#### Cmd: validateContractCall
_**详细描述: 验证调用合约**_

#### 参数列表
| 参数名              |    参数类型    | 参数描述                                                                      | 是否必填 |
| ---------------- |:----------:| ------------------------------------------------------------------------- |:----:|
| chainId          |    int     | 链id                                                                       |  是   |
| sender           |   string   | 交易创建者账户地址                                                                 |  是   |
| value            | biginteger | 调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO                                  |  是   |
| gasLimit         |    long    | GAS限制                                                                     |  是   |
| price            |    long    | GAS单价                                                                     |  是   |
| contractAddress  |   string   | 合约地址                                                                      |  是   |
| methodName       |   string   | 合约方法                                                                      |  是   |
| methodDesc       |   string   | 合约方法描述，若合约内方法没有重载，则此参数可以为空                                                |  否   |
| args             |  object[]  | 参数列表                                                                      |  否   |
| multyAssetValues | string[][] | 调用者向合约地址转入的其他资产金额，没有此业务时填空，规则: [[\<value\>,\<assetChainId\>,\<assetId\>]] |  否   |

#### 返回值
| 字段名     |  字段类型   | 参数描述      |
| ------- |:-------:| --------- |
| success | boolean | 验证成功与否    |
| code    | string  | 验证失败的错误码  |
| msg     | string  | 验证失败的错误信息 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "validateContractCall",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", 80000000000, 200000, 30, "tNULSeBaNA4yaXmfaQVXpX3QWPcUaHRRryoXHa", "multyForAddress", null, [ "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM", "400000000", "tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29", "900000000", "tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf", "800000000" ], null ],
  "id" : 1234
}
```

#### Example response data: 

```json
[ {
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "success" : true
  }
}, "校验失败示例请参考[validateContractDelete] - 验证删除合约" ]
```

### 4.15 验证删除合约
#### Cmd: validateContractDelete
_**详细描述: 验证删除合约**_

#### 参数列表
| 参数名             |  参数类型  | 参数描述      | 是否必填 |
| --------------- |:------:| --------- |:----:|
| chainId         |  int   | 链id       |  是   |
| sender          | string | 交易创建者账户地址 |  是   |
| contractAddress | string | 合约地址      |  是   |

#### 返回值
| 字段名     |  字段类型   | 参数描述      |
| ------- |:-------:| --------- |
| success | boolean | 验证成功与否    |
| code    | string  | 验证失败的错误码  |
| msg     | string  | 验证失败的错误信息 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "validateContractDelete",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "tNULSeBaMvkanBqyWF1h7MuQ22Pq3JkL8FTfsL" ],
  "id" : 1234
}
```

#### Example response data: 

```json
[ {
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "success" : true
  }
}, {
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "msg" : "Cannot delete contract when balance is not 0;Cannot delete contract when balance is not 0",
    "success" : false,
    "code" : "sc_0017"
  }
} ]
```

### 4.16 估算发布合约交易的GAS
#### Cmd: imputedContractCreateGas
_**详细描述: 估算发布合约交易的GAS**_

#### 参数列表
| 参数名          |   参数类型   | 参数描述                 | 是否必填 |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | 链id                  |  是   |
| sender       |  string  | 交易创建者账户地址            |  是   |
| contractCode |  string  | 智能合约代码(字节码的Hex编码字符串) |  是   |
| args         | object[] | 参数列表                 |  否   |

#### 返回值
| 字段名      | 字段类型 | 参数描述              |
| -------- |:----:| ----------------- |
| gasLimit | long | 消耗的gas值，执行失败返回数值1 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "imputedContractCreateGas",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb302b5d048ae386e16f614f2590d47be917bb5bdeb3bc6121c57ea7da53cc3e974f181710d772c60af6ec0828e129242c5030d772e84be2ebea0313555c84f53dd722b972a9846d11c543306c92a6ca4d3667ad48eef7232d79cf565c5f29832e66e0ae53315510ebb49fabf84973d1032ec2d1babe2260f2ed77eb868565f96550e559739b3ab12d7af19cc5109a6bdc583c0d5ba283569a64e90657aeb9f4fd659ab7fa24115ffc68a40df63f3eaf0528b9cf7678b131a06ff27bbb5358547f2e542cadc6349cb2df7306bbf6cd7d18fa48edb65d8884d3a36638b8e217ca0e3436cd1b0a27a6fdc6d1427d9ac3abe8d3b74dc25c35f64f347f858c727381fc4051d177149c7a7b84c42abc3edca76cf61a74586a64da1c82573def5e11ca96477c62816cda2e0213d5cd17115333aaee13a0fccbacf782dd58a888e49983a72c8e83821c367b8a1c382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe274703aa254cbe6691d5fe0bc8ea36087270e4f9a11551d916cb9588a8c999109f6312f864869d2c845f2850819dfc8f078e4f67e394e289db32c389413c98fdb36fac5c52f79e32c7cafb216aae57260ecb899a2f6b535fb6bb7f3a261cd7c5153772c0b2e706ce8d001be1c1edacf712a7f527a6bb82e6f86b2e54cc9529f59fdf55ab0ded9d69a78f749eb34149d5b29c22b7f812bcc3f55a6735bbdf7be1d916db7cef4dd3aa5a1bda68123ac4e524ec0cc5a54df5587bd544dd93d3f6172df861a306a1a0d16ec0f025ae83d26e71b53f9ec9451607ee761337e04f9c9868c7b85097943f71eabc51de8e637750c3ec49140039a842af8e3a049d8423d49187cfa8433d493c400d2b492b7f2ef2ecffb003fd699616ca3bc9d3349be697c2e8a5e8676416df90ec70635b9163b38eaf6067c173bf91c745677f1a911422d45beb90000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e00000000000000000000000011000000000000000000000000004b000000696f2f6e756c732f636f6e74726163742f504b01020a000a0000080000aa7b564e00000000000000000000000017000000000000000000000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000", [ "io", "IO", 80000, 1 ] ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "gasLimit" : 20143
  }
}
```

### 4.17 估算调用合约交易的GAS
#### Cmd: imputedContractCallGas
_**详细描述: 估算调用合约交易的GAS**_

#### 参数列表
| 参数名              |    参数类型    | 参数描述                                                                      | 是否必填 |
| ---------------- |:----------:| ------------------------------------------------------------------------- |:----:|
| chainId          |    int     | 链id                                                                       |  是   |
| sender           |   string   | 交易创建者账户地址                                                                 |  是   |
| value            | biginteger | 调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO                                  |  是   |
| contractAddress  |   string   | 合约地址                                                                      |  是   |
| methodName       |   string   | 合约方法                                                                      |  是   |
| methodDesc       |   string   | 合约方法描述，若合约内方法没有重载，则此参数可以为空                                                |  否   |
| args             |  object[]  | 参数列表                                                                      |  否   |
| multyAssetValues | string[][] | 调用者向合约地址转入的其他资产金额，没有此业务时填空，规则: [[\<value\>,\<assetChainId\>,\<assetId\>]] |  否   |

#### 返回值
| 字段名      | 字段类型 | 参数描述              |
| -------- |:----:| ----------------- |
| gasLimit | long | 消耗的gas值，执行失败返回数值1 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "imputedContractCallGas",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", 80000000000, "tNULSeBaNA4yaXmfaQVXpX3QWPcUaHRRryoXHa", "multyForAddress", null, [ "tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM", "400000000", "tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29", "900000000", "tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf", "8045645645" ], null ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "gasLimit" : 44691
  }
}
```

### 4.18 调用合约不上链方法
#### Cmd: invokeView
_**详细描述: 调用合约不上链方法**_

#### 参数列表
| 参数名             |   参数类型   | 参数描述                       | 是否必填 |
| --------------- |:--------:| -------------------------- |:----:|
| chainId         |   int    | 链id                        |  是   |
| contractAddress |  string  | 合约地址                       |  是   |
| methodName      |  string  | 合约方法                       |  是   |
| methodDesc      |  string  | 合约方法描述，若合约内方法没有重载，则此参数可以为空 |  否   |
| args            | object[] | 参数列表                       |  否   |

#### 返回值
| 字段名    |  字段类型  | 参数描述      |
| ------ |:------:| --------- |
| result | string | 视图方法的调用结果 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "invokeView",
  "params" : [ 2, "tNULSeBaMvkanBqyWF1h7MuQ22Pq3JkL8FTfsL", "balanceOf", null, [ "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD" ] ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "result" : "80"
  }
}
```

### 4.19 离线 - 发布合约交易
#### Cmd: contractCreateOffline
_**详细描述: 离线 - 发布合约交易**_

#### 参数列表
| 参数名           |    参数类型    | 参数描述                 | 是否必填 |
| ------------- |:----------:| -------------------- |:----:|
| chainId       |    int     | 链id                  |  是   |
| sender        |   string   | 交易创建者账户地址            |  是   |
| senderBalance | biginteger | 账户余额                 |  是   |
| nonce         |   string   | 账户nonce值             |  是   |
| alias         |   string   | 合约别名                 |  是   |
| contractCode  |   string   | 智能合约代码(字节码的Hex编码字符串) |  是   |
| gasLimit      |    long    | 设置合约执行消耗的gas上限       |  是   |
| args          |  object[]  | 参数列表                 |  否   |
| argsType      |  string[]  | 参数类型列表               |  否   |
| remark        |   string   | 交易备注                 |  否   |

#### 返回值
| 字段名             |  字段类型  | 参数描述     |
| --------------- |:------:| -------- |
| hash            | string | 交易hash   |
| txHex           | string | 交易序列化字符串 |
| contractAddress | string | 生成的合约地址  |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "contractCreateOffline",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "999999998523475", "9c0aea02bed90ddd", "off_nrc20", "504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47151b6a7b62b87b92a39679924afcbd7d4c6b6b05fdada96cd86ee5acd20e0d3b7a6f75b0decc5c35c37da31a968822cbc80c66f304ae61dd6cfc078d02ebb164166639125dd09143e64365ab60a6a5b2541548e16938dcbb4032e75dad1b8e45f99339b3a0a173fef3acc8e294994bcb565d1d19ac14e8a269235336a5796655c0a9a94a152c192919a9132c47f56eb382b910ba4a2755d0cd7ad79116adf7e05e1d79301e7fc5df747c8ef31a3a8673c5f2f8b895b2b82fe27841b91affa3c939c7bf3f3573f636aed1d3d8099a47488871aee9018d71bac54a8cd3aad17e1ffae9fb75fa2ad32ad3da997e0996ce1c2096ce1e404a0f1d40fe51445d21d98538c92c790ea109c36881810e1a82ab644d87f118c01b80d0785e26349e3926349e5bc20dd293120986374fc8b7f036490ec7a09587c6d33f21f6c3717e45186f8a7c7ae810e5634861300a1e276f9e4d1175bc42af09eb2d8ab92d4ee80abd8e112b1162ba060d8148b540720d81641a03916a814c50cce419407a2220596a743d10b916c84c4320c322a80e885c0be40ec5dc3d0308e78b27a68b1e9df517f970ea5ea433df232eef670ed192ae229ea1df77884bfb99dfa12c70de0e699a68c9d2af0af55b24f7a065859dbc783f4307896cd24907e9c8413e441b5fc8265791d843926b7f4095f7204bfb04421265f41250200f150bd4b94582f9804a5ec2975816650d84808fcb7a41ff266e8a724a18215b0cef8be1d4d516f637fa557ca0b06e85756a93bc747adba2d27f8eda381b56d79c1695357130a3ad57fb36c39ab8598acc4f8fec7268a732b4d0def70a7ebbb8398fe826add2f0ac119b8f4fb0311bc1ee20d01fe22382c2c10e41699b64ffd20d8f115c82493a223dc658e1f42705d1531a55f1848ee58332f22b2eadbe84dede5c25064529c9f6d65097b8de16ea8439d9fa4d15ed5574d40eded313833712018d614cc8715c8f9e19051fd393d4f41f504b0708826261e37e040000ca090000504b01020a000a0000080000aa7b564e000000000000000000000000030004000000000000000000000000000000696f2ffeca0000504b01020a000a0000080000aa7b564e000000000000000000000000080000000000000000000000000025000000696f2f6e756c732f504b01020a000a0000080000aa7b564e03742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000", 20245, [ "air", "AIR", 10000, 2 ], [ "String", "String", "BigInteger", "int" ], "offline create" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0f00212d375d0e6f66666c696e6520637265617465fd7a19020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000219465936f10d20dd74de26ce391f9764acfcd445fd1a19504b03040a0000080000aa7b564e00000000000000000000000003000400696f2ffeca0000504b03040a0000080000aa7b564e00000000000000000000000008000000696f2f6e756c732f504b03040a0000080000aa7b564e00000000000000000000000011000000696f2f6e756c732f636f6e74726163742f504b03040a0000080000aa7b564e00000000000000000000000017000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b0304140008080800aa7b564e00000000000000000000000028000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373b558f97754e5197eeecc90990c972d9090059209a498cc9291a58a8152020d35ca5602b1605bbd99b9492ecc12670922b46ead4babd56aeb5ad1565bd4aa2c020169d59ed353cfe93fd17f84d3d3e7fdee9d3b37612639d5d31ff2ddf77ecbfb3eeff67c77f2afff7cf639804df87b23a23825c323329c96e14c1831fcb411cdf859108f86119497281e93e171997e22cce149197e1ec62ff0d462acc2d3413c13c672fc2a8ce7f0bcccfc3a8817827831cc53bf91e125195e96e5df8af4bb205e09a3430e44f19a0cafcbf086a87d5386df87f096cc9c9597b7457a27843fc8f38f21bc2b7ade0be24f41fc39887341bcaf219033b2a686a6bdc78d692399317213c99152c1ca4d6cd3d0503c951dcb673484d266caca1a99a2066d58c3e252be646446ca535399531a5aeca359a33499dc654d0ce74ae68459e0f1d098417d2993a796d97bca252b93dc674c71b171c49ac819a57281c68766af6edf6be593b972a6984ce573a582912a258be913c9c174ba60168bdb6a9bdb419d412393c99f34d31a4a5f43e337342f0056f5f6d58a6360773e6d4a10ac9cb9bf9c1d330b878db18c8a7a3e6564468d8225efce64a0346931643db7c228e54f98b9e488959de26691a9bbed503957b2b2e6a855b4787c3097636e4a563e47bbcff200687701b4ab9ad494340f80075c00136c0151d15d01b0b602404d5c45671d1c59e2c8d1cdbc0747b78ba3dbc5d14d1febe2305c1c978943623c142586ae8aedfdf1c45544ce62859a60e53040e7a057b0b6cc83d5efe9f022a35c22ee32ab6d9a55f4302faf47d8e7a73d8d30e4621f72b10f31cd5b5487df89946a04a9b746e9e21e165ba896533ef914a42c4e3de1907584f545740381685b40bc4bc8108bb705e85d375daaf2acdd0d8fb2d01ea3d5c7b1923abad85955268ab83823187770469846bb61234ec3865821f72826f2c907af83e78c83a7d38327d1b46e06eb2b687a66a1b1cbfe296a7b1a2bf00ccbfe590f924e1749a78ba4d345d2e922697191582e927f7045740cd8b1f0f2b74dd66dce453610508b6dce1d8f21664699b404882462f01053e818a4fe9e43e23989f13e52ff026be14b40602c0355ab7e89fc6e3824e1143148be009d1a0badac2fe46bf8a2715d6adb0b836cea9d32b1752ff393cc6e9805d734a306be26046da4ef7ad069c78580ac36f55e37210271a5a10efbb07bf43dc9eafe9367d43cdf32da6f0dd1e35a643d89d000000007a000000696f2f6e756c732f636f6e74726163742f746f6b656e2f504b01021400140008080800aa7b564eec308779cb090000281800002800000000000000000000000000af000000696f2f6e756c732f636f6e74726163742f746f6b656e2f53696d706c65546f6b656e2e636c617373504b01021400140008080800aa7b564e68fe421cca0100005e0400002200000000000000000000000000d00a0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e2e636c617373504b01021400140008080800aa7b564eea7bbc798f040000e60900003000000000000000000000000000ea0c0000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e24417070726f76616c4576656e742e636c617373504b01021400140008080800aa7b564e826261e37e040000ca0900003000000000000000000000000000d7110000696f2f6e756c732f636f6e74726163742f746f6b656e2f546f6b656e245472616e736665724576656e742e636c617373504b0506000000000800080051020000b31600000000096f66665f6e72633230154f0000000000001900000000000000040103616972010341495201053130303030010132480117020001f7ec6473df12e751d64cf20a8baa7edd50810f81020001006d67120000000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000000",
    "contractAddress" : "tNULSeBaMxHqD1Vdcmyx4P43GMCPGcRiFzqjie",
    "hash" : "2ca7d4e9f2ffcb92e1d134ea4a544ffb947f4c9154ff73223834d87bba9734ba"
  }
}
```

### 4.20 离线 - 调用合约
#### Cmd: contractCallOffline
_**详细描述: 离线 - 调用合约**_

#### 参数列表
| 参数名              |    参数类型    | 参数描述                                                                                | 是否必填 |
| ---------------- |:----------:| ----------------------------------------------------------------------------------- |:----:|
| chainId          |    int     | 链id                                                                                 |  是   |
| sender           |   string   | 交易创建者账户地址                                                                           |  是   |
| senderBalance    | biginteger | 账户余额                                                                                |  是   |
| nonce            |   string   | 账户nonce值                                                                            |  是   |
| value            | biginteger | 调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO                                            |  是   |
| contractAddress  |   string   | 合约地址                                                                                |  是   |
| gasLimit         |    long    | 设置合约执行消耗的gas上限                                                                      |  是   |
| methodName       |   string   | 合约方法                                                                                |  是   |
| methodDesc       |   string   | 合约方法描述，若合约内方法没有重载，则此参数可以为空                                                          |  否   |
| args             |  object[]  | 参数列表                                                                                |  否   |
| argsType         |  string[]  | 参数类型列表                                                                              |  否   |
| remark           |   string   | 交易备注                                                                                |  否   |
| multyAssetValues | string[][] | 调用者向合约地址转入的其他资产金额，没有此业务时填空，规则: [[\<value\>,\<assetChainId\>,\<assetId\>,\<nonce\>]] |  否   |

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "contractCallOffline",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "999999998523475", "9c0aea02bed90ddd", 0, "tNULSeBaMwYiR4p1X9xNJPiyJfrXjr4KgkcFjG", 14166, "transfer", null, [ "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", 3800 ], [ "Address", "BigInteger" ], "remark_call_test", null ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "1000f22d375d1072656d61726b5f63616c6c5f7465737497020001f7ec6473df12e751d64cf20a8baa7edd50810f810200020d2f73cb93099a8cfd0cbdd060155abfe2f50917000000000000000000000000000000000000000000000000000000000000000056370000000000001900000000000000087472616e7366657200020126744e554c536542614d6e7273364a4b724379365451647a594a5a6b4d5a4a446e673751417344010433383030480117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010006ee060000000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000000",
    "hash" : "b5d6e09a8ecee2c1e4fd1d9c6a9704fb84a9fbe1bd069762d684c7b29b1e4668"
  }
}
```

### 4.21 离线 - 删除合约
#### Cmd: contractDeleteOffline
_**详细描述: 离线 - 删除合约**_

#### 参数列表
| 参数名             |    参数类型    | 参数描述      | 是否必填 |
| --------------- |:----------:| --------- |:----:|
| chainId         |    int     | 链id       |  是   |
| sender          |   string   | 交易创建者账户地址 |  是   |
| senderBalance   | biginteger | 账户余额      |  是   |
| nonce           |   string   | 账户nonce值  |  是   |
| contractAddress |   string   | 合约地址      |  是   |
| remark          |   string   | 交易备注      |  否   |

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "contractDeleteOffline",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "999999998523475", "9c0aea02bed90ddd", "tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu", "delete contract" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "1100462e375d0f64656c65746520636f6e74726163742e020001f7ec6473df12e751d64cf20a8baa7edd50810f81020002245bcd36879bc30bfc719a417939b3aa924247ca480117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a086010000000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000000",
    "hash" : "79c7385eb8a538ed770cca5eea015e05cb602b1ccb2c9694ea6dbba32b781df4"
  }
}
```

### 4.22 离线 - 合约token转账
#### Cmd: tokentransferOffline
_**详细描述: 离线 - 合约token转账**_

#### 参数列表
| 参数名             |    参数类型    | 参数描述           | 是否必填 |
| --------------- |:----------:| -------------- |:----:|
| chainId         |    int     | 链id            |  是   |
| fromAddress     |   string   | 转出者账户地址        |  是   |
| senderBalance   | biginteger | 转出者账户余额        |  是   |
| nonce           |   string   | 转出者账户nonce值    |  是   |
| toAddress       |   string   | 转入者账户地址        |  是   |
| contractAddress |   string   | token合约地址      |  是   |
| gasLimit        |    long    | 设置合约执行消耗的gas上限 |  是   |
| amount          | biginteger | 转出的token资产金额   |  是   |
| remark          |   string   | 交易备注           |  否   |

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "tokentransferOffline",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "999999998523475", "9c0aea02bed90ddd", "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD", "tNULSeBaN3MH7HX8kXzKw4X9tLKQ991X1GiAbK", 14166, 10, "1个" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "1000b32e375d0431e4b8aa95020001f7ec6473df12e751d64cf20a8baa7edd50810f810200026b8d9b09ed5c1a692a6109c5ee99ccb6177b13a1000000000000000000000000000000000000000000000000000000000000000056370000000000001900000000000000087472616e7366657200020126744e554c536542614d6e7273364a4b724379365451647a594a5a6b4d5a4a446e67375141734401023130480117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010006ee060000000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000000",
    "hash" : "9f09e5719624727c4bff3bc6f690cc0866238c0205551c7d57a93f49afbb9596"
  }
}
```

### 4.23 离线 - 从账户地址向合约地址转账(主链资产)的合约交易
#### Cmd: transfer2contractOffline
_**详细描述: 离线 - 从账户地址向合约地址转账(主链资产)的合约交易**_

#### 参数列表
| 参数名           |    参数类型    | 参数描述           | 是否必填 |
| ------------- |:----------:| -------------- |:----:|
| chainId       |    int     | 链id            |  是   |
| fromAddress   |   string   | 转出者账户地址        |  是   |
| senderBalance | biginteger | 转出者账户余额        |  是   |
| nonce         |   string   | 转出者账户nonce值    |  是   |
| toAddress     |   string   | 转入的合约地址        |  是   |
| gasLimit      |    long    | 设置合约执行消耗的gas上限 |  是   |
| amount        | biginteger | 转出的主链资产金额      |  是   |
| remark        |   string   | 交易备注           |  否   |

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "transfer2contractOffline",
  "params" : [ 2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "999999998523475", "9c0aea02bed90ddd", "tNULSeBaMxyMyafiQjq1wCW7cQouyEhRL8njtu", 25896, "400000000", "离线向合约转账" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "1000fe2e375d15e7a6bbe7babfe59091e59088e7baa6e8bdace8b4a677020001f7ec6473df12e751d64cf20a8baa7edd50810f81020002245bcd36879bc30bfc719a417939b3aa924247ca0084d7170000000000000000000000000000000000000000000000000000000028650000000000001900000000000000085f70617961626c650e28292072657475726e20766f6964008c0117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010088ebe21700000000000000000000000000000000000000000000000000000000089c0aea02bed90ddd000117020002245bcd36879bc30bfc719a417939b3aa924247ca020001000084d71700000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "ea3d34129992757e7e7b032c8b5b0ad2b9bc6823592bd83814c3e92d0221417d"
  }
}
```

### 5.1 创建共识节点
#### Cmd: createAgent
_**详细描述: 创建共识节点**_

#### 参数列表
| 参数名                                                       | 参数类型 | 参数描述               | 是否必填 |
| ------------------------------------------------------------ | :------: | ---------------------- | :------: |
| chainId                                                      |   int    | 链ID                   |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress |  string  | 节点地址               |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress |  string  | 节点出块地址           |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rewardAddress |  string  | 奖励地址，默认节点地址 |    否    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;commissionRate |   int    | 佣金比例               |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit      |  string  | 抵押金额               |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password     |  string  | 密码                   |    是    |

#### 返回值
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createAgent",
  "params" : [ 2, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "tNULSeBaMhbVDg6CpiWx2jzExLFarBr6vJ6aSF", "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", 10, "2000000000000", "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "157ad5e8061328c764090b85b60624d461d1815357c22f2910506a3cdcbbb6d5"
}
```

### 5.2 注销共识节点
#### Cmd: stopAgent
_**详细描述: 注销共识节点**_

#### 参数列表
| 参数名                                                   | 参数类型 | 参数描述     | 是否必填 |
| -------------------------------------------------------- | :------: | ------------ | :------: |
| chainId                                                  |   int    | 链ID         |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address  |  string  | 共识节点地址 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |  string  | 密码         |    是    |

#### 返回值
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "stopAgent",
  "params" : [ 2, "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG", "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "fcaf8c92a0eafd2ca57744c165e1a955edcbfde98248494937200cc30d524e2e"
}
```

### 5.3 委托参与共识
#### Cmd: depositToAgent
_**详细描述: 委托参与共识**_

#### 参数列表
| 参数名                                                    | 参数类型 | 参数描述         | 是否必填 |
| --------------------------------------------------------- | :------: | ---------------- | :------: |
| chainId                                                   |   int    | 链ID             |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address   |  string  | 参与共识账户地址 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash |  string  | 共识节点hash     |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit   |  string  | 参与共识的金额   |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password  |  string  | 密码             |    是    |

#### 返回值
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "depositToAgent",
  "params" : [ 2, "tNULSeBaMhcccH1KeXhMpH5y3pvtRzatAiuMJk", "157ad5e8061328c764090b85b60624d461d1815357c22f2910506a3cdcbbb6d5", "200000000000", "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "4a1177e2a738f72ba5063a8667a81e10bd7523f91ea08b2aa3fb851ca8dc8b07"
}
```

### 5.4 退出共识
#### Cmd: withdraw
_**详细描述: 退出共识**_

#### 参数列表
| 参数名                                                   | 参数类型 | 参数描述             | 是否必填 |
| -------------------------------------------------------- | :------: | -------------------- | :------: |
| chainId                                                  |   int    | 链ID                 |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address  |  string  | 节点地址             |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash   |  string  | 加入共识时的交易hash |    是    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password |  string  | 密码                 |    是    |

#### 返回值
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "withdraw",
  "params" : [ 2, "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG", "4ae333f8bf821884d0f589f35516c8bdd9661dbd8a7009b063ac862eeefc10f6", "abcd1234" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : "13a0e252bf05ec02f3ae0a84fc3b8183dbfc0e16c562b20b8e28b73b139f2c0e"
}
```

### 5.5 查询节点的委托共识列表
#### Cmd: getDepositList
_**详细描述: 查询节点的委托共识列表**_

#### 参数列表
| 参数名       |  参数类型  | 参数描述          | 是否必填 |
| --------- |:------:| ------------- |:----:|
| chainId   |  int   | 链ID           |  是   |
| agentHash | string | 创建共识节点的交易hash |  是   |

#### 返回值
| 字段名         |  字段类型  | 参数描述      |
| ----------- |:------:| --------- |
| deposit     | string | 委托金额      |
| agentHash   | string | 节点hash    |
| address     | string | 账户地址      |
| time        |  long  | 委托时间      |
| txHash      | string | 委托交易hash  |
| blockHeight |  long  | 委托时的区块高度  |
| delHeight   |  long  | 退出委托的区块高度 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getDepositList",
  "params" : [ 2, "786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : [ {
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

### 5.6 根据最大高度和原始种子个数生成一个随机种子并返回
#### Cmd: getRandomSeedByCount
_**详细描述: 包括最大高度往后退1000个区块，在这个区块区间内找到指定个数的原始种子，汇总生成一个随机种子并返回**_

#### 参数列表
| 参数名       |  参数类型  | 参数描述                      | 是否必填 |
| --------- |:------:| ------------------------- |:----:|
| chainId   |  int   | 链ID                       |  是   |
| height    |  long  | 最大高度                      |  是   |
| count     |  int   | 原始种子个数                    |  是   |
| algorithm | string | 算法标识：SHA3, KECCAK, MERKLE |  否   |

#### 返回值
| 字段名       |  字段类型  | 参数描述    |
| --------- |:------:| ------- |
| seed      | string | 生成的随机种子 |
| algorithm | string | 算法标识    |
| count     |  int   | 原始种子个数  |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getRandomSeedByCount",
  "params" : [ 2, 15, 9, "sha3" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "seed" : "39348806759173754289552718450552160894738020452243263500745175936916037359443",
    "algorithm" : "SHA3",
    "count" : 9
  }
}
```

### 5.7 根据高度区间生成一个随机种子并返回
#### Cmd: getRandomSeedByHeight
_**详细描述: 在这个区块区间内找到所有有效的原始种子，汇总生成一个随机种子并返回**_

#### 参数列表
| 参数名         |  参数类型  | 参数描述                      | 是否必填 |
| ----------- |:------:| ------------------------- |:----:|
| chainId     |  int   | 链ID                       |  是   |
| startHeight |  long  | 起始高度                      |  是   |
| endHeight   |  long  | 截止高度                      |  是   |
| algorithm   | string | 算法标识：SHA3, KECCAK, MERKLE |  否   |

#### 返回值
| 字段名       |  字段类型  | 参数描述    |
| --------- |:------:| ------- |
| seed      | string | 生成的随机种子 |
| algorithm | string | 算法标识    |
| count     |  int   | 原始种子个数  |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getRandomSeedByHeight",
  "params" : [ 2, 7, 15, "sha3" ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "seed" : "32532675763615856265810357233291461242017048552507569663816339711779497299975",
    "algorithm" : "sha3",
    "count" : 9
  }
}
```

### 5.8 根据最大高度和原始种子个数查找原始种子列表并返回
#### Cmd: getRandomRawSeedsByCount
_**详细描述: 包括最大高度往后退1000个区块，在这个区块区间内找到指定个数的原始种子并返回**_

#### 参数列表
| 参数名     | 参数类型 | 参数描述   | 是否必填 |
| ------- |:----:| ------ |:----:|
| chainId | int  | 链ID    |  是   |
| height  | long | 最大高度   |  是   |
| count   | int  | 原始种子个数 |  是   |

#### 返回值
| 字段名    |      字段类型       | 参数描述 |
| ------ |:---------------:| ---- |
| 原始种子列表 | list&lt;string> |      |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getRandomRawSeedsByCount",
  "params" : [ 2, 15, 9 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : [ "-29372401885335809343334881114861862848664228571153431589582614750707853803688", "-12374588462997313588598897726376463898687300536133622323300129617802447843663", "35414850206903943716465298994826456060042987072617525631637631635987014797108", "-57234933950614201812269794723322473738769164815685574368298879134983145932442", "-36943716549467048219120901994813656501647327407366907446161430460954253977405", "30934978871350238591664023000030597630129456116167320700551408944317816121383", "-40719234813631611496719465228844846754749045533296280679027880790124492776813", "-9760170464524872943819135990753457668421091036911187432097064247132004006726", "8470565416062428412592833383521885451190767259837871270725993030997862574316" ]
}
```

### 5.9 根据高度区间查找原始种子列表并返回
#### Cmd: getRandomRawSeedsByHeight
_**详细描述: 在这个区块区间内找到所有有效的原始种子并返回**_

#### 参数列表
| 参数名         | 参数类型 | 参数描述 | 是否必填 |
| ----------- |:----:| ---- |:----:|
| chainId     | int  | 链ID  |  是   |
| startHeight | long | 起始高度 |  是   |
| endHeight   | long | 截止高度 |  是   |

#### 返回值
| 字段名    |      字段类型       | 参数描述 |
| ------ |:---------------:| ---- |
| 原始种子列表 | list&lt;string> |      |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "getRandomRawSeedsByHeight",
  "params" : [ 2, 7, 15 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : [ "8470565416062428412592833383521885451190767259837871270725993030997862574316", "-9760170464524872943819135990753457668421091036911187432097064247132004006726", "-40719234813631611496719465228844846754749045533296280679027880790124492776813", "30934978871350238591664023000030597630129456116167320700551408944317816121383", "-36943716549467048219120901994813656501647327407366907446161430460954253977405", "-57234933950614201812269794723322473738769164815685574368298879134983145932442", "35414850206903943716465298994826456060042987072617525631637631635987014797108", "-12374588462997313588598897726376463898687300536133622323300129617802447843663", "-29372401885335809343334881114861862848664228571153431589582614750707853803688" ]
}
```

### 5.10 离线组装 - 创建共识节点
#### Cmd: createAgentOffline
_**详细描述: 参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)**_

#### 参数列表
| 参数名                                                                                                          |     参数类型     | 参数描述       | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:------------:| ---------- |:----:|
| chainId                                                                                                      |     int      | 链ID        |  是   |
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

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "createAgentOffline",
  "params" : [ 2, "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG", "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG", 10, "2000000000000", {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "2000001000000",
    "nonce" : "63ac862eeefc10f6"
  } ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0400e6b72d5d006600204aa9d1010000000000000000000000000000000000000000000000000000020001efa328e600912da9872390a675486ab9e8ec2114020001f7ec6473df12e751d64cf20a8baa7edd50810f81020001efa328e600912da9872390a675486ab9e8ec21140a8c0117020001efa328e600912da9872390a675486ab9e8ec211402000100406259a9d10100000000000000000000000000000000000000000000000000000863ac862eeefc10f6000117020001efa328e600912da9872390a675486ab9e8ec21140200010000204aa9d1010000000000000000000000000000000000000000000000000000ffffffffffffffff00",
    "hash" : "7a5c405239c742d0253a4067dd7df94b0bd4103b0edc4d3226575b5176a07ad0"
  }
}
```

```
createAgent
```

### 5.11 离线组装 - 注销共识节点
#### Cmd: stopAgentOffline
_**详细描述: 组装交易的StopDepositDto信息，可通过查询节点的委托共识列表获取，input的nonce值可为空**_

#### 参数列表
| 参数名                                                                                                                                                          |       参数类型       | 参数描述        | 是否必填 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------ |:----------------:| ----------- |:----:|
| chainId                                                                                                                                                      |       int        | 链ID         |  是   |
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

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "stopAgentOffline",
  "params" : [ 2, "786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd", "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG", "2000000000000", "100000", [ {
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
  } ] ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "090075bd2d5d0020786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcdfd5c010317020001efa328e600912da9872390a675486ab9e8ec21140200010000204aa9d1010000000000000000000000000000000000000000000000000000087a1f080d3dd30dcdff17020001efa328e600912da9872390a675486ab9e8ec21140200010000d0ed902e00000000000000000000000000000000000000000000000000000008629b5a4c7f36c452ff17020001efa328e600912da9872390a675486ab9e8ec21140200010000d0ed902e000000000000000000000000000000000000000000000000000000081ca9812c66d27e84ff0217020001efa328e600912da9872390a675486ab9e8ec211402000100609948a9d101000000000000000000000000000000000000000000000000000085cb2d5d0000000017020001efa328e600912da9872390a675486ab9e8ec21140200010000a0db215d000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "15e9f16c7b430ea217408ae63dd6e90739bc0a5f0f6b3c0907b4689d02dc744a"
  }
}
```

### 5.12 离线组装 - 委托参与共识
#### Cmd: depositToAgentOffline
_**详细描述: 参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)**_

#### 参数列表
| 参数名                                                                                                          |    参数类型    | 参数描述       | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:----------:| ---------- |:----:|
| chainId                                                                                                      |    int     | 链ID        |  是   |
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

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "depositToAgentOffline",
  "params" : [ 2, "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG", "200000000000", "786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd", {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : "200010000000",
    "nonce" : "7a1f080d3dd30dcd"
  } ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0500c6b82d5d005700d0ed902e000000000000000000000000000000000000000000000000000000020001efa328e600912da9872390a675486ab9e8ec2114786402b17649b968e4643cb52fa30225645b0dc7b8761b047a1f080d3dd30dcd8c0117020001efa328e600912da9872390a675486ab9e8ec211402000100806686912e000000000000000000000000000000000000000000000000000000087a1f080d3dd30dcd000117020001efa328e600912da9872390a675486ab9e8ec21140200010000d0ed902e000000000000000000000000000000000000000000000000000000ffffffffffffffff00",
    "hash" : "bd93cf73331c0d9986cb90922d2eec785ea9eda3da85cd9d629b5a4c7f36c452"
  }
}
```

### 5.13 离线组装 - 退出共识
#### Cmd: withdrawOffline
_**详细描述: 离线组装 - 退出共识**_

#### 参数列表
| 参数名                                                                                                          |    参数类型     | 参数描述        | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:-----------:| ----------- |:----:|
| chainId                                                                                                      |     int     | 链ID         |  是   |
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

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "withdrawOffline",
  "params" : [ 2, "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG", "be5257bc0814cbda61378ff2afa81e98cae0018cd7d78b8d1ca9812c66d27e84", "1000000", {
    "address" : "tNULSeBaMujLBcZWfE2wHKnZo7PGvqvNrt6yWG",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : 200000000000,
    "nonce" : ""
  } ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "060031bb2d5d0020be5257bc0814cbda61378ff2afa81e98cae0018cd7d78b8d1ca9812c66d27e848c0117020001efa328e600912da9872390a675486ab9e8ec21140200010000d0ed902e000000000000000000000000000000000000000000000000000000081ca9812c66d27e84ff0117020001efa328e600912da9872390a675486ab9e8ec211402000100c08dde902e000000000000000000000000000000000000000000000000000000000000000000000000",
    "hash" : "bad82cb423722793a77d729444fee0c1a99a679c8ab0a2cb5ccc10be584c7726"
  }
}
```

### 5.14 离线组装 - 多签账户创建共识节点
#### Cmd: multiSignCreateAgentOffline
_**详细描述: 参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)**_

#### 参数列表
| 参数名                                                                                                          |         参数类型          | 参数描述           | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:---------------------:| -------------- |:----:|
| chainId                                                                                                      |          int          | 链ID            |  是   |
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

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "multiSignCreateAgentOffline",
  "params" : [ 2, "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy", "tNULSeBaMowgMLTbRUngAuj2BvGy2RmVLt3okv", "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy", 10, 2000000000000, {
    "address" : "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : 2000001000000,
    "nonce" : "0000000000000000"
  }, [ "0377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db", "03f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1" ], 2 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "04001d5a775d006600204aa9d1010000000000000000000000000000000000000000000000000000020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe202000191866cefc8c9e1181b4e1e068b64fa288405b3e6020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe20a8c0117020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe202000100406259a9d1010000000000000000000000000000000000000000000000000000080000000000000000000117020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe20200010000204aa9d1010000000000000000000000000000000000000000000000000000ffffffffffffffff460202210377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db2103f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1",
    "hash" : "4b0aa8c126bf314dbc7d42dc94127064392643b33e194cfa050884a38557392c"
  }
}
```

### 5.15 离线组装 - 多签账户注销共识节点
#### Cmd: multiSignStopAgentOffline
_**详细描述: 组装交易的StopDepositDto信息，可通过查询节点的委托共识列表获取，input的nonce值可为空**_

#### 参数列表
| 参数名                                                                                                                                                          |           参数类型            | 参数描述           | 是否必填 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------ |:-------------------------:| -------------- |:----:|
| chainId                                                                                                                                                      |            int            | 链ID            |  是   |
| MultiSignStopConsensusDto                                                                                                                                    | multisignstopconsensusdto | 多签账户离线注销共识节点表单 |  是   |
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

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "multiSignStopAgentOffline",
  "params" : [ 2, "e67ed0f09cea8bd4e2ad3b4b6d83a39841f9f83dd2a9e5737b73b4d5ad203537", "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy", 2000000000000, 1000000, [ {
    "depositHash" : "d4a9404a823ea533d1c7fba34470970ac499a974f35172bb8a717b0d6c4d4cbe",
    "input" : {
      "address" : "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy",
      "assetChainId" : 2,
      "assetId" : 1,
      "amount" : 200000000000
    }
  } ], [ "0377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db", "03f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1" ], 2 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0900a55b775d0020e67ed0f09cea8bd4e2ad3b4b6d83a39841f9f83dd2a9e5737b73b4d5ad203537fd16010217020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe20200010000204aa9d1010000000000000000000000000000000000000000000000000000087b73b4d5ad203537ff17020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe20200010000d0ed902e000000000000000000000000000000000000000000000000000000088a717b0d6c4d4cbeff0217020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe202000100c0dd3aa9d1010000000000000000000000000000000000000000000000000000b569775d0000000017020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe20200010000d0ed902e0000000000000000000000000000000000000000000000000000000000000000000000460202210377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db2103f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1",
    "hash" : "fb7f4d0e078b0eba8e89e22faccab54af18eab73858fb0d1c8bddbf6c771f1e3"
  }
}
```

### 5.16 离线组装 - 多签账户委托参与共识
#### Cmd: multiSignDepositToAgentOffline
_**详细描述: 参与共识所需资产可通过查询链信息接口获取(agentChainId和agentAssetId)**_

#### 参数列表
| 参数名                                                                                                          |        参数类型         | 参数描述           | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:-------------------:| -------------- |:----:|
| chainId                                                                                                      |         int         | 链ID            |  是   |
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

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "multiSignDepositToAgentOffline",
  "params" : [ 2, "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy", 200000000000, "e67ed0f09cea8bd4e2ad3b4b6d83a39841f9f83dd2a9e5737b73b4d5ad203537", {
    "address" : "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : 2000001000000,
    "nonce" : "0000000000000000"
  }, [ "0377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db", "03f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1" ], 2 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0500595c775d005700d0ed902e000000000000000000000000000000000000000000000000000000020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe2e67ed0f09cea8bd4e2ad3b4b6d83a39841f9f83dd2a9e5737b73b4d5ad2035378c0117020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe202000100406259a9d1010000000000000000000000000000000000000000000000000000080000000000000000000117020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe20200010000d0ed902e000000000000000000000000000000000000000000000000000000ffffffffffffffff460202210377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db2103f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1",
    "hash" : "afa774c23dc75dd61356135407959385728c34f358017842d9da090c1d9d08ec"
  }
}
```

### 5.17 离线组装 - 多签账户退出共识
#### Cmd: multiSignWithdrawOffline
_**详细描述: 离线组装 - 多签账户退出共识**_

#### 参数列表
| 参数名                                                                                                          |         参数类型         | 参数描述         | 是否必填 |
| ------------------------------------------------------------------------------------------------------------ |:--------------------:| ------------ |:----:|
| chainId                                                                                                      |         int          | 链ID          |  是   |
| MultiSignWithDrawDto                                                                                         | multisignwithdrawdto | 多签账户离线退出共识表单 |  是   |
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

#### 返回值
| 字段名   |  字段类型  | 参数描述     |
| ----- |:------:| -------- |
| hash  | string | 交易hash   |
| txHex | string | 交易序列化字符串 |
#### Example request data: 

```json
{
  "jsonrpc" : "2.0",
  "method" : "multiSignWithdrawOffline",
  "params" : [ 2, "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy", "e67ed0f09cea8bd4e2ad3b4b6d83a39841f9f83dd2a9e5737b73b4d5ad203537", 1000000, {
    "address" : "tNULSeBaNTcZo37gNC5mNjJuB39u8zT3TAy8jy",
    "assetChainId" : 2,
    "assetId" : 1,
    "amount" : 200000000000
  }, [ "0377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db", "03f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1" ], 2 ],
  "id" : 1234
}
```

#### Example response data: 

```json
{
  "jsonrpc" : "2.0",
  "id" : "1234",
  "result" : {
    "txHex" : "0600255d775d0020e67ed0f09cea8bd4e2ad3b4b6d83a39841f9f83dd2a9e5737b73b4d5ad2035378c0117020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe20200010000d0ed902e000000000000000000000000000000000000000000000000000000087b73b4d5ad203537ff0117020003f6231825aa05e4d25b4772909a15c9ba3c0b6fe202000100c08dde902e0000000000000000000000000000000000000000000000000000000000000000000000460202210377a7e02381a11a1efe3995d1bced0b3e227cb058d7b09f615042123640f5b8db2103f66892ff89daf758a5585aed62a3f43b0a12cbec8955c3b155474071e156a8a1",
    "hash" : "ac7d378ffcc7d7688e16f5916d177ffae992f62cdd0718594308bde445f31b9c"
  }
}
```

