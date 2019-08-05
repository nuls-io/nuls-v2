# 账户模块
账户模块是提供关于账户各项功能的基础性模块。主要对账户的生成、安全和保管、信息的获取等几个方面的功能提供支持，其他模块可以根据账户模块提供的接口来使用账户的各种功能以及获取账户信息，用户或者其他应用可以根据RPC接口对账户进行更加实用性和个性化的操作。账户是基础模块，也是用户数据的载体 。

- 账户的生成
- 创建账户、导入账户
- 账户的安全和保管
- 账户的备份、设置账户密码、修改账户密码、移除账户
- 账户信息的获取
- 查询单个账户信息、获取多个账户信息、获取账户地址、查询账户余额、查询账户别名
- 其他实用性和个性化功能  设置账户别名、设置账户备注、验证账户是否加密、签名、验证账户地址格式、验证账户密码是否正确等功能


ac\_createOfflineAccount
========================
### scope:public
### version:1.0
创建离线账户, 该账户不保存到数据库, 并将直接返回账户的所有信息/create an offline account, which is not saved to the database and will directly return all information to the account.

参数列表
----
| 参数名      |  参数类型  | 参数描述      | 是否非空 |
| -------- |:------:| --------- |:----:|
| chainId  |  int   | 链id       |  是   |
| count    |  int   | 需要创建账户的数量 |  是   |
| password | string | 账户密码      |  是   |

返回值
---
| 字段名                                                             |      字段类型       | 参数描述   |
| --------------------------------------------------------------- |:---------------:| ------ |
| list                                                            | list&lt;object> | 离线账户集合 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address         |     string      | 账户地址   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKey          |     string      | 公钥     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;priKey          |     string      | 私钥     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPriKey |     string      | 加密后的私钥 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;extend          |     string      | 其他信息   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;createTime      |      long       | 创建时间   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encrypted       |     boolean     | 账户是否加密 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark          |     string      | 账户备注   |

ac\_createContractAccount
=========================
### scope:public
### version:1.0
创建智能合约账户/create smart contract account

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名     |  字段类型  | 参数描述   |
| ------- |:------:| ------ |
| address | string | 智能合约地址 |

ac\_getEncryptedAddressList
===========================
### scope:public
### version:1.0
获取本地加密账户列表/Get a list of locally encrypted accounts

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述     |
| ---- |:---------------:| -------- |
| list | list&lt;string> | 返回账户地址集合 |

ac\_getAccountByAddress
=======================
### scope:public
### version:1.0
通过地址获取账户信息/get account info according to address

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链id  |  是   |
| address | string | 账户地址 |  是   |

返回值
---
| 字段名                |  字段类型  | 参数描述  |
| ------------------ |:------:| ----- |
| address            | string | 账户地址  |
| alias              | string | 别名    |
| pubkeyHex          | string | 公钥    |
| encryptedPrikeyHex | string | 已加密私钥 |

ac\_importAccountByPriKey
=========================
### scope:public
### version:1.0
根据私钥导入账户/Import accounts by private key

参数列表
----
| 参数名       |  参数类型   | 参数描述         | 是否非空 |
| --------- |:-------:| ------------ |:----:|
| chainId   |   int   | 链id          |  是   |
| password  | string  | 设置新密码        |  是   |
| priKey    | string  | 账户私钥         |  是   |
| overwrite | boolean | 如果账户已存在,是否覆盖 |  是   |

返回值
---
| 字段名     |  字段类型  | 参数描述    |
| ------- |:------:| ------- |
| address | string | 导入的账户地址 |

ac\_importAccountByKeystore
===========================
### scope:public
### version:1.0
根据AccountKeyStore导入账户/Import accounts by AccountKeyStore

