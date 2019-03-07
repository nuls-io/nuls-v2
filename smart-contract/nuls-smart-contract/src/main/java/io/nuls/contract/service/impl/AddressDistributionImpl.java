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
package io.nuls.contract.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.model.txdata.DeleteContractData;
import io.nuls.contract.service.AddressDistribution;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.*;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
@Service
public class AddressDistributionImpl implements AddressDistribution {

    @Override
    public Map<String, List<ContractWrapperTransaction>> distribution(List<Transaction> txList) throws NulsException {
        Map<String, List<ContractWrapperTransaction>> map = new LinkedHashMap<>();
        ContractWrapperTransaction ctx;
        byte[] contractAddressBytes;
        String contractAddress;
        int i = 0;
        for (Transaction tx : txList) {
            ctx = this.parseContractTransaction(tx);
            if(ctx == null) {
                continue;
            }
            ctx.setOrder(i++);
            contractAddressBytes = ctx.getContractData().getContractAddress();
            contractAddress = AddressTool.getStringAddressByBytes(contractAddressBytes);
            List<ContractWrapperTransaction> transactions = map.get(contractAddress);
            if (transactions == null) {
                transactions = new ArrayList<>();
                map.put(contractAddress, transactions);
            }
            transactions.add(ctx);
        }
        return map;
    }

    private ContractWrapperTransaction parseContractTransaction(Transaction tx) throws NulsException {
        ContractWrapperTransaction contractTransaction = null;
        ContractData contractData = null;
        boolean isContractTx = true;
        switch (tx.getType()) {
            case TX_TYPE_CREATE_CONTRACT:
                CreateContractData create = new CreateContractData();
                create.parse(tx.getTxData(), 0);
                contractData = create;
                break;
            case TX_TYPE_CALL_CONTRACT:
                CallContractData call = new CallContractData();
                call.parse(tx.getTxData(), 0);
                contractData = call;
                break;
            case TX_TYPE_DELETE_CONTRACT:
                DeleteContractData delete = new DeleteContractData();
                delete.parse(tx.getTxData(), 0);
                contractData = delete;
                break;
            default:
                isContractTx = false;
                break;
        }
        if(isContractTx) {
            contractTransaction = new ContractWrapperTransaction(tx, contractData);
        }
        return contractTransaction;
    }

}
