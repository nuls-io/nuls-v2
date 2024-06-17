package io.nuls.base.api.provider.account.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 13:44
 * @Description: account info
 */
public class AccountInfo {

    /**
     * Account address
     */
    private String address;

    /**
     * alias
     */
    private String alias;

    /**
     * Public keyHex.encode(byte[])
     */
    private String pubkeyHex;


    /**
     * Encrypted private keyHex.encode(byte[])
     */
    private String encryptedPrikeyHex;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountInfo)) {
            return false;
        }

        AccountInfo that = (AccountInfo) o;

        if (address != null ? !address.equals(that.address) : that.address != null) {
            return false;
        }
        if (alias != null ? !alias.equals(that.alias) : that.alias != null) {
            return false;
        }
        if (pubkeyHex != null ? !pubkeyHex.equals(that.pubkeyHex) : that.pubkeyHex != null) {
            return false;
        }
        return encryptedPrikeyHex != null ? encryptedPrikeyHex.equals(that.encryptedPrikeyHex) : that.encryptedPrikeyHex == null;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (pubkeyHex != null ? pubkeyHex.hashCode() : 0);
        result = 31 * result + (encryptedPrikeyHex != null ? encryptedPrikeyHex.hashCode() : 0);
        return result;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPubkeyHex() {
        return pubkeyHex;
    }

    public void setPubkeyHex(String pubkeyHex) {
        this.pubkeyHex = pubkeyHex;
    }

    public String getEncryptedPrikeyHex() {
        return encryptedPrikeyHex;
    }

    public void setEncryptedPrikeyHex(String encryptedPrikeyHex) {
        this.encryptedPrikeyHex = encryptedPrikeyHex;
    }
}
