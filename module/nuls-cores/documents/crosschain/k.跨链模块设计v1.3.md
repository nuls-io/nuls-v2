# Cross chain module design document

[TOC]

## Overall description

### Module Overview

####  1 Why do we need to have《Cross chain》module

​	stayNULS2.0In the ecosystem, multiple parallel chains with different protocols are allowed to operate and interact simultaneously. Due to the different protocols between different parallel chains, their protocol interactions need to be handled byNULSThe main network is used for intermediary, and the cross chain module is used to convert the protocol of this chain intoNULSMain network protocol and what will be receivedNULSThe functional module that converts the main network protocol into the main chain protocol.

#### 2《Cross chain》What to do

- Initiate cross chain transactions and convert them into main network protocol transactions
- Byzantine signatures within cross chain transactions
- Broadcast cross chain related transactions
- Cross chain transaction protocol conversion
- Byzantine verification of off chain cross chain transactions
- Off chain asset management
- Cross chain verifier maintenance
- Verifier Change Maintenance

#### 3《Cross chain》Positioning in the system

​	stayNULS2.0In the ecosystem, cross chain modules are mainly responsible for initiating, verifying, protocol conversion, maintaining off chain assets, and maintaining verifier changes for cross chain transactions.

Dependent modules

* Transaction Management Module
* Network module
* Consensus module
* Chain management module（The main network requires dependencies, and parallel chains do not require dependencies）
* Ledger module

## Module Configuration

```
minNodeAmount              When parallel chains interact with the main network across chains, the minimum number of main network nodes that need to be connected
maxNodeAmount              The maximum number of cross chain nodes that a node can connect to
sendHeight                 How many block confirmations are required after packaging cross chain transactions
byzantineRatio             Byzantine proportion of cross chain transaction signatures on this chain（This value must be greater than or equal to the value filled in when registering this chain on the main network							   Byzantine proportion of signatures on this chain）
crossSeedIps               Main network cross chain seed node
verifiers                  The initial list of main network validators, which is the address list of the main network seed node outgoing blocks
mainByzantineRatio         Byzantine proportion of cross chain transaction signatures on the main network（This value must be less than or equal to the Byzantine ratio of cross chain signatures within the main network distribution chain                            example）
maxSignatureCount          The maximum number of Byzantine signatures set by the main network
```

## functional design

###  Functional architecture diagram

![](.\cross-chain\cross_chain_functions.png)

### Core processes

#### 1.Initialize Chain

When the module starts, it needs to read the configuration information of all existing chains to initialize each chain, and the default chain for configuration will be started the first time.

- Basic information of initialization chain

  Load the configuration information of the chain, initialize various identifiers when running the chain、Status, etc.

- Initialize ChainRocksDBsurface

  Creating various data stores for chain runtimeDBTable.

- Log for initializing the chain

  Create various print log objects for the chain.

- Register relevant information with dependent modules

  Register the transactions of this module with the transaction module, register the protocol of this module with the network, activate the cross chain network, and register the chain address prefix with the account module

- Initialize the cache of the chain

  Create a cache and queue for chain runtime.

- Initialize chain task scheduler and worker threads

  Create various scheduled tasks and threads for chain runtime.

#### 2.Registration Chain

To achieve cross chain functionality in parallel chains, the first step is toNULSMain Network Registration Chain Information（Magic parameters of this chain, list of validators, cross chain asset information, etc）

#### 3.Initialize Verifier

After registering cross chain transactions on the main network, parallel chains need to synchronize the current list of validators on the main network, as well as synchronize the current list of validators on this chain with the main network

![](.\cross-chain\VerifireInit.jpg)

##### 3.1Main network validator initialization

- Main network creation initialization validator list transaction
- Initiate Byzantine verification of seed node signature within the chain
- After Byzantine verification, broadcast the transaction to the registration chain

##### 3.2Parallel chain validator initialization

