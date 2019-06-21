模块说明

```
这个文件夹下才开始放置代码，可以是任意语言开发的代码
```

txValidator
===========
### scope:public
### version:1.0


参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   |      |  是   |
| txList      |  list  |      |  是   |
| blockHeader | string |      |  是   |

返回值
---
无返回值

txCommit
========
### scope:public
### version:1.0


参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   |      |  是   |
| txList      |  list  |      |  是   |
| blockHeader | string |      |  是   |

返回值
---
无返回值

txRollback
==========
### scope:public
### version:1.0


参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   |      |  是   |
| txList      |  list  |      |  是   |
| blockHeader | string |      |  是   |

返回值
---
无返回值

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

protocolVersionChange
=====================
### scope:private
### version:1.0


参数列表
----
| 参数名             | 参数类型  | 参数描述 | 是否非空 |
| --------------- |:-----:| ---- |:----:|
| chainId         |  int  |      |  是   |
| protocolVersion | short |      |  是   |

返回值
---
无返回值

sc_invoke_contract
==================
### scope:public
### version:1.0
invoke contract one by one

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   |      |  是   |
| tx      | string |      |  是   |

返回值
---
无返回值

sc_batch_begin
==============
### scope:public
### version:1.0
batch begin

参数列表
----
| 参数名            |  参数类型  | 参数描述 | 是否非空 |
| -------------- |:------:| ---- |:----:|
| chainId        |  int   |      |  是   |
| blockHeight    |  long  |      |  是   |
| blockTime      |  long  |      |  是   |
| packingAddress | string |      |  是   |
| preStateRoot   | string |      |  是   |

返回值
---
无返回值

sc_batch_end
============
### scope:public
### version:1.0
batch end

参数列表
----
| 参数名         | 参数类型 | 参数描述 | 是否非空 |
| ----------- |:----:| ---- |:----:|
| chainId     | int  |      |  是   |
| blockHeight | long |      |  是   |

返回值
---
无返回值

sc_batch_before_end
===================
### scope:public
### version:1.0
batch before end

参数列表
----
| 参数名         | 参数类型 | 参数描述 | 是否非空 |
| ----------- |:----:| ---- |:----:|
| chainId     | int  |      |  是   |
| blockHeight | long |      |  是   |

返回值
---
无返回值

sc_contract_offline_tx_hash_list
================================
### scope:public
### version:1.0
contract offline tx hash list

参数列表
----
| 参数名       |  参数类型  | 参数描述 | 是否非空 |
| --------- |:------:| ---- |:----:|
| chainId   |  int   |      |  是   |
| blockHash | string |      |  是   |

返回值
---
无返回值

sc_initial_account_token
========================
### scope:public
### version:1.0
initial account token

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   |      |  是   |
| address | string |      |  是   |

返回值
---
无返回值

sc_register_cmd_for_contract
============================
### scope:public
### version:1.0
register cmd for contract

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否非空 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   |      |  是   |
| moduleCode      | string |      |  是   |
| cmdRegisterList |  list  |      |  是   |

返回值
---
无返回值

sc_trigger_payable_for_consensus_contract
=========================================
### scope:public
### version:1.0
trigger payable for consensus contract

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否非空 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   |      |  是   |
| stateRoot       | string |      |  是   |
| blockHeight     |  long  |      |  是   |
| contractAddress | string |      |  是   |
| tx              | string |      |  是   |

返回值
---
无返回值

msgProcess
==========
### scope:public
### version:1.0


参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   |      |  是   |
| nodeId      | string |      |  是   |
| cmd         | string |      |  是   |
| messageBody | string |      |  是   |

返回值
---
无返回值

sc_constructor
==============
### scope:public
### version:1.0
contract code constructor

参数列表
----
| 参数名          |  参数类型  | 参数描述                 | 是否非空 |
| ------------ |:------:| -------------------- |:----:|
| chainId      |  int   | 链ID                  |  否   |
| contractCode | string | 智能合约代码(字节码的Hex编码字符串) |  否   |

