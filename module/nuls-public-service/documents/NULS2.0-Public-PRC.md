# NULS2.0——API接口文档

## 简介

每个 NULS2.0节点都可选的提供了一套 API 接口，用于从节点获取可视化区块链数据，使得开发区块链应用变得十分方便。接口通过 [JSON-RPC](http://wiki.geekdream.com/Specification/json-rpc_2.0.html) 的方式提供，底层使用 HTTP协议进行通讯。

要启动一个提供 RPC 服务的节点，需做到以下几个步骤：

- 获取钱包

方式一：下载能提供RPC服务的全节点钱包（http://正式上线后填充 下载地址链接）

方式二：同步https://github.com/nuls-io/nuls-v2上NULS2.0项目master分支的源代码，执行如下命令，手动打包全节点钱包：

```
./package -a public-service
./package
```

- 节点服务器需安装mongoDB数据库
- 修改module.ncf文件，[public-service]的相关配置，如下：

```
[public-service]
#数据库url地址
databaseUrl=127.0.0.1
#数据库端口号
databasePort=27017
```

完成配置后，启动节点程序，客户端会解析已同步的区块，并存储到mongoDB中。

## 监听端口

默认端口为18003，可修改module.ncf文件，[public-service]的相关配置，如下：

```
[public-service]
#public-service模块对外的rpc端口号
rpcPort=18003
```

## 接口说明

### 字符集编码

UTF-8

### 远程调用协议

JSON-RPC

```
{
	"jsonrpc":"2.0",
	"method":"getChainInfo",		//接口名称
	"params":[],					//所有接口的参数，都已数组方式传递，且参数顺序不能变
	"id":1234
}
```

### 接口返回格式

```
正常返回
//example
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
         "networkHeight": 4936,
         "localHeight": 4936
     }
}
异常返回
//example
{
     "jsonrpc": "2.0",
     "id": 1234,
     "error": {
          "code": 1000,
          "message": "Parameters is wrong!",
          "data": "Incorrect number of parameters"
     }
}
```

### 代币转换

涉及到有代币的接口，参数和返回里的代币数量为了避免小数精度丢失，统一转为BigInteger格式。NULS主网的小数精度为8位，因此接口层统一右移8位，即100,000,000 = 1NULS。

### 返回数据定义

#### 交易类型(txType)

```
    int COIN_BASE = 1;						//coinBase奖励
    int TRANSFER = 2;						//转账
 	int ACCOUNT_ALIAS = 3;					//设置账户别名
   	int REGISTER_AGENT = 4;					//新建共识节点
   	int DEPOSIT = 5;						//委托参与共识
    int CANCEL_DEPOSIT = 6;					//取消委托
    int YELLOW_PUNISH = 7;					//黄牌惩罚
    int RED_PUNISH = 8;						//红牌惩罚
    int STOP_AGENT = 9;						//注销共识节点
    int CROSS_CHAIN = 10;					//跨链转账
	int REGISTER_CHAIN_AND_ASSET = 11;		//注册链
    int DESTROY_CHAIN_AND_ASSET = 12;		//销毁链
    int ADD_ASSET_TO_CHAIN = 13;			//新增资产上链
   	int REMOVE_ASSET_FROM_CHAIN = 14;		//注销资产
    int CREATE_CONTRACT = 15;				//创建智能合约
    int CALL_CONTRACT = 16;					//调用智能合约
    int DELETE_CONTRACT = 17;				//删除智能合约
    int CONTRACT_TRANSFER = 18;				//合约内部转账
    int CONTRACT_RETURN_GAS = 19;			//合约执行手续费返还
    int CONTRACT_CREATE_AGENT = 20;			//合约创建共识节点
	int CONTRACT_DEPOSIT = 21;				//合约委托参与共识
 	int CONTRACT_CANCEL_DEPOSIT = 22;		//合约取消委托
 	int CONTRACT_STOP_AGENT = 23;			//合约注销共识节点
```



#### 资产信息(assetInfo)

```
assetInfo：{
    "key": "100-1",						//string	主键
    "chainId": 100,						//int		资产的链id 		
    "assetId": 1,						//int		资产id
    "symbol": "NULS",					//string	资产符号
    "decimals":8						//int		资产支持小数位
    "initCoins": 100000000000000,		//bigInt	资产初始金额
    "address": "tNULSeBaMoodYW7A……",	//string	资产创建人地址			
    "status": 1							//int		状态， 0：注销	1：启用
}


```

#### 区块头信息(blockHeaderInfo)

```
blockHeaderInfo: {
    "hash": "c31d198b6fb5a……",					//string	区块hash
    "height": 304,								//long		区块高度
    "preHash": "d7596990d508……",				//string	前一区块hash
    "merkleHash": "85c661b36aa3fdc……",			//string	梅克尔hash
    "createTime": 1559725301,					//long		创建时间
    "agentHash": null,							//string	出块节点的hash
    "agentId": "8CPcA7kaXSHbWb3GHP7……",			//string	出块节点的id
    "packingAddress": "8CPcA7kaXSH……",			//string	出块节点的区块打包地址
    "agentAlias": null,							//string	出块节点的代理人别名
    "txCount": 1,								//int		区块打包交易数量
    "roundIndex": 155972530,					//long		出块轮次
    "totalFee": 0,								//bigInt	打包的交易手续费
    "reward": 0,								//bigInt	出块奖励
    "size": 235,								//long		区块大小
    "packingIndexOfRound": 1,					//int		本轮的出块顺序
    "scriptSign": "210e2ab7a219bca2a……",		//string	区块签名
    "txHashList": [								//[string]	区块打包的交易对应的交易hash集合
        "85c661b36aa3fdc93b9bc27bb8fdf1……"
    ],
    "roundStartTime": 1559725291,				//long		本轮出块的起始出块时间
    "agentVersion": 1,							//int		出块节点的协议版本号
    "seedPacked": true							//boolean	当前区块是否是种子节点打包
}
```

#### 交易信息(txInfo)

```
txInfo: {
    "hash": "0020b15e564……",				//string	交易hash
    "type": 2,								//int 		交易类型(txType)
    "height": -1,							//long		确认交易的区块高度,-1表示还未被确认
    "size": 228,							//int		交易大小
    "createTime": 1552300674920,			//long		创建时间
    "remark": "transfer test",				//string	备注
    "txData": null,							//object	交易业务对象，根据交易类型区分，
    													具体见后面的数据定义
    "txDataHex": null,						//string	业务对象16进制序列化字符串
    "txDataList": null,						//[object]	交易业务对象集合，根据交易类型区分
    "fee": { 								//bigInt	手续费
        "chainId": 100,						//手续费链id
        "assetId": 1,						//手续费资产id
        "symbol": "ATOM",					//手续费资产符号
        "value": 100000						//手续费金额
    },
    "coinFroms": [
    {
        "address": "5MR_2CbSSboa……",			//string	转出地址
        "chainId": 12345,						//int		转出资产的链id
        "assetsId": 1,							//int		转出资产的id
        "amount": 1870000000000,				//bigInt	转出金额	
        "locked": 0,							//long		锁定时间
        "nonce": "ffffffff"						//string	转出资产最新nonce值
        "symbol":"nuls"							//string	资产符号
    }
    ],
    "coinTos": [
    {
        "address": "5MR_2CbSSboa……",			//string	接收地址
        "chainId": 12345,						//int		接收资产的链id
        "assetsId": 1,							//int		接收资产的id
        "amount": 1870000000000,				//bigInt	接收金额	
        "locked": 0,							//long		锁定时间
        "symbol":"nuls"							//string	资产符号
    }
    ],
    "value": 1860000000000						//bigInt	交易涉及到的资产变动额
}
```

#### 账户信息(accountInfo)

```
accountInfo: {
    "address": "5MR_2ChNj……",					//string	账户地址
    "alias": null,								//string	账户别名
    "type": 1,									//int		账户类型 
                                                //1：普通地址	2：合约地址	3：多签地址
    "txCount": 8,								//int		交易笔数
    "totalOut": 0,								//bigInt	总支出
    "totalIn": 1000000000000000,				//bigInt	总收入
    "consensusLock": 0,							//bigInt	本链默认资产共识锁定
    "timeLock": 0,								//bigInt	本链默认资产时间锁定
    "balance": 1000000000000000,				//bigInt	本链默认资产可用余额
    "totalBalance": 1000000000000000,			//bigInt	本链默认资产总额
    "totalReward": 0,							//bigInt	共识总奖励
    "tokens": []								//[string]	拥有的nrc20资产符号列表
}
```

#### 资产信息(accountLedgerInfo)

```
accountLedgerInfo: {
    "address": "tNULSeBaMrbMRiFAUeeAt……",			//string	账户地址
    "chainId": 2,									//int		资产的链id
    "assetId": 1,									//int		资产的id
    "symbol": "NULS",								//string	资产的符号
    "totalBalance": 1000000000000000,				//bigInt	资产总额
    "balance": 1000000000000000,					//bigInt	可用余额
    "timeLock": 0,									//bigInt	时间锁定
    "consensusLock": 0								//bigInt	共识锁定
}
```

#### 共识节点信息(consensusInfo)

```
 {
     "txHash": "0020c734c7ec……",				//string	创建共识节点的交易hash
     "agentId": "e4ae68a2",						//string	节点id
     "agentAddress": "5MR_2CfWGwnfh……",			//string	创建节点的代理账户地址
     "packingAddress": "5MR_2CeXYdnth……",		//string	节点负责打包区块的账户地址
     "rewardAddress": "5MR_2CeXYdnt……",			//string	节点获取共识奖励的账户地址
     "agentAlias": null,						//string	节点的代理地址别名
     "deposit": 2000000000000,					//bigInt	创建节点时代理节点的保证金
     "commissionRate": 10,						//int		节点收取的佣金比例，单位%
     "createTime": 1552300674920,				//long		节点的创建时间
     "status": 0,								//int		节点状态 
     											//0:待共识, 1:共识中, 2:已注销
     "totalDeposit": 20000000000000,			//bigInt	委托参与共识总金额
     "depositCount": 0,							//int		委托次数
     "creditValue": 0,							//double	信用值 取值[-1,1]
     "totalPackingCount": 3966,					//int		节点已打包出块总数
     "lostRate": 0,								//double	丢块率
     "lastRewardHeight": 8000,					//long		最后一次出块获取奖励的区块高度
     "deleteHash": null,						//string	注销节点的交易hash
     "blockHeight": 67,							//long		创建节点时的区块高度
     "deleteHeight": 0,							//long		注销节点时的区块高度
     "totalReward": 1256976254880,				//bigInt	总共识奖励				                                                       totalReward=commissionReward+agentReward
     "commissionReward": 1256976254880,			//bigInt	委托共识奖励
     "agentReward": 0,							//bigInt	节点获取奖励
     "roundPackingTime": 0,						//long		当前轮次节点负责打包区块的时间
     "version": 1,								//int		节点的协议版本号
     "type": 1,									//int		1:普通节点,2:开发者节点,3:大使节点
 }
```

#### 委托共识信息(depositInfo)

```
depositInfo:{
    "txHash": "0020dd1b606191068566c……",			//string	委托共识的交易hash
    "amount": 20000000000000,						//bigint	委托金额		
    "agentHash": "0020c734c7ecf447……",				//string	委托的共识节点的交易hash
    "address": "5MR_2CfWGwnfhPcdnho……",				//string	委托人的账户地址
    "createTime": 1552292357109,					//long		委托时间
    "blockHeight": 69,								//long		委托时的区块高度
    "deleteHeight": 0,								//long		取消委托时的区块高度
    "type": 0										//int		0:委托, 1:取消委托
    "fee": { 										//bigInt	委托交易的手续费
        "chainId": 100,								//手续费链id
        "assetId": 1,								//手续费资产id
        "symbol": "ATOM",							//手续费资产符号
        "value": 100000								//手续费金额
    },
}
```



## 接口列表

### 链相关接口[chain]

#### 查询本链信息

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getChainInfo",
    "params":[],
    "id":1234
}
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "chainId": 100,								//本链的id
          "chainName": "nuls",							//链名称
          "defaultAsset": {assetInfo},					//本链默认资产信息
          "assets": [									//本链所有资产信息集合
               {assetInfo}
          ],
          "seeds": [									//链的共识种子节点地址
               "8CPcA7kaXSHbWb3GHP7bd5hRLFu8RZv57rY9w"	
          ],
          "inflationCoins": 500000000000000,			//本链默认资产的通胀代币数量/年
          "status": 1									//状态：0 注销，1启用
     }
}
```

#### 查询链运行后的通用信息

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getInfo",
    "params":[chainId],
    "id":1234
}
//参数说明
chainId: int									//本链的id
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "networkHeight": 278,							//网络最新区块高度
          "localHeight": 278							//当前节点已同步的区块高度
          "defaultAsset": {								//本链默认资产信息
               "symbol": "NULS",						//资产符号
               "chainId": 2,							//资产链ID
               "assetId": 1,							//资产ID
               "decimals": 8							//支持小数位
          },
          "agentAsset": {								//本链参与共识所用资产信息
               "symbol": "NULS",
               "chainId": 2,
               "assetId": 1,
               "decimals": 8
          },
          "isRunCrossChain": true,						//是否支持跨链
          "isRunSmartContract": true					//是否启用智能合约
     }
}
```

