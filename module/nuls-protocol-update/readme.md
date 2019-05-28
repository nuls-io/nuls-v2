# 协议升级改造说明

## 协议升级配置文件

每个涉及到网络消息收发、交易处理的模块都需要在项目resources目录下新建一个protocol-config.json文件.protocol-config.json的加载、解析不需要各模块自己写代码,最后会融合到RpcModule中(未完成).

### 文件格式

```json
[
  {
    "version": "1",
    "extend": "",
	"moduleValidator": "xxx",
    "moduleCommit": "xxx",
    "moduleRollback": "xxx",
    "validTxs": [
	  {
        "type": "2",
        "systemTx": false,
        "unlockTx": false,
        "verifySignature": true,
        "handler": "io.nuls.account.rpc.cmd.AccountTransactionHandler",
        "validate": "transferTxValidate",
        "commit": "",
        "rollback": ""
      },
      {
        "type": "3",
        "systemTx": false,
        "unlockTx": false,
        "verifySignature": true,
        "handler": "io.nuls.account.rpc.cmd.AccountTransactionHandler",
        "validate": "aliasTxValidate",
        "commit": "aliasTxCommit",
        "rollback": "aliasTxRollback"
      }
	],
    "validMsgs": [
      {
        "name": "io.nuls.block.message.HashListMessage",
        "protocolCmd": "getBlock,forward,getsBlock",
        "handlers": "io.nuls.block.message.handler.GetTxGroupHandler#process"
      },
      {
        "name": "io.nuls.block.message.HashMessage",
        "protocolCmd": "getBlock,forward,getsBlock",
        "handlers": "io.nuls.block.message.handler.ForwardSmallBlockHandler#process,io.nuls.block.message.handler.GetBlockHandler#process,io.nuls.block.message.handler.GetSmallBlockHandler#process"
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

moduleValidator:模块交易统一验证接口(交易注册使用)

moduleCommit:模块交易统一提交接口(交易注册使用)

moduleRollback:模块交易统一回滚接口(交易注册使用)

validTxs:该版本有效的交易配置

    type:交易类型
    systemTx:是否系统交易
    unlockTx:是否解锁交易
    verifySignature:是否验证签名
    handler:交易处理类
    validate:交易验证方法名
    commit:交易提交方法名
    rollback:交易回滚方法名

validMsgs:该版本有效的网络消息配置

    name:消息类名
    protocolCmd:消息对应的网络处理接口(向网络模块注册消息时使用)
    handlers:消息对应的处理方法(类名#方法名)

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

增加MessageHandler和TransactionProcessor两个注解,MessageHandler用于网络消息处理方法上,TransactionProcessor用于交易的验证、提交、回滚方法上,使用方式如下

```java
@Service
public class ForwardSmallBlockHandler extends BaseCmd {

    @CmdAnnotation(cmd = FORWARD_SMALL_BLOCK_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    @MessageHandler(message = HashMessage.class)
    public Response process(Map map) {
        return success();
    }
}
```

```java
@Service
public class TransactionHandler extends BaseCmd {

    /**
     * 转账交易验证
     */
    @CmdAnnotation(cmd = "ac_transferTxValidate", version = 1.0, description = "create transfer transaction validate 1.0")
    @ResisterTx(txType = TxProperty.TRANSFER, methodType = TxMethodType.VALID, methodName = "ac_transferTxValidate")
    @Parameter(parameterName = RpcParameterNameConstant.CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = RpcParameterNameConstant.TX, parameterType = "String")
    @TransactionProcessor(txType = TxType.TRANSFER, methodType = TxMethodType.VALID)
    public Response transferTxValidate(Map<String, Object> params) {
        return success(resultMap);
    }
}
```

注意几个地方

- 要使用@Service注解,否则拦截失效
- 方法名与protocol-config.json中保持一致
- 一个交易的验证、提交、回滚方法写在一个类里

## 协议升级拦截实现方式

新增TransactionProcessorInterceptor拦截@TransactionProcessor注解

```java
@Interceptor(MessageHandler.class)
public class MessageHandlerInterceptor implements BeanMethodInterceptor<MessageHandler> {

    @Override
    public Object intercept(MessageHandler annotation, Object object, Method method, Object[] params, BeanMethodInterceptorChain interceptorChain) throws Throwable {
        Map map = (Map) params[0];
        int chainId = (Integer) map.get("chainId");
        ProtocolGroup context = ProtocolGroupManager.getProtocol(chainId);
        short version = context.getVersion();
        Protocol protocol = context.getProtocolsMap().get(version);
        boolean validate = ProtocolValidator.meaasgeValidate(annotation.message(), object.getClass().getSuperclass(), protocol, method.getName());
        if (!validate) {
            throw new RuntimeException("The message or message handler is not available in the current version!");
        }
        return interceptorChain.execute(annotation, object, method, params);
    }
}
```

新增MessageHandlerInterceptor拦截@MessageHandler注解

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

## 协议升级的一些问题

- 是否在系统启动时注册多个版本的交易到交易模块?

[^1]:可配置,同一条链内保持一致

