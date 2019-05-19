package io.nuls.base.api.provider.crosschain.facade;

import io.nuls.base.api.provider.BaseReq;

import java.math.BigInteger;
import java.util.ArrayList;
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

    private List<Item> listFrom = new ArrayList<>();

    private List<Item> listTo = new ArrayList<>();

    private CreateCrossTxReq(Integer chainId){
        this.setChainId(chainId);
    }

    public static class Item {

        /**
         * address : 8CPcA7kaXSHbWb3GHP7bd5hRLFu8RZv57rY9w
         * assetsChainId : 2
         * assetsId : 1
         * amount : 100000000
         * password : nuls123456
         */
        private String address;
        private Integer assetsChainId;
        private Integer assetsId;
        private BigInteger amount;
        private String password;

        public Item(String address,Integer assetsChainId,Integer assetsId,BigInteger amount,String password){
            this.address = address;
            this.amount = amount;
            this.assetsChainId = assetsChainId;
            this.assetsId = assetsId;
            this.password = password;
        }

        public Item(String address,Integer assetsChainId,Integer assetsId,BigInteger amount){
            this.address = address;
            this.amount = amount;
            this.assetsChainId = assetsChainId;
            this.assetsId = assetsId;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getAssetsChainId() {
            return assetsChainId;
        }

        public void setAssetsChainId(Integer assetsChainId) {
            this.assetsChainId = assetsChainId;
        }

        public Integer getAssetsId() {
            return assetsId;
        }

        public void setAssetsId(Integer assetsId) {
            this.assetsId = assetsId;
        }

        public BigInteger getAmount() {
            return amount;
        }

        public void setAmount(BigInteger amount) {
            this.amount = amount;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }


    public static class CreateCrossTxReqBuilder {

        private List<CreateCrossTxReq.Item> listForm;

        private List<CreateCrossTxReq.Item> listTo;

        private String remark;

        private Integer chainId;

        public CreateCrossTxReqBuilder(Integer chainId) {
            this.chainId = chainId;
            this.listForm = new ArrayList<>();
            this.listTo = new ArrayList<>();
        }

        public CreateCrossTxReq build() {
            CreateCrossTxReq req = new CreateCrossTxReq(this.chainId);
            req.setRemark(this.remark);
            if (listForm.isEmpty()) {
                throw new IllegalArgumentException("form info can't be empty");
            }
            checkItem(listForm);
            req.setListFrom(listForm);
            if (listTo.isEmpty()) {
                throw new IllegalArgumentException("to info can't be empty");
            }
            checkItem(listTo);
            req.setListTo(listTo);
            return req;
        }

        private void checkItem(List<CreateCrossTxReq.Item> list) {

            for (CreateCrossTxReq.Item item : list) {
                if (item.getAssetsChainId() == null) {
                    throw new IllegalArgumentException("assetsChainId can not be null");
                }
                if (item.getAssetsId() == null) {
                    throw new IllegalArgumentException("assetsId can not be null");
                }
            }
        }

        public CreateCrossTxReq.CreateCrossTxReqBuilder setRemark(String remark) {
            this.remark = remark;
            return this;
        }


        public CreateCrossTxReq.CreateCrossTxReqBuilder addForm(Integer assetChainId, Integer assetId, String address, String password, BigInteger amount) {
            this.listForm.add(new CreateCrossTxReq.Item(address,assetChainId, assetId, amount, password));
            return this;
        }

        public CreateCrossTxReq.CreateCrossTxReqBuilder addTo(Integer assetChainId, Integer assetId, String address, BigInteger amount) {
            this.listTo.add(new CreateCrossTxReq.Item(address,assetChainId, assetId, amount));
            return this;
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
