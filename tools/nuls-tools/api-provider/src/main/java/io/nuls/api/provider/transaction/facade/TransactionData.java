package io.nuls.api.provider.transaction.facade;

import io.nuls.base.constant.TxStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 16:39
 * @Description: 功能描述
 */
@Data
public class TransactionData {

    private int type;

    private String time;

    private String transactionSignature;

    private String remark;

    private String hash;

    private long blockHeight = -1L;

    private TxStatusEnum status = TxStatusEnum.UNCONFIRM;

    private int size;

    /**
     * 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序
     */
    private int inBlockIndex;

    private List<TransactionCoinData> form;

    private List<TransactionCoinData> to;

}
