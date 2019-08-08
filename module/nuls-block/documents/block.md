# 模块概述

## 为什么要有《区块管理》模块

​区块链上所有交易数据都保存在区块中，所以要有一个模块负责区块的存储与管理，以便其他模块对区块中数据进行验证、业务处理时可以获取到区块。

​区块链程序初次启动时，需要同步网络上的最新区块到本地，这个过程一般耗时较长，且同步未完成时不能发起交易，所以适合由单独模块来完成该工作。

​综上所述，为其他模块提供统一的区块数据服务是必要的，也能更好地把区块的管理与区块的具体业务进行分离，用到区块的模块不必关心区块的获取细节。

## 《区块管理》要做什么

- 提供api，进行区块存储、查询、回滚的操作
- 从网络上同步最新区块，验证通过后保存
- 区块同步、广播、转发消息的处理
- 分叉区块的判断、存储
- 孤儿区块的判断、存储
- 分叉链维护、切换
- 孤儿链维护、切换

# 模块运行环境

- jdk: 11
- ide: IntelliJ IDEA 2018.3.3 (Community Edition)
- maven: 3.3.9

# 常见日志分析

|日志内容															|日志生成原因|
|----|----|
|Skip block syn|				把minNodeAmount设置为0,并且没有链接到任何可用节点时,直接跳过区块同步流程(一般用于运行单节点网络)|
|There are no consistent nodes							|				连接到的节点高度或最新区块hash不一致|
|This blockchain just started running			|				连接到的节点高度都是0,表示这条链刚开始运行|
|The local node's block is the latest height	|				本地节点的区块已经是最新的,不需要进行区块同步|
|The number of rolled back blocks exceeded the configured value|	本地区块与网络上的区块不一致,本地区块回滚,但是回滚数量已经超过阈值,停止回滚|
|The local genesis block is different from networks		|				本地节点的创世区块hash与网络上的创世区块hash不匹配,需要检查本地创世块配置或创世块路径|
|wait until network stable						|				连接到的可用节点数量不足,检查minNodeAmount这个配置项,以及网络模块配置、日志|
|BlockDownloader start work						|				区块下载线程开始工作,记录了本次下载的起始高度与结束高度,以及同步节点|
|BlockDownloader wait!						|				区块下载线程暂停工作,因为缓存的区块字节数已经超过配置的阈值|
|Block syn complete successfully	|				区块同步成功,且已经同步到最新区块|
|Block syn complete but another syn is needed			|				区块同步成功,但还不是最新区块,需要进行再次同步|
|Block syn fail			|				区块同步失败,downResult标识下载是否成功,storageResult标识保存过程是否正常|
|An exception occurred while saving the downloaded block	|				保存区块失败,重点检查共识模块,交易模块,账本模块的日志|
|retryDownload	|				区块同步超时,启动重试下载机制|

# 常用配置说明

## 创世区块

配置文件路径：[genesis-block.json](./src/main/resources/genesis-block.json)

## 系统参数

配置文件路径：[module.json](./src/main/resources/module.json)

### 配置项说明[^1]
|配置项															|说明|
|----|----|
|forkChainsMonitorInterval|分叉链监视线程运行间隔|
|orphanChainsMonitorInterval|孤儿链监视线程运行间隔|
|orphanChainsMaintainerInterval|孤儿链维护线程运行间隔|
|storageSizeMonitorInterval|缓存数据库容量监视线程运行间隔|
|networkResetMonitorInterval|网络监视线程运行间隔|
|nodesMonitorInterval|节点数量监视线程运行间隔|
|txGroupRequestorInterval|TxGroup获取线程运行间隔|
|txGroupTaskDelay|分叉链监视线程运行间隔|
|testAutoRollbackAmount|启动后自动回滚的区块数量,仅供测试区块回滚用,正式网络下设置为0|
|blockMaxSize|区块最大字节数|
|extendMaxSize|区块扩展字段最大字节数|
|resetTime|本地区块高度不更新时,引发重置网络动作的时间间隔|
|chainSwtichThreshold|引发分叉链切换的高度差阈值|
|cacheSize|分叉链、孤儿链区块最大缓存数量|
|heightRange|接收新区块的范围|
|maxRollback|本地区块与网络区块不一致时,本地最大回滚数|
|consistencyNodePercent|统计网络上的节点最新区块高度、hash一致的百分比阈值|
|minNodeAmount|最小链接节点数,当链接到的网络节点低于此参数时,会持续等待|
|downloadNumber|区块同步过程中,每次从网络上节点下载的区块数量|
|validBlockInterval|为阻止恶意节点提前出块,设置此参数,区块时间戳大于当前时间多少就丢弃该区块|
|cacheSize|同步区块时最多缓存多少个区块|
|smallBlockCache|系统正常运行时最多缓存多少个从别的节点接收到的小区块|
|orphanChainMaxAge|孤儿链维护失败时,年龄加一,此参数是孤儿链能达到的最大年龄,高于这个值会被清理线程清理|
|singleDownloadTimeout|从网络节点下载单个区块的超时时间|
|waitNetworkInterval|等待网络稳定的时间间隔|
|genesisBlockPath|创世区块文件路径|
    
## 协议信息

配置文件路径：[protocol-config.json](./src/main/resources/protocol-config.json)


