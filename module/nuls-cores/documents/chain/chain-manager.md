# Chain management module

## Why do we need to have《Chain management》module

stayNULS 1.0In the middle, there is only one chain（NULSMain network）Therefore, there is no need for a chain management module.

stayNULS 2.0Middle,NULSOther friend chain information can be registered on the main website, including:        

- NULSChains in the ecosystem：Related toNULSThe main network is derived from the same set of code.
- Other chains：Bitcoin、Ethereum, etc

《Chain management》The module is used to manage all joinsNULSInformation on the main network's friend chains

Noun interpretation：

- NULSMain network：Different fromNULS 1.0, is another chain that runs independently, also known asNULS 2.0.
  《Chain management》yesNULSOne of the modules in the main network
- Friendly Chain：stayNULSOther chains registered on the main website

hypothesis1：Friendly ChainA, it owns assetsA

hypothesis2：Friendly ChainB, it owns assetsB

- Cross chain transactions：
  - Friendly ChainATransfer assetsAGo to Friend ChainB
  - Friendly ChainBInternal transfer of assetsA
  - Friendly ChainBTransfer assetsATransfer back to Friendly ChainA
  - Friendly ChainBTransfer assetsAGo to other friend chains（C,Detc.）
- Non cross chain transactions：
  - Friendly ChainAInternal transfer of assetsA
  - Friendly ChainBInternal transfer of assetsB

Remarks：Whether it is on chain assets or off chain assets, as long as assets are traded across chains, confirmation from the main network is required.

## 《Chain management》What to do

《Chain management》The module is used to manage joiningNULSBasic information about the chain of the main network, including：

* Register a new friend chain
* Destroy existing friend chains
* Query Friend Chain Information
* Specific friend chains increase asset types
* Specific Friend Chain Destruction Asset Types
* Cross chain asset verification

## 《Chain management》Positioning in the system

《Chain management》Strongly dependent modules：

- Core modules
- Network module
- Transaction Management Module
- Ledger module

《Chain management》Weakly dependent modules：

- Event bus module



## Interface List
### cm\_chainReg
Chain registration-Cross chain registration for parallel chains
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name                 |      Parameter type       | Parameter Description                    | Is it not empty |
| ------------------- |:---------------:| ----------------------- |:----:|
| chainId             |       int       | Asset ChainId,Value range[3-65535]     |  yes   |
| chainName           |     string      | Chain Name                     |  yes   |
| addressType         |       int       | 1 applyNULSThe chain of framework construction Within the ecosystem,2Outside the ecosystem |  yes   |
| addressPrefix       |     string      | Chain Address Prefix,1-5character             |  yes   |
| magicNumber         |      long       | Network Magic Parameters                  |  yes   |
| minAvailableNodeNum |       int       | Minimum number of connections                   |  yes   |
| assetId             |       int       | assetId,Value range[1-65535]      |  yes   |
| symbol              |     string      | Asset symbols                    |  yes   |
| assetName           |     string      | Asset Name                    |  yes   |
| initNumber          |     string      | Initial value of assets                   |  yes   |
| decimalPlaces       |      short      | Decimal Places of Assets                 |  yes   |
| address             |     string      | Create an account address for the transaction               |  yes   |
| password            |     string      | Account password                    |  yes   |
| verifierList        | list&lt;string> | List of Verifiers                 |  yes   |
| signatureBFTRatio   |     integer     | Byzantine proportion,A value greater than or equal to this is a valid confirmation       |  yes   |
| maxSignatureCount   |     integer     | Maximum number of signatures,Limit the maximum number of verifier signature lists    |  yes   |

#### Return value
| Field Name                  |  Field type  | Parameter Description           |
| -------------------- |:------:| -------------- |
| txHash               | string | transactionhashvalue        |
| mainNetVerifierList  | string | List of main network validators,Comma separated   |
| mainNetCrossSeedList | string | Main network verification seed node list,Comma separated |

