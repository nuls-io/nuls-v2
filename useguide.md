# NULS2.0Wallet usage guide
## nuls.ncf
The wallet core configuration file allows for the configuration of all modules to be completed.
### Configuration file structure
The configuration file format adopts a classwindowThe system configuration file structure is divided into groups and parameters.

```
[network]   #group
port=18001  #parameterkeySum value
```
<<<<<<< HEAD
nuls.ncf 里面的内容看上去大概是这个样子
=======
nuls.ncfThe content inside looks like this
>>>>>>> toEn

```
[global]
encoding=UTF-8
language=en
...
[account]
keystoreFolder=/keystore/backup

[network]
port=10081
...
```
<<<<<<< HEAD
[global] 是一个特殊的组，所有的模块都会继承这个组里的配置项。全局通用配置可以配置在这个组下面。
[account]、[network]这两个是账户模块和网络模块的专有配置。名称对应模块Module.ncf里面的APP_NAME配置项。
当[global]和模块组下面有相同配置项时，模块组下面的配置优先级更高，覆盖global里的配置。
### 与模块内配置文件的关系
在模块内部（比如Modules/Nuls/account/1.0.0)目录下，有一个Module.ncf的配置文件，外部nuls.ncf的优先级高于模块内部的Module.ncf，当出现同名配置项时，nuls.ncf里的配置项将覆盖模块内部的同名配置项。
=======
[global]It is a special group, and all modules will inherit the configuration items in this group. Global configuration can be configured under this group.
[account]、[network]These two are proprietary configurations for the account module and network module. Name corresponding moduleModule.ncfInsideAPP_NAMEConfiguration items.
When[global]When there are the same configuration items under the module group, the priority of the configuration under the module group is higher, coveringglobalThe configuration inside.
### Relationship with configuration files within the module
Within the module（such asModules/Nuls/account/1.0.0)Under the directory, there is aModule.ncfConfiguration file, externalnuls.ncfThe priority of is higher than that of the internal modulesModule.ncfWhen a configuration item with the same name appears,nuls.ncfThe configuration items in the module will overwrite the configuration items with the same name inside the module.
>>>>>>> toEn

### Generate configuration file
When downloading or packaging the wallet program for the first time, it does not existnuls.ncfThe first execution of this configuration filestartorstart-devWhen, it will be automatically generatednuls.ncf.
### [nuls.ncfConfiguration Description List](#nuls.ncf)
## start
Wallet startup script, used in production environment to start wallet

```
./start
```
## shop
Wallet stop script, production environment uses this script to stop the wallet

```
./stop
```
## start-dev
start-upNULS2.0development environment(compatiblemacOSsystem)

```
./start-dev
```
## stop-dev
ceaseNULS2.0development environment

```
./stop-dev
```
## cmd
Start the command line to perform wallet operations.

```
./cmd
```
Specify the log level, the default log level isERROR,Optional log level:DEBUG、INFO、WARN、ERROR

```
./cmd -l DEBUG #Set the log level toDEBUG
```
Specify the configuration file path, the default configuration file is in the same directorynuls.ncf

```
./cmd -c /home/my.ncf 
```
## check-status
Check the startup status of the module. This function can quickly check whether each basic module has been successfully started. The script principle is to read the log flag bits in the log file.

```
./check-status
```
results of enforcement

```
==================MODULE PROCESS====================
account PROCESS IS START
block PROCESS IS START
consensus PROCESS IS START
ledger PROCESS IS START
network PROCESS IS START
transaction PROCESS IS START
==================RPC REDAY MODULE==================
account RPC READY
block RPC READY
consensus RPC READY
ledger RPC READY
network RPC READY
transaction RPC READY
======================REDAY MODULE==================
account STATE IS READY
block STATE IS READY
consensus STATE IS READY
ledger STATE IS READY
network STATE IS READY
transaction STATE IS READY
================TRY RUNNING MODULE==================
account TRY RUNNING
block TRY RUNNING
consensus TRY RUNNING
ledger TRY RUNNING
network TRY RUNNING
transaction TRY RUNNING
===================RUNNING MODULE===================
account STATE IS RUNNING
block STATE IS RUNNING
consensus STATE IS RUNNING
ledger STATE IS RUNNING
network STATE IS RUNNING
transaction STATE IS RUNNING
==================NULS WALLET STATE=================
==========================
NULS WALLET IS RUNNING
==========================
```
When I seeNULS WALLET IS RUNNINGWhen, it indicates successful startup.
## create-address
Generate account address and private key. Not dependent on wallet, can run independently to generate addresses.
```
./create-address
chainId:2
number:1
====================================================================================================
address   :tNULSeBaMi3UWVb1hMrsoEmv4XPPLW7CKmBVgn
privateKey:e27e3961384bc4749cb5bd535b16c90c4430d4da2cd34e1edd10b50b0d01fa1d
====================================================================================================
```
Specify the generated addresschainId

```
./create-address -c 1   #specifychainIdby1 ,（Default fromnuls.ncfRead fromchainId）
```
Generate a specified number of addresses

