package io.nuls.contract.mock.helper;

import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.core.rpc.util.RPCUtil;

import java.util.Arrays;

public class ContractHelperMock extends ContractHelper {

    public ContractBalance getBalance(int chainId, byte[] address) {
        byte[] currentNonceBytes = Arrays.copyOfRange(address, address.length - 8, address.length);
        ContractBalance contractBalance = ContractBalance.newInstance();
        contractBalance.setNonce(RPCUtil.encode(currentNonceBytes));
        return contractBalance;
    }
}