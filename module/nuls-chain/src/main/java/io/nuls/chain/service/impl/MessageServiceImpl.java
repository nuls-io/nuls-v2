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
package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.ChainAssetTotalCirculate;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.MessageService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息协议服务
 * Message protocol service implement
 *
 * @author: lan
 * @create: 2018/12/04
 **/
@Component
public class MessageServiceImpl implements MessageService {
    @Autowired
    AssetService assetService ;

    Map<String, Map<Integer, List<ChainAssetTotalCirculate>>> chainAssetMap = new HashMap<>();


    @Override
    public boolean initChainIssuingAssets(int chainId) {
        chainAssetMap.remove(String.valueOf(chainId));
        chainAssetMap.put(String.valueOf(chainId), new HashMap<Integer, List<ChainAssetTotalCirculate>>());
        return true;
    }

    /**
     * 接收链发行资产
     * recieve Chain Issuing Assets
     *
     * @return
     */
    @Override
    public void recChainIssuingAssets(int chainId, List<ChainAssetTotalCirculate> chainAssetTotalCirculates) {
        Map<Integer, List<ChainAssetTotalCirculate>> assetMap = chainAssetMap.get(String.valueOf(chainId));
        if (null != assetMap) {
            for (ChainAssetTotalCirculate chainAssetTotalCirculate : chainAssetTotalCirculates) {
                List<ChainAssetTotalCirculate> list = assetMap.get(chainAssetTotalCirculate.getAssetId());
                if (null == list) {
                    list = new ArrayList<>();
                }
                list.add(chainAssetTotalCirculate);
                assetMap.put(chainAssetTotalCirculate.getAssetId(), list);
            }
        }
    }

    @Override
    public void dealChainIssuingAssets(int chainId) {
        Map<Integer, List<ChainAssetTotalCirculate>> assetMap = chainAssetMap.get(String.valueOf(chainId));
        if (null != assetMap) {
            for (Map.Entry<Integer, List<ChainAssetTotalCirculate>> entry : assetMap.entrySet()) {
                BigInteger totalAmount = BigInteger.ZERO;
                List<ChainAssetTotalCirculate> assetTotalCirculates = entry.getValue();
                for (ChainAssetTotalCirculate chainAssetTotalCirculate : assetTotalCirculates) {
                    totalAmount = totalAmount.add(chainAssetTotalCirculate.getFreeze()).add(chainAssetTotalCirculate.getAvailableAmount());
                }
                if(assetTotalCirculates.size()>0){
                    String key = CmRuntimeInfo.getAssetKey(chainId,entry.getKey());
                    totalAmount = new BigDecimal( totalAmount.toString()).divide( new BigDecimal(assetTotalCirculates.size()),RoundingMode.HALF_DOWN).setScale(0).toBigInteger();
                    try {
                        assetService.saveMsgChainCirculateAmount(key,totalAmount);
                    } catch (Exception e) {
                        LoggerUtil.logger().error(e);
                    }
                }
            }
        }
    }
}
