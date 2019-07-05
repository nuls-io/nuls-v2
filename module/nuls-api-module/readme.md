# 模块运行环境

- jdk: 11
- ide: IntelliJ IDEA 2018.3.3 (Community Edition)
- maven: 3.3.9

#模块说明
nuls2.0对外提供的数据查询功能RCP接口。
默认该模块不启动。启动该模块时需要安装mongoDB数据库。模块启动后，会自动解析底层区块信息，
将区块数据转换为可查询业务数据存储到mongoDB数据库里。
api-module提供的相关接口，详见NULS2.0-API-RPC接口文档

#模块配置说明
#api-module模块对外的rpc端口号
rpcPort=18003
#mongoDB数据库url地址
databaseUrl=127.0.0.1
#mongoDB数据库端口号
databasePort=27017
#连接池最大数
maxAliveConnect=20
#连接最大等待时间
maxWaitTime=120000
#连接超时时间
connectTimeOut=30000