返回值
---
| 字段名                                                                                                      |     字段类型     | 参数描述              |
| -------------------------------------------------------------------------------------------------------- |:------------:| ----------------- |
| constructor                                                                                              |    object    | 合约构造函数详情          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |    string    | 方法名称              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |    string    | 方法描述              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list<object> | 方法参数列表            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |    string    | 参数类型              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |    string    | 参数名称              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |   boolean    | 是否必填              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |    string    | 返回值类型             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |   boolean    | 是否视图方法（调用此方法数据不上链 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |   boolean    | 是否是事件             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |   boolean    | 是否是可接受主链资产转账的方法   |
| isNrc20                                                                                                  |   boolean    | 是否是NRC20合约        |

sc_delete
=========
### scope:public
### version:1.0
delete contract

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否非空 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   |      |  是   |
| sender          | string |      |  是   |
| contractAddress | string |      |  是   |
| password        | string |      |  是   |
| remark          | string |      |  是   |

返回值
---
无返回值

sc_create
=========
### scope:public
### version:1.0
发布合约/create contract

参数列表
----
| 参数名          |   参数类型   | 参数描述                 | 是否非空 |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | 链id                  |  否   |
| sender       |  string  | 交易创建者账户地址            |  否   |
| password     |  string  | 账户密码                 |  否   |
| alias        |  string  | 合约别名                 |  否   |
| gasLimit     |   long   | GAS限制                |  否   |
| price        |   long   | GAS单价                |  否   |
| contractCode |  string  | 智能合约代码(字节码的Hex编码字符串) |  否   |
| args         | object[] | 参数列表                 |  是   |
| remark       |  string  | 交易备注                 |  是   |

返回值
---
| 字段名             |  字段类型  | 参数描述        |
| --------------- |:------:| ----------- |
| txHash          | string | 发布合约的交易hash |
| contractAddress | string | 生成的合约地址     |

sc_transfer
===========
### scope:public
### version:1.0
transfer NULS from sender to contract address

参数列表
----
| 参数名       |    参数类型    | 参数描述 | 是否非空 |
| --------- |:----------:| ---- |:----:|
| chainId   |    int     |      |  是   |
| address   |   string   |      |  是   |
| toAddress |   string   |      |  是   |
| password  |   string   |      |  是   |
| amount    | biginteger |      |  是   |
| remark    |   string   |      |  是   |

返回值
---
无返回值

sc_contract_result
==================
### scope:public
### version:1.0
contract result

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   |      |  是   |
| hash    | string |      |  是   |

返回值
---
无返回值

sc_contract_result_list
=======================
### scope:public
### version:1.0
contract result list

参数列表
----
| 参数名      |     参数类型     | 参数描述 | 是否非空 |
| -------- |:------------:| ---- |:----:|
| chainId  |     int      |      |  是   |
| hashList | list<string> |      |  是   |

返回值
---
无返回值

sc_validate_create
==================
### scope:public
### version:1.0
validate create contract

参数列表
----
| 参数名          |   参数类型   | 参数描述 | 是否非空 |
| ------------ |:--------:| ---- |:----:|
| chainId      |   int    |      |  是   |
| sender       |  string  |      |  是   |
| gasLimit     |   long   |      |  是   |
| price        |   long   |      |  是   |
| contractCode |  string  |      |  是   |
| args         | object[] |      |  是   |

返回值
---
无返回值

sc_validate_call
================
### scope:public
### version:1.0
validate call contract

参数列表
----
| 参数名             |    参数类型    | 参数描述 | 是否非空 |
| --------------- |:----------:| ---- |:----:|
| chainId         |    int     |      |  否   |
| sender          |   string   |      |  是   |
| value           | biginteger |      |  是   |
| gasLimit        |    long    |      |  是   |
| price           |    long    |      |  是   |
| contractAddress |   string   |      |  是   |
| methodName      |   string   |      |  是   |
| methodDesc      |   string   |      |  是   |
| args            |  object[]  |      |  是   |

