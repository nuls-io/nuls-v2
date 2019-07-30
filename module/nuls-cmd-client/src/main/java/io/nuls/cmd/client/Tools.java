package io.nuls.cmd.client;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.cmd.client.config.Config;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.parse.SerializeUtils;

import java.util.Arrays;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 18:22
 * @Description: 功能描述
 */
public class Tools {

    public static void main(String[] args) {
        args = new String[]{"address",String.valueOf(Integer.MAX_VALUE)};
        if(args.length < 1){
            System.out.println("cmd must be null");
            System.exit(0);
        }
        SpringLiteContext.init("io.nuls.cmd.client.config");
        Config config = SpringLiteContext.getBean(Config.class);
        String cmd = args[0];
        switch (cmd){
            case "address" : {
                int count = 1;
                int chainId = config.getChainId();
                if(args.length >= 3){
                    chainId = Integer.parseInt(args[2]);
                }
                if(args.length >= 2){
                    count = Integer.parseInt(args[1]);
                }
                System.out.println("chainId:"+chainId);
                System.out.println("number:"+count);
                for (int i = 0; i < count; i++) {
                    ECKey key = new ECKey();
                    Address address = new Address(chainId, config.getAddressPrefix(),BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
                    System.out.println("=".repeat(100));
                    System.out.println("address   :" + AddressTool.getStringAddressByBytes(address.getAddressBytes(),address.getPrefix()));
                    System.out.println("privateKey:" + key.getPrivateKeyAsHex());
                    System.out.println("=".repeat(100));

                }
                System.exit(0);
            }
            default:
                System.out.println("error command :" + args[0]);
                System.exit(0);
        }
    }

}