- Received the initialization validator list transaction sent by the main network
- Perform signature Byzantine verification on the transaction using the main network initialization verifier list configured on this chain
- Verify and refresh the main network initialization verifier list of this chain after passing the verification
- Create a transaction to initialize the list of validators in this chain
- Initiate Byzantine verification of seed node signature within the chain
- After Byzantine verification, broadcast the transaction to the main network

##### 3.3Main network update registration chain witness list

- Received the initialization validator list transaction sent by the registration chain
- Sign the Byzantine verification for the transaction using the initialization verifier list filled out during registration with the registration chain
- If the verification is successful, update the registration chain witness list

#### 4.Verifier change

When a new validator joins or exits the parallel chain, it is necessary to broadcast the new and cancelled validator information to the main network. Similarly, when a new validator joins or cancels the main network, it is necessary to broadcast the new and cancelled validator information to notify all parallel chains

##### 4.1Change of main network verifier

![](.\cross-chain\MainNet-VerifierChange.jpg)

- The main network has added or cancelled validators
- Create Verifier Change Transaction（List of validators including new and cancelled validators）
- Initiate Byzantine signature verification within the chain
- Byzantine verification passed, broadcasting the change of main network verifier information to all parallel chains registered on the main network across chains
- After receiving the transaction of the main network verifier change, the parallel chain verifies the main network verifier change transaction, and updates the main network verifier list of this chain after verification is successful

##### 4.2Parallel chain verifier change

- Parallel chains have validators added or validators removed
- Create Verifier Change Transaction（List of validators including new and cancelled validators）
- Initiate Byzantine signature verification within the chain
- Byzantine verification passed, broadcasting parallel chain verifier change information to the main network
- The main network receives a parallel chain validator change transaction and verifies the parallel chain validator change transaction. If the verification is successful, update the list of Ping Xin chain validators

#### 5.Create cross chain transfer transactions

Cross chain transfer transaction fees need to be consumedNULSSo parallel chain accounts need to ensure that they have sufficient funds when initiating cross chain transfer transactionsNULS.

##### 5.1Main network to parallel chain

![](.\cross-chain\Main-Parallel-Ctx.jpg)

###### 5.1.1Main network process

- Initiate cross chain transfer transactions
- Verify cross chain transfer transactions, including whether the receiving chain is a registered cross chain and the transfer accountNULSIs it sufficient to pay handling fees, etc
- After verification, initiate in chain signature Byzantine
- After Byzantine verification, broadcast the cross chain transfer transaction to the receiving chain

###### 5.1.2Receiving Chain Process

- Receive the link and receive the cross chain transfer transaction broadcasted by the main network,
- Signature Byzantine verification for cross chain transfer transactions broadcasted on the main network
- After verification, convert the main network protocol cross chain transaction into the local protocol cross chain transaction, send the transaction module for processing, and wait for the transaction to be packaged when the transaction module completes processing

##### 5.2Parallel chain to main network

![](.\cross-chain\Parallel-Main-Ctx.jpg)

###### 5.2.1Initiate Chain Process

- Initiate cross chain transfer transactions
- Verify cross chain transfer transactions, including whether this chain has registered cross chain transactions, whether the receiving chain has registered cross chain transactions, and transfer accountsNULSIs it sufficient to pay handling fees, etc
- After verification, initiate in chain signature Byzantine
- After Byzantine verification, convert the cross chain transfer transaction of this chain protocol into the main network protocol and broadcast the cross chain transfer transaction to the main network protocol

###### 5.2.2Main network process

- After receiving the cross chain transfer transaction broadcasted by the parallel chain, the main network performs signature Byzantine verification on the cross chain transfer transaction broadcasted by the parallel chain
- After verification, the transaction module will be sent for processing. When the transaction module completes processing, it will wait for the transaction to be packaged

##### 5.3Parallel chainAParallel chain conversionB

![](.\cross-chain\Parallel-Parallel-Ctx.jpg)

###### 5.3.1Initiate Chain Process

