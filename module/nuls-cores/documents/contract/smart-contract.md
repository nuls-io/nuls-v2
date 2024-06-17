# Smart Contract Module
- NULSThe smart contract adopts the innovative concept of modular design and incorporates it intoNULSThe module repository allows developers to directly select when building chainsNULSThe smart contract module can complete the relevant configuration.
- NULSSmart contracts enable minute level deployment, allowing developers to quickly deploy them on the chain after writing the smart contract.
- NULSSmart contractNVMIt is based onJVMImplementation,NULSThe smart contract interpreter will seamlessly supportJVMThe system programming language will gradually support other mainstream programming languages, and application developers can use their familiar languages to designNULSSmart contracts.
- Through such innovative design thinking,NULSHope to enable the development of smart contracts、Deploying and calling can be more convenient, thereby increasing developers' interest in application development. In the near future, a rich library of smart contracts can be created.

## Interface List
### sc\_batch\_begin
Execute the start notification for a batch of contracts, generate information for the current batch/batch begin
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name            |  Parameter type  | Parameter Description         | Is it not empty |
| -------------- |:------:| ------------ |:----:|
| chainId        | string | chainid          |  yes   |
| blockHeight    | string | The height of the currently packaged blocks    |  yes   |
| blockTime      | string | The current packaged block time    |  yes   |
| packingAddress | string | The current block packaging address  |  yes   |
| preStateRoot   | string | PreviousstateRoot |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### sc\_batch\_before\_end
After the transaction module has packaged the transaction, before conducting unified verification, notify the contract module to stop receiving transactions and start asynchronous processing of the results of this batch/batch before end
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description      | Is it not empty |
| ----------- |:------:| --------- |:----:|
| chainId     | string | chainid       |  yes   |
| blockHeight | string | The height of the currently packaged blocks |  yes   |

#### Return value
| Field Name | Field type | Parameter Description                                                 |
| --- |:----:| ---------------------------------------------------- |
| N/A | void | No specific return value, success is achieved without errors. If an error is returned, the batch is discarded, and all executed contract transactions within the batch are returned to the queue for packaging transactions |

### sc\_batch\_end
Notify the end of the current batch and return the result/batch end
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description      | Is it not empty |
| ----------- |:------:| --------- |:----:|
| chainId     | string | chainid       |  yes   |
| blockHeight | string | The height of the currently packaged blocks |  yes   |

#### Return value
| Field Name       |      Field type       | Parameter Description                                   |
| --------- |:---------------:| -------------------------------------- |
| stateRoot |     string      | currentstateRoot                            |
| txList    | list&lt;string> | List of newly generated transaction serialization strings for contracts(There may be a contract transfer、Contract consensus、Contract returnGAS) |

### sc\_package\_batch\_end
Packaging completed - Notify the end of the current batch and return the result/batch end
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description      | Is it not empty |
| ----------- |:------:| --------- |:----:|
| chainId     | string | chainid       |  yes   |
| blockHeight | string | The height of the currently packaged blocks |  yes   |

#### Return value
| Field Name       |      Field type       | Parameter Description                                   |
| --------- |:---------------:| -------------------------------------- |
| stateRoot |     string      | currentstateRoot                            |
| txList    | list&lt;string> | List of newly generated transaction serialization strings for contracts(There may be a contract transfer、Contract consensus、Contract returnGAS) |

### sc\_contract\_offline\_tx\_hash\_list
Return the contract generated transaction in the specified block（Contract returnGASExcluding transactions）ofhashlist（Newly generated transactions in the contract except for contract returnsGASExcept for transactions, they are not saved to the block. The contract module saves the relationship between these transactions and the specified block）/contract offline tx hash list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |  Parameter type  | Parameter Description   | Is it not empty |
| --------- |:------:| ------ |:----:|
| chainId   | string | chainid    |  yes   |
| blockHash | string | blockhash |  yes   |

#### Return value
| Field Name  |      Field type       | Parameter Description                       |
| ---- |:---------------:| -------------------------- |
| list | list&lt;string> | Contract transaction serialization string list(There may be a contract transfer、Contract consensus) |

