# public-service模块设计文档

[TOC]

## 总体概览

### 模块概述

#### 为什么要有public-service模块

区块链项目在运行过程中，所产生的链上的数据，都会相互广播，每个节点也会存储数据。但这些数据，并不能直观的展示给用户，且用户需要查询相关的数据也很麻烦。public-service指在给用户提供一个通过浏览器或网页钱包查询链上数据和相关统计信息。

#### public-service要做什么

解析节点钱包已同步到的区块，将数据存入到可提供关系查询和统计的数据库中。

对外提供查询区块、交易、账户、共识信息、合约信息、统计信息等接口。

#### public-service在系统中的定位

public-service属于辅助型模块，非底层核心模块，因此默认钱包启动后不会运行该模块。

运行public-service前服务器需要先安装数据库，默认实现是mongoDB数据库。

## 功能设计

### 功能架构图

![](/img/public-service-functions.png)



### 接口说明

**io.nuls.api.analysis**

负责调用底层模块接口和解析接口返回的数据

WalletRpcHandler：public-service调用其他模块RPC接口处理类

AnalysisHandler: public-service解析底层区块数据处理类

**io.nuls.api.db**

提供public-service数据库增删改查的接口与实现

**io.nuls.api.model**

public-service的数据结构，包括持久层、dto层

**io.nuls.api.rpc**

对外提供rpc接口，查询区块、交易、账户信息等

**io.nuls.api.service**

public-service同步区块和回滚区块的主业务接口

SyncService: 同步区块

RollbackService：回滚区块

**io.nuls.api.task**

public-service定时任务，包括同步区块任务、统计任务等

SyncBlockTask：同步区块的定时任务

## 模块RPC接口

参考[NULS2.0-API接口文档](./account.md)

 

## Java特有的设计

### JAVA实现细节简要说明

**io.nuls.api.cache.ApiCache**

缓存链上的常用数据，包括链信息、账户信息、共识信息、统计信息等。

**io.nuls.api.task.SyncBlockTask**

调用底层区块模块接口，获取下一个区块，区块连续性验证成功后，存储数据到mongoDB，继续获取下一区块信息；若区块hash连续性验证失败，回滚当前已存储的最新块，直到连续性验证通过为止。

若获取不到下一区块，说明当前public-service已经解析到最新高度，则每过10秒，重新获取一次最新高度的区块并做解析和存储。

**io.nuls.api.service.SyncService**

同步区块的主接口， 首先得处理区块奖励的统计，再根据不同交易，处理各个业务相关的数据，再处理轮次相关的信息，最后将解析完的数据存储到数据库。

