# Module Overview

## Why do we need a consensus module

	As is well known, the core of blockchain is the consensus mechanism. Compared to traditional internetcliet-serverThe architecture is different, and the nodes in the blockchain are equal without a center, so everyone has the same rights；So, in order to achieve consistency in data, a consensus mechanism is used to maintain a set of universally recognized ledgers in a decentralized network.

	In a broad sense, consensus mechanism is the rule or algorithm that each node in the blockchain follows together, which is the foundation for achieving mutual trust. Only in this way can decentralized and unregulated operations be achieved, and the normal operation of the entire platform be maintained.

	Narrowly speaking, the consensus mechanism determines the mechanism by which each node verifies and confirms transactions on the blockchain.

## What should the consensus module do

	Every transaction in blockchain must be recognized by each node, and only when the entire network reaches a consensus can the transaction be considered complete. Just like in democratic elections, the voting method or rules must be recognized by the people in order to complete the election. In blockchain, the main manifestation of consensus mechanism is incentive system, which is the reward given to miners. Under the guarantee of consensus mechanism, every miner can receive rewards, so that the entire blockchain can operate in an orderly manner and provide fairness、A transparent and trustworthy environment. Therefore, the consensus module needs to provide specific algorithms to maintain, namely consensus algorithms.

	There are various consensus mechanisms for public chains, with mainstream ones beingPOW、POS、DPOS.NULSThe main network adopts independently created contentPOC（Proof Of Credit）Consensus mechanism, a type of inheritanceDposThe security and efficiency of the consensus mechanism, as well as significant improvements in collaboration, can be seen as an upgraded versionDpos.

	POCResponsibilities of the consensus module：

- Legitimacy verification after block synchronization
- Create consensus nodes、Entrusting participation in consensus、Cancel delegation、Unregister consensus node★
- Package consensus nodes into blocks
- Distribution of network maintenance incentives
- Punishment for wrongdoing nodes★
  PS：Different consensus mechanisms have different consensus algorithms, which are marked above★ForPOCConsensus specific

## 《Consensus module》Positioning in the system

	The consensus module is a relatively core part of the system, mainly responsible for packaging transaction blocks, verifying block headers, managing consensus node information, delegation information, penalty information, etc. in the system.


## Interface List
### createAgentValid
create agent transaction validate
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| tx      | string | transaction   |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description     |
| ----- |:-------:| -------- |
| value | boolean | Create node validation results |

### stopAgentValid
stop agent transaction validate
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| tx      | string | transaction   |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description       |
| ----- |:-------:| ---------- |
| value | boolean | Stop node transaction verification results |

### depositValid
deposit agent transaction validate
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| tx      | string | transaction   |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description     |
| ----- |:-------:| -------- |
| value | boolean | Verification results of entrusted transactions |

### withdrawValid
withdraw deposit agent transaction validate
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| tx      | string | transaction   |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description       |
| ----- |:-------:| ---------- |
| value | boolean | Exit consensus transaction verification result |

### cs\_runChain
Running a sub chain 1.0
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId | string |      |  yes   |

#### Return value
No return value

### cs\_getAgentChangeInfo
get seed nodes list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId | string |      |  yes   |

#### Return value
No return value

### cs\_addEvidenceRecord
Chain fork evidence record/add evidence record
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name            |  Parameter type  | Parameter Description   | Is it not empty |
| -------------- |:------:| ------ |:----:|
| chainId        |  int   | chainid    |  yes   |
| blockHeader    | string | Fork block head one |  yes   |
| evidenceHeader | string | Fork block head two |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description |
| ----- |:-------:| ---- |
| value | boolean | Processing results |

### cs\_doubleSpendRecord
Shuanghua transaction records/double spend transaction record 
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| block   | string | Block information |  yes   |
| tx      | string | Forked transaction |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description |
| ----- |:-------:| ---- |
| value | boolean | Processing results |

