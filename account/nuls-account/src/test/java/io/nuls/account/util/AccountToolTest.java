package io.nuls.account.util;

import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.Address;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import org.junit.Test;

/**
 * @author Niels
 */
public class AccountToolTest {

    @Test
    public void createAccount() throws NulsException {
        System.out.println("=======================test net=======================");
        for (int i = 0; i < 100; i++) {
            ECKey key = new ECKey();
            Address address = new Address(2, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
            System.out.println(address.getBase58()+"==========="+key.getPrivateKeyAsHex());
        }
        System.out.println("=======================main net=======================");
        for (int i = 0; i < 100; i++) {
            ECKey key = new ECKey();
            Address address = new Address(1, (byte)1, SerializeUtils.sha256hash160(key.getPubKey()));
            System.out.println(address.getBase58()+"==========="+key.getPrivateKeyAsHex());
        }
    }
}