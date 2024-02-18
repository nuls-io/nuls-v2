# Account module
## Module Overview
The account module is a fundamental module that provides information about various functions of the account. Mainly for account generation、Security and storage、Support is provided for several functions such as information acquisition, while other modules can use various functions of the account and obtain account information based on the interface provided by the account module. Users or other applications can useRPCThe interface provides more practical and personalized operations for accounts. Accounts are the basic module and also the carrier of user data .
## Interface functions
- Account generation
- Create an account、Import account
- Account security and custody
- Account backup、Set account password、Change account password、Remove account
- Obtaining account information
- Query individual account information、Obtain information from multiple accounts、Obtain account address、Query account balance、Query account alias
- Other practical and personalized features  Set account alias、Set account notes、Verify if the account is encrypted、autograph、Verify account address format、Verify account password correctness and other functions


## Interface List
### ac\_removeAccount
Remove specified account/Remove specified account
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| address  | string | Account address |  yes   |
| password | string | Account password |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description |
| ----- |:-------:| ---- |
| value | boolean | Whether successful |

### ac\_getAccountList
Get all account collections,And put it in cache/query all account collections and put them in cache
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name                                                                |      Field type       | Parameter Description   |
| ------------------------------------------------------------------ |:---------------:| ------ |
| list                                                               | list&lt;object> | Return account collection |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address            |     string      | Account address   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;alias              |     string      | alias     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubkeyHex          |     string      | Public key     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPrikeyHex |     string      | Encrypted private key  |

### ac\_signDigest
Data Summary Signature/Data digest signature
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description  | Is it not empty |
| -------- |:------:| ----- |:----:|
| chainId  |  int   | chainid   |  yes   |
| address  | string | Account address  |  yes   |
| password | string | Account password  |  yes   |
| data     | string | Data to be signed |  yes   |

#### Return value
| Field Name       |  Field type  | Parameter Description  |
| --------- |:------:| ----- |
| signature | string | Data after signature |

### ac\_getAccountByAddress
Obtain account information through address/get account info according to address
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| address | string | Account address |  yes   |

#### Return value
| Field Name                |  Field type  | Parameter Description  |
| ------------------ |:------:| ----- |
| address            | string | Account address  |
| alias              | string | alias    |
| pubkeyHex          | string | Public key    |
| encryptedPrikeyHex | string | Encrypted private key |

### ac\_signBlockDigest
Block Data Summary Signature/Block data digest signature
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description  | Is it not empty |
| -------- |:------:| ----- |:----:|
| chainId  |  int   | chainid   |  yes   |
| address  | string | Account address  |  yes   |
| password | string | Account password  |  yes   |
| data     | string | Data to be signed |  yes   |

#### Return value
| Field Name       |  Field type  | Parameter Description  |
| --------- |:------:| ----- |
| signature | string | Data after signature |

### ac\_setRemark
Set notes for the account/Set remark for accounts
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| address | string | Account address |  yes   |
| remark  | string | Remarks   |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description |
| ----- |:-------:| ---- |
| value | boolean | Whether successful |

### ac\_importAccountByPriKey
Import account based on private key/Import accounts by private key
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |  Parameter type   | Parameter Description         | Is it not empty |
| --------- |:-------:| ------------ |:----:|
| chainId   |   int   | chainid          |  yes   |
| password  | string  | Set a new password        |  yes   |
| priKey    | string  | Account private key         |  yes   |
| overwrite | boolean | If the account already exists,Is it covered |  yes   |

#### Return value
| Field Name     |  Field type  | Parameter Description    |
| ------- |:------:| ------- |
| address | string | Imported account address |

### ac\_createOfflineAccount
Create an offline account, This account is not saved to the database, And will directly return all information of the account/create an offline account, which is not saved to the database and will directly return all information to the account.
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description      | Is it not empty |
| -------- |:------:| --------- |:----:|
| chainId  |  int   | chainid       |  yes   |
| count    |  int   | Number of accounts to be created |  yes   |
| password | string | Account password      |  yes   |

#### Return value
| Field Name                                                             |      Field type       | Parameter Description   |
| --------------------------------------------------------------- |:---------------:| ------ |
| list                                                            | list&lt;object> | Offline account collection |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address         |     string      | Account address   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;pubKey          |     string      | Public key     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;priKey          |     string      | Private key     |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encryptedPriKey |     string      | Encrypted private key |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;extend          |     string      | Other information   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;createTime      |      long       | Creation time   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;encrypted       |     boolean     | Is the account encrypted |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;remark          |     string      | Account notes   |

