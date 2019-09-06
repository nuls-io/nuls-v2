# 链管理模块

## 为什么要有《链管理》模块

在NULS 1.0中，只有一条链（NULS主网），因此不需要链管理模块。

在NULS 2.0中，NULS主网上可以注册其他友链信息，包括:        

- NULS生态圈中的链：与NULS主网使用同一套代码衍生出来。
- 其他链：比特币、以太坊等

《链管理》模块用来管理所有加入NULS主网的友链的信息

名词解释：

- NULS主网：不同于NULS 1.0，是独立运行的另一条链，也称之为NULS 2.0。
  《链管理》是NULS主网的其中一个模块
- 友链：在NULS主网上注册的其他链

假设1：友链A，其拥有资产A

假设2：友链B，其拥有资产B

- 跨链交易：
  - 友链A把资产A转到友链B
  - 友链B内部转移资产A
  - 友链B把资产A转回到友链A
  - 友链B把资产A转到其他友链（C,D等）
- 非跨链交易：
  - 友链A内部转移资产A
  - 友链B内部转移资产B

备注：不论链内资产，还是链外资产，只要资产跨链进行交易，就需要主网进行确认。

## 《链管理》要做什么

《链管理》模块用来管理加入NULS主网的链的基本信息，包括：

* 注册一条新的友链
* 销毁已经存在的友链
* 查询友链信息
* 特定友链增加资产类型
* 特定友链销毁资产类型
* 跨链资产校验

## 《链管理》在系统中的定位

《链管理》强依赖的模块：

- 核心模块
- 网络模块
- 交易管理模块
- 账本模块

《链管理》弱依赖的模块：

- 事件总线模块



## 接口列表
### cm\_chainReg
链注册-用于平行链的跨链注册
#### scope:public
#### version:1.0

#### 参数列表
| 参数名                 |      参数类型       | 参数描述                    | 是否非空 |
| ------------------- |:---------------:| ----------------------- |:----:|
| chainId             |       int       | 资产链Id,取值区间[3-65535]     |  是   |
| chainName           |     string      | 链名称                     |  是   |
| addressType         |       int       | 1 使用NULS框架构建的链 生态内，2生态外 |  是   |
| addressPrefix       |     string      | 链地址前缀,1-5字符             |  是   |
| magicNumber         |      long       | 网络魔法参数                  |  是   |
| minAvailableNodeNum |       int       | 最小连接数                   |  是   |
| assetId             |       int       | 资产Id,取值区间[1-65535]      |  是   |
| symbol              |     string      | 资产符号                    |  是   |
| assetName           |     string      | 资产名称                    |  是   |
| initNumber          |     string      | 资产初始值                   |  是   |
| decimalPlaces       |      short      | 资产小数点位数                 |  是   |
| address             |     string      | 创建交易的账户地址               |  是   |
| password            |     string      | 账户密码                    |  是   |
| verifierList        | list&lt;string> | 验证者名单列表                 |  是   |
| signatureBFTRatio   |     integer     | 拜占庭比例,大于等于该值为有效确认       |  是   |
| maxSignatureCount   |     integer     | 最大签名数量,限制验证者签名列表的最大数    |  是   |

#### 返回值
| 字段名                  |  字段类型  | 参数描述           |
| -------------------- |:------:| -------------- |
| txHash               | string | 交易hash值        |
| mainNetVerifierList  | string | 主网验证人列表,逗号分隔   |
| mainNetCrossSeedList | string | 主网验种子节点列表,逗号分隔 |

### cm\_chainActive
链更新激活-用于平行链的跨链更新激活（激活之前注销的链）
#### scope:public
#### version:1.0

#### 参数列表
| 参数名                 |      参数类型       | 参数描述                    | 是否非空 |
| ------------------- |:---------------:| ----------------------- |:----:|
| chainId             |       int       | 资产链Id,取值区间[1-65535]     |  是   |
| chainName           |     string      | 链名称                     |  是   |
| addressType         |       int       | 1 使用NULS框架构建的链 生态内，2生态外 |  是   |
| addressPrefix       |     string      | 链地址前缀,1-5字符             |  是   |
| magicNumber         |      long       | 网络魔法参数                  |  是   |
| minAvailableNodeNum |       int       | 最小连接数                   |  是   |
| assetId             |       int       | 资产Id,取值区间[1-65535]      |  是   |
| symbol              |     string      | 资产符号                    |  是   |
| assetName           |     string      | 资产名称                    |  是   |
| initNumber          |     string      | 资产初始值                   |  是   |
| decimalPlaces       |      short      | 资产小数点位数                 |  是   |
| address             |     string      | 创建交易的账户地址               |  是   |
| password            |     string      | 账户密码                    |  是   |
| verifierList        | list&lt;string> | 验证者名单列表                 |  是   |
| signatureBFTRatio   |     integer     | 拜占庭比例,大于等于该值为有效确认       |  是   |
| maxSignatureCount   |     integer     | 最大签名数量,限制验证者签名列表的最大数    |  是   |

