模块说明

```
这个文件夹下才开始放置代码，可以是任意语言开发的代码
```

cs\_doubleSpendRecord
=====================
### scope:public
### version:1.0
double spend transaction record 1.0

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链id  |  是   |
| block   | string | 区块信息 |  是   |
| tx      | string | 分叉交易 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 处理结果 |

cs\_getWholeInfo
================
### scope:public
### version:1.0
query the consensus information of the whole network 1.0

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名                    |  字段类型  | 参数描述       |
| ---------------------- |:------:| ---------- |
| agentCount             |  int   | 节点数量       |
| totalDeposit           | string | 总委托两       |
| rewardOfDay            | string | 当天共识奖励总量   |
| consensusAccountNumber |  int   | 参与共识人数     |
| packingAgentCount      |  int   | 当前轮次出块节点数量 |

cs\_getInfo
===========
### scope:public
### version:1.0
query consensus information for specified accounts 1.0

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链id  |  是   |
| address | string | 账户地址 |  是   |

返回值
---
| 字段名            |  字段类型  | 参数描述      |
| -------------- |:------:| --------- |
| agentCount     |  int   | 节点数量      |
| totalDeposit   | string | 参与共识的总金额  |
| joinAgentCount |  int   | 参与共识节点的数量 |
| usableBalance  | string | 可用余额      |
| reward         | string | 获得的共识奖励   |
| rewardOfDay    | string | 当天获得的共识奖励 |
| agentHash      | string | 创建的节点HASH |

cs\_getPublishList
==================
### scope:public
### version:1.0
query punish list 1.0

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链id  |  是   |
| address | string | 地址   |  是   |
| type    |  int   | 惩罚类型 |  是   |

返回值
---
| 字段名          |      字段类型       | 参数描述      |
| ------------ |:---------------:| --------- |
| redPunish    | list&lt;string> | 获得的红牌列表   |
| yellowPunish | list&lt;string> | 获得的黄牌惩罚列表 |

cs\_getRoundInfo
================
### scope:public
### version:1.0
get current round information 1.0

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名                                                                                                            |      字段类型       | 参数描述                                  |
| -------------------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------- |
| totalWeight                                                                                                    |     double      | 当前轮次总权重                               |
| index                                                                                                          |      long       | 轮次下标                                  |
| startTime                                                                                                      |      long       | 轮次开始时间                                |
| endTime                                                                                                        |      long       | 轮次结束时间                                |
| memberCount                                                                                                    |       int       | 本轮次出块节点数                              |
| memberList                                                                                                     | list&lt;object> | 本轮次出块成员信息                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundIndex                                                     |      long       | 轮次下标                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundStartTime                                                 |      long       | 轮次开始时间                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingIndexOfRound                                            |       int       | 该节点在本轮次中第几个出块                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agent                                                          |     object      | 共识节点信息                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress   |     byte[]      | 节点地址                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress |     byte[]      | 出块地址                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rewardAddress  |     byte[]      | 奖励地址                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |   biginteger    | 保证金                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;commissionRate |      byte       | 佣金比例                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time           |      long       | 创建时间                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight    |      long       | 所在区块高度                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delHeight      |      long       | 节点注销高度                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status         |       int       | 状态，0:待共识 unConsensus, 1:共识中 consensus |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;creditVal      |     double      | 信誉值                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalDeposit   |   biginteger    | 节点总委托金额                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash         |    nulshash     | 创建该节点的交易HASH                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;memberCount    |       int       | 参与共识人数                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alais          |     string      | 节点别名                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositList                                                    | list&lt;object> | 当前节点委托信息                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |   biginteger    | 委托金额                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash      |    nulshash     | 委托的节点HASH                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address        |     byte[]      | 委托账户                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time           |      long       | 委托时间                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status         |       int       | 状态                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash         |    nulshash     | 委托交易HASH                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight    |      long       | 委托交易被打包的高度                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delHeight      |      long       | 退出委托高度                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sortValue                                                      |     string      | 排序值                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packStartTime                                                  |      long       | 当前节点开始出块时间                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packEndTime                                                    |      long       | 当前节点出块结束时间                            |
| preRound                                                                                                       |  meetinground   | 上一轮信息                                 |
| myMember                                                                                                       |     object      | 当前节点出块信息                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundIndex                                                     |      long       | 轮次下标                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundStartTime                                                 |      long       | 轮次开始时间                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingIndexOfRound                                            |       int       | 该节点在本轮次中第几个出块                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agent                                                          |     object      | 共识节点信息                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress   |     byte[]      | 节点地址                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress |     byte[]      | 出块地址                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rewardAddress  |     byte[]      | 奖励地址                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |   biginteger    | 保证金                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;commissionRate |      byte       | 佣金比例                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time           |      long       | 创建时间                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight    |      long       | 所在区块高度                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delHeight      |      long       | 节点注销高度                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status         |       int       | 状态，0:待共识 unConsensus, 1:共识中 consensus |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;creditVal      |     double      | 信誉值                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalDeposit   |   biginteger    | 节点总委托金额                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash         |    nulshash     | 创建该节点的交易HASH                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;memberCount    |       int       | 参与共识人数                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alais          |     string      | 节点别名                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositList                                                    | list&lt;object> | 当前节点委托信息                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |   biginteger    | 委托金额                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash      |    nulshash     | 委托的节点HASH                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address        |     byte[]      | 委托账户                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time           |      long       | 委托时间                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status         |       int       | 状态                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash         |    nulshash     | 委托交易HASH                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight    |      long       | 委托交易被打包的高度                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delHeight      |      long       | 退出委托高度                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sortValue                                                      |     string      | 排序值                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packStartTime                                                  |      long       | 当前节点开始出块时间                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packEndTime                                                    |      long       | 当前节点出块结束时间                            |

