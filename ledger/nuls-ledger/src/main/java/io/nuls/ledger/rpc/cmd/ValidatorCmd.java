/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.rpc.cmd;

import io.nuls.base.data.Transaction;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.validator.CoinDataValidator;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/22.
 */
@Component
public class ValidatorCmd extends BaseCmd {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    CoinDataValidator coinDataValidator;
    /**
     * validate coin data
     *进行nonce-hash校验，进行可用余额校验
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "validateCoinData",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "isBatchValidate", parameterType = "boolean")
    public Response validateCoinData(Map params) {
        //TODO.. 验证参数个数和格式
        Integer chainId = (Integer) params.get("chainId");
        String txHex = (String) params.get("txHex");
        boolean isBatchValidate = Boolean.valueOf(params.get("isBatchValidate").toString());
        Transaction tx = new Transaction();
        try {
            tx.parse(HexUtil.hexToByte(txHex),0);
            if(isBatchValidate){
                ValidateResult validateResult = coinDataValidator.bathValidatePerTx(chainId, tx);
                return success(validateResult);
            }else {
                ValidateResult validateResult = coinDataValidator.validateCoinData(chainId, tx);
                return success(validateResult);
            }

        } catch (NulsException e) {
            e.printStackTrace();
            return failed(e.getErrorCode());
        }

    }
    /**
     * bathValidateBegin
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "bathValidateBegin",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response bathValidateBegin(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        coinDataValidator.beginBatchPerTxValidate(chainId);
        Map<String,Object> rtData = new HashMap<>();
        rtData.put("value",1);
        return success(rtData);
    }
}
