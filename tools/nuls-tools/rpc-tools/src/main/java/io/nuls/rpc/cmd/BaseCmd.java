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

package io.nuls.rpc.cmd;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.ConfigItem;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;

/**
 * @author tangyi
 * @date 2018/10/15
 * @description
 */
public abstract class BaseCmd {

    /**
     * set module configuration
     */
    protected void setConfigItem(String key, Object value, boolean readOnly) {
        ConfigItem configItem = new ConfigItem(key, value, readOnly);
        RuntimeInfo.configItemMap.put(key, configItem);
    }

    protected Response success(Object responseData) {
        Response response = new Response();
        response.setResponseStatus(Constants.RESPONSE_STATUS_SUCCESS);
        response.setResponseData(responseData);
        response.setResponseComment("Congratulations! Processing completed！");
        return response;
    }

    protected Response failed(ErrorCode errorCode) {
        Response response = new Response();
        response.setResponseStatus(Constants.RESPONSE_STATUS_FAILED);
        response.setResponseComment(errorCode.getCode() + ":" + errorCode.getMsg());
        return response;
    }

    protected Response failed(String errMsg) {
        Response response = new Response();
        response.setResponseStatus(Constants.RESPONSE_STATUS_FAILED);
        response.setResponseComment(errMsg);
        return response;
    }
}
