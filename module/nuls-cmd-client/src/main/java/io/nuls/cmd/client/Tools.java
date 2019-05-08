package io.nuls.cmd.client;

import io.nuls.base.data.Address;
import io.nuls.cmd.client.config.Config;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.parse.SerializeUtils;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 18:22
 * @Description: 功能描述
 */
public class Tools {

    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("cmd must be null");
            System.exit(0);
        }
        SpringLiteContext.init("io.nuls.cmd.client.config");
        String cmd = args[0];
        switch (cmd){
            case "address" : {
                int count = 1;
                if(args.length == 2){
                    count = Integer.parseInt(args[1]);
                }
                Config config = SpringLiteContext.getBean(Config.class);
                for (int i = 0; i < count; i++) {
                    ECKey key = new ECKey();
                    Address address = new Address(11, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
                    System.out.println("=".repeat(100));
                    System.out.println("address   :" + address.getBase58());
                    System.out.println("privateKey:" + key.getPrivateKeyAsHex());
                    System.out.println("=".repeat(100));
                }
                System.exit(0);
            }
        }
    }

}
