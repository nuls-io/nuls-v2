#交易模块
在NULS2.0的生态体系中，交易会在链中或者链与链之间流转，各条链的节点不仅要处理链内的交易，可能还会处理跨链的交易，于是每个节点需要处理的交易会越来越多，并且更复杂，因此我们需要一个但单独的模块来统一处理各种交易。而从NULS2.0的架构设计来看，我们需要一个独立的模块来处理交易的收集、验证、为区块组装提供安全的交易数据、存储等功能，对于所有交易来说，这些功能具有共用性、统一性，因此我们把交易管理作为一个独立的模块来运行。

###对本地交易进行处理

- 收集交易
- 本地验证
- 广播转发交易给其他节点
- 提取可打包的交易
- 提交、回滚交易
- 保存未确认的、可打包的以及已确认的交易
- 提供交易的数据

tx\_register
============
### scope:public
### version:1.0
注册模块交易/Register module transactions

参数列表
----
| 参数名                                                             |       参数类型       | 参数描述        | 是否非空 |
| --------------------------------------------------------------- |:----------------:| ----------- |:----:|
| chainId                                                         |       int        | 链id         |  是   |
| moduleCode                                                      |      string      | 注册交易的模块code |  是   |
| list                                                            |       list       | 待注册交易的数据    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txType          |       int        | 交易类型        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;systemTx        |     boolean      | 是否是系统交易     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;unlockTx        |     boolean      | 是否是解锁交易     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;verifySignature |     boolean      | 交易是否需要签名    |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;verifyFee       |     boolean      | 交易是否需要验证手续费 |  是   |
| delList                                                         | list&lt;integer> | 待移除已注册交易数据  |  否   |

返回值
---
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否注册成功 |

tx\_getNonexistentUnconfirmedHashs
==================================
### scope:public
### version:1.0
查询传入的交易hash中,不在未确认库中的交易hash/Get nonexistent unconfirmed transaction hashs

参数列表
----
| 参数名        |      参数类型       | 参数描述        | 是否非空 |
| ---------- |:---------------:| ----------- |:----:|
| chainId    |       int       | 链id         |  是   |
| txHashList | list&lt;string> | 待查询交易hash集合 |  是   |

返回值
---
| 字段名    |      字段类型       | 参数描述           |
| ------ |:---------------:| -------------- |
| txList | list&lt;string> | 返回交易序列化数据字符串集合 |

tx\_getTx
=========
### scope:public
### version:1.0
根据hash获取交易, 先查未确认, 查不到再查已确认/Get transaction by tx hash

参数列表
----
| 参数名     |  参数类型  | 参数描述      | 是否非空 |
| ------- |:------:| --------- |:----:|
| chainId |  int   | 链id       |  是   |
| txHash  | string | 待查询交易hash |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述             |
| --- |:------:| ---------------- |
| tx  | string | 获取到的交易的序列化数据的字符串 |

tx\_cs\_state
=============
### scope:public
### version:1.0
设置节点打包状态(由共识模块设置)/Set the node packaging state

参数列表
----
| 参数名       |  参数类型   | 参数描述   | 是否非空 |
| --------- |:-------:| ------ |:----:|
| chainId   |   int   | 链id    |  是   |
| packaging | boolean | 是否正在打包 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即设置成功 |

tx\_batchVerify
===============
### scope:public
### version:1.0
验证区块所有交易/Verify all transactions in the block

参数列表
----
| 参数名          |      参数类型       | 参数描述            | 是否非空 |
| ------------ |:---------------:| --------------- |:----:|
| chainId      |       int       | 链id             |  是   |
| txList       | list&lt;string> | 待验证交易序列化数据字符串集合 |  是   |
| blockHeader  |     string      | 对应的区块头          |  是   |
| preStateRoot |     string      | 前一个区块状态根        |  是   |