#### 查询其他已注册跨链的链信息

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getOtherChainList",
    "params":[chainId],
    "id":1234
}
//参数说明
chainId: int									//本链的id
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": [
          {
               "chainName": "nuls2",					//链名称
               "chainId": 2								//链id
          }
     ]
}
```

### 区块相关接口[block]

#### 查询最新区块头

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getBestBlockHeader",
    "params":[chainId],
    "id":1234
}
//参数说明
chainId: int									//链的id
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {blockHeaderInfo}						//区块头信息
}
```

#### 根据高度查询区块头

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getHeaderByHeight",
    "params":[chainId, blockHeight],
    "id":1234
}
//参数说明
chainId: int									 //链的id
blockHeight：long								//区块高度
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {blockHeaderInfo}						//区块头信息
}
```

#### 根据区块hash查询区块头

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getHeaderByHash",
    "params":[chainId, blockHash],
    "id":1234
}
chainId: int									 //链的id
blockHash：string								//区块hash
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {blockHeaderInfo}						//区块头信息
}
```

#### 根据高度查询完整区块

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getBlockByHeight",
    "params":[chainId, blockHeight],
    "id":1234
}
//参数说明
chainId: int									 //链的id
blockHeight：long								//区块高度
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
     	"header":{blockHeaderInfo},						//区块头信息
     	"txList":[										//打包的交易信息
     		{txInfo}
     	]
     }						
}
```

#### 根据区块hash查询完整区块

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getBlockByHash",
    "params":[chainId, blockHash],
    "id":1234
}
//参数说明
chainId: int									 //链的id
blockHash：string								//区块hash
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
     	"header":{blockHeaderInfo},						//区块头信息
     	"txList":[										//打包的交易信息
     		{txInfo}
     	]
     }						
}
```

