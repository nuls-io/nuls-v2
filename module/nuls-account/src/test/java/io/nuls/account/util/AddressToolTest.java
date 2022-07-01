package io.nuls.account.util;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.base.data.Block;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.sdk.core.model.CallContractData;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Niels
 */
public class AddressToolTest {

    @Test
    public void test7y() throws NulsException, io.nuls.sdk.core.exception.NulsException {
//        String address = "NULSd6Hgam8YajetEDnCoJBdEFkMNP41PfH7y";
//        System.out.println(AddressTool.validAddress(1,address));
        Block block = new Block();
        block.parse(HexUtil.decode("a829c86c37a448cc1af7aeb1fec3b8a8c22bec7b6cc337cf2bcde4392f879465eabb575d75a7bbca1b4349a08b1edd8ab95842ca3a2b9428965ebc16485fd8d50492b45fd2da3800070000005c178f00005f00768eb45f5b0007000700500a0020adcce866f2cc3ebfab4e9d777c2902b4338cdc32c4de575e9f0307c781e9d7583e223721c6373a13df8e5289934b70c7f008f4ec09ae6223b58fae21c17e56449f6168a5210b41bd21031771427c2a5e80d84b1dfc86fde6b48a7e91655849a9fef34082bd58afa71a4a46304402206cf00001c8def91af2a30a9a02b91e685e22360112e88ced637a7553d7d13d7d0220335c860e4e18bf99eeab50121abf6b1f59099339126d1e7b58cd0459b4bf0d8b01000492b45f00008a000217010001fedf6a65d337e344872e8cdf455d39537e78e539010001004f202804000000000000000000000000000000000000000000000000000000000000000000000000170100018efa2de9522ce624e53d635993e042f3c5f90c7001000100f578ec00000000000000000000000000000000000000000000000000000000000000000000000000001000f691b45f009c0100011d52afe277b0c575355e618a194ffa1ae1cb518f010002be36277487d7c45974d1fcc722d6729f47cddd8900000000000000000000000000000000000000000000000000000000000000001b540000000000001900000000000000087472616e73666572000201254e554c5364364867554763523351626f76593155467561753532374532464e354c70434e54010a333030303030303030304801170100011d52afe277b0c575355e618a194ffa1ae1cb518f0100010043bd090000000000000000000000000000000000000000000000000000000000082665ce6eda74395500006a2103a1f65c80936606df6185fe9bd808d7dd5201e1e88f2a475f6b2a70d81f7f52e4473045022100f79379974afcd3a06289076f9ef62c635636b0ce1ce00174e5bb0f869ad1406e022014680dc8750e614ce61da83260ba3aca2513b560cb0b82680d31251608e56afd1000f591b45f009c01000191053835d0242f800d2d8786c2e9813cde1ae020010002be36277487d7c45974d1fcc722d6729f47cddd890000000000000000000000000000000000000000000000000000000000000000a0860100000000001900000000000000087472616e73666572000201254e554c536436486767644353705475464b524761623874395163434e455245623631796766010a3136383030363535303748011701000191053835d0242f800d2d8786c2e9813cde1ae0200100010040ac27000000000000000000000000000000000000000000000000000000000008c4577cb35a4a07d200006a21036358a3e9ab6da104d177b029e19ff305608d1457e87afdcb7cd6823fc3e923994730450221008e1eba0e0216c03178d7b3ef25caff1465f13a0a39866f531edc8e99abaaa6460220142c8e71e7ca2178ce14bea4caeaadefb754279985efe077f8e4483c2ba615710200f791b45f00008c01170100016f60abe6550578d554d1b2539d5464160df4ceb201000100a067f70500000000000000000000000000000000000000000000000000000000088216c34144b051fc0001170100010efee4de7eba9220847645799b0cc18c1c9554980100010000e1f505000000000000000000000000000000000000000000000000000000000000000000000000692102f5a7c4151ed8bbe3af47a55e40b464504a1e77a0fe8cfe92a2757058c08bc49b463044022052ce140fd312b3e1b40e4eaa48a23690fc0f889fb147582c999d2a56a1808ba702200abd4ba4109cad251e4b9088b3795d658c062dfca56baab2eccd5a491ce82aa91000fa91b45f0094010001d1e60e0b3fb774e325165043aae8b2b3335d60b80100029dcaeebcca8c5ec7e6d1ca7727f530e89e4cd4120000000000000000000000000000000000000000000000000000000000000000bcd001000000000019000000000000000b636c61696d4561726e65641d28537472696e6720636176654e616d65292072657475726e20766f6964010109426c61636b49726f6e480117010001d1e60e0b3fb774e325165043aae8b2b3335d60b801000100fce82e0000000000000000000000000000000000000000000000000000000000082ba5a208dd1a0bb300006921033852484d44ec6a7d0fa740a9aad6dfa399b59ccf26a035fb7ca9c630019186ca4630440220421aa258be60c590688871a447dc3415036bc74591d78532c07e8d059051f19502203ca83d52d5d00fd4a0967caf76110a00aadf2817deff89ab71d9a7466b3949dc1000fa91b45f00ce0100015470f6c2c3d3850d5fe3ded46e5c658e9fb6393e010002be36277487d7c45974d1fcc722d6729f47cddd890000000000000000000000000000000000000000000000000000000000000000a839000000000000190000000000000007617070726f7665322841646472657373207370656e6465722c20426967496e74656765722076616c7565292072657475726e20626f6f6c65616e0201254e554c53643648677878584a34767a66336a58387365546d444e6e384242626e6354554850010b32343030303030303030304801170100015470f6c2c3d3850d5fe3ded46e5c658e9fb6393e0100010008280700000000000000000000000000000000000000000000000000000000000879acd1b1f9062e8700006a2102b5e3913df715832ebe207b63e30333355aa285b4183e035b84fa767f67edf6224730450221008ead5406624ce7715feb0a3da547081166dc491b14cd32259d28b4137abb0ca5022045d3393df0832a5588039aac2d4fc30ec34e4dcd281935db9fbeba20892c3c2b13000492b45f0000fd12010004170100011d52afe277b0c575355e618a194ffa1ae1cb518f01000100e1bc02000000000000000000000000000000000000000000000000000000000000000000000000001701000191053835d0242f800d2d8786c2e9813cde1ae0200100010002de1f0000000000000000000000000000000000000000000000000000000000000000000000000017010001d1e60e0b3fb774e325165043aae8b2b3335d60b801000100b5220f00000000000000000000000000000000000000000000000000000000000000000000000000170100015470f6c2c3d3850d5fe3ded46e5c658e9fb6393e0100010078e0010000000000000000000000000000000000000000000000000000000000000000000000000000"), 0);
        for (Transaction tx : block.getTxs()) {
            if(tx.getHash().toHex().equals("7f4a3125bd232a1be8bed8b6ca7567aa69ca5f4fdb67048287bffcfca74efd82")){
                System.out.println(HexUtil.encode(tx.getTxData()));
                CallContractData data = new CallContractData();
                data.parse(tx.getTxData(),0);
                System.out.println(data);


            }
        }
    }

