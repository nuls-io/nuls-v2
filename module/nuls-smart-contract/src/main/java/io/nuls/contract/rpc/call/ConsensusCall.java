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
package io.nuls.contract.rpc.call;

import io.nuls.contract.rpc.CallHelper;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-02-27
 */
public class ConsensusCall {

    public static String getRandomSeedByCount(int chainId, long endHeight, int count, String algorithm) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("height", endHeight);
        params.put("count", count);
        params.put("algorithm", algorithm);
        Map resultMap = (Map) CallHelper.request(ModuleE.CS.abbr, "cs_random_seed_count", params);
        return (String) resultMap.get("seed");
    }

    public static String getRandomSeedByHeight(int chainId, long startHeight, long endHeight, String algorithm) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("startHeight", startHeight);
        params.put("endHeight", endHeight);
        params.put("algorithm", algorithm);
        Map resultMap = (Map) CallHelper.request(ModuleE.CS.abbr, "cs_random_seed_height", params);
        return (String) resultMap.get("seed");
    }

    public static List<String> getRandomRawSeedsByCount(int chainId, long endHeight, int count) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("height", endHeight);
        params.put("count", count);
        return (List<String>) CallHelper.request(ModuleE.CS.abbr, "cs_random_raw_seeds_count", params);
    }

    public static List<String> getRandomRawSeedsByHeight(int chainId, long startHeight, long endHeight) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("startHeight", startHeight);
        params.put("endHeight", endHeight);
        return (List<String>) CallHelper.request(ModuleE.CS.abbr, "cs_random_raw_seeds_height", params);
    }


}
