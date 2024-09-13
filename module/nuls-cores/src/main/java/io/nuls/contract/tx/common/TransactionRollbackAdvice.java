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
package io.nuls.contract.tx.common;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.CommonAdvice;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.enums.BlockType;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.storage.ContractOfflineTxHashListStorageService;
import io.nuls.contract.storage.ContractRewardLogByConsensusStorageService;
import io.nuls.contract.tx.v1.CallContractProcessor;
import io.nuls.contract.tx.v8.CallContractProcessorV8;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.contract.constant.ContractConstant.RPC_RESULT_KEY;
import static io.nuls.core.constant.CommonCodeConstanst.PARAMETER_ERROR;

/**
 * @author: PierreLuo
 * @date: 2019-12-01
 */
@Component
public class TransactionRollbackAdvice implements CommonAdvice {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractOfflineTxHashListStorageService contractOfflineTxHashListStorageService;
    @Autowired
    private CallContractProcessor callContractProcessor;
    @Autowired
    private CallContractProcessorV8 callContractProcessorV8;

    @Override
    public void begin(int chainId, List<Transaction> txList, BlockHeader header) {
        try {
            ChainManager.chainHandle(chainId, BlockType.VERIFY_BLOCK.type());
            Short currentVersion = ProtocolGroupManager.getCurrentVersion(chainId);
            Log.info("[Rollback] height: {}, blockHash: {}, begin", header != null ? header.getHeight() : 0, header != null ? header.getHash().toHex() : "empty");
            // Delete smart contract off chain transactionshash
            contractOfflineTxHashListStorageService.deleteOfflineTxHashList(chainId, header.getHash().getBytes());
            // add by pierre at 2019-12-01 handletype10Business rollback of transactions, Protocol upgrade required done
            if(currentVersion >= ContractContext.UPDATE_VERSION_V250) {
                List<Transaction> crossTxList = txList.stream().filter(tx -> tx.getType() == TxType.CROSS_CHAIN).collect(Collectors.toList());
                if(currentVersion >= ContractContext.UPDATE_VERSION_CONTRACT_ASSET ) {
                    callContractProcessorV8.rollback(chainId, crossTxList, header);
                } else {
                    callContractProcessor.rollback(chainId, crossTxList, header);
                }
            }
            // end code by pierre
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void end(int chainId, List<Transaction> txList, BlockHeader blockHeader) {
    }

    @Override
    public void coinbase(int chainId, Transaction tx, BlockHeader blockHeader) {
        try {
            if (ProtocolGroupManager.getCurrentVersion(chainId) < ContractContext.PROTOCOL_21) {
                return;
            }
            if(TxType.COIN_BASE != tx.getType()) {
                return;
            }
            Log.info("Rollback contractRewardLogByConsensus, height: {}, coinbase hash: {}", blockHeader.getHeight(), tx.getHash().toHex());
            CoinData coinData = tx.getCoinDataInstance();
            List<CoinTo> toList = coinData.getTo();
            int toListSize = toList.size();
            if (toListSize == 0) {
                return;
            }
            byte[] address;
            BigInteger value;
            List<CoinTo> assetRewardList = new ArrayList<>();
            for(CoinTo to : toList) {
                address = to.getAddress();
                value = to.getAmount();
                if (value.compareTo(BigInteger.ZERO) < 0) {
                    Log.error("address [{}] - error amount [{}]", AddressTool.getStringAddressByBytes(address), value.toString());
                    return;
                }
                if(AddressTool.validContractAddress(address, chainId)) {
                    assetRewardList.add(to);
                }
            }
            // rollback -> record reward from consensus after P21
            contractHelper.deleteContractRewardLogByConsensus(chainId, assetRewardList);
            Log.info("Rollback contractRewardLogByConsensus end, assetRewardList size: {}", assetRewardList.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
