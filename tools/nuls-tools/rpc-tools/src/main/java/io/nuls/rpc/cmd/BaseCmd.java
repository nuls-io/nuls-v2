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
import io.nuls.rpc.server.ServerRuntime;
import io.nuls.rpc.model.ConfigItem;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;

/**
 * 所有对外提供的接口的父类，必须继承BaseCmd才能被反射调用到
 * The parent class of all externally provided interfaces, must inherit BaseCmd to be invoked by reflection
 *
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
        ServerRuntime.configItemMap.put(key, configItem);
    }

    /**
     * 返回基本的成功对象
     * Returns the basic success object
     */
    protected Response success() {
        return success(null);
    }

    /**
     * 返回有特定内容的成功对象
     * Returns a success object with specific content
     */
    protected Response success(Object responseData) {
        Response response = ServerRuntime.newResponse("", Constants.booleanString(true), "Congratulations! Processing completed！");
        response.setResponseData(responseData);
        return response;
    }

    /**
     * 返回预定义的失败对象
     * Returns the predefined failed object
     */
    protected Response failed(ErrorCode errorCode) {
        Response response = ServerRuntime.newResponse("", Constants.booleanString(false), "");
        response.setResponseData(errorCode);
        return response;
    }

    /**
     * 返回自定义错误消息的失败对象
     * Returns the failed object of the custom error message
     */
    protected Response failed(String errMsg) {
        return ServerRuntime.newResponse("", Constants.booleanString(false), errMsg);
    }

    /**
     * 预定义失败对象，同时带有自定义错误消息
     * Predefined failed object with a custom error message
     */
    protected Response failed(ErrorCode errorCode, String errMsg) {
        Response response = ServerRuntime.newResponse("", Constants.booleanString(false), errMsg);
        response.setResponseData(errorCode);
        return response;
    }
}
