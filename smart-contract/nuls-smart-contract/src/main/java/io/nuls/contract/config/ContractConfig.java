/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
@Configuration(domain = "smart_contract")
public class ContractConfig {
    /**
     * 编码方式
     */
    private String encoding;

    /**
     * 语言
     */
    private String language;

    private String kernelUrl;

    /**
     * 模块日志配置信息
     */
    private String logFilePath;
    private String logFileName;
    private String logFileLevel;
    private String logConsoleLevel;

    private int mainChainId;

    private int mainAssetId;

    /**
     * ROCK DB 数据库文件存储路径
     */
    @Value("DataPath")
    private String dataPath;


    private ConfigBean chainConfig;

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getKernelUrl() {
        return kernelUrl;
    }

    public void setKernelUrl(String kernelUrl) {
        this.kernelUrl = kernelUrl;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public String getLogFileLevel() {
        return logFileLevel;
    }

    public void setLogFileLevel(String logFileLevel) {
        this.logFileLevel = logFileLevel;
    }

    public String getLogConsoleLevel() {
        return logConsoleLevel;
    }

    public void setLogConsoleLevel(String logConsoleLevel) {
        this.logConsoleLevel = logConsoleLevel;
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

    public ConfigBean getChainConfig() {
        return chainConfig;
    }

    public void setChainConfig(ConfigBean chainConfig) {
        this.chainConfig = chainConfig;
    }
}
