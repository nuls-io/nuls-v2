package io.nuls.base.protocol;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.rpc.model.Key;
import io.nuls.core.rpc.model.ResponseData;
import io.nuls.core.rpc.model.TypeDescriptor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 交易处理器
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/23 17:43
 */
public interface TransactionProcessor {

    /**
     * 获取该交易器绑定的交易类型,参见{@link TxType}
     *
     * @return
     */
    int getType();

    /**
     * 根据处理优先级进行排序
     */
    Comparator<TransactionProcessor> COMPARATOR = Comparator.comparingInt(TransactionProcessor::getPriority);

    /**
     * 验证接口
     *
     * @param chainId       链Id
     * @param txs           类型为{@link #getType()}的所有交易集合
     * @param txMap         不同交易类型与其对应交易列表键值对
     * @param blockHeader   区块头
     * @return 验证错误码和未通过验证的交易,需要丢弃
     */
    @ResponseData(description = "返回一个map，map中包含验证错误码和未通过验证的交易", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "errorCode", description = "错误码"),
            @Key(name = "txList", valueType = List.class, valueElement = Transaction.class, description = "返回类型为List<Transaction>")
    }))
    Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);
    //List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);

    /**
     * 提交接口
     *
     * @param chainId       链Id
     * @param txs           类型为{@link #getType()}的所有交易集合
     * @param blockHeader   区块头
     * @return 是否提交成功
     */
    boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 回滚接口
     *
     * @param chainId       链Id
     * @param txs           类型为{@link #getType()}的所有交易集合
     * @param blockHeader   区块头
     * @return 是否回滚成功
     */
    boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 获取处理优先级,数字越大,优先级越高
     *
     * @return
     */
    default int getPriority() {
        return 1;
    }
}
