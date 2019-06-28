模块说明

```
这个文件夹下才开始放置代码，可以是任意语言开发的代码
```

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

sc\_invoke\_contract
====================
### scope:public
### version:1.0
批次通知开始后，一笔一笔执行合约/invoke contract one by one

参数列表
----
| 参数名     |  参数类型  | 参数描述           | 是否非空 |
| ------- |:------:| -------------- |:----:|
| chainId |  int   | 链id            |  是   |
| tx      | string | 交易序列化的HEX编码字符串 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述                          |
| --- |:----:| ----------------------------- |
| N/A | void | 无特定返回值，没有错误即成功，如果返回错误，则丢弃这笔交易 |

sc\_batch\_begin
================
### scope:public
### version:1.0
执行合约一个批次的开始通知，生成当前批次的信息/batch begin

参数列表
----
| 参数名            |  参数类型  | 参数描述         | 是否非空 |
| -------------- |:------:| ------------ |:----:|
| chainId        |  int   | 链id          |  是   |
| blockHeight    |  long  | 当前打包的区块高度    |  是   |
| blockTime      |  long  | 当前打包的区块时间    |  是   |
| packingAddress | string | 当前打包的区块打包地址  |  是   |
| preStateRoot   | string | 上一个stateRoot |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

sc\_batch\_before\_end
======================
### scope:public
### version:1.0
交易模块打包完交易，在做统一验证前，通知合约模块，合约模块停止接收交易，开始异步处理这个批次的结果/batch before end

参数列表
----
| 参数名         | 参数类型 | 参数描述      | 是否非空 |
| ----------- |:----:| --------- |:----:|
| chainId     | int  | 链id       |  是   |
| blockHeight | long | 当前打包的区块高度 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述                                                 |
| --- |:----:| ---------------------------------------------------- |
| N/A | void | 无特定返回值，没有错误即成功，如果返回错误，则废弃这个批次，批次内已执行的合约交易退还到待打包交易队列中 |

sc\_batch\_end
==============
### scope:public
### version:1.0
通知当前批次结束并返回结果/batch end

参数列表
----
| 参数名         | 参数类型 | 参数描述      | 是否非空 |
| ----------- |:----:| --------- |:----:|
| chainId     | int  | 链id       |  是   |
| blockHeight | long | 当前打包的区块高度 |  是   |

返回值
---
| 字段名       |      字段类型       | 参数描述                                   |
| --------- |:---------------:| -------------------------------------- |
| stateRoot |     string      | 当前stateRoot                            |
| txList    | list&lt;string> | 合约新生成的交易序列化字符串列表(可能有合约转账、合约共识、合约返回GAS) |

sc\_contract\_offline\_tx\_hash\_list
=====================================
### scope:public
### version:1.0
返回指定区块中合约生成交易（合约返回GAS交易除外）的hash列表（合约新生成的交易除合约返回GAS交易外，不保存到区块中，合约模块保存了这些交易和指定区块的关系）/contract offline tx hash list

参数列表
----
| 参数名       |  参数类型  | 参数描述   | 是否非空 |
| --------- |:------:| ------ |:----:|
| chainId   |  int   | 链id    |  是   |
| blockHash | string | 区块hash |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述                       |
| ---- |:---------------:| -------------------------- |
| list | list&lt;string> | 合约交易序列化字符串列表(可能有合约转账、合约共识) |

sc\_initial\_account\_token
===========================
### scope:public
### version:1.0
初始化账户token信息，节点导入账户时调用/initial account token

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链id  |  是   |
| address | string | 账户地址 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

sc\_register\_cmd\_for\_contract
================================
### scope:public
### version:1.0
其他模块向合约模块注册可被合约调用的命令，注册后，可在合约代码内调用注册的命令/register cmd for contract

参数列表
----
| 参数名             |  参数类型  | 参数描述   | 是否非空 |
| --------------- |:------:| ------ |:----:|
| chainId         |  int   | 链id    |  是   |
| moduleCode      | string | 模块代码   |  是   |
| cmdRegisterList |  list  | 注册信息列表 |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

sc\_trigger\_payable\_for\_consensus\_contract
==============================================
### scope:public
### version:1.0
共识奖励收益地址是合约地址时，会触发合约的_payable(String[][] args)方法，参数是节点收益地址明细<br>args[0] = new String[]{address, amount}<br>...<br>/trigger payable for consensus contract

