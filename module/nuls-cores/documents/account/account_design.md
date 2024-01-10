# Account module design document

[TOC]

## Overall Overview

### Module Overview

#### Why do we need an account module

The address and its related information play an extremely important role in blockchain, which is related to data security issues. Moreover, the information related to the account address is also the most interactive part with the user. Although the structure of the entire account address information is simple, its functions are quite complex. Therefore, we have developed a separate account module to handle the relevant information and functions of the account address.

#### What does the account need to do

The account module is a fundamental module that provides information about various functions of the account. Mainly for account generation、Security and storage、Support is provided for several functions such as information acquisition, while other modules can use various functions of the account and obtain account information based on the interface provided by the account module. Users or other applications can useRPCThe interface provides more practical and personalized operations for accounts. Accounts are the basic module and also the carrier of user data .

- Account generation

  Create an account、Import account

- Account security and custody

  Account backup、Set account password、Change account password、Remove account

- Obtaining account information

  Query individual account information、Obtain information from multiple accounts、Obtain account address、Query account balance、Query account alias

- Other practical and personalized features

  Set account alias、Set account notes、Signature, etc

#### Account positioning in the system

![](./img/account-context.png)

Accounts are the underlying modules that are linked to the ledger、Consensus、transaction、kernel、Event bus、The community governance module has dependencies.

1、The account module relies on the ledger module

	ledgerThe module needs to handle local transactions and relies on account information.
	
	The account module needs to initiate an alias setting transaction, which requiresledgerModule payment fees
	
	Account balance query requires dependencyledgermodule

2、The account module depends on the kernel module

```
Report module information、Shared data operation
```

3、The account module depends on the network module

```
Receive and send data through network modules
```

4、The account module depends on the event bus module

```
Create an account、Delete account、Password modification event sends messages through the event bus module
The account module is not strongly dependent on the event bus module, as even if the event sending fails, it does not affect the normal business process
```

5、Consensus module relies on account module

```
Consensus requires account information to be packaged into blocks
```

6、The transaction management module relies on the account module

```
The transaction management module needs to verify transactions, relying on the accountaddressFunction, verifying address legality, etc
```

7、The community governance module relies on the account module

```
Community governance requires account signature
```

### Architecture diagram

![](./img/account-module.png)



1、API：Provide an interface layer externally and create accounts、backups、Set aliases and other operations；

2、Business logic layer：Defining accounts、Account address、The function of aliases；

3、Data Persistence Layer：Save Account、Alias data；

## functional design

### Functional architecture diagram

![](./img/account-functions.png)



### NULSProtocol Address Explanation

#### ECKey  

Create aNULSThe first step in addressing is to obtain a public-private key pair based on the elliptic curve algorithm.NULSThe elliptic curve parameters are the same as Bitcoin, usingsecp256k1.

#### Address Format  

```
address = prefix + Base58Encode(chainId+addressType+pkh+xor)
```

- address Count Reg23

- chainIdChain for the current chainid, used to distinguish addresses from different chains

- addressTypeAddress types are divided into ,1:Regular address,2:Smart contract address,3:Multiple signature addresses.

#### prefix  

prefixprefixThe existence of is for easy identification、Distinguish the addresses of different chains. at presentNULSProvides two optionsprefixDetermine the plan:

1. default setting：NULShold1For the main networkchainId, also default to allchainIdby1The address of theNULSBeginning. hold2For the core testing networkchainId, default to allchainIdby2The address of thetNULSBeginning.
2. By registering cross chain prefix settings：When registering for cross chain, it is necessary to fill in the prefix of this chain, and the system will maintain itchainIdGenerate corresponding addresses based on the corresponding table of prefixes.
3. Automatic calculation：otherchainIdThe address,NULSA unified algorithm is provided to calculate prefixes, and the specific calculation code is as follows：

```
//takechainIdConvert to a byte array usingbase58The algorithm calculates byte arrays and converts them all to uppercase letters after calculation
String prefix = Base58.encode(SerializeUtils.int16ToBytes(chainId)).toUpperCase();
```

Separate the prefix from the actual address with a lowercase letter to facilitate extraction from the addresschainIdVerify the address type and correctness.
The selection method for lowercase letters is to provide an array, fill in lowercase letters in the order of installing the alphabet, and follow theprefixSelect the separated letters based on their length.

```
//How many letters is the length of the prefix? Choose the element that separates the letters.
//If the prefix length is2, then usebDivided by a length of3usecDivided by a length of4usedSeparation,……
String[] LENGTHPREFIX = new String[]{"", "a", "b", "c", "d", "e"};
```

#### chainid  

