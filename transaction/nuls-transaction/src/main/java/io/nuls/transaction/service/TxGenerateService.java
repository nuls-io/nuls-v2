/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.transaction.service;

import io.nuls.base.data.Transaction;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.dto.AccountSignDTO;
import io.nuls.transaction.model.dto.CoinDTO;

import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2019/3/15
 */
public interface TxGenerateService {

    /**
     * 创建不包含多签地址跨链交易，支持多普通地址
     * Create a cross-chain transaction
     *
     * @param chain    当前链的id Current chain
     * @param listFrom 交易的转出者数据 payer coins
     * @param listTo   交易的接收者数据 payee  coins
     * @param remark   交易备注 remark
     * @return String
     * @throws NulsException NulsException
     */
    String createCrossTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) throws NulsException;

    /**
     * 创建跨链多签签名地址交易
     * 所有froms中有且仅有一个地址，并且只能是多签地址，但可以包含多个资产(from)
     *
     * @param chain          当前链 chain
     * @param listFrom       交易的转出者数据 payer coins
     * @param listTo         交易的接收者数据 payee  coins
     * @param remark         交易备注 remark
     * @param accountSignDTO
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> createCrossMultiTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark, AccountSignDTO accountSignDTO) throws NulsException;

    /**
     * 创建跨链多签签名地址交易
     * 所有froms中有且仅有一个地址，并且只能是多签地址，但可以包含多个资产(from)
     *
     * @param chain    当前链的id Current chain
     * @param listFrom 交易的转出者数据 payer coins
     * @param listTo   交易的接收者数据 payee  coins
     * @param remark   交易备注 remark
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> createCrossMultiTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) throws NulsException;

    /**
     * 对多签交易进行签名的数据组装
     *
     * @param chain    链信息
     * @param tx       待签名的交易数据
     * @param address  执行签名的账户地址
     * @param password 账户密码
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> signMultiTransaction(Chain chain, String tx, String address, String password) throws NulsException;

    /**
     * 处理多签交易的签名 追加签名
     *
     * @param chain 链信息
     * @param tx    交易数据 tx
     * @param ecKey 签名者的eckey
     * @return Map<String, String>
     * @throws NulsException NulsException
     */
    Map<String, String> txMultiSignProcess(Chain chain, Transaction tx, ECKey ecKey) throws NulsException;


    /**
     * 处理多签交易的签名，第一次签名可以先组装多签账户的基础数据，到签名数据中
     *
     * @param chain                链信息
     * @param tx                   交易数据 tx
     * @param ecKey                签名者的 eckey数据
     * @param multiSignTxSignature 新的签名数据  sign entity
     * @return Map<String   ,       String>
     * @throws NulsException NulsException
     */
    Map<String, String> txMultiSignProcess(Chain chain, Transaction tx, ECKey ecKey, MultiSignTxSignature multiSignTxSignature) throws NulsException;
}