返回值
---
| 字段名          |      字段类型       | 参数描述       |
| ------------ |:---------------:| ---------- |
| value        |     boolean     | 是否验证成功     |
| contractList | list&lt;string> | 智能合约新产生的交易 |

tx\_gengsisSave
===============
### scope:public
### version:1.0
保存创世块的交易/Save the transactions of the Genesis block 

参数列表
----
| 参数名         |      参数类型       | 参数描述     | 是否非空 |
| ----------- |:---------------:| -------- |:----:|
| chainId     |       int       | 链id      |  是   |
| txList      | list&lt;string> | 待保存的交易集合 |  是   |
| blockHeader |     string      | 区块头      |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 是否成功 |

tx\_newTx
=========
### scope:public
### version:1.0
接收本地新交易/receive a new transaction

参数列表
----
| 参数名     |  参数类型  | 参数描述       | 是否非空 |
| ------- |:------:| ---------- |:----:|
| chainId |  int   | 链id        |  是   |
| tx      | string | 交易序列化数据字符串 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否成功   |
| hash  | string  | 交易hash |

tx\_packableTxs
===============
### scope:public
### version:1.0
获取可打包的交易集/returns a list of packaged transactions

参数列表
----
| 参数名            |  参数类型  | 参数描述      | 是否非空 |
| -------------- |:------:| --------- |:----:|
| chainId        |  int   | 链id       |  是   |
| endTimestamp   |  long  | 截止时间      |  是   |
| maxTxDataSize  |  int   | 交易集最大容量   |  是   |
| blockTime      |  long  | 本次出块区块时间  |  是   |
| packingAddress | string | 当前出块地址    |  是   |
| preStateRoot   | string | 前一个区块的状态根 |  是   |

返回值
---
| 字段名           |      字段类型       | 参数描述      |
| ------------- |:---------------:| --------- |
| list          | list&lt;string> | 可打包交易集    |
| stateRoot     |     string      | 当前出块的状态根  |
| packageHeight |      long       | 本次打包区块的高度 |

tx\_getBlockTxs
===============
### scope:public
### version:1.0
获取区块的完整交易，如果没有查询到，或者查询到的不是区块完整的交易数据，则返回空集合/Get block transactions

参数列表
----
| 参数名        |      参数类型       | 参数描述        | 是否非空 |
| ---------- |:---------------:| ----------- |:----:|
| chainId    |       int       | 链id         |  是   |
| txHashList | list&lt;string> | 待查询交易hash集合 |  是   |

返回值
---
| 字段名    |      字段类型       | 参数描述           |
| ------ |:---------------:| -------------- |
| txList | list&lt;string> | 返回交易序列化数据字符串集合 |

tx\_getBlockTxsExtend
=====================
### scope:public
### version:1.0
根据hash列表，获取交易，先查未确认，再查已确认/Get transactions by hashs

参数列表
----
| 参数名        |      参数类型       | 参数描述                                       | 是否非空 |
| ---------- |:---------------:| ------------------------------------------ |:----:|
| chainId    |       int       | 链id                                        |  是   |
| txHashList | list&lt;string> | 待查询交易hash集合                                |  是   |
| allHits    |     boolean     | true：必须全部查到才返回数据，否则返回空list； false：查到几个返回几个 |  是   |

返回值
---
| 字段名    |      字段类型       | 参数描述           |
| ------ |:---------------:| -------------- |
| txList | list&lt;string> | 返回交易序列化数据字符串集合 |

tx\_save
========
### scope:public
### version:1.0
保存新区块的交易/Save the confirmed transaction

参数列表
----
| 参数名          |      参数类型       | 参数描述     | 是否非空 |
| ------------ |:---------------:| -------- |:----:|
| chainId      |       int       | 链id      |  是   |
| txList       | list&lt;string> | 待保存的交易集合 |  是   |
| contractList | list&lt;string> | 智能合约交易   |  是   |
| blockHeader  |     string      | 区块头      |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 是否成功 |