### cm\_chainActive
Chain update activation-Cross chain update activation for parallel chains（Activate previously logged out chains）
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name                 |      Parameter type       | Parameter Description                    | Is it not empty |
| ------------------- |:---------------:| ----------------------- |:----:|
| chainId             |       int       | Asset ChainId,Value range[1-65535]     |  yes   |
| chainName           |     string      | Chain Name                     |  yes   |
| addressType         |       int       | 1 applyNULSThe chain of framework construction Within the ecosystem,2Outside the ecosystem |  yes   |
| addressPrefix       |     string      | Chain Address Prefix,1-5character             |  yes   |
| magicNumber         |      long       | Network Magic Parameters                  |  yes   |
| minAvailableNodeNum |       int       | Minimum number of connections                   |  yes   |
| assetId             |       int       | assetId,Value range[1-65535]      |  yes   |
| symbol              |     string      | Asset symbols                    |  yes   |
| assetName           |     string      | Asset Name                    |  yes   |
| initNumber          |     string      | Initial value of assets                   |  yes   |
| decimalPlaces       |      short      | Decimal Places of Assets                 |  yes   |
| address             |     string      | Create an account address for the transaction               |  yes   |
| password            |     string      | Account password                    |  yes   |
| verifierList        | list&lt;string> | List of Verifiers                 |  yes   |
| signatureBFTRatio   |     integer     | Byzantine proportion,A value greater than or equal to this is a valid confirmation       |  yes   |
| maxSignatureCount   |     integer     | Maximum number of signatures,Limit the maximum number of verifier signature lists    |  yes   |

#### Return value
| Field Name                      |  Field type  | Parameter Description           |
| ------------------------ |:------:| -------------- |
| txHash                   | string | transactionhashvalue        |
| mainNetVerifierSeeds     | string | Main network validator seed list,Comma separated |
| mainNetCrossConnectSeeds | string | Main network verification seed node list,Comma separated |

### cm\_getChainsSimpleInfo
Obtain a list of cross chain registered chains
#### scope:public
#### version:1.0

#### parameter list
No parameters

#### Return value
| Field Name        |     Field type     | Parameter Description          |
| ---------- |:------------:| ------------- |
| chainInfos | list&lt;map> | Return a brief list of chain and asset information |

### getCrossChainInfos
Obtain cross chain registration asset information
#### scope:public
#### version:1.0

#### parameter list
No parameters

#### Return value
| Field Name                                                                 |      Field type       | Parameter Description         |
| ------------------------------------------------------------------- |:---------------:| ------------ |
| chainInfos                                                          | list&lt;object> | Registered Chain and Asset Information List |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainId             |       int       | chainid          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainName           |     string      | Chain Name          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minAvailableNodeNum |       int       | Minimum number of connections        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetInfoList       |      list       | Asset Information List       |

### cm\_chain
View chain information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                | Is it not empty |
| ------- |:----:| ------------------- |:----:|
| chainId | int  | Asset ChainId,Value range[1-65535] |  yes   |

#### Return value
| Field Name                      |  Field type   | Parameter Description                         |
| ------------------------ |:-------:| ---------------------------- |
| chainId                  |   int   | chainid                          |
| chainName                | string  | Chain Name                          |
| addressType              | string  | Address type（1：NulsEcology,2：other）          |
| addressPrefix            | string  | Address prefix                         |
| magicNumber              |  long   | Magic parameters                         |
| minAvailableNodeNum      |   int   | Minimum number of available nodes                      |
| txConfirmedBlockNum      |   int   | Number of transaction confirmation blocks                      |
| isDelete                 | boolean | Has it been cancelled                        |
| createTime               |  long   | Creation time                         |
| regAddress               | string  | The address used when registering the chain                    |
| regTxHash                | string  | Transaction hash during registration chain                    |
| regAssetId               |   int   | Asset serial number added during registration chain                  |
| selfAssetKeyList         |  list   | All assets created in this chain,Key=chaiId_assetId |
| totalAssetKeyList        |  list   | All assets circulating on the chain,Key=chaiId_assetId |
| verifierList             |  list   | Verifier List                        |
| signatureByzantineRatio  |   int   | Byzantine proportion                        |
| maxSignatureCount        |   int   | Maximum number of signatures                       |
| mainNetVerifierSeeds     | string  | List of main network validators,Comma separated                 |
| mainNetCrossConnectSeeds | string  | Main network connection seeds provided across chains,Comma separated             |
| enable                   | boolean | Is it available                         |

### cm\_getCirculateChainAsset
Query asset information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name              |  Parameter type  | Parameter Description                 | Is it not empty |
| ---------------- |:------:| -------------------- |:----:|
| circulateChainId | string | Running ChainID,Value range[1-65535] |  yes   |
| assetChainId     | string | Asset ChainId,Value range[1-65535]  |  yes   |
| assetId          | string | assetId,Value range[1-65535]   |  yes   |

