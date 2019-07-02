# account ledger module

blockValidate
=============
### scope:public
### version:1.0
整区块入账校验

参数列表
----
| 参数名         | 参数类型 | 参数描述                 | 是否非空 |
| ----------- |:----:| -------------------- |:----:|
| chainId     | int  | 运行的链Id,取值区间[1-65535] |  是   |
| txList      | list | []交易Hex值列表           |  是   |
| blockHeight | long | 区块高度                 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述               |
| ----- |:-------:| ------------------ |
| value | boolean | true处理成功，false处理失败 |

verifyCoinData
==============
### scope:public
### version:1.0
未确认交易校验

参数列表
----
| 参数名     |  参数类型  | 参数描述                 | 是否非空 |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | 运行的链Id,取值区间[1-65535] |  是   |
| tx      | string | 交易Hex值               |  是   |

返回值
---
| 字段名    |  字段类型   | 参数描述            |
| ------ |:-------:| --------------- |
| orphan | boolean | true孤儿，false非孤儿 |

rollbackTxValidateStatus
========================
### scope:public
### version:1.0
回滚打包校验状态

参数列表
----
| 参数名     |  参数类型  | 参数描述                 | 是否非空 |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | 运行的链Id,取值区间[1-65535] |  是   |
| tx      | string | 交易Hex值               |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述               |
| ----- |:-------:| ------------------ |
| value | boolean | true回滚成功，false回滚失败 |

verifyCoinDataBatchPackaged
===========================
### scope:public
### version:1.0
打包交易校验

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 运行的链Id,取值区间[1-65535] |  是   |
| txList  | list | []交易列表（HEX值列表）       |  是   |

返回值
---
| 字段名     |      字段类型       | 参数描述          |
| ------- |:---------------:| ------------- |
| fail    | list&lt;string> | 校验失败Hash值列表   |
| orphan  | list&lt;string> | 校验为孤儿的Hash值列表 |
| success | list&lt;string> | 校验成功的Hash值列表  |

batchValidateBegin
==================
### scope:public
### version:1.0
开始批量打包:状态通知

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 运行的链Id,取值区间[1-65535] |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述               |
| ----- |:-------:| ------------------ |
| value | boolean | true处理成功，false处理失败 |

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

getBlockHeight
==============
### scope:public
### version:1.0


参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  |      |  是   |

返回值
---
无返回值

getAssetsByChainId
==================
### scope:public
### version:1.0


参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  |      |  是   |

返回值
---
无返回值

getSnapshot
===========
### scope:public
### version:1.0


参数列表
----
| 参数名         | 参数类型 | 参数描述 | 是否非空 |
| ----------- |:----:| ---- |:----:|
| chainId     | int  |      |  是   |
| blockHeight | long |      |  是   |

返回值
---
无返回值

commitUnconfirmedTx
===================
### scope:public
### version:1.0
未确认交易提交账本(校验并更新nonce值)

参数列表
----
| 参数名     |  参数类型  | 参数描述                 | 是否非空 |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | 运行的链Id,取值区间[1-65535] |  是   |
| tx      | string | 交易Hex值               |  是   |

返回值
---
| 字段名    |  字段类型   | 参数描述                  |
| ------ |:-------:| --------------------- |
| orphan | boolean | true 孤儿交易，false 非孤儿交易 |

commitBatchUnconfirmedTxs
=========================
### scope:public
### version:1.0
未确认交易批量提交账本(校验并更新nonce值)

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 运行的链Id,取值区间[1-65535] |  是   |
| txList  | list | []交易Hex值列表           |  是   |

返回值
---
| 字段名    |      字段类型       | 参数描述         |
| ------ |:---------------:| ------------ |
| orphan | list&lt;string> | 孤儿交易Hash列表   |
| fail   | list&lt;string> | 校验失败交易Hash列表 |

rollBackUnconfirmTx
===================
### scope:public
### version:1.0
回滚提交的未确认交易

参数列表
----
| 参数名     |  参数类型  | 参数描述                 | 是否非空 |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | 运行的链Id,取值区间[1-65535] |  是   |
| tx      | string | 交易Hex值               |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述             |
| ----- |:-------:| ---------------- |
| value | boolean | true 成功，false 失败 |

clearUnconfirmTxs
=================
### scope:public
### version:1.0
清除所有账户未确认交易

参数列表
----
| 参数名     | 参数类型 | 参数描述                 | 是否非空 |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | 运行的链Id,取值区间[1-65535] |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述             |
| ----- |:-------:| ---------------- |
| value | boolean | true 成功，false 失败 |

commitBlockTxs
==============
### scope:public
### version:1.0
提交区块

参数列表
----
| 参数名         | 参数类型 | 参数描述                 | 是否非空 |
| ----------- |:----:| -------------------- |:----:|
| chainId     | int  | 运行的链Id,取值区间[1-65535] |  是   |
| txList      | list | 交易Hex值列表             |  是   |
| blockHeight | long | 区块高度                 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述             |
| ----- |:-------:| ---------------- |
| value | boolean | true 成功，false 失败 |

