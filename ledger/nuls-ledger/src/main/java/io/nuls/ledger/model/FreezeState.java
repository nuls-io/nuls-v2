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
}