#### 返回值
| 字段名                      |  字段类型  | 参数描述           |
| ------------------------ |:------:| -------------- |
| txHash                   | string | 交易hash值        |
| mainNetVerifierSeeds     | string | 主网验证人种子列表,逗号分隔 |
| mainNetCrossConnectSeeds | string | 主网验种子节点列表,逗号分隔 |

### cm\_getChainsSimpleInfo
获取跨链已注册链列表
#### scope:public
#### version:1.0

#### 参数列表
无参数

#### 返回值
| 字段名        |     字段类型     | 参数描述          |
| ---------- |:------------:| ------------- |
| chainInfos | list&lt;map> | 返回链及资产的简要信息列表 |

### getCrossChainInfos
获取跨链注册资产信息
#### scope:public
#### version:1.0

#### 参数列表
无参数

#### 返回值
| 字段名                                                                 |      字段类型       | 参数描述         |
| ------------------------------------------------------------------- |:---------------:| ------------ |
| chainInfos                                                          | list&lt;object> | 已注册的链与资产信息列表 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainId             |       int       | 链id          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainName           |     string      | 链名称          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minAvailableNodeNum |       int       | 最小连接数        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetInfoList       |      list       | 资产信息列表       |

### cm\_chain
查看链信息
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     | 参数类型 | 参数描述                | 是否非空 |
| ------- |:----:| ------------------- |:----:|
| chainId | int  | 资产链Id,取值区间[1-65535] |  是   |

#### 返回值
| 字段名                      |  字段类型   | 参数描述                         |
| ------------------------ |:-------:| ---------------------------- |
| chainId                  |   int   | 链id                          |
| chainName                | string  | 链名称                          |
| addressType              | string  | 地址类型（1：Nuls生态，2：其他）          |
| addressPrefix            | string  | 地址前缀                         |
| magicNumber              |  long   | 魔法参数                         |
| minAvailableNodeNum      |   int   | 最小可用节点数                      |
| txConfirmedBlockNum      |   int   | 交易确认区块数                      |
| isDelete                 | boolean | 是否已注销                        |
| createTime               |  long   | 创建时间                         |
| regAddress               | string  | 注册链时使用的地址                    |
| regTxHash                | string  | 注册链时的交易哈希                    |
| regAssetId               |   int   | 注册链时添加的资产序号                  |
| selfAssetKeyList         |  list   | 本链创建的所有资产，Key=chaiId_assetId |
| totalAssetKeyList        |  list   | 链上流通的所有资产，Key=chaiId_assetId |
| verifierList             |  list   | 验证人列表                        |
| signatureByzantineRatio  |   int   | 拜占庭比例                        |
| maxSignatureCount        |   int   | 最大签名数量                       |
| mainNetVerifierSeeds     | string  | 主网验证人列表,逗号分隔                 |
| mainNetCrossConnectSeeds | string  | 跨链提供的主网连接种子,逗号分隔             |
| enable                   | boolean | 是否可用                         |

### cm\_getCirculateChainAsset
查询资产信息
#### scope:public
#### version:1.0

#### 参数列表
| 参数名              |  参数类型  | 参数描述                 | 是否非空 |
| ---------------- |:------:| -------------------- |:----:|
| circulateChainId | string | 运行的链ID,取值区间[1-65535] |  是   |
| assetChainId     | string | 资产链Id,取值区间[1-65535]  |  是   |
| assetId          | string | 资产Id,取值区间[1-65535]   |  是   |

#### 返回值
| 字段名              |    字段类型    | 参数描述   |
| ---------------- |:----------:| ------ |
| circulateChainId |  integer   | 运行的链ID |
| assetChainId     |  integer   | 资产链ID  |
| assetId          |  integer   | 资产ID   |
| initNumber       | biginteger | 初始资产数量 |
| chainAssetAmount | biginteger | 现有资产数量 |