### sc\_initial\_account\_token
Initialize accounttokenInformation, called when importing accounts from nodes/initial account token
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId | string | chainid  |  yes   |
| address | string | Account address |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### sc\_register\_cmd\_for\_contract
Other modules register commands that can be called by the contract with the contract module, and after registration, the registered commands can be called within the contract code/register cmd for contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description   | Is it not empty |
| --------------- |:------:| ------ |:----:|
| chainId         | string | chainid    |  yes   |
| moduleCode      | string | Module code   |  yes   |
| cmdRegisterList | string | Registration Information List |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### sc\_trigger\_payable\_for\_consensus\_contract
When the consensus reward return address is the contract address, it will trigger the contract_payable(String[][] args)Method, parameter is node revenue address details<br>args[0] = new String[]{address, amount}<br>...<br>/trigger payable for consensus contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description                     | Is it not empty |
| --------------- |:------:| ------------------------ |:----:|
| chainId         | string | chainid                      |  yes   |
| stateRoot       | string | CurrentstateRoot             |  yes   |
| blockHeight     | string | The height of the currently packaged blocks                |  yes   |
| contractAddress | string | Contract address                     |  yes   |
| tx              | string | The current packaging block containsCoinBaseTransaction serialization string |  yes   |

#### Return value
| Field Name   |  Field type  | Parameter Description          |
| ----- |:------:| ------------- |
| value | string | After changesstateRoot |

### sc\_invoke\_contract
After the batch notification starts, execute the contract one by one/invoke contract one by one
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description           | Is it not empty |
| ------- |:------:| -------------- |:----:|
| chainId | string | chainid            |  yes   |
| tx      | string | Serialized transactionHEXEncoding string |  yes   |

#### Return value
| Field Name | Field type | Parameter Description                          |
| --- |:----:| ----------------------------- |
| N/A | void | No specific return value, successful without errors. If an error is returned, the transaction will be discarded |

### sc\_constructor
contract code constructor
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |  Parameter type  | Parameter Description                 | Is it not empty |
| ------------ |:------:| -------------------- |:----:|
| chainId      |  int   | chainID                  |  yes   |
| contractCode | string | Smart Contract Code(BytecodeHexEncoding string) |  yes   |

#### Return value
| Field Name                                                                                                      |      Field type       | Parameter Description               |
| -------------------------------------------------------------------------------------------------------- |:---------------:| ------------------ |
| constructor                                                                                              |     object      | Contract constructor details           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | Method Name               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | Method description               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | Method parameter list             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | Parameter type               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | Parameter Name               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | Is it mandatory to fill in               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | return type              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | View Method（Call this method and the data will not be linked） |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | Is it an event              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | Is it an acceptable method for transferring main chain assets    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jsonSerializable                                         |     boolean     | Is the method return valueJSONserialize     |
| nrc20                                                                                                    |     boolean     | Is itNRC20contract         |

### sc\_delete
delete contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description      | Is it not empty |
| --------------- |:------:| --------- |:----:|
| chainId         |  int   | chainid       |  yes   |
| sender          | string | Transaction creator account address |  yes   |
| password        | string | Transaction account password    |  yes   |
| contractAddress | string | Contract address      |  yes   |
| remark          | string | Transaction notes      |  no   |

#### Return value
| Field Name    |  Field type  | Parameter Description        |
| ------ |:------:| ----------- |
| txHash | string | Delete transactions for contractshash |

### sc\_create
Publish contract/create contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |   Parameter type   | Parameter Description                 | Is it not empty |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | chainid                  |  yes   |
| sender       |  string  | Transaction creator account address            |  yes   |
| password     |  string  | Account password                 |  yes   |
| alias        |  string  | Contract alias                 |  yes   |
| gasLimit     |   long   | GASlimit                |  yes   |
| price        |   long   | GASunit price                |  yes   |
| contractCode |  string  | Smart Contract Code(BytecodeHexEncoding string) |  yes   |
| args         | object[] | parameter list                 |  no   |
| remark       |  string  | Transaction notes                 |  no   |

#### Return value
| Field Name             |  Field type  | Parameter Description        |
| --------------- |:------:| ----------- |
| txHash          | string | Transactions for publishing contractshash |
| contractAddress | string | Generated contract address     |

