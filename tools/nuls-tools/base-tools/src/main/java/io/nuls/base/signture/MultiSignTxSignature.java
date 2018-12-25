/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.base.signture;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 多签账户签名
 * @author: Charlie
 * @date: 2018-12-12
 */
public class MultiSignTxSignature extends TransactionSignature {

    private byte m;
    private List<byte[]> pubKeyList;


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        stream.write(m);
        for (int i = 0; i < pubKeyList.size(); i++) {
            stream.writeBytesWithLength(pubKeyList.get(i));
        }


    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        super.parse(byteBuffer);
        this.m = byteBuffer.readByte();
        long count = byteBuffer.readUint32();
        for (int i = 0; i < count; i++) {
            pubKeyList.add(byteBuffer.readByLengthByte());
        }
    }

    @Override
    public int size() {
        int size = super.size();
        size += 1;
        for (int i = 0; i < pubKeyList.size(); i++) {
            size += SerializeUtils.sizeOfBytes(pubKeyList.get(i));
        }
        return size;
    }


    public void addPubkey(byte[] pubkey) {
        if(null == this.pubKeyList){
            this.pubKeyList = new ArrayList<>();
        }
        this.pubKeyList.add(pubkey);
    }

    /**
     * 签名验证
     * */
    public List<P2PHKSignature> getValidSignature(){
        if(m <= 0 || pubKeyList == null || pubKeyList.size() == 0 || m>pubKeyList.size()){
            return null;
        }
        List<P2PHKSignature> validSignatures = new ArrayList<>();
        List<String> pubKeyStrList = ByteUtils.bytesToStrings(pubKeyList);
        for (P2PHKSignature p2PHKSignature:p2PHKSignatures) {
            if(pubKeyStrList.contains(ByteUtils.asString(p2PHKSignature.getPublicKey()))){
                validSignatures.add(p2PHKSignature);
            }
        }
        if(validSignatures.size()<m || validSignatures.size() > pubKeyList.size()){
            return null;
        }
        return validSignatures;
    }

    public byte getM() {
        return m;
    }

    public void setM(byte m) {
        this.m = m;
    }

    public List<byte[]> getPubKeyList() {
        return pubKeyList;
    }

    public void setPubKeyList(List<byte[]> pubKeyList) {
        this.pubKeyList = pubKeyList;
    }
}
