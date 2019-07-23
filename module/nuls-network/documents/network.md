## 1 . 模块概述

### 1. 1 为什么要有《网络模块》

网络模块保障了去中心化节点间的通讯，为NULS基础模块之一，提供最底层的网络通信、节点发现等服务。区块链的网络基础就是Peer to  Peer,即P2P。P2P网络中的所有参与者，可以是提供服务（server），也可以是资源使用者（client）。P2P网络的特点：去中心化、可扩展性、健壮性、高性价比、隐私保护、负载均衡。

### 1.2 《网络模块》要做什么

网络模块是整个系统的基础模块，用来管理节点、节点间的连接及连接的状态、数据的发送与接收。网络模块不涉及复杂的业务逻辑。

* 接收到的网络消息，根据内核模块中的指令服务映射关系，推送消息相应的处理模块中。

* 开放接口供其他模块封装好的消息调用推送到指定的peer节点以及广播到指定的网络组中。

### 1.3 《网络模块》在系统中的定位

* 网络模块是底层应用模块，任何需要网络通讯的模块都要通过网络模块来进行消息的收发。
* 网络模块依赖核心模块进行服务接口的治理。
* 网络模块按网络id（魔法参数） 来进行不同网络的构建。
* 网络模块在卫星链中的节点在进行跨链网络组建时，需要链管理模块提供跨链配置信息。
* 网络模块在子链中的节点在进行跨链网络组建时，需要跨链模块提供跨链配置信息。



## 2 . 模块配置

```
#本链服务端口
port=18001
#跨链服务端口
crossPort=18002
#魔法参数
packetMagic=55886633
#种子连接节点
selfSeedIps=192.168.1.12:18001
#最大入网连接数
maxInCount=100
#最大出网连接数
maxOutCount=20
```


nw\_info
========
### scope:public
### version:1.0
获取节点网络基本信息

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 连接的链Id,取值区间[1-65535] |  是   |

返回值
---
| 字段名             |  字段类型   | 参数描述                 |
| --------------- |:-------:| -------------------- |
| localBestHeight |  long   | 本地节点区块高度             |
| netBestHeight   |  long   | 网络节点区块最高高度           |
| timeOffset      |  long   | 节点与网络时间相差值           |
| inCount         | integer | 最为Server,peer接入数量    |
| outCount        | integer | 作为client连接外部Server数量 |

nw\_nodes
=========
### scope:public
### version:1.0
获取网络连接节点信息

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 连接的链Id,取值区间[1-65535] |  是   |

返回值
---
| 字段名         |  字段类型  | 参数描述     |
| ----------- |:------:| -------- |
| peer        | string | peer节点ID |
| blockHeight |  long  | 节点高度     |
| blockHash   | string | 节点Hash   |

nw\_currentTimeMillis
=====================
### scope:public
### version:1.0
获取节点网络时间

参数列表
----
无参数

返回值
---
| 字段名               | 字段类型 | 参数描述                   |
| ----------------- |:----:| ---------------------- |
| currentTimeMillis | long | 时间毫秒-currentTimeMillis |

nw\_addNodes
============
### scope:public
### version:1.0
增加待连接节点

参数列表
----
| 参数名     |  参数类型  | 参数描述                 | 是否非空 |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | 连接的链Id,取值区间[1-65535] |  是   |
| isCross |  int   | 1跨链连接,0普通连接          |  是   |
| nodes   | string | 节点组ID，逗号拼接           |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

nw\_delNodes
============
### scope:public
### version:1.0
删除节点组节点

参数列表
----
| 参数名     |  参数类型  | 参数描述                 | 是否非空 |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | 连接的链Id,取值区间[1-65535] |  是   |
| nodes   | string | 节点组ID，逗号拼接           |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

nw\_getNodes
============
### scope:public
### version:1.0
分页查看连接节点信息,startPage与pageSize 都为0时，不分页，返回所有节点信息