### sc\_transfer
Transfer from account address to contract address(Main chain assets)/transfer NULS from sender to contract address
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |    Parameter type    | Parameter Description      | Is it not empty |
| --------- |:----------:| --------- |:----:|
| chainId   |    int     | chainid       |  yes   |
| address   |   string   | Transferor's account address   |  yes   |
| toAddress |   string   | Transferred contract address   |  yes   |
| password  |   string   | Transferor account password   |  yes   |
| amount    | biginteger | The amount of main chain assets transferred out |  yes   |
| remark    |   string   | Transaction notes      |  no   |

#### Return value
| Field Name    |  Field type  | Parameter Description   |
| ------ |:------:| ------ |
| txHash | string | transactionhash |

### sc\_validate\_create
Verify release contract/validate create contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |   Parameter type   | Parameter Description                 | Is it not empty |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | chainid                  |  yes   |
| sender       |  string  | Transaction creator account address            |  yes   |
| gasLimit     |   long   | GASlimit                |  yes   |
| price        |   long   | GASunit price                |  yes   |
| contractCode |  string  | Smart Contract Code(BytecodeHexEncoding string) |  yes   |
| args         | object[] | parameter list                 |  no   |

#### Return value
| Field Name | Field type | Parameter Description             |
| --- |:----:| ---------------- |
| N/A | void | No specific return value, validation successful without errors |

### sc\_validate\_call
validate call contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |    Parameter type    | Parameter Description                                     | Is it not empty |
| --------------- |:----------:| ---------------------------------------- |:----:|
| chainId         |    int     | chainid                                      |  yes   |
| sender          |   string   | Transaction creator account address                                |  yes   |
| value           | biginteger | The amount of main network assets transferred by the caller to the contracted address, to be filled in when this service is not availableBigInteger.ZERO |  yes   |
| gasLimit        |    long    | GASlimit                                    |  yes   |
| price           |    long    | GASunit price                                    |  yes   |
| contractAddress |   string   | Contract address                                     |  yes   |
| methodName      |   string   | Contract method                                     |  yes   |
| methodDesc      |   string   | Contract method description, if the method in the contract is not overloaded, this parameter can be empty               |  no   |
| args            |  object[]  | parameter list                                     |  no   |

#### Return value
| Field Name | Field type | Parameter Description             |
| --- |:----:| ---------------- |
| N/A | void | No specific return value, validation successful without errors |

### sc\_validate\_delete
validate delete contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description      | Is it not empty |
| --------------- |:------:| --------- |:----:|
| chainId         |  int   | chainid       |  yes   |
| sender          | string | Transaction creator account address |  yes   |
| contractAddress | string | Contract address      |  yes   |

#### Return value
| Field Name | Field type | Parameter Description             |
| --- |:----:| ---------------- |
| N/A | void | No specific return value, validation successful without errors |

### sc\_contract\_result
contract result
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description   | Is it not empty |
| ------- |:------:| ------ |:----:|
| chainId |  int   | chainid    |  yes   |
| hash    | string | transactionhash |  yes   |

#### Return value
| Field Name                                                                                                   |      Field type       | Parameter Description                                        |
| ----------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------- |
| success                                                                                               |     boolean     | Whether the contract execution was successful                                    |
| errorMessage                                                                                          |     string      | Execution failure information                                      |
| contractAddress                                                                                       |     string      | Contract address                                        |
| result                                                                                                |     string      | Contract execution results                                      |
| gasLimit                                                                                              |      long       | GASlimit                                       |
| gasUsed                                                                                               |      long       | UsedGAS                                      |
| price                                                                                                 |      long       | GASunit price                                       |
| totalFee                                                                                              |     string      | Total transaction fees                                      |
| txSizeFee                                                                                             |     string      | Transaction size handling fee                                     |
| actualContractFee                                                                                     |     string      | Actual contract execution fee                                   |
| refundFee                                                                                             |     string      | Contract return handling fee                                    |
| value                                                                                                 |     string      | The amount of main network assets transferred by the caller to the contracted address. If there is no such service, it is:0                 |
| stackTrace                                                                                            |     string      | Abnormal stack trace                                      |
| transfers                                                                                             | list&lt;object> | Contract transfer list（Transfer out from contract）                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                |     string      | Contract generation transaction：Contract transfer transactionhash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | Transferred contract address                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | Transfer amount                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                               | list&lt;object> | Transferred address list                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to    |     string      | Transfer address                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value |     string      | Transfer amount                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                           |     string      | Call contract transactionshash（Source transactionhashContract trading is derived from calling contract trading）         |
| events                                                                                                | list&lt;string> | Contract Event List                                      |
| tokenTransfers                                                                                        | list&lt;object> | contracttokenTransfer List                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                       |     string      | Contract address                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | Payer                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                    |     string      | Payee                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | Transfer amount                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                  |     string      | tokenname                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                |     string      | tokensymbol                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                              |      long       | tokenSupported Decimal Places                                |
| invokeRegisterCmds                                                                                    | list&lt;object> | List of call records for contract calls to external commands                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                               |     string      | Command Name                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                  |       map       | Command parameters are not fixed and come from different commands, so they are not described here. The structure is {Parameter Name=Parameter values} |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                       |     string      | Registered Command Mode（QUERY\_DATA or NEW\_TX）             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                             |     string      | Generated transactionshash（When the command mode being called is NEW\_TX When, a transaction will be generated）        |
| contractTxList                                                                                        | list&lt;string> | Serialized string list for contract generation transactions                             |
| remark                                                                                                |     string      | Remarks                                          |

