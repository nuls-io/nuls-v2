/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.ledger.service.impl;

import io.nuls.base.data.Transaction;
import io.nuls.ledger.model.ChainHeight;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.BlockSnapshotAccounts;
import io.nuls.ledger.model.po.BlockTxs;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.BlockDataService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.LedgerUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

import java.util.List;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * @author lan
 * @description
 * @date 2019/02/14
 **/
@Service
public class BlockDataServiceImpl implements BlockDataService {
    @Autowired
    private Repository repository;
    @Autowired
    private AccountStateService accountStateService;
    @Override
    public void initBlockDatas() throws Exception {
        //获取确认高度
        List<ChainHeight> list = repository.getChainsBlockHeight();
        if(null != list){
            for(ChainHeight chainHeight : list ){
                BlockSnapshotAccounts blockSnapshotAccounts = repository.getBlockSnapshot(chainHeight.getChainId(),chainHeight.getBlockHeight()+1) ;
                if(null != blockSnapshotAccounts){
                    List<AccountState> preAccountStates = blockSnapshotAccounts.getAccounts();
                    //回滚高度
                    for (AccountState accountState :preAccountStates) {
                        String key = LedgerUtils.getKeyStr(accountState.getAddress(), accountState.getAssetChainId(), accountState.getAssetId());
                        accountStateService.rollAccountState(key,accountState);
                        logger.info("rollBack account={},assetChainId={},assetId={}, height={},lastHash= {} ", key, accountState.getAssetChainId(),accountState.getAssetId(),
                                accountState.getHeight(), accountState.getTxHash());
                    }
                }
            }
        }
    }

    @Override
    public void saveLatestBlockDatas(int chainId,long height,List<Transaction> txList) throws Exception {
        BlockTxs blockTxs = new BlockTxs();
        blockTxs.setTransactions(txList);
        repository.saveBlock(chainId,height,blockTxs);

    }
}
