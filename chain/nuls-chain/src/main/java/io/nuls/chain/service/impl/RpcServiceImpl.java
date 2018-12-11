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

import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.service.RpcService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: nuls2.0
 * @description: 远程接口调用
 * @author: lan
 * @create: 2018/11/20
 **/
@Service
public class RpcServiceImpl  implements RpcService {
    @Override
    public String getCrossChainSeeds() {

        try {
            //TODO:远程接口待完善
//            CmdDispatcher.call(CmConstants.CMD_NW_CROSS_SEEDS, new Object[]{},1.0 );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return "";
    }

    @Override
    public boolean regTx() {
        try {
            //TODO:远程接口待完善
//            CmdDispatcher.call(CmConstants.CMD_TX_REGISTER, new Object[]{},1.0 );

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean newTx(Transaction tx) {

        try {
            //TODO:远程接口待完善
//            CmdDispatcher.call(CmConstants.CMD_TX_NEW_TX, new Object[]{},1.0 );

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean createCrossGroup(BlockChain blockChain) {
        try {
            Map<String,Object> map = new HashMap<>();
            map.put("chainId",blockChain.getChainId());
            map.put("magicNumber",blockChain.getMagicNumber());
            map.put("maxOut","");
            map.put("maxIn","");
            map.put("minAvailableCount",blockChain.getMinAvailableNodeNum());
            map.put("seedIps","");
            map.put("isMoonNode","1");
           Response response =  CmdDispatcher.requestAndResponse(CmConstants.MODULE_ROLE, CmConstants.CMD_NW_CREATE_NODEGROUP,map );
           return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean destroyCrossGroup(BlockChain blockChain) {
        try {
            //TODO:远程接口待完善
//            CmdDispatcher.call(CmConstants.CMD_NW_DELETE_NODEGROUP, new Object[]{},1.0 );

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public AccountBalance getCoinData(int chainId,int assetId,String address) {
        try {
            //TODO:远程接口待完善
//            CmdDispatcher.call(CmConstants.CMD_LG_GET_COINDATA, new Object[]{},1.0 );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

}