### sc\_contract\_result\_list
contract result list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |      Parameter type       | Parameter Description     | Is it not empty |
| -------- |:---------------:| -------- |:----:|
| chainId  |       int       | chainid      |  yes   |
| hashList | list&lt;string> | transactionhashlist |  yes   |

#### Return value
| Field Name                                                                                                                                                   |      Field type       | Parameter Description                                        |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------- |
| hash1 or hash2 or hash3...                                                                                                                            |     object      | TradinghashIn the listhashValue askeyHerekey nameIt is dynamic       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;success                                                                                               |     boolean     | Whether the contract execution was successful                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;errorMessage                                                                                          |     string      | Execution failure information                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                                                                       |     string      | Contract address                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;result                                                                                                |     string      | Contract execution results                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit                                                                                              |      long       | GASlimit                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasUsed                                                                                               |      long       | UsedGAS                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                                                                 |      long       | GASunit price                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalFee                                                                                              |     string      | Total transaction fees                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txSizeFee                                                                                             |     string      | Transaction size handling fee                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;actualContractFee                                                                                     |     string      | Actual contract execution fee                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;refundFee                                                                                             |     string      | Contract return handling fee                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                                                                 |     string      | The amount of main network assets transferred by the caller to the contracted address. If there is no such service, it is:0                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;stackTrace                                                                                            |     string      | Abnormal stack trace                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transfers                                                                                             | list&lt;object> | Contract transfer list（Transfer out from contract）                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                |     string      | Contract generation transaction：Contract transfer transactionhash                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | Transferred contract address                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | Transfer amount                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                               | list&lt;object> | Transferred address list                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to    |     string      | Transfer address                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value |     string      | Transfer amount                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                           |     string      | Call contract transactionshash（Source transactionhashContract trading is derived from calling contract trading）         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;events                                                                                                | list&lt;string> | Contract Event List                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tokenTransfers                                                                                        | list&lt;object> | contracttokenTransfer List                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                       |     string      | Contract address                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                  |     string      | Payer                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                    |     string      | Payee                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                 |     string      | Transfer amount                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                  |     string      | tokenname                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                |     string      | tokensymbol                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                              |      long       | tokenSupported Decimal Places                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;invokeRegisterCmds                                                                                    | list&lt;object> | List of call records for contract calls to external commands                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                               |     string      | Command Name                                        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                  |       map       | Command parameters are not fixed and come from different commands, so they are not described here. The structure is {Parameter Name=Parameter values} |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                       |     string      | Registered Command Mode（QUERY\_DATA or NEW\_TX）             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                             |     string      | Generated transactionshash（When the command mode being called is NEW\_TX When, a transaction will be generated）        |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractTxList                                                                                        | list&lt;string> | Serialized string list for contract generation transactions                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                                                                |     string      | Remarks                                          |

### sc\_imputed\_create\_gas
Estimated release contract consumptionGAS/imputed create gas
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |   Parameter type   | Parameter Description                 | Is it not empty |
| ------------ |:--------:| -------------------- |:----:|
| chainId      |   int    | chainid                  |  yes   |
| sender       |  string  | Transaction creator account address            |  yes   |
| contractCode |  string  | Smart Contract Code(BytecodeHexEncoding string) |  yes   |
| args         | object[] | parameter list                 |  no   |