参数列表
----
| 参数名             |  参数类型  | 参数描述                     | 是否非空 |
| --------------- |:------:| ------------------------ |:----:|
| chainId         |  int   | 链id                      |  是   |
| stateRoot       | string | 当前的stateRoot             |  是   |
| blockHeight     |  long  | 当前打包的区块高度                |  是   |
| contractAddress | string | 合约地址                     |  是   |
| tx              | string | 当前打包区块中的CoinBase交易序列化字符串 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述          |
| ----- |:------:| ------------- |
| value | string | 变化后的stateRoot |

sc\_constructor
===============
### scope:public
### version:1.0
contract code constructor

参数列表
----
| 参数名          |  参数类型  | 参数描述                 | 是否非空 |
| ------------ |:------:| -------------------- |:----:|
| chainId      |  int   | 链ID                  |  是   |
| contractCode | string | 智能合约代码(字节码的Hex编码字符串) |  是   |

返回值
---
| 字段名                                                                                                      |      字段类型       | 参数描述               |
| -------------------------------------------------------------------------------------------------------- |:---------------:| ------------------ |
| constructor                                                                                              |     object      | 合约构造函数详情           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | 方法名称               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | 方法描述               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | 方法参数列表             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | 参数类型               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | 参数名称               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | 是否必填               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | 返回值类型              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | 是否视图方法（调用此方法数据不上链） |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | 是否是事件              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | 是否是可接受主链资产转账的方法    |
| isNrc20                                                                                                  |     boolean     | 是否是NRC20合约         |

sc\_delete
==========
### scope:public
### version:1.0
delete contract

参数列表
----
| 参数名             |  参数类型  | 参数描述      | 是否非空 |
| --------------- |:------:| --------- |:----:|
| chainId         |  int   | 链id       |  是   |
| sender          | string | 交易创建者账户地址 |  是   |
| contractAddress | string | 合约地址      |  是   |
| password        | string | 交易账户密码    |  是   |
| remark          | string | 交易备注      |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述        |
| ------ |:------:| ----------- |
| txHash | string | 删除合约的交易hash |

sc\_create
==========
### scope:public
### version:1.0
发布合约/create contract

参数列表
----
| 参数名          |   参数类型   | 参数描述                 | 是否非空 |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | 链id                  |  是   |
| sender       |  string  | 交易创建者账户地址            |  是   |
| password     |  string  | 账户密码                 |  是   |
| alias        |  string  | 合约别名                 |  是   |
| gasLimit     |   long   | GAS限制                |  是   |
| price        |   long   | GAS单价                |  是   |
| contractCode |  string  | 智能合约代码(字节码的Hex编码字符串) |  是   |
| args         | object[] | 参数列表                 |  否   |
| remark       |  string  | 交易备注                 |  否   |

返回值
---
| 字段名             |  字段类型  | 参数描述        |
| --------------- |:------:| ----------- |
| txHash          | string | 发布合约的交易hash |
| contractAddress | string | 生成的合约地址     |

sc\_transfer
============
### scope:public
### version:1.0
从账户地址向合约地址转账(主链资产)/transfer NULS from sender to contract address

参数列表
----
| 参数名       |    参数类型    | 参数描述      | 是否非空 |
| --------- |:----------:| --------- |:----:|
| chainId   |    int     | 链id       |  是   |
| address   |   string   | 转出者账户地址   |  是   |
| toAddress |   string   | 转入的合约地址   |  是   |
| password  |   string   | 转出者账户密码   |  是   |
| amount    | biginteger | 转出的主链资产金额 |  是   |
| remark    |   string   | 交易备注      |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述   |
| ------ |:------:| ------ |
| txHash | string | 交易hash |

sc\_contract\_result
====================
### scope:public
### version:1.0
contract result

参数列表
----
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链id    |  是   |
| hash    | string | 交易hash |  是   |

