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

import io.nuls.base.data.Transaction;
import io.nuls.chain.info.ChainTxConstants;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: nuls2.0
 * @description: moduleValidateCmd
 * @author: lan
 * @create: 2018/11/22
 **/
@Component
public class TxModuleValidateCmd extends BaseCmd {
    @Autowired
    private TxAssetCmd txAssetCmd;
    @Autowired
    private TxChainCmd txChainCmd;

    /**
     * chainModuleTxValidate
     * 批量校验
     */
    @CmdAnnotation(cmd = "cm_chainModuleTxValidate", version = 1.0,
            description = "chainModuleTxValidate")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHexs", parameterType = "array")
    public Response chainModuleTxValidate(Map params) {
        try {
            //TODO:
            //1获取交易类型
            //2进入不同验证器里处理
            //3封装失败交易返回
            int chainId = Integer.valueOf(params.get("chainId").toString());
            List<Transaction> registerChainAndAssetList = new ArrayList<>();
            List<Transaction> destroyAssetAndChainList = new ArrayList<>();
            List<Transaction> addAssetToChainList = new ArrayList<>();
            List<Transaction> removeAssetFromChainList = new ArrayList<>();
            for (String txHex : (String[]) params.get("txHexs")) {
                byte[] txBytes = HexUtil.hexToByte(txHex);
                byte[] typeByte = ByteUtils.copyOf(txBytes, 2);
                int type = ByteUtils.bytesToBigInteger(typeByte).intValue();
                Transaction tx = new Transaction(type);
                tx.parse(txBytes, 0);

                switch (type) {
                    case ChainTxConstants.TX_TYPE_REGISTER_CHAIN_AND_ASSET:
                        registerChainAndAssetList.add(tx);
                        break;
                    case ChainTxConstants.TX_TYPE_DESTROY_ASSET_AND_CHAIN:
                        destroyAssetAndChainList.add(tx);
                        break;
                    case ChainTxConstants.TX_TYPE_ADD_ASSET_TO_CHAIN:
                        addAssetToChainList.add(tx);
                        break;
                    case ChainTxConstants.TX_TYPE_REMOVE_ASSET_FROM_CHAIN:
                        removeAssetFromChainList.add(tx);
                        break;
                    default:
                        break;
                }
            }

            /*
            验证注册链
             */

            return success();
        } catch (NulsException e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    private List<Transaction> errorInRegisterChainAndAssetList(List<Transaction> registerChainAndAssetList) {
        List<Transaction> error = new ArrayList<>();
        List<Integer> chainIdList = new ArrayList<>();
        List<Integer> assetIdList = new ArrayList<>();
        for (Transaction tx : registerChainAndAssetList) {
//            ChainTx chainTx=(ChainTx)tx;
        }
        return error;
    }
}