参数列表
----
| 参数名       |  参数类型   | 参数描述         | 是否非空 |
| --------- |:-------:| ------------ |:----:|
| chainId   |   int   | 链id          |  是   |
| password  | string  | 设置新密码        |  是   |
| keyStore  | string  | keyStore字符串  |  是   |
| overwrite | boolean | 如果账户已存在,是否覆盖 |  是   |

返回值
---
| 字段名     |  字段类型  | 参数描述    |
| ------- |:------:| ------- |
| address | string | 导入的账户地址 |

ac\_exportAccountKeyStore
=========================
### scope:public
### version:1.0
账户备份，导出AccountKeyStore字符串/export account KeyStore

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 账户密码 |  是   |
| filePath | string | 备份地址 |  否   |

返回值
---
| 字段名  |  字段类型  | 参数描述      |
| ---- |:------:| --------- |
| path | string | 实际备份文件的地址 |

ac\_updateOfflineAccountPassword
================================
### scope:public
### version:1.0
离线账户修改密码/Offline account change password

参数列表
----
| 参数名         |  参数类型  | 参数描述  | 是否非空 |
| ----------- |:------:| ----- |:----:|
| chainId     |  int   | 链id   |  是   |
| address     | string | 账户地址  |  是   |
| password    | string | 账户旧密码 |  是   |
| newPassword | string | 账户新密码 |  是   |
| priKey      | string | 账户私钥  |  是   |

返回值
---
| 字段名             |  字段类型  | 参数描述       |
| --------------- |:------:| ---------- |
| encryptedPriKey | string | 返回修改后加密的私钥 |

ac\_getPubKey
=============
### scope:public
### version:1.0
根据账户地址和密码,查询账户公钥/Get the account's public key

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 账户密码 |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述 |
| ------ |:------:| ---- |
| pubKey | string | 公钥   |

ac\_setRemark
=============
### scope:public
### version:1.0
为账户设置备注/Set remark for accounts

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链id  |  是   |
| address | string | 账户地址 |  是   |
| remark  | string | 备注   |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 是否成功 |

ac\_createAccount
=================
### scope:public
### version:1.0
创建指定个数的账户/create a specified number of accounts

参数列表
----
| 参数名      |  参数类型  | 参数描述      | 是否非空 |
| -------- |:------:| --------- |:----:|
| chainId  |  int   | 链id       |  是   |
| count    |  int   | 需要创建账户的数量 |  是   |
| password | string | 账户密码      |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述      |
| ---- |:---------------:| --------- |
| list | list&lt;string> | 创建的账户地址集合 |

ac\_exportKeyStoreJson
======================
### scope:public
### version:1.0
导出AccountKeyStore字符串/export account KeyStore json

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 账户密码 |  是   |

返回值
---
| 字段名      |  字段类型  | 参数描述        |
| -------- |:------:| ----------- |
| keyStore | string | keyStore字符串 |

ac\_validationPassword
======================
### scope:public
### version:1.0
验证账户密码是否正确/Verify that the account password is correct

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 账户密码 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述     |
| ----- |:-------:| -------- |
| value | boolean | 账户密码是否正确 |

ac\_getAllPriKey
================
### scope:public
### version:1.0
获取所有本地账户账户私钥，必须保证所有账户密码一致，如果本地账户中的密码不一致，将返回错误信息/Get the all local private keys. if the password in the local account is different, the error message will be returned.

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| password | string | 账户密码 |  是   |

返回值
---
| 字段名  |      字段类型       | 参数描述 |
| ---- |:---------------:| ---- |
| list | list&lt;string> | 私钥集合 |

ac\_getAccountList
==================
### scope:public
### version:1.0
获取所有账户集合,并放入缓存/query all account collections and put them in cache

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名                                                                |      字段类型       | 参数描述   |
| ------------------------------------------------------------------ |:---------------:| ------ |
| list                                                               | list&lt;object> | 返回账户集合 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address            |     string      | 账户地址   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias              |     string      | 别名     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubkeyHex          |     string      | 公钥     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPrikeyHex |     string      | 已加密私钥  |

