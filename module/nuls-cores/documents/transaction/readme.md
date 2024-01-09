# Transaction module
stayNULS2.0In the ecosystem, transactions will flow within or between chains, and nodes in each chain not only need to handle transactions within the chain, but may also handle transactions across chains. Therefore, each node needs to handle more and more complex transactions, so we need a separate module to handle various transactions uniformly. And fromNULS2.0From the perspective of architecture design, we need an independent module to handle transaction collection、validate、Provide secure transaction data for block assembly、Storage and other functions are shared for all transactions、Uniformity, therefore we operate transaction management as an independent module.

## Process local transactions

- Collect transactions
- Local verification
- Broadcast forwarding transactions to other nodes
- Extract packable transactions
- Submit、Rollback transaction
- Save unconfirmed、Packable and confirmed transactions
- Provide transaction data
