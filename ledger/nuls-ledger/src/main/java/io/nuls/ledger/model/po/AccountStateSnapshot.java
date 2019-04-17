/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */
public class AccountStateSnapshot extends BaseNulsData {
    private AccountState accountState;
    private List<String> nonces = new ArrayList<>();
    private List<String> txHashList = new ArrayList<>();

    public AccountStateSnapshot() {
        super();
    }

    public AccountStateSnapshot(AccountState accountState, List<String> nonces,Set<String> txHashs) {
        this.accountState = accountState;
        this.nonces = nonces;
        this.txHashList.addAll(txHashs);
    }

    public AccountState getAccountState() {
        return accountState;
    }

    public void setAccountState(AccountState accountState) {
        this.accountState = accountState;
    }

    public List<String> getNonces() {
        return nonces;
    }

    public void setNonces(List<String> nonces) {
        this.nonces = nonces;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(accountState);
        stream.writeUint16(nonces.size());
        for (String nonce : nonces) {
            stream.writeString(nonce);
        }
        stream.writeUint16(txHashList.size());
        for (String txHash : txHashList) {
            stream.writeString(txHash);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.accountState = byteBuffer.readNulsData(new AccountState());
        int nonceCount = byteBuffer.readUint16();
        for (int i = 0; i < nonceCount; i++) {
            this.nonces.add(byteBuffer.readString());
        }
        int hashCount = byteBuffer.readUint16();
        for (int i = 0; i < hashCount; i++) {
            this.txHashList.add(byteBuffer.readString());
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(accountState);
        size += SerializeUtils.sizeOfUint16();
        for (String nonce : nonces) {
            size += SerializeUtils.sizeOfString(nonce);
        }
        size += SerializeUtils.sizeOfUint16();
        for (String txHash : txHashList) {
            size += SerializeUtils.sizeOfString(txHash);
        }
        return size;
    }

    public List<String> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<String> txHashList) {
        this.txHashList = txHashList;
    }
}
