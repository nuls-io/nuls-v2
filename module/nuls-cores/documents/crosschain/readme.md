#### Why do we need to have《Cross chain》module

​	stayNULS2.0In the ecosystem, multiple parallel chains with different protocols are allowed to operate and interact simultaneously. Due to the different protocols between different parallel chains, their protocol interactions need to be handled byNULSThe main network is used for intermediary, and the cross chain module is used to convert the protocol of this chain intoNULSMain network protocol and what will be receivedNULSThe functional module that converts the main network protocol into the main chain protocol.

#### 《Cross chain》What to do

- Initiate cross chain transactions and convert them into main network protocol transactions
- Byzantine signatures within cross chain transactions
- Broadcast cross chain related transactions
- Cross chain transaction protocol conversion
- Byzantine verification of off chain cross chain transactions
- Off chain asset management
- Cross chain verifier maintenance
- Verifier Change Maintenance

#### 《Cross chain》Positioning in the system

​	stayNULS2.0In the ecosystem, cross chain modules are mainly responsible for initiating, verifying, protocol conversion, maintaining off chain assets, and maintaining verifier changes for cross chain transactions.

Dependent modules

- Transaction Management Module
- Network module
- Consensus module
- Chain management module（The main network requires dependencies, and parallel chains do not require dependencies）
- Ledger module

