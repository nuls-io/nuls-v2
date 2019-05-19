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
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cody on 2019/01/09.
 * 用于缓存区块中的账户信息
 *
 * @author lanjinsheng
 */
public class BlockSnapshotAccounts extends BaseNulsData {
    /**
     * accounts
     */
    private List<AccountStateSnapshot> accounts = new ArrayList<AccountStateSnapshot>();

    public void addAccountState(AccountStateSnapshot accountState) {
        accounts.add(accountState);
    }

    public BlockSnapshotAccounts() {
        super();
    }

    public List<AccountStateSnapshot> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountStateSnapshot> accounts) {
        this.accounts = accounts;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(accounts.size());
        for (AccountStateSnapshot accountStateSnapshot : accounts) {
            stream.writeNulsData(accountStateSnapshot);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int accountsCount = byteBuffer.readUint16();
        for (int i = 0; i < accountsCount; i++) {
            try {
                AccountStateSnapshot accountStateSnapshot = new AccountStateSnapshot();
                byteBuffer.readNulsData(accountStateSnapshot);
                this.accounts.add(accountStateSnapshot);
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        for (AccountStateSnapshot accountStateSnapshot : accounts) {
            size += accountStateSnapshot.size();
        }
        return size;
    }
}
