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
package io.nuls.base;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;

public class RPCUtil {
    public static String encode(byte[] src) {
        return (src == null) ? null :HexUtil.encode(src);
    }

    public static byte[] decode(String src) {
        return (src == null) ? null : HexUtil.decode(src);
    }

    public static <T> T getInstance(byte[] bytes, Class<? extends BaseNulsData> clazz) {
        if (null == bytes || bytes.length == 0) {
            Log.error("error code-" + CommonCodeConstanst.DESERIALIZE_ERROR);
            return null;
        }
        try {
            BaseNulsData baseNulsData = clazz.getDeclaredConstructor().newInstance();
            baseNulsData.parse(new NulsByteBuffer(bytes));
            return (T) baseNulsData;
        } catch (Exception e) {
            Log.error("error code-" + CommonCodeConstanst.DESERIALIZE_ERROR);
            return null;
        }
    }

    /**
     * RPCUtil 反序列化
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getInstanceRpcStr(String data, Class<? extends BaseNulsData> clazz) {
        if (StringUtils.isBlank(data)) {
            Log.error("error code-" + CommonCodeConstanst.DESERIALIZE_ERROR);
            return null;
        }
        return getInstance(decode(data), clazz);
    }
}