- Initiate cross chain transfer transactions
- Verify cross chain transfer transactions, including whether this chain has registered cross chain transactions, whether the receiving chain has registered cross chain transactions, and transfer accountsNULSIs it sufficient to pay handling fees, etc
- After verification, initiate in chain signature Byzantine
- After Byzantine verification, convert the cross chain transfer transaction of this chain protocol into the main network protocol and broadcast the cross chain transfer transaction to the main network protocol

###### 5.3.2Main network process

- After receiving the cross chain transfer transaction broadcasted by the parallel chain, the main network performs signature Byzantine verification on the cross chain transfer transaction broadcasted by the parallel chain
- After verification, the transaction module will be sent for processing. When the transaction module completes processing, it will wait for the transaction to be packaged
- Determine whether the main network is a receiving chain. If the main network is not a receiving chain, clear the existing signature list of the received cross chain transfer transaction and initiate a Byzantine signature for the transaction within the main network chain
- The Byzantine signature within the main network chain is completed, and the cross chain transactions after the Byzantine signature within the main network chain are broadcasted to the receiving chain

###### 5.3.3Receiving Chain Process

- Receive the link and receive the cross chain transfer transaction broadcasted by the main network,
- Signature Byzantine verification for cross chain transfer transactions broadcasted on the main network
- After verification, convert the main network protocol cross chain transaction into the local protocol cross chain transaction, send the transaction module for processing, and wait for the transaction to be packaged when the transaction module completes processing

#### 6.On chain signature Byzantine process

- Create a new cross chain transaction to determine if the local node is a validator node
- If the local node is the validator node, sign the cross chain transaction and broadcast the signature to other nodes in the chain
- Collect signatures from the verifiers of this chain for cross chain transactions
- When collecting the number of signatures >= Minimum Byzantine Signature Count（The current number of validators in this chain * The Byzantine proportion of signatures configured on this chain）When removing invalid signatures（Not the signature of the current verifier on this chain）Afterwards, if a valid signature is provided >= The minimum number of signatures indicates that the Byzantine completion of intra chain signatures for cross chain transactions

#### 7.Received cross chain transaction signature Byzantine verification process

- Cross chain transactions received from other chain broadcasts
- Verify the correctness of cross chain transaction signatures
- Query the list of validators and Byzantine proportion information for the sending chain
- Verify the number of cross chain transaction signatures >= The minimum number of Byzantine signatures in the sending chain（Number of validators *  Byzantine proportion）
- Verify whether the cross chain transaction signature is the signature of the verifier
- If all of the above are verified to be successful, it indicates that the received cross chain transaction signature has been Byzantine verified to be successful



## Module Services

reference[Cross chain moduleRPC-APIInterface documentation](./cross-chain.md)



## protocol

### 1.BroadCtxHashMessage

- Message Description：Cross chain broadcasting and cross chain transactionsHash

- cmd:recvCtxHash

  | Length | Fields      | Type   | Remark               |
  | ------ | ----------- | ------ | -------------------- |
  | ?      | convertHash | byte[] | Main network protocol cross chain transactionshash |

### 2.BroadCtxSignMessage

- Message Description：Broadcast cross chain transaction signatures to other nodes in the chain

- cmd:recvCtxSign

  | Length | Fields    | Type   | Remark               |
  | ------ | --------- | ------ | -------------------- |
  | ?      | localHash | byte[] | Cross chain transactions under this chain protocolhash |
  | ？     | signature | byte[] | Cross chain transaction signature         |

### 3.GetCtxMessage

- Message Description：Obtain complete cross chain transactions from other nodes in this chain

- cmd:getCtx

  | Length | Fields      | Type   | Remark               |
  | ------ | ----------- | ------ | -------------------- |
  | ?      | requestHash | byte[] | Cross chain transactions under this chain protocolhash |

### 4.GetOtherCtxMessage

- Message Description：Obtain complete cross chain transactions from the sending chain

- cmd:getOtherCtx

  | Length | Fields      | Type   | Remark               |
  | ------ | ----------- | ------ | -------------------- |
  | ?      | requestHash | byte[] | Main network protocol cross chain transactionshash |

### 5.NewCtxMessage

- Message Description：Received complete cross chain transactions sent by other nodes in the chain

