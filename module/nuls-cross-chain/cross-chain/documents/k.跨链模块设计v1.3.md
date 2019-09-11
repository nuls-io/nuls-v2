# 跨链模块设计文档

[TOC]

## 总体描述

### 模块概述

####  1 为什么要有《跨链》模块

​	在NULS2.0的生态体系中允许多个不同协议的平行链同时运行交互，由于不同平行链间协议不同，所以他们之间的协议交互需要由NULS主网来中转，跨链模块就是用于将本链协议转换为NULS主网协议和将接收到的NULS主网协议转换为本链协议的功能模块。

#### 2《跨链》要做什么

- 发起跨链交易，将跨链交易转换为主网协议交易
- 跨链交易链内拜占庭签名
- 广播跨链相关交易
- 跨链交易协议转换
- 链外跨链交易拜占庭验证
- 链外资产管理
- 跨链验证人维护
- 验证人变更维护

#### 3《跨链》在系统中的定位

​	在NULS2.0的生态体系中，跨链模块主要负责跨链交易的发起，验证，协议转换，链外资产维护，验证人变更维护等。

依赖模块

* 交易管理模块
* 网络模块
* 共识模块
* 链管理模块（主网需要依赖，平行链不需要依赖）
* 账本模块

## 模块配置

```
minNodeAmount              平行链与主网跨链交互时，最少需要连接的主网节点数
maxNodeAmount              一个节点最多连接的跨链节点数
sendHeight                 跨链交易打包之后需要多少个区块确认
byzantineRatio             本链跨链交易签名拜占庭比例（这个值必须大于等于在主网注册本链时填写的							   本链签名拜占庭比例）
crossSeedIps               主网跨链种子节点
verifiers                  主网验证人初始列表，该值为主网种子节点出块地址列表
mainByzantineRatio         主网跨链交易签名拜占庭比例（这个值必须小于等于主网配链内跨链签名拜占庭比                            例）
maxSignatureCount          主网设置的最大拜占庭签名数量
```

## 功能设计

###  功能架构图

![](.\cross-chain\cross_chain_functions.png)

### 核心流程

#### 1.初始化链

模块启动时需要读取已存在的所有链的配置信息来对各条链进行初始化，第一次将启动配置的默认链。

- 初始化链的基本信息

  加载链的配置信息，初始化运行链时的各种标识、状态等。

- 初始化链RocksDB表

  创建链运行时的各项数据存储的DB表。

- 初始化链的日志

  创建链的各个打印日志对象。

- 向依赖模块注册相关信息

  向交易模块注册本模块的交易，向网络注册本模块的协议，激活跨链网络，向账户模块注册链地址前缀

- 初始化链的缓存

  创建链运行时的缓存和队列。

- 初始化链任务调度器及工作线程

  创建链运行时的各种定时任务及线程。

#### 2.注册链

平行链要实现跨链功能，首先需要在NULS主网注册本链信息（本链魔法参数，验证人列表，跨链资产信息等）

#### 3.初始化验证人

平行链在主网注册跨链交易后需要同步主网当前的验证人列表，也需要将本链当前的验证人列表同步给主网

![](.\cross-chain\VerifireInit.jpg)

##### 3.1主网验证人初始化

- 主网创建初始化验证人列表交易
- 在链内发起种子节点签名拜占庭验证
- 拜占庭验证通过后将交易广播给注册链

##### 3.2平行链验证人初始化

- 收到主网发送过来的初始化验证人列表交易
- 用本链配置的主网初始化验证人列表对该交易做签名拜占庭验证
- 验证通过之刷新本链主网初始化验证人列表
- 创建一笔初始化本链验证人列表交易
- 在链内发起种子节点签名拜占庭验证
- 拜占庭验证通过之后将交易广播给主网

##### 3.3主网更新注册链验证人列表

- 收到注册链发送过来的初始化验证人列表交易
- 用注册链注册时填写的初始化验证人列表对该交易做签名拜占庭验证
- 验证通过则更新注册链验证人列表

#### 4.验证人变更

当平行链有新的验证人加入或有验证人退出时需要将新增和注销的验证人信息广播通知主网，同样的当主网有新的验证人加入或有验证人注销时，需要将新增的和注销的验证人信息广播通知所有平新链

##### 4.1主网验证人变更

![](.\cross-chain\MainNet-VerifierChange.jpg)

