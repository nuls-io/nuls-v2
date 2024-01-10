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

