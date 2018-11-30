/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.chain.cmd;

import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.message.Response;

/**
 * @program: nuls2.0
 * @description:
 * @author: lan
 * @create: 2018/11/28
 **/
public class BaseChainCmd extends BaseCmd {
    private static final String BOOLEAN_TRUE = "1";

    boolean isSuccess(Response response){
        if(response.getResponseStatus().equals(BOOLEAN_TRUE)){
            return true;
        }
        return false;
    }
    boolean isMainChain(int chainId){
        return Integer.valueOf(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID)) == chainId;
    }
    boolean isMainAsset(String assetKey){
        String chainId = CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID);
        String assetId = CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_ID);
        return CmRuntimeInfo.getAssetKey(Integer.valueOf(chainId),Integer.valueOf(assetId)).equals(assetKey);
    }
}
