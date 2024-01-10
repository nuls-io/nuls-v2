# Cross chain module



## Interface List
### createCrossTx
Create cross chain transfer transactions/Creating Cross-Chain Transfer Transactions
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name                                                           |    Parameter type    | Parameter Description   | Is it not empty |
| ------------------------------------------------------------- |:----------:| ------ |:----:|
| chainId                                                       |    int     | chainID    |  yes   |
| listFrom                                                      |    list    | Transfer out information list |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | Account address   |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | Asset ChainID  |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | assetID   |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | Transfer amount   |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password      |   string   | Account password   |  yes   |
| listTo                                                        |    list    | Convert to information list |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | Account address   |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | Asset ChainID  |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | assetID   |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | Transfer amount   |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password      |   string   | Account password   |  yes   |
| remark                                                        |   string   | Remarks     |  no   |

#### Return value
| Field Name    |  Field type  | Parameter Description     |
| ------ |:------:| -------- |
| txHash | string | Cross chain transactionsHASH |

### newApiModuleCrossTx
receiveAPI_MODULECross chain transactions for assembly/Receiving cross-chain transactions assembled by API_MODULE
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainID  |  yes   |
| tx      | string | transaction   |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description   |
| ------ |:------:| ------ |
| txHash | string | transactionHash |

### getCrossTxState
Query the status of cross chain transaction processing/get cross transaction process state
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description   | Is it not empty |
| ------- |:------:| ------ |:----:|
| chainId |  int   | chainID    |  yes   |
| txHash  | string | transactionHASH |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description       |
| ----- |:-------:| ---------- |
| value | boolean | Has the cross chain transaction been processed successfully |

### getRegisteredChainInfoList
Search for cross chain information registered on the main website/Query for cross-chain chain information registered on the main network
#### scope:public
#### version:1.0

#### parameter list
No parameters

#### Return value
| Field Name                                                                                                           |      Field type       | Parameter Description      |
| ------------------------------------------------------------------------------------------------------------- |:---------------:| --------- |
| list                                                                                                          | list&lt;object> | Registered cross chain chain information |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainId                                                       |       int       | chainID       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainName                                                     |     string      | Chain Name       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;minAvailableNodeNum                                           |       int       | Minimum number of links     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;maxSignatureCount                                             |       int       | Maximum number of signatures     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;signatureByzantineRatio                                       |       int       | Signature Byzantine Ratio   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addressPrefix                                                 |     string      | Chain account prefix     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetInfoList                                                 | list&lt;object> | Chain Asset List     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetId       |       int       | assetID      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol        |     string      | Asset symbols      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetName     |     string      | Asset Name      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;usable        |     boolean     | Is it available      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimalPlaces |       int       | accuracy        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;verifierList                                                  |       set       | Verifier List     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;registerTime                                                  |      long       | Registration time      |

### getByzantineCount
Query the minimum number of Byzantine passes for the current signature/Query the minimum number of Byzantine passes for the current signature
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |

#### Return value
| Field Name   | Field type | Parameter Description       |
| ----- |:----:| ---------- |
| value | int  | The current minimum number of signatures in Byzantium |

### getChains
cancel Cross Chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### registerCrossChain
Chain registration cross chain/register Cross Chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name                 |  Parameter type  | Parameter Description  | Is it not empty |
| ------------------- |:------:| ----- |:----:|
| chainId             |  int   | chainID   |  yes   |
| chainName           | string | Chain Name   |  yes   |
| minAvailableNodeNum |  int   | Minimum number of links |  yes   |
| assetInfoList       | string | Asset List  |  yes   |
| registerTime        |  long  | Chain registration time |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description |
| ----- |:-------:| ---- |
| value | boolean | Processing results |

### cancelCrossChain
Designated chain assets exit cross chain/Specified Chain Assets Exit Cross Chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |
| assetId | int  | assetID |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description |
| ----- |:-------:| ---- |
| value | boolean | Processing results |

### crossChainRegisterChange
Cross chain registration information change/Registered Cross Chain change
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### recvCirculat
Receive asset information sent by other chain nodes/Receiving asset information sent by other link nodes
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### registerAsset
Chain registration cross chain/register Cross Chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name           |  Parameter type   | Parameter Description | Is it not empty |
| ------------- |:-------:| ---- |:----:|
| chainId       |   int   | chainID  |  yes   |
| assetId       |   int   | assetID |  yes   |
| symbol        | string  | Asset symbols |  yes   |
| assetName     | string  | Asset Name |  yes   |
| usable        | boolean | Is it available |  yes   |
| decimalPlaces |   int   | accuracy   |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description |
| ----- |:-------:| ---- |
| value | boolean | Processing results |

### getFriendChainCirculate
Obtaining Friendly Chain Asset Information/Access to Friendship Chain Asset Information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description             | Is it not empty |
| -------- |:------:| ---------------- |:----:|
| chainId  |  int   | chainID              |  yes   |
| assetIds | string | assetIDMultiple assetsIDSeparate with commas |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### newBlockHeight
Chain block height change/receive new block height
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainID  |  yes   |
| height  | string | chainID  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### recvCtxState
Cross chain transaction processing status messages/receive cross transaction state
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### recvCtx
Receive complete transactions broadcasted by nodes in this chain/Complete Transaction for Receiving Broadcast from Local Chain Nodes
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### recvOtherCtx
Receive complete transactions broadcasted across chain nodes/Receiving Complete Transactions for Cross-Chain Node Broadcasting
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### getCtxState
Obtaining Cross Chain Transaction Processing Status/Getting the state of cross-chain transaction processing
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### recvRegChain
Received chain information for registered cross chain transactions returned by the main network/Receiving chain information of registered cross-chain transactions returned from the main network
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### getCirculat
Query the asset information message of this chain/get chain circulation
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### recvCtxSign
Receive transaction signatures broadcasted by nodes within the chain/Transaction signature for broadcasting in receiving chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### getCtx
In chain nodes obtain and complete cross chain transactions from this node/The intra-chain node acquires and completes the cross-chain transaction from its own node
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### getOtherCtx
Cross chain nodes obtain complete transactions from this node/Cross-chain nodes obtain complete transactions from their own nodes
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### recvCtxHash
Receive transactions broadcasted across chain nodesHash/Transaction Hash receiving cross-link node broadcasting
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainID  |  yes   |
| nodeId      | string | nodeIP |  yes   |
| messageBody | string | Message Body  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