```
./create-address -n 100 #Batch generation100Addresses,(default1）
```
## appendix
### <span id="nuls.ncf">nuls.ncf configuration file</span>
#### Global configuration:group
| Configuration items | Value range | explain |
| --- | --- | --- |
| encoding | character set | defaultUTF-8Not recommended for modification |
| language | en/zh_CHS | Language pack. |
| logPath | Relative path to folder | Log file storage path, configuration file context relative path |
| logLevel | DEBUG,INFO,WARN,ERROR | log level |
| dataPath | Relative path to folder | Data file storage path, configuration file context relative path |
| chainId | positive integer | The chain of the default running chainid |
| assetId | positive integer | Default main asset of the running chainid |
| chainName | character string | The chain name of the default running chain |
| symbol | character string | The main asset symbol of the default running chain |
| decimals | positive integer | The number of digits to the right of the decimal point of the default asset |
| blackHolePublicKey | character string | Black Hole Address Public Key |

#### Network module configuration：network

| Configuration items | Value range | explain |
| --- | --- | --- |
| port | positive integer | Network communication port |
| crossPort | positive integer | Cross chain transaction communication port |
| packetMagic | positive integer | Network magic parameters, the same magic parameters are necessary for networking |
| selfSeedIps | character string | Default connected network nodesipMultiple separated by English commas |
| maxInCount | positive integer | The total number of connections allowed between external nodes and the current node |
| maxOutCount | positive integer | The total number of connections allowed between the current node and external nodes |

#### Account module configuration:account

| Configuration items | Value range | explain |
| --- | --- | --- |
| keystoreFolder | Folder path | Storage accountkeystoreThe path to the file, which is located in thedataPathPath value in the path |

#### Block module configuration:block

| Configuration items | Value range | explain |
| --- | --- | --- |
| blockMaxSize | positive integer | The maximum number of bytes that a block can store |
| extendMaxSize | positive integer | The maximum number of bytes that a block extension field can store |
| chainSwtichThreshold | positive integer | Threshold of height difference that triggers fork chain switching |
| maxRollback | positive integer  | When the local block is inconsistent with the network block,Maximum local rollback count |
| consistencyNodePercent | positive integer | Count the latest block height of nodes on the network、hashConsistent percentage threshold |
| minNodeAmount | positive integer | Minimum number of linked nodes,When the network node linked to is below this parameter,Will continue to wait |
| downloadNumber | positive integer | During block synchronization process,The number of blocks downloaded from nodes on the network each time |
| singleDownloadTimeout | positive integer | The timeout for downloading a single block from a network node |
| batchDownloadTimeout | positive integer | Time out for downloading multiple blocks from network nodes |
| cachedBlockSizeLimit | positive integer | Maximum number of cached block bytes during block synchronization process |

#### pocConsensus module configuration:consensus

| Configuration items | Value range | explain |
| --- | --- | --- |
| seedNodes | Address List | Seed node block address list, multiple addresses separated by English commas |
| password | character string | The default password for the block address of the seed node must match the password set when importing the address |
| packingInterval | positive integer | Block interval, in seconds, configured as10, representing every10Generate a block in seconds |
| agentAssetId | positive integer | Assets allowed to participate in consensusid |
| agentChainId | positive integer | Chain of assets allowed to participate in consensusid |
| awardAssetId | positive integer | Assets of consensus rewardsid（Must be in the chain where the consensus asset is located, i.e. the asset chainididentical） |
| feeUnit | positive integer | Unit price of handling fee |
| packingInterval | Long integral type | Block output interval time |
| redPublishLockTime | Long integral type | Node red card deposit lock up time |
| stopAgentLockTime | Long integral type | Stop node margin locking time |
| commissionRateMin | byte | Minimum proportion of consensus reward commission                                         |
| commissionRateMax | byte | Maximum proportion of consensus node consensus reward commission |
| depositMin | BIgInteger | Create minimum margin value for nodes |
| depositMax | BIgInteger | Create maximum margin value for nodes |
| commissionMin | BIgInteger | Minimum commission amount for node block output |
| commissionMax | BIgInteger | Maximum commission amount for nodes |
| entrusterDepositMin | BIgInteger | The latest amount for a single commission |

#### Smart contract configuration:smart_contract

| Configuration items | Value range | explain |
| --- | --- | --- |
| maxViewGas | positive integer | The maximum consumption of contract view method callsGas |

#### public-serviceModule Configuration:public-service

| Configuration items | Value range | explain |
| --- | --- | --- |
| rpcPort | Port number | httpThe port number used by the interface |
| databaseUrl | ipaddress | mongodbdatabaseipaddress |
| databasePort | Port number | mongodbDatabase port number |
| maxAliveConnect | positive integer | The maximum number of connections in the database connection pool |
| maxWaitTime | positive integer | The maximum waiting time for obtaining a connection from the database(millisecond） |
| connectTimeOut | positive integer| Database connection timeout（millisecond）|

#### Cross chain module:cross-chain
| Configuration items | Value range | explain |
| --- | --- | --- |
| minNodeAmount | positive integer | The minimum value of cross chain node links |
| maxNodeAmount | positive integer | Maximum value of cross chain node links |
| sendHeight | positive integer | Cross chain transactions confirm the number of blocks within this chain |
| byzantineRatio | positive integer | Byzantine proportion of cross chain transactions |
| crossSeedIps | ipAddress List | Main network cross chain seed node list |

