package io.nuls.contract.validator;

import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.contract.util.ContractUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.List;

import static io.nuls.contract.constant.ContractConstant.TX_TYPE_CALL_CONTRACT;
import static io.nuls.contract.constant.ContractConstant.TX_TYPE_COINBASE;
import static io.nuls.contract.constant.ContractErrorCode.TX_DATA_VALIDATION_ERROR;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author tag
 */
@Component
public class ContractAcceptTransferredTxValidator{
    
    public Result validate(Transaction tx) throws NulsException {
        if(tx.getCoinData() == null){
            return getSuccess();
        }
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(), 0);
        List<CoinTo> toList = coinData.getTo();
        if(toList == null || toList.size() == 0){
            return getSuccess();
        }
        int type = tx.getType();
        for (CoinTo coinTo : toList) {
            if(ContractUtil.isLegalContractAddress(coinTo.getAddress())) {
                if(type != TX_TYPE_COINBASE && type != TX_TYPE_CALL_CONTRACT) {
                    Log.error("contract entity error: The contract does not accept transfers of this type[{}] of transaction.", type);
                    return Result.getFailed(TX_DATA_VALIDATION_ERROR);
                }
            }
        }
        return getSuccess();
    }
}
