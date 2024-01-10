# Ledger module

## Why do we need to have《Ledger module》

> The ledger module is the data hub of blockchain, and the balance of all accounts、All transactions are saved in the ledger module,
  A comprehensive ledger is saved on each network node to ensure data integrity、open、transparent,Simultaneously ensuring that data is not tampered with、Traceability

## 《Ledger module》What to do

> Provide data support for assembly transactions,Mainly for bookkeeping and auditing,Verify the legality of the transaction,as:Is there sufficient balance and are there duplicate payments(honeysuckle)

## 《Ledger module》Positioning in the system

> The ledger module is the data hub,Save the result data of all existing transactions in the system,It does not rely on any business modules,Other modules depend on it as needed.
##《Ledger module》Explanation of Middle Nouns

- Random number of transactions（nonce, TransactionhashAfter the value8byte）
  - nonce：A scalar value equal to the number of transactions sent to this address, which will be included in every transaction initiated by the usernonce.
  - Each transaction in this account needs to save the previous expense transactionnonce.
  - Strictly speaking,nonceIt is an attribute of the originating address（It only makes sense in the context of the sending address）. However, thenonceNot explicitly stored in the blockchain as part of the account status.
  - nonceThe value is also used to prevent incorrect calculation of account balance. For example, suppose an account has10individualNULSAnd signed two transactions, both of which cost6individualNULS, each withnonce 1andnonce 2. Which of these two transactions is valid？In a blockchain distributed system, nodes may receive transactions in an unordered manner.nonceForce transactions at any address to be processed in order, regardless of the interval or the order received by the node. In this way, all nodes will calculate the same balance. payment6The Ether transaction will be successfully processed, and the account balance will be reduced to4 ether. No matter when it is received, all nodes consider it to be associated with thenonce 2The transaction is invalid. If a node receives it firstnonce 2The transaction will hold it, but after receiving and processing itnonce 1It will not be submitted before the transaction.
  - applynonceEnsure that all nodes calculate the same balance and correctly sort transactions, equivalent to using Bitcoin to prevent“Dual payment”The mechanism. However, because Ethereum tracks account balances and does not track individual coins separately（In Bitcoin, it is calledUTXO）So it only occurs when there is an error in calculating the account balance“Dual payment”.nonceMechanisms can prevent such situations from occurring.
  


## Interface List
### blockValidate
Whole block accounting verification
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |      Parameter type       | Parameter Description                 | Is it not empty |
| ----------- |:---------------:| -------------------- |:----:|
| chainId     |       int       | Running ChainId,Value range[1-65535] |  yes   |
| txList      | list&lt;string> | []transactionHexValue List           |  yes   |
| blockHeight |      long       | block height                 |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description               |
| ----- |:-------:| ------------------ |
| value | boolean | trueSuccessfully processed,falseProcessing failed |

### verifyCoinData
Unconfirmed transaction verification
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | Running ChainId,Value range[1-65535] |  yes   |
| tx      | string | transactionHexvalue               |  yes   |

#### Return value
| Field Name    |  Field type   | Parameter Description            |
| ------ |:-------:| --------------- |
| orphan | boolean | trueOrphans,falseNon orphan |

### rollbackTxValidateStatus
Rollback packaging verification status
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | Running ChainId,Value range[1-65535] |  yes   |
| tx      | string | transactionHexvalue               |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description               |
| ----- |:-------:| ------------------ |
| value | boolean | trueRollback successful,falseRollback failed |

### verifyCoinDataBatchPackaged
Package transaction verification
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |      Parameter type       | Parameter Description                 | Is it not empty |
| ------- |:---------------:| -------------------- |:----:|
| chainId |       int       | Running ChainId,Value range[1-65535] |  yes   |
| txList  | list&lt;string> | []Transaction List（HEXValue List）       |  yes   |

#### Return value
| Field Name     |      Field type       | Parameter Description          |
| ------- |:---------------:| ------------- |
| fail    | list&lt;string> | Verification failedHashValue List   |
| orphan  | list&lt;string> | Verified as orphanedHashValue List |
| success | list&lt;string> | Verified successfullyHashValue List  |

### batchValidateBegin
Start bulk packaging:Status notification
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                 | Is it not empty |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | Running ChainId,Value range[1-65535] |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description               |
| ----- |:-------:| ------------------ |
| value | boolean | trueSuccessfully processed,falseProcessing failed |

### commitUnconfirmedTx
Unconfirmed transaction submission ledger(Verify and updatenoncevalue)
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId | string | Running ChainId,Value range[1-65535] |  yes   |
| tx      | string | transactionHexvalue               |  yes   |

#### Return value
| Field Name    |  Field type   | Parameter Description                  |
| ------ |:-------:| --------------------- |
| orphan | boolean | true Orphan trading,false Non orphan transactions |

### commitBatchUnconfirmedTxs
Unconfirmed transaction batch submission ledger(Verify and updatenoncevalue)
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId | string | Running ChainId,Value range[1-65535] |  yes   |
| txList  | string | []transactionHexValue List           |  yes   |

#### Return value
| Field Name    |      Field type       | Parameter Description         |
| ------ |:---------------:| ------------ |
| orphan | list&lt;string> | Orphan TradingHashlist   |
| fail   | list&lt;string> | Verification failed transactionHashlist |

