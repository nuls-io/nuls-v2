/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.contract.model.dto;


import io.nuls.base.RPCUtil;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.ContractTransferData;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.util.MapUtil;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.Key;
import io.nuls.core.rpc.model.TypeDescriptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.util.ContractUtil.bigInteger2String;
import static io.nuls.core.constant.TxType.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 */
@ApiModel
public class ContractTransactionDto {

    @ApiModelProperty(description = "transactionhash")
    private String hash;
    @ApiModelProperty(description = "Transaction type")
    private Integer type;
    @ApiModelProperty(description = "Transaction time")
    private Long time;
    @ApiModelProperty(description = "block height")
    private Long blockHeight;
    @ApiModelProperty(description = "Transaction fees")
    private String fee;
    @ApiModelProperty(description = "Transaction amount")
    private String value;
    @ApiModelProperty(description = "Remarks")
    private String remark;
    @ApiModelProperty(description = "Signature information")
    private String scriptSig;
    @ApiModelProperty(description = "Transaction status（0 - Confirming,1 - Confirmed）")
    private Integer status;
    @ApiModelProperty(description = "Number of transaction confirmations")
    private Long confirmCount;
    @ApiModelProperty(description = "Transaction size")
    private int size;
    @ApiModelProperty(description = "Transaction Input Set", type = @TypeDescriptor(value = List.class, collectionElement = InputDto.class))
    private List<InputDto> inputs;
    @ApiModelProperty(description = "Transaction output set", type = @TypeDescriptor(value = List.class, collectionElement = OutputDto.class))
    private List<OutputDto> outputs;
    @ApiModelProperty(description = "Contract trading business data", type = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "data", description = "Reflect different business data based on contract transaction types（Here, in order to describe four situations, the four businesses are described together, but in reality, they do not exist simultaneously, only one exists）", valueType = ContractTransactionTxDataDescriptor.class)
    }))
    private Map<String, Object> txData;
    @ApiModelProperty(description = "Contract execution results")
    private ContractResultDto contractResult;

    public ContractTransactionDto(int chainId, ContractBaseTransaction tx) throws NulsException {
        this.hash = tx.getHash().toHex();
        this.type = tx.getType();
        this.time = tx.getTime();
        this.blockHeight = tx.getBlockHeight();
        this.fee = bigInteger2String(tx.getCoinDataObj().getFeeByAsset(chainId, 1));
        this.size = tx.getSize();
        this.txData = makeTxData(tx);
        long bestBlockHeight = BlockCall.getLatestHeight(chainId);
        if (this.blockHeight > 0 || TxStatusEnum.CONFIRMED.equals(tx.getStatus())) {
            this.confirmCount = bestBlockHeight - this.blockHeight;
        } else {
            this.confirmCount = 0L;
        }
        if (TxStatusEnum.CONFIRMED.equals(tx.getStatus())) {
            this.status = 1;
        } else {
            this.status = 0;
        }

        if (tx.getRemark() != null) {
            this.setRemark(new String(tx.getRemark(), StandardCharsets.UTF_8));
        }
        if (tx.getTransactionSignature() != null) {
            this.setScriptSig(RPCUtil.encode(tx.getTransactionSignature()));
        }

        CoinData coinData = tx.getCoinDataObj();
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        if (coinData != null) {
            List<CoinFrom> froms = coinData.getFrom();
            for (CoinFrom from : froms) {
                inputs.add(new InputDto(from));
            }
            List<CoinTo> tos = coinData.getTo();
            for (CoinTo to : tos) {
                outputs.add(new OutputDto(to));
            }
        }
    }

    private Map<String, Object> makeTxData(ContractBaseTransaction tx) throws NulsException {
        Map<String, Object> result = new HashMap<>();
        ContractData txData = (ContractData) tx.getTxDataObj();
        if (type == CREATE_CONTRACT) {
            result.put("data", new CreateContractDataDto(txData));
        } else if (type == CALL_CONTRACT) {
            result.put("data", new CallContractDataDto(txData));
        } else if (type == DELETE_CONTRACT) {
            result.put("data", new DeleteContractDataDto(txData));
        } else if (type == CONTRACT_TRANSFER) {
            result.put("data", new ContractTransferDataDto((ContractTransferData) txData));
        }
        return result;
    }

    @ApiModel
    private static class ContractTransactionTxDataDescriptor {
        @ApiModelProperty(description = "Publish business data for contract transactions")
        private CreateContractDataDto create;
        @ApiModelProperty(description = "Call business data for contract transactions")
        private CallContractDataDto call;
        @ApiModelProperty(description = "Delete business data for contract transactions")
        private DeleteContractDataDto delete;
        @ApiModelProperty(description = "Business data for contract transfer transactions")
        private ContractTransferDataDto transfer;

        public CreateContractDataDto getCreate() {
            return create;
        }

        public void setCreate(CreateContractDataDto create) {
            this.create = create;
        }

        public CallContractDataDto getCall() {
            return call;
        }

        public void setCall(CallContractDataDto call) {
            this.call = call;
        }

        public DeleteContractDataDto getDelete() {
            return delete;
        }

        public void setDelete(DeleteContractDataDto delete) {
            this.delete = delete;
        }

        public ContractTransferDataDto getTransfer() {
            return transfer;
        }

        public void setTransfer(ContractTransferDataDto transfer) {
            this.transfer = transfer;
        }
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(String scriptSig) {
        this.scriptSig = scriptSig;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getConfirmCount() {
        return confirmCount;
    }

    public void setConfirmCount(Long confirmCount) {
        this.confirmCount = confirmCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<InputDto> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputDto> inputs) {
        this.inputs = inputs;
    }

    public List<OutputDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<OutputDto> outputs) {
        this.outputs = outputs;
    }

    public Map<String, Object> getTxData() {
        return txData;
    }

    public void setTxData(Map<String, Object> txData) {
        this.txData = txData;
    }

    public ContractResultDto getContractResult() {
        return contractResult;
    }

    public void setContractResult(ContractResultDto contractResult) {
        this.contractResult = contractResult;
    }
}