- cmd:recvCtx

  | Length | Fields      | Data Type | Remark                         |
  | ------ | ----------- | --------- | ------------------------------ |
  | 2      | type        | uint16    | Transaction type                       |
  | 4      | time        | uint32    | Transaction time                       |
  | ？     | txData      | VarByte   | Transaction data, storing the original cross chain transactionHash |
  | ？     | coinData    | VarByte   | Transaction inputs and outputs                 |
  | ？     | remark      | VarString | Remarks                           |
  | ？     | scriptSig   | VarByte   | Digital Script and Transaction Signature             |
  | ?      | requestHash | byte[]    | Main network protocol cross chain transactionshash           |

### 6.NewOtherCtxMessage

- Message Description：Received complete cross chain transactions sent by other chain nodes

- cmd:recvOtherCtx

  | Length | Fields      | Data Type | Remark                         |
  | ------ | ----------- | --------- | ------------------------------ |
  | 2      | type        | uint16    | Transaction type                       |
  | 4      | time        | uint32    | Transaction time                       |
  | ？     | txData      | VarByte   | Transaction data, storing the original cross chain transactionHash |
  | ？     | coinData    | VarByte   | Transaction inputs and outputs                 |
  | ？     | remark      | VarString | Remarks                           |
  | ？     | scriptSig   | VarByte   | Digital Script and Transaction Signature             |
  | ?      | requestHash | byte[]    | Main network protocol cross chain transactionshash           |

### 7.GetCtxStateMessage

- Message Description：Other chain nodes query the cross chain transaction processing status from this node

- cmd:getCtxState

  | Length | Fields      | Type   | Remark               |
  | ------ | ----------- | ------ | -------------------- |
  | ?      | requestHash | byte[] | Main network protocol cross chain transactionshash |

### 8.CtxStateMessage

- Message Description：Received cross chain transaction processing result return value

- cmd:recvCtxState

  | Length | Fields       | Type   | Remark                                            |
  | ------ | ------------ | ------ | ------------------------------------------------- |
  | ?      | requestHash  | byte[] | Main network protocol cross chain transactionshash                              |
  | 1      | handleResult | byte   | Cross chain transaction processing results0Unconfirmed 1Main network confirmed 2Receiving chain confirmed |

### 9.GetCirculationMessage

- Message Description：Parallel chain nodes receive query asset messages sent by the main network node for this chain

- cmd:getCirculat

  | Length | Fields   | Type   | Remark                                         |
  | ------ | -------- | ------ | ---------------------------------------------- |
  | ?      | assetIds | String | Parallel chain assets that need to be queriedID（Multiple assetsIDSeparate with commas） |

### 10.CirculationMessage

- Message Description：The main network has received a parallel chain asset message

- cmd:recvCirculat

  | Length | Fields          |  Type   | Remark                          |
  | ------ | --------------- | :-----: | ------------------------------- |
  | ?      | circulationList | VarByte | Parallel Chain Asset ListList<Circulation> |

- Circulation

  | Length | Fields          | Type       | Remark   |
  | ------ | --------------- | ---------- | -------- |
  | 2      | assetId         | uint16     | assetID   |
  | ?      | availableAmount | BigInteger | Available assets |
  | ?      | freeze          | BigInteger | Freeze assets |

### 11.GetRegisteredChainMessage

- Message Description：Parallel chain queries the main network for all registered cross chain chain information
- cmd:getChains



### 12.RegisteredChainMessage

- Message Description：Parallel link receives registered cross chain chain information returned by the main network

- cmd:recvRegChain

  | Length | Fields        |  Type   | Remark                            |
  | ------ | ------------- | :-----: | --------------------------------- |
  | ?      | chainInfoList | VarByte | Registered cross chain chain listList<ChainInfo> |

