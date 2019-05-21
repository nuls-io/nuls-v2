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

package io.nuls.transaction.token;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2019/4/16
 */
@JsonAutoDetect
public class AccountData {

    private String address;
    private String alias;
    private int type;
    private int txCount;
    private long totalOut;
    private long totalIn;
    private long consensusLock;
    private int timeLock;
    private long balance;
    private long totalBalance;
    private long totalReward;
    private List<String> tokens;
    private boolean news;

    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddress() {
        return address;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
    public String getAlias() {
        return alias;
    }

    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }
    public int getTxCount() {
        return txCount;
    }

    public void setTotalOut(long totalOut) {
        this.totalOut = totalOut;
    }
    public long getTotalOut() {
        return totalOut;
    }

    public void setTotalIn(long totalIn) {
        this.totalIn = totalIn;
    }
    public long getTotalIn() {
        return totalIn;
    }

    public void setConsensusLock(long consensusLock) {
        this.consensusLock = consensusLock;
    }
    public long getConsensusLock() {
        return consensusLock;
    }

    public void setTimeLock(int timeLock) {
        this.timeLock = timeLock;
    }
    public int getTimeLock() {
        return timeLock;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
    public long getBalance() {
        return balance;
    }

    public void setTotalBalance(long totalBalance) {
        this.totalBalance = totalBalance;
    }
    public long getTotalBalance() {
        return totalBalance;
    }

    public void setTotalReward(long totalReward) {
        this.totalReward = totalReward;
    }
    public long getTotalReward() {
        return totalReward;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }
    public List<String> getTokens() {
        return tokens;
    }

    public void setNews(boolean news) {
        this.news = news;
    }
    public boolean getNews() {
        return news;
    }

}
