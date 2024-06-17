# Protocol upgrade and renovation instructions

## Protocol upgrade configuration file

Each involving network message sending and receiving、The modules for transaction processing need to be included in the projectresourcesCreate a new one in the directoryprotocol-config.jsonfile.protocol-config.jsonLoading of、Parsing does not require each module to write their own code,Finally, it will merge intoRpcModulein(Incomplete).

### file format

```json
[
  {
    "version": "1",
    "extend": "",
    "validTxs": [
	  {
        "type": "1",
        "systemTx": false,
        "unlockTx": false,
        "verifySignature": true,
        "verifyFee": true,
        "handler": "TransferProcessorV1"
      },
      {
        "type": "2",
        "systemTx": false,
        "unlockTx": false,
        "verifySignature": true,
        "verifyFee": true,
        "handler": "TransferProcessorV1"
      }
	],
    "validMsgs": [
      {
        "name": "io.nuls.block.message.HashMessage",
        "protocolCmd": "getBlock",
        "handlers": "ForwardSmallBlockHandlerV1"
      },
      {
        "name": "io.nuls.block.message.HashMessage",
        "protocolCmd": "getBlock,forward",
        "handlers": "ForwardSmallBlockHandlerV1,GetBlockHandlerV1"
      }
    ],
    "invalidTxs": "2,3",
    "invalidMsgs": "io.nuls.block.message.HashListMessage,io.nuls.block.message.HashListMessage"
  }
]
```

### Main Field Description

version:Version number

extend:Inherit which version of the configuration

validTxs:The valid transaction configuration for this version

    type:Transaction type
    systemTx:Is it a system transaction
    unlockTx:Whether to unlock the transaction
    verifySignature:Whether to verify signature
    verifyFee:Whether to verify the handling fee
    handler:Transaction processing class

validMsgs:The valid network message configuration for this version

    name:Message class name
    protocolCmd:The network processing interface corresponding to the message(Use when registering messages with network modules)
    handlers:Message processing class

invalidTxs:This version has invalid transaction configurations(Fill in the transaction type to be scrapped)

invalidMsgs:This version has invalid network message configuration(Fill in the name of the message class to be discarded)

## Principles of Protocol Upgrade Statistics

There are four fields in the block header related to protocol upgrade
```java
public class BlockExtendsData extends BaseNulsData {

    /**
     * The current effective version of the main network
     */
    private short mainVersion;

    /**
     * Version of blocks,Can be understood as the version of a local wallet
     */
    private short blockVersion;

    /**
     * The minimum effective ratio within each statistical interval(60-100)
     */
    private byte effectiveRatio;

    /**
     * The number of consecutive intervals that the agreement must meet in order to take effect(50-1000)
     */
    private short continuousIntervalCount;
}
```
When receiving the new block,The block module will notify the protocol upgrade module,The protocol upgrade module will parse the above four fields,every other1000[^1]Count the proportion of protocol versions in each block within that interval once,The proportion of new protocol versions is greater thaneffectiveRatioTime,The number of effective intervals for the new agreement+1,Accumulated enoughcontinuousIntervalCountTime,New agreement takes effect；If there is a new protocol version ratio lower thaneffectiveRatio,Zero cumulative count.

## Example of protocol upgrade and transformation

increaseMessageProcessorandTransactionProcessorTwo interfaces,Message processing class inheritanceMessageProcessor,Transaction processing class inheritanceTransactionProcessor,And use it@ComponentAnnotations indicatebeanName

```java
public interface MessageProcessor {

    /**
     * Obtain the corresponding message to be processedcmd
     *
     * @return
     */
    String getCmd();

    /**
     * Message processing methods
     *
     * @param chainId
     * @param message
     */
    void process(int chainId, String nodeId, String message);

}
```

```java
public interface TransactionProcessor {

    /**
     * Obtain the transaction type bound to this trader,See also{@link TxType}
     *
     * @return
     */
    int getType();

    /**
     * Sort based on processing priority
     */
    Comparator<TransactionProcessor> COMPARATOR = Comparator.comparingInt(TransactionProcessor::getPriority);

    /**
     * Verify Interface
     *
     * @param chainId       chainId
     * @param txs           Type is{@link #getType()}All transaction sets for
     * @param txMap         Different transaction types and their corresponding transaction list key value pairs
     * @param blockHeader   Block head
     * @return Verification error codes and transactions that did not pass verification,Need to discard
     */
    @ResponseData(description = "Return amap,mapIt contains verification error codes and transactions that did not pass verification", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "errorCode", description = "Error code"),
            @Key(name = "txList", valueType = List.class, valueElement = Transaction.class, description = "The return type isList<Transaction>")
    }))
    Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);

    /**
     * Submit Interface
     *
     * @param chainId       chainId
     * @param txs           Type is{@link #getType()}All transaction sets for
     * @param blockHeader   Block head
     * @return Whether the submission was successful
     */
    boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Rollback interface
     *
     * @param chainId       chainId
     * @param txs           Type is{@link #getType()}All transaction sets for
     * @param blockHeader   Block head
     * @return Is the rollback successful
     */
    boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * Get processing priority,The larger the number, the more,The higher the priority, the higher the priority
     *
     * @return
     */
    default int getPriority() {
        return 1;
    }
}
```

Pay attention to a few places

- beanNameRelated toprotocol-config.jsonMaintain consistency in the middle

## Implementation method for protocol upgrade interception

New additionTransactionDispatcherUnified forwarding and processing of all types of transactions in this module

```java
    /**
     * Module Unified Transaction VerifierRPCinterface
     */
    public static final String TX_VALIDATOR = "txValidator";

    /**
     * Module Unified Transaction SubmissionRPCinterface
     */
    public static final String TX_COMMIT = "txCommit";

    /**
     * Module Unified Transaction RollbackRPCinterface
     */
    public static final String TX_ROLLBACK = "txRollback";
```

New additionMessageDispatcherUnified forwarding and processing of all types of messages in this module

## Protocol upgrade test cases

- Testing without upgrading
- Testing continuous upgrades(There is no fluctuation in the midway statistics,No cross version upgrade)
- Testing continuous upgrades(There are fluctuations in the midway statistics,No cross version upgrade)
- Testing continuous upgrades(There is no fluctuation in the midway statistics,There are cross version upgrades available)
- Testing continuous upgrades(There are fluctuations in the midway statistics,There are cross version upgrades available)
- Test continuous upgrade followed by continuous rollback and degradation(There is no fluctuation in the midway statistics,No cross version upgrade)
- Test continuous upgrade followed by continuous rollback and degradation(There are fluctuations in the midway statistics,No cross version upgrade)
- Test continuous upgrade followed by continuous rollback and degradation(There is no fluctuation in the midway statistics,There are cross version upgrades available)
- Test continuous upgrade followed by continuous rollback and degradation(There are fluctuations in the midway statistics,There are cross version upgrades available)

[^1]:Configurable,Maintain consistency within the same chain

