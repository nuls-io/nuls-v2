/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.core.rpc.cmd;

import io.nuls.core.rpc.model.ConfigItem;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;

/**
 * All parent classes of externally provided interfaces must inheritBaseCmdCan only be called by reflection to
 * The parent class of all externally provided interfaces, must inherit BaseCmd to be invoked by reflection
 *
 * @author tangyi
 * @date 2018/10/15
 */
public abstract class BaseCmd {

    /**
     * Set module configuration parameters
     * Setting Module Configuration Parameters
     *
     * @param key      Key
     * @param value    Value
     * @param readOnly Read only?
     */
    protected void setConfigItem(String key, Object value, boolean readOnly) {
        ConfigItem configItem = new ConfigItem(key, value, readOnly);
        ConnectManager.CONFIG_ITEM_MAP.put(key, configItem);
    }

    /**
     * Return basic success objects
     * Returns the basic success object
     *
     * @return Response
     */
    protected Response success() {
        return success(null);
    }


    /**
     * Return successful objects with specific content
     * Returns a success object with specific content
     *
     * @param responseData Object, can be any values
     * @return Response
     */
    protected Response success(Object responseData) {
        Response response = MessageUtil.newSuccessResponse("", Response.SUCCESS_MSG);
        response.setResponseData(responseData);
        return response;
    }

    /**
     * Return predefined failed objects
     * Returns the predefined failed object
     *
     * @param errorCode ErrorCode
     * @return Response
     */
    protected Response failed(ErrorCode errorCode) {
        return MessageUtil.newFailResponse("", errorCode);
    }

    /**
     * Return the failed object for custom error messages
     * Returns the failed object of the custom error message
     *
     * @param errMsg User defined error message
     * @return Response
     */
    protected Response failed(String errMsg) {
        return failed(CommonCodeConstanst.FAILED,errMsg);
    }

    /**
     * Predefine failed objects with custom error messages
     * Predefined failed object with a custom error message
     *
     * @param errorCode ErrorCode
     * @param errMsg    User defined error message
     * @return Response
     */
    protected Response failed(ErrorCode errorCode, String errMsg) {
        Response response = MessageUtil.newFailResponse("", errMsg);
        response.setResponseErrorCode(errorCode.getCode());
        return response;
    }
}
