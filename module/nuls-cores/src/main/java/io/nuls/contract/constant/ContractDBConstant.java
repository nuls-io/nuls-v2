/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.contract.constant;

/**
 * 交易数据存储常量
 * Transaction entity storage constants
 *
 * @author: PierreLuo
 */
public interface ContractDBConstant {

    /**
     * 配置信息表名
     * chain configuration table name
     */
    String DB_NAME_CONGIF = "contract_config";

    String DB_NAME_CONTRACT = "contract";
    String DB_NAME_CONTRACT_ADDRESS = "contract_address";
    String DB_NAME_CONTRACT_EXECUTE_RESULT = "contract_execute_result";

    String DB_NAME_CONTRACT_OFFLINE_TX_HASH_LIST = "contract_offline_tx_hash_list";

}