### cs\_getWholeInfo
Query consensus data across the entire network/query the consensus information of the whole network
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name                    |  Field type  | Parameter Description       |
| ---------------------- |:------:| ---------- |
| agentCount             |  int   | Number of nodes       |
| totalDeposit           | string | Total commission two       |
| rewardOfDay            | string | Total amount of consensus rewards for the day   |
| consensusAccountNumber |  int   | Number of participants in consensus     |
| packingAgentCount      |  int   | Number of block nodes in the current round |

### cs\_getInfo
Query consensus data for specified accounts/query consensus information for specified accounts
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| address | string | Account address |  yes   |

#### Return value
| Field Name            |  Field type  | Parameter Description      |
| -------------- |:------:| --------- |
| agentCount     |  int   | Number of nodes      |
| totalDeposit   | string | Total amount of participation in consensus  |
| joinAgentCount |  int   | Number of participating consensus nodes |
| usableBalance  | string | Available balance      |
| reward         | string | Consensus rewards obtained   |
| rewardOfDay    | string | Consensus rewards obtained on the day |
| agentHash      | string | Created nodesHASH |

### cs\_getPublishList
Query red and yellow card records/query punish list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                    | Is it not empty |
| ------- |:------:| ----------------------- |:----:|
| chainId |  int   | chainid                     |  yes   |
| address | string | address                      |  yes   |
| type    |  int   | Punishment type 0Red and yellow card records 1Red Card Record 2Yellow card record |  yes   |

#### Return value
| Field Name          |      Field type       | Parameter Description      |
| ------------ |:---------------:| --------- |
| redPunish    | list&lt;string> | List of red cards obtained   |
| yellowPunish | list&lt;string> | List of yellow card penalties obtained |

### cs\_getRoundInfo
Obtain current round information/get current round information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name                                                                                                            |          Field type           | Parameter Description                                  |
| -------------------------------------------------------------------------------------------------------------- |:-----------------------:| ------------------------------------- |
| totalWeight                                                                                                    |         double          | Total weight of the current round                               |
| index                                                                                                          |          long           | Round index                                  |
| startTime                                                                                                      |          long           | Start time of round                                |
| endTime                                                                                                        |          long           | End time of round                                |
| memberCount                                                                                                    |           int           | The number of block nodes in this round                              |
| memberList                                                                                                     |     list&lt;object>     | Member information for this round of block production                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundIndex                                                     |          long           | Round index                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundStartTime                                                 |          long           | Start time of round                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingIndexOfRound                                            |           int           | Which block did this node exit in this round                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agent                                                          |         object          | Consensus node information                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress   |         byte[]          | Node address                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress |         byte[]          | Block address                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rewardAddress  |         byte[]          | Reward Address                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |       biginteger        | Margin                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;commissionRate |          byte           | commission rate                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time           |          long           | Creation time                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight    |          long           | Block height                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delHeight      |          long           | Node deregistration height                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status         |           int           | Status,0:Pending consensus unConsensus, 1:In consensus consensus |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;creditVal      |         double          | Reputation value                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalDeposit   |       biginteger        | Total entrusted amount of nodes                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash         |        nulshash         | Create transactions for this nodeHASH                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;memberCount    |           int           | Number of participants in consensus                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alais          |         string          | net aliases                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositList                                                    |     list&lt;object>     | Current node delegation information                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |       biginteger        | Entrusted amount                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash      |        nulshash         | Delegated nodesHASH                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address        |         byte[]          | Entrusted account                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time           |          long           | Entrustment time                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status         |           int           | state                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash         |        nulshash         | Entrusted transactionHASH                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight    |          long           | The height at which the entrusted transaction is packaged                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delHeight      |          long           | Exit commission height                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sortValue                                                      |         string          | Sorting values                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packStartTime                                                  |          long           | Starting block time of the current node                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packEndTime                                                    |          long           | End time of current node block output                            |
| preRound                                                                                                       | object&lt;meetinground> | Previous round information                                 |
| myMember                                                                                                       |         object          | Current node block information                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundIndex                                                     |          long           | Round index                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;roundStartTime                                                 |          long           | Start time of round                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingIndexOfRound                                            |           int           | Which block did this node exit in this round                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agent                                                          |         object          | Consensus node information                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentAddress   |         byte[]          | Node address                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packingAddress |         byte[]          | Block address                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;rewardAddress  |         byte[]          | Reward Address                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |       biginteger        | Margin                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;commissionRate |          byte           | commission rate                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time           |          long           | Creation time                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight    |          long           | Block height                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delHeight      |          long           | Node deregistration height                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status         |           int           | Status,0:Pending consensus unConsensus, 1:In consensus consensus |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;creditVal      |         double          | Reputation value                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalDeposit   |       biginteger        | Total entrusted amount of nodes                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash         |        nulshash         | Create transactions for this nodeHASH                          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;memberCount    |           int           | Number of participants in consensus                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alais          |         string          | net aliases                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;depositList                                                    |     list&lt;object>     | Current node delegation information                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;deposit        |       biginteger        | Entrusted amount                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;agentHash      |        nulshash         | Delegated nodesHASH                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address        |         byte[]          | Entrusted account                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time           |          long           | Entrustment time                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;status         |           int           | state                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash         |        nulshash         | Entrusted transactionHASH                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;blockHeight    |          long           | The height at which the entrusted transaction is packaged                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delHeight      |          long           | Exit commission height                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sortValue                                                      |         string          | Sorting values                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packStartTime                                                  |          long           | Starting block time of the current node                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packEndTime                                                    |          long           | End time of current node block output                            |

