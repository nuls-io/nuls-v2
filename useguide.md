# NULS2.0钱包使用指南
## nuls.ncf
钱包核心配置文件，可在此文件中完成所有模块的配置。
### 配置文件结构
配置文件格式采用类window系统配置文件结构，分为组和参数。

```
[network]   #组
port=18001  #参数key和值
```
nuls.ncf里面的内容看上去大概是这个样子

```
[global]
[global]
encoding=UTF-8
language=en
。。。
[account]
keystoreFolder=/keystore/backup

[network]
port=10081
...
```
[global]是一个特殊的组，所有的模块都会继承这个组里的配置项。全局通用配置可以配置在这个组下面。
[account]、[network]这两个是账户模块和网络模块的专有配置。名称对应模块Module.ncf里面的APP_NAME配置项。
当[global]和模块组下面有相同配置项时，模块组下面的配置优先级更高，覆盖global里的配置。
### 与模块内配置文件的关系
在模块内部（比如Modules/Nuls/account/1.0.0)目录下，有一个Module.ncf的配置文件，外部nuls.ncf的优先级高于模块内部的Module.ncf，当出现同名配置项时，nuls.ncf里的配置项将覆盖模块内部的同名配置项。

### 生成配置文件
当首次下载或打包好钱包程序时，是不存在nuls.ncf这个配置文件的，首次执行start或start-dev时，会自动生成nuls.ncf。
### [nuls.ncf配置说明列表](#nuls.ncf)
## start
钱包启动脚本，生产环境使用此脚本启动钱包

```
./start
```
## shop
钱包停止脚本，生产环境使用此脚本停止钱包

```
./stop
```
## start-dev
启动NULS2.0开发环境(兼容macOS系统)

```
./start-dev
```
## stop-dev
停止NULS2.0开发环境

```
./stop-dev
```
## cmd
启动命令行，进行钱包操作。

```
./cmd
```
指定日志级别，默认日志级别是ERROR,可选日志级别:DEBUG、INFO、WARN、ERROR

```
./cmd -l DEBUG #设置日志级别为DEBUG
```
指定配置文件路径，默认配置文件为同目录nuls.ncf

```
./cmd -c /home/my.ncf 
```
## check-status
检查模块的启动状态。通过此功能可以快速检查各个基础模块是否启动成功。脚本原理是读取日志文件里面的日志标志位。

```
./check-status
```
执行结果

```
==================MODULE PROCESS====================
account PROCESS IS START
block PROCESS IS START
consensus PROCESS IS START
ledger PROCESS IS START
network PROCESS IS START
transaction PROCESS IS START
==================RPC REDAY MODULE==================
account RPC READY
block RPC READY
consensus RPC READY
ledger RPC READY
network RPC READY
transaction RPC READY
======================REDAY MODULE==================
account STATE IS READY
block STATE IS READY
consensus STATE IS READY
ledger STATE IS READY
network STATE IS READY
transaction STATE IS READY
================TRY RUNNING MODULE==================
account TRY RUNNING
block TRY RUNNING
consensus TRY RUNNING
ledger TRY RUNNING
network TRY RUNNING
transaction TRY RUNNING
===================RUNNING MODULE===================
account STATE IS RUNNING
block STATE IS RUNNING
consensus STATE IS RUNNING
ledger STATE IS RUNNING
network STATE IS RUNNING
transaction STATE IS RUNNING
==================NULS WALLET STATE=================
==========================
NULS WALLET IS RUNNING
==========================
```
当看到NULS WALLET IS RUNNING时表示启动成功。
## create-address
生成账户地址和私钥。不依赖钱包，可独立运行生成地址。
```
./create-address
chainId:2
number:1
====================================================================================================
address   :tNULSeBaMi3UWVb1hMrsoEmv4XPPLW7CKmBVgn
privateKey:e27e3961384bc4749cb5bd535b16c90c4430d4da2cd34e1edd10b50b0d01fa1d
====================================================================================================
```
指定生成地址的chainId

```
./create-address -c 1   #指定chainId为1 ,（默认从nuls.ncf从读取chainId）
```
生成指定数量的地址

```
./create-address -n 100 #批量生成100个地址,(默认1）
```
## 附录
### <span id="nuls.ncf">nuls.ncf 配置文件</span>
#### 全局配置:group
| 配置项 | 取值范围 | 说明 |
| --- | --- | --- |
| encoding | 字符集 | 默认UTF-8，不建议修改 |
| language | en/zh_CHS | 语言包。 |
| logPath | 文件夹相对路径 | 日志文件存储路径，配置文件上下文相对路径 |
| logLevel | DEBUG,INFO,WARN,ERROR | 日志级别 |
| dataPath | 文件夹相对路径 | 数据文件存储路径，配置文件上下文相对路径 |
| chainId | 正整数 | 默认运行的链的链id |
| assetId | 正整数 | 默认运行链的主资产id |
| chainName | 字符串 | 默认运行链的链名称 |
| symbol | 字符串 | 默认运行链的主资产符号 |
| decimals | 正整数 | 默认资产的小数点右侧的位数 |
| blackHolePublicKey | 字符串 | 黑洞地址公钥 |

#### 网络模块配置：network

| 配置项 | 取值范围 | 说明 |
| --- | --- | --- |
| port | 正整数 | 网络通信端口 |
| crossPort | 正整数 | 跨链交易通信端口 |
| packetMagic | 正整数 | 网络魔法参数，魔法参数相同才会组网 |
| selfSeedIps | 字符串 | 默认连接的网络节点ip，多个用英文逗号隔开 |
| maxInCount | 正整数 | 允许外部节点与当前节点建立连接的总数量 |
| maxOutCount | 正整数 | 允许当前节点与外部节点建立连接的总数量 |

#### 账户模块配置:account

| 配置项 | 取值范围 | 说明 |
| --- | --- | --- |
| keystoreFolder | 文件夹路径 | 存储账户keystore文件的路径，此路径为在dataPath路径里的路径值 |

#### 区块模块配置:block

| 配置项 | 取值范围 | 说明 |
| --- | --- | --- |
| blockMaxSize | 正整数 | 一个区块可存储的最大字节数 |
| extendMaxSize | 正整数 | 区块扩展字段可存储的最大字节数 |
| chainSwtichThreshold | 正整数 | 引发分叉链切换的高度差阈值 |
| maxRollback | 正整数  | 本地区块与网络区块不一致时,本地最大回滚数 |
| consistencyNodePercent | 正整数 | 统计网络上的节点最新区块高度、hash一致的百分比阈值 |
| minNodeAmount | 正整数 | 最小链接节点数,当链接到的网络节点低于此参数时,会持续等待 |
| downloadNumber | 正整数 | 区块同步过程中,每次从网络上节点下载的区块数量 |
| singleDownloadTimeout | 正整数 | 从网络节点下载单个区块的超时时间 |
| batchDownloadTimeout | 正整数 | 从网络节点下载多个区块的超时时间 |
| cachedBlockSizeLimit | 正整数 | 区块同步过程中缓存的区块字节数上限 |
