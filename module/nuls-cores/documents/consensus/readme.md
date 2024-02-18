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