### cs\_getRoundMemberList
Query the member list of the specified block in the round/Query the membership list of the specified block's rounds
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description    | Is it not empty |
| ------- |:------:| ------- |:----:|
| chainId |  int   | chainid     |  yes   |
| extend  | string | Block header extension information |  yes   |

#### Return value
| Field Name             |      Field type       | Parameter Description       |
| --------------- |:---------------:| ---------- |
| packAddressList | list&lt;string> | Current block address list |

### cs\_getConsensusConfig
Obtain consensus module configuration information/get consensus config
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name             |  Field type   | Parameter Description              |
| --------------- |:-------:| ----------------- |
| seedNodes       | string  | Seed node list            |
| inflationAmount | integer | Maximum entrusted amount           |
| agentAssetId    | integer | Consensus assetsID            |
| agentChainId    | integer | Consensus Asset ChainID           |
| awardAssetId    | integer | Reward assetsID（Consensus rewards are assets of this chain） |

### cs\_runMainChain
run main chain 1.0
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId | string |      |  yes   |

#### Return value
No return value

### cs\_stopChain
stop a chain 1.0
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId | string |      |  yes   |

#### Return value
No return value

### cs\_getAgentList
Query the list of consensus nodes in the current network/Query the list of consensus nodes in the current network
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name        |  Parameter type  | Parameter Description | Is it not empty |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | chainid  |  yes   |
| pageNumber |  int   | Page number   |  no   |
| pageSize   |  int   | Page size |  no   |
| keyWord    | string | keyword  |  no   |

#### Return value
| Field Name            |  Field type  | Parameter Description       |
| -------------- |:------:| ---------- |
| agentHash      | string | nodeHASH     |
| agentAddress   | string | Node address       |
| packingAddress | string | Node block address     |
| rewardAddress  | string | Node reward address     |
| deposit        | string | Mortgage amount       |
| commissionRate |  byte  | commission rate       |
| agentName      | string | Node Name       |
| agentId        | string | nodeID       |
| introduction   | string | Node Introduction       |
| time           |  long  | Node creation time     |
| blockHeight    |  long  | Node packaging height     |
| delHeight      |  long  | Node failure height     |
| status         |  int   | state         |
| creditVal      | double | Reputation value        |
| totalDeposit   | string | Total entrusted amount      |
| txHash         | string | Create node transactionsHASH |
| memberCount    |  int   | Number of Commissioners       |
| version        | string | version         |

### cs\_stopAgent
Unregister node/stop agent
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| address  | string | Node address |  yes   |
| password | string | password   |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description       |
| ------ |:------:| ---------- |
| txHash | string | Stop node transactionsHASH |