ac\_getPriKeyByAddress
======================
### scope:public
### version:1.0
通过账户地址和密码,查询账户私匙/Inquire the account's private key according to the address

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 账户密码 |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述 |
| ------ |:------:| ---- |
| priKey | string | 私钥   |
| pubKey | string | 公钥   |

ac\_getAddressList
==================
### scope:public
### version:1.0
分页查询账户地址列表/Paging query account address list

参数列表
----
| 参数名        | 参数类型 | 参数描述   | 是否非空 |
| ---------- |:----:| ------ |:----:|
| chainId    | int  | 链id    |  是   |
| pageNumber | int  | 页码     |  是   |
| pageSize   | int  | 每一页记录数 |  是   |

返回值
---
| 字段名 |      字段类型       | 参数描述            |
| --- |:---------------:| --------------- |
| 返回值 | list&lt;string> | 返回一个Page对象，账户集合 |

ac\_removeAccount
=================
### scope:public
### version:1.0
移除指定账户/Remove specified account

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 账户密码 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 是否成功 |

ac\_updatePassword
==================
### scope:public
### version:1.0
根据原密码修改账户密码/Modify the account password by the original password

参数列表
----
| 参数名         |  参数类型  | 参数描述  | 是否非空 |
| ----------- |:------:| ----- |:----:|
| chainId     |  int   | 链id   |  是   |
| address     | string | 账户地址  |  是   |
| password    | string | 账户旧密码 |  是   |
| newPassword | string | 账户新密码 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否设置成功 |

ac\_signDigest
==============
### scope:public
### version:1.0
数据摘要签名/Data digest signature

参数列表
----
| 参数名      |  参数类型  | 参数描述  | 是否非空 |
| -------- |:------:| ----- |:----:|
| chainId  |  int   | 链id   |  是   |
| address  | string | 账户地址  |  是   |
| password | string | 账户密码  |  是   |
| data     | string | 待签名数据 |  是   |

返回值
---
| 字段名       |  字段类型  | 参数描述  |
| --------- |:------:| ----- |
| signature | string | 签名后数据 |

ac\_signBlockDigest
===================
### scope:public
### version:1.0
区块数据摘要签名/Block data digest signature

参数列表
----
| 参数名      |  参数类型  | 参数描述  | 是否非空 |
| -------- |:------:| ----- |:----:|
| chainId  |  int   | 链id   |  是   |
| address  | string | 账户地址  |  是   |
| password | string | 账户密码  |  是   |
| data     | string | 待签名数据 |  是   |

返回值
---
| 字段名       |  字段类型  | 参数描述  |
| --------- |:------:| ----- |
| signature | string | 签名后数据 |

ac\_verifySignData
==================
### scope:public
### version:1.0
验证数据签名/Verification Data Signature

参数列表
----
| 参数名    |  参数类型  | 参数描述  | 是否非空 |
| ------ |:------:| ----- |:----:|
| pubKey | string | 账户公钥  |  是   |
| sig    | string | 签名    |  是   |
| data   | string | 待签名数据 |  是   |

返回值
---
| 字段名       |  字段类型   | 参数描述   |
| --------- |:-------:| ------ |
| signature | boolean | 签名是否正确 |

ac\_addAddressPrefix
====================
### scope:public
### version:1.0
添加地址前缀,链管理模块会调用该接口

参数列表
----
| 参数名                                                           |  参数类型   | 参数描述    | 是否非空 |
| ------------------------------------------------------------- |:-------:| ------- |:----:|
| prefixList                                                    |  list   | 链地址前缀列表 |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainId       | integer | 链id     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addressPrefix | string  | 地址前缀    |  是   |

返回值
---
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

ac\_getAllAddressPrefix
=======================
### scope:public
### version:1.0
获取所有链的地址前缀

参数列表
----
无参数

