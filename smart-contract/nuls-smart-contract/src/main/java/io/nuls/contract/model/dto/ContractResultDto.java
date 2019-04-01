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

import io.nuls.base.basic.AddressTool;
import io.nuls.contract.model.bo.ContractMergedTransfer;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.LongUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.nuls.contract.util.ContractUtil.bigInteger2String;

/**
 * @author: PierreLuo
 */
@Getter
@Setter
public class ContractResultDto {

    private boolean success;

    private String errorMessage;

    private String contractAddress;

    private String result;

    private long gasLimit;

    private long gasUsed;

    private long price;

    private String totalFee;

    private String txSizeFee;

    private String actualContractFee;

    private String refundFee;

    private String stateRoot;

    private String value;

    private String stackTrace;

    //private String balance;

    private List<ContractMergedTransferDto> transfers;

    private List<String> events;

    private List<ContractTokenTransferDto> tokenTransfers;

    private String remark;

    public ContractResultDto() {
    }

    public ContractResultDto(int chainId, ContractResult result, ContractBaseTransaction tx) throws NulsException {
        ContractData contractData = (ContractData) tx.getTxDataObj();
        this.gasLimit = contractData.getGasLimit();
        this.gasUsed = result.getGasUsed();
        this.price = result.getPrice();
        BigInteger totalFee = tx.getFee();
        this.totalFee = bigInteger2String(totalFee);
        BigInteger actualContractFee = BigInteger.valueOf(LongUtils.mul(this.gasUsed, this.price));
        this.actualContractFee = bigInteger2String(actualContractFee);
        BigInteger contractFee = BigInteger.valueOf(LongUtils.mul(gasLimit, price));
        this.refundFee = bigInteger2String(contractFee.subtract(actualContractFee));
        this.txSizeFee = bigInteger2String(totalFee.subtract(contractFee));
        this.value = String.valueOf(result.getValue());
        //this.balance = bigInteger2String(result.getBalance());
        this.contractAddress = AddressTool.getStringAddressByBytes(result.getContractAddress());
        this.result = result.getResult();
        this.stateRoot = (result.getStateRoot() != null ? HexUtil.encode(result.getStateRoot()) : null);
        this.success = result.isSuccess();
        this.errorMessage = result.getErrorMessage();
        this.stackTrace = result.getStackTrace();
        this.setMergedTransfers(result.getMergedTransferList());
        this.events = result.getEvents();
        this.remark = result.getRemark();
        if (result.isSuccess()) {
            this.makeTokenTransfers(chainId, result.getEvents());
        }
    }

    public ContractResultDto(int chainId, ContractResult contractExecuteResult, ContractBaseTransaction tx, ContractTokenTransferInfoPo transferInfoPo) throws NulsException {
        this(chainId, contractExecuteResult, tx);
        if (transferInfoPo != null) {
            this.tokenTransfers = new ArrayList<>();
            this.tokenTransfers.add(new ContractTokenTransferDto(transferInfoPo));
        }
    }

    public List<ContractTokenTransferDto> getTokenTransfers() {
        return tokenTransfers == null ? new ArrayList<>() : tokenTransfers;
    }

    public void setTokenTransfers(List<ContractTokenTransferDto> tokenTransfers) {
        this.tokenTransfers = tokenTransfers;
    }

    private void makeTokenTransfers(int chainId, List<String> tokenTransferEvents) {
        List<ContractTokenTransferDto> result = new ArrayList<>();
        if (tokenTransferEvents != null && tokenTransferEvents.size() > 0) {
            ContractTokenTransferInfoPo po;
            for (String event : tokenTransferEvents) {
                po = ContractUtil.convertJsonToTokenTransferInfoPo(chainId, event);
                if (po != null) {
                    result.add(new ContractTokenTransferDto(po));
                }
            }
        }
        this.tokenTransfers = result;
    }

    public List<ContractMergedTransferDto> getTransfers() {
        return transfers == null ? new ArrayList<>() : transfers;
    }

    public void setMergedTransfers(List<ContractMergedTransfer> transfers) {
        List<ContractMergedTransferDto> list = new LinkedList<>();
        this.transfers = list;
        if (transfers == null || transfers.size() == 0) {
            return;
        }
        for (ContractMergedTransfer transfer : transfers) {
            list.add(new ContractMergedTransferDto(transfer));
        }
    }

}