- 主网有验证人增加或验证人注销
- 创建验证人变更交易（包含新增和注销的验证人列表）
- 发起链内拜占庭签名验证
- 拜占庭验证通过，将主网验证人变更信息广播给所有在主网注册跨链的平行链
- 平行链收到主网验证人变更交易后，验证主网验证人变更交易，验证通过后更新本链主网验证人列表

##### 4.2平行链验证人变更

- 平行链有验证人增加或验证人注销
- 创建验证人变更交易（包含新增和注销的验证人列表）
- 发起链内拜占庭签名验证
- 拜占庭验证通过，将平行链验证人变更信息广播给主网
- 主网收到平行链验证人变更交易并验证平行链验证人变更交易，验证通过，更新平新链验证人列表

#### 5.创建跨链转账交易

跨链转账交易手续费需要消耗NULS，所以平行链账户在发起跨链转账交易时需要确保账户有足够的NULS。

##### 5.1主网转平行链

![](.\cross-chain\Main-Parallel-Ctx.jpg)

###### 5.1.1主网流程

- 发起跨链转账交易
- 验证跨链转账交易，包括接收链是已注册跨链，转账账户NULS是否足够支付手续费等
- 验证通过之后，发起链内签名拜占庭
- 拜占庭验证通过之后，将跨链转账交易广播给接收链

###### 5.1.2接收链流程

- 接收链接收到主网广播过来的跨链转账交易，
- 对主网广播的跨链转账交易做签名拜占庭验证
- 验证通过后，将主网协议跨链交易转换为本链协议跨链交易，发送交易模块处理，交易模块处理完成时候，则等待交易被打包

##### 5.2平行链转主网

![](.\cross-chain\Parallel-Main-Ctx.jpg)

###### 5.2.1发起链流程

- 发起跨链转账交易
- 验证跨链转账交易，包括本链是否已注册跨链，接收链是都注册跨链，转账账户NULS是否足够支付手续费等
- 验证通过之后，发起链内签名拜占庭
- 拜占庭验证通过之后，将本链协议跨链转交易转换为主网协议跨链转账交易广播给主网

###### 5.2.2主网流程

- 主网接收到平行链广播过来的跨链转账交易后对平行链广播的跨链转账交易做签名拜占庭验证
- 验证通过后，则发送交易模块处理，交易模块处理完成时候，则等待交易被打包

##### 5.3平行链A转平行链B

![](.\cross-chain\Parallel-Parallel-Ctx.jpg)

###### 5.3.1发起链流程

- 发起跨链转账交易
- 验证跨链转账交易，包括本链是否已注册跨链，接收链是都注册跨链，转账账户NULS是否足够支付手续费等
- 验证通过之后，发起链内签名拜占庭
- 拜占庭验证通过之后，将本链协议跨链转交易转换为主网协议跨链转账交易广播给主网

###### 5.3.2主网流程

- 主网接收到平行链广播过来的跨链转账交易后对平行链广播的跨链转账交易做签名拜占庭验证
- 验证通过后，则发送交易模块处理，交易模块处理完成时候，则等待交易被打包
- 判断主网是否为接收链，如果主网不为接收链，则将收到的跨链转账交易已有的签名列表清空，并在主网链内对该交易发起签名拜占庭
- 主网链内签名拜占庭完成，将主网链内拜占庭后的跨链交易广播给接收链

###### 5.3.3接收链流程

- 接收链接收到主网广播过来的跨链转账交易，
- 对主网广播的跨链转账交易做签名拜占庭验证
- 验证通过后，将主网协议跨链交易转换为本链协议跨链交易，发送交易模块处理，交易模块处理完成时候，则等待交易被打包

#### 6.链内签名拜占庭流程

- 新建跨链交易，判断本地节点是否为验证人节点
- 如果本地是验证人节点，则对跨链交易签名并将签名广播给链内其他节点
- 收集本链验证人对跨链交易的签名
- 当收集的签名数量 >= 最小拜占庭签名数（本链当前验证人数量 * 本链配置的签名拜占庭比例）时，剔除无效签名（不是本链当前验证人的签名）后，如果有效签名 >= 最小签名数量，则表示跨链交易的链内签名拜占庭完成

#### 7.接收到跨链交易签名拜占庭验证流程

- 接收到其他链广播的跨链交易
- 验证跨链交易签名正确性
- 查询发送链的验证人列表及签名拜占庭比例信息
- 验证跨链交易签名数量 >= 发送链最小拜占庭签名数量（验证人数量 *  拜占庭比例）
- 验证跨链交易签名是否为验证人签名
- 如果以上几条都验证通过则表示接收的跨链交易签名拜占庭验证通过



## 模块服务

