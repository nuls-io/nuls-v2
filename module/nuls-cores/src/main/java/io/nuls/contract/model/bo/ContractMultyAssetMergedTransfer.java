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
package io.nuls.contract.model.bo;

import io.nuls.base.data.NulsHash;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: PierreLuo
 */
public class ContractMultyAssetMergedTransfer {

    private byte[] from;
    private int assetChainId;
    private int assetId;
    private BigInteger value;
    private List<MultyAssetOutput> outputs;

    /**
     * Smart contract tradinghash
     */
    private NulsHash orginHash;

    /**
     * Contract transfer(Transfer out from contract)transactionhash
     */
    private NulsHash hash;

    public ContractMultyAssetMergedTransfer() {
        outputs = new ArrayList<>();
    }

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }

    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public List<MultyAssetOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<MultyAssetOutput> outputs) {
        this.outputs = outputs;
    }

    public NulsHash getOrginHash() {
        return orginHash;
    }

    public void setOrginHash(NulsHash orginHash) {
        this.orginHash = orginHash;
    }

    public NulsHash getHash() {
        return hash;
    }

    public void setHash(NulsHash hash) {
        this.hash = hash;
    }
}