返回值
---
| 字段名                                                                                                   |      字段类型       | 参数描述                                        |
| ----------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------- |
| success                                                                                               |     boolean     | 合约执行是否成功                                    |
| errorMessage                                                                                          |     string      | 执行失败信息                                      |
| contractAddress                                                                                       |     string      | 合约地址                                        |
| result                                                                                                |     string      | 合约执行结果                                      |
| gasLimit                                                                                              |      long       | GAS限制                                       |
| gasUsed                                                                                               |      long       | 已使用GAS                                      |
| price                                                                                                 |      long       | GAS单价                                       |
| totalFee                                                                                              |     string      | 交易总手续费                                      |
| txSizeFee                                                                                             |     string      | 交易大小手续费                                     |
| actualContractFee                                                                                     |     string      | 实际执行合约手续费                                   |
| refundFee                                                                                             |     string      | 合约返回的手续费                                    |
| value                                                                                                 |     string      | 调用者向合约地址转入的主网资产金额，没有此业务时则为0                 |
| stackTrace                                                                                            |     string      | 异常堆栈踪迹                                      |
| transfers                                                                                             | list&lt;object> | 合约转账列表（从合约转出）                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                |     string      | 合约生成交易：合约转账交易hash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | 转出的合约地址                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                               | list&lt;object> | 转入的地址列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to    |     string      | 转入地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value |     string      | 转入金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                           |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）         |
| events                                                                                                | list&lt;string> | 合约事件列表                                      |
| tokenTransfers                                                                                        | list&lt;object> | 合约token转账列表                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                       |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | 付款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                    |     string      | 收款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                  |     string      | token名称                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                |     string      | token符号                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                              |      long       | token支持的小数位数                                |
| invokeRegisterCmds                                                                                    | list&lt;object> | 合约调用外部命令的调用记录列表                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                               |     string      | 命令名称                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                  |       map       | 命令参数，参数不固定，依据不同的命令而来，故此处不作描述，结构为 {参数名称=参数值} |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                       |     string      | 注册的命令模式（QUERY\_DATA or NEW\_TX）             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                             |     string      | 生成的交易hash（当调用的命令模式是 NEW\_TX 时，会生成交易）        |
| contractTxList                                                                                        | list&lt;string> | 合约生成交易的序列化字符串列表                             |
| remark                                                                                                |     string      | 备注                                          |

sc\_contract\_result\_list
==========================
### scope:public
### version:1.0
contract result list

参数列表
----
| 参数名      |     参数类型     | 参数描述     | 是否非空 |
| -------- |:------------:| -------- |:----:|
| chainId  |     int      | 链id      |  是   |
| hashList | list<string> | 交易hash列表 |  是   |

返回值
---
| 字段名                                                                                                                                                   |      字段类型       | 参数描述                                        |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------- |
| hash1 or hash2 or hash3...                                                                                                                            |     object      | 以交易hash列表中的hash值作为key，这里的key name是动态的       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;success                                                                                               |     boolean     | 合约执行是否成功                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;errorMessage                                                                                          |     string      | 执行失败信息                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                                                                       |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;result                                                                                                |     string      | 合约执行结果                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit                                                                                              |      long       | GAS限制                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasUsed                                                                                               |      long       | 已使用GAS                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                                                                 |      long       | GAS单价                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalFee                                                                                              |     string      | 交易总手续费                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txSizeFee                                                                                             |     string      | 交易大小手续费                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;actualContractFee                                                                                     |     string      | 实际执行合约手续费                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;refundFee                                                                                             |     string      | 合约返回的手续费                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                                                                 |     string      | 调用者向合约地址转入的主网资产金额，没有此业务时则为0                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;stackTrace                                                                                            |     string      | 异常堆栈踪迹                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transfers                                                                                             | list&lt;object> | 合约转账列表（从合约转出）                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                |     string      | 合约生成交易：合约转账交易hash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | 转出的合约地址                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                               | list&lt;object> | 转入的地址列表                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to    |     string      | 转入地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value |     string      | 转入金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                           |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;events                                                                                                | list&lt;string> | 合约事件列表                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tokenTransfers                                                                                        | list&lt;object> | 合约token转账列表                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                       |     string      | 合约地址                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | 付款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                    |     string      | 收款方                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | 转账金额                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                  |     string      | token名称                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                |     string      | token符号                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                              |      long       | token支持的小数位数                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;invokeRegisterCmds                                                                                    | list&lt;object> | 合约调用外部命令的调用记录列表                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                               |     string      | 命令名称                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                  |       map       | 命令参数，参数不固定，依据不同的命令而来，故此处不作描述，结构为 {参数名称=参数值} |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                       |     string      | 注册的命令模式（QUERY\_DATA or NEW\_TX）             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                             |     string      | 生成的交易hash（当调用的命令模式是 NEW\_TX 时，会生成交易）        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractTxList                                                                                        | list&lt;string> | 合约生成交易的序列化字符串列表                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                                                                |     string      | 备注                                          |