#### 查询区块头列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getBlockHeaderList",
    "params":[chainId,pageNumber,pageSize, isHidden, packedAddress],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
isHidden:boolean								//是否隐藏只有共识奖励交易的块 
packedAddress:string							//根据区块打包地址过滤，非必填
```

返回:

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 7,
          "list": [
               {blockHeaderInfo}
          ]
     }
}
```

### 账户相关的接口[account]

#### 查询账户详细信息

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccount",
    "params":[chainId,address],
    "id":1234
}
//参数说明
chainId: int									//链的id
address: string									//账户地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {accountInfo}					//账户信息
}
```

#### 根据别名查询账户详细信息

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountByAlias",
    "params":[chainId,alias],
    "id":1234
}
//参数说明
chainId: int									//链的id
alias: string									//账户别名
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {accountInfo}					//账户信息
}
```

#### 查询持币账户排名

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getCoinRanking",
    "params":[chainId,pageNumber,pageSize],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
               {
                    "address": "tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1",
                    "alias": null,
                    "type": 1,
                    "totalBalance": 1000000000000000,				//余额
                    "totalOut": 0,									//总支出
                    "totalIn": 1000000000000000						//总收入
               }
               ……
          ]
     }
}
```

#### 查询账户本链资产列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountLedgerList",
    "params":[chainId,address],
    "id":1234
}
//参数说明
chainId: int									//链的id
address: string									//账户地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": [
          {accountLedgerInfo}
     ]
}
```