#### Return value
| Field Name              |    Field type    | Parameter Description   |
| ---------------- |:----------:| ------ |
| circulateChainId |  integer   | Running ChainID |
| assetChainId     |  integer   | Asset ChainID  |
| assetId          |  integer   | assetID   |
| initNumber       | biginteger | Initial asset quantity |
| chainAssetAmount | biginteger | Number of existing assets |

### cm\_assetCirculateCommit
Query asset information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |      Parameter type       | Parameter Description                 | Is it not empty |
| ----------- |:---------------:| -------------------- |:----:|
| chainId     |       int       | Running ChainID,Value range[1-65535] |  yes   |
| txList      | list&lt;string> | transactionHexValue List             |  yes   |
| blockHeader |     string      | Block headHexvalue              |  yes   |

#### Return value
| Field Name | Field type | Parameter Description             |
| --- |:----:| ---------------- |
| N/A | void | No specific return value, submit successfully without errors |

### cm\_assetCirculateRollBack
Query asset information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |      Parameter type       | Parameter Description                 | Is it not empty |
| ----------- |:---------------:| -------------------- |:----:|
| chainId     |       int       | Running ChainID,Value range[1-65535] |  yes   |
| txList      | list&lt;string> | transactionHexValue List             |  yes   |
| blockHeader |     string      | Block headHexvalue              |  yes   |

#### Return value
| Field Name | Field type | Parameter Description             |
| --- |:----:| ---------------- |
| N/A | void | No specific return value, validation successful without errors |

### updateChainAsset
Query asset information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |     Parameter type     | Parameter Description                | Is it not empty |
| ------- |:------------:| ------------------- |:----:|
| chainId |     int      | Asset ChainID,Value range[1-65535] |  yes   |
| assets  | list&lt;int> | assetidlist              |  yes   |

#### Return value
| Field Name | Field type | Parameter Description             |
| --- |:----:| ---------------- |
| N/A | void | No specific return value, validation successful without errors |

### cm\_assetCirculateValidator
Query asset information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId | string | Running ChainID,Value range[1-65535] |  yes   |
| tx      | string | transactionHexvalue               |  yes   |

#### Return value
| Field Name | Field type | Parameter Description             |
| --- |:----:| ---------------- |
| N/A | void | No specific return value, validation successful without errors |

### cm\_assetReg
Asset registration
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name           |    Parameter type    | Parameter Description                | Is it not empty |
| ------------- |:----------:| ------------------- |:----:|
| chainId       |    int     | Asset ChainId,Value range[1-65535] |  yes   |
| assetId       |    int     | assetId,Value range[1-65535]  |  yes   |
| symbol        |   string   | Asset symbols                |  yes   |
| assetName     |   string   | Asset Name                |  yes   |
| initNumber    | biginteger | Initial value of assets               |  yes   |
| decimalPlaces |   short    | Decimal Places of Assets             |  yes   |
| address       |   string   | Create an account address for the transaction           |  yes   |
| password      |   string   | Account password                |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description    |
| ------ |:------:| ------- |
| txHash | string | transactionhashvalue |

### cm\_assetDisable
Asset cancellation
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description                | Is it not empty |
| -------- |:------:| ------------------- |:----:|
| chainId  |  int   | Asset ChainId,Value range[1-65535] |  yes   |
| assetId  |  int   | assetId,Value range[1-65535]  |  yes   |
| address  | string | Create an account address for the transaction           |  yes   |
| password | string | Account password                |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description    |
| ------ |:------:| ------- |
| txHash | string | transactionhashvalue |

### cm\_asset
Asset registration information query
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                | Is it not empty |
| ------- |:----:| ------------------- |:----:|
| chainId | int  | Asset ChainId,Value range[1-65535] |  yes   |
| assetId | int  | assetId,Value range[1-65535]  |  yes   |

#### Return value
| Field Name |    Field type     | Parameter Description  |
| --- |:-----------:| ----- |
|     | regassetdto | Return Chain Information |

### cm\_getChainAsset
Asset View
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          | Parameter type | Parameter Description                | Is it not empty |
| ------------ |:----:| ------------------- |:----:|
| chainId      | int  | Run ChainId,Value range[1-65535] |  yes   |
| assetChainId | int  | Asset ChainId,Value range[1-65535] |  yes   |
| assetId      | int  | assetId,Value range[1-65535]  |  yes   |

#### Return value
| Field Name          |    Field type    | Parameter Description  |
| ------------ |:----------:| ----- |
| chainId      |  integer   | Run ChainId |
| assetChainId |  integer   | Asset Chainid |
| assetId      |  integer   | assetid  |
| asset        | biginteger | Asset value   |

