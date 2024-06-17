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
  

