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
package io.nuls.account.rpc.cmd;

import io.nuls.account.model.bo.Account;
import io.nuls.account.util.AccountTool;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 * @description
 * @date 2018/12/14
 **/
public class TestAddress {
    private int chainId = 2;

    @Test
    public void test1(){
        try {
            Account account = AccountTool.createAccount(8964);
//           System.out.println( AddressTool.validAddress(8964,"JQJmP5xKDzAgJ8tJSQkCtKwbodAu20423"));
            System.out.println(account.getAddress().toString());
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test2(){
            System.out.println( AddressTool.validAddress(8964,"JQJmP5xKDzAgJ8tJSQkCtKwbodAu20423"));
    }

    @Test
    public void createAccount() throws NulsException {
        int start = 5, count = 30;
        List<Account> list = new ArrayList<>();
        for(int i=0;i<count;i++) {
            list.add(createAccount(chainId));
        }
        System.out.println();
        System.out.println();
        for(Account account : list) {
            System.out.println("importPriKey(\""+ HexUtil.encode(account.getPriKey()) +"\", password);//"+(start++)+" "+account.getAddress().toString());
        }
        System.out.println();
        System.out.println();
        start = 5;
        for(Account account : list) {
            System.out.println("protected String toAddress"+(start++)+" = \""+account.getAddress().toString()+"\";");
        }
        System.out.println();
        System.out.println();
    }

    private static Account createAccount(int chainId) throws NulsException {
        ECKey key = new ECKey();
        Address address = new Address(chainId, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
        Account account = new Account();
        account.setChainId(chainId);
        account.setAddress(address);
        account.setPubKey(key.getPubKey());
        account.setPriKey(key.getPrivKeyBytes());
        account.setEncryptedPriKey(new byte[0]);
        account.setCreateTime(System.currentTimeMillis());
        account.setEcKey(key);
        return account;
    }
}