### cs\_createAgent
Create node transactions/create agent transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name            |  Parameter type  | Parameter Description        | Is it not empty |
| -------------- |:------:| ----------- |:----:|
| chainId        |  int   | chainid         |  yes   |
| agentAddress   | string | Node address        |  yes   |
| packingAddress | string | Node block address      |  yes   |
| rewardAddress  | string | Reward Address,Default node address |  no   |
| commissionRate |  int   | commission rate        |  yes   |
| deposit        | string | Mortgage amount        |  yes   |
| password       | string | password          |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description       |
| ------ |:------:| ---------- |
| txHash | string | Create node transactionsHASH |

### cs\_getAgentInfo
Query detailed information of pointing nodes/Query pointer node details
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |  Parameter type  | Parameter Description   | Is it not empty |
| --------- |:------:| ------ |:----:|
| chainId   |  int   | chainid    |  yes   |
| agentHash | string | nodeHASH |  yes   |

#### Return value
| Field Name            |  Field type  | Parameter Description       |
| -------------- |:------:| ---------- |
| agentHash      | string | nodeHASH     |
| agentAddress   | string | Node address       |
| packingAddress | string | Node block address     |
| rewardAddress  | string | Node reward address     |
| deposit        | string | Mortgage amount       |
| commissionRate |  byte  | commission rate       |
| agentName      | string | Node Name       |
| agentId        | string | nodeID       |
| introduction   | string | Node Introduction       |
| time           |  long  | Node creation time     |
| blockHeight    |  long  | Node packaging height     |
| delHeight      |  long  | Node failure height     |
| status         |  int   | state         |
| creditVal      | double | Reputation value        |
| totalDeposit   | string | Total entrusted amount      |
| txHash         | string | Create node transactionsHASH |
| memberCount    |  int   | Number of Commissioners       |
| version        | string | version         |

### cs\_getAgentStatus
Query the status of specified consensus nodes/query the specified consensus node status 1.0
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |  Parameter type  | Parameter Description   | Is it not empty |
| --------- |:------:| ------ |:----:|
| chainId   |  int   | chainid    |  yes   |
| agentHash | string | nodeHASH |  yes   |

#### Return value
| Field Name    | Field type | Parameter Description |
| ------ |:----:| ---- |
| status | byte | Node status |

### cs\_updateAgentConsensusStatus
Modify node consensus status/modifying the Node Consensus State
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name | Field type | Parameter Description                    |
| --- |:----:| ----------------------- |
| N/A | void | No specific return value, no error indicates successful modification of node consensus state |

### cs\_updateAgentStatus
Modify node packaging status/modifying the Packing State of Nodes
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |
| status  | int  | Node status |  yes   |

#### Return value
| Field Name | Field type | Parameter Description                    |
| --- |:----:| ----------------------- |
| N/A | void | No specific return value, no error indicates successful modification of node packaging status |

### cs\_getNodePackingAddress
Get the current node's outbound address/Get the current node's out-of-block address
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name         |  Field type  | Parameter Description     |
| ----------- |:------:| -------- |
| packAddress | string | Current node block address |

### cs\_getAgentAddressList
Obtain the current consensus node block address list or query the most recentNOutbound address of blocks/Get all node out-of-block addresses or specify N block out-of-block designations
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name         |  Field type  | Parameter Description   |
| ----------- |:------:| ------ |
| packAddress | string | Consensus node list |

### cs\_getPackerInfo
Obtain the outbound account information of the current node/modifying the Packing State of Nodes
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name             |      Field type       | Parameter Description     |
| --------------- |:---------------:| -------- |
| address         |     string      | Current node block address |
| password        |     string      | Current node password   |
| packAddressList | list&lt;string> | Current packaging address list |

### cs\_getSeedNodeInfo
Obtain seed node information/get seed node info
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name             |      Field type       | Parameter Description     |
| --------------- |:---------------:| -------- |
| address         |     string      | Current node block address |
| password        |     string      | Current node password   |
| packAddressList | list&lt;string> | Current packaging address list |

