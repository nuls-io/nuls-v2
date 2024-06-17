# Network module

## Why do we need to have《Network module》

The network module ensures communication between decentralized nodes, providingNULSOne of the basic modules, providing the lowest level network communication、Node discovery and other services. The network foundation of blockchain isPeer to  Peer,NamelyP2P.P2PAll participants in the network can provide services（server）, can also be a resource user（client）.P2PThe characteristics of the network：Decentralization、Scalability、Robustness、High cost performance ratio、Privacy protection、Load balancing.

## 《Network module》What to do

The network module is the fundamental module of the entire system, used to manage nodes、The connections between nodes and their status、Sending and receiving data. The network module does not involve complex business logic.

* The received network message is pushed to the corresponding processing module based on the instruction service mapping relationship in the kernel module.

* Open interfaces for message calls encapsulated by other modules to be pushed to the specifiedpeerNode and broadcast to the specified network group.

## 《Network module》Positioning in the system

* The network module is the underlying application module, and any module that requires network communication must transmit and receive messages through the network module.
* The network module relies on the core module for service interface governance.
* Network module by networkid（Magic parameters） To build different networks.
* When the nodes in the satellite chain of the network module are constructing a cross chain network, the chain management module needs to provide cross chain configuration information.
* When nodes in the sub chain of a network module are constructing a cross chain network, the cross chain module needs to provide cross chain configuration information.



## Module Configuration

```
#This chain service port
port=18001
#Cross chain service port
crossPort=18002
#Magic parameters
packetMagic=55886633
#Seed Connection Node
selfSeedIps=192.168.1.12:18001
#Maximum number of network connections
maxInCount=100
#Maximum number of outbound connections
maxOutCount=20
```


## Interface List
### nw\_info
Obtain basic information about node networks
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                 | Is it not empty |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | Connected ChainId,Value range[1-65535] |  yes   |

#### Return value
| Field Name             |  Field type   | Parameter Description                 |
| --------------- |:-------:| -------------------- |
| localBestHeight |  long   | Local node block height             |
| netBestHeight   |  long   | The highest height of network node blocks           |
| timeOffset      |  long   | Node and network time difference value           |
| inCount         | integer | the mostServer,peerAccess quantity    |
| outCount        | integer | As aclientConnect externalServerquantity |

### nw\_nodes
Obtain network connection node information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                 | Is it not empty |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | Connected ChainId,Value range[1-65535] |  yes   |

#### Return value
| Field Name         |  Field type  | Parameter Description     |
| ----------- |:------:| -------- |
| peer        | string | peernodeID |
| blockHeight |  long  | Node height     |
| blockHash   | string | nodeHash   |

### nw\_currentTimeMillis
Obtain node network time
#### scope:public
#### version:1.0

#### parameter list
No parameters

#### Return value
| Field Name               | Field type | Parameter Description                   |
| ----------------- |:----:| ---------------------- |
| currentTimeMillis | long | Time milliseconds-currentTimeMillis |

### nw\_delNodes
Delete node group node
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | Connected ChainId,Value range[1-65535] |  yes   |
| nodes   | string | Node groupIDComma splicing           |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### nw\_addNodes
Add nodes to be connected
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | Connected ChainId,Value range[1-65535] |  yes   |
| isCross |  int   | 1Cross chain connection,0Normal connection          |  yes   |
| nodes   | string | Node groupIDComma splicing           |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### nw\_getNodes
Paging to view connection node information,startPageRelated topageSize All for0When not paginated, returns all node information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       |  Parameter type   | Parameter Description                  | Is it not empty |
| --------- |:-------:| --------------------- |:----:|
| chainId   |   int   | Connected ChainId,Value range[1-65535]  |  yes   |
| state     |   int   | 0:All connections,1:Connected  2:Not connected   |  yes   |
| isCross   | boolean | false:Non cross chain connection,true:Cross chain connection |  yes   |
| startPage |   int   | Number of starting pages for pagination                |  yes   |
| pageSize  |   int   | Display quantity per page                |  yes   |