### ac\_createContractAccount
Create a smart contract account/create smart contract account
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name     |  Field type  | Parameter Description   |
| ------- |:------:| ------ |
| address | string | Smart contract address |

### ac\_getEncryptedAddressList
Get a list of local encrypted accounts/Get a list of locally encrypted accounts
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name  |      Field type       | Parameter Description     |
| ---- |:---------------:| -------- |
| list | list&lt;string> | Return account address set |

### ac\_getAddressList
Pagination query account address list/Paging query account address list
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name        | Parameter type | Parameter Description   | Is it not empty |
| ---------- |:----:| ------ |:----:|
| chainId    | int  | chainid    |  yes   |
| pageNumber | int  | Page number     |  yes   |
| pageSize   | int  | Number of records per page |  yes   |

#### Return value
| Field Name |      Field type       | Parameter Description            |
| --- |:---------------:| --------------- |
| Return value | list&lt;string> | Return aPageObject, Account Collection |

### ac\_getPriKeyByAddress
By account address and password,Query account private key/Inquire the account's private key according to the address
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| address  | string | Account address |  yes   |
| password | string | Account password |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description |
| ------ |:------:| ---- |
| priKey | string | Private key   |
| pubKey | string | Public key   |

### ac\_getAllPriKey
To obtain all local account private keys, it is necessary to ensure that all account passwords are consistent. If the passwords in the local account are inconsistent, an error message will be returned/Get the all local private keys. if the password in the local account is different, the error message will be returned.
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| password | string | Account password |  yes   |

#### Return value
| Field Name  |      Field type       | Parameter Description |
| ---- |:---------------:| ---- |
| list | list&lt;string> | Private key set |

### ac\_importAccountByKeystore
according toAccountKeyStoreImport account/Import accounts by AccountKeyStore
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |  Parameter type   | Parameter Description         | Is it not empty |
| --------- |:-------:| ------------ |:----:|
| chainId   |   int   | chainid          |  yes   |
| password  | string  | Set a new password        |  yes   |
| keyStore  | string  | keyStorecharacter string  |  yes   |
| overwrite | boolean | If the account already exists,Is it covered |  yes   |

#### Return value
| Field Name     |  Field type  | Parameter Description    |
| ------- |:------:| ------- |
| address | string | Imported account address |

### ac\_exportKeyStoreJson
exportAccountKeyStorecharacter string/export account KeyStore json
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| address  | string | Account address |  yes   |
| password | string | Account password |  yes   |

#### Return value
| Field Name      |  Field type  | Parameter Description        |
| -------- |:------:| ----------- |
| keyStore | string | keyStorecharacter string |

### ac\_exportAccountKeyStore
Account backup, exportAccountKeyStorecharacter string/export account KeyStore
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| address  | string | Account address |  yes   |
| password | string | Account password |  yes   |
| filePath | string | Backup address |  no   |

#### Return value
| Field Name  |  Field type  | Parameter Description      |
| ---- |:------:| --------- |
| path | string | The actual backup file address |

### ac\_updatePassword
Change the account password based on the original password/Modify the account password by the original password
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description  | Is it not empty |
| ----------- |:------:| ----- |:----:|
| chainId     |  int   | chainid   |  yes   |
| address     | string | Account address  |  yes   |
| password    | string | Old account password |  yes   |
| newPassword | string | Account New Password |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description   |
| ----- |:-------:| ------ |
| value | boolean | Is it successfully set up |

### ac\_updateOfflineAccountPassword
Offline account password modification/Offline account change password
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description  | Is it not empty |
| ----------- |:------:| ----- |:----:|
| chainId     |  int   | chainid   |  yes   |
| address     | string | Account address  |  yes   |
| password    | string | Old account password |  yes   |
| newPassword | string | Account New Password |  yes   |
| priKey      | string | Account private key  |  yes   |

#### Return value
| Field Name             |  Field type  | Parameter Description       |
| --------------- |:------:| ---------- |
| encryptedPriKey | string | Return the encrypted private key after modification |

### ac\_validationPassword
Verify if the account password is correct/Verify that the account password is correct
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| address  | string | Account address |  yes   |
| password | string | Account password |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description     |
| ----- |:-------:| -------- |
| value | boolean | Is the account password correct |