sc\_imputed\_create\_gas
========================
### scope:public
### version:1.0
预估发布合约消耗的GAS/imputed create gas

参数列表
----
| 参数名          |   参数类型   | 参数描述                 | 是否非空 |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | 链id                  |  是   |
| sender       |  string  | 交易创建者账户地址            |  是   |
| contractCode |  string  | 智能合约代码(字节码的Hex编码字符串) |  是   |
| args         | object[] | 参数列表                 |  否   |

返回值
---
| 字段名      | 字段类型 | 参数描述              |
| -------- |:----:| ----------------- |
| gasLimit | long | 消耗的gas值，执行失败返回数值1 |

sc\_imputed\_call\_gas
======================
### scope:public
### version:1.0
imputed call gas

参数列表
----
| 参数名             |    参数类型    | 参数描述                                     | 是否非空 |
| --------------- |:----------:| ---------------------------------------- |:----:|
| chainId         |    int     | 链id                                      |  是   |
| sender          |   string   | 交易创建者账户地址                                |  是   |
| value           | biginteger | 调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO |  是   |
| contractAddress |   string   | 合约地址                                     |  是   |
| methodName      |   string   | 合约方法                                     |  是   |
| methodDesc      |   string   | 合约方法描述，若合约内方法没有重载，则此参数可以为空               |  否   |
| args            |  object[]  | 参数列表                                     |  否   |

返回值
---
| 字段名      | 字段类型 | 参数描述              |
| -------- |:----:| ----------------- |
| gasLimit | long | 消耗的gas值，执行失败返回数值1 |

sc\_token\_transfer
===================
### scope:public
### version:1.0
NRC20-token转账/transfer NRC20-token from address to toAddress

参数列表
----
| 参数名             |    参数类型    | 参数描述         | 是否非空 |
| --------------- |:----------:| ------------ |:----:|
| chainId         |    int     | 链id          |  是   |
| address         |   string   | 转出者账户地址      |  是   |
| toAddress       |   string   | 转入地址         |  是   |
| contractAddress |   string   | token合约地址    |  是   |
| password        |   string   | 转出者账户密码      |  是   |
| amount          | biginteger | 转出的token资产金额 |  是   |
| remark          |   string   | 交易备注         |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述   |
| ------ |:------:| ------ |
| txHash | string | 交易hash |

sc\_token\_balance
==================
### scope:public
### version:1.0
NRC20代币余额详情/NRC20-token balance

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否非空 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | 链ID  |  是   |
| contractAddress | string | 合约地址 |  是   |
| address         | string | 账户地址 |  是   |

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

sc\_invoke\_view
================
### scope:public
### version:1.0
invoke view contract

参数列表
----
| 参数名             |   参数类型   | 参数描述                       | 是否非空 |
| --------------- |:--------:| -------------------------- |:----:|
| chainId         |   int    | 链id                        |  是   |
| contractAddress |  string  | 合约地址                       |  是   |
| methodName      |  string  | 合约方法                       |  是   |
| methodDesc      |  string  | 合约方法描述，若合约内方法没有重载，则此参数可以为空 |  否   |
| args            | object[] | 参数列表                       |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述      |
| ------ |:------:| --------- |
| result | string | 视图方法的调用结果 |

sc\_contract\_info
==================
### scope:public
### version:1.0
合约信息详情/contract info

参数列表
----
| 参数名             |  参数类型  | 参数描述 | 是否非空 |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | 链ID  |  是   |
| contractAddress | string | 合约地址 |  是   |

