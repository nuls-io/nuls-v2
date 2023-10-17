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
package io.nuls.contract.callable;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsHash;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractNewTxHandler;
import io.nuls.contract.helper.ContractTransferHandler;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.BatchInfoV8;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.ContractTransferData;
import io.nuls.contract.service.ContractExecutor;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramInternalCreate;
import io.nuls.core.basic.Result;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static io.nuls.contract.config.ContractContext.ASSET_ID;
import static io.nuls.contract.config.ContractContext.CHAIN_ID;
import static io.nuls.contract.util.ContractUtil.extractPublicKey;
import static io.nuls.contract.util.ContractUtil.makeContractResult;
import static io.nuls.core.constant.TxType.*;

// add by pierre at 2022/6/2 p14
public class ContractTxCallableV14 {

    private ContractExecutor contractExecutor;
    private ContractHelper contractHelper;
    private ContractNewTxHandler contractNewTxHandler;
    private ContractTransferHandler contractTransferHandler;
    private ContractTempBalanceManager tempBalanceManager;
    private ProgramExecutor executor;
    private String contract;
    private ContractWrapperTransaction tx;
    private long number;
    private String preStateRoot;
    private int chainId;
    private int blockType;
    private long blockTime;
    private BatchInfoV8 batchInfo;


    public ContractTxCallableV14(int chainId, int blockType, long blockTime, ProgramExecutor executor, String contract, ContractWrapperTransaction tx, long number, String preStateRoot) {
        this.chainId = chainId;
        this.blockType = blockType;
        this.blockTime = blockTime;
        this.contractExecutor = SpringLiteContext.getBean(ContractExecutor.class);
        this.contractHelper = SpringLiteContext.getBean(ContractHelper.class);
        this.contractNewTxHandler = SpringLiteContext.getBean(ContractNewTxHandler.class);
        this.contractTransferHandler = SpringLiteContext.getBean(ContractTransferHandler.class);
        this.tempBalanceManager = contractHelper.getBatchInfoTempBalanceManagerV8(chainId);
        this.executor = executor;
        this.contract = contract;
        this.tx = tx;
        this.number = number;
        this.preStateRoot = preStateRoot;
    }

    public ContractResult call() throws Exception {
        ChainManager.chainHandle(chainId, blockType);
        this.batchInfo = contractHelper.getChain(chainId).getBatchInfoV8();
        String hash = tx.getHash().toHex();
        long start = System.currentTimeMillis();
        ContractData contractData;
        ContractResult contractResult = null;
        contractData = tx.getContractData();
        int type = tx.getType();
        do {
            if (type != DELETE_CONTRACT && !ContractUtil.checkPrice(contractData.getPrice())) {
                contractResult = contractHelper.makeFailedContractResult(chainId, tx, null, "The gas price is error.");
                break;
            }

            switch (type) {
                case CREATE_CONTRACT:
                    contractResult = contractExecutor.create(executor, contractData, number, preStateRoot, extractPublicKey(tx));
                    checkCreateResult(tx, contractResult);
                    break;
                case CROSS_CHAIN:
                case CALL_CONTRACT:
                    // 创建合约无论成功与否，后续的其他的跳过执行，视作失败 -> 合约锁定中或者合约不存在
                    if (batchInfo.getCreateSet().contains(contract)) {
                        contractResult = contractHelper.makeFailedContractResult(chainId, tx, null, "contract lock or not exist.");
                        break;
                    }
                    // 删除合约成功后，后续的其他的跳过执行，视作失败 -> 合约已删除
                    if (batchInfo.getDeleteSet().contains(contract)) {
                        contractResult = contractHelper.makeFailedContractResult(chainId, tx, null, "contract has been terminated.");
                        break;
                    }
                    contractHelper.extractAssetInfoFromCallTransaction((CallContractData) contractData, tx);
                    contractResult = contractExecutor.call(executor, contractData, number, preStateRoot, extractPublicKey(tx));
                    checkCallResult(tx, contractResult);
                    break;
                case DELETE_CONTRACT:
                    contractResult = contractExecutor.delete(executor, contractData, number, preStateRoot);
                    checkDeleteResult(tx, contractResult);
                    break;
                default:
                    break;
            }
        } while (false);
        if (contractResult != null) {
            if(!contractResult.isSuccess()) {
                Log.error("Failed TxType [{}] Execute ContractResult is {}", tx.getType(), contractResult.toString());
            }
        }
        Log.info("[Per Contract Execution Cost Time] TxType is {}, TxHash is {}, Cost Time is {}", tx.getType(), hash, System.currentTimeMillis() - start);
        return contractResult;
    }

    private void checkCreateResult(ContractWrapperTransaction tx, ContractResult contractResult) {
        makeContractResult(tx, contractResult);
        batchInfo.getCreateSet().add(contract);
        if (contractResult.isSuccess()) {
            Result checkResult = contractHelper.validateNrc20Contract(chainId, (ProgramExecutor) contractResult.getTxTrack(), tx, contractResult);
            if (checkResult.isFailed()) {
                Log.error("check validateNrc20Contract Result is {}", checkResult.toString());
            }
            if (checkResult.isSuccess()) {
                commitContract(contractResult);
            }
        }
    }


    private void checkCallResult(ContractWrapperTransaction tx, ContractResult contractResult) throws IOException, NulsException {
        makeContractResult(tx, contractResult);
        // 处理合约结果
        dealCallResult(tx, contractResult, chainId);
    }