### cs\_stopContractAgent
Smart contract cancellation node/contract stop agent
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description          | Is it not empty |
| --------------- |:------:| ------------- |:----:|
| chainId         |  int   | chainid           |  yes   |
| contractAddress | string | Contract address          |  yes   |
| contractSender  | string | Contract caller address       |  yes   |
| contractBalance | string | Current balance of contract address     |  yes   |
| contractNonce   | string | The current contract addressnoncevalue |  yes   |
| blockTime       |  long  | The current packaged block time     |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description        |
| --- |:---------------:| ----------- |
| Return value | list&lt;string> | Return transactionHASHAnd transactions |

### cs\_createContractAgent
Smart contract creation node/contract create agent
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description          | Is it not empty |
| --------------- |:------:| ------------- |:----:|
| chainId         |  int   | chainid           |  yes   |
| packingAddress  | string | Block address          |  yes   |
| deposit         | string | Mortgage amount          |  yes   |
| commissionRate  | string | commission rate          |  yes   |
| contractAddress | string | Contract address          |  yes   |
| contractSender  | string | Contract caller address       |  yes   |
| contractBalance | string | Current balance of contract address     |  yes   |
| contractNonce   | string | The current contract addressnoncevalue |  yes   |
| blockTime       |  long  | The current packaged block time     |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description        |
| --- |:---------------:| ----------- |
| Return value | list&lt;string> | Return transactionHASHAnd transactions |

### cs\_contractDeposit
Smart Contract Entrustment Consensus/contract deposit agent transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description          | Is it not empty |
| --------------- |:------:| ------------- |:----:|
| chainId         |  int   | chainid           |  yes   |
| agentHash       | string | Delegated nodesHASH     |  yes   |
| deposit         | string | Entrusted amount          |  yes   |
| contractAddress | string | Contract address          |  yes   |
| contractSender  | string | Contract caller address       |  yes   |
| contractBalance | string | Current balance of contract address     |  yes   |
| contractNonce   | string | The current contract addressnoncevalue |  yes   |
| blockTime       |  long  | The current packaged block time     |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description        |
| --- |:---------------:| ----------- |
| Return value | list&lt;string> | Return transactionHASHAnd transactions |

### cs\_contractWithdraw
Consensus on smart contract exit/contract withdraw deposit agent transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description          | Is it not empty |
| --------------- |:------:| ------------- |:----:|
| chainId         |  int   | chainid           |  yes   |
| joinAgentHash   | string | nodeHASH        |  yes   |
| contractAddress | string | Contract address          |  yes   |
| contractSender  | string | Contract caller address       |  yes   |
| contractBalance | string | Current balance of contract address     |  yes   |
| contractNonce   | string | The current contract addressnoncevalue |  yes   |
| blockTime       |  long  | The current packaged block time     |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description        |
| --- |:---------------:| ----------- |
| Return value | list&lt;string> | Return transactionHASHAnd transactions |

### cs\_getContractDepositInfo
Smart contract query for specified account delegation information/Intelligent Contract Query for Assigned Account Delegation Information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description    | Is it not empty |
| --------------- |:------:| ------- |:----:|
| chainId         |  int   | chainid     |  yes   |
| joinAgentHash   | string | nodeHASH  |  yes   |
| contractAddress | string | Contract address    |  yes   |
| contractSender  | string | Contract caller address |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description |
| --- |:---------------:| ---- |
| Return value | list&lt;string> | Entrustment information |

### cs\_getContractAgentInfo
Smart contract nodes/contract get agent info
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description    | Is it not empty |
| --------------- |:------:| ------- |:----:|
| chainId         |  int   | chainid     |  yes   |
| agentHash       | string | nodeHASH  |  yes   |
| contractAddress | string | Contract address    |  yes   |
| contractSender  | string | Contract caller address |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description |
| --- |:---------------:| ---- |
| Return value | list&lt;string> | Node information |

### cs\_triggerCoinBaseContract
Transaction module triggeredCoinBaseSmart contracts/trigger coin base contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description      | Is it not empty |
| ----------- |:------:| --------- |:----:|
| chainId     |  int   | chainid       |  yes   |
| tx          | string | Transaction information      |  yes   |
| blockHeader | string | Block head       |  yes   |
| stateRoot   | string | stateRoot |  yes   |