### rollBackUnconfirmTx
Rollback submitted unconfirmed transactions
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | Running ChainId,Value range[1-65535] |  yes   |
| tx      | string | transactionHexvalue               |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description             |
| ----- |:-------:| ---------------- |
| value | boolean | true Success,false fail |

### clearUnconfirmTxs
Clear all unconfirmed transactions from accounts
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                 | Is it not empty |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | Running ChainId,Value range[1-65535] |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description             |
| ----- |:-------:| ---------------- |
| value | boolean | true Success,false fail |

### commitBlockTxs
Submit block
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |      Parameter type       | Parameter Description                 | Is it not empty |
| ----------- |:---------------:| -------------------- |:----:|
| chainId     |       int       | Running ChainId,Value range[1-65535] |  yes   |
| txList      | list&lt;string> | transactionHexValue List             |  yes   |
| blockHeight |      long       | block height                 |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description             |
| ----- |:-------:| ---------------- |
| value | boolean | true Success,false fail |

### rollBackBlockTxs
Block rollback
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |      Parameter type       | Parameter Description                 | Is it not empty |
| ----------- |:---------------:| -------------------- |:----:|
| chainId     |       int       | Running ChainId,Value range[1-65535] |  yes   |
| txList      | list&lt;string> | []transactionHexValue List           |  yes   |
| blockHeight |     string      | block height                 |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description             |
| ----- |:-------:| ---------------- |
| value | boolean | true Success,false fail |

### getNonce
Obtain account assetsNONCEvalue
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |  Parameter type  | Parameter Description                 | Is it not empty |
| ------------ |:------:| -------------------- |:----:|
| chainId      |  int   | Running ChainId,Value range[1-65535] |  yes   |
| assetChainId |  int   | Asset ChainId,Value range[1-65535]  |  yes   |
| assetId      |  int   | assetId,Value range[1-65535]   |  yes   |
| address      | string | Asset location address               |  yes   |

#### Return value
| Field Name       |  Field type   | Parameter Description                      |
| --------- |:-------:| ------------------------- |
| nonce     | string  | Account assetsnoncevalue                |
| nonceType | integer | 1：Confirmednoncevalue,0：unacknowledgednoncevalue |

### getBalance
Obtain account assets(Blocked)
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |  Parameter type  | Parameter Description                | Is it not empty |
| ------------ |:------:| ------------------- |:----:|
| chainId      |  int   | Run ChainId,Value range[1-65535] |  yes   |
| assetChainId |  int   | Asset ChainId,Value range[1-65535] |  yes   |
| assetId      |  int   | assetId,Value range[1-65535]  |  yes   |
| address      | string | Asset location address              |  yes   |

#### Return value
| Field Name       |    Field type    | Parameter Description |
| --------- |:----------:| ---- |
| total     | biginteger | Total amount  |
| freeze    | biginteger | Freeze amount |
| available |   string   | Available amount |

### getBalanceNonce
Obtain account asset balance andNONCEvalue
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |  Parameter type  | Parameter Description                 | Is it not empty |
| ------------ |:------:| -------------------- |:----:|
| chainId      |  int   | Running ChainId,Value range[1-65535] |  yes   |
| assetChainId |  int   | Asset ChainId,Value range[1-65535]  |  yes   |
| assetId      |  int   | assetId,Value range[1-65535]   |  yes   |
| address      | string | Asset location address               |  yes   |

#### Return value
| Field Name              |    Field type    | Parameter Description                      |
| ---------------- |:----------:| ------------------------- |
| nonce            |   string   | Account assetsnoncevalue                |
| nonceType        |  integer   | 1：Confirmednoncevalue,0：unacknowledgednoncevalue |
| available        | biginteger | Available amount                      |
| permanentLocked  | biginteger | Permanently locked amount                    |
| timeHeightLocked | biginteger | Height or Time Locked Amount                 |

### getFreezeList
Paging to obtain account locked asset list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |  Parameter type  | Parameter Description                | Is it not empty |
| ------------ |:------:| ------------------- |:----:|
| chainId      |  int   | Run ChainId,Value range[1-65535] |  yes   |
| assetChainId |  int   | Asset ChainId,Value range[1-65535] |  yes   |
| assetId      |  int   | assetId,Value range[1-65535]  |  yes   |
| address      | string | Asset location address              |  yes   |
| pageNumber   |  int   | Starting page count                |  yes   |
| pageSize     |  int   | Display quantity per page              |  yes   |

#### Return value
| Field Name                                                         |      Field type       | Parameter Description            |
| ----------------------------------------------------------- |:---------------:| --------------- |
| totalCount                                                  |     integer     | Total number of records            |
| pageNumber                                                  |     integer     | Starting page count            |
| pageSize                                                    |     integer     | Display quantity per page          |
| list                                                        | list&lt;object> | Lock Amount List          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash      |     string      | transactionhash          |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount      |   biginteger    | Lock in amount            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockedValue |      long       | Lock time or height,-1To permanently lock |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;time        |      long       | Transaction generation time,second        |

### getAssetsById
Query the amount information of a specified set of assets off the chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description                 | Is it not empty |
| -------- |:------:| -------------------- |:----:|
| chainId  |  int   | Running ChainId,Value range[1-65535] |  yes   |
| assetIds | string | assetid,Comma separated            |  yes   |

#### Return value
| Field Name             |    Field type    | Parameter Description |
| --------------- |:----------:| ---- |
| assetId         |  integer   | assetid |
| availableAmount | biginteger | Available amount |
| freeze          | biginteger | Freeze amount |

