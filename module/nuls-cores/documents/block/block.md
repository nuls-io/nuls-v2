# Block module

## Why do we need to have《Block Management》module

​All transaction data on the blockchain is saved in the block, so there needs to be a module responsible for storing and managing the block, so that other modules can verify the data in the block、Blocks can be obtained during business processing.

​When a blockchain program is first launched, it needs to synchronize the latest blocks on the network to the local network. This process is generally time-consuming, and transactions cannot be initiated before synchronization is completed. Therefore, it is suitable for a separate module to complete this task.

​In summary, it is necessary to provide unified block data services for other modules, and it can better separate block management from the specific business of blocks. Modules that use blocks do not need to worry about the details of block acquisition.

## 《Block Management》What to do

- provideapi, perform block storage、query、Rollback operation
- Synchronize the latest blocks from the network for preliminary verification、Bifurcation verification. If there is no fork, call the consensus module for consensus verification, call the transaction module for transaction verification, and save it locally after all verifications are passed.
- Block synchronization、broadcast、Processing of forwarded messages
- Judgment of forked blocks、storage
- Judgment of Orphan Blocks、storage
- Forked chain maintenance、switch
- Orphan Chain Maintenance、switch

# Module operating environment

- jdk: 11
- ide: IntelliJ IDEA 2018.3.3 (Community Edition)
- maven: 3.3.9

# Common log analysis

|Log content															|Reason for log generation|
|----|----|
|skip block syn because minNodeAmount is set to 0|				holdminNodeAmountSet to0This log will be printed,Skip the block synchronization process directly,If you want to block a node, you need to change this parameter|
|no consistent nodes								|				Caused by inconsistent height of nodes connected to|
|first start										|				The height of the nodes connected to is0,Indicates that this chain has just started running|
|local blocks is newest							|				The block of the local node is already the latest,No need for block synchronization|
|The number of rolled back blocks exceeded the configured value|	The local block is inconsistent with the block on the network,Local block rollback,But the number of rollback exceeds the threshold,Stop rollback|
|The local GenesisBlock differ from network		|				The Genesis block of the local nodehashConnect with the Genesis block on the internethashMismatch,Local configuration needs to be checked|
|available nodes not enough						|				Insufficient number of available nodes connected to,inspectminNodeAmountThis configuration item,And network module configuration、journal|
|block syn complete successfully current height	|				Block synchronization successful,And it has been synchronized to the latest block|
|block syn complete but is not newest			|				Block synchronization successful,But it's not the latest block yet,Will automatically synchronize again|
|error occur when saving downloaded blocks	|				Block synchronization failed,Usually, an error occurs when saving synchronized blocks,Key inspection of block modules、Consensus module、Log of transaction module|

# Common configuration instructions

## Genesis Block

Configuration file path：[genesis-block.json](./src/main/resources/genesis-block.json)

## system parameter

Configuration file path：[module.json](./src/main/resources/module.json)

## Configuration Item Description[^1]
|Configuration items															|explain|
|----|----|
|dataFolder|Database folder name|
|language|Error code language|
|forkChainsMonitorInterval|Fork chain monitoring thread running interval|
|orphanChainsMonitorInterval|Orphan Chain Monitoring Thread Run Interval|
|orphanChainsMaintainerInterval|Orphan Chain Maintenance Thread Run Interval|
|storageSizeMonitorInterval|Cache database capacity monitoring thread running interval|
|networkResetMonitorInterval|Network monitoring thread running interval|
|nodesMonitorInterval|Node count monitoring thread running interval|
|txGroupRequestorInterval|TxGroupGet thread running interval|
|txGroupTaskDelay|Fork chain monitoring thread running interval|
|testAutoRollbackAmount|Number of blocks automatically rolled back after startup,For testing block rollback only,Set to in production environment0|
|chainName|Default Chain Name|
|chainId|Default ChainID|
|assetId|Default assetsID|
|blockMaxSize|Maximum number of bytes in a block|
|extendMaxSize|Maximum number of bytes for block extension fields|
|resetTime|When the local block height is not updated,The time interval for triggering a reset network action|
|chainSwtichThreshold|Threshold of height difference that triggers fork chain switching|
|cacheSize|Forked chain、The maximum cache size of orphan chain blocks|
|heightRange|Scope of receiving new blocks|
|waitInterval|When downloading blocks in bulk,If receivedCompleteMessageTime,The block has not been saved yet,How much waiting time is reserved for each block|
|maxRollback|When the local block is inconsistent with the network block,Maximum local rollback count|
|consistencyNodePercent|Count the latest block height of nodes on the network、hashConsistent percentage threshold|
|minNodeAmount|Minimum number of linked nodes,When the network node linked to is below this parameter,Will continue to wait|
|downloadNumber|During block synchronization process,The number of blocks downloaded from nodes on the network each time|
|validBlockInterval|To prevent malicious nodes from leaving the block prematurely,Set this parameter,Discard the block if its timestamp is greater than the current time|
|blockCache|How many blocks can be cached at most when synchronizing blocks|
|smallBlockCache|How many cell blocks can be cached at most when the system is running normally and received from other nodes|
|orphanChainMaxAge|When orphan chain maintenance fails,Age plus one,This parameter is the maximum age that an orphan chain can reach,Higher than this value will be cleared by the cleaning thread|
|logLevel|log level,Distinguish according to different chains|
|singleDownloadTimeout|The timeout for downloading a single block from a network node|
|batchDownloadTimeout|Time out for downloading multiple blocks from network nodes|
|maxLoop|When downloading blocks in bulk,If receivedCompleteMessageTime,The block has not been saved yet,How many rounds can you wait in a loop at most|
|synSleepInterval|The time interval between two block synchronizations|
|waitNetworkInterval|Waiting for the time interval for network stability|
|cleanParam|Fork chain monitoring thread running interval|
    