返回值
---
无返回值

sc_validate_delete
==================
### scope:public
### version:1.0
validate delete contract

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否非空 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   |      |  是   |
| sender          | string |      |  是   |
| contractAddress | string |      |  是   |

返回值
---
无返回值

sc_imputed_create_gas
=====================
### scope:public
### version:1.0
imputed create gas

参数列表
----
| 参数名          |   参数类型   | 参数描述 | 是否非空 |
| ------------ |:--------:| ---- |:----:|
| chainId      |   int    |      |  是   |
| sender       |  string  |      |  是   |
| contractCode |  string  |      |  是   |
| args         | object[] |      |  是   |

返回值
---
无返回值

sc_imputed_call_gas
===================
### scope:public
### version:1.0
imputed call gas

参数列表
----
| 参数名             |    参数类型    | 参数描述 | 是否非空 |
| --------------- |:----------:| ---- |:----:|
| chainId         |    int     |      |  是   |
| sender          |   string   |      |  是   |
| value           | biginteger |      |  是   |
| contractAddress |   string   |      |  是   |
| methodName      |   string   |      |  是   |
| methodDesc      |   string   |      |  是   |
| args            |  object[]  |      |  是   |

返回值
---
无返回值

sc_transfer_fee
===============
### scope:public
### version:1.0
transfer fee, transfer NULS from sender to contract address

参数列表
----
| 参数名       |    参数类型    | 参数描述 | 是否非空 |
| --------- |:----------:| ---- |:----:|
| chainId   |    int     |      |  是   |
| address   |   string   |      |  是   |
| toAddress |   string   |      |  是   |
| amount    | biginteger |      |  是   |
| remark    |   string   |      |  是   |

返回值
---
无返回值

sc_token_transfer
=================
### scope:public
### version:1.0
transfer NRC20-token from address to toAddress

参数列表
----
| 参数名             |    参数类型    | 参数描述 | 是否非空 |
| --------------- |:----------:| ---- |:----:|
| chainId         |    int     |      |  是   |
| address         |   string   |      |  是   |
| toAddress       |   string   |      |  是   |
| contractAddress |   string   |      |  是   |
| password        |   string   |      |  是   |
| amount          | biginteger |      |  是   |
| remark          |   string   |      |  是   |

返回值
---
无返回值

sc_token_balance
================
### scope:public
### version:1.0
NRC20代币余额详情/NRC20-token balance

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否非空 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | 链ID  |  否   |
| contractAddress | string | 合约地址 |  否   |
| address         | string | 账户地址 |  否   |

返回值
---
| 字段名             |  字段类型  | 参数描述                    |
| --------------- |:------:| ----------------------- |
| contractAddress | string | 合约地址                    |
| name            | string | token名称                 |
| symbol          | string | token符号                 |
| amount          | string | token数量                 |
| decimals        |  long  | token支持的小数位数            |
| blockHeight     |  long  | 合约创建时的区块高度              |
| status          |  int   | 合约状态(0-不存在, 1-正常, 2-终止) |

sc_invoke_view
==============
### scope:public
### version:1.0
invoke view contract

参数列表
----
| 参数名             |   参数类型   | 参数描述 | 是否非空 |
| --------------- |:--------:| ---- |:----:|
| chainId         |   int    |      |  是   |
| contractAddress |  string  |      |  是   |
| methodName      |  string  |      |  是   |
| methodDesc      |  string  |      |  是   |
| args            | object[] |      |  是   |

返回值
---
无返回值

sc_contract_info
================
### scope:public
### version:1.0
contract info

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否非空 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | 链ID  |  否   |
| contractAddress | string | 合约地址 |  否   |