[^1]:配置文件中所有时间参数单位都是毫秒
info
====
### scope:public
### version:1.0
returns network node height and local node height

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

返回值
---
| 字段名           | 字段类型 | 参数描述       |
| ------------- |:----:| ---------- |
| networkHeight | long | 网络节点最新区块高度 |
| localHeight   | long | 本地节点最新区块高度 |

latestBlock
===========
### scope:public
### version:1.0
the latest block of master chain

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述              |
| --- |:------:| ----------------- |
| 返回值 | string | 返回一个区块序列化后的HEX字符串 |

downloadBlockByHash
===================
### scope:public
### version:1.0
get a block by hash

参数列表
----
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| hash    | string | 区块hash |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述            |
| --- |:------:| --------------- |
| 返回值 | string | 返回区块序列化后的HEX字符串 |

latestHeight
============
### scope:public
### version:1.0
the latest height of master chain

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

返回值
---
| 字段名   | 字段类型 | 参数描述   |
| ----- |:----:| ------ |
| value | long | 最新主链高度 |

latestBlockHeader
=================
### scope:public
### version:1.0
the latest block header of master chain

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述               |
| --- |:------:| ------------------ |
| 返回值 | string | 返回一个区块头序列化后的HEX字符串 |

latestBlockHeaderPo
===================
### scope:public
### version:1.0
the latest block header po of master chain

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述                 |
| --- |:------:| -------------------- |
| 返回值 | string | 返回一个区块头PO序列化后的HEX字符串 |

getBlockHeaderByHeight
======================
### scope:public
### version:1.0
get a block header by height

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| height  | long | 区块高度 |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述               |
| --- |:------:| ------------------ |
| 返回值 | string | 返回一个区块头序列化后的HEX字符串 |

getBlockHeaderPoByHeight
========================
### scope:public
### version:1.0
get a block header po by height

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| height  | long | 区块高度 |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述                 |
| --- |:------:| -------------------- |
| 返回值 | string | 返回一个区块头PO序列化后的HEX字符串 |

getBlockByHeight
================
### scope:public
### version:1.0
get a block by height

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| height  | long | 区块高度 |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述                |
| --- |:------:| ------------------- |
| 返回值 | string | 返回区块序列化后的HEX字符串List |

getBlockHeaderByHash
====================
### scope:public
### version:1.0
get a block header by hash

参数列表
----
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| hash    | string | 区块hash |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述             |
| --- |:------:| ---------------- |
| 返回值 | string | 返回区块头序列化后的HEX字符串 |

getBlockHeaderPoByHash
======================
### scope:public
### version:1.0
get a block header po by hash

参数列表
----
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| hash    | string | 区块hash |  是   |

返回值
---
| 字段名 |  字段类型  | 参数描述               |
| --- |:------:| ------------------ |
| 返回值 | string | 返回区块头PO序列化后的HEX字符串 |

getLatestBlockHeaders
=====================
### scope:public
### version:1.0
get the latest number of block headers

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| size    | int  | 数量   |  是   |

返回值
---
| 字段名 |      字段类型       | 参数描述                 |
| --- |:---------------:| -------------------- |
| 返回值 | list&lt;string> | 返回区块头序列化后的HEX字符串List |

getLatestRoundBlockHeaders
==========================
### scope:public
### version:1.0
get the latest several rounds of block headers

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| round   | int  | 共识轮次 |  是   |

返回值
---
| 字段名 |      字段类型       | 参数描述                 |
| --- |:---------------:| -------------------- |
| 返回值 | list&lt;string> | 返回区块头序列化后的HEX字符串List |

getRoundBlockHeaders
====================
### scope:public
### version:1.0
get the latest several rounds of block headers

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| height  | long | 起始高度 |  是   |
| round   | int  | 共识轮次 |  是   |

返回值
---
| 字段名 |      字段类型       | 参数描述                 |
| --- |:---------------:| -------------------- |
| 返回值 | list&lt;string> | 返回区块头序列化后的HEX字符串List |

receivePackingBlock
===================
### scope:public
### version:1.0
receive the new packaged block

参数列表
----
| 参数名     |  参数类型  | 参数描述          | 是否非空 |
| ------- |:------:| ------------- |:----:|
| chainId |  int   | 链ID           |  是   |
| block   | string | 区块序列化后的HEX字符串 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述 |
| --- |:----:| ---- |
| N/A | void | 无返回值 |

getBlockHeadersByHeightRange
============================
### scope:public
### version:1.0
get the block headers according to the height range

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| begin   | long | 起始高度 |  是   |
| end     | long | 结束高度 |  是   |

返回值
---
| 字段名 |      字段类型       | 参数描述                 |
| --- |:---------------:| -------------------- |
| 返回值 | list&lt;string> | 返回区块头序列化后的HEX字符串List |

getBlockHeadersForProtocol
==========================
### scope:public
### version:1.0
get block headers for protocol upgrade module

参数列表
----
| 参数名      | 参数类型 | 参数描述     | 是否非空 |
| -------- |:----:| -------- |:----:|
| chainId  | int  | 链ID      |  是   |
| interval | int  | 协议升级统计区间 |  是   |

返回值
---
| 字段名 |      字段类型       | 参数描述                 |
| --- |:---------------:| -------------------- |
| 返回值 | list&lt;string> | 返回区块头序列化后的HEX字符串List |