## Protocol Information

Configuration file path：[protocol-config.json](./src/main/resources/protocol-config.json)


[^1]:All time parameters in the configuration file are in milliseconds
## Interface List
### info
returns network node height and local node height
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |

#### Return value
| Field Name           | Field type | Parameter Description       |
| ------------- |:----:| ---------- |
| networkHeight | long | The latest block height of network nodes |
| localHeight   | long | The latest block height of the local node |

### latestBlock
the latest block of master chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description              |
| --- |:------:| ----------------- |
| Return value | string | Returns a serialized blockHEXcharacter string |

### downloadBlockByHash
get a block by hash
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description   | Is it not empty |
| ------- |:------:| ------ |:----:|
| chainId |  int   | chainID    |  yes   |
| hash    | string | blockhash |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description            |
| --- |:------:| --------------- |
| Return value | string | Return the serialized blockHEXcharacter string |

### latestHeight
the latest height of master chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |

#### Return value
| Field Name   | Field type | Parameter Description   |
| ----- |:----:| ------ |
| value | long | Latest main chain height |

### latestBlockHeader
the latest block header of master chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description               |
| --- |:------:| ------------------ |
| Return value | string | Returns a serialized block headerHEXcharacter string |

### latestBlockHeaderPo
the latest block header po of master chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description                 |
| --- |:------:| -------------------- |
| Return value | string | Return a block headerPOSerializedHEXcharacter string |

### getBlockHeaderByHeight
get a block header by height
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |
| height  | long | block height |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description               |
| --- |:------:| ------------------ |
| Return value | string | Returns a serialized block headerHEXcharacter string |

### getBlockHeaderPoByHeight
get a block header po by height
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |
| height  | long | block height |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description                 |
| --- |:------:| -------------------- |
| Return value | string | Return a block headerPOSerializedHEXcharacter string |

### getBlockByHeight
get a block by height
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |
| height  | long | block height |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description                |
| --- |:------:| ------------------- |
| Return value | string | Return the serialized blockHEXcharacter stringList |

### getBlockHeaderByHash
get a block header by hash
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description   | Is it not empty |
| ------- |:------:| ------ |:----:|
| chainId |  int   | chainID    |  yes   |
| hash    | string | blockhash |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description             |
| --- |:------:| ---------------- |
| Return value | string | Returns the serialized block headerHEXcharacter string |

### getBlockHeaderPoByHash
get a block header po by hash
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description   | Is it not empty |
| ------- |:------:| ------ |:----:|
| chainId |  int   | chainID    |  yes   |
| hash    | string | blockhash |  yes   |

#### Return value
| Field Name |  Field type  | Parameter Description               |
| --- |:------:| ------------------ |
| Return value | string | Return block headerPOSerializedHEXcharacter string |

### getLatestBlockHeaders
get the latest number of block headers
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |
| size    | int  | quantity   |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description                 |
| --- |:---------------:| -------------------- |
| Return value | list&lt;string> | Returns the serialized block headerHEXcharacter stringList |

### getLatestRoundBlockHeaders
get the latest several rounds of block headers
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |
| round   | int  | Consensus round |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description                 |
| --- |:---------------:| -------------------- |
| Return value | list&lt;string> | Returns the serialized block headerHEXcharacter stringList |

### getRoundBlockHeaders
get the latest several rounds of block headers
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |
| height  | long | Starting height |  yes   |
| round   | int  | Consensus round |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description                 |
| --- |:---------------:| -------------------- |
| Return value | list&lt;string> | Returns the serialized block headerHEXcharacter stringList |

### receivePackingBlock
receive the new packaged block
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description          | Is it not empty |
| ------- |:------:| ------------- |:----:|
| chainId |  int   | chainID           |  yes   |
| block   | string | Block serializedHEXcharacter string |  yes   |

#### Return value
| Field Name | Field type | Parameter Description |
| --- |:----:| ---- |
| N/A | void | No return value |

### getBlockHeadersByHeightRange
get the block headers according to the height range
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |
| begin   | long | Starting height |  yes   |
| end     | long | End height |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description                 |
| --- |:---------------:| -------------------- |
| Return value | list&lt;string> | Returns the serialized block headerHEXcharacter stringList |

### getBlockHeadersForProtocol
get block headers for protocol upgrade module
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      | Parameter type | Parameter Description     | Is it not empty |
| -------- |:----:| -------- |:----:|
| chainId  | int  | chainID      |  yes   |
| interval | int  | Protocol upgrade statistics interval |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description                 |
| --- |:---------------:| -------------------- |
| Return value | list&lt;string> | Returns the serialized block headerHEXcharacter stringList |

### getStatus
receive the new packaged block
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainID  |  yes   |

#### Return value
| Field Name    |  Field type   | Parameter Description |
| ------ |:-------:| ---- |
| status | integer | running state |

