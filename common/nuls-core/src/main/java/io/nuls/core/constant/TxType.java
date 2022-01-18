package io.nuls.core.constant;

/**
 * 交易类型
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/24 18:47
 */
public class TxType {

    /**
     * coinBase出块奖励
     */
    public static final int COIN_BASE = 1;
    /**
     * 转账
     * the type of the transfer transaction
     */
    public static final int TRANSFER = 2;

    /**
     * 设置账户别名
     * Set the transaction type of account alias.
     */
    public static final int ACCOUNT_ALIAS = 3;
    /**
     * 新建共识节点`
     */
    public static final int REGISTER_AGENT = 4;
    /**
     * 委托参与共识
     */
    public static final int DEPOSIT = 5;
    /**
     * 取消委托
     */
    public static final int CANCEL_DEPOSIT = 6;
    /**
     * 黄牌
     */
    public static final int YELLOW_PUNISH = 7;
    /**
     * 红牌
     */
    public static final int RED_PUNISH = 8;
    /**
     * 注销共识节点
     */
    public static final int STOP_AGENT = 9;
    /**
     * 跨链转账
     */
    public static final int CROSS_CHAIN = 10;

    /**
     * 注册链
     */
    public static final int REGISTER_CHAIN_AND_ASSET = 11;
    /**
     * 注销链
     */
    public static final int DESTROY_CHAIN_AND_ASSET = 12;
    /**
     * 为链新增一种资产
     */
    public static final int ADD_ASSET_TO_CHAIN = 13;
    /**
     * 删除链上资产
     */
    public static final int REMOVE_ASSET_FROM_CHAIN = 14;
    /**
     * 创建智能合约
     */
    public static final int CREATE_CONTRACT = 15;
    /**
     * 调用智能合约
     */
    public static final int CALL_CONTRACT = 16;
    /**
     * 删除智能合约
     */
    public static final int DELETE_CONTRACT = 17;
    /**
     * 合约内部转账
     * contract transfer tx
     */
    public static final int CONTRACT_TRANSFER = 18;
    /**
     * 合约执行手续费返还
     * contract return gas tx
     */
    public static final int CONTRACT_RETURN_GAS = 19;
    /**
     * 合约新建共识节点
     * contract create agent tx
     */
    public static final int CONTRACT_CREATE_AGENT = 20;

    /**
     * 合约委托参与共识
     * contract deposit tx
     */
    public static final int CONTRACT_DEPOSIT = 21;

    /**
     * 合约取消委托共识
     * contract withdraw tx
     */
    public static final int CONTRACT_CANCEL_DEPOSIT = 22;

    /**
     * 合约注销共识节点
     * contract stop agent tx
     */
    public static final int CONTRACT_STOP_AGENT = 23;

    /**
     * 验证人变更
     * Verifier Change
     */
    public static final int VERIFIER_CHANGE = 24;

    /**
     * 验证人初始化
     * Verifier init
     */
    public static final int VERIFIER_INIT = 25;

    /**
     * 合约token跨链转账
     * contract token cross transfer tx
     */
    public static final int CONTRACT_TOKEN_CROSS_TRANSFER = 26;
    /**
     * 账本链内资产注册登记
     */
    public static final int LEDGER_ASSET_REG_TRANSFER = 27;


    /**
     * 追加节点保证金
     * Additional agent margin
     */
    public static final int APPEND_AGENT_DEPOSIT = 28;

    /**
     * 撤销节点保证金
     * Cancel agent deposit
     */
    public static final int REDUCE_AGENT_DEPOSIT = 29;

    /**
     * 喂价交易
     */
    public static final int QUOTATION = 30;

    /**
     * 最终喂价交易
     */
    public static final int FINAL_QUOTATION = 31;

    /**
     * 批量退出staking交易
     */
    public static final int BATCH_WITHDRAW = 32;

    /**
     * 合并活期staking记录
     */
    public static final int BATCH_STAKING_MERGE = 33;

    /**
     * 创建交易对
     */
    public static final int COIN_TRADING = 228;

    /**
     * 挂单委托
     */
    public static final int TRADING_ORDER = 229;

    /**
     * 挂单撤销
     */
    public static final int TRADING_ORDER_CANCEL = 230;

    /**
     * 挂单成交
     */
    public static final int TRADING_DEAL = 231;

    /**
     * 修改交易对
     */
    public static final int EDIT_COIN_TRADING = 232;

    /**
     * 撤单交易确认
     */
    public static final int ORDER_CANCEL_CONFIRM = 233;

    /**
     * 确认 虚拟银行变更交易
     */
    public static final int CONFIRM_CHANGE_VIRTUAL_BANK = 40;

    /**
     * 虚拟银行变更交易
     */
    public static final int CHANGE_VIRTUAL_BANK = 41;

    /**
     * 链内充值交易
     */
    public static final int RECHARGE = 42;

    /**
     * 提现交易
     */
    public static final int WITHDRAWAL = 43;

    /**
     * 确认提现成功状态交易
     */
    public static final int CONFIRM_WITHDRAWAL = 44;

    /**
     * 发起提案交易
     */
    public static final int PROPOSAL = 45;

    /**
     * 对提案进行投票交易
     */
    public static final int VOTE_PROPOSAL = 46;

    /**
     * 异构链交易手续费补贴
     */
    public static final int DISTRIBUTION_FEE = 47;

    /**
     * 虚拟银行初始化异构链
     */
    public static final int INITIALIZE_HETEROGENEOUS = 48;
    /**
     * 异构链合约资产注册等待
     */
    public static final int HETEROGENEOUS_CONTRACT_ASSET_REG_PENDING = 49;
    /**
     * 异构链合约资产注册完成
     */
    public static final int HETEROGENEOUS_CONTRACT_ASSET_REG_COMPLETE = 50;
    /**
     * 确认提案执行交易
     */
    public static final int CONFIRM_PROPOSAL = 51;
    /**
     * 重置异构链(合约)虚拟银行
     */
    public static final int RESET_HETEROGENEOUS_VIRTUAL_BANK = 52;
    /**
     * 确认重置异构链(合约)虚拟银行
     */
    public static final int CONFIRM_HETEROGENEOUS_RESET_VIRTUAL_BANK = 53;
    /**
     * 异构链充值待确认交易
     */
    public static final int RECHARGE_UNCONFIRMED = 54;
    /**
     * 异构链提现已发布到异构链网络
     */
    public static final int WITHDRAWAL_HETEROGENEOUS_SEND = 55;
    /**
     * 追加提现手续费
     */
    public static final int WITHDRAWAL_ADDITIONAL_FEE = 56;
    /**
     * 异构链主资产注册
     */
    public static final int HETEROGENEOUS_MAIN_ASSET_REG = 57;
    /**
     * 已注册跨链的链信息变更
     */
    public static final int REGISTERED_CHAIN_CHANGE = 60;
    /**
     * 重置跨链模块存储的本链验证人列表
     * 本交易必须由种子节点发出，节点收到本交易后从共识模块获取到最新的共识节点出块地址列表，覆盖跨链模块本地存储的本链验证人列表。
     */
    public static final int RESET_LOCAL_VERIFIER_LIST = 61;

    public static final int RESET_CHAIN_INFO = 62;

    public static final int BLOCK_ACCOUNT = 63;

    public static final int UNBLOCK_ACCOUNT = 64;

}
