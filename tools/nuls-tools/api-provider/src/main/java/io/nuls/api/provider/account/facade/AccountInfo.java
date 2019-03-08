package io.nuls.api.provider.account.facade;

import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 13:44
 * @Description: account info
 */
@Data
public class AccountInfo {

    /**
     * 账户地址
     */
    private String address;

    /**
     * 别名
     */
    private String alias;

    /**
     * 公钥Hex.encode(byte[])
     */
    private String pubkeyHex;


    /**
     * 已加密私钥Hex.encode(byte[])
     */
    private String encryptedPrikeyHex;

}
