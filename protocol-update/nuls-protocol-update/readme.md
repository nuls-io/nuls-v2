# 协议升级改造说明

## 协议升级配置文件

每个涉及到网络消息收发、交易处理的模块都需要在项目resources目录下新建一个protocol-config.json文件.protocol-config.json的加载、解析不需要各模块自己写代码,最后会融合到RpcModule中(未完成).

### 文件格式

```json
[
  {
    "version": "1",
    "extend": "",
    "validTransactions": [
      {
        "type": "1",
        "name": "coinbase",
        "handler": "io.nuls.test.TestTransactionHandler1",
        "validate": "validate1",
        "commit": "commit1",
        "rollback": "rollback1"
      },
      {
        "type": "2",
        "name": "transfer",
        "handler": "io.nuls.test.TestTransactionHandler2",
        "validate": "validate1",
        "commit": "commit1",
        "rollback": "rollback1"
      }
    ],
    "validMessages": [
      {
        "name": "io.nuls.block.message.BlockMessage",
        "handlers": [
          {
            "name": "io.nuls.block.message.handler.BlockHandler#processV1"
          }
        ]
      },
      {
        "name": "io.nuls.block.test.TestMessage2",
        "handlers": [
          {
            "name": "io.nuls.block.test.TestMessageHandler2#processV1"
          },
          {
            "name": "io.nuls.block.test.TestMessageHandler2#processV2"
          }
        ]
      }
    ],
    "invalidTransactions": [],
    "invalidMessages": []
  },
  {
    "version": "2",
    "extend": "1",
    "validTransactions": [],
    "validMessages": [],
    "invalidTransactions": [
      {
        "name": "transfer"
      }
    ],
    "invalidMessages": [
      {
        "name": "io.nuls.block.test.TestMessage3"
      }
    ]
  },
  {
    "version": "3",
    "extend": "2",
    "validTransactions": [],
    "validMessages": [
      {
        "name": "io.nuls.block.test.TestMessage3",
        "handlers": [
          {
            "name": "io.nuls.block.test.TestMessageHandler3#processV5"
          },
          {
            "name": "io.nuls.block.test.TestMessageHandler3#processV6"
          }
        ]
      }
    ],
    "invalidTransactions": [],
    "invalidMessages": []
  }
]
```

### 主要字段说明

version:版本号

extend:继承哪个版本的配置

validTransactions:该版本有效的交易配置

validMessages:该版本有效的网络消息配置

invalidTransactions:该版本无效的交易配置

invalidMessages:该版本无效的网络消息配置

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

- 关心当前协议版本的模块采用什么方式来获取协议版本号?
- 多链的协议版本配置加载

[^1]:可配置,同一条链内保持一致