#### 查询账户跨链资产列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountCrossLedgerList",
    "params":[chainId,address],
    "id":1234
}
//参数说明
chainId: int									//链的id
address: string									//账户地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": [
          {accountLedgerInfo}
     ]
}
```

#### 查询账户单个资产余额

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountBalance",
    "params":[chainId,assetChainId,assetId,address],
    "id":1234
}
//参数说明
chainId: int									//本链的id
assetChainId: int								//资产的链id
assetId: int									//资产的id
address: string									//账户地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "totalBalance": 1000000000000000,					//bigInt	资产总额
          "balance": 1000000000000000,						//bigInt	可用余额
          "timeLock": 0,									//bigInt	时间锁定金额
          "consensusLock": 0,								//bigInt	共识锁定金额
          "freeze": 0,										//bigInt	交易未确认金额
          "nonce": "0000000000000000",						//string	资产的nonce值
          "nonceType": 1									//int		nonce值是否已确认
          													// 0:未确认, 1:已确认
     }
}
```

#### 查询账户锁定金额列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountFreezes",
    "params":[chainId,pageNumber,pageSize,address],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
address: string									//账户地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "txHash":"d3ks2x9bAl38bfsl……" 		//交易hash
          "type":1								//锁定类型 
          										//1：时间锁定, 2:高度锁定, 3:共识锁定
          "time":1552300674920					//生成时间
          "lockedValue":155650000000			//锁定值
          "amount":100000000					//锁定金额
          "reason":"共识奖励"					 //锁定原因
     }
}
```

#### 查询别名是否可用

请求：

```
{
    "jsonrpc":"2.0",
    "method":"isAliasUsable",
    "params":[chainId,alias],
    "id":1234
}
//参数说明
chainId: int									//链的id
alias:string									//别名
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "value": true							//boolean	true: 可用, false: 不可用
     }
}
```

### 交易相关接口[transaction]

#### 查询交易详情

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getTx",
    "params":[chainId,txHash],
    "id":1234
}
//参数说明
chainId: int									//链的id
txHash: string									//交易hash	
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {txInfo}
}
```

