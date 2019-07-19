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
package io.nuls.contract.randomseed;

import io.nuls.contract.rpc.call.ConsensusCall;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import org.junit.Test;

import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-07-17
 */
public class RandomSeedCallTest extends BaseQuery {

    @Test
    public void getRandomSeedByCount() throws NulsException {
        //int chainId,
        //long endHeight,
        //int count,
        //String algorithm
        int size = 10;
        for (int i = 0; i < size; i++) {
            System.out.println(ConsensusCall.getRandomSeedByCount(2, 15, 1 + i, "sha3"));
        }
    }

    @Test
    public void getRandomSeedByHeight() throws NulsException {
        //int chainId,
        //long startHeight,
        //long endHeight,
        //String algorithm
        int size = 10;
        for (int i = 0; i < size; i++) {
            System.out.println(ConsensusCall.getRandomSeedByHeight(2, 5, 10 + i, "sha3"));
        }
    }

    @Test
    public void getRandomRawSeedsByCount() throws Exception {
        //int chainId,
        //long endHeight,
        //int count
        List<String> seeds = ConsensusCall.getRandomRawSeedsByCount(2, 100, 10);
        seeds.forEach(s -> System.out.println(s));
    }

    @Test
    public void getRandomRawSeedsByHeight() throws Exception {
        //int chainId,
        //long startHeight,
        //long endHeight
        List<String> seeds = ConsensusCall.getRandomRawSeedsByHeight(2, 0, 100);
        seeds.forEach(s -> System.out.println(s));
    }
}
