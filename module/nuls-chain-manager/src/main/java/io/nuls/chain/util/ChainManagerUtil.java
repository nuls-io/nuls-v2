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
package io.nuls.chain.util;

import io.nuls.chain.model.po.BlockChain;

import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/05/20
 **/
public class ChainManagerUtil {
    public static void putChainMap(BlockChain blockChain, Map<String, Integer> chainMap) {
        chainMap.put("cId-" + String.valueOf(blockChain.getChainId()), 1);
        chainMap.put("magic-" + String.valueOf(blockChain.getMagicNumber()), 1);
        chainMap.put("cName-" + String.valueOf(blockChain.getChainName()), 1);
    }

    public static boolean duplicateChainId(BlockChain blockChain, Map<String, Integer> chainMap) {
        return null != chainMap.get("cId-" + String.valueOf(blockChain.getChainId()));
    }

    public static boolean duplicateMagicNumber(BlockChain blockChain, Map<String, Integer> chainMap) {
        return null != chainMap.get("magic-" + String.valueOf(blockChain.getMagicNumber()));
    }

    public static boolean duplicateChainName(BlockChain blockChain, Map<String, Integer> chainMap) {
        return null != chainMap.get("cName-" + String.valueOf(blockChain.getChainName()));
    }
}
