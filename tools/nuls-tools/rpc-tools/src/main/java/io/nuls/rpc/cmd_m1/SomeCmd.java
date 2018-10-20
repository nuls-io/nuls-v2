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

package io.nuls.rpc.cmd_m1;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.CmdInfo;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/17
 * @description
 */
public class SomeCmd extends BaseCmd {

    @CmdInfo(cmd = "cmd1", version = 1.0, preCompatible = true)
    public Object methodName(List params) {
        System.out.println("I'm version 1");
        return "cmd1->1.0";
    }

    @CmdInfo(cmd = "cmd1", version = 1.1, preCompatible = true)
    public Object methodName1(List params) {
        System.out.println("I'm version 1.1");
        return "cmd1->1.1";
    }

    @CmdInfo(cmd = "cmd1", version = 2.0, preCompatible = false)
    public Object cmd111(List params) {
        System.out.println("I'm version 2.0");
        return "cmd1->2.0";
    }

    @CmdInfo(cmd = "cmd1", version = 2.1, preCompatible = true)
    public Object cmd1111(List params) {
        System.out.println("I'm version 2.1");
        return "cmd1->2.1";
    }

    @CmdInfo(cmd = "cmd1", version = 2.2, preCompatible = true)
    public Object cmd11111(List params) {
        System.out.println("I'm version 2.2");
        return "cmd1->2.2";
    }

    @CmdInfo(cmd = "cmd1", version = 3.0, preCompatible = false)
    public Object cmd111111(List params) {
        System.out.println("I'm version 3.0");
        return "cmd1->3.0";
    }

    @CmdInfo(cmd = "cmd2", version = 2.0, preCompatible = true)
    public Object cmd2(List params) {
        System.out.println("I'm version 2");
        return "cmd2->2.0";
    }
}