#### Return value
| Field Name      | Field type | Parameter Description              |
| -------- |:----:| ----------------- |
| gasLimit | long | ConsumablegasValue, return value for execution failure1 |

### sc\_imputed\_call\_gas
imputed call gas
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |    Parameter type    | Parameter Description                                     | Is it not empty |
| --------------- |:----------:| ---------------------------------------- |:----:|
| chainId         |    int     | chainid                                      |  yes   |
| sender          |   string   | Transaction creator account address                                |  yes   |
| value           | biginteger | The amount of main network assets transferred by the caller to the contracted address, to be filled in when this service is not availableBigInteger.ZERO |  yes   |
| contractAddress |   string   | Contract address                                     |  yes   |
| methodName      |   string   | Contract method                                     |  yes   |
| methodDesc      |   string   | Contract method description, if the method in the contract is not overloaded, this parameter can be empty               |  no   |
| args            |  object[]  | parameter list                                     |  no   |

#### Return value
| Field Name      | Field type | Parameter Description              |
| -------- |:----:| ----------------- |
| gasLimit | long | ConsumablegasValue, return value for execution failure1 |

### sc\_token\_transfer
NRC20-tokenTransfer/transfer NRC20-token from address to toAddress
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |    Parameter type    | Parameter Description         | Is it not empty |
| --------------- |:----------:| ------------ |:----:|
| chainId         |    int     | chainid          |  yes   |
| address         |   string   | Transferor's account address      |  yes   |
| toAddress       |   string   | Transfer address         |  yes   |
| contractAddress |   string   | tokenContract address    |  yes   |
| password        |   string   | Transferor account password      |  yes   |
| amount          | biginteger | Transferred outtokenAsset amount |  yes   |
| remark          |   string   | Transaction notes         |  no   |

#### Return value
| Field Name    |  Field type  | Parameter Description   |
| ------ |:------:| ------ |
| txHash | string | transactionhash |

### sc\_token\_balance
NRC20Token balance details/NRC20-token balance
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description | Is it not empty |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | chainID  |  yes   |
| contractAddress | string | Contract address |  yes   |
| address         | string | Account address |  yes   |

#### Return value
| Field Name             |  Field type  | Parameter Description                    |
| --------------- |:------:| ----------------------- |
| contractAddress | string | Contract address                    |
| name            | string | tokenname                 |
| symbol          | string | tokensymbol                 |
| amount          | string | tokenquantity                 |
| decimals        |  long  | tokenSupported Decimal Places            |
| blockHeight     |  long  | Block height during contract creation              |
| status          |  int   | Contract status(0-Not present, 1-normal, 2-termination) |

### sc\_invoke\_view
invoke view contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |   Parameter type   | Parameter Description                       | Is it not empty |
| --------------- |:--------:| -------------------------- |:----:|
| chainId         |   int    | chainid                        |  yes   |
| contractAddress |  string  | Contract address                       |  yes   |
| methodName      |  string  | Contract method                       |  yes   |
| methodDesc      |  string  | Contract method description, if the method in the contract is not overloaded, this parameter can be empty |  no   |
| args            | object[] | parameter list                       |  no   |

#### Return value
| Field Name    |  Field type  | Parameter Description      |
| ------ |:------:| --------- |
| result | string | The call result of the view method |

### sc\_contract\_info
Contract information details/contract info
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |  Parameter type  | Parameter Description | Is it not empty |
| --------------- |:------:| ---- |:----:|
| chainId         |  int   | chainID  |  yes   |
| contractAddress | string | Contract address |  yes   |

