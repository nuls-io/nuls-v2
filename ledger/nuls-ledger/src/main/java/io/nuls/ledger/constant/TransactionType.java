package io.nuls.ledger.constant;

/**
 * transaction type
 *
 * @seelink https://github.com/nuls-io/nuls_2.0_docs/blob/master/nulstar/%E4%BA%A4%E6%98%93%E7%B1%BB%E5%9E%8B%E5%AD%97%E5%85%B8.md
 * Created by wangkun23 on 2018/12/3.
 */
public interface TransactionType {

    /**
     * Coinbase,共识奖励交易
     */
    int TX_TYPE_COINBASE = 1;

    /**
     * 转账交易
     */
    int TX_TYPE_TRANSFER = 2;

    /**
     * 设置账户别名交易
     */
    int TX_TYPE_ACCOUNT_ALIAS = 3;


    /**
     * consensus module type
     */
    int TX_TYPE_REGISTER_AGENT = 4;
    int TX_TYPE_JOIN_CONSENSUS = 5;
    int TX_TYPE_CANCEL_DEPOSIT = 6;
    int TX_TYPE_YELLOW_PUNISH = 7;
    int TX_TYPE_RED_PUNISH = 8;
    int TX_TYPE_STOP_AGENT = 9;
    int TX_TYPE_CROSS_CHAIN_TRANSFER = 10;

    /**
     * 链管理
     * chain manager
     */
    int TX_TYPE_REGISTER_CHAIN_AND_ASSET = 11;
    int TX_TYPE_DESTROY_ASSET_AND_CHAIN = 12;
    int TX_TYPE_ADD_ASSET_TO_CHAIN = 13;
    int TX_TYPE_REMOVE_ASSET_FROM_CHAIN = 14;


    /**
     * 创建智能合约交易
     */
    int TX_TYPE_CREATE_CONTRACT = 100;

    /**
     * 调用智能合约交易
     */
    int TX_TYPE_CALL_CONTRACT = 101;

    /**
     * 删除智能合约交易
     */
    int TX_TYPE_DELETE_CONTRACT = 102;

    /**
     * 向合约地址转账交易
     */
    int TX_TYPE_CONTRACT_TRANSFER = 103;
}