返回值
---
| 字段名                                                                                                      |     字段类型     | 参数描述                          |
| -------------------------------------------------------------------------------------------------------- |:------------:| ----------------------------- |
| createTxHash                                                                                             |    string    | 发布合约的交易hash                   |
| address                                                                                                  |    string    | 合约地址                          |
| creater                                                                                                  |    string    | 合约创建者地址                       |
| alias                                                                                                    |    string    | 合约别名                          |
| createTime                                                                                               |    string    | 合约创建时间（单位：秒）                  |
| blockHeight                                                                                              |    string    | 合约创建时的区块高度                    |
| isNrc20                                                                                                  |    string    | 是否是NRC20合约                    |
| nrc20TokenName                                                                                           |    string    | NRC20-token名称                 |
| nrc20TokenSymbol                                                                                         |    string    | NRC20-token符号                 |
| decimals                                                                                                 |    string    | NRC20-token支持的小数位数            |
| totalSupply                                                                                              |    string    | NRC20-token发行总量               |
| status                                                                                                   |    string    | 合约状态（not_found, normal, stop） |
| method                                                                                                   | list<object> | 合约方法列表                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |    string    | 方法名称                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |    string    | 方法描述                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list<object> | 方法参数列表                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |    string    | 参数类型                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |    string    | 参数名称                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |   boolean    | 是否必填                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |    string    | 返回值类型                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |   boolean    | 是否视图方法（调用此方法数据不上链             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |   boolean    | 是否是事件                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |   boolean    | 是否是可接受主链资产转账的方法               |

sc_contract_tx
==============
### scope:public
### version:1.0
contract tx

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   |      |  是   |
| hash    | string |      |  是   |

返回值
---
无返回值

sc_token_assets_list
====================
### scope:public
### version:1.0
token资产集合/token assets list

参数列表
----
| 参数名        |  参数类型  | 参数描述 | 是否非空 |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | 链ID  |  否   |
| address    | string | 账户地址 |  否   |
| pageNumber |  int   | 页码   |  是   |
| pageSize   |  int   | 每页大小 |  是   |

返回值
---
| 字段名             |  字段类型  | 参数描述                    |
| --------------- |:------:| ----------------------- |
| contractAddress | string | 合约地址                    |
| name            | string | token名称                 |
| symbol          | string | token符号                 |
| amount          | string | token数量                 |
| decimals        |  long  | token支持的小数位数            |
| blockHeight     |  long  | 合约创建时的区块高度              |
| status          |  int   | 合约状态(0-不存在, 1-正常, 2-终止) |

sc_token_transfer_list
======================
### scope:public
### version:1.0
token transfer list

参数列表
----
| 参数名        |  参数类型  | 参数描述 | 是否非空 |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   |      |  是   |
| address    | string |      |  是   |
| pageNumber |  int   |      |  是   |
| pageSize   |  int   |      |  是   |

返回值
---
无返回值

sc_account_contracts
====================
### scope:public
### version:1.0
account contract list

参数列表
----
| 参数名        |  参数类型  | 参数描述 | 是否非空 |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   |      |  是   |
| address    | string |      |  是   |
| pageNumber |  int   |      |  是   |
| pageSize   |  int   |      |  是   |

返回值
---
无返回值

sc_upload
=========
### scope:public
### version:1.0
upload

参数列表
----
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   |      |  是   |
| jarFileData | string |      |  是   |

返回值
---
无返回值

sc_call
=======
### scope:public
### version:1.0
call contract

参数列表
----
| 参数名             |    参数类型    | 参数描述 | 是否非空 |
| --------------- |:----------:| ---- |:----:|
| chainId         |    int     |      |  是   |
| sender          |   string   |      |  是   |
| value           | biginteger |      |  是   |
| gasLimit        |    long    |      |  是   |
| price           |    long    |      |  是   |
| contractAddress |   string   |      |  是   |
| methodName      |   string   |      |  是   |
| methodDesc      |   string   |      |  是   |
| args            |  object[]  |      |  是   |
| password        |   string   |      |  是   |
| remark          |   string   |      |  是   |

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

