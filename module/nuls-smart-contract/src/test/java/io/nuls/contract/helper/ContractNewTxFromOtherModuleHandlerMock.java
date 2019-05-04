package io.nuls.contract.helper;

import io.nuls.contract.util.Log;

public class ContractNewTxFromOtherModuleHandlerMock extends ContractNewTxFromOtherModuleHandler{

    @Override
    public void handleContractNewTxFromOtherModule(int chainId, String txHash, String txStr) {
        Log.info("chainId: {}, txHash: {}, txStr: {}", chainId, txHash, txStr);
    }
}