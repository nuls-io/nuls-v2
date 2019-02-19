package io.nuls.account.util;

import io.nuls.tools.crypto.ECKey;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Niels
 */
public class AccountToolsTest {

    @Test
    public void createAddress() {
        for (int i = 0; i < 100; i++) {
            ECKey ecKey = new ECKey();
            String address = AccountTools.createAddress((short) 261, (byte) 1, ecKey);
            System.out.println(address);
        }
        System.out.println("====================================");
        for (int i = 0; i < 100; i++) {
            ECKey ecKey = new ECKey();
            String address = AccountTools.createAddress((short) 261, (byte) 2, ecKey);
            System.out.println(address);
        }
        System.out.println("====================================");
        for (int i = 0; i < 100; i++) {
            ECKey ecKey = new ECKey();
            String address = AccountTools.createAddress((short) 261, (byte) 3, ecKey);
            System.out.println(address);

        }
        assertTrue(true);
    }
}