cs\_getRoundMemberList
======================
### scope:public
### version:1.0
get current round information 1.0

参数列表
----
| 参数名     |  参数类型  | 参数描述    | 是否非空 |
| ------- |:------:| ------- |:----:|
| chainId |  int   | 链id     |  是   |
| extend  | string | 区块头扩展信息 |  是   |

返回值
---
| 字段名             |      字段类型       | 参数描述       |
| --------------- |:---------------:| ---------- |
| packAddressList | list&lt;string> | 当前伦次出块地址列表 |

cs\_getConsensusConfig
======================
### scope:public
### version:1.0
get consensus config

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名             |  字段类型   | 参数描述              |
| --------------- |:-------:| ----------------- |
| seedNodes       | string  | 种子节点列表            |
| inflationAmount | integer | 委托金额最大值           |
| agentAssetId    | integer | 共识资产ID            |
| agentChainId    | integer | 共识资产链ID           |
| awardAssetId    | integer | 奖励资产ID（共识奖励为本链资产） |

cs\_addEvidenceRecord
=====================
### scope:public
### version:1.0
add evidence record 1.0

参数列表
----
| 参数名            |  参数类型  | 参数描述   | 是否非空 |
| -------------- |:------:| ------ |:----:|
| chainId        |  int   | 链id    |  是   |
| blockHeader    | string | 分叉区块头一 |  是   |
| evidenceHeader | string | 分叉区块头二 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 处理结果 |

cs\_stopAgent
=============
### scope:public
### version:1.0
stop agent 1.0

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| address  | string | 节点地址 |  是   |
| password | string | 密码   |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述       |
| ------ |:------:| ---------- |
| txHash | string | 停止节点交易HASH |

cs\_createAgent
===============
### scope:public
### version:1.0
create agent 1.0

参数列表
----
| 参数名            |  参数类型  | 参数描述        | 是否非空 |
| -------------- |:------:| ----------- |:----:|
| chainId        |  int   | 链id         |  是   |
| agentAddress   | string | 节点地址        |  是   |
| packingAddress | string | 节点出块地址      |  是   |
| rewardAddress  | string | 奖励地址,默认节点地址 |  否   |
| commissionRate |  int   | 佣金比例        |  是   |
| deposit        | string | 抵押金额        |  是   |
| password       | string | 密码          |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述       |
| ------ |:------:| ---------- |
| txHash | string | 创建节点交易HASH |

cs\_getAgentInfo
================
### scope:public
### version:1.0
query specified node information 1.0

参数列表
----
| 参数名     | 参数类型 | 参数描述   | 是否非空 |
| ------- |:----:| ------ |:----:|
| chainId | int  | 链id    |  是   |
| chainId | int  | 节点HASH |  是   |

