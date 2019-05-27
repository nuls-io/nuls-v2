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
package io.nuls.contract.serialize;

import io.nuls.contract.model.po.ContractOfflineTxHashPo;
import io.nuls.core.crypto.HexUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author: PierreLuo
 * @date: 2019-05-27
 */
public class ContractOfflineTxHashPoTest {

    @Test
    public void test() throws IOException {
        ArrayList<byte[]> hashList = new ArrayList<>();
        hashList.add(HexUtil.decode("e3f57c0c08d9ac5f73523f8f69c340ac80c24588c4968bf4056e97319c14af5f"));
        ContractOfflineTxHashPo po = new ContractOfflineTxHashPo(hashList);
        byte[] serialize = po.serialize();
        System.out.println(HexUtil.encode(serialize));
    }
}