返回值
---
| 字段名                                                                                                      |      字段类型       | 参数描述                          |
| -------------------------------------------------------------------------------------------------------- |:---------------:| ----------------------------- |
| createTxHash                                                                                             |     string      | 发布合约的交易hash                   |
| address                                                                                                  |     string      | 合约地址                          |
| creater                                                                                                  |     string      | 合约创建者地址                       |
| alias                                                                                                    |     string      | 合约别名                          |
| createTime                                                                                               |     string      | 合约创建时间（单位：秒）                  |
| blockHeight                                                                                              |     string      | 合约创建时的区块高度                    |
| isNrc20                                                                                                  |     string      | 是否是NRC20合约                    |
| nrc20TokenName                                                                                           |     string      | NRC20-token名称                 |
| nrc20TokenSymbol                                                                                         |     string      | NRC20-token符号                 |
| decimals                                                                                                 |     string      | NRC20-token支持的小数位数            |
| totalSupply                                                                                              |     string      | NRC20-token发行总量               |
| status                                                                                                   |     string      | 合约状态（not_found, normal, stop） |
| method                                                                                                   | list&lt;object> | 合约方法列表                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | 方法名称                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | 方法描述                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | 方法参数列表                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | 参数类型                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | 参数名称                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | 是否必填                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | 返回值类型                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | 是否视图方法（调用此方法数据不上链）            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | 是否是事件                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | 是否是可接受主链资产转账的方法               |

sc\_contract\_tx
================
### scope:public
### version:1.0
合约交易/contract tx

参数列表
----
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链id    |  是   |
| hash    | string | 交易hash |  是   |

返回值
---
| 字段名                                                                                                                                                             |      字段类型       | 参数描述                                                    |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------------------- |
| hash                                                                                                                                                            |     string      | 交易hash                                                  |
| type                                                                                                                                                            |     integer     | 交易类型                                                    |
| time                                                                                                                                                            |      long       | 交易时间                                                    |
| blockHeight                                                                                                                                                     |      long       | 区块高度                                                    |
| fee                                                                                                                                                             |     string      | 交易手续费                                                   |
| value                                                                                                                                                           |     string      | 交易金额                                                    |
| remark                                                                                                                                                          |     string      | 备注                                                      |
| scriptSig                                                                                                                                                       |     string      | 签名信息                                                    |
| status                                                                                                                                                          |     integer     | 交易状态（0 - 确认中，1 - 已确认）                                   |
| confirmCount                                                                                                                                                    |      long       | 交易确认次数                                                  |
| size                                                                                                                                                            |       int       | 交易大小                                                    |
| inputs                                                                                                                                                          | list&lt;object> | 交易输入集合                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address                                                                                                         |     string      | 输入地址                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId                                                                                                   |       int       | 资产链ID                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId                                                                                                        |       int       | 资产ID                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount                                                                                                          |     string      | 花费金额                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce                                                                                                           |     string      | 地址的账本nonce值                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;locked                                                                                                          |      byte       | 解锁交易的标签（0 - 非解锁交易，1 - 解锁交易）                             |
| outputs                                                                                                                                                         | list&lt;object> | 交易输出集合                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address                                                                                                         |     string      | 输出地址                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId                                                                                                   |       int       | 资产链ID                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId                                                                                                        |       int       | 资产ID                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount                                                                                                          |     string      | 输出金额                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime                                                                                                        |      long       | 锁定时间                                                    |
| txData                                                                                                                                                          |       map       | 合约交易业务数据                                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;data                                                                                                            |     object      | 根据合约交易类型反映不同的业务数据（这里为了描述四种情况，四种业务放在一起描述，实际上不同时存在，只存在一个） |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;create                                                          |     object      | 发布合约交易的业务数据                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |     string      | 交易创建者地址                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string      | 创建的合约地址                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias           |     string      | 合约别名                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hexCode         |     string      | 智能合约代码(字节码的Hex编码字符串)                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit        |      long       | GAS限制                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price           |      long       | GAS单价                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |   string[][]    | 参数列表                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;call                                                            |     object      | 调用合约交易的业务数据                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |     string      | 交易创建者地址                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string      | 合约地址                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value           |     string      | 调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit        |      long       | GAS限制                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price           |      long       | GAS单价                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |     string      | 合约方法                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |     string      | 合约方法描述，若合约内方法没有重载，则此参数可以为空                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |   string[][]    | 参数列表                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delete                                                          |     object      | 删除合约交易的业务数据                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |     string      | 交易创建者地址                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string      | 合约地址                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transfer                                                        |     object      | 合约转账交易的业务数据                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash     |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string      | 合约地址                                                    |
| contractResult                                                                                                                                                  |     object      | 合约执行结果                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;success                                                                                                         |     boolean     | 合约执行是否成功                                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;errorMessage                                                                                                    |     string      | 执行失败信息                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                                                                                 |     string      | 合约地址                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;result                                                                                                          |     string      | 合约执行结果                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit                                                                                                        |      long       | GAS限制                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasUsed                                                                                                         |      long       | 已使用GAS                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                                                                           |      long       | GAS单价                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalFee                                                                                                        |     string      | 交易总手续费                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txSizeFee                                                                                                       |     string      | 交易大小手续费                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;actualContractFee                                                                                               |     string      | 实际执行合约手续费                                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;refundFee                                                                                                       |     string      | 合约返回的手续费                                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                                                                           |     string      | 调用者向合约地址转入的主网资产金额，没有此业务时则为0                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;stackTrace                                                                                                      |     string      | 异常堆栈踪迹                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transfers                                                                                                       | list&lt;object> | 合约转账列表（从合约转出）                                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                          |     string      | 合约生成交易：合约转账交易hash                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                            |     string      | 转出的合约地址                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                           |     string      | 转账金额                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                                         | list&lt;object> | 转入的地址列表                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to              |     string      | 转入地址                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value           |     string      | 转入金额                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                                     |     string      | 调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;events                                                                                                          | list&lt;string> | 合约事件列表                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tokenTransfers                                                                                                  | list&lt;object> | 合约token转账列表                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                                 |     string      | 合约地址                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                            |     string      | 付款方                                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                              |     string      | 收款方                                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                           |     string      | 转账金额                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                            |     string      | token名称                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                          |     string      | token符号                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                                        |      long       | token支持的小数位数                                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;invokeRegisterCmds                                                                                              | list&lt;object> | 合约调用外部命令的调用记录列表                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                                         |     string      | 命令名称                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                            |       map       | 命令参数，参数不固定，依据不同的命令而来，故此处不作描述，结构为 {参数名称=参数值}             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                                 |     string      | 注册的命令模式（QUERY\_DATA or NEW\_TX）                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                                       |     string      | 生成的交易hash（当调用的命令模式是 NEW\_TX 时，会生成交易）                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractTxList                                                                                                  | list&lt;string> | 合约生成交易的序列化字符串列表                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                                                                          |     string      | 备注                                                      |

