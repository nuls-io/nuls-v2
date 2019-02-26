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

package io.nuls.transaction.rpc.call.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.service.ConfirmedTxService;

import java.util.HashMap;

/**
 * @author: Charlie
 * @date: 2019-01-02
 */
public class EventNewBlockHeightInvoke extends BaseInvoke {

    @Autowired
    private ConfirmedTxService confirmedTxService;

    private Chain chain;

    public EventNewBlockHeightInvoke(Chain chain){
        this.chain = chain;
    }

    @Override
    public void callBack(Response response) {
        try {
            Log.debug("EventNewBlockHeightInvoke 更新最新区块......");
            try {
                Log.debug(JSONUtils.obj2json(response));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            if (response.isSuccess()) {
                HashMap result = ((HashMap) response.getResponseData());
                long blockHeight = (long) result.get("height");
                chain.setBestBlockHeight(blockHeight);
                confirmedTxService.processEffectCrossTx(chain, blockHeight);
            }
        } catch (NulsException e) {
            chain.getLogger().error(e);
        }
    }
}
