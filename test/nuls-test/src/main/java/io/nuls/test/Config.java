package io.nuls.test;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.ImportAccountByPrivateKeyReq;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.exception.NulsException;
import io.nuls.test.cases.Constants;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 14:31
 * @Description: 功能描述
 */
@Configuration(domain = "test")
public class Config implements InitializingBean {

    int httpPort;

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Value("testNodeExclude")
    String nodeExclude;

    @Value("testSeedAccountPriKey")
    String testSeedAccount;

    @Value("testNodeType")
    String nodeType;

    String testNodeList;

    int testNodeCount;

    int chainId;

    int assetsId;

    String seedAddress;

    String packetMagic;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(Constants.PASSWORD,testSeedAccount,true));
        this.seedAddress = result.getData();
    }

    public boolean isMaster(){
        return "master".equals(nodeType);
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public String getNodeExclude() {
        return nodeExclude;
    }

    public void setNodeExclude(String nodeExclude) {
        this.nodeExclude = nodeExclude;
    }

    public String getTestSeedAccount() {
        return testSeedAccount;
    }

    public void setTestSeedAccount(String testSeedAccount) {
        this.testSeedAccount = testSeedAccount;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getTestNodeList() {
        return testNodeList;
    }

    public void setTestNodeList(String testNodeList) {
        this.testNodeList = testNodeList;
    }

    public int getTestNodeCount() {
        return testNodeCount;
    }

    public void setTestNodeCount(int testNodeCount) {
        this.testNodeCount = testNodeCount;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(int assetsId) {
        this.assetsId = assetsId;
    }

    public String getSeedAddress() {
        return seedAddress;
    }

    public void setSeedAddress(String seedAddress) {
        this.seedAddress = seedAddress;
    }

    public String getPacketMagic() {
        return packetMagic;
    }

    public void setPacketMagic(String packetMagic) {
        this.packetMagic = packetMagic;
    }
}
