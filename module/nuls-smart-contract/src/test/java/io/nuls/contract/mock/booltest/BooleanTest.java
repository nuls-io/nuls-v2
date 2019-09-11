/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.mock.booltest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.contract.mock.basetest.MockBase;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author: PierreLuo
 * @date: 2019-08-19
 */
public class BooleanTest extends MockBase {

    @Test
    public void testCreate() throws IOException {
        InputStream in = new FileInputStream(this.getClass().getResource("/maven-plugin-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);

        byte[] prevStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        byte[] stateRoot = super.create(prevStateRoot, SENDER, contractCode);

        Log.info("stateRoot: " + HexUtil.encode(stateRoot));
        Log.info("\n");
    }

    @Test
    public void testView() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("d0362bdbab5e467f4ac551ffc7be37bf2006c49b7007f5628a2d36122ee3368d");
        String balanceOf = super.view(prevStateRoot, "m2", new String[]{});
        Log.info("view result: " + balanceOf);
    }

    @Test
    public void testCall() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("d0362bdbab5e467f4ac551ffc7be37bf2006c49b7007f5628a2d36122ee3368d");
        Object[] objects = super.call(prevStateRoot, SENDER, "booltest", new String[]{});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot));
        Log.info("\n");
    }

}
