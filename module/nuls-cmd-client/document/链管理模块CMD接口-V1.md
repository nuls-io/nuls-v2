# 链管理模块CMD接口

[TOC]

[^]: 

#### 1 注册一条新的友链

* 功能说明：

  注册新的友链到NULS主网。

  注册一条新友链必须含一个注册资产。


- 接口定义

  - 接口说明

  ​        向链管理模块注册友链信息。

  ​        method : cm_chainReg

  - 请求示例

  ```
  {
          "chainId": "链标识",
          "assetId": 2,
          "chainName": "nuls chain",
          "addressType": "1",
          "magicNumber":454546,
          "minAvailableNodeNum":5,
          "txConfirmedBlockNum":30,
          "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
          "symbol":"NULS",
          "assetName":"纳斯",
          "initNumber":"1000000000",
          "decimalPlaces":8,
          "password":"xxxxxxxxxxxxx"
          
  }
  ```
  - 请求参数说明

  | parameter           | required | type   | description                                 |
  | :------------------ | :------- | :----- | ------------------------------------------- |
  | chainId             | true     | int    | 链标识                                      |
  | assetId             | true     | int    | 资产id                                      |
  | chainName           | true     | string | 链名称                                      |
  | addressType         | true     | int    | 链上创建的账户的地址类型：1生态内 2非生态内 |
  | magicNumber         | true     | string | 网络魔法参数                                |
  | minAvailableNodeNum | true     | int    | 最小可用节点数量                            |
  | txConfirmBlockNum   | true     | int    | 交易确认块数                                |
  | symbol              | true     | string | 资产符号                                    |
  | assetName           | true     | string | 资产名称                                    |
  | initNumber          | true     | string | 资产初始值                                  |
  | decimalPlaces       | true     | int    | 最小资产可分割位数                          |
  | address             | true     | string | 创建链的主网地址                            |
  | password            | true     | string | 私钥对应的密码                              |

  - 返回示例

     Failed

     ```
     统一RPC标准格式
     
     ```

     Success

     ```
     统一RPC标准格式
     
     ```

  - 返回字段说明

  | parameter | type   | description |
  | --------- | ------ | ----------- |
  | txHash    | String | 交易hash    |




#### 2.注销已经存在的友链

- 功能说明：

  注销已经存在的友链。




1. 链是随资产而创建，所以注销链必须进行资产校验，只有删除最后一条资产，链才会随着一起注销。

 2.判断是否允许注销的条件：

​     资产与链存在。

​    随链只有最后一个资产。

​    链资产有n%资产在自有主链上。 

 3.链管理模块进行链交易的封装发送给交易模块。

​     期间需要通过账本模块获取账户余额及交易nonce值。

 4.交易模块会在交易处理过程中进行数据校验的回调。

 5.链管理模块通过 交易模块回调 “提交链注销交易”的接口 来进行注销数据提交。

 6.链管理模块存储数据并将注册信息下发给网络模块。

 7.删除链随注销的资产，将退回抵押押金的80%。

- 接口定义

  - 接口说明

  ​        向链管理模块注销友链信息（调用的是资产注销接口，因为链随最后资产一起注销）。

  ​        method : cm_assetDisable

  - 请求示例

  ```
  {
          "chainId": 152,
          "assetId": 45,
          "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
          "password":"xxxxxxxxxxxxx"
          
  }
  ```

  - 请求参数说明

  | parameter | required | type   | description          |
  | :-------- | :------- | :----- | -------------------- |
  | chainId   | true     | int    | 链标识               |
  | assetId   | true     | int    | 资产id               |
  | address   | true     | string | 创建链的主网账户地址 |
  | password  | true     | string | 私钥对应的密码       |

  - 返回示例

    Failed

    ```
    统一RPC标准格式
    
    ```

    Success

    ```
    统一RPC标准格式
    
    ```

  - 返回字段说明

  | parameter | type   | description |
  | --------- | ------ | ----------- |
  | txHash    | string | 交易hash    |



#### 3  增加资产信息

- 功能说明：

  已有链上登记注册资产。


- 接口定义

  - 接口说明

  ​        向链管理模块注册资产信息。

  ​        method : cm_assetReg

  - 请求示例

  ```
  {
          "chainId": 152,
          "assetId":85,
          "symbol":"NULS",
          "assetName":"纳斯",
          "initNumber":"1000000000",
          "decimalPlaces":8,
           "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
          "password":"xxxxxxxxxxxxx"
          
  }
  ```

  - 请求参数说明

  | parameter     | required | type   | description        |
  | :------------ | :------- | :----- | ------------------ |
  | chainId       | true     | int    | 链标识             |
  | symbol        | true     | string | 资产符号           |
  | assetName     | true     | string | 资产名称           |
  | initNumber    | true     | string | 资产初始值         |
  | decimalPlaces | true     | int    | 最小资产可分割位数 |
  | address       | true     | string | 创建链的主网地址   |
  | password      | true     | string | 私钥对应的密码     |

  - 返回示例

    Failed

    ```
    统一RPC标准格式
    ```

    Success

    ```
    统一RPC标准格式
    ```

  - 返回字段说明

  | parameter | type   | description |
  | --------- | ------ | ----------- |
  | txHash    | String | 交易hash值  |



#### 4 已登记友链删除资产类型

- 功能说明：

  对指定友链销毁资产。

- 接口定义

  - 接口说明

  ​        向链管理模块注销资产信息。

  ​        method : cm_assetDisable

  - 请求示例

  ```
  {
          "chainId": 152,
          "assetId": 45,
          "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
          "password":"xxxxxxxxxxxxx"
          
  }
  ```

  - 请求参数说明

  | parameter | required | type   | description          |
  | :-------- | :------- | :----- | -------------------- |
  | chainId   | true     | int    | 链标识               |
  | assetId   | true     | int    | 资产id               |
  | address   | true     | string | 创建链的主网账户地址 |
  | password  | true     | string | 私钥对应的密码       |

  - 返回示例

    Failed

    ```
    统一RPC标准格式
    
    ```

    Success

    ```
    统一RPC标准格式
    
    ```

  - 返回字段说明

  | parameter | type   | description  |
  | --------- | ------ | ------------ |
  | txHash    | String | 删除指定资产 |

#### 5  查询注册链信息

- 功能说明：

  查询注册链信息

- 流程描述

​        无

- 接口定义

  - 接口说明

  ​        查询注册友链信息。

  ​        method : cm_chain

  - 请求示例

  ```
  {
     "chainId":4545 
  }
  ```

  - 请求参数说明

  | parameter | required | type | description |
  | :-------- | :------- | :--- | ----------- |
  | chainId   | true     | int  | 链标识      |

  - 返回示例

    Failed

    ```
    统一RPC标准格式
    
    ```

    Success

    ```
    {
            "chainId": 152,
            "chainName": "nuls chain",
            "addressType": 1,
            "magicNumber":454546,
            "minAvailableNodeNum":5,
            "txConfirmedBlockNum":30,
            "regAddress":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
            "regTxHash":"FFFFF", 
            "selfAssetKeyList":["1232_32","528_8"],
            "totalAssetKeyList":["1232_32","528_8"],
            "createTime":1212131,
            "seeds":"xxx.xxx.xxx.xxx:8001,xxx.xxx.xxx.xxx:8002"
    }
    ```

  - 返回字段说明

  | parameter           | type   | description                                           |
  | :------------------ | :----- | ----------------------------------------------------- |
  | chainId             | int    | 链标识                                                |
  | chainName           | string | 链名称                                                |
  | addressType         | int    | 链上创建的账户的地址类型：1生态内 2非生态内           |
  | magicNumber         | string | 网络魔法参数                                          |
  | minAvailableNodeNum | int    | 最小可用节点数量                                      |
  | txConfirmBlockNum   | int    | 交易确认块数                                          |
  | regTxHash           | string | 交易hash                                              |
  | regAddress          | string | 创建链的主网地址                                      |
  | selfAssetKeyList    | list   | 链下注册的资产列表，由chainId_assetId 组合的资产key值 |
  | totalAssetKeyList   | list   | 链下流通的资产列表，由chainId_assetId 组合的资产key值 |
  | createTime          | long   | 创建时间                                              |
  | seeds               | String | cross Seed node                                       |



#### 6. 查询链下资产信息

- 功能说明：

  查询某链资产信息。

- 流程描述

​        无

- 接口定义

  - 接口说明

  ​        向链管理模块查询某资产信息。

  ​        method : cm_asset

  - 请求示例

  ```
  {
     "chainId":4545， 
     "assetId":45
  }
  ```

  - 请求参数说明

  | parameter | required | type | description |
  | :-------- | :------- | :--- | ----------- |
  | chainId   | true     | int  | 链标识      |
  | assetId   | true     | int  | 资产id      |

  - 返回示例

    Failed

    ```
    统一RPC标准格式
    
    ```

    Success

    ```
    {
            "chainId": 152,
            "assetId":85,
            "symbol":"NULS",
            "assetName":"纳斯",
            "initNumber":"1000000000",
            "decimalPlaces":8,
            "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
            "txHash":"xxxxxxxxxxxxx",
            "createTime":125848
            }
    ```

  - 返回字段说明

  | parameter     | type   | description        |
  | :------------ | :----- | ------------------ |
  | chainId       | int    | 链标识             |
  | symbol        | string | 资产符号           |
  | assetName     | string | 资产名称           |
  | initNumber    | string | 资产初始值         |
  | decimalPlaces | int    | 最小资产可分割位数 |
  | address       | string | 创建链的主网地址   |
  | txHash        | string | 交易hash           |
  | createTime    | long   | 创建时间           |


#### 7. 创建跨链交易

- 功能说明：

  创建一笔跨链交易

- 流程描述

​        无

- 接口定义

  - 接口说明

  ​        发起一笔跨链交易

  ​        method : createCrossTx

  - 请求示例

  ```
  {
     "chainId":2, 
     "remark":"跨链转账交易",
     "listFrom":[
     	{
         "address":"8CPcA7kaXSHbWb3GHP7bd5hRLFu8RZv57rY9w",
         "assetsChainId":2,
         "assetsId":1,
         "amount":100000000,
         "password":"nuls123456"
     	}
     ],
     "listTo":[
     	{
         "address":"8CPcA7kaXSHbWb3GHP7bd5hRLFu8RZv57rY9w",
         "assetsChainId":2,
         "assetsId":1,
         "amount":100000000,
         "password":"nuls123456"
     	}
     ]
  }
  ```

  - 请求参数说明

  | parameter     | required | type       | description |
  | :------------ | :------- | :--------- | ----------- |
  | chainId       | true     | int        | 链标识      |
  | remark        | false    | String     | 备注        |
  | address       | true     | String     | 账户地址    |
  | assetsChainId | true     | int        | 资产链ID    |
  | assetsId      | true     | int        | 资产ID      |
  | amount        | true     | BigInteger | 交易金额    |
  | password      | true     | String     | 账户密码    |



  - 返回示例

    Failed

    ```
    统一RPC标准格式
    
    ```

    Success

    ```
    {
           "txHash":"8JHSFGSGFSDHDHDHJGASVXCBCN" 
    }
    ```

  - 返回字段说明

  | parameter | type   | description |
  | :-------- | :----- | ----------- |
  | txHash    | string | 交易hash    |


#### 8.查询跨链交易处理状态

- 功能说明：

  查询跨链交易在其他链的处理状态

- 流程描述

​        无

- 接口定义

  - 接口说明

  ​        查询跨链交易在其他链的处理状态

  ​        method : getCrossTxState

  - 请求示例

    ```
    {
        "chainId":2, 
        "txHash":"8JHSFGSGFSDHDHDHJGASVXCBCN" 
    }
    ```

  - 参数说明

    | parameter | type   | required | description |
    | --------- | ------ | -------- | ----------- |
    | chainId   | int    | true     | 链ID        |
    | txHash    | string | true     | 交易hash    |

  - 返回示例

    Failed

    ```
    {
        "value":false
    }
    ```

    Success

    ```
    {
        "value":true
    }
    ```

  - 返回参数说明

    | parameter | type    | description          |
    | --------- | ------- | -------------------- |
    | value     | boolean | 跨链交易处理是否完成 |