    @Test
    public void createAddress(){
        ECKey ecKey = new ECKey();
        System.out.println(AddressTool.getAddressString(ecKey.getPubKey(),1)+" ==== "+ecKey.getPrivateKeyAsHex());
    }

    @Test
    public void createAccountByPrefix() {
        AddressTool.addPrefix(4, "LJS");
        for (int i = 0; i < 10; i++) {
            ECKey key = new ECKey();
            Address address = new Address(4, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
            System.out.println(address.toString() + "================" + address.getBase58() + "===========" + key.getPrivateKeyAsHex());
        }
    }

    @Test
    public void creaateMainNetAccount() {
        System.out.println("=======================main net=======================");
        while (true) {
            ECKey key = new ECKey();
            Address address = new Address(1, (byte) 1, SerializeUtils.sha256hash160(key.getPubKey()));
            String value = address.getBase58();
            if (value.toUpperCase().endsWith("55"))
                System.out.println(value + "===========" + key.getPrivateKeyAsHex());
        }
    }

    @Test
    public void getBlackWhole() {
        Address address = new Address(1, (byte) 1, SerializeUtils.sha256hash160(HexUtil.decode("000000000000000000000000000000000000000000000000000000000000000000")));
        System.out.println(address);
    }

    /**
     * 通缩计算
     */
    @Test
    public void calc() {
        double rate = 0.996;
        long total = 21000000000000000l;
        long init = 11000000000000000l;
        long month = 1;
        long monthReward = 41095890410959L;
        while (init < total) {
            monthReward = (long) DoubleUtils.mul(monthReward, rate);
            if (0 == monthReward) {
                break;
            }
            init = init + monthReward;
            month++;
        }
        System.out.println(init);
        System.out.println(month);
        System.out.println(month / 12);
    }

    @Test
    public void createAccount() throws NulsException {
        System.out.println("=======================test net=======================");
        for (int i = 0; i < 100; i++) {
            ECKey key = new ECKey();
            Address address = new Address(2, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
            System.out.println(address.getBase58() + "===========" + key.getPrivateKeyAsHex());
        }
        System.out.println("=======================main net=======================");
        for (int i = 0; i < 100; i++) {
            ECKey key = new ECKey();
            Address address = new Address(1, (byte) 1, SerializeUtils.sha256hash160(key.getPubKey()));
            System.out.println(address.getBase58() + "===========" + key.getPrivateKeyAsHex());
        }
        System.out.println("=======================other net=======================");
        for (int i = 3; i < 100; i++) {
            ECKey key = new ECKey();
            Address address = new Address(i, (byte) 1, SerializeUtils.sha256hash160(key.getPubKey()));
            System.out.println(i + "==========" + address.getBase58() + "===========" + key.getPrivateKeyAsHex());
        }
        for (int i = 65535; i > 65400; i--) {
            ECKey key = new ECKey();
            Address address = new Address(i, (byte) 1, SerializeUtils.sha256hash160(key.getPubKey()));
            System.out.println(i + "==========" + address.getBase58() + "===========" + key.getPrivateKeyAsHex());
        }
    }


    @Test
    public void testValid() {
        String address1 = "tNULSeBaMrNbr7kDHan5tBVms4fUZbfzed6851";
        boolean result = AddressTool.validAddress(2, address1);
        assertTrue(!result);

        address1 = "NULSeBaMrNbr7kDHan5tBVms4fUZbfzed685k";
        result = AddressTool.validAddress(1, address1);
        assertTrue(!result);

        address1 = "AHUcC84FN4CWrhuMgvvGPy6UacBvcutgQ4rAR";
        result = AddressTool.validAddress(65401, address1);
        assertTrue(!result);

    }

    @Test
    public void testGetAddress() {
        String address = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";

        byte[] bytes = AddressTool.getAddress(address);

        String address1 = AddressTool.getStringAddressByBytes(bytes);

        assertTrue(address.equalsIgnoreCase(address1));

    }

    @Test
    public void testChainId() {
        String address = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
        int id = AddressTool.getChainIdByAddress(address);
        System.out.println(id);

        boolean result = AddressTool.validAddress(2, address);
        assertTrue(result);
    }

    @Test
    public void testGetPrefix() {
        String address1 = "tNULSeBaMrNbr7kDHan5tBVms4fUZbfzed6851";
        String address2 = "NULSeBaMrNbr7kDHan5tBVms4fUZbfzed685k";
        String address3 = "APNcCm4yik6XXquTHUNbHqfPhGrfcSoGoMudc";


        assertEquals("tNULS", AddressTool.getPrefix(address1));
        assertEquals("NULS", AddressTool.getPrefix(address2));
        assertEquals("APN", AddressTool.getPrefix(address3));


    }
}