package io.nuls.base.api.provider.crosschain.facade;

import io.nuls.base.api.provider.BaseReq;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 16:56
 * @Description: 功能描述
 */
public class CreateCrossTxReq extends BaseReq {

    /**
     * remark : 跨链转账交易
     */

    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    private List<Item> listFrom;

    private List<Item> listTo;

    public static class Item {

        /**
         * address : 8CPcA7kaXSHbWb3GHP7bd5hRLFu8RZv57rY9w
         * assetsChainId : 2
         * assetsId : 1
         * amount : 100000000
         * password : nuls123456
         */
        private String address;
        private int assetsChainId;
        private int assetsId;
        private int amount;
        private String password;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getAssetsChainId() {
            return assetsChainId;
        }

        public void setAssetsChainId(int assetsChainId) {
            this.assetsChainId = assetsChainId;
        }

        public int getAssetsId() {
            return assetsId;
        }

        public void setAssetsId(int assetsId) {
            this.assetsId = assetsId;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public List<Item> getListFrom() {
        return listFrom;
    }

    public void setListFrom(List<Item> listFrom) {
        this.listFrom = listFrom;
    }

    public List<Item> getListTo() {
        return listTo;
    }

    public void setListTo(List<Item> listTo) {
        this.listTo = listTo;
    }
}
