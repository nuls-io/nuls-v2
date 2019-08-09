/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.base.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author
 */
public class CoinData extends BaseNulsData {

    private List<CoinFrom> from;

    private List<CoinTo> to;

    public CoinData() {
        from = new ArrayList<>();
        to = new ArrayList<>();
    }

    public CoinData(byte[] bytes) throws NulsException {
        from = new ArrayList<>();
        to = new ArrayList<>();
        this.parse(new NulsByteBuffer(bytes));
    }


    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        int fromCount = from == null ? 0 : from.size();
        stream.writeVarInt(fromCount);
        if (null != from) {
            for (CoinFrom coin : from) {
                stream.writeNulsData(coin);
            }
        }
        int toCount = to == null ? 0 : to.size();
        stream.writeVarInt(toCount);
        if (null != to) {
            for (CoinTo coin : to) {
                stream.writeNulsData(coin);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int fromCount = (int) byteBuffer.readVarInt();

        if (0 < fromCount) {
            List<CoinFrom> from = new ArrayList<>();
            for (int i = 0; i < fromCount; i++) {
                from.add(byteBuffer.readNulsData(new CoinFrom()));
            }
            this.from = from;
        }

        int toCount = (int) byteBuffer.readVarInt();

        if (0 < toCount) {
            List<CoinTo> to = new ArrayList<>();
            for (int i = 0; i < toCount; i++) {
                to.add(byteBuffer.readNulsData(new CoinTo()));
            }
            this.to = to;
        }
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfVarInt(from == null ? 0 : from.size());
        if (null != from) {
            for (CoinFrom coin : from) {
                size += SerializeUtils.sizeOfNulsData(coin);
            }
        }
        size += SerializeUtils.sizeOfVarInt(to == null ? 0 : to.size());
        if (null != to) {
            for (CoinTo coin : to) {
                size += SerializeUtils.sizeOfNulsData(coin);
            }
        }
        return size;
    }

    public List<CoinFrom> getFrom() {
        return from;
    }

    public void setFrom(List<CoinFrom> from) {
        this.from = from;
    }

    public List<CoinTo> getTo() {
        return to;
    }

    public void setTo(List<CoinTo> to) {
        this.to = to;
    }

    public void addTo(CoinTo coinTo) {
        if (null == to) {
            to = new ArrayList<>();
        }
        to.add(coinTo);
    }

    public void addFrom(CoinFrom coinFrom) {
        if (null == from) {
            from = new ArrayList<>();
        }
        from.add(coinFrom);
    }

    /**
     * 从CoinData中获取和交易相关的地址(缺少txData中相关地址，需要对应的交易单独获取)
     *
     * @return
     */
    @JsonIgnore
    public Set<byte[]> getAddresses() {

        Set<ByteArrayWrapper> addressSetWrapper = new HashSet<>();
        if (to != null && to.size() != 0) {
            for (int i = 0; i < to.size(); i++) {
                byte[] address = to.get(i).getAddress();
                ByteArrayWrapper baw = new ByteArrayWrapper(address);
                addressSetWrapper.add(baw);
            }
        }
        if (from != null && from.size() != 0) {
            for (int i = 0; i < from.size(); i++) {
                byte[] address = from.get(i).getAddress();
                ByteArrayWrapper baw = new ByteArrayWrapper(address);
                addressSetWrapper.add(baw);
            }
        }
        Set<byte[]> addressSet = new HashSet<>();
        Iterator<ByteArrayWrapper> it = addressSetWrapper.iterator();
        while (it.hasNext()) {
            addressSet.add(it.next().getBytes());
        }
        return addressSet;
    }

    public Set<String> getFromAddressList(){
        Set<String> fromAddressList = new HashSet<>();
        if(from != null && !from.isEmpty()){
            for (CoinFrom coinFrom:from) {
                fromAddressList.add(AddressTool.getStringAddressByBytes(coinFrom.getAddress()));
            }
        }
        return fromAddressList;
    }

    public int getFromAddressCount(){
        Set<String> addressSet = new HashSet<>();
        for (CoinFrom coinFrom:from) {
            addressSet.add(AddressTool.getStringAddressByBytes(coinFrom.getAddress()));
        }
        return addressSet.size();
    }

    /**
     * 计算指定资产手续费
     * @param assetChainId     指定资产链ID
     * @param assetId          指定资产ID
     * @return                 手续费大小
     * */
    public BigInteger getFeeByAsset(int assetChainId, int assetId){
        BigInteger fromAmount = BigInteger.ZERO;
        BigInteger toAmount = BigInteger.ZERO;
        for (CoinFrom coinFrom : from) {
            if (coinFrom.getAssetsChainId() == assetChainId && coinFrom.getAssetsId() == assetId) {
                fromAmount = fromAmount.add(coinFrom.getAmount());
            }
        }
        for (CoinTo coinTo : to) {
            if (coinTo.getAssetsChainId() == assetChainId && coinTo.getAssetsId() == assetId) {
                toAmount = toAmount.add(coinTo.getAmount());
            }
        }
        return fromAmount.subtract(toAmount);
    }
}