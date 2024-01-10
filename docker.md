# adoptdocker hubMirror running wallet

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
among18001For the main chain data exchange port,18002For cross chain transaction data exchange interface,dataFor the data storage directory,logsStore directory for logs.

# Our institution creates a mirror image

```
cd nuls-v2/docker
docker build -t nuls:beta3 .
```
# Enter commands and view module startup status


```
docker exec -it nuls-wallet cmd   #Start Command Line
docker exec -it nuls-wallet check-status #Check the module startup status
```

# Get an image of a lightweight wallet with a blockchain browser and web pages

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
among18005For block browsershttpPort,18006For the lightweight wallet of web pageshttpPort.

Blockchain Browser Web Access Address: http://127.0.0.1:18005
Web Light Wallet Web Access Address  : http://127.0.0.1:18006