NULSThe goal is to establish a blockchain ecosystem with multiple parallel chains and interconnected values, defining a unique one for each chain from the beginning of its designID,2Bytes, value range1~65535.ChainIdIt is a very important data in the address and the foundation of cross chain operations.

#### Account type  

NULSSupport setting different account types within a network, such as regular addresses、Contract address、Developers can design according to their own needs, such as signing multiple addresses and so on.
The account type is1Bytes,1~128Value range

#### Public Key SummaryPKH  

ECKeyThe association with the address is reflected in this section,NULSThe approach is to first useSha-256Perform a calculation on the public key and obtain the result through RIPEMD160Perform a calculation to obtain20The result of one byte isPKH.

#### Check bit  

NULSWhen generating an address in string format, an additional byte of checksum will be added, and the calculation method is based on the previous one23Bytes（chainId+type+pkh）Obtained by XOR.
The checksum does not participate in serialization.

#### Generate Address  

- Serialized Address

  ```
  address = chainId(2) + type(1) + PKH(20)
  ```

- Fixed prefix string address

  ```
  addressString = prefix + Delimiter + Base58Encode(address+xor)
  ```

- Automatic prefix string address

  ```
  addressString = Base58Encode(chainId) + Delimiter + Base58Encode(address+xor)
  ```

#### wrongnulsThe address format of the system  

NULSIt is a network that supports access to all blockchains, for both andNULSCompletely different address formats,NULSDesigned an address translation protocol, with the specific content as follows：

copy

```
address = Base58Encode(chainId+Original address length+Original address+xor)
```

for example：Bitcoin address, add two bytes before the addresschainIdThen follow the original address of Bitcoin, and the address resolution method is determined by the chain configuration to ensure that any address can be resolved within theNULSObtain the mapped address.  

### Multi signature account

reference[Multi signed account documents]()



## Module Services

reference[Account moduleRPC-APIInterface documentation](./account.md)

## protocol

### Transaction Business Data Protocol

* Set alias

  * protocol

    Compared to general transactions, only types andtxDataThere are differences, the specific differences are as follows

  ```
  type: n //Set the type of alias transaction
  txData:{
      address:  //VarByte Set the address for the alias
      alias：   //VarByte Byte array converted from alias string, usingUTF-8decoding
  }
  ```

  - Alias transaction parameters

  | Len  | Fields  | Data Type | Remark                                |
  | ---- | ------- | --------- | ------------------------------------- |
  | 24   | address | byte[]    | Set the address for the alias                        |
  | 32   | alias   | byte[]    | Byte array converted from alias string, usingUTF-8decoding |

  * Validator

  ```
  1、Validation of alias format validity
  2、The address must be a satellite link address, and only one alias can be set for each address
  3、Burn down onetokenunit
  4、Transaction fees
  5、autograph：Set address、input、Three party signature verification
  ```

  * processor

  ```
  1、Asset processor
  2、storagealiasdata
  3、Update local account information
  ```



## JavaUnique design

* AccountObject design

  The table is stored usingkey：

  NULSsystem：chainId+type+hash160

  wrongNULSsystem：chainId+length+address


| `Field Name`      | ` type` | `explain`                                      |
| :-------------- | :------ | :------------------------------------------ |
| chainId         | short   | chainID                                        |
| address         | String  | Account address（Base58(address)+Base58(chainId)） |
| alias           | String  | Account Aliases                                    |
| status          | Integer | Default account（Do not save）                      |
| pubKey          | byte[]  | Public key                                        |
| priKey          | byte[]  | Private key-unencrypted                                 |
| encryptedPriKey | byte[]  | Encrypted private key                                  |
| extend          | byte[]  | Extended data                                    |
| remark          | String  | Remarks                                        |
| createTime      | long    | Creation time                                    |

* AddressObject design（Non persistent storage）

| `Field Name`   | ` type` | `explain`       |
| ------------ | ------- | ------------ |
| chainId      | short   | chainID         |
| addressType  | byte    | Address type     |
| hash160      | byte[]  | Public keyhash     |
| addressBytes | byte[]  | Address Byte Array |

- AliasObject design

  The table is stored usingkey：

  addressandaliasAs separatekeystorage,Store two copies of alias data

  Different alias tables need to be created according to different chains

| `Field Name` | ` type` | `explain`   |
| ---------- | ------- | -------- |
| address    | byte[]  | Account address |
| alias      | String  | Account Aliases |

- MultiSigAccountObject design

| `Field Name` | ` type`      | `explain`             |
| ---------- | ------------ | ------------------ |
| address    | String       | Account address           |
| pubKeyList | List<byte[]> | List of public keys that require signature |
| minSigns   | long         | Minimum number of signatures         |