    private void dealCallResult(ContractWrapperTransaction tx, ContractResult contractResult, int chainId) throws IOException, NulsException {
        if (contractResult.isSuccess()) {
            // 处理合约生成的其他交易、临时余额、合约内部转账
            boolean isSuccess = contractNewTxHandler.handleContractNewTx(chainId, blockTime, tx, contractResult, tempBalanceManager);
            if (isSuccess) {
                // add by pierre at 2022/6/2 p14
                // 处理内部创建
                List<ProgramInternalCreate> internalCreates = contractResult.getProgramInternalCreates();
                if (internalCreates != null && !internalCreates.isEmpty()) {
                    for (ProgramInternalCreate internalCreate : internalCreates) {
                        // 处理内部创建的合约，装到执行结果的`internalCreates`中
                        Result checkResult = contractHelper.validateNrc20ContractByInternalCreate(chainId, (ProgramExecutor) contractResult.getTxTrack(), internalCreate, contractResult);
                        if (checkResult.isFailed()) {
                            Log.error("ProgramInternalCreate check validateNrc20Contract Result is {}", checkResult.toString());
                            isSuccess = false;
                            break;
                        }
                        //TODO pierre 测试内部创建合约后，同一个区块中，发出调用此合约的交易
                        batchInfo.getCreateSet().add(AddressTool.getStringAddressByBytes(internalCreate.getContractAddress()));
                    }
                }
            }
            if (!isSuccess) {
                // 处理调用失败的合约，把需要退还的NULS 生成一笔合约内部转账交易，退还给调用者
                this.handleFailedContract(contractResult);
            }
        } else {
            // 处理调用失败的合约，把需要退还的NULS 生成一笔合约内部转账交易，退还给调用者
            this.handleFailedContract(contractResult);
        }
        // 处理合约内部转账成功后，提交合约
        if (contractResult.isSuccess()) {
            commitContract(contractResult);
        }
    }

    private void handleFailedContract(ContractResult contractResult) throws IOException, NulsException {
        ContractWrapperTransaction orginTx = contractResult.getTx();
        ContractData contractData = orginTx.getContractData();
        NulsHash orginTxHash = orginTx.getHash();
        BigInteger value = contractData.getValue();
        byte[] contractAddress = contractData.getContractAddress();

        int assetChainId, assetId;
        CoinData orginTxCoinData = orginTx.getCoinDataInstance();
        List<CoinFrom> fromList = orginTxCoinData.getFrom();
        for(CoinFrom from : fromList) {
            assetChainId = from.getAssetsChainId();
            assetId = from.getAssetsId();
            if (CHAIN_ID != assetChainId || ASSET_ID != assetId) {
                // 多个账户向合约转入多个资产，合约执行失败后，退还转入的资产金额
                ContractTransferTransaction tx = this.generateContractTransferTransaction(orginTxHash, contractAddress, from.getAddress(), from.getAmount(), assetChainId, assetId);
                contractResult.getContractTransferList().add(tx);
            } else if (from.getAmount().compareTo(value) >= 0){
                orginTx.setValueSender(from.getAddress());
            }
        }

        if (value.compareTo(BigInteger.ZERO) > 0) {
            byte[] sender = orginTx.getValueSender();
            if (sender == null) {
                sender = contractData.getSender();
            }
            ContractTransferTransaction tx = this.generateContractTransferTransaction(orginTxHash, contractAddress, sender, value, CHAIN_ID, ASSET_ID);
            contractResult.getContractTransferList().add(tx);
        }
        contractResult.setMergedTransferList(contractTransferHandler.contractTransfer2mergedTransfer(orginTx, contractResult.getContractTransferList()));
        contractResult.setMergerdMultyAssetTransferList(contractTransferHandler.contractMultyAssetTransfer2mergedTransfer(orginTx, contractResult.getContractTransferList()));
    }

    private ContractTransferTransaction generateContractTransferTransaction(NulsHash orginTxHash, byte[] contractAddress, byte[] recipient, BigInteger value, int assetChainId, int assetId) throws IOException {
        ContractTransferData txData = new ContractTransferData(orginTxHash, contractAddress);

        CoinData coinData = new CoinData();
        ContractBalance balance = tempBalanceManager.getBalance(contractAddress, assetChainId, assetId).getData();
        byte[] nonceBytes = RPCUtil.decode(balance.getNonce());

        CoinFrom coinFrom = new CoinFrom(contractAddress, assetChainId, assetId, value, nonceBytes, (byte) 0);
        coinData.getFrom().add(coinFrom);
        CoinTo coinTo = new CoinTo(recipient, assetChainId, assetId, value, 0L);
        coinData.getTo().add(coinTo);

        ContractTransferTransaction tx = new ContractTransferTransaction();
        tx.setCoinDataObj(coinData);
        tx.setTxDataObj(txData);
        tx.setTime(blockTime);

        tx.serializeData();
        NulsHash hash = NulsHash.calcHash(tx.serializeForHash());
        byte[] hashBytes = hash.getBytes();
        byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
        balance.setNonce(RPCUtil.encode(currentNonceBytes));
        tx.setHash(hash);
        return tx;
    }

    private void commitContract(ContractResult contractResult) {
        if (!contractResult.isSuccess()) {
            return;
        }
        Object txTrackObj = contractResult.getTxTrack();
        if (txTrackObj != null && txTrackObj instanceof ProgramExecutor) {
            ProgramExecutor txTrack = (ProgramExecutor) txTrackObj;
            txTrack.commit();
        }
    }

    private boolean checkDeleteResult(ContractWrapperTransaction tx, ContractResult contractResult) {
        batchInfo.getDeleteSet().add(contract);
        makeContractResult(tx, contractResult);
        boolean result = false;
        if (contractResult.isSuccess()) {
            result = true;
            commitContract(contractResult);
        }
        return result;
    }
}