#### 查询交易列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getLxList",
    "params":[chainId,pageNumber,pageSize,address,txType,startHeight,endHeight],                       
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
txType:int										//交易类型(txType),type=0时查询所有交易
isHidden:boolean    //是否隐藏共识奖励交易，默认是不隐藏，这个参数只能是type=0时有效
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
               {
                    "txHash": "a8611112f2b35385ee84f85……",		//交易hash
                    "address": "tNULSeBaMrbMRiFA……",			//账户地址
                    "type": 1,									//交易类型
                    "createTime": 1531152,						//交易时间，单位秒
                    "height": 0,								//交易被打包确定的区块高度
                    "chainId": 2,								//资产的链id
                    "assetId": 1,								//资产id
                    "symbol": "NULS",							//资产符号
                    "values": 1000000000000000,					//交易金额
                    "fee": { 									//bigInt	手续费
                        "chainId": 100,							//手续费链id
                        "assetId": 1,							//手续费资产id
                        "symbol": "ATOM",						//手续费资产符号
                        "value": 100000							//手续费金额
                    },
                    "balance": 1000000000000000,				//交易后账户的余额
                    "transferType": 1,							// -1:转出, 1:转入
                    "status": 1									//交易状态 0:未确认,1:已确认
               }
          ]
     }
}
```

#### 查询区块打包的交易

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getBlockTxList",
    "params":[chainId,pageNumber,pageSize,blockHeight,txType], 
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
blockHeight:long								//区块高度
txType:int										//交易类型(txType),type=0时查询所有交易
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
               {
                    "txHash": "a8611112f2b35385ee84f85……",		//交易hash
                    "address": "tNULSeBaMrbMRiFA……",			//账户地址
                    "type": 1,									//交易类型
                    "createTime": 1531152,						//交易时间，单位秒
                    "height": 0,								//交易被打包确定的区块高度
                    "chainId": 2,								//资产的链id
                    "assetId": 1,								//资产id
                    "symbol": "NULS",							//资产符号
                    "values": 1000000000000000,					//交易金额
                    "fee": { 									//bigInt	手续费
                        "chainId": 100,							//手续费链id
                        "assetId": 1,							//手续费资产id
                        "symbol": "ATOM",						//手续费资产符号
                        "value": 100000							//手续费金额
                    },
                    "balance": 1000000000000000,				//交易后账户的余额
                    "transferType": 1,							// -1:转出, 1:转入
                    "status": 1									//交易状态 0:未确认,1:已确认
               }
          ]
     }
}
```

#### 查询账户的交易列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountTxs",
    "params":[chainId,pageNumber,pageSize,address,txType,startHeight, endHeight],                       
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
address: string									//账户地址
txType:int										//交易类型(txType),type=0时查询所有交易
startHeight:long                                //打包交易的块起始高度，默认为-1,不限制