参考[跨链模块RPC-API接口文档](./cross-chain.md)



## 协议

### 1.BroadCtxHashMessage

- 消息说明：跨链广播跨链交易Hash

- cmd:recvCtxHash

  | Length | Fields      | Type   | Remark               |
  | ------ | ----------- | ------ | -------------------- |
  | ?      | convertHash | byte[] | 主网协议跨链交易hash |

### 2.BroadCtxSignMessage

- 消息说明：广播跨链交易签名给链内其他节点

- cmd:recvCtxSign

  | Length | Fields    | Type   | Remark               |
  | ------ | --------- | ------ | -------------------- |
  | ?      | localHash | byte[] | 本链协议跨链交易hash |
  | ？     | signature | byte[] | 跨链交易签名         |

### 3.GetCtxMessage

- 消息说明：向本链其他节点获取完整跨链交易

- cmd:getCtx

  | Length | Fields      | Type   | Remark               |
  | ------ | ----------- | ------ | -------------------- |
  | ?      | requestHash | byte[] | 本链协议跨链交易hash |

### 4.GetOtherCtxMessage

- 消息说明：向发送链获取完整跨链交易

- cmd:getOtherCtx

  | Length | Fields      | Type   | Remark               |
  | ------ | ----------- | ------ | -------------------- |
  | ?      | requestHash | byte[] | 主网协议跨链交易hash |

### 5.NewCtxMessage

- 消息说明：接收到链内其他节点发送的完整跨链交易

- cmd:recvCtx

  | Length | Fields      | Data Type | Remark                         |
  | ------ | ----------- | --------- | ------------------------------ |
  | 2      | type        | uint16    | 交易类型                       |
  | 4      | time        | uint32    | 交易时间                       |
  | ？     | txData      | VarByte   | 交易数据，存放原跨链交易的Hash |
  | ？     | coinData    | VarByte   | 交易输入和输出                 |
  | ？     | remark      | VarString | 备注                           |
  | ？     | scriptSig   | VarByte   | 数字脚本与交易签名             |
  | ?      | requestHash | byte[]    | 主网协议跨链交易hash           |

### 6.NewOtherCtxMessage

- 消息说明：接收到其他链节点发送的完整跨链交易

- cmd:recvOtherCtx

  | Length | Fields      | Data Type | Remark                         |
  | ------ | ----------- | --------- | ------------------------------ |
  | 2      | type        | uint16    | 交易类型                       |
  | 4      | time        | uint32    | 交易时间                       |
  | ？     | txData      | VarByte   | 交易数据，存放原跨链交易的Hash |
  | ？     | coinData    | VarByte   | 交易输入和输出                 |
  | ？     | remark      | VarString | 备注                           |
  | ？     | scriptSig   | VarByte   | 数字脚本与交易签名             |
  | ?      | requestHash | byte[]    | 主网协议跨链交易hash           |

### 7.GetCtxStateMessage

- 消息说明：其他链节点向本节点查询跨链交易处理状态

- cmd:getCtxState

  | Length | Fields      | Type   | Remark               |
  | ------ | ----------- | ------ | -------------------- |
  | ?      | requestHash | byte[] | 主网协议跨链交易hash |

### 8.CtxStateMessage

- 消息说明：接收到跨链交易处理结果返回值

- cmd:recvCtxState

  | Length | Fields       | Type   | Remark                                            |
  | ------ | ------------ | ------ | ------------------------------------------------- |
  | ?      | requestHash  | byte[] | 主网协议跨链交易hash                              |
  | 1      | handleResult | byte   | 跨链交易处理结果0未确认 1主网已确认 2接收链已确认 |

### 9.GetCirculationMessage

- 消息说明：平行链节点收到主网节点发送的查询本链资产消息

- cmd:getCirculat

  | Length | Fields   | Type   | Remark                                         |
  | ------ | -------- | ------ | ---------------------------------------------- |
  | ?      | assetIds | String | 需要查询的平行链资产ID（多个资产ID用逗号分隔） |

### 10.CirculationMessage

- 消息说明：主网收到平行链资产消息

- cmd:recvCirculat

  | Length | Fields          |  Type   | Remark                          |
  | ------ | --------------- | :-----: | ------------------------------- |
  | ?      | circulationList | VarByte | 平行链资产列表List<Circulation> |

- Circulation

  | Length | Fields          | Type       | Remark   |
  | ------ | --------------- | ---------- | -------- |
  | 2      | assetId         | uint16     | 资产ID   |
  | ?      | availableAmount | BigInteger | 可用资产 |
  | ?      | freeze          | BigInteger | 冻结资产 |

