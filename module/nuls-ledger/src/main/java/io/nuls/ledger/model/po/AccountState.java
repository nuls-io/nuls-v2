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
import io.nuls.core.model.ByteUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.constant.LedgerConstant;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 1.用户地址资产账号对应的账本信息
 * 2.该持久化对象是区块确认后的,最终信息：包含nonce值，余额，以及冻结信息。
 * 3.key值:address-assetChainId-assetId
 * @author lanjinsheng
 */

public class AccountState extends BaseNulsData {

    private byte[] nonce = LedgerConstant.getInitNonceByte();
    /**
     * 最近一次的账本冻结数据的处理时间,存储秒
     */
    private long latestUnFreezeTime = 0;
    /**
     * 账户总金额出账
     * 对应coindata里的coinfrom 累加值
     */
    private BigInteger totalFromAmount = BigInteger.ZERO;

    /**
     * 账户总金额入账
     * 对应coindata里的cointo 累加值
     */
    private BigInteger totalToAmount = BigInteger.ZERO;


    /**
     * 账户冻结的资产(高度冻结)
     */
    private List<FreezeHeightState> freezeHeightStates = new ArrayList<>();

    /**
     * 账户冻结的资产(时间冻结)
     */
    private List<FreezeLockTimeState> freezeLockTimeStates = new ArrayList<>();

    public AccountState() {
        super();
    }

    public AccountState(byte[] pNonce) {
        System.arraycopy(pNonce,0,this.nonce,0,LedgerConstant.NONCE_LENGHT);
    }

    /**
     * 获取账户可用金额（不含锁定金额）
     *
     * @return BigInteger
     */
    public BigInteger getAvailableAmount() {
        return totalToAmount.subtract(totalFromAmount);
    }

    public void addTotalFromAmount(BigInteger value) {
        totalFromAmount = totalFromAmount.add(value);
    }

    public void addTotalToAmount(BigInteger value) {
        totalToAmount = totalToAmount.add(value);
    }

    /**
     * 获取账户总金额（含锁定金额）
     *
     * @return BigInteger
     */
    public BigInteger getTotalAmount() {
        return totalToAmount.subtract(totalFromAmount).add(getFreezeTotal());
    }


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(nonce);
        stream.writeUint32(latestUnFreezeTime);
        stream.writeBigInteger(totalFromAmount);
        stream.writeBigInteger(totalToAmount);
        stream.writeUint32(freezeHeightStates.size());
        for (FreezeHeightState heightState : freezeHeightStates) {
            stream.writeNulsData(heightState);
        }
        stream.writeUint32(freezeLockTimeStates.size());
        for (FreezeLockTimeState lockTimeState : freezeLockTimeStates) {
            stream.writeNulsData(lockTimeState);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.nonce = byteBuffer.readBytes(8);
        this.latestUnFreezeTime = byteBuffer.readUint32();
        this.totalFromAmount = byteBuffer.readBigInteger();
        this.totalToAmount = byteBuffer.readBigInteger();
        int freezeHeightCount = (int) byteBuffer.readUint32();
        this.freezeHeightStates = new ArrayList<>(freezeHeightCount);
        for (int i = 0; i < freezeHeightCount; i++) {
            try {
                FreezeHeightState heightState = new FreezeHeightState();
                byteBuffer.readNulsData(heightState);
                this.freezeHeightStates.add(heightState);
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
        int freezeLockTimeCount = (int) byteBuffer.readUint32();
        this.freezeLockTimeStates = new ArrayList<>(freezeLockTimeCount);
        for (int i = 0; i < freezeLockTimeCount; i++) {
            try {
                FreezeLockTimeState lockTimeState = new FreezeLockTimeState();
                byteBuffer.readNulsData(lockTimeState);
                this.freezeLockTimeStates.add(lockTimeState);
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += nonce.length;
        size += SerializeUtils.sizeOfUint32();
        //totalFromAmount
        size += SerializeUtils.sizeOfBigInteger();
        //totalToAmount
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfUint32();
        for (FreezeHeightState heightState : freezeHeightStates) {
            size += SerializeUtils.sizeOfNulsData(heightState);
        }
        size += SerializeUtils.sizeOfUint32();
        for (FreezeLockTimeState lockTimeState : freezeLockTimeStates) {
            size += SerializeUtils.sizeOfNulsData(lockTimeState);
        }
        return size;
    }

    /**
     * 查询用户冻结金额
     *
     * @return
     */
    public BigInteger getFreezeTotal() {
        BigInteger freeze = BigInteger.ZERO;
        for (FreezeHeightState heightState : freezeHeightStates) {
            freeze = freeze.add(heightState.getAmount());
        }

        for (FreezeLockTimeState lockTimeState : freezeLockTimeStates) {
            freeze = freeze.add(lockTimeState.getAmount());
        }
        return freeze;
    }


    public AccountState deepClone() {
        AccountState orgAccountState = new AccountState();
        orgAccountState.setNonce(ByteUtils.copyOf(this.getNonce(), 8));
        orgAccountState.setLatestUnFreezeTime(this.getLatestUnFreezeTime());
        orgAccountState.setTotalFromAmount(this.getTotalFromAmount());
        orgAccountState.setTotalToAmount(this.getTotalToAmount());
        List<FreezeHeightState> heightStateArrayList = new ArrayList<>();
        heightStateArrayList.addAll(this.getFreezeHeightStates());
        orgAccountState.setFreezeHeightStates(heightStateArrayList);
        List<FreezeLockTimeState> lockTimeStateArrayList = new ArrayList<>();
        lockTimeStateArrayList.addAll(this.getFreezeLockTimeStates());
        orgAccountState.setFreezeLockTimeStates(lockTimeStateArrayList);
        return orgAccountState;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public long getLatestUnFreezeTime() {
        return latestUnFreezeTime;
    }

    public void setLatestUnFreezeTime(long latestUnFreezeTime) {
        this.latestUnFreezeTime = latestUnFreezeTime;
    }

    public BigInteger getTotalFromAmount() {
        return totalFromAmount;
    }

    public void setTotalFromAmount(BigInteger totalFromAmount) {
        this.totalFromAmount = totalFromAmount;
    }

    public BigInteger getTotalToAmount() {
        return totalToAmount;
    }

    public void setTotalToAmount(BigInteger totalToAmount) {
        this.totalToAmount = totalToAmount;
    }

    public List<FreezeHeightState> getFreezeHeightStates() {
        return freezeHeightStates;
    }

    public void setFreezeHeightStates(List<FreezeHeightState> freezeHeightStates) {
        this.freezeHeightStates = freezeHeightStates;
    }

    public List<FreezeLockTimeState> getFreezeLockTimeStates() {
        return freezeLockTimeStates;
    }

    public void setFreezeLockTimeStates(List<FreezeLockTimeState> freezeLockTimeStates) {
        this.freezeLockTimeStates = freezeLockTimeStates;
    }

    public boolean timeAllow() {
        long now = NulsDateUtils.getCurrentTimeSeconds();
        if ((now - latestUnFreezeTime) > LedgerConstant.TIME_RECALCULATE_FREEZE) {
            return true;
        }
        return false;
    }
}
