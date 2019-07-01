模块说明

```
这个文件夹下才开始放置代码，可以是任意语言开发的代码
```


createCrossTx
=============
### scope:public
### version:1.0
创建跨链转账交易/Creating Cross-Chain Transfer Transactions

参数列表
----
| 参数名                                                           |    参数类型    | 参数描述   | 是否非空 |
| ------------------------------------------------------------- |:----------:| ------ |:----:|
| chainId                                                       |    int     | 链ID    |  是   |
| listFrom                                                      |    list    | 转出信息列表 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | 账户地址   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | 资产链ID  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | 资产ID   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | 转出金额   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password      |   string   | 账户密码   |  是   |
| listTo                                                        |    list    | 转如信息列表 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | 账户地址   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | 资产链ID  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | 资产ID   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | 转出金额   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password      |   string   | 账户密码   |  是   |
| remark                                                        |   string   | 备注     |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述     |
| ------ |:------:| -------- |
| txHash | string | 跨链交易HASH |

getCrossTxState
===============
### scope:public
### version:1.0
查询跨链交易处理状态/get cross transaction process state

参数列表
----
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| txHash  | string | 交易HASH |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述       |
| ----- |:-------:| ---------- |
| value | boolean | 跨链交易是否处理完成 |

getRegisteredChainInfoList
==========================
### scope:public
### version:1.0
查询在主网上注册跨链的链信息/Query for cross-chain chain information registered on the main network

参数列表
----
无参数

返回值
---
| 字段名                                                                                                           |      字段类型       | 参数描述      |
| ------------------------------------------------------------------------------------------------------------- |:---------------:| --------- |
| list                                                                                                          | list&lt;object> | 已注册跨链的链信息 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainId                                                       |       int       | 链ID       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainName                                                     |     string      | 链名称       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minAvailableNodeNum                                           |       int       | 最小链接数     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetInfoList                                                 | list&lt;object> | 链资产列表     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId       |       int       | 资产ID      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol        |     string      | 资产符号      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetName     |     string      | 资产名称      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;usable        |     boolean     | 是否可用      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimalPlaces |       int       | 精度        |

getFriendChainCirculate
=======================
### scope:public
### version:1.0
获取友链资产信息/Access to Friendship Chain Asset Information

参数列表
----
| 参数名      |  参数类型  | 参数描述             | 是否非空 |
| -------- |:------:| ---------------- |:----:|
| chainId  |  int   | 链ID              |  是   |
| assetIds | string | 资产ID，多个资产ID用逗号分隔 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

getChains
=========
### scope:public
### version:1.0
cancel Cross Chain

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

recvCirculat
============
### scope:public
### version:1.0
接收其他链节点发送的资产信息/Receiving asset information sent by other link nodes

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

registerCrossChain
==================
### scope:public
### version:1.0
链注册跨链/register Cross Chain

参数列表
----
| 参数名                 |  参数类型  | 参数描述  | 是否非空 |
| ------------------- |:------:| ----- |:----:|
| chainId             |  int   | 链ID   |  是   |
| chainName           | string | 链名称   |  是   |
| minAvailableNodeNum |  int   | 最小链接数 |  是   |
| assetInfoList       | string | 资产列表  |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 处理结果 |

cancelCrossChain
================
### scope:public
### version:1.0
指定链资产退出跨链/Specified Chain Assets Exit Cross Chain

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| assetId | int  | 资产ID |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 处理结果 |

crossChainRegisterChange
========================
### scope:public
### version:1.0
跨链注册信息变更/Registered Cross Chain change

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

newBlockHeight
==============
### scope:public
### version:1.0
链区块高度变更/receive new block height

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链ID  |  是   |
| height  | string | 链ID  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

getOtherCtx
===========
### scope:public
### version:1.0
跨链节点向本节点获取完整交易/Cross-chain nodes obtain complete transactions from their own nodes

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

recvCtxSign
===========
### scope:public
### version:1.0
接收链内节点广播的交易签名/Transaction signature for broadcasting in receiving chain

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

verifyCtx
=========
### scope:public
### version:1.0
验证跨链交易/Verification of cross-chain transactions

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

getCtx
======
### scope:public
### version:1.0
链内节点向本节点获取完成跨链交易/The intra-chain node acquires and completes the cross-chain transaction from its own node

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

getCtxState
===========
### scope:public
### version:1.0
获取跨链交易处理状态/Getting the state of cross-chain transaction processing

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

recvVerifyRs
============
### scope:public
### version:1.0
接收跨链交易验证结果/receive cross transaction verify result

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

recvCtxState
============
### scope:public
### version:1.0
跨链交易处理状态消息/receive cross transaction state

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

recvCtx
=======
### scope:public
### version:1.0
接收本链节点广播的完整交易/Complete Transaction for Receiving Broadcast from Local Chain Nodes

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

recvOtherCtx
============
### scope:public
### version:1.0
接收跨链节点广播的完整交易/Receiving Complete Transactions for Cross-Chain Node Broadcasting

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

recvCtxHash
===========
### scope:public
### version:1.0
接收跨链节点广播的交易Hash/Transaction Hash receiving cross-link node broadcasting

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

recvRegChain
============
### scope:public
### version:1.0
接收到主网返回的已注册跨链交易的链信息/Receiving chain information of registered cross-chain transactions returned from the main network

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

getCirculat
===========
### scope:public
### version:1.0
查询本链资产信息消息/get chain circulation

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

