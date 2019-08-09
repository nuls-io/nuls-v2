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


