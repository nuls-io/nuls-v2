###运行测链前需要修改配置文件
运行前需要修改创世块配置文件的绝对路径，修改方法：打开nuls.ncf配置文件，找到block配置组下genesisBlockPath配置项，将配置改为同目录genesis-block.json文件的绝对路径。
比如钱包解压在/home/nuls/NULS-Wallet-linux64-alpha3目录，那么配置看上去应该像下面这样

```
genesisBlockPath=/home/nuls/NULS-Wallet-linux64-alpha3/genesis-block.json
```