返回值
---
| 字段名           |  字段类型   | 参数描述 |
| ------------- |:-------:| ---- |
| chainId       | integer | 链id  |
| addressPrefix | string  | 地址前缀 |

ac\_getAddressPrefixByChainId
=============================
### scope:public
### version:1.0
通过链id获取地址前缀

参数列表
----
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链id  |  是   |

返回值
---
| 字段名           |  字段类型   | 参数描述 |
| ------------- |:-------:| ---- |
| chainId       | integer | 链id  |
| addressPrefix | string  | 地址前缀 |

ac\_getAliasByAddress
=====================
### scope:public
### version:1.0
根据地址获取别名/get the alias by address

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链id  |  是   |
| address | string | 账户地址 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述 |
| ----- |:------:| ---- |
| alias | string | 别名   |

ac\_isAliasUsable
=================
### scope:public
### version:1.0
检查别名是否可用/check whether the account is usable

参数列表
----
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链id  |  是   |
| alias   | string | 别名   |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述     |
| ----- |:-------:| -------- |
| value | boolean | 别名是否可以使用 |

ac\_setAlias
============
### scope:public
### version:1.0
设置别名/Set the alias of account

参数列表
----
| 参数名      |  参数类型  | 参数描述 | 是否非空 |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | 链id  |  是   |
| address  | string | 账户地址 |  是   |
| password | string | 账户密码 |  是   |
| alias    | string | 别名   |  是   |

返回值
---
| 字段名    |  字段类型  | 参数描述       |
| ------ |:------:| ---------- |
| txHash | string | 设置别名交易hash |

ac\_transfer
============
### scope:public
### version:1.0
创建普通转账交易/create transfer transaction

参数列表
----
| 参数名                                                           |    参数类型    | 参数描述                          | 是否非空 |
| ------------------------------------------------------------- |:----------:| ----------------------------- |:----:|
| chainId                                                       |    int     | 链id                           |  是   |
| inputs                                                        |    list    | 交易支付方数据                       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | 账户地址                          |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | 资产的链ID                        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | 资产ID                          |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | 数量                            |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password      |   string   | 转出账户(from)的密码, 组装接收方(to)数据时忽略 |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime      |    long    | 解锁时间, -1为一直锁定, 0为不锁定(默认)      |  否   |
| outputs                                                       |    list    | 交易接受方数据                       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | 账户地址                          |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | 资产的链ID                        |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | 资产ID                          |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | 数量                            |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password      |   string   | 转出账户(from)的密码, 组装接收方(to)数据时忽略 |  否   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime      |    long    | 解锁时间, -1为一直锁定, 0为不锁定(默认)      |  否   |
| remark                                                        |   string   | 交易备注                          |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述   |
| ----- |:------:| ------ |
| value | string | 交易hash |

ac\_createMultiSignTransfer
===========================
### scope:public
### version:1.0
创建多签地址转账交易/create multi sign transfer

参数列表
----
| 参数名                                                           |    参数类型    | 参数描述                     | 是否非空 |
| ------------------------------------------------------------- |:----------:| ------------------------ |:----:|
| chainId                                                       |    int     | 链id                      |  是   |
| inputs                                                        |    list    | 交易支付方数据                  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | 账户地址                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | 资产的链ID                   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | 资产ID                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | 数量                       |  是   |
| outputs                                                       |    list    | 交易接受方数据                  |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | 账户地址                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | 资产的链ID                   |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | 资产ID                     |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | 数量                       |  是   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime      |    long    | 解锁时间, -1为一直锁定, 0为不锁定(默认) |  否   |
| remark                                                        |   string   | 交易备注                     |  是   |
| signAddress                                                   |   string   | 第一个签名账户地址(不填则只创建交易不签名)   |  否   |
| signPassword                                                  |   string   | 第一个签名账户密码(不填则只创建交易不签名)   |  否   |

