# 跨链模块



## 接口列表
### createCrossTx
创建跨链转账交易/Creating Cross-Chain Transfer Transactions
#### scope:public
#### version:1.0

#### 参数列表
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
| remark                                                        |   string   | 备注     |  否   |

#### 返回值
| 字段名    |  字段类型  | 参数描述     |
| ------ |:------:| -------- |
| txHash | string | 跨链交易HASH |

### newApiModuleCrossTx
接收API_MODULE组装的跨链交易/Receiving cross-chain transactions assembled by API_MODULE
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链ID  |  是   |
| tx      | string | 交易   |  是   |

#### 返回值
| 字段名    |  字段类型  | 参数描述   |
| ------ |:------:| ------ |
| txHash | string | 交易Hash |

### getCrossTxState
查询跨链交易处理状态/get cross transaction process state
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     |  参数类型  | 参数描述   | 是否非空 |
| ------- |:------:| ------ |:----:|
| chainId |  int   | 链ID    |  是   |
| txHash  | string | 交易HASH |  是   |

#### 返回值
| 字段名   |  字段类型   | 参数描述       |
| ----- |:-------:| ---------- |
| value | boolean | 跨链交易是否处理完成 |

### getRegisteredChainInfoList
查询在主网上注册跨链的链信息/Query for cross-chain chain information registered on the main network
#### scope:public
#### version:1.0

#### 参数列表
无参数

#### 返回值
| 字段名                                                                                                           |      字段类型       | 参数描述      |
| ------------------------------------------------------------------------------------------------------------- |:---------------:| --------- |
| list                                                                                                          | list&lt;object> | 已注册跨链的链信息 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainId                                                       |       int       | 链ID       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainName                                                     |     string      | 链名称       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minAvailableNodeNum                                           |       int       | 最小链接数     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;maxSignatureCount                                             |       int       | 最大签名数     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;signatureByzantineRatio                                       |       int       | 签名拜占庭比例   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addressPrefix                                                 |     string      | 链账户前缀     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetInfoList                                                 | list&lt;object> | 链资产列表     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId       |       int       | 资产ID      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol        |     string      | 资产符号      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetName     |     string      | 资产名称      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;usable        |     boolean     | 是否可用      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimalPlaces |       int       | 精度        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;verifierList                                                  |       set       | 验证人列表     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;registerTime                                                  |      long       | 注册时间      |

### getByzantineCount
查询当前签名拜占庭最小通过数量/查询当前签名拜占庭最小通过数量
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

#### 返回值
| 字段名   | 字段类型 | 参数描述       |
| ----- |:----:| ---------- |
| value | int  | 当前拜占庭最小签名数 |

### getChains
cancel Cross Chain
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### registerCrossChain
链注册跨链/register Cross Chain
#### scope:public
#### version:1.0

#### 参数列表
| 参数名                 |  参数类型  | 参数描述  | 是否非空 |
| ------------------- |:------:| ----- |:----:|
| chainId             |  int   | 链ID   |  是   |
| chainName           | string | 链名称   |  是   |
| minAvailableNodeNum |  int   | 最小链接数 |  是   |
| assetInfoList       | string | 资产列表  |  是   |
| registerTime        |  long  | 链注册时间 |  是   |

#### 返回值
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 处理结果 |

### cancelCrossChain
指定链资产退出跨链/Specified Chain Assets Exit Cross Chain
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |
| assetId | int  | 资产ID |  是   |

#### 返回值
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 处理结果 |

### crossChainRegisterChange
跨链注册信息变更/Registered Cross Chain change
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     | 参数类型 | 参数描述 | 是否非空 |
| ------- |:----:| ---- |:----:|
| chainId | int  | 链ID  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### recvCirculat
接收其他链节点发送的资产信息/Receiving asset information sent by other link nodes
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### registerAsset
链注册跨链/register Cross Chain
#### scope:public
#### version:1.0

#### 参数列表
| 参数名           |  参数类型   | 参数描述 | 是否非空 |
| ------------- |:-------:| ---- |:----:|
| chainId       |   int   | 链ID  |  是   |
| assetId       |   int   | 资产ID |  是   |
| symbol        | string  | 资产符号 |  是   |
| assetName     | string  | 资产名称 |  是   |
| usable        | boolean | 是否可用 |  是   |
| decimalPlaces |   int   | 精度   |  是   |

#### 返回值
| 字段名   |  字段类型   | 参数描述 |
| ----- |:-------:| ---- |
| value | boolean | 处理结果 |

### getFriendChainCirculate
获取友链资产信息/Access to Friendship Chain Asset Information
#### scope:public
#### version:1.0

#### 参数列表
| 参数名      |  参数类型  | 参数描述             | 是否非空 |
| -------- |:------:| ---------------- |:----:|
| chainId  |  int   | 链ID              |  是   |
| assetIds | string | 资产ID，多个资产ID用逗号分隔 |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### newBlockHeight
链区块高度变更/receive new block height
#### scope:public
#### version:1.0

#### 参数列表
| 参数名     |  参数类型  | 参数描述 | 是否非空 |
| ------- |:------:| ---- |:----:|
| chainId |  int   | 链ID  |  是   |
| height  | string | 链ID  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### recvCtxState
跨链交易处理状态消息/receive cross transaction state
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### recvCtx
接收本链节点广播的完整交易/Complete Transaction for Receiving Broadcast from Local Chain Nodes
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### recvOtherCtx
接收跨链节点广播的完整交易/Receiving Complete Transactions for Cross-Chain Node Broadcasting
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### getCtxState
获取跨链交易处理状态/Getting the state of cross-chain transaction processing
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### recvRegChain
接收到主网返回的已注册跨链交易的链信息/Receiving chain information of registered cross-chain transactions returned from the main network
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### getCirculat
查询本链资产信息消息/get chain circulation
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### recvCtxSign
接收链内节点广播的交易签名/Transaction signature for broadcasting in receiving chain
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### getCtx
链内节点向本节点获取完成跨链交易/The intra-chain node acquires and completes the cross-chain transaction from its own node
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### getOtherCtx
跨链节点向本节点获取完整交易/Cross-chain nodes obtain complete transactions from their own nodes
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

### recvCtxHash
接收跨链节点广播的交易Hash/Transaction Hash receiving cross-link node broadcasting
#### scope:public
#### version:1.0

#### 参数列表
| 参数名         |  参数类型  | 参数描述 | 是否非空 |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | 链ID  |  是   |
| nodeId      | string | 节点IP |  是   |
| messageBody | string | 消息体  |  是   |

#### 返回值
| 字段名 | 字段类型 | 参数描述           |
| --- |:----:| -------------- |
| N/A | void | 无特定返回值，没有错误即成功 |

