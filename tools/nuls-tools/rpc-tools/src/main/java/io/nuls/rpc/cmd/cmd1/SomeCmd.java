/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.rpc.cmd.cmd1;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.cmd.KernelCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;

import java.io.IOException;
import java.util.List;

/**
 * this class is only used by testing.
 *
 * @author tangyi
 * @date 2018/10/17
 * @description
 */
public class SomeCmd extends BaseCmd implements KernelCmd {

    @CmdAnnotation(cmd = "cmd1", version = 1.0, preCompatible = true)
    public CmdResponse methodName(List params) {
        System.out.println("I'm version 1");
        return success(1.0, "cmd1->1.0", null);
    }

    @CmdAnnotation(cmd = "cmd1", version = 1.1, preCompatible = true)
    public CmdResponse methodName1(List params) {
        System.out.println("I'm version 1.1");
        return success(1.1);
    }

    @CmdAnnotation(cmd = "cmd1", version = 2.0, preCompatible = false)
    public CmdResponse cmd111(List params) {
        System.out.println("I'm version 2.0");
        return success(2.0, "cmd1->2.0", null);
    }

    @CmdAnnotation(cmd = "cmd1", version = 2.1, preCompatible = true)
    public CmdResponse cmd1111(List params) {
        System.out.println("I'm version 2.1");
        return success(2.1, "cmd1->2.1", null);
    }

    @CmdAnnotation(cmd = "cmd1", version = 2.2, preCompatible = true)
    public CmdResponse cmd11111(List params) {
        System.out.println("I'm version 2.2");
        return success(2.2, "cmd1->2.2", null);
    }

    @CmdAnnotation(cmd = "cmd1", version = 3.0, preCompatible = false)
    public CmdResponse cmd111111(List params) {
        System.out.println("I'm version 3.0");
        return success(3.0, "cmd1->3.0", null);
    }

    @CmdAnnotation(cmd = "cmd2", version = 2.0, preCompatible = true)
    public CmdResponse cmd2(List params) {
        System.out.println("I'm version 2");
        return success(2.0, "2.0success", new String[]{"hello world cmd2", "inchain best"});
    }

    /**
     * 接收kernel推送的信息
     *
     * @param params：
     * @return Object
     */
    @Override
    @CmdAnnotation(cmd = Constants.STATUS, version = 1.0, preCompatible = true)
    public CmdResponse status(List params) {
        try {
            return super.status(params);
        } catch (IOException e) {
            e.printStackTrace();
            return failed(Constants.PARSE_ERROR, 1.0, null);
        }
    }

    /**
     * 关闭服务：在已有业务完成之后
     *
     * @param params ：
     * @return Object
     */
    @Override
    @CmdAnnotation(cmd = Constants.SHUTDOWN, version = 1.0, preCompatible = true)
    public CmdResponse shutdown(List params) {
        return success(1.0);
    }

    /**
     * 关闭服务：立即关闭，不管业务是否完成
     *
     * @param params ：
     * @return Object
     */
    @Override
    @CmdAnnotation(cmd = Constants.TERMINATE, version = 1.0, preCompatible = true)
    public CmdResponse terminate(List params) {
        return success(1.0);
    }

    /**
     * 提供本地配置信息
     *
     * @param params ：
     * @return Object
     */
    @Override
    @CmdAnnotation(cmd = Constants.CONF_GET, version = 1.0, preCompatible = true)
    public CmdResponse confGet(List params) {
        return success(1.0);
    }

    /**
     * 更新本地配置信息
     *
     * @param params ：
     * @return Object
     */
    @Override
    @CmdAnnotation(cmd = Constants.CONF_SET, version = 1.0, preCompatible = true)
    public CmdResponse confSet(List params) {
        return success(1.0);
    }

    /**
     * 重置本地配置信息
     *
     * @param params ：
     * @return Object
     */
    @Override
    @CmdAnnotation(cmd = Constants.CONF_RESET, version = 1.0, preCompatible = true)
    public CmdResponse confReset(List params) {
        return success(1.0);
    }
}
