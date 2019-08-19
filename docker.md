# 通过docker hub镜像运行钱包

```
docker run \
    --name nuls-wallet \
    -d \
    -p 18001:18001 \
    -p 18002:18002 \
    -v data:/nuls/data \
    -v log:/nuls/Logs \
    nulsio/nuls-wallet:beta3
```
其中18001为主链数据交换端口，18002为跨链交易数据交换接口，data为数据存储目录，logs为日志存储目录。

# 本机构建镜像

```
cd nuls-v2/docker
docker build -t nuls:beta3 .
```
# 进入命令和查看模块启动状态


```
docker exec -it nuls-wallet cmd   #启动命令行
docker exec -it nuls-wallet check-status #检查模块启动状态
```

# 获取带区块链浏览器和网页轻钱包的镜像

```
docker run \
    --name nuls-wallet \
    -d \
    -p 18001:18001 \
    -p 18002:18002 \
    -p 18005:1999  \
    -p 18006:18004 \
    -v data:/nuls/data \
    -v log:/nuls/Logs \
    nulsio/nuls-wallet-pro:beta3
```
其中18005为区块浏览器的http端口，18006为网页轻钱包的http端口。

区块链浏览器网页访问地址: http://127.0.0.1:18005
网页轻钱包网页访问地址  : http://127.0.0.1:18006