- ChainInfo

  | Length |         Fields          |   Type    |         Remark          |
  | :----: | :---------------------: | :-------: | :---------------------: |
  |   2    |         chainId         |  uint16   |          chainID           |
  |   ?    |        chainName        | VarString |         Chain Name          |
  |   2    |   minAvailableNodeNum   |  uint16   |     Minimum number of cross chain links      |
  |   2    |    maxSignatureCount    |  uint16   | Maximum number of signatures in Byzantium  |
  |   2    | signatureByzantineRatio |  uint16   |     Signature Byzantine Ratio      |
  |   ?    |      addressPrefix      | VarString |        Address prefix         |
  |   4    |      registerTime       |  uint32   |        Registration time         |
  |   ?    |      assetInfoList      |  VarByte  | Asset ListList<AssetInfo> |
  |   ?    |      verifierList       |  VarByte  |  Verifier ListSet<String>  |

- AssetInfo

  | Length |    Fields     |  Type   |  Remark  |
  | :----: | :-----------: | :-----: | :------: |
  |   2    |    assetId    | uint16  |  assetid  |
  |   ？   |    symbol     | String  | Asset symbols |
  |   ？   |   assetName   | String  | Asset Name |
  |   2    |    usable     | boolean | Is it available |
  |   2    | decimalPlaces | uint16  | Asset accuracy |

## How to develop a cross chain module

- Create a new onemavenProject Import Cross Chain Basic Package

  ```
  <dependency>
        <groupId>io.nuls.v2.cross-chain</groupId>
        <artifactId>base-lib</artifactId>
        <version>1.0.0-SNAPSHOT</version>
  </dependency>
  ```

- Create a module startup class and let it inheritio.nuls.crosschain.base.BaseCrossChainBootStrapclass

  - If the newly created cross chain module has any otherbase-libProvided incmdOther thancmd,Then it needs to be done in theinit()Add the new additions in this module in the methodcmdAdd class path tocmdIn the directory list

    ```
    registerRpcPath(RPC_PATH);stayinit()Add in methodcmdcatalogue
    ```

- achieveio.nuls.crosschain.base.service.CrossChainServiceClass, which is responsible for handling operations related to cross chain transfer transactions

  - createCrossTxCreate cross chain transfer transactions
  - newApiModuleCrossTx handleapiModuleCross chain transfer transactions sent
  - commitCrossTxCross chain transfer transaction submission
  - rollbackCrossTxCross chain transfer transaction rollback
  - crossTxBatchValidCross chain transaction validator
  - getCrossTxStateQuery the processing status of cross chain transfer transactions
  - getRegisteredChainInfoList handleapiModuleQuery all registered cross chain connection information
  - getByzantineCounthandleapiModuleQuery the current minimum number of Byzantine signatures

- achieveio.nuls.crosschain.base.service.VerifierChangeTxServiceClass, responsible for handling cross chain validator change transaction related operations

  - validateVerifier change transaction verification
  - commitVerifier change transaction submission
  - rollbackVerifier change transaction rollback

- achieveio.nuls.crosschain.base.service.VerifierInitServiceClass, which is responsible for handling validator initialization transaction related operations

  - validateVerifier initializes transaction verification
  - commitVerifier initializes transaction submission
  - rollbackVerifier initializes transaction rollback

- achieveio.nuls.crosschain.base.service.ProtocolServiceClass, which is responsible for handling cross chain network messages

  - receiveCtxSignCross chain transaction signature received from in chain node broadcast
  - getCtxIn chain nodes obtain complete cross chain transactions from this node
  - receiveCtxReceived complete cross chain transactions broadcasted by nodes in this chain
  - receiveCtxHashReceived cross chain transactions broadcasted by cross chain nodesHash
  - getOtherCtxCross chain nodes obtain complete cross chain transactions from this chain
  - receiveOtherCtxReceived complete cross chain transactions broadcasted by cross chain nodes
  - getCtxStateCross chain nodes query the processing status of cross chain transactions to the current node
  - receiveCtxStateReceived cross chain transaction processing results from cross chain node broadcast
  - getCirculationMain network to parallel chain nodes to search for new circulation of assets in this chain
  - receiveRegisteredChainInfoParallel links receive registered cross chain chain information broadcasted by the main network