返回值
---
| 字段名            |  字段类型  | 参数描述       |
| -------------- |:------:| ---------- |
| agentHash      | string | 节点HASH     |
| agentAddress   | string | 节点地址       |
| packingAddress | string | 节点出块地址     |
| rewardAddress  | string | 节点奖励地址     |
| deposit        | string | 抵押金额       |
| commissionRate |  byte  | 佣金比例       |
| agentName      | string | 节点名称       |
| agentId        | string | 节点ID       |
| introduction   | string | 节点简介       |
| time           |  long  | 节点创建时间     |
| blockHeight    |  long  | 节点打包高度     |
| delHeight      |  long  | 节点失效高度     |
| status         |  int   | 状态         |
| creditVal      | double | 信誉值        |
| totalDeposit   | string | 总委托金额      |
| txHash         | string | 创建节点交易HASH |
| memberCount    |  int   | 委托人数       |
| version        | string | 版本         |

cs\_getAgentStatus
==================
### scope:public
### version:1.0
query the specified consensus node status 1.0

参数列表
----
| 参数名       |  参数类型  | 参数描述   | 是否非空 |
| --------- |:------:| ------ |:----:|
| chainId   |  int   | 链id    |  是   |
| agentHash | string | 节点HASH |  是   |

返回值
---
| 字段名    | 字段类型 | 参数描述 |
| ------ |:----:| ---- |
| status | byte | 节点状态 |

cs\_updateAgentConsensusStatus
==============================
### scope:public
### version:1.0
modifying the Node Consensus State 1.0

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述                    |
| --- |:----:| ----------------------- |
| N/A | void | 无特定返回值，无错误则表示节点共识状态修改成功 |

cs\_updateAgentStatus
=====================
### scope:public
### version:1.0
modifying the Packing State of Nodes 1.0

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |
| status  | int  | 节点状态 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述                    |
| --- |:----:| ----------------------- |
| N/A | void | 无特定返回值，无错误则表示节点打包状态修改成功 |

cs\_getNodePackingAddress
=========================
### scope:public
### version:1.0
Get the current node's out-of-block address 1.0

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名         |  字段类型  | 参数描述     |
| ----------- |:------:| -------- |
| packAddress | string | 当前节点出块地址 |

cs\_getAgentAddressList
=======================
### scope:public
### version:1.0
Get all node out-of-block addresses/specify N block out-of-block designations

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名         |  字段类型  | 参数描述   |
| ----------- |:------:| ------ |
| packAddress | string | 共识节点列表 |

cs\_getPackerInfo
=================
### scope:public
### version:1.0
modifying the Packing State of Nodes 1.0

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名             |      字段类型       | 参数描述     |
| --------------- |:---------------:| -------- |
| address         |     string      | 当前节点出块地址 |
| password        |     string      | 当前节点密码   |
| packAddressList | list&lt;string> | 当前打包地址列表 |

cs\_getAgentList
================
### scope:public
### version:1.0
query consensus node list 1.0

参数列表
----
| 参数名        |  参数类型  | 参数描述 | 是否非空 |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | 链id  |  是   |
| pageNumber |  int   | 页码   |  是   |
| pageSize   |  int   | 每页数量 |  是   |
| keyWord    | string | 关键字  |  是   |

返回值
---
| 字段名            |  字段类型  | 参数描述       |
| -------------- |:------:| ---------- |
| agentHash      | string | 节点HASH     |
| agentAddress   | string | 节点地址       |
| packingAddress | string | 节点出块地址     |
| rewardAddress  | string | 节点奖励地址     |
| deposit        | string | 抵押金额       |
| commissionRate |  byte  | 佣金比例       |
| agentName      | string | 节点名称       |
| agentId        | string | 节点ID       |
| introduction   | string | 节点简介       |
| time           |  long  | 节点创建时间     |
| blockHeight    |  long  | 节点打包高度     |
| delHeight      |  long  | 节点失效高度     |
| status         |  int   | 状态         |
| creditVal      | double | 信誉值        |
| totalDeposit   | string | 总委托金额      |
| txHash         | string | 创建节点交易HASH |
| memberCount    |  int   | 委托人数       |
| version        | string | 版本         |

RegisterAPI
===========
### scope:public
### version:1.0
Register API

参数列表
----
无参数

返回值
---
无返回值

cs\_getContractDepositInfo
==========================
### scope:public
### version:1.0
contract get deposit info

