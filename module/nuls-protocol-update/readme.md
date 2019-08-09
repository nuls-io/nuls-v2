# 协议升级改造说明

## 协议升级配置文件

每个涉及到网络消息收发、交易处理的模块都需要在项目resources目录下新建一个protocol-config.json文件.protocol-config.json的加载、解析不需要各模块自己写代码,最后会融合到RpcModule中(未完成).

### 文件格式

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

### 主要字段说明

version:版本号

extend:继承哪个版本的配置

validTxs:该版本有效的交易配置

    type:交易类型
    systemTx:是否系统交易
    unlockTx:是否解锁交易
    verifySignature:是否验证签名
    verifyFee:是否验证手续费
    handler:交易处理类

validMsgs:该版本有效的网络消息配置

    name:消息类名
    protocolCmd:消息对应的网络处理接口(向网络模块注册消息时使用)
    handlers:消息处理类

invalidTxs:该版本无效的交易配置(填入要废弃的交易类型)

invalidMsgs:该版本无效的网络消息配置(填入要废弃的消息类名)

## 协议升级统计原理

区块头中有四个字段跟协议升级有关
```java
public class BlockExtendsData extends BaseNulsData {

    /**
     * 主网当前生效的版本
     */
    private short mainVersion;

    /**
     * 区块的版本,可以理解为本地钱包的版本
     */
    private short blockVersion;

    /**
     * 每个统计区间内的最小生效比例(60-100)
     */
    private byte effectiveRatio;

    /**
     * 协议生效要满足的连续区间数(50-1000)
     */
    private short continuousIntervalCount;
}
```
收到新区块时,区块模块会通知协议升级模块,协议升级模块会解析上面四个字段,每隔1000[^1]个区块统计一次该区间内的协议版本比例,新的协议版本比例大于effectiveRatio时,新协议的生效区间数+1,累计够continuousIntervalCount时,新协议生效；中途如果有一个区间新协议版本比例低于effectiveRatio,累计计数清零.

## 协议升级改造实例

增加MessageProcessor和TransactionProcessor两个接口,消息处理类继承MessageProcessor,交易处理类继承TransactionProcessor,并使用@Component注解标识出beanName

```java
public interface MessageProcessor {

    /**
     * 获取要处理的消息对应的cmd
     *
     * @return
     */
    String getCmd();

    /**
     * 消息处理方法
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
     * 获取该交易器绑定的交易类型,参见{@link TxType}
     *
     * @return
     */
    int getType();

    /**
     * 根据处理优先级进行排序
     */
    Comparator<TransactionProcessor> COMPARATOR = Comparator.comparingInt(TransactionProcessor::getPriority);

    /**
     * 验证接口
     *
     * @param chainId       链Id
     * @param txs           类型为{@link #getType()}的所有交易集合
     * @param txMap         不同交易类型与其对应交易列表键值对
     * @param blockHeader   区块头
     * @return 验证错误码和未通过验证的交易,需要丢弃
     */
    @ResponseData(description = "返回一个map，map中包含验证错误码和未通过验证的交易", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "errorCode", description = "错误码"),
            @Key(name = "txList", valueType = List.class, valueElement = Transaction.class, description = "返回类型为List<Transaction>")
    }))
    Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);

    /**
     * 提交接口
     *
     * @param chainId       链Id
     * @param txs           类型为{@link #getType()}的所有交易集合
     * @param blockHeader   区块头
     * @return 是否提交成功
     */
    boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 回滚接口
     *
     * @param chainId       链Id
     * @param txs           类型为{@link #getType()}的所有交易集合
     * @param blockHeader   区块头
     * @return 是否回滚成功
     */
    boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 获取处理优先级,数字越大,优先级越高
     *
     * @return
     */
    default int getPriority() {
        return 1;
    }
}
```

注意几个地方

- beanName与protocol-config.json中保持一致

## 协议升级拦截实现方式

新增TransactionDispatcher统一转发处理本模块的所有类型交易

```java
    /**
     * 模块统一交易验证器RPC接口
     */
    public static final String TX_VALIDATOR = "txValidator";

    /**
     * 模块统一交易提交RPC接口
     */
    public static final String TX_COMMIT = "txCommit";

    /**
     * 模块统一交易回滚RPC接口
     */
    public static final String TX_ROLLBACK = "txRollback";
```

新增MessageDispatcher统一转发处理本模块的所有类型消息

## 协议升级测试案例

- 测试不升级
- 测试连续升级(中途统计没有波动,没有跨版本升级)
- 测试连续升级(中途统计有波动,没有跨版本升级)
- 测试连续升级(中途统计没有波动,有跨版本升级)
- 测试连续升级(中途统计有波动,有跨版本升级)
- 测试连续升级后连续回滚降级(中途统计没有波动,没有跨版本升级)
- 测试连续升级后连续回滚降级(中途统计有波动,没有跨版本升级)
- 测试连续升级后连续回滚降级(中途统计没有波动,有跨版本升级)
- 测试连续升级后连续回滚降级(中途统计有波动,有跨版本升级)

[^1]:可配置,同一条链内保持一致

