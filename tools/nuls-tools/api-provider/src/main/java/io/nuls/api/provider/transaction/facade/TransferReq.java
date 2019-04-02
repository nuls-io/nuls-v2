package io.nuls.api.provider.transaction.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 16:19
 * @Description: 功能描述
 */
@Data
public class TransferReq extends BaseReq {

    @Data
    @AllArgsConstructor
    public static class Item {

        private Integer assetsChainId;

        private Integer assetsId;

        private String address;

        private String password;

        private BigInteger amount;

        public Item(Integer chainId,Integer assetsId,String address,BigInteger amount){
            this.assetsChainId = chainId;
            this.address = address;
            this.assetsId = assetsId;
            this.amount = amount;
        }

    }


    public static class TransferReqBuilder {

        private List<Item> inputs;

        private List<Item> outputs;

        private String remark;

        private Integer assetsId;

        private Integer chainId;

        public TransferReqBuilder(Integer chainId){
            this.chainId = chainId;
            this.inputs = new ArrayList<>();
            this.outputs = new ArrayList<>();
        }

        public TransferReqBuilder(Integer chainId,Integer assetsId){
            this.chainId = chainId;
            this.assetsId = assetsId;
            this.inputs = new ArrayList<>();
            this.outputs = new ArrayList<>();
        }

        public TransferReq build(){
            TransferReq req = new TransferReq(this.chainId);
            req.setRemark(this.remark);
            if(inputs.isEmpty()){
                throw new IllegalArgumentException("form info can't be empty");
            }
            checkItem(inputs);
            req.setInputs(inputs);
            if(outputs.isEmpty()){
                throw new IllegalArgumentException("to info can't be empty");
            }
            checkItem(outputs);
            req.setOutputs(outputs);
            return req;
        }

        private void checkItem(List<Item> list){

            for (Item item: list) {
                if(item.getAssetsChainId() == null){
                    item.setAssetsChainId(this.chainId);
                }
                if(item.getAssetsId() == null && this.assetsId == null){
                    throw new IllegalArgumentException("assetsId can not be null");
                }
                if(item.getAssetsId() == null){
                    item.setAssetsId(this.assetsId);
                }
            }
        }

        public TransferReqBuilder setRemark(String remark){
            this.remark = remark;
            return this;
        }


        public TransferReqBuilder addForm(Integer chainId,Integer assetsId,String address,String password,BigInteger amount){
            this.inputs.add(new Item(chainId,assetsId,address,password,amount));
            return this;
        }

        public TransferReqBuilder addForm(String address,String password,BigInteger amount){
            this.inputs.add(new Item(chainId,assetsId,address,password,amount));
            return this;
        }

        public TransferReqBuilder addTo(Integer chainId,Integer assetsId,String address,BigInteger amount){
            this.outputs.add(new Item(chainId,assetsId,address,amount));
            return this;
        }

        public TransferReqBuilder addTo(String address,BigInteger amount){
            this.outputs.add(new Item(chainId,assetsId,address,amount));
            return this;
        }

    }


    private List<Item> inputs;

    private List<Item> outputs;

    private String remark;

    public TransferReq(Integer chainId){
        this.setChainId(chainId);
    }

}