### cm\_assetCirculateCommit
查询资产信息
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |      参数类型       | 参数描述                 | 是否非空 |
| ----------- |:---------------:| -------------------- |:----:|
| chainId     |       int       | 运行的链ID,取值区间[1-65535] |  是   |
| txList      | list&lt;string> | 交易Hex值列表             |  是   |
| blockHeader |     string      | 区块头Hex值              |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即提交成功 |

### cm\_assetCirculateRollBack
查询资产信息
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |      参数类型       | 参数描述                 | 是否非空 |
| ----------- |:---------------:| -------------------- |:----:|
| chainId     |       int       | 运行的链ID,取值区间[1-65535] |  是   |
| txList      | list&lt;string> | 交易Hex值列表             |  是   |
| blockHeader |     string      | 区块头Hex值              |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即验证成功 |

### updateChainAsset
查询资产信息
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     |     参数类型     | 参数描述                | 是否非空 |
| ------- |:------------:| ------------------- |:----:|
| chainId |     int      | 资产链ID,取值区间[1-65535] |  是   |
| assets  | list&lt;int> | 资产id列表              |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即验证成功 |

### cm\_assetCirculateValidator
查询资产信息
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     |  参数类型  | 参数描述                 | 是否非空 |
| ------- |:------:| -------------------- |:----:|
| chainId | string | 运行的链ID,取值区间[1-65535] |  是   |
| tx      | string | 交易Hex值               |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述             |
| --- |:----:| ---------------- |
| N/A | void | 无特定返回值，没有错误即验证成功 |

### cm\_assetReg
资产注册
#### scope:public
#### version:1.0

#### 参数列表
| 参数名           |    参数类型    | 参数描述                | 是否非空 |
| ------------- |:----------:| ------------------- |:----:|
| chainId       |    int     | 资产链Id,取值区间[1-65535] |  是   |
| assetId       |    int     | 资产Id,取值区间[1-65535]  |  是   |
| symbol        |   string   | 资产符号                |  是   |
| assetName     |   string   | 资产名称                |  是   |
| initNumber    | biginteger | 资产初始值               |  是   |
| decimalPlaces |   short    | 资产小数点位数             |  是   |
| address       |   string   | 创建交易的账户地址           |  是   |
| password      |   string   | 账户密码                |  是   |

#### 返回值
| 字段名    |  字段类型  | 参数描述    |
| ------ |:------:| ------- |
| txHash | string | 交易hash值 |

### cm\_assetDisable
资产注销
#### scope:public
#### version:1.0

#### 参数列表
| 参数名      |  参数类型  | 参数描述                | 是否非空 |
| -------- |:------:| ------------------- |:----:|
| chainId  |  int   | 资产链Id,取值区间[1-65535] |  是   |
| assetId  |  int   | 资产Id,取值区间[1-65535]  |  是   |
| address  | string | 创建交易的账户地址           |  是   |
| password | string | 账户密码                |  是   |

#### 返回值
| 字段名    |  字段类型  | 参数描述    |
| ------ |:------:| ------- |
| txHash | string | 交易hash值 |

### cm\_asset
资产注册信息查询
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     | 参数类型 | 参数描述                | 是否非空 |
| ------- |:----:| ------------------- |:----:|
| chainId | int  | 资产链Id,取值区间[1-65535] |  是   |
| assetId | int  | 资产Id,取值区间[1-65535]  |  是   |

#### 返回值
| 字段名 |    字段类型     | 参数描述  |
| --- |:-----------:| ----- |
|     | regassetdto | 返回链信息 |

### cm\_getChainAsset
资产查看
#### scope:public
#### version:1.0

#### 参数列表
| 参数名          | 参数类型 | 参数描述                | 是否非空 |
| ------------ |:----:| ------------------- |:----:|
| chainId      | int  | 运行链Id,取值区间[1-65535] |  是   |
| assetChainId | int  | 资产链Id,取值区间[1-65535] |  是   |
| assetId      | int  | 资产Id,取值区间[1-65535]  |  是   |

#### 返回值
| 字段名          |    字段类型    | 参数描述  |
| ------------ |:----------:| ----- |
| chainId      |  integer   | 运行链Id |
| assetChainId |  integer   | 资产链id |
| assetId      |  integer   | 资产id  |
| asset        | biginteger | 资产值   |

