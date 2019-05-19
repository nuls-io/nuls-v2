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

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.model.ChainHeight;
import io.nuls.ledger.model.po.AccountStateSnapshot;
import io.nuls.ledger.model.po.BlockSnapshotAccounts;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.BlockDataService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LoggerUtil;

import java.util.List;

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
    @Autowired
    private LedgerChainManager ledgerChainManager;
    @Override
    public void initBlockDatas() throws Exception {
        //获取确认高度
        List<ChainHeight> list = repository.getChainsBlockHeight();
        if(null != list){
            LoggerUtil.logger().info("chainList size = {}", list.size());
            for(ChainHeight chainHeight : list ){
                LoggerUtil.logger().info("begin chain ledger checked..chainId = {},chainHeight={}", chainHeight.getChainId(), chainHeight.getBlockHeight());
                BlockSnapshotAccounts blockSnapshotAccounts = repository.getBlockSnapshot(chainHeight.getChainId(),chainHeight.getBlockHeight()+1) ;
                if(null != blockSnapshotAccounts){
                    List<AccountStateSnapshot> preAccountStates = blockSnapshotAccounts.getAccounts();
                    //回滚高度
                    for (AccountStateSnapshot accountStateSnapshot :preAccountStates) {
                        String key = LedgerUtil.getKeyStr(accountStateSnapshot.getAccountState().getAddress(), accountStateSnapshot.getAccountState().getAssetChainId(), accountStateSnapshot.getAccountState().getAssetId());
                        accountStateService.rollAccountState(key,accountStateSnapshot);
                        LoggerUtil.logger().info("rollBack account={},assetChainId={},assetId={}, height={},lastHash= {} ", key,  accountStateSnapshot.getAccountState().getAssetChainId(), accountStateSnapshot.getAccountState().getAssetId(),
                                accountStateSnapshot.getAccountState().getHeight(), accountStateSnapshot.getAccountState().getTxHash());
                    }
                }
                LoggerUtil.logger().info("end chain ledger checked..chainId = {},chainHeight={}", chainHeight.getChainId(), chainHeight.getBlockHeight());
            }
        }
    }
}
