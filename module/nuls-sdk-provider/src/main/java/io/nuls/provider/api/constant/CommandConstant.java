/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.provider.api.constant;

/**
 * 存储对外提供的接口命令
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:15
 */
public interface CommandConstant {

    //根据区块高度获取区块
    String GET_BLOCK_BY_HEIGHT = "getBlockByHeight";
    //根据区块hash获取区块
    String GET_BLOCK_BY_HASH = "downloadBlockByHash";

    String INFO = "info";
    //获取账户余额
    String GET_BALANCE = "getBalanceNonce";
    //
    String IS_ALAIS_USABLE= "ac_isAliasUsable";
    //获取账户锁定列表
    String GET_FREEZE = "getFreezeList";

    //查询交易详情
    String GET_TX = "tx_getTxClient";
    //交易验证
    String TX_VALIEDATE = "tx_verifyTx";
    //新交易确认并广播
    String TX_NEWTX = "tx_newTx";
    //查询节点详情
    String GET_AGENT = "cs_getAgentInfo";
    //获取共识配置
    String GET_CONSENSUS_CONFIG = "cs_getConsensusConfig";
    //查询智能合约详情
    String CONTRACT_INFO = "sc_contract_info";
    //查询智能合约执行结果
    String CONTRACT_RESULT = "sc_contract_result";
    //查询智能合约构造函数
    String CONSTRUCTOR = "sc_constructor";
    //验证创建合约
    String VALIDATE_CREATE = "sc_validate_create";
    //验证调用合约
    String VALIDATE_CALL = "sc_validate_call";
    //验证删除合约
    String VALIDATE_DELETE = "sc_validate_delete";
    //预估创建合约的gas
    String IMPUTED_CREATE_GAS = "sc_imputed_create_gas";
    //预估调用合约的gas
    String IMPUTED_CALL_GAS = "sc_imputed_call_gas";
    //上传合约代码jar包
    String UPLOAD = "sc_upload";
    //获取智能合约结果集合
    String CONTRACT_RESULT_LIST = "sc_contract_result_list";
    //调用合约不上链方法
    String INVOKE_VIEW = "sc_invoke_view";
    //查询NRC20-TOKEN余额
    String TOKEN_BALANCE = "sc_token_balance";
    //查询已注册的跨链信息
    String GET_REGISTERED_CHAIN = "getRegisteredChainInfoList";
}
