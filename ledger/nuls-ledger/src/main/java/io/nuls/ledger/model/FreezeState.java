package io.nuls.ledger.model;

import io.nuls.ledger.utils.ByteUtil;
import io.nuls.ledger.utils.RLP;
import io.nuls.ledger.utils.RLPElement;
import io.nuls.ledger.utils.RLPList;
import io.nuls.tools.data.LongUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wangkun23 on 2018/11/28.
 */
@ToString
@NoArgsConstructor
public class FreezeState {
    /**
     * 锁定金额,如加入共识的金额
     */
    @Setter
    @Getter
    private long amount = 0L;

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


    @Setter
    @Getter
    private byte[] rlpEncoded;


    public FreezeState(byte[] rawData) {
        this.rlpEncoded = rawData;
        try {
            RLPList decodedList = RLP.decode2(rlpEncoded);
            RLPList freezeState = (RLPList) decodedList.get(0);

            byte[] amountBytes = freezeState.get(0).getRLPData();
            RLPList freezeHeightStatesList = (RLPList) freezeState.get(1);
            RLPList freezeLockTimeStatesList = (RLPList) freezeState.get(2);

            this.amount = ByteUtil.byteArrayToLong(amountBytes);
            for (RLPElement rawState : freezeHeightStatesList) {
                FreezeHeightState heightState = new FreezeHeightState(rawState.getRLPData());

                this.freezeHeightStates.add(heightState);
            }

            for (RLPElement rawState : freezeLockTimeStatesList) {
                FreezeLockTimeState lockTimeState = new FreezeLockTimeState(rawState.getRLPData());
                this.freezeLockTimeStates.add(lockTimeState);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
    }

    /**
     * 查询用户所有可用金额
     *
     * @return
     */
    public long getTotal() {
        long freeze = 0L;
        for (FreezeHeightState heightState : freezeHeightStates) {
            freeze = LongUtils.add(freeze, heightState.getAmount());
        }

        for (FreezeLockTimeState lockTimeState : freezeLockTimeStates) {
            freeze = LongUtils.add(freeze, lockTimeState.getAmount());
        }
        long total = LongUtils.add(amount, freeze);
        return total;
    }

    private byte[] getFreezeHeightStatesEncoded() {
        byte[][] encoded = new byte[freezeHeightStates.size()][];
        int i = 0;
        for (FreezeHeightState state : freezeHeightStates) {
            encoded[i] = state.getEncoded();
            ++i;
        }
        return RLP.encodeList(encoded);
    }

    private byte[] getFreezeLockTimeStatesEncoded() {
        byte[][] encoded = new byte[freezeLockTimeStates.size()][];
        int i = 0;
        for (FreezeLockTimeState state : freezeLockTimeStates) {
            encoded[i] = state.getEncoded();
            ++i;
        }
        return RLP.encodeList(encoded);
    }

    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            byte[] amount = RLP.encodeBigInteger(BigInteger.valueOf(this.amount));
            byte[] freezeHeightStates = getFreezeHeightStatesEncoded();
            byte[] freezeLockTimeStates = getFreezeLockTimeStatesEncoded();
            this.rlpEncoded = RLP.encodeList(amount, freezeHeightStates, freezeLockTimeStates);
        }
        return rlpEncoded;
    }
}
