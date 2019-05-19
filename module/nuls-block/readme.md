# 模块运行环境

- jdk: 11
- ide: IntelliJ IDEA 2018.3.3 (Community Edition)
- maven: 3.3.9

# 常见日志分析

|日志内容															|日志生成原因|
|----|----|
|skip block syn because minNodeAmount is set to 0|				把minNodeAmount设置为0会打印此日志,直接跳过区块同步流程,如果想一个节点出块才需要改这个参数|
|no consistent nodes								|				连接到的节点高度不一致造成的|
|first start										|				连接到的节点高度都是0,表示这条链刚开始运行|
|local blocks is newest							|				本地节点的区块已经是最新的,不需要进行区块同步|
|The number of rolled back blocks exceeded the configured value|	本地区块与网络上的区块不一致,本地区块回滚,但是回滚数量超过阈值,停止回滚|
|The local GenesisBlock differ from network		|				本地节点的创世区块hash与网络上的创世区块hash不匹配,需要检查本地配置|
|available nodes not enough						|				连接到的可用节点数量不足,检查minNodeAmount这个配置项,以及网络模块配置、日志|
|block syn complete successfully current height	|				区块同步成功,且已经同步到最新区块|
|block syn complete but is not newest			|				区块同步成功,但还不是最新区块,会自动进行再次同步|
|error occur when saving downloaded blocks height-	|				区块同步失败,一般是保存同步到的区块时报错,重点检查区块模块、共识模块、交易模块的日志|

# 常用配置说明

## 创世区块

配置文件路径：[genesis-block.json](./src/main/resources/genesis-block.json)

## 系统参数

配置文件路径：[module.json](./src/main/resources/module.json)

### 配置项说明[^1]
|配置项															|说明|
|----|----|
|dataFolder|数据库文件夹名|
|language|错误码语言|
|forkChainsMonitorInterval|分叉链监视线程运行间隔|
|orphanChainsMonitorInterval|孤儿链监视线程运行间隔|
|orphanChainsMaintainerInterval|孤儿链维护线程运行间隔|
|storageSizeMonitorInterval|缓存数据库容量监视线程运行间隔|
|networkResetMonitorInterval|网络监视线程运行间隔|
|nodesMonitorInterval|节点数量监视线程运行间隔|
|txGroupRequestorInterval|TxGroup获取线程运行间隔|
|txGroupTaskDelay|分叉链监视线程运行间隔|
|testAutoRollbackAmount|启动后自动回滚的区块数量,仅供测试区块回滚用,生产环境下设置为0|
|chainName|默认链名称|
|chainId|默认链ID|
|assetId|默认资产ID|
|blockMaxSize|区块最大字节数|
|extendMaxSize|区块扩展字段最大字节数|
|resetTime|本地区块高度不更新时,引发重置网络动作的时间间隔|
|chainSwtichThreshold|引发分叉链切换的高度差阈值|
|cacheSize|分叉链、孤儿链区块最大缓存数量|
|heightRange|接收新区块的范围|
|waitInterval|批量下载区块时,如果收到CompleteMessage时,区块还没有保存完,每一个区块预留多长等待时间|
|maxRollback|本地区块与网络区块不一致时,本地最大回滚数|
|consistencyNodePercent|统计网络上的节点最新区块高度、hash一致的百分比阈值|
|minNodeAmount|最小链接节点数,当链接到的网络节点低于此参数时,会持续等待|
|downloadNumber|区块同步过程中,每次从网络上节点下载的区块数量|
|validBlockInterval|为阻止恶意节点提前出块,设置此参数,区块时间戳大于当前时间多少就丢弃该区块|
|blockCache|同步区块时最多缓存多少个区块|
|smallBlockCache|系统正常运行时最多缓存多少个从别的节点接收到的小区块|
|orphanChainMaxAge|孤儿链维护失败时,年龄加一,此参数是孤儿链能达到的最大年龄,高于这个值会被清理线程清理|
|logLevel|日志级别,按照不同的链进行区分|
|singleDownloadTimeout|从网络节点下载单个区块的超时时间|
|batchDownloadTimeout|从网络节点下载多个区块的超时时间|
|maxLoop|批量下载区块时,如果收到CompleteMessage时,区块还没有保存完,最多循环等待几个回合|
|synSleepInterval|两次区块同步之间的时间间隔|
|waitNetworkInterval|等待网络稳定的时间间隔|
|cleanParam|分叉链监视线程运行间隔|
    
## 协议信息

配置文件路径：[protocol-config.json](./src/main/resources/protocol-config.json)


[^1]:配置文件中所有时间参数单位都是毫秒