参数列表
----
| 参数名             |  参数类型  | 参数描述    | 是否非空 |
| --------------- |:------:| ------- |:----:|
| chainId         |  int   | 链id     |  是   |
| joinAgentHash   | string | 节点HASH  |  是   |
| contractAddress |  int   | 合约地址    |  是   |
| contractSender  |  int   | 合约调用者地址 |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述 |
| ---- |:---------------:| ---- |
| null | list&lt;string> | 委托信息 |

cs\_triggerCoinBaseContract
===========================
### scope:public
### version:1.0
trigger coin base contract

参数列表
----
| 参数名         |  参数类型  | 参数描述      | 是否非空 |
| ----------- |:------:| --------- |:----:|
| chainId     |  int   | 链id       |  是   |
| tx          | string | 交易信息      |  是   |
| blockHeader | string | 区块头       |  是   |
| stateRoot   | string | stateRoot |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述      |
| ----- |:------:| --------- |
| value | string | stateRoot |

cs\_stopContractAgent
=====================
### scope:public
### version:1.0
contract stop agent 1.0

参数列表
----
| 参数名             | 参数类型 | 参数描述          | 是否非空 |
| --------------- |:----:| ------------- |:----:|
| chainId         | int  | 链id           |  是   |
| contractAddress | int  | 合约地址          |  是   |
| contractSender  | int  | 合约调用者地址       |  是   |
| contractBalance | int  | 合约地址的当前余额     |  是   |
| contractNonce   | int  | 合约地址的当前nonce值 |  是   |
| blockTime       | int  | 当前打包的区块时间     |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述        |
| ---- |:---------------:| ----------- |
| null | list&lt;string> | 返回交易HASH和交易 |

cs\_contractDeposit
===================
### scope:public
### version:1.0
contract deposit agent transaction 1.0

参数列表
----
| 参数名             |  参数类型  | 参数描述          | 是否非空 |
| --------------- |:------:| ------------- |:----:|
| chainId         |  int   | 链id           |  是   |
| agentHash       | string | 委托的节点HASH     |  是   |
| deposit         | string | 委托金额          |  是   |
| contractAddress |  int   | 合约地址          |  是   |
| contractSender  |  int   | 合约调用者地址       |  是   |
| contractBalance |  int   | 合约地址的当前余额     |  是   |
| contractNonce   |  int   | 合约地址的当前nonce值 |  是   |
| blockTime       |  int   | 当前打包的区块时间     |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述        |
| ---- |:---------------:| ----------- |
| null | list&lt;string> | 返回交易HASH和交易 |

cs\_contractWithdraw
====================
### scope:public
### version:1.0
contract withdraw deposit agent transaction 1.0

参数列表
----
| 参数名             |  参数类型  | 参数描述          | 是否非空 |
| --------------- |:------:| ------------- |:----:|
| chainId         |  int   | 链id           |  是   |
| joinAgentHash   | string | 节点HASH        |  是   |
| contractAddress |  int   | 合约地址          |  是   |
| contractSender  |  int   | 合约调用者地址       |  是   |
| contractBalance |  int   | 合约地址的当前余额     |  是   |
| contractNonce   |  int   | 合约地址的当前nonce值 |  是   |
| blockTime       |  int   | 当前打包的区块时间     |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述        |
| ---- |:---------------:| ----------- |
| null | list&lt;string> | 返回交易HASH和交易 |

cs\_createContractAgent
=======================
### scope:public
### version:1.0
contract create agent 1.0

参数列表
----
| 参数名             |  参数类型  | 参数描述          | 是否非空 |
| --------------- |:------:| ------------- |:----:|
| chainId         |  int   | 链id           |  是   |
| packingAddress  | string | 出块地址          |  是   |
| deposit         | string | 抵押金额          |  是   |
| commissionRate  | string | 佣金比例          |  是   |
| contractAddress |  int   | 合约地址          |  是   |
| contractSender  |  int   | 合约调用者地址       |  是   |
| contractBalance |  int   | 合约地址的当前余额     |  是   |
| contractNonce   |  int   | 合约地址的当前nonce值 |  是   |
| blockTime       |  int   | 当前打包的区块时间     |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述        |
| ---- |:---------------:| ----------- |
| null | list&lt;string> | 返回交易HASH和交易 |

cs\_getContractAgentInfo
========================
### scope:public
### version:1.0
contract get agent info

参数列表
----
| 参数名             |  参数类型  | 参数描述    | 是否非空 |
| --------------- |:------:| ------- |:----:|
| chainId         |  int   | 链id     |  是   |
| agentHash       | string | 节点HASH  |  是   |
| contractAddress |  int   | 合约地址    |  是   |
| contractSender  |  int   | 合约调用者地址 |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述 |
| ---- |:---------------:| ---- |
| null | list&lt;string> | 节点信息 |