#### Return value
| Field Name         |  Field type  | Parameter Description               |
| ----------- |:------:| ------------------ |
| chainId     |  int   | chainID                |
| nodeId      | string | nodeID               |
| magicNumber |  long  | Network Magic Parameters             |
| blockHeight |  long  | peerNode block height         |
| blockHash   | string | peerLatest Blockhash       |
| ip          | string | peerconnectIPaddress         |
| port        |  int   | peerConnection port number          |
| state       |  int   | 0:Unfinished handshake 1:Connection with completed handshake |
| isOut       |  int   | 0:Network connection 1:Outgoing network connection      |
| time        |  long  | Connection time in milliseconds             |

### nw\_updateNodeInfo
Update connection node information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description                 | Is it not empty |
| ----------- |:------:| -------------------- |:----:|
| chainId     |  int   | Connected ChainId,Value range[1-65535] |  yes   |
| nodeId      | string | Connecting nodesID               |  yes   |
| blockHeight |  long  | block height                 |  yes   |
| blockHash   | string | blockhashvalue              |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### protocolRegisterWithPriority
Module protocol instruction registration with priority parameters
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name                                                      |  Parameter type  | Parameter Description                        | Is it not empty |
| -------------------------------------------------------- |:------:| --------------------------- |:----:|
| role                                                     | string | Module Role Name                      |  yes   |
| protocolCmds                                             |  list  | Register Instruction List                      |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cmd      | string | Protocol instruction name,12byte               |  yes   |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;priority | string | priority,3Level,HIGH,DEFAULT,LOWER |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### nw\_protocolRegister
Module Protocol Instruction Registration
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |      Parameter type       | Parameter Description   | Is it not empty |
| ------------ |:---------------:| ------ |:----:|
| role         |     string      | Module Role Name |  yes   |
| protocolCmds | list&lt;string> | Register Instruction List |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### nw\_sendPeersMsg
Send messages to specified nodes
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name         |  Parameter type  | Parameter Description                   | Is it not empty |
| ----------- |:------:| ---------------------- |:----:|
| chainId     |  int   | Connected ChainId,Value range[1-65535]   |  yes   |
| nodes       | string | Specify sendingpeernodeIdString concatenated with commas |  yes   |
| messageBody | string | Message BodyHex                 |  yes   |
| command     | string | Message Protocol Instructions                 |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### nw\_broadcast
Broadcast messages
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name          |  Parameter type   | Parameter Description                 | Is it not empty |
| ------------ |:-------:| -------------------- |:----:|
| chainId      |   int   | Connected ChainId,Value range[1-65535] |  yes   |
| excludeNodes | string  | eliminatepeernodeId, separated by commas     |  yes   |
| messageBody  | string  | Message BodyHex               |  yes   |
| command      | string  | Message Protocol Instructions               |  yes   |
| isCross      | boolean | Is it cross chain                |  yes   |
| percent      |   int   | Broadcast transmission ratio,Not filled in,default100     |  yes   |

#### Return value
| Field Name   |  Field type   | Parameter Description               |
| ----- |:-------:| ------------------ |
| value | boolean | Returned when no node has been sent outfalse |

### nw\_createNodeGroup
Create a cross chain network for the main network or a chain factory to create a chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name               |  Parameter type   | Parameter Description                           | Is it not empty |
| ----------------- |:-------:| ------------------------------ |:----:|
| chainId           |   int   | Connected ChainId,Value range[1-65535]           |  yes   |
| magicNumber       |  long   | Network Magic Parameters                         |  yes   |
| maxOut            |   int   | As aclientMaximum number of active external connections              |  yes   |
| maxIn             |   int   | As aseverAllow maximum number of external connections               |  yes   |
| minAvailableCount |   int   | Minimum effective number of connections                        |  yes   |
| isCrossGroup      | boolean | Do you want to create a cross chain connection group:true Cross chain connection,false Normal connection |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### nw\_activeCross
Cross chain protocol module activation cross chain
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type  | Parameter Description                 | Is it not empty |
| ------- |:------:| -------------------- |:----:|
| chainId |  int   | Connected ChainId,Value range[1-65535] |  yes   |
| maxOut  | string | As aclientMaximum number of active external connections    |  yes   |
| maxIn   |  int   | As aseverAllow maximum number of external connections     |  yes   |
| seedIps | string | Seed Connection NodeID,Splicing with commas       |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### nw\_getGroupByChainId
Get node group information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                 | Is it not empty |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | Connected ChainId,Value range[1-65535] |  yes   |