参数列表
----
| 参数名       |  参数类型   | 参数描述                  | 是否非空 |
| --------- |:-------:| --------------------- |:----:|
| chainId   |   int   | 连接的链Id,取值区间[1-65535]  |  是   |
| state     |   int   | 0:所有连接,1:已连接  2:未连接   |  是   |
| isCross   | boolean | false:非跨链连接，true:跨链连接 |  是   |
| startPage |   int   | 分页起始页数                |  是   |
| pageSize  |   int   | 每页显示数量                |  是   |

返回值
---
| 字段名         |  字段类型  | 参数描述               |
| ----------- |:------:| ------------------ |
| chainId     |  int   | 链ID                |
| nodeId      | string | 节点ID               |
| magicNumber |  long  | 网络魔法参数             |
| blockHeight |  long  | peer节点区块高度         |
| blockHash   | string | peer最新区块hash       |
| ip          | string | peer连接IP地址         |
| port        |  int   | peer连接端口号          |
| state       |  int   | 0:未完成握手 1:已完成握手的连接 |
| isOut       |  int   | 0:入网连接 1:出网连接      |
| time        |  long  | 连接时间毫秒             |

nw\_updateNodeInfo
==================
### scope:public
### version:1.0
更新连接节点信息

参数列表
----
| 参数名         |  参数类型  | 参数描述                 | 是否非空 |
| ----------- |:------:| -------------------- |:----:|
| chainId     |  int   | 连接的链Id,取值区间[1-65535] |  是   |
| nodeId      | string | 连接节点ID               |  是   |
| blockHeight |  long  | 区块高度                 |  是   |
| blockHash   | string | 区块hash值              |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

nw\_broadcast
=============
### scope:public
### version:1.0
广播消息

参数列表
----
| 参数名          |  参数类型   | 参数描述                 | 是否非空 |
| ------------ |:-------:| -------------------- |:----:|
| chainId      |   int   | 连接的链Id,取值区间[1-65535] |  是   |
| excludeNodes | string  | 排除peer节点Id，用逗号分割     |  是   |
| messageBody  | string  | 消息体Hex               |  是   |
| command      | string  | 消息协议指令               |  是   |
| isCross      | boolean | 是否是跨链                |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述               |
| ----- |:-------:| ------------------ |
| value | boolean | 一个节点都没发送出去时返回false |

nw\_protocolRegister
====================
### scope:public
### version:1.0
模块协议指令注册

参数列表
----
| 参数名          |      参数类型       | 参数描述   | 是否非空 |
| ------------ |:---------------:| ------ |:----:|
| role         |     string      | 模块角色名称 |  是   |
| protocolCmds | list&lt;string> | 注册指令列表 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

nw\_sendPeersMsg
================
### scope:public
### version:1.0
向指定节点发送消息

参数列表
----
| 参数名         |  参数类型  | 参数描述                   | 是否非空 |
| ----------- |:------:| ---------------------- |:----:|
| chainId     |  int   | 连接的链Id,取值区间[1-65535]   |  是   |
| nodes       | string | 指定发送peer节点Id，用逗号拼接的字符串 |  是   |
| messageBody | string | 消息体Hex                 |  是   |
| command     | string | 消息协议指令                 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

nw\_createNodeGroup
===================
### scope:public
### version:1.0
主网创建跨链网络或者链工厂创建链

参数列表
----
| 参数名               |  参数类型   | 参数描述                           | 是否非空 |
| ----------------- |:-------:| ------------------------------ |:----:|
| chainId           |   int   | 连接的链Id,取值区间[1-65535]           |  是   |
| magicNumber       |  long   | 网络魔法参数                         |  是   |
| maxOut            |   int   | 作为client主动对外最大连接数              |  是   |
| maxIn             |   int   | 作为sever允许外部最大连接数               |  是   |
| minAvailableCount |   int   | 最小有效连接数                        |  是   |
| isCrossGroup      | boolean | 是否创建跨链连接组:true 跨链连接，false 普通连接 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

nw\_activeCross
===============
### scope:public
### version:1.0
跨链协议模块激活跨链

参数列表
----
| 参数名     |  参数类型  | 参数描述                 | 是否非空 |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | 连接的链Id,取值区间[1-65535] |  是   |
| maxOut  | string | 作为client主动对外最大连接数    |  是   |
| maxIn   |  int   | 作为sever允许外部最大连接数     |  是   |
| seedIps | string | 种子连接节点ID,用逗号拼接       |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