### 11.GetRegisteredChainMessage

- 消息说明：平行链向主网查询所有已注册跨链的链信息
- cmd:getChains



### 12.RegisteredChainMessage

- 消息说明：平行链接收到主网返回的已注册跨链的链信息

- cmd:recvRegChain

  | Length | Fields        |  Type   | Remark                            |
  | ------ | ------------- | :-----: | --------------------------------- |
  | ?      | chainInfoList | VarByte | 已注册跨链的链列表List<ChainInfo> |

- ChainInfo

  | Length |         Fields          |   Type    |         Remark          |
  | :----: | :---------------------: | :-------: | :---------------------: |
  |   2    |         chainId         |  uint16   |          链ID           |
  |   ?    |        chainName        | VarString |         链名称          |
  |   2    |   minAvailableNodeNum   |  uint16   |     跨链最小链接数      |
  |   2    |    maxSignatureCount    |  uint16   | 签名拜占庭最大签名数量  |
  |   2    | signatureByzantineRatio |  uint16   |     签名拜占庭比例      |
  |   ?    |      addressPrefix      | VarString |        地址前缀         |
  |   4    |      registerTime       |  uint32   |        注册时间         |
  |   ?    |      assetInfoList      |  VarByte  | 资产列表List<AssetInfo> |
  |   ?    |      verifierList       |  VarByte  |  验证人列表Set<String>  |

- AssetInfo

  | Length |    Fields     |  Type   |  Remark  |
  | :----: | :-----------: | :-----: | :------: |
  |   2    |    assetId    | uint16  |  资产id  |
  |   ？   |    symbol     | String  | 资产符号 |
  |   ？   |   assetName   | String  | 资产名称 |
  |   2    |    usable     | boolean | 是否可用 |
  |   2    | decimalPlaces | uint16  | 资产精度 |

## 如何开发一个跨链模块

- 新建一个maven项目导入跨链基础包

  ```
  <dependency>
        <groupId>io.nuls.v2.cross-chain</groupId>
        <artifactId>base-lib</artifactId>
        <version>1.0.0-SNAPSHOT</version>
  </dependency>
  ```

- 创建模块启动类并让该类继承io.nuls.crosschain.base.BaseCrossChainBootStrap类

  - 如果新创建的跨链模块有除了base-lib中提供的cmd以外的其他cmd,则需要在init()方法中将本模块中新增的cmd类路径添加到cmd目录列表里

    ```
    registerRpcPath(RPC_PATH);在init()方法中添加cmd目录
    ```

- 实现io.nuls.crosschain.base.service.CrossChainService类，该类负责处理跨链转账交易相关操作

  - createCrossTx创建跨链转账交易
  - newApiModuleCrossTx 处理apiModule发送的跨链转账交易
  - commitCrossTx跨链转账交易提交
  - rollbackCrossTx跨链转账交易回滚
  - crossTxBatchValid跨链交易验证器
  - getCrossTxState查询跨链转账交易处理状态
  - getRegisteredChainInfoList 处理apiModule查询所有已注册跨链的连信息
  - getByzantineCount处理apiModule查询当前最小拜占庭签名数量

- 实现io.nuls.crosschain.base.service.VerifierChangeTxService类，该类负责处理跨链验证人变更交易相关操作

  - validate验证人变更交易验证
  - commit验证人变更交易提交
  - rollback验证人变更交易回滚

- 实现io.nuls.crosschain.base.service.VerifierInitService类，该类负责处理验证人初始化交易相关操作

  - validate验证人初始化交易验证
  - commit验证人初始化交易提交
  - rollback验证人初始化交易回滚

- 实现io.nuls.crosschain.base.service.ProtocolService类，该类负责处理跨链网络消息

  - receiveCtxSign接收到链内节点广播的跨链交易签名
  - getCtx链内节点向本节点获取完整跨链交易
  - receiveCtx接收到本链节点广播的完整跨链交易
  - receiveCtxHash接收到跨链节点广播的跨链交易Hash
  - getOtherCtx跨链节点向本链获取完整跨链交易
  - receiveOtherCtx接收到跨链节点广播的完整跨链交易
  - getCtxState跨链节点向当前节点查询跨链交易处理状态
  - receiveCtxState接收到跨链节点广播的跨链交易处理结果
  - getCirculation主网向平行链节点查新本链资产流通量
  - receiveRegisteredChainInfo平行链接收到主网广播的已注册跨链的链信息