#### Return value
| Field Name                  | Field type | Parameter Description        |
| -------------------- |:----:| ----------- |
| chainId              | int  | chainID         |
| magicNumber          | long | Network Magic Parameters      |
| totalCount           | int  | Total number of connections        |
| connectCount         | int  | Number of connected nodes in the local network  |
| disConnectCount      | int  | Number of local network waiting nodes   |
| inCount              | int  | Number of local network connection nodes |
| outCount             | int  | Number of local network outbound connection nodes |
| connectCrossCount    | int  | Number of cross chain network connection nodes   |
| disConnectCrossCount | int  | Number of waiting nodes in cross chain network   |
| inCrossCount         | int  | Number of cross chain network access nodes   |
| outCrossCount        | int  | Number of outbound nodes in cross chain networks   |
| isActive             | int  | Is the local network working   |
| isCrossActive        | int  | Is the cross chain network working   |
| isMoonNet            | int  | Is the network group a primary network link node |

### nw\_getChainConnectAmount
Get the specified number of network group connections
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     |  Parameter type   | Parameter Description                      | Is it not empty |
| ------- |:-------:| ------------------------- |:----:|
| chainId |   int   | Connected ChainId,Value range[1-65535]      |  yes   |
| isCross | boolean | trueObtain the number of cross chain connections,falseNumber of local network connections |  yes   |

#### Return value
| Field Name           |  Field type   | Parameter Description |
| ------------- |:-------:| ---- |
| connectAmount | integer | Number of connections available |

### nw\_delNodeGroup
Delete specified network group
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                 | Is it not empty |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | Connected ChainId,Value range[1-65535] |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

### nw\_getSeeds
View seed nodes provided by cross chain networks
#### scope:public
#### version:1.0

#### parameter list
No parameters

#### Return value
| Field Name      |  Field type  | Parameter Description                |
| -------- |:------:| ------------------- |
| seedsIps | string | Seed nodes that can be connected to the main networkIDSplicing with commas |

### nw\_getMainMagicNumber
View the magic parameters of the main network
#### scope:public
#### version:1.0

#### parameter list
No parameters

#### Return value
| Field Name   | Field type | Parameter Description   |
| ----- |:----:| ------ |
| value | long | Main network magic parameters |

### nw\_getGroups
Paging to obtain network group information,startPageRelated topageSize All for0When not paginated, returns all network group information
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name       | Parameter type | Parameter Description   | Is it not empty |
| --------- |:----:| ------ |:----:|
| startPage | int  | Start Page   |  yes   |
| pageSize  | int  | Display quantity per page |  yes   |

#### Return value
| Field Name                  | Field type | Parameter Description        |
| -------------------- |:----:| ----------- |
| chainId              | int  | chainID         |
| magicNumber          | long | Network Magic Parameters      |
| totalCount           | int  | Total number of connections        |
| connectCount         | int  | Number of connected nodes in the local network  |
| disConnectCount      | int  | Number of local network waiting nodes   |
| inCount              | int  | Number of local network connection nodes |
| outCount             | int  | Number of local network outbound connection nodes |
| connectCrossCount    | int  | Number of cross chain network connection nodes   |
| disConnectCrossCount | int  | Number of waiting nodes in cross chain network   |
| inCrossCount         | int  | Number of cross chain network access nodes   |
| outCrossCount        | int  | Number of outbound nodes in cross chain networks   |
| isActive             | int  | Is the local network working   |
| isCrossActive        | int  | Is the cross chain network working   |
| isMoonNet            | int  | Is the network group a primary network link node |

### nw\_reconnect
Local network restart
#### scope:public
#### version:1.0

#### parameter list
| Parameter Name     | Parameter type | Parameter Description                 | Is it not empty |
| ------- |:----:| -------------------- |:----:|
| chainId | int  | Networking ChainId,Value range[1-65535] |  yes   |

#### Return value
| Field Name | Field type | Parameter Description           |
| --- |:----:| -------------- |
| N/A | void | No specific return value, successful without errors |