返回值
---
| 字段名       |  字段类型   | 参数描述                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | 完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)    |
| txHash    | string  | 交易hash,交易已完成(已广播)                     |
| completed | boolean | true:交易已完成(已广播),false:交易没完成,没有达到最小签名数 |

ac\_signMultiSignTransaction
============================
### scope:public
### version:1.0
多签交易签名/sign MultiSign Transaction

参数列表
----
| 参数名          |  参数类型  | 参数描述    | 是否非空 |
| ------------ |:------:| ------- |:----:|
| chainId      |  int   | 链id     |  是   |
| tx           | string | 交易数据字符串 |  是   |
| signAddress  | string | 签名账户地址  |  是   |
| signPassword | string | 签名账户密码  |  是   |

返回值
---
| 字段名       |  字段类型   | 参数描述                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | 完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)    |
| txHash    | string  | 交易hash,交易已完成(已广播)                     |
| completed | boolean | true:交易已完成(已广播),false:交易没完成,没有达到最小签名数 |

ac\_setMultiSignAlias
=====================
### scope:public
### version:1.0
设置多签账户别名/set the alias of multi sign account

参数列表
----
| 参数名          |  参数类型  | 参数描述                   | 是否非空 |
| ------------ |:------:| ---------------------- |:----:|
| chainId      |  int   | 链id                    |  是   |
| address      | string | 多签账户地址                 |  是   |
| alias        | string | 别名                     |  是   |
| signAddress  | string | 第一个签名账户地址(不填则只创建交易不签名) |  否   |
| signPassword | string | 第一个签名账户密码(不填则只创建交易不签名) |  否   |

返回值
---
| 字段名       |  字段类型   | 参数描述                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | 完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)    |
| txHash    | string  | 交易hash,交易已完成(已广播)                     |
| completed | boolean | true:交易已完成(已广播),false:交易没完成,没有达到最小签名数 |

ac\_createMultiSignAccount
==========================
### scope:public
### version:1.0
创建多签账户/create a multi sign account

参数列表
----
| 参数名      |      参数类型       | 参数描述                            | 是否非空 |
| -------- |:---------------:| ------------------------------- |:----:|
| chainId  |       int       | 链id                             |  是   |
| pubKeys  | list&lt;string> | 公钥集合(任意普通地址的公钥或存在于当前节点中的普通账户地址) |  是   |
| minSigns |       int       | 最小签名数                           |  是   |

返回值
---
| 字段名     |  字段类型  | 参数描述   |
| ------- |:------:| ------ |
| address | string | 多签账户地址 |

ac\_removeMultiSignAccount
==========================
### scope:public
### version:1.0
移除多签账户/remove the multi sign account

参数列表
----
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链id    |  是   |
| address | string | 多签账户地址 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述   |
| ----- |:-------:| ------ |
| value | boolean | 是否移除成功 |

ac\_getMultiSignAccount
=======================
### scope:public
### version:1.0
根据多签账户地址获取完整多签账户/Search for multi-signature account by address

参数列表
----
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链id    |  是   |
| address | string | 多签账户地址 |  是   |

返回值
---
| 字段名   |  字段类型  | 参数描述         |
| ----- |:------:| ------------ |
| value | string | 多签账户序列化数据字符串 |

ac\_isMultiSignAccountBuilder
=============================
### scope:public
### version:1.0
验证是否多签账户的创建者之一/Whether it is multiSign account Builder

参数列表
----
| 参数名     |  参数类型  | 参数描述              | 是否非空 |
| ------- |:------:| ----------------- |:----:|
| chainId |  int   | 链id               |  是   |
| address | string | 多签账户地址            |  是   |
| pubKey  | string | 创建者公钥或已存在于当前节点的地址 |  是   |

返回值
---
| 字段名   |  字段类型   | 参数描述         |
| ----- |:-------:| ------------ |
| value | boolean | 是否多签账户的创建者之一 |