endHeight:long                                  //交易的块截止高度，默认为-1，不限制
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
               {
                    "txHash": "a8611112f2b35385ee84f85……",		//交易hash
                    "address": "tNULSeBaMrbMRiFA……",			//账户地址
                    "type": 1,									//交易类型
                    "createTime": 1531152,						//交易时间，单位秒
                    "height": 0,								//交易被打包确定的区块高度
                    "chainId": 2,								//资产的链id
                    "assetId": 1,								//资产id
                    "symbol": "NULS",							//资产符号
                    "values": 1000000000000000,					//交易金额
                    "fee": { 									//bigInt	手续费
                        "chainId": 100,							//手续费链id
                        "assetId": 1,							//手续费资产id
                        "symbol": "ATOM",						//手续费资产符号
                        "value": 100000							//手续费金额
                    },
                    "balance": 1000000000000000,				//交易后账户的余额
                    "transferType": 1,							// -1:转出, 1:转入
                    "status": 1									//交易状态 0:未确认,1:已确认
               }
          ]
     }
}
```

#### 验证离线组装交易是否合法

请求：

```
{
    "jsonrpc":"2.0",
    "method":"validateTx",
    "params":[chainId, txHex], 
    "id":1234
}
//参数说明
chainId: int									//链的id
txHex: string									//组装的交易序列化后的16进制字符串
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "value": "46b90763901898c0c250bd749……"				//交易hash
     }
}
```

#### 广播离线组装交易

请求：

```
{
    "jsonrpc":"2.0",
    "method":"broadcastTx",
    "params":[chainId, txHex], 
    "id":1234
}
//参数说明
chainId: int									//链的id
txHex: string									//组装的交易序列化后的16进制字符串
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "value": true							//true广播成功，false广播失败
     }
}
```

#### 广播离线组装交易(不验证合约)

请求：

```
{
    "jsonrpc":"2.0",
    "method":"broadcastTxWithNoContractValidation",
    "params":[chainId, txHex], 
    "id":1234
}
//参数说明
chainId: int									//链的id
txHex: string									//组装的交易序列化后的16进制字符串
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "value": true							//true广播成功，false广播失败
     }
}
```

### 共识相关接口[consensus]

#### 查询可委托共识节点列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getConsensusNodes",
    "params":[chainId,pageNumber,pageSize,type],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
type:int										//节点类型
												//0:所有节点,1:普通节点,2:开发者节点,3:大使节点
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
               {conesnsusInfo}
          ]
     }
}
```

#### 查询所有委托共识节点列表（包括已退出、或被红牌罚下的）

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAllConsensusNodes",
    "params":[chainId,pageNumber,pageSize],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
               {conesnsusInfo}
          ]
     }
}
```

#### 查询账户委托的共识节点列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountConsensus",
    "params":[chainId,pageNumber,pageSize, address],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
address:string									//账户地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
           		{conesnsusInfo}
          ]
     }
}
```

#### 查询共识节点详情

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getConsensusNode",
    "params":[chainId,txHash],
    "id":1234
}
//参数说明
chainId: int									//链的id
txHash:string									//创建节点时的交易hash
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {conesnsusInfo}
}
```

#### 查询账户创建的共识节点详情

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountConsensusNode",
    "params":[chainId,address],
    "id":1234
}
//参数说明
chainId: int									//链的id
address:string									//账户地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {conesnsusInfo}
}
```

#### 查询节点委托中列表信息

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getConsensusDeposit",
    "params":[chainId,pageNumber,pageSize,txHash],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
txHash:string									//创建节点时的交易hash
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
           		{depositInfo}
          ]
     }
}
```

#### 查询节点历史委托列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAllConsensusDeposit",
    "params":[chainId,pageNumber,pageSize,txHash,type],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
txHash:string									//创建节点时的交易hash
type:int										//0:加入委托,1:退出委托,2:所有  
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
           		{depositInfo}
          ]
     }
}
```

#### 查询账户的委托列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountDeposit",
    "params":[chainId,pageNumber,pageSize,address,agentHash],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
address:string									//账户地址	
txHash:string									//创建节点时的交易hash,为空时查询账户所有委托
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
           		{depositInfo}
          ]
     }
}
```

#### 查询账户的委托总额

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAccountDepositValue",
    "params":[chainId,address,agentHash],
    "id":1234
}
//参数说明
chainId: int									//链的id
address:string									//账户地址	
txHash:string									//创建节点时的交易hash,为空时查询账户所有委托
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": 10000000000						//委托总额
}
```

#### 查询共识惩罚列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getPunishList",
    "params":[chainId,pageNumber,pageSize,0,agentAddress],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