### ac\_verifySignData
Verify data signature/Verification Data Signature
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name    |  Parameter type  | Parameter Description  | Is it not empty |
| ------ |:------:| ----- |:----:|
| pubKey | string | Account public key  |  yes   |
| sig    | string | autograph    |  yes   |
| data   | string | Data to be signed |  yes   |

#### Return value
| Field Name       |  Field type   | Parameter Description   |
| --------- |:-------:| ------ |
| signature | boolean | Is the signature correct |

### ac\_createAccount
Create a specified number of accounts/create a specified number of accounts
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description      | Is it not empty |
| -------- |:------:| --------- |:----:|
| chainId  |  int   | chainid       |  yes   |
| count    |  int   | Number of accounts to be created |  yes   |
| password | string | Account password      |  yes   |

#### Return value
| Field Name  |      Field type       | Parameter Description      |
| ---- |:---------------:| --------- |
| list | list&lt;string> | The set of account addresses created |

### ac\_getPubKey
Based on account address and password,Query account public key/Get the account's public key
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| address  | string | Account address |  yes   |
| password | string | Account password |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description |
| ------ |:------:| ---- |
| pubKey | string | Public key   |

### ac\_getAliasByAddress
Retrieve aliases based on address/get the alias by address
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| address | string | Account address |  yes   |

#### Return value
| Field Name   |  Field type  | Parameter Description |
| ----- |:------:| ---- |
| alias | string | alias   |

### ac\_setAlias
Set alias/Set the alias of account
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |  Parameter type  | Parameter Description | Is it not empty |
| -------- |:------:| ---- |:----:|
| chainId  |  int   | chainid  |  yes   |
| address  | string | Account address |  yes   |
| password | string | Account password |  yes   |
| alias    | string | alias   |  yes   |

#### Return value
| Field Name    |  Field type  | Parameter Description       |
| ------ |:------:| ---------- |
| txHash | string | Set up alias transactionshash |

### ac\_isAliasUsable
Check if aliases are available/check whether the account is usable
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description | Is it not empty |
| ------- |:------:| ---- |:----:|
| chainId |  int   | chainid  |  yes   |
| alias   | string | alias   |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description     |
| ----- |:-------:| -------- |
| value | boolean | Can aliases be used |

### ac\_getAllAddressPrefix
Get address prefixes for all chains
#### scope:public
#### version:1.0

#### parameter list
No parameters

#### Return value
| Field Name           |  Field type   | Parameter Description |
| ------------- |:-------:| ---- |
| chainId       | integer | chainid  |
| addressPrefix | string  | Address prefix |

### ac\_getAddressPrefixByChainId
By chainidGet address prefix
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description | Is it not empty |
| ------- |:----:| ---- |:----:|
| chainId | int  | chainid  |  yes   |

#### Return value
| Field Name           |  Field type   | Parameter Description |
| ------------- |:-------:| ---- |
| chainId       | integer | chainid  |
| addressPrefix | string  | Address prefix |

### ac\_addAddressPrefix
Add address prefix,The chain management module will call this interface
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name                                                           |  Parameter type   | Parameter Description    | Is it not empty |
| ------------------------------------------------------------- |:-------:| ------- |:----:|
| prefixList                                                    |  list   | Chain Address Prefix List |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;chainId       | integer | chainid     |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addressPrefix | string  | Address prefix    |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### ac\_transfer
Create a regular transfer transaction/create transfer transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name                                                           |    Parameter type    | Parameter Description                          | Is it not empty |
| ------------------------------------------------------------- |:----------:| ----------------------------- |:----:|
| chainId                                                       |    int     | chainid                           |  yes   |
| inputs                                                        |    list    | Transaction payer data                       |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | Account address                          |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | The chain of assetsID                        |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | assetID                          |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | quantity                            |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password      |   string   | Transfer out of account(from)Password for, Assembly recipient(to)Ignoring data |  no   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime      |    long    | Unlock time, -1To keep locked, 0To not lock(default)      |  no   |
| outputs                                                       |    list    | Transaction recipient data                       |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | Account address                          |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | The chain of assetsID                        |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | assetID                          |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | quantity                            |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password      |   string   | Transfer out of account(from)Password for, Assembly recipient(to)Ignoring data |  no   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime      |    long    | Unlock time, -1To keep locked, 0To not lock(default)      |  no   |
| remark                                                        |   string   | Transaction notes                          |  yes   |

#### Return value
| Field Name   |  Field type  | Parameter Description   |
| ----- |:------:| ------ |
| value | string | transactionhash |

