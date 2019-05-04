package io.nuls.contract.helper;

import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.rpc.util.RPCUtil;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ContractHelperMock extends ContractHelper{

    @Override
    public ContractBalance getBalance(int chainId, byte[] address) {
        byte[] currentNonceBytes = Arrays.copyOfRange(address, address.length - 8, address.length);
        ContractBalance contractBalance = ContractBalance.newInstance();
        contractBalance.setNonce(RPCUtil.encode(currentNonceBytes));
        return contractBalance;
    }
}