type:int							 			//惩罚类型  0:查询所有,1:黄牌,2:红牌
agentAddress:string								//共识节点的代理账户地址	
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
           		{
           			"txHash":				//string	惩罚交易hash
           			"type":					//int		惩罚类型 1:黄牌,2:红牌
           			"address":				//string	惩罚共识节点的代理账户地址
           			"time":					//long		惩罚时间
           			"blockHeight":			//long		惩罚交易的区块高度
           			"roundIndex":			//long		区块的轮次		
           			"packageIndex":			//long		打包的序列号
           			"reason":				//string	惩罚原因
           		}
          ]
     }
}
```

#### 查询轮次列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getRoundList",
    "params":[chainId,pageNumber,pageSize],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 1,
          "totalCount": 4036,
          "list": [
               {
                    "index": 155233203,				//long	共识轮次
                    "startTime": 1552371670001,		//long	当前轮起始时间
                    "memberCount": 2,				//int	当前轮出块节点数
                    "endTime": 1552371690001,		//long	当前轮结束时间
                    "redCardCount": 0,				//int	本轮罚出红牌数量
                    "yellowCardCount": 0,			//int	本轮发出黄牌数量
                    "producedBlockCount": 1,		//int	本轮共计出块数量
                    "startHeight": 8000,			//long	本轮起始高度
                    "endHeight": 0,					//long	本轮结束高度	
                    "lostRate": 0					//double 丢块率
               }
          ]
     }
}
```



### 智能合约相关接口[contract]

#### 查询合约详情

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getContract",
    "params":[chainId, contractAddress],
    "id":1234
}
chainId: int									//链的id
contractAddress:string							//智能合约地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "contractAddress": "tNULSeBaNC46Z66DgU……",		//string	合约地址
          "creater": "tNULSeBaMvEtDfvZuu……",				//string	合约创建人地址
          "createTxHash": "00209d28833258b192493……",		//string	创建合约的交易hash
          "blockHeight": 15,								//long		创建合约的区块高度
          "success": true,									//boolean	是否创建成功
          "balance": 0,										//bigInt	合约的NULS余额
          "errorMsg": null,									//string	创建失败的错误信息
          "status": 0,										//int		合约状态
          									-1:执行失败,0:未认证,1:正在审核,2:通过验证,3:已删除
          "certificationTime": 0,							//long		认证时间
          "createTime": 1553336525059,						//long		合约创建时间
          "remark": "create contract test",					//string	备注
          "txCount": 2,										//int		合约相关的交易
          "deleteHash": null,								//string	删除合约的交易hash
          "methods": [										//[object]	合约包含的函数
               {
                    "name": "name",							//string	接口名称
                    "returnType": "String",					//string	返回值类型
                    "params": []							//[object]	接口参数
               }
          ],
          "nrc20": true,									//boolean	是否是nrc20合约
          "tokenName": "KQB",								//string	token名称		
          "symbol": "KQB",									//string	token符号
          "decimals": 2,									//string	小数位
          "totalSupply": "1000000000000",					//bigInt	总量
          "transferCount": 2,								//int		token转账次数
          "owners": [										//[string]	token持有者
               "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
               "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD"
          ]
     }
}
```

#### 查询合约列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getContractList",
    "params":[chainId,pageNumber,pageSize,onlyNrc20,isHidden],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
tokenType:int 								    //合约token类型  0: 非token, 1: NRC20, 2: NRC721
isHidden: boolean 								//是否隐藏token类型合约
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
           		{
           			"contractAddress":				//string	合约地址
           			"remark":						//string	备注
           			"txCount":						//int		智能合约相关交易数量
           			"status":						//int		合约状态
           									-1:执行失败,0:未认证,1:正在审核,2:通过验证,3:已删除
           			"createTime":					//long		创建时间
           			"balance":						//bigInt	合约剩余NULS余额
           			"tokenName":					//string	token名称
           		    "symbol": "KQB",				//string	token符号
                    "decimals": 2,					//string	小数位
        			"totalSupply": "1000000000000", //bigInt	总量,
        			"tokenType":1                   //int       token类型, 0: 非token, 1: NRC20, 2: NRC721
           		}
          ]
     }
}
```

#### 查询合约相关交易列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getContractTxList",
    "params":[chainId,pageNumber,pageSize,txType,contractAddress],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
txType:int										//交易类型 默认为0，查询所有交易
contractAddress:string							//合约地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 3,
          "list": [
               {
                    "contractAddress": "tNULSeBaN32a2h……",		//string 合约地址
                    "txHash": "0020658e3edc61196e73be0……		//string 交易hash
                    "blockHeight": 12,							//long	 交易确认区块高度
                    "time": 1553336503846,						//long 	 交易生成时间
                    "type": 20									//int    交易类型
                    "fee": "5100000"							//bigint 交易手续费
               }
          ]
     }
}
```

#### 查询nrc20合约转账记录列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getContractTokens",
    "params":[chainId,pageNumber,pageSize,contractAddress],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
contractAddress:string							//合约地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 3,
          "list": [
              {
                 "address": "tNULSeBaMvEt……",				//string	账户地址
                 "tokenName": "KQB",						//string	转账token名称
                 "tokenSymbol": "KQB",						//string	转账token符号
                 "contractAddress": "tNULSeBaNC46Z……",		//string	合约地址
                 "balance": 999900000000,					//bigint	转账后余额
                 "decimals": 2								//int		精确小数位数
              }
          ]
     }
}
```

#### 查询账户nrc20转账记录列表

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getTokenTransfers",
    "params":[chainId,pageNumber,pageSize,address,contractAddress],
    "id":1234
}
//参数说明
chainId: int									//链的id
pageNumber:int									//页码
pageSize:int									//每页显示条数，取值[1-1000]
address:string									//账户地址
contractAddress:string							//合约地址
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": {
          "pageNumber": 1,
          "pageSize": 10,
          "totalCount": 1,
          "list": [
               {
                    "txHash": "002016f5a811b939535……",		//string	交易hash
                    "height": 19,							//long		交易打包确认区块高度
                    "contractAddress": "tNULSeBaNC……",		//string	合约地址
                    "name": "KQB",							//string	token名称
                    "symbol": "KQB",						//string	token符号
                    "decimals": 2,							//int		精确小数位数
                    "fromAddress": "tNULSeBaMvE……",			//string	转账地址
                    "toAddress": "tNULSeBaMnrs6……",			//string	接收地址
                    "value": "100000000",					//bigInt	转账金额
                    "time": 1553336574791,					//long		交易时间
                    "fromBalance": "999900000000",			//bigInt	转账人余额
                    "toBalance": "100000000"				//bigInt	接收人余额
               }
          ]
     }
}
```

### 统计相关接口[statistical]

#### 交易数量统计

请求：

```
{
"jsonrpc":"2.0",
"method":"getTxStatistical",
"params":[chainId,type],
"id":1234
}
//参数说明
chainId: int								//链的id
type: int							 		//0:最近14天, 1:最近一周, 2:最近一月, 3:最近一年
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": [
          {
               "key": "2018-6",						//string	统计周期
               "value": 265234						//long		统计数量
          },
          {
               "key": "2018-7",
               "value": 425327
          }
     ]
}
```

#### 统计共识节点数量

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getConsensusNodeCount",
    "params":[chainId],
    "id":1234
}
//参数说明
chainId: int								//链的id
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result":{
          "consensusCount":78,						//int	共识节点数量
          "seedsCount":5,							//int	种子节点数量
          "totalCount":83							//int	总数量
     }
}
```

#### 共识奖励统计

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getConsensusStatistical",
    "params":[chainId,type],
    "id":1234
}
//参数说明
chainId: int								//链的id
type: int							 		//0:14天，1:周，2：月，3：年，4：全部
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": [
          {
               "key": "6/5",							//string	统计周期
               "value": 556572872229264					//bigInt	共识奖励总额
          },
          {
               "key": "6/6",
               "value": 608939272229264
          },
          {
               "key": "6/7",
               "value": 628717072229264
          },
          {
               "key": "6/8",
               "value": 632738172229264
          },
          {
               "key": "6/9",
               "value": 629865972229264
          },
          {
               "key": "6/10",
               "value": 671865972229264
          }
     ]
}
```

#### 年化奖励率统计

请求：

```
{
    "jsonrpc":"2.0",
    "method":"getAnnulizedRewardStatistical",
    "params":[chainId,type],
    "id":1234
}
//参数说明
chainId: int								//链的id
type: int							 		//0:14天，1:周，2：月，3：年，4：全部
```

返回：

```
{
     "jsonrpc": "2.0",
     "id": 1234,
     "result": [
          {
               "key": "5/29",						//string	统计周期
               "value": 116.17						//dobule	年化收益%
          },
          {
               "key": "5/30",
               "value": 121.61
          },
          {
               "key": "5/31",
               "value": 106.16
          },
          {
               "key": "6/1",
               "value": 112.27
          },
          {
               "key": "6/2",
               "value": 112.27
          }
     ]
}     
```

