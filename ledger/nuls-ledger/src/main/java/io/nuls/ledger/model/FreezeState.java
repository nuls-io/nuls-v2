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
package io.nuls.ledger.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wangkun23 on 2018/11/28.
 */
@ToString
@NoArgsConstructor
public class FreezeState extends BaseNulsData {
    /**
     * 锁定金额,如加入共识的金额
     */
    @Setter
    @Getter
    private BigInteger amount = BigInteger.ZERO;

    /**
     * 账户冻结的资产(高度冻结)
     */
    @Setter
    @Getter
    private List<FreezeHeightState> freezeHeightStates = new CopyOnWriteArrayList<>();

    /**
     * 账户冻结的资产(时间冻结)
     */
    @Setter
    @Getter
    private List<FreezeLockTimeState> freezeLockTimeStates = new CopyOnWriteArrayList<>();


    /**
     * 查询用户所有可用金额
     *
     * @return
     */
    public BigInteger getTotal() {
        BigInteger freeze = BigInteger.ZERO;
        for (FreezeHeightState heightState : freezeHeightStates) {
            freeze.add(heightState.getAmount());
        }

        for (FreezeLockTimeState lockTimeState : freezeLockTimeStates) {
            freeze.add(lockTimeState.getAmount());
        }
        return amount.add(freeze);
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(freezeHeightStates.size());
        stream.writeUint16(freezeLockTimeStates.size());
        for (FreezeHeightState heightState : freezeHeightStates) {
            stream.writeNulsData(heightState);
        }
        for (FreezeLockTimeState lockTimeState : freezeLockTimeStates) {
            stream.writeNulsData(lockTimeState);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int freezeHeightCount = byteBuffer.readUint16();
        int freezeLockTimeCount = byteBuffer.readUint16();
        this.freezeHeightStates = new ArrayList<>(freezeHeightCount);
        this.freezeLockTimeStates = new ArrayList<>(freezeLockTimeCount);
        for (int i = 0; i < freezeHeightCount; i++) {
            try {
                FreezeHeightState heightState = new FreezeHeightState();
                byteBuffer.readNulsData(heightState);
                this.freezeHeightStates.add(heightState);
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
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
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint16();
        for (FreezeHeightState heightState : freezeHeightStates) {
            size += SerializeUtils.sizeOfNulsData(heightState);
        }
        for (FreezeLockTimeState lockTimeState : freezeLockTimeStates) {
            size += SerializeUtils.sizeOfNulsData(lockTimeState);
        }
        return size;
    }
}
