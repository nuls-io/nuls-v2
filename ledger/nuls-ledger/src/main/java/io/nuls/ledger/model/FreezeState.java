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
}
