/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.constant;


import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
public interface ContractConstant {

    String INITIAL_STATE_ROOT = "56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421";

    short MODULE_ID_CONTRACT = 10;
    /**
     * CONTRACT STATUS
     */
    int NOT_FOUND = 0;

    int NORMAL = 1;

    int STOP = 2;


    long CONTRACT_TRANSFER_GAS_COST = 1000;

    String BALANCE_TRIGGER_METHOD_NAME = "_payable";
    String BALANCE_TRIGGER_METHOD_DESC = "() return void";
    String BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC = "(String[][] args) return void";
    String BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC_IN_VM = "([[Ljava/lang/String;)V";

    String CONTRACT_CONSTRUCTOR = "<init>";


    String CALL_REMARK = "call";
    String PREVIEW_CALL_REMARK = "previewCall";
    String CREATE_REMARK = "create";
    String DELETE_REMARK = "delete";

    String NOT_ENOUGH_GAS = "not enough gas";

    long AGENT_CONTRACT_CONSTANT_GASLIMIT = 500000;
    long DEPOSIT_CONTRACT_CONSTANT_GASLIMIT = 100000;
    long CONTRACT_CONSTANT_GASLIMIT = 10000000;
    long CONTRACT_CONSTANT_PRICE = 1;

    long MAX_GASLIMIT = 10000000;
    long MAX_GAS_COST_IN_BLOCK = 13000000L;
    int MAX_CONTRACT_TX_IN_BLOCK = 600;

    long CONTRACT_MINIMUM_PRICE = 25;



    /**
     * 合约资产跨链
     */
    long CROSS_CHAIN_GASLIMIT = 300000;
    //TODO pierre 系统合约地址
    byte[] CROSS_CHAIN_SYSTEM_CONTRACT = null;
    String CROSS_CHAIN_SYSTEM_CONTRACT_TRANSFER_IN_METHOD_NAME = "crossChainTokenTransfer";
    String CMD_TOKEN_OUT_CROSS_CHAIN = "cc_tokenOutCrossChain";
    int TOKEN_ASSET_TYPE = 2;
    String CROSS_CHAIN_NRC20_CONTRACT_TRANSFER_OUT_METHOD_NAME = "transferCrossChain";
    String CROSS_CHAIN_NRC20_CONTRACT_TRANSFER_OUT_METHOD_DESC = "(String to, BigInteger value) return boolean";

    /**
     *
     */
    String CONTRACT_EVENT = "event";
    String CONTRACT_EVENT_ADDRESS = "contractAddress";
    String CONTRACT_EVENT_DATA = "payload";

    /**
     * NRC20
     */
    String NRC20_METHOD_NAME = "name";
    String NRC20_METHOD_SYMBOL = "symbol";
    String NRC20_METHOD_DECIMALS = "decimals";
    String NRC20_METHOD_TOTAL_SUPPLY = "totalSupply";
    String NRC20_METHOD_BALANCE_OF = "balanceOf";
    String NRC20_METHOD_TRANSFER = "transfer";
    String NRC20_METHOD_TRANSFER_FROM = "transferFrom";
    String NRC20_METHOD_APPROVE = "approve";
    String NRC20_METHOD_ALLOWANCE = "allowance";
    String NRC20_EVENT_TRANSFER = "TransferEvent";
    String NRC20_EVENT_APPROVAL = "ApprovalEvent";
    String NRC20_STANDARD_FILE = "nrc20.json";

    /**
     * NRC721
     */
    String NRC721_SAFETRANSFERFROM = "safeTransferFrom";
    String NRC721_SAFETRANSFERFROM_DATA = "safeTransferFrom-data";
    String NRC721_STANDARD_FILE = "nrc721.json";

    int DEFAULT_MAX_VIEW_GAS = 100000000;
    String SYS_FILE_ENCODING = "file.encoding";
    String MODULE_CONFIG_FILE = "module.json";

    /**
     * 最小转账金额 - 0.01 NULS
     * Minimum transfer amount - 0.01 NULS
     */
    BigInteger MININUM_TRANSFER_AMOUNT = BigInteger.TEN.pow(6);

    byte UNLOCKED_TX = (byte) 0;

    String LOG_FILE_FOLDER = "";
    String LOG_FILE_NAME = "smart-contract";

    String STRING = "String";

    String RPC_RESULT_KEY = "value";
    String RPC_COLLECTION_RESULT_KEY = "list";
}
