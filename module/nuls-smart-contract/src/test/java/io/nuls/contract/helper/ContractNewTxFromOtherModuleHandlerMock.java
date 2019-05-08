package io.nuls.contract.helper;

import io.nuls.base.basic.AddressTool;
import io.nuls.contract.util.Log;

public class ContractNewTxFromOtherModuleHandlerMock extends ContractNewTxFromOtherModuleHandler{

    @Override
    public void handleContractNewTxFromOtherModule(int chainId, byte[] contractAddressBytes, String txHash, String txStr) {
        Log.info("chainId: {}, contractAddress: {}, txHash: {}, txStr: {}", chainId, AddressTool.getStringAddressByBytes(contractAddressBytes), txHash, txStr);
    }
}