nw\_getGroupByChainId
=====================
### scope:public
### version:1.0
获取节点组信息

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 连接的链Id,取值区间[1-65535] |  是   |

返回值
---
| 字段名                  | 字段类型 | 参数描述        |
| -------------------- |:----:| ----------- |
| chainId              | int  | 链ID         |
| magicNumber          | long | 网络魔法参数      |
| totalCount           | int  | 总连接数        |
| connectCount         | int  | 本地网络已连接节点数  |
| disConnectCount      | int  | 本地网络待接节点数   |
| inCount              | int  | 本地网络入网连接节点数 |
| outCount             | int  | 本地网络出网连接节点数 |
| connectCrossCount    | int  | 跨链网络连接节点数   |
| disConnectCrossCount | int  | 跨链网络待接节点数   |
| inCrossCount         | int  | 跨链网络入网节点数   |
| outCrossCount        | int  | 跨链网络出网节点数   |
| isActive             | int  | 本地网络是否已工作   |
| isCrossActive        | int  | 跨链网络是否已工作   |
| isMoonNet            | int  | 网络组是否是卫星链节点 |

nw\_getChainConnectAmount
=========================
### scope:public
### version:1.0
获取指定网络组可连接数量

参数列表
----
| 参数名     |  参数类型   | 参数描述                      | 是否非空 |
| ------- |:-------:| ------------------------- |:----:|
| chainId |   int   | 连接的链Id,取值区间[1-65535]      |  是   |
| isCross | boolean | true，获取跨链连接数，false本地网络连接数 |  是   |

返回值
---
| 字段名           |  字段类型   | 参数描述 |
| ------------- |:-------:| ---- |
| connectAmount | integer | 可连接数 |

nw\_delNodeGroup
================
### scope:public
### version:1.0
删除指定网络组

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 连接的链Id,取值区间[1-65535] |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

nw\_getSeeds
============
### scope:public
### version:1.0
查看跨链网络提供的种子节点

参数列表
----
无参数

返回值
---
| 字段名      |  字段类型  | 参数描述                |
| -------- |:------:| ------------------- |
| seedsIps | string | 主网可连接的种子节点ID，逗号进行拼接 |

nw\_getMainMagicNumber
======================
### scope:public
### version:1.0
查看主网的魔法参数

参数列表
----
无参数

返回值
---
| 字段名   | 字段类型 | 参数描述   |
| ----- |:----:| ------ |
| value | long | 主网魔法参数 |

nw\_getGroups
=============
### scope:public
### version:1.0
分页获取网络组信息,startPage与pageSize 都为0时，不分页，返回所有网络组信息

参数列表
----
| 参数名       | 参数类型 | 参数描述   | 是否非空 |
| --------- |:----:| ------ |:----:|
| startPage | int  | 开始页数   |  是   |
| pageSize  | int  | 每页展示数量 |  是   |

返回值
---
| 字段名                  | 字段类型 | 参数描述        |
| -------------------- |:----:| ----------- |
| chainId              | int  | 链ID         |
| magicNumber          | long | 网络魔法参数      |
| totalCount           | int  | 总连接数        |
| connectCount         | int  | 本地网络已连接节点数  |
| disConnectCount      | int  | 本地网络待接节点数   |
| inCount              | int  | 本地网络入网连接节点数 |
| outCount             | int  | 本地网络出网连接节点数 |
| connectCrossCount    | int  | 跨链网络连接节点数   |
| disConnectCrossCount | int  | 跨链网络待接节点数   |
| inCrossCount         | int  | 跨链网络入网节点数   |
| outCrossCount        | int  | 跨链网络出网节点数   |
| isActive             | int  | 本地网络是否已工作   |
| isCrossActive        | int  | 跨链网络是否已工作   |
| isMoonNet            | int  | 网络组是否是卫星链节点 |

nw\_reconnect
=============
### scope:public
### version:1.0
本地网络重启

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 组网的链Id,取值区间[1-65535] |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

