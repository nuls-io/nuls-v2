package io.nuls.contract.mock.helper;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.Frame;
import io.nuls.contract.helper.ContractNewTxFromOtherModuleHandler;

public class ContractNewTxFromOtherModuleHandlerMock extends ContractNewTxFromOtherModuleHandler{

    @Override
    public Transaction updateNonceAndVmBalance(int chainId, byte[] contractAddressBytes, String txHash, String txStr, Frame frame) {
        Log.info("chainId: {}, contractAddress: {}, txHash: {}, txStr: {}", chainId, AddressTool.getStringAddressByBytes(contractAddressBytes), txHash, txStr);
        return null;
    }
}