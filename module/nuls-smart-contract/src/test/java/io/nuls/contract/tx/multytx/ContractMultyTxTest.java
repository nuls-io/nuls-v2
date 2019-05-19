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

import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.tx.contractcallcontract.ContractCallContractSendTxTest;
import io.nuls.contract.tx.nrc20.ContractNRC20TokenSendTxTest;
import io.nuls.contract.util.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

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
        // TestAddress.createAccount 生成地址，得到 importPriKey 语句，放入 contractNRC20TokenSendTxTest.importPriKeyTest 中执行
    }

    @Test
    public void loopCallContract() throws Exception {
        contractNRC20TokenSendTxTest.setContractAddress_nrc20("tNULSeBaMxPE2ESCiEAZ9CxXMyzBkySBKKSUQ4");
        long s = System.currentTimeMillis();
        int times = 2000;
        for(int i=0;i<times;i++) {
            contractNRC20TokenSendTxTest.callContract();
        }
        long e = System.currentTimeMillis();
        Log.info("{} times cost time is {}", times, e - s);
    }

    @Test
    public void createNRC20AndInnerCallContractTest() throws Exception {
        contractNRC20TokenSendTxTest.createContract();
        contractCallContractSendTxTest.createContract();
    }

    @Test
    public void callAndDeleteTest() throws Exception {
        contractNRC20TokenSendTxTest.setContractAddress_nrc20("tNULSeBaNAsyKtqQRFPVQkxtiEch4hw4X6iYdZ");
        contractNRC20TokenSendTxTest.callContract();

        TimeUnit.MILLISECONDS.sleep(500);

        // tNULSeBaN6AqEa9HL9mXdHEcUqQMaJ8KkH5X43
        contractCallContractSendTxTest.setContractAddress("tNULSeBaN3Yy2cJdJ62atRja7r8WMGBgwHmQS6");
        contractCallContractSendTxTest.delete();
    }

    /**
     * 依赖于contractNRC20TokenSendTxTest.transfer()
     * 35个sender 创建35个NRC20的合约
     */
    @Test
    public void multyCreateNRC20() throws Exception {
        for (int i = 0; i < 35; i++) {
            contractNRC20TokenSendTxTest.setSender(address("getToAddress", i));
            contractNRC20TokenSendTxTest.createContract();
        }
        // 执行后，复制contract module日志的合约地址，赋值给成员变量contractAddress_nrc20X (X -> [0,34])
    }

    /**
     * 依赖于contractNRC20TokenSendTxTest.transfer()
     * 35个sender 创建35个可内部调用的合约
     */
    @Test
    public void multyCreateContractCallContract() throws Exception {
        // 执行后，复制contract module日志的合约地址，赋值给成员变量contractAddressX (X -> [0,34])
        for (int i = 0; i < 35; i++) {
            contractCallContractSendTxTest.setSender(address("getToAddress", i));
            contractCallContractSendTxTest.createContract();
        }
    }

    /**
     * 35个sender 调用一个NRC20合约，比较时间
     */
    @Test
    public void multySenderCallOneContract() throws Exception {
        int times = 35;
        contractNRC20TokenSendTxTest.setContractAddress_nrc20(address("getContractAddress_nrc20", 0));
        contractNRC20TokenSendTxTest.setMethodName("approve");
        for (int i = 0; i < times; i++) {
            contractNRC20TokenSendTxTest.setSender(address("getToAddress", i));
            contractNRC20TokenSendTxTest.callContract();
        }
    }

    /**
     * 35个sender 调用35个NRC20合约，比较时间
     */
    @Test
    public void multySenderCallMultyContracts() throws Exception {
        int times = 35;
        contractNRC20TokenSendTxTest.setMethodName("approve");
        for (int i = 0; i < times; i++) {
            contractNRC20TokenSendTxTest.setSender(address("getToAddress", i));
            contractNRC20TokenSendTxTest.setContractAddress_nrc20(address("getContractAddress_nrc20", i));
            contractNRC20TokenSendTxTest.callContract();
        }
    }

    /**
     * 35个sender 调用35个NRC20合约，向`contractCallContract`合约转入token
     */
    @Test
    public void multySenderTokenTransferToMultyContracts() throws Exception {
        int times = 35;
        for (int i = 0; i < times; i++) {
            contractNRC20TokenSendTxTest.setSender(address("getToAddress", i));
            contractNRC20TokenSendTxTest.setContractAddress_nrc20(address("getContractAddress_nrc20", i));
            contractNRC20TokenSendTxTest.setContractAddress(address("getContractAddress", i));
            contractNRC20TokenSendTxTest.tokenTransfer();
        }
    }

    /**
     * 35个sender 每7个调用一个合约（一共5个合约），比较时间
     */
    @Test
    public void multySenderCallFiveContracts() throws Exception {
        int times = 35;
        contractNRC20TokenSendTxTest.setMethodName("approve");
        for (int i = 0; i < times; i++) {
            contractNRC20TokenSendTxTest.setSender(address("getToAddress", i));
            if(i % 7 == 0) {
                contractNRC20TokenSendTxTest.setContractAddress_nrc20(address("getContractAddress_nrc20", i));
            }
            contractNRC20TokenSendTxTest.callContract();
        }
    }

    /**
     * 35个sender 每5个调用一个合约（一共7个合约），方法为内部调用，内部调用的合约在外层7个合约当中，确保能够出现内部调用与外层调用冲突
     */
    @Test
    public void multySenderCallSevenContracts() throws Exception {
        int times = 35;
        String sender;
        contractNRC20TokenSendTxTest.setMethodName("approve");
        for (int i = 0; i < times; i++) {
            sender = address("getToAddress", i);
            contractNRC20TokenSendTxTest.setSender(sender);
            contractCallContractSendTxTest.setSender(sender);
            if(i % 5 == 0) {
                if((i/5 +1)%2 == 0) {
                    contractCallContractSendTxTest.setContractAddress(address("getContractAddress", i));
                } else {
                    contractNRC20TokenSendTxTest.setContractAddress_nrc20(address("getContractAddress_nrc20", i));
                }
            }
            if(i%10 < 5) {
                contractCallContractSendTxTest.callContract_contractCallContract();
            } else {
                contractNRC20TokenSendTxTest.callContract();
            }
        }
    }

}
