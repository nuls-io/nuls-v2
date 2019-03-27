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
package io.nuls.contract.tx.multytx;

import io.nuls.contract.tx.ContractNRC20TokenSendTxTest;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.tx.contractcallcontract.ContractCallContractSendTxTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: PierreLuo
 * @date: 2019-03-26
 */
public class ContractMultyTxTest extends BaseQuery {

    private ContractCallContractSendTxTest contractCallContractSendTxTest;
    private ContractNRC20TokenSendTxTest contractNRC20TokenSendTxTest;

    @Before
    public void beforeTest() {
        contractCallContractSendTxTest = new ContractCallContractSendTxTest();
        contractNRC20TokenSendTxTest = new ContractNRC20TokenSendTxTest();
    }

    @Test
    public void multyCreateNRC20() throws Exception {
        // 执行后，复制contract module日志的合约地址，赋值给成员变量contractAddress_nrc20X (X -> [0,34])
        for (int i = 0; i < 35; i++) {
            contractNRC20TokenSendTxTest.setSender(sender("getToAddress", i));
            contractNRC20TokenSendTxTest.createContract();
        }
    }

    @Test
    public void multyCreateContractCallContract() throws Exception {
        // 执行后，复制contract module日志的合约地址，赋值给成员变量contractAddressX (X -> [0,34])
        for (int i = 0; i < 35; i++) {
            contractCallContractSendTxTest.setSender(sender("getToAddress", i));
            contractCallContractSendTxTest.createContract();
        }
    }

    private String sender(String methodBaseName, int i) throws Exception {
        return this.getClass().getMethod(methodBaseName + i).invoke(this).toString();
    }

    @Test
    public void loopCallInit() throws Exception {
        // 创建NRC20合约, 调用ContractNRC20TokenSendTxTest.createContract, 得到合约地址，赋值给成员变量contractAddress_nrc20
        // 创建ContractCallContract合约, 调用ContractCallContractSendTxTest.createContract, 得到合约地址，赋值给成员变量contractAddress
        contractCallContractSendTxTest.tokenTransfer();
        contractCallContractSendTxTest.setSender(sender("getToAddress", 0));
        contractCallContractSendTxTest.transfer2Contract();
    }

    @Test
    public void loopCall() throws Exception {
        int times = 1;
        for (int i = 0; i < times; i++) {
            contractCallContractSendTxTest.callContract_transferOut();
            contractCallContractSendTxTest.setSender(sender("getToAddress", 0));
            contractCallContractSendTxTest.callContract_contractCallContract();
            contractCallContractSendTxTest.setSender(sender("getToAddress", 1));
            contractCallContractSendTxTest.callContract_transferOut_contractCallContract();
            contractCallContractSendTxTest.setSender(sender("getToAddress", 2));
            contractCallContractSendTxTest.callContract_transferOut();
            contractCallContractSendTxTest.setSender(sender("getToAddress", 3));
            contractCallContractSendTxTest.callContract_contractCallContract();
            contractCallContractSendTxTest.setSender(sender("getToAddress", 4));
            contractCallContractSendTxTest.callContract_transferOut_contractCallContract();
        }
    }

    //TODO pierre 35个sender 调用一个合约，比较时间

    //TODO pierre 35个sender 调用35个合约，比较时间

    //TODO pierre 35个sender 每7个调用一个合约（一共5个合约），比较时间

    //TODO pierre 35个sender 每5个调用一个合约（一共7个合约），方法为内部调用，内部调用的合约在外层7个合约当中，确保能够出现内部调用与外层调用冲突
}
