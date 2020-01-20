/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.config;

import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.rpc.model.ModuleE;

import java.lang.reflect.InvocationTargetException;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
@Component
@Configuration(domain = ModuleE.Constant.SMART_CONTRACT)
public class ContractConfig implements ModuleConfig {
    /**
     * 编码方式
     */
    private String encoding;

    private int mainChainId;

    private int mainAssetId;

    /**
     * ROCK DB 数据库文件存储路径
     */
    private String dataPath;

    private int chainId;

    private int assetId;

    private long maxViewGas;

    private String packageLogPackages;

    private String packageLogLevels;

    private String blackHolePublicKey;

    private String crossTokenSystemContract;

    public String getCrossTokenSystemContract() {
        return crossTokenSystemContract;
    }

    public void setCrossTokenSystemContract(String crossTokenSystemContract) {
        this.crossTokenSystemContract = crossTokenSystemContract;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public long getMaxViewGas() {
        return maxViewGas;
    }

    public void setMaxViewGas(long maxViewGas) {
        this.maxViewGas = maxViewGas;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getMainChainId() {
        return mainChainId;
    }

    public void setMainChainId(int mainChainId) {
        this.mainChainId = mainChainId;
    }

    public int getMainAssetId() {
        return mainAssetId;
    }

    public void setMainAssetId(int mainAssetId) {
        this.mainAssetId = mainAssetId;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getPackageLogPackages() {
        return packageLogPackages;
    }

    public void setPackageLogPackages(String packageLogPackages) {
        this.packageLogPackages = packageLogPackages;
    }

    public String getPackageLogLevels() {
        return packageLogLevels;
    }

    public void setPackageLogLevels(String packageLogLevels) {
        this.packageLogLevels = packageLogLevels;
    }

    public String getBlackHolePublicKey() {
        return blackHolePublicKey;
    }

    public void setBlackHolePublicKey(String blackHolePublicKey) {
        this.blackHolePublicKey = blackHolePublicKey;
    }

    public ConfigBean getChainConfig() {
        ConfigBean configBean = new ConfigBean();
        configBean.setAssetId(assetId);
        configBean.setChainId(chainId);
        configBean.setMaxViewGas(maxViewGas);
        return configBean;
    }

    @Override
    public VersionChangeInvoker getVersionChangeInvoker() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> aClass = Class.forName("io.nuls.contract.tx.SmartContractVersionChangeInvoker");
        return (VersionChangeInvoker) aClass.getDeclaredConstructor().newInstance();
    }
}