#### Return value
| Field Name                                                                                                      |      Field type       | Parameter Description                                       |
| -------------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------ |
| createTxHash                                                                                             |     string      | Transactions for publishing contractshash                                |
| address                                                                                                  |     string      | Contract address                                       |
| creater                                                                                                  |     string      | Contract Creator Address                                    |
| alias                                                                                                    |     string      | Contract alias                                       |
| createTime                                                                                               |      long       | Contract creation time（unit：second）                               |
| blockHeight                                                                                              |      long       | Block height during contract creation                                 |
| directPayable                                                                                            |     boolean     | Do you accept direct transfer                                   |
| tokenType                                                                                                |       int       | tokentype, 0 - wrongtoken, 1 - NRC20, 2 - NRC721 |
| nrc20                                                                                                    |     boolean     | Is itNRC20contract                                 |
| nrc20TokenName                                                                                           |     string      | NRC20-tokenname                              |
| nrc20TokenSymbol                                                                                         |     string      | NRC20-tokensymbol                              |
| decimals                                                                                                 |      long       | NRC20-tokenSupported Decimal Places                         |
| totalSupply                                                                                              |     string      | NRC20-tokenTotal issuance amount                            |
| status                                                                                                   |     string      | Contract status（not_found, normal, stop）              |
| method                                                                                                   | list&lt;object> | List of Contract Methods                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | Method Name                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | Method description                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | Method parameter list                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | Parameter type                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | Parameter Name                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | Is it mandatory to fill in                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | return type                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | View Method（Call this method and the data will not be linked）                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | Is it an event                                      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | Is it an acceptable method for transferring main chain assets                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jsonSerializable                                         |     boolean     | Is the method return valueJSONserialize                             |

### sc\_contract\_tx
Contract trading/contract tx
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description   | Is it not empty |
| ------- |:------:| ------ |:----:|
| chainId |  int   | chainid    |  yes   |
| hash    | string | transactionhash |  yes   |

#### Return value
| Field Name                                                                                                                                                             |      Field type       | Parameter Description                                                    |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------- |:---------------:| ------------------------------------------------------- |
| hash                                                                                                                                                            |     string      | transactionhash                                                  |
| type                                                                                                                                                            |     integer     | Transaction type                                                    |
| time                                                                                                                                                            |      long       | Transaction time                                                    |
| blockHeight                                                                                                                                                     |      long       | block height                                                    |
| fee                                                                                                                                                             |     string      | Transaction fees                                                   |
| value                                                                                                                                                           |     string      | Transaction amount                                                    |
| remark                                                                                                                                                          |     string      | Remarks                                                      |
| scriptSig                                                                                                                                                       |     string      | Signature information                                                    |
| status                                                                                                                                                          |     integer     | Transaction status（0 - Confirming,1 - Confirmed）                                   |
| confirmCount                                                                                                                                                    |      long       | Number of transaction confirmations                                                  |
| size                                                                                                                                                            |       int       | Transaction size                                                    |
| inputs                                                                                                                                                          | list&lt;object> | Transaction Input Set                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address                                                                                                         |     string      | Enter address                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId                                                                                                   |       int       | Asset ChainID                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId                                                                                                        |       int       | assetID                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount                                                                                                          |     string      | Spending amount                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nonce                                                                                                           |     string      | Address ledgernoncevalue                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;locked                                                                                                          |      byte       | Unlock transaction tags（0 - Non unlocked transactions,1 - Unlock transaction）                             |
| outputs                                                                                                                                                         | list&lt;object> | Transaction output set                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address                                                                                                         |     string      | Output address                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId                                                                                                   |       int       | Asset ChainID                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId                                                                                                        |       int       | assetID                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount                                                                                                          |     string      | Output amount                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime                                                                                                        |      long       | Lock time                                                    |
| txData                                                                                                                                                          |       map       | Contract trading business data                                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;data                                                                                                            |     object      | Reflect different business data based on contract transaction types（Here, in order to describe four situations, the four businesses are described together, but in reality, they do not exist simultaneously, only one exists） |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;create                                                          |     object      | Publish business data for contract transactions                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |     string      | Transaction creator address                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string      | Contract address created                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias           |     string      | Contract alias                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;hexCode         |     string      | Smart Contract Code(BytecodeHexEncoding string)                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit        |      long       | GASlimit                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price           |      long       | GASunit price                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |   string[][]    | parameter list                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;call                                                            |     object      | Call business data for contract transactions                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |     string      | Transaction creator address                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string      | Contract address                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value           |     string      | The amount of main network assets transferred by the caller to the contracted address, to be filled in when this service is not availableBigInteger.ZERO                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit        |      long       | GASlimit                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price           |      long       | GASunit price                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodName      |     string      | Contract method                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;methodDesc      |     string      | Contract method description, if the method in the contract is not overloaded, this parameter can be empty                              |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args            |   string[][]    | parameter list                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;delete                                                          |     object      | Delete business data for contract transactions                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sender          |     string      | Transaction creator address                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string      | Contract address                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transfer                                                        |     object      | Business data for contract transfer transactions                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash     |     string      | Call contract transactionshash（Source transactionhashContract trading is derived from calling contract trading）                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress |     string      | Contract address                                                    |
| contractResult                                                                                                                                                  |     object      | Contract execution results                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;success                                                                                                         |     boolean     | Whether the contract execution was successful                                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;errorMessage                                                                                                    |     string      | Execution failure information                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                                                                                 |     string      | Contract address                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;result                                                                                                          |     string      | Contract execution results                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasLimit                                                                                                        |      long       | GASlimit                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gasUsed                                                                                                         |      long       | UsedGAS                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;price                                                                                                           |      long       | GASunit price                                                   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;totalFee                                                                                                        |     string      | Total transaction fees                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txSizeFee                                                                                                       |     string      | Transaction size handling fee                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;actualContractFee                                                                                               |     string      | Actual contract execution fee                                               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;refundFee                                                                                                       |     string      | Contract return handling fee                                                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                                                                           |     string      | The amount of main network assets transferred by the caller to the contracted address. If there is no such service, it is:0                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;stackTrace                                                                                                      |     string      | Abnormal stack trace                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;transfers                                                                                                       | list&lt;object> | Contract transfer list（Transfer out from contract）                                           |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;txHash                                                          |     string      | Contract generation transaction：Contract transfer transactionhash                                       |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                            |     string      | Transferred contract address                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                           |     string      | Transfer amount                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;outputs                                                         | list&lt;object> | Transferred address list                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to              |     string      | Transfer address                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value           |     string      | Transfer amount                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;orginTxHash                                                     |     string      | Call contract transactionshash（Source transactionhashContract trading is derived from calling contract trading）                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;events                                                                                                          | list&lt;string> | Contract Event List                                                  |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;tokenTransfers                                                                                                  | list&lt;object> | contracttokenTransfer List                                             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractAddress                                                 |     string      | Contract address                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from                                                            |     string      | Payer                                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to                                                              |     string      | Payee                                                     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;value                                                           |     string      | Transfer amount                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                            |     string      | tokenname                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;symbol                                                          |     string      | tokensymbol                                                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;decimals                                                        |      long       | tokenSupported Decimal Places                                            |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;invokeRegisterCmds                                                                                              | list&lt;object> | List of call records for contract calls to external commands                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdName                                                         |     string      | Command Name                                                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                            |       map       | Command parameters are not fixed and come from different commands, so they are not described here. The structure is {Parameter Name=Parameter values}             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmdRegisterMode                                                 |     string      | Registered Command Mode（QUERY\_DATA or NEW\_TX）                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;newTxHash                                                       |     string      | Generated transactionshash（When the command mode being called is NEW\_TX When, a transaction will be generated）                    |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;contractTxList                                                                                                  | list&lt;string> | Serialized string list for contract generation transactions                                         |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark                                                                                                          |     string      | Remarks                                                      |

### sc\_token\_assets\_list
tokenAsset Collection/token assets list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name        |  Parameter type  | Parameter Description | Is it not empty |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | chainID  |  yes   |
| address    | string | Account address |  yes   |
| pageNumber |  int   | Page number   |  no   |
| pageSize   |  int   | Page size |  no   |

#### Return value
| Field Name             |  Field type  | Parameter Description                    |
| --------------- |:------:| ----------------------- |
| contractAddress | string | Contract address                    |
| name            | string | tokenname                 |
| symbol          | string | tokensymbol                 |
| amount          | string | tokenquantity                 |
| decimals        |  long  | tokenSupported Decimal Places            |
| blockHeight     |  long  | Block height during contract creation              |
| status          |  int   | Contract status(0-Not present, 1-normal, 2-termination) |

### sc\_token\_transfer\_list
tokenTransfer transaction list/token transfer list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name        |  Parameter type  | Parameter Description | Is it not empty |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | chainID  |  yes   |
| address    | string | Account address |  yes   |
| pageNumber |  int   | Page number   |  no   |
| pageSize   |  int   | Page size |  no   |