sc\_token\_assets\_list
=======================
### scope:public
### version:1.0
token资产集合/token assets list

参数列表
----
| 参数名        |  参数类型  | 参数描述 | 是否非空 |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | 链ID  |  是   |
| address    | string | 账户地址 |  是   |
| pageNumber |  int   | 页码   |  否   |
| pageSize   |  int   | 每页大小 |  否   |

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

sc\_token\_transfer\_list
=========================
### scope:public
### version:1.0
token转账交易列表/token transfer list

参数列表
----
| 参数名        |  参数类型  | 参数描述 | 是否非空 |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | 链ID  |  是   |
| address    | string | 账户地址 |  是   |
| pageNumber |  int   | 页码   |  否   |
| pageSize   |  int   | 每页大小 |  否   |

返回值
---
| 字段名             |  字段类型  | 参数描述                           |
| --------------- |:------:| ------------------------------ |
| contractAddress | string | 合约地址                           |
| from            | string | 付款方                            |
| to              | string | 收款方                            |
| value           | string | 转账金额                           |
| time            |  long  | 交易时间                           |
| status          |  byte  | 交易状态（0 - 确认中， 1 - 已确认， 2 - 失败） |
| txHash          | string | 交易hash                         |
| blockHeight     |  long  | 区块高度                           |
| name            | string | token名称                        |
| symbol          | string | token符号                        |
| decimals        |  long  | token支持的小数位数                   |
| info            | string | token资产变动信息                    |

sc\_account\_contracts
======================
### scope:public
### version:1.0
账户的合约地址列表/account contract list

参数列表
----
| 参数名        |  参数类型  | 参数描述 | 是否非空 |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | 链ID  |  是   |
| address    | string | 账户地址 |  是   |
| pageNumber |  int   | 页码   |  否   |
| pageSize   |  int   | 每页大小 |  否   |

返回值
---
| 字段名             |  字段类型  | 参数描述                                                   |
| --------------- |:------:| ------------------------------------------------------ |
| contractAddress | string | 合约地址                                                   |
| createTime      |  long  | 合约创建时间                                                 |
| height          |  long  | 合约创建时区块高度                                              |
| confirmCount    |  long  | 合约创建确认次数                                               |
| alias           | string | 合约别名                                                   |
| status          |  int   | 合约状态（0 - 不存在或者创建中, 1 - 正常, 2 - 已删除, 3 - 创建失败, 4 - 锁定中） |
| msg             | string | 合约创建失败的错误信息                                            |