### ac\_createMultiSignTransfer
Create multiple address transfer transactions/create multi sign transfer
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name                                                           |    Parameter type    | Parameter Description                     | Is it not empty |
| ------------------------------------------------------------- |:----------:| ------------------------ |:----:|
| chainId                                                       |    int     | chainid                      |  yes   |
| inputs                                                        |    list    | Transaction payer data                  |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | Account address                     |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | The chain of assetsID                   |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | assetID                     |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | quantity                       |  yes   |
| outputs                                                       |    list    | Transaction recipient data                  |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;address       |   string   | Account address                     |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsChainId |  integer   | The chain of assetsID                   |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assetsId      |  integer   | assetID                     |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;amount        | biginteger | quantity                       |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lockTime      |    long    | Unlock time, -1To keep locked, 0To not lock(default) |  no   |
| remark                                                        |   string   | Transaction notes                     |  yes   |
| signAddress                                                   |   string   | First signature account address(If left blank, only create transactions without signing)   |  no   |
| signPassword                                                  |   string   | First signature account password(If left blank, only create transactions without signing)   |  no   |

#### Return value
| Field Name       |  Field type   | Parameter Description                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign          |
| txHash    | string  | transactionhash                                |
| completed | boolean | true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures |

### ac\_signMultiSignTransaction
Multiple transaction signatures/sign MultiSign Transaction
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |  Parameter type  | Parameter Description    | Is it not empty |
| ------------ |:------:| ------- |:----:|
| chainId      |  int   | chainid     |  yes   |
| tx           | string | Transaction data string |  yes   |
| signAddress  | string | Signature account address  |  yes   |
| signPassword | string | Signature account password  |  yes   |

#### Return value
| Field Name       |  Field type   | Parameter Description                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign          |
| txHash    | string  | transactionhash                                |
| completed | boolean | true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures |

### ac\_createMultiSignAccount
Create a multi signature account/create a multi sign account
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name      |      Parameter type       | Parameter Description                            | Is it not empty |
| -------- |:---------------:| ------------------------------- |:----:|
| chainId  |       int       | chainid                             |  yes   |
| pubKeys  | list&lt;string> | Public key set(Public key of any ordinary address or ordinary account address existing in the current node) |  yes   |
| minSigns |       int       | Minimum number of signatures                           |  yes   |

#### Return value
| Field Name     |  Field type  | Parameter Description   |
| ------- |:------:| ------ |
| address | string | Multiple account addresses signed |

### ac\_removeMultiSignAccount
Remove multiple signed accounts/remove the multi sign account
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description   | Is it not empty |
| ------- |:------:| ------ |:----:|
| chainId |  int   | chainid    |  yes   |
| address | string | Multiple account addresses signed |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description   |
| ----- |:-------:| ------ |
| value | boolean | Was removal successful |

### ac\_setMultiSignAlias
Set multiple account aliases/set the alias of multi sign account
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |  Parameter type  | Parameter Description                   | Is it not empty |
| ------------ |:------:| ---------------------- |:----:|
| chainId      |  int   | chainid                    |  yes   |
| address      | string | Multiple account addresses signed                 |  yes   |
| alias        | string | alias                     |  yes   |
| signAddress  | string | First signature account address(If left blank, only create transactions without signing) |  no   |
| signPassword | string | First signature account password(If left blank, only create transactions without signing) |  no   |

#### Return value
| Field Name       |  Field type   | Parameter Description                                  |
| --------- |:-------:| ------------------------------------- |
| tx        | string  | Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign          |
| txHash    | string  | transactionhash                                |
| completed | boolean | true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures |

### ac\_getMultiSignAccount
Obtain the complete multi signature account based on the address of the multi signature account/Search for multi-signature account by address
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description   | Is it not empty |
| ------- |:------:| ------ |:----:|
| chainId |  int   | chainid    |  yes   |
| address | string | Multiple account addresses signed |  yes   |

#### Return value
| Field Name   |  Field type  | Parameter Description         |
| ----- |:------:| ------------ |
| value | string | Serializing data strings for multiple account signatures |

### ac\_isMultiSignAccountBuilder
Verify if one of the creators of the multi signed account/Whether it is multiSign account Builder
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description              | Is it not empty |
| ------- |:------:| ----------------- |:----:|
| chainId |  int   | chainid               |  yes   |
| address | string | Multiple account addresses signed            |  yes   |
| pubKey  | string | Creator public key or address that already exists on the current node |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description         |
| ----- |:-------:| ------------ |
| value | boolean | Is it one of the creators who signed multiple accounts |

