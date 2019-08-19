FROM centos
ADD http://nuls-usa-west.oss-us-west-1.aliyuncs.com/beta3/NULS_Wallet_beta3-main-linux.tar.gz ./
#COPY NULS_Wallet_beta3-main-linux.tar.gz ./
RUN tar -xvf ./NULS_Wallet_beta3-main-linux.tar.gz \
    && mv NULS_Wallet_linux64_beta3 /nuls \
    && rm -f NULS_Wallet_beta3-main-linux.tar.gz \
    && echo "tail -f /dev/null" >> /nuls/start

WORKDIR /nuls

CMD ["./start"]

RUN echo "successfully build nuls image"



#===== 构建镜像
docker build -t nuls:beta3 .

#===== 启动容器
docker run --name nuls-beta3 -d \
  -p 18001:18001 \
  -p 18002:18002 \
  -p 18004:18004 \
  -v `pwd`/data:/nuls/data \
  -v `pwd`/logs:/nuls/Logs \
  nuls:beta3


#===== 查看模块启动状态
docker exec -it nuls-beta3 ./check-status

#===== 进入命令行
docker exec -it nuls-beta3 ./cmd