tx\_backPackableTxs
===================
### scope:public
### version:1.0
共识模块把不能打包的交易还回来，重新加入待打包列表/back packaged transactions

参数列表
----
| 参数名     |      参数类型       | 参数描述         | 是否非空 |
| ------- |:---------------:| ------------ |:----:|
| chainId |       int       | 链id          |  是   |
| txList  | list&lt;string> | 交易序列化数据字符串集合 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 是否成功 |

tx\_rollback
============
### scope:public
### version:1.0
回滚区块的交易/transaction rollback

参数列表
----
| 参数名         |      参数类型       | 参数描述    | 是否非空 |
| ----------- |:---------------:| ------- |:----:|
| chainId     |       int       | 链id     |  是   |
| txHashList  | list&lt;string> | 待回滚交易集合 |  是   |
| blockHeader |     string      | 区块头     |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 是否成功 |

tx\_getSystemTypes
==================
### scope:public
### version:1.0
获取所有系统交易类型/Get system transaction types

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名  |       字段类型       | 参数描述     |
| ---- |:----------------:| -------- |
| list | list&lt;integer> | 系统交易类型集合 |

tx\_bl\_state
=============
### scope:public
### version:1.0
设置节点区块同步状态(由区块模块设置)/Set the node block state

参数列表
----
| 参数名     | 参数类型 | 参数描述          | 是否非空 |
| ------- |:----:| ------------- |:----:|
| chainId | int  | 链id           |  是   |
| status  | int  | 是否进入等待, 不处理交易 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即设置成功 |

tx\_blockHeight
===============
### scope:public
### version:1.0
接收最新区块高度/Receive the latest block height

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |
| height  | long | 区块高度 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 是否成功 |

tx\_getConfirmedTx
==================
### scope:public
### version:1.0
根据hash获取已确认交易(只查已确认)/Get confirmed transaction by tx hash

参数列表
----
| 参数名     |  参数类型  | 参数描述      | 是否非空 |
| ------- |:------:| --------- |:----:|
| chainId |  int   | 链id       |  是   |
| txHash  | string | 待查询交易hash |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述             |
| --- |:------:| ---------------- |
| tx  | string | 获取到的交易的序列化数据的字符串 |

tx\_getTxClient
===============
### scope:public
### version:1.0
根据hash获取交易，先查未确认，查不到再查已确认/Get transaction by tx hash

参数列表
----
| 参数名     |  参数类型  | 参数描述      | 是否非空 |
| ------- |:------:| --------- |:----:|
| chainId |  int   | 链id       |  是   |
| txHash  | string | 待查询交易hash |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述                   |
| ------ |:------:| ---------------------- |
| tx     | string | 获取到的交易的序列化数据的字符串       |
| height | string | 获取到的交易的确认高度，未确认交易高度为-1 |
| status | string | 获取到的交易是否确认的状态          |

transferCMDTest
===============
### scope:public
### version:1.0


参数列表
----
无参数

返回值
---
无返回值

tx\_getConfirmedTxClient
========================
### scope:public
### version:1.0
根据hash获取已确认交易(只查已确认)/Get confirmed transaction by tx hash

参数列表
----
| 参数名     |  参数类型  | 参数描述      | 是否非空 |
| ------- |:------:| --------- |:----:|
| chainId |  int   | 链id       |  是   |
| txHash  | string | 待查询交易hash |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述             |
| ------ |:------:| ---------------- |
| tx     | string | 获取到的交易的序列化数据的字符串 |
| height | string | 获取到的交易的确认高度      |
| status | string | 获取到的交易是否确认的状态    |

tx\_verifyTx
============
### scope:public
### version:1.0
验证交易接口，包括含基础验证、验证器、账本验证/Verify transation

参数列表
----
| 参数名     |  参数类型  | 参数描述       | 是否非空 |
| ------- |:------:| ---------- |:----:|
| chainId |  int   | 链id        |  是   |
| tx      | string | 待验证交易完整字符串 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |

