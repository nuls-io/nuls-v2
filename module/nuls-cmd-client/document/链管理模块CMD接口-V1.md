# Chain management moduleCMDinterface

[TOC]

[^]: 

#### 1 Register a new friend chain

* Function Description：

  Register a new friend link toNULSMain network.

  Registering a new friend chain must include one registered asset.


- Interface Definition

  - Interface Description

  ​        Register friend chain information with the chain management module.

  ​        method : cm_chainReg

  - Request Example

  ```
  {
          "chainId": "Chain identification",
          "assetId": 2,
          "chainName": "nuls chain",
          "addressType": "1",
          "magicNumber":454546,
          "minAvailableNodeNum":5,
          "txConfirmedBlockNum":30,
          "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
          "symbol":"NULS",
          "assetName":"Nass",
          "initNumber":"1000000000",
          "decimalPlaces":8,
          "password":"xxxxxxxxxxxxx"
          
  }
  ```
  - Request parameter description

  | parameter           | required | type   | description                                 |
  | :------------------ | :------- | :----- | ------------------------------------------- |
  | chainId             | true     | int    | Chain identification                                      |
  | assetId             | true     | int    | assetid                                      |
  | chainName           | true     | string | Chain Name                                      |
  | addressType         | true     | int    | The address type of the account created on the chain：1Within the ecosystem 2Non ecological interior |
  | magicNumber         | true     | string | Network Magic Parameters                                |
  | minAvailableNodeNum | true     | int    | Minimum number of available nodes                            |
  | txConfirmBlockNum   | true     | int    | Number of transaction confirmation blocks                                |
  | symbol              | true     | string | Asset symbols                                    |
  | assetName           | true     | string | Asset Name                                    |
  | initNumber          | true     | string | Initial value of assets                                  |
  | decimalPlaces       | true     | int    | Minimum divisible digits of assets                          |
  | address             | true     | string | Create the main network address for the chain                            |
  | password            | true     | string | The password corresponding to the private key                              |

  - Return Example

     Failed

     ```
     unifiedRPCstandard format
     
     ```

     Success

     ```
     unifiedRPCstandard format
     
     ```

  - Return Field Description

  | parameter | type   | description |
  | --------- | ------ | ----------- |
  | txHash    | String | transactionhash    |




#### 2.Cancel existing friend chains

- Function Description：

  Cancel existing friend chains.




1. Chains are created with assets, so asset verification must be performed to cancel the chain. Only by deleting the last asset will the chain be cancelled together.

 2.Determine the conditions for allowing deregistration：

​     Assets and chains exist.

​    There is only one last asset along the chain.

​    Chain assets includen%Assets are on their own main chain. 

 3.The chain management module encapsulates chain transactions and sends them to the transaction module.

​     During this period, it is necessary to obtain account balance and transactions through the ledger modulenonceValue.

 4.The transaction module will perform data validation callbacks during the transaction processing.

 5.The chain management module passes through Transaction module callback “Submit Chain Cancellation Transaction”Interface for To submit cancellation data.

 6.The chain management module stores data and distributes registration information to the network module.

 7.Delete the assets that have been cancelled along with the chain, and the collateral deposit will be refunded80%.

- Interface Definition

  - Interface Description

  ​        Unregister friend chain information from the chain management module（The asset deregistration interface was called because the chain was deregistered along with the final asset）.

  ​        method : cm_assetDisable

  - Request Example

  ```
  {
          "chainId": 152,
          "assetId": 45,
          "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
          "password":"xxxxxxxxxxxxx"
          
  }
  ```

  - Request parameter description

  | parameter | required | type   | description          |
  | :-------- | :------- | :----- | -------------------- |
  | chainId   | true     | int    | Chain identification               |
  | assetId   | true     | int    | assetid               |
  | address   | true     | string | Create the main network account address for the chain |
  | password  | true     | string | The password corresponding to the private key       |

  - Return Example

    Failed

    ```
    unifiedRPCstandard format
    
    ```

    Success

    ```
    unifiedRPCstandard format
    
    ```

  - Return Field Description

  | parameter | type   | description |
  | --------- | ------ | ----------- |
  | txHash    | string | transactionhash    |



#### 3  Add asset information

- Function Description：

  Assets have already been registered on the chain.


- Interface Definition

  - Interface Description

  ​        Register asset information with the chain management module.

  ​        method : cm_assetReg

  - Request Example

  ```
  {
          "chainId": 152,
          "assetId":85,
          "symbol":"NULS",
          "assetName":"Nass",
          "initNumber":"1000000000",
          "decimalPlaces":8,
           "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
          "password":"xxxxxxxxxxxxx"
          
  }
  ```

  - Request parameter description

  | parameter     | required | type   | description        |
  | :------------ | :------- | :----- | ------------------ |
  | chainId       | true     | int    | Chain identification             |
  | symbol        | true     | string | Asset symbols           |
  | assetName     | true     | string | Asset Name           |
  | initNumber    | true     | string | Initial value of assets         |
  | decimalPlaces | true     | int    | Minimum divisible digits of assets |
  | address       | true     | string | Create the main network address for the chain   |
  | password      | true     | string | The password corresponding to the private key     |

  - Return Example

    Failed

    ```
    unifiedRPCstandard format
    ```

    Success

    ```
    unifiedRPCstandard format
    ```

  - Return Field Description

  | parameter | type   | description |
  | --------- | ------ | ----------- |
  | txHash    | String | transactionhashvalue  |



#### 4 Registered Friend Chain Delete Asset Type

- Function Description：

  Destroy assets on designated friend chains.

- Interface Definition

  - Interface Description

  ​        Unregister asset information from the chain management module.

  ​        method : cm_assetDisable

  - Request Example

  ```
  {
          "chainId": 152,
          "assetId": 45,
          "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
          "password":"xxxxxxxxxxxxx"
          
  }
  ```

  - Request parameter description

  | parameter | required | type   | description          |
  | :-------- | :------- | :----- | -------------------- |
  | chainId   | true     | int    | Chain identification               |
  | assetId   | true     | int    | assetid               |
  | address   | true     | string | Create the main network account address for the chain |
  | password  | true     | string | The password corresponding to the private key       |

  - Return Example

    Failed

    ```
    unifiedRPCstandard format
    
    ```

    Success

    ```
    unifiedRPCstandard format
    
    ```

  - Return Field Description

  | parameter | type   | description  |
  | --------- | ------ | ------------ |
  | txHash    | String | Delete specified assets |

#### 5  Query registration chain information

- Function Description：

  Query registration chain information

- Process Description

​        nothing

- Interface Definition

  - Interface Description

  ​        Search for registered friend chain information.

  ​        method : cm_chain

  - Request Example

  ```
  {
     "chainId":4545 
  }
  ```

  - Request parameter description

  | parameter | required | type | description |
  | :-------- | :------- | :--- | ----------- |
  | chainId   | true     | int  | Chain identification      |

  - Return Example

    Failed

    ```
    unifiedRPCstandard format
    
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

  - Return Field Description

  | parameter           | type   | description                                           |
  | :------------------ | :----- | ----------------------------------------------------- |
  | chainId             | int    | Chain identification                                                |
  | chainName           | string | Chain Name                                                |
  | addressType         | int    | The address type of the account created on the chain：1Within the ecosystem 2Non ecological interior           |
  | magicNumber         | string | Network Magic Parameters                                          |
  | minAvailableNodeNum | int    | Minimum number of available nodes                                      |
  | txConfirmBlockNum   | int    | Number of transaction confirmation blocks                                          |
  | regTxHash           | string | transactionhash                                              |
  | regAddress          | string | Create the main network address for the chain                                      |
  | selfAssetKeyList    | list   | List of assets registered off the chain, bychainId_assetId Portfolio assetskeyvalue |
  | totalAssetKeyList   | list   | The list of assets circulating off the chain, consisting ofchainId_assetId Portfolio assetskeyvalue |
  | createTime          | long   | Creation time                                              |
  | seeds               | String | cross Seed node                                       |



#### 6. Query off chain asset information

- Function Description：

  Query asset information for a certain chain.

- Process Description

​        nothing

- Interface Definition

  - Interface Description

  ​        Query asset information from the chain management module.

  ​        method : cm_asset

  - Request Example

  ```
  {
     "chainId":4545, 
     "assetId":45
  }
  ```

  - Request parameter description

  | parameter | required | type | description |
  | :-------- | :------- | :--- | ----------- |
  | chainId   | true     | int  | Chain identification      |
  | assetId   | true     | int  | assetid      |

  - Return Example

    Failed

    ```
    unifiedRPCstandard format
    
    ```

    Success

    ```
    {
            "chainId": 152,
            "assetId":85,
            "symbol":"NULS",
            "assetName":"Nass",
            "initNumber":"1000000000",
            "decimalPlaces":8,
            "address":"NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp",
            "txHash":"xxxxxxxxxxxxx",
            "createTime":125848
            }
    ```

  - Return Field Description

  | parameter     | type   | description        |
  | :------------ | :----- | ------------------ |
  | chainId       | int    | Chain identification             |
  | symbol        | string | Asset symbols           |
  | assetName     | string | Asset Name           |
  | initNumber    | string | Initial value of assets         |
  | decimalPlaces | int    | Minimum divisible digits of assets |
  | address       | string | Create the main network address for the chain   |
  | txHash        | string | transactionhash           |
  | createTime    | long   | Creation time           |


#### 7. Create cross chain transactions

- Function Description：

  Create a cross chain transaction

- Process Description

​        nothing

- Interface Definition

  - Interface Description

  ​        Initiate a cross chain transaction

  ​        method : createCrossTx

  - Request Example

  ```
  {
     "chainId":2, 
     "remark":"Cross chain transfer transactions",
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

  - Request parameter description

  | parameter     | required | type       | description |
  | :------------ | :------- | :--------- | ----------- |
  | chainId       | true     | int        | Chain identification      |
  | remark        | false    | String     | Remarks        |
  | address       | true     | String     | Account address    |
  | assetsChainId | true     | int        | Asset ChainID    |
  | assetsId      | true     | int        | assetID      |
  | amount        | true     | BigInteger | Transaction amount    |
  | password      | true     | String     | Account password    |



  - Return Example

    Failed

    ```
    unifiedRPCstandard format
    
    ```

    Success

    ```
    {
           "txHash":"8JHSFGSGFSDHDHDHJGASVXCBCN" 
    }
    ```

  - Return Field Description

  | parameter | type   | description |
  | :-------- | :----- | ----------- |
  | txHash    | string | transactionhash    |


#### 8.Query the status of cross chain transaction processing

- Function Description：

  Query the processing status of cross chain transactions in other chains

- Process Description

​        nothing

- Interface Definition

  - Interface Description

  ​        Query the processing status of cross chain transactions in other chains

  ​        method : getCrossTxState

  - Request Example

    ```
    {
        "chainId":2, 
        "txHash":"8JHSFGSGFSDHDHDHJGASVXCBCN" 
    }
    ```

  - Parameter Description

    | parameter | type   | required | description |
    | --------- | ------ | -------- | ----------- |
    | chainId   | int    | true     | chainID        |
    | txHash    | string | true     | transactionhash    |

  - Return Example

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

  - Return parameter description

    | parameter | type    | description          |
    | --------- | ------- | -------------------- |
    | value     | boolean | Is the cross chain transaction processing completed |