sc\_upload
==========
### scope:public
### version:1.0
合约代码jar包上传/upload

参数列表
----
| 参数名         |  参数类型  | 参数描述                                         | 是否非空 |
| ----------- |:------:| -------------------------------------------- |:----:|
| chainId     |  int   | 链id                                          |  是   |
| jarFileData | string | 文件描述和文件字节流转换Base64编码字符串（文件描述和Base64字符串以逗号隔开） |  是   |

返回值
---
| 字段名                                                                                                      |      字段类型       | 参数描述                 |
| -------------------------------------------------------------------------------------------------------- |:---------------:| -------------------- |
| constructor                                                                                              |     object      | 合约构造函数详情             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | 方法名称                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | 方法描述                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | 方法参数列表               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | 参数类型                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | 参数名称                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | 是否必填                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | 返回值类型                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | 是否视图方法（调用此方法数据不上链）   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | 是否是事件                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | 是否是可接受主链资产转账的方法      |
| isNrc20                                                                                                  |     boolean     | 是否是NRC20合约           |
| code                                                                                                     |     string      | 智能合约代码(字节码的Hex编码字符串) |

sc\_validate\_create
====================
### scope:public
### version:1.0
验证发布合约/validate create contract

参数列表
----
| 参数名          |   参数类型   | 参数描述                 | 是否非空 |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | 链id                  |  是   |
| sender       |  string  | 交易创建者账户地址            |  是   |
| gasLimit     |   long   | GAS限制                |  是   |
| price        |   long   | GAS单价                |  是   |
| contractCode |  string  | 智能合约代码(字节码的Hex编码字符串) |  是   |
| args         | object[] | 参数列表                 |  否   |

返回值
---
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即验证成功 |

sc\_validate\_call
==================
### scope:public
### version:1.0
validate call contract

参数列表
----
| 参数名             |    参数类型    | 参数描述                                     | 是否非空 |
| --------------- |:----------:| ---------------------------------------- |:----:|
| chainId         |    int     | 链id                                      |  是   |
| sender          |   string   | 交易创建者账户地址                                |  是   |
| value           | biginteger | 调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO |  是   |
| gasLimit        |    long    | GAS限制                                    |  是   |
| price           |    long    | GAS单价                                    |  是   |
| contractAddress |   string   | 合约地址                                     |  是   |
| methodName      |   string   | 合约方法                                     |  是   |
| methodDesc      |   string   | 合约方法描述，若合约内方法没有重载，则此参数可以为空               |  否   |
| args            |  object[]  | 参数列表                                     |  否   |

返回值
---
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即验证成功 |

sc\_validate\_delete
====================
### scope:public
### version:1.0
validate delete contract

参数列表
----
| 参数名             |  参数类型  | 参数描述      | 是否非空 |
| --------------- |:------:| --------- |:----:|
| chainId         |  int   | 链id       |  是   |
| sender          | string | 交易创建者账户地址 |  是   |
| contractAddress | string | 合约地址      |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即验证成功 |

sc\_call
========
### scope:public
### version:1.0
call contract

参数列表
----
| 参数名             |    参数类型    | 参数描述                                     | 是否非空 |
| --------------- |:----------:| ---------------------------------------- |:----:|
| chainId         |    int     | 链id                                      |  是   |
| sender          |   string   | 交易创建者账户地址                                |  是   |
| value           | biginteger | 调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO |  是   |
| gasLimit        |    long    | GAS限制                                    |  是   |
| price           |    long    | GAS单价                                    |  是   |
| contractAddress |   string   | 合约地址                                     |  是   |
| methodName      |   string   | 合约方法                                     |  是   |
| methodDesc      |   string   | 合约方法描述，若合约内方法没有重载，则此参数可以为空               |  否   |
| args            |  object[]  | 参数列表                                     |  否   |
| password        |   string   | 调用者账户密码                                  |  是   |
| remark          |   string   | 交易备注                                     |  否   |

返回值
---
| 字段名    |  字段类型  | 参数描述        |
| ------ |:------:| ----------- |
| txHash | string | 调用合约的交易hash |

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