#### Return value
| Field Name             |  Field type  | Parameter Description                           |
| --------------- |:------:| ------------------------------ |
| contractAddress | string | Contract address                           |
| from            | string | Payer                            |
| to              | string | Payee                            |
| value           | string | Transfer amount                           |
| time            |  long  | Transaction time                           |
| status          |  byte  | Transaction status（0 - Confirming, 1 - Confirmed, 2 - fail） |
| txHash          | string | transactionhash                         |
| blockHeight     |  long  | block height                           |
| name            | string | tokenname                        |
| symbol          | string | tokensymbol                        |
| decimals        |  long  | tokenSupported Decimal Places                   |
| info            | string | tokenAsset change information                    |

### sc\_account\_contracts
List of contract addresses for the account/account contract list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name        |  Parameter type  | Parameter Description | Is it not empty |
| ---------- |:------:| ---- |:----:|
| chainId    |  int   | chainID  |  yes   |
| address    | string | Account address |  yes   |
| pageNumber |  int   | Page number   |  no   |
| pageSize   |  int   | Page size |  no   |

#### Return value
| Field Name             |  Field type  | Parameter Description                                                   |
| --------------- |:------:| ------------------------------------------------------ |
| contractAddress | string | Contract address                                                   |
| createTime      |  long  | Contract creation time                                                 |
| height          |  long  | Block height during contract creation                                              |
| confirmCount    |  long  | Contract creation confirmation times                                               |
| alias           | string | Contract alias                                                   |
| status          |  int   | Contract status（0 - Does not exist or is being created, 1 - normal, 2 - Removed, 3 - Creation failed, 4 - Locked） |
| msg             | string | Error message for contract creation failure                                            |

### sc\_upload
Contract codejarPackage upload/upload
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description                                         | Is it not empty |
| ----------- |:------:| -------------------------------------------- |:----:|
| chainId     |  int   | chainid                                          |  yes   |
| jarFileData | string | File description and file byte stream conversionBase64Encoding string（File description andBase64String separated by commas） |  yes   |

#### Return value
| Field Name                                                                                                      |      Field type       | Parameter Description                 |
| -------------------------------------------------------------------------------------------------------- |:---------------:| -------------------- |
| constructor                                                                                              |     object      | Contract constructor details             |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name                                                     |     string      | Method Name                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;desc                                                     |     string      | Method description                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;args                                                     | list&lt;object> | Method parameter list               |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;type     |     string      | Parameter type                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name     |     string      | Parameter Name                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;required |     boolean     | Is it mandatory to fill in                 |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;returnArg                                                |     string      | return type                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;view                                                     |     boolean     | View Method（Call this method and the data will not be linked）   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;event                                                    |     boolean     | Is it an event                |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;payable                                                  |     boolean     | Is it an acceptable method for transferring main chain assets      |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jsonSerializable                                         |     boolean     | Is the method return valueJSONserialize       |
| isNrc20                                                                                                  |     boolean     | Is itNRC20contract           |
| code                                                                                                     |     string      | Smart Contract Code(BytecodeHexEncoding string) |

### sc\_call
call contract
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name             |    Parameter type    | Parameter Description                                     | Is it not empty |
| --------------- |:----------:| ---------------------------------------- |:----:|
| chainId         |    int     | chainid                                      |  yes   |
| sender          |   string   | Transaction creator account address                                |  yes   |
| password        |   string   | Caller account password                                  |  yes   |
| value           | biginteger | The amount of main network assets transferred by the caller to the contracted address, to be filled in when this service is not availableBigInteger.ZERO |  yes   |
| gasLimit        |    long    | GASlimit                                    |  yes   |
| price           |    long    | GASunit price                                    |  yes   |
| contractAddress |   string   | Contract address                                     |  yes   |
| methodName      |   string   | Contract method                                     |  yes   |
| methodDesc      |   string   | Contract method description, if the method in the contract is not overloaded, this parameter can be empty               |  no   |
| args            |  object[]  | parameter list                                     |  no   |
| remark          |   string   | Transaction notes                                     |  no   |

#### Return value
| Field Name    |  Field type  | Parameter Description        |
| ------ |:------:| ----------- |
| txHash | string | Transaction calling contracthash |

