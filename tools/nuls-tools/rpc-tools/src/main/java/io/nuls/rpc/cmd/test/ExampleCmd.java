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

package io.nuls.rpc.cmd.test;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;

import java.util.Map;

/**
 * this class is only used by testing.
 *
 * @author tangyi
 * @date 2018/10/17
 * @description
 */
public class ExampleCmd extends BaseCmd {

    @CmdAnnotation(cmd = "getBalance", version = 1.1,
            description = "test getHeight 1.1")
    public Response getHeight1(Map map) {
        Log.info("getHeight version 1.1");
        return success("Here is your real return value");
    }

    @CmdAnnotation(cmd = "getHeight", version = 1.3, scope = Constants.PUBLIC, minEvent = 1, minPeriod = 10,
            description = "test getHeight 1.3")
    @Parameter(parameterName = "aaa", parameterType = "int", parameterValidRange = "(1,100]")
    public Response getHeight2(Map map) {
        Log.info("getHeight version 1.3");
        return success("getHeight->1.3");
    }

    @CmdAnnotation(cmd = "getHeight", version = 2.0, scope = Constants.ADMIN,
            description= "test getHeight 2.0")
    @Parameter(parameterName = "bbb", parameterType = "string", parameterValidRegExp = "^[A-Za-z0-9\\-]+$")
    public Response getHeight3(Map map) {
        Log.info("getHeight version 2.0");
        return success("getHeight->2.0");
    }

    @CmdAnnotation(cmd = "getBalance", version = 1.0, scope = Constants.ADMIN,
            description= "test getBalance")
    public Response getBalance(Map map) {
        Log.info("getBalance invoked");
        return success("getBalance->ha-ha-ha");
    }
}