#### Return value
| Field Name   |  Field type  | Parameter Description      |
| ----- |:------:| --------- |
| value | string | stateRoot |

### cs\_chainRollBack
Block rollback/chain rollback
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description     | Is it not empty |
| ------- |:----:| -------- |:----:|
| chainId | int  | chainid      |  yes   |
| height  | int  | The height to which the block is rolled back |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description   |
| ----- |:-------:| ------ |
| value | boolean | Block rollback result |

### cs\_addBlock
Receive and cache new blocks/Receiving and caching new blocks
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description | Is it not empty |
| ----------- |:------:| ---- |:----:|
| chainId     |  int   | chainid  |  yes   |
| blockHeader | string | Block head  |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description     |
| ----- |:-------:| -------- |
| value | boolean | Whether the interface execution is successful or not |

### cs\_receiveHeaderList
Receive and cache block list/Receive and cache block lists
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name        |  Parameter type  | Parameter Description  | Is it not empty |
| ---------- |:------:| ----- |:----:|
| chainId    |  int   | chainid   |  yes   |
| headerList | string | Block header list |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description     |
| ----- |:-------:| -------- |
| value | boolean | Successfully received and processed |

### cs\_validBlock
Verify Block/verify block correctness
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| download |  int   | Block status |  yes   |
| block    | string | Block information |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description |
| ----- |:-------:| ---- |
| value | boolean | Verification results |

### cs\_createMultiAgent
Multiple account creation nodes/Multi-Sign Account create agent transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name            |  Parameter type  | Parameter Description        | Is it not empty |
| -------------- |:------:| ----------- |:----:|
| chainId        |  int   | chainid         |  yes   |
| agentAddress   | string | Node address(Multiple signed addresses)  |  yes   |
| packingAddress | string | Node block address      |  yes   |
| rewardAddress  | string | Reward Address,Default node address |  no   |
| commissionRate |  int   | commission rate        |  yes   |
| deposit        | string | Mortgage amount        |  yes   |
| password       | string | Signature account password      |  yes   |
| signAddress    | string | Signature account address      |  yes   |

#### Return value
| Field Name       |  Field type   | Parameter Description                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign          |
| txHash    | string  | transactionhash                                |
| completed | boolean | true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures |

### cs\_stopMultiAgent
Multiple account cancellation nodes/Multi-Sign Account stop agent
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description       | Is it not empty |
| ----------- |:------:| ---------- |:----:|
| chainId     |  int   | chainid        |  yes   |
| address     | string | Node address(Multiple signed addresses) |  yes   |
| password    | string | Signature account password     |  yes   |
| signAddress | string | Signature account address     |  yes   |

#### Return value
| Field Name       |  Field type   | Parameter Description                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign          |
| txHash    | string  | transactionhash                                |
| completed | boolean | true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures |

### cs\_multiDeposit
Multiple account delegation consensus/Multi-Sign Account deposit agent transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description   | Is it not empty |
| ----------- |:------:| ------ |:----:|
| chainId     |  int   | chainid    |  yes   |
| address     | string | Multiple account addresses signed |  yes   |
| agentHash   | string | nodeHASH |  yes   |
| deposit     | string | Entrusted amount   |  yes   |
| password    | string | Signature account password |  yes   |
| signAddress | string | Signature account address |  yes   |

#### Return value
| Field Name       |  Field type   | Parameter Description                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign          |
| txHash    | string  | transactionhash                                |
| completed | boolean | true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures |

### cs\_multiWithdraw
Consensus on account exit with multiple signatures/Multi-Sign Account withdraw deposit agent transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description       | Is it not empty |
| ----------- |:------:| ---------- |:----:|
| chainId     |  int   | chainid        |  yes   |
| address     | string | Multiple account addresses signed     |  yes   |
| txHash      | string | Join consensus tradingHASH |  yes   |
| password    | string | Signature account password     |  yes   |
| signAddress | string | Signature account address     |  yes   |

#### Return value
| Field Name       |  Field type   | Parameter Description                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign          |
| txHash    | string  | transactionhash                                |
| completed | boolean | true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures |

### cs\_random\_seed\_count
Generate a random seed based on height and the number of original seeds and return it
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |  Parameter type  | Parameter Description         | Is it not empty |
| --------- |:------:| ------------ |:----:|
| chainId   |  int   | chainid          |  yes   |
| height    |  long  | Maximum height         |  yes   |
| count     |  int   | Original number of seeds       |  yes   |
| algorithm | string | Algorithm identification：SHA3... |  yes   |

#### Return value
| Field Name       |  Field type  | Parameter Description    |
| --------- |:------:| ------- |
| seed      | string | Generate random seeds |
| algorithm | string | Algorithm identification    |
| count     |  int   | Original number of seeds  |

### cs\_random\_seed\_height
Generate a random seed based on the height interval and return it
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description         | Is it not empty |
| ----------- |:------:| ------------ |:----:|
| chainId     |  int   | chainid          |  yes   |
| startHeight |  long  | Starting height         |  yes   |
| endHeight   |  long  | Cut-off height         |  yes   |
| algorithm   | string | Algorithm identification：SHA3... |  yes   |

#### Return value
| Field Name       |  Field type  | Parameter Description    |
| --------- |:------:| ------- |
| seed      | string | Generate random seeds |
| algorithm | string | Algorithm identification    |
| count     |  int   | Original number of seeds  |

### cs\_random\_raw\_seeds\_count
Search for the original seed list based on height and return
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |
| height  | long | Starting height |  yes   |
| count   | int  | Cut-off height |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description |
| --- |:---------------:| ---- |
| Return value | list&lt;string> |      |

### cs\_random\_raw\_seeds\_height
Query the original seed list based on the height interval and return it
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         | Parameter type | Parameter Description | Is it not empty |
| ----------- |:----:| ---- |:----:|
| chainId     | int  | chainid  |  yes   |
| startHeight | long | Starting height |  yes   |
| endHeight   | long | Cut-off height |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description |
| --- |:---------------:| ---- |
| Return value | list&lt;string> |      |

### cs\_getDepositList
Query delegation information for a specified account or node/Query delegation information for a specified account or node
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name        |  Parameter type  | Parameter Description   | Is it not empty |
| ---------- |:------:| ------ |:----:|
| chainId    |  int   | chainid    |  yes   |
| pageNumber |  int   | Page number     |  yes   |
| pageSize   |  int   | Quantity per page   |  yes   |
| address    | string | Account address   |  yes   |
| agentHash  | string | nodeHASH |  yes   |

#### Return value
| Field Name          |  Field type  | Parameter Description              |
| ------------ |:------:| ----------------- |
| deposit      | string | Entrusted amount              |
| agentHash    | string | nodeHASH            |
| address      | string | Account address              |
| time         |  long  | Entrustment time              |
| txHash       | string | Entrusted transactionHASH          |
| blockHeight  |  long  | The packaging height of entrusted transactions         |
| delHeight    |  long  | Exit commission height            |
| status       |  int   | Node status 0:Pending consensus, 1:Consensus reached |
| agentName    | string | Node Name              |
| agentAddress | string | Node address              |

### cs\_depositToAgent
Create entrusted transactions/deposit agent transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |  Parameter type  | Parameter Description   | Is it not empty |
| --------- |:------:| ------ |:----:|
| chainId   |  int   | chainid    |  yes   |
| address   | string | Account address   |  yes   |
| agentHash | string | nodeHASH |  yes   |
| deposit   | string | Entrusted amount   |  yes   |
| password  | string | Account password   |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description       |
| ------ |:------:| ---------- |
| txHash | string | Join consensus tradingHash |

### cs\_withdraw
Exit entrusted transaction/withdraw deposit agent transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description       | Is it not empty |
| -------- |:------:| ---------- |:----:|
| chainId  |  int   | chainid        |  yes   |
| address  | string | Account address       |  yes   |
| txHash   | string | Join consensus tradingHASH |  yes   |
| password | string | Account password       |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description       |
| ------ |:------:| ---------- |
| txHash | string | Exit consensus tradingHash |