connectReady
============
### scope:private
### version:1.0
check module rpc is ready

参数列表
----
无参数

返回值
---
无返回值

listenerDependenciesReady
=========================
### scope:private
### version:1.0
notify module is ready

参数列表
----
无参数

返回值
---
无返回值

registerModuleDependencies
==========================
### scope:private
### version:1.0
Register module followerList

参数列表
----
无参数

返回值
---
无返回值

cs\_getDepositList
==================
### scope:public
### version:1.0
query delegation information list 1.0

参数列表
----
| 参数名        |  参数类型  | 参数描述   | 是否非空 |
| ---------- |:------:| ------ |:----:|
| chainId    |  int   | 链id    |  是   |
| pageNumber |  int   | 页码     |  是   |
| pageSize   |  int   | 每页数量   |  是   |
| address    | string | 账户地址   |  是   |
| agentHash  | string | 节点HASH |  是   |

返回值
---
| 字段名          |  字段类型  | 参数描述              |
| ------------ |:------:| ----------------- |
| deposit      | string | 委托金额              |
| agentHash    | string | 节点HASH            |
| address      | string | 账户地址              |
| time         |  long  | 委托时间              |
| txHash       | string | 委托交易HASH          |
| blockHeight  |  long  | 委托交易被打包高度         |
| delHeight    |  long  | 退出委托高度            |
| status       |  int   | 节点状态 0:待共识, 1:已共识 |
| agentName    | string | 节点名称              |
| agentAddress | string | 节点地址              |

cs\_depositToAgent
==================
### scope:public
### version:1.0
deposit agent transaction 1.0

参数列表
----
| 参数名       |  参数类型  | 参数描述   | 是否非空 |
| --------- |:------:| ------ |:----:|
| chainId   |  int   | 链id    |  是   |
| address   | string | 账户地址   |  是   |
| agentHash | string | 节点HASH |  是   |
| deposit   | string | 委托金额   |  是   |
| password  | string | 账户密码   |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述       |
| ------ |:------:| ---------- |
| txHash | string | 加入共识交易Hash |

cs\_withdraw
============
### scope:public
### version:1.0
withdraw deposit agent transaction 1.0

参数列表
----
| 参数名      |  参数类型  | 参数描述       | 是否非空 |
| -------- |:------:| ---------- |:----:|
| chainId  |  int   | 链id        |  是   |
| address  | string | 账户地址       |  是   |
| txHash   | string | 加入共识交易HASH |  是   |
| password | string | 账户密码       |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述       |
| ------ |:------:| ---------- |
| txHash | string | 退出共识交易Hash |

paramTestCmd
============
### scope:public
### version:1.0


参数列表
----
| 参数名        | 参数类型  | 参数描述 | 是否非空 |
| ---------- |:-----:| ---- |:----:|
| intCount   |  int  |      |  是   |
| byteCount  | byte  |      |  是   |
| shortCount | short |      |  是   |
| longCount  | long  |      |  是   |

返回值
---
无返回值

cs\_chainRollBack
=================
### scope:public
### version:1.0
chain roll back 1.0

参数列表
----
| 参数名     | 参数类型 | 参数描述     | 是否非空 |
| ------- |:----:| -------- |:----:|
| chainId | int  | 链id      |  是   |
| height  | int  | 区块回滚到的高度 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 区块回滚结果 |

cs\_addBlock
============
### scope:public
### version:1.0
add block 1.0

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链id  |  是   |
| blockHeader | string | 区块头  |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述     |
| ----- |:-------:| -------- |
| value | boolean | 接口执行成功与否 |

cs\_receiveHeaderList
=====================
### scope:public
### version:1.0
verify block correctness 1.0

参数列表
----
| 参数名        |     参数类型     | 参数描述  | 是否非空 |
| ---------- |:------------:| ----- |:----:|
| chainId    |     int      | 链id   |  是   |
| headerList | list<string> | 区块头列表 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述     |
| ----- |:-------:| -------- |
| value | boolean | 是否成功接收处理 |

cs\_validBlock
==============
### scope:public
### version:1.0
verify block correctness 1.0

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| download |  int   | 区块状态 |  是   |
| block    | string | 区块信息 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 验证结果 |