rollBackBlockTxs
================
### scope:public
### version:1.0
区块回滚

参数列表
----
| 参数名         | 参数类型 | 参数描述                 | 是否非空 |
| ----------- |:----:| -------------------- |:----:|
| chainId     | int  | 运行的链Id,取值区间[1-65535] |  是   |
| txList      | list | []交易Hex值列表           |  是   |
| blockHeight | long | 区块高度                 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述             |
| ----- |:-------:| ---------------- |
| value | boolean | true 成功，false 失败 |

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

getNonce
========
### scope:public
### version:1.0
获取账户资产NONCE值

参数列表
----
| 参数名          |  参数类型  | 参数描述                 | 是否非空 |
| ------------ |:------:| -------------------- |:----:|
| chainId      |  int   | 运行的链Id,取值区间[1-65535] |  是   |
| assetChainId |  int   | 资产链Id,取值区间[1-65535]  |  是   |
| assetId      |  int   | 资产Id,取值区间[1-65535]   |  是   |
| address      | string | 资产所在地址               |  是   |

返回值
---
| 字段名       |  字段类型   | 参数描述                      |
| --------- |:-------:| ------------------------- |
| nonce     | string  | 账户资产nonce值                |
| nonceType | integer | 1：已确认的nonce值,0：未确认的nonce值 |

getBalanceNonce
===============
### scope:public
### version:1.0
获取账户资产余额与NONCE值

参数列表
----
| 参数名          |  参数类型  | 参数描述                 | 是否非空 |
| ------------ |:------:| -------------------- |:----:|
| chainId      |  int   | 运行的链Id,取值区间[1-65535] |  是   |
| assetChainId |  int   | 资产链Id,取值区间[1-65535]  |  是   |
| assetId      |  int   | 资产Id,取值区间[1-65535]   |  是   |
| address      | string | 资产所在地址               |  是   |

返回值
---
| 字段名              |    字段类型    | 参数描述                      |
| ---------------- |:----------:| ------------------------- |
| nonce            |   string   | 账户资产nonce值                |
| nonceType        |  integer   | 1：已确认的nonce值,0：未确认的nonce值 |
| available        | biginteger | 可用金额                      |
| permanentLocked  | biginteger | 永久锁定金额                    |
| timeHeightLocked | biginteger | 高度或时间锁定金额                 |

getBalance
==========
### scope:public
### version:1.0
获取账户资产(已入区块)

参数列表
----
| 参数名          |  参数类型  | 参数描述                | 是否非空 |
| ------------ |:------:| ------------------- |:----:|
| chainId      |  int   | 运行链Id,取值区间[1-65535] |  是   |
| assetChainId |  int   | 资产链Id,取值区间[1-65535] |  是   |
| assetId      |  int   | 资产Id,取值区间[1-65535]  |  是   |
| address      | string | 资产所在地址              |  是   |

返回值
---
| 字段名       |    字段类型    | 参数描述 |
| --------- |:----------:| ---- |
| total     | biginteger | 总金额  |
| freeze    | biginteger | 冻结金额 |
| available |   string   | 可用金额 |

getFreezeList
=============
### scope:public
### version:1.0
分页获取账户锁定资产列表

参数列表
----
| 参数名        |  参数类型  | 参数描述                | 是否非空 |
| ---------- |:------:| ------------------- |:----:|
| chainId    |  int   | 资产链Id,取值区间[1-65535] |  是   |
| assetId    |  int   | 资产Id,取值区间[1-65535]  |  是   |
| address    | string | 资产所在地址              |  是   |
| pageNumber |  int   | 起始页数                |  是   |
| pageSize   |  int   | 每页显示数量              |  是   |

返回值
---
| 字段名                                                         |      字段类型       | 参数描述            |
| ----------------------------------------------------------- |:---------------:| --------------- |
| totalCount                                                  |     integer     | 记录总数            |
| pageNumber                                                  |     integer     | 起始页数            |
| pageSize                                                    |     integer     | 每页显示数量          |
| list                                                        | list&lt;object> | 锁定金额列表          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash      |     string      | 交易hash          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount      |   biginteger    | 锁定金额            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockedValue |      long       | 锁定时间或高度，-1为永久锁定 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time        |      long       | 交易产生时间,秒        |

getAssetsById
=============
### scope:public
### version:1.0
清除所有账户未确认交易

参数列表
----
| 参数名      |  参数类型  | 参数描述                 | 是否非空 |
| -------- |:------:| -------------------- |:----:|
| chainId  |  int   | 运行的链Id,取值区间[1-65535] |  是   |
| assetIds | string | 资产id,逗号分隔            |  是   |

返回值
---
| 字段名             |    字段类型    | 参数描述 |
| --------------- |:----------:| ---- |
| assetId         |  integer   | 资产id |
| availableAmount | biginteger | 可用金额 |
| freeze          | biginteger | 冻结金额 |

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

