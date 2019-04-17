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
package io.nuls.crosschain.srorage.imp;

import io.nuls.crosschain.constant.CrossChainConstant;
import io.nuls.crosschain.srorage.LanguageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.model.ByteUtils;

/**
 * 语言存储管理实现类
 * Language Storage Management Implementation Class
 *
 * @author tag
 * 2018/11/8
 */
@Service
public class LanguageServiceImpl implements LanguageService {
    @Override
    public boolean saveLanguage(String language) throws  Exception{
        return RocksDBService.put(CrossChainConstant.DB_NAME_CONSUME_LANGUAGE,CrossChainConstant.DB_NAME_CONSUME_LANGUAGE.getBytes(),language.getBytes());
    }

    @Override
    public String getLanguage() {
        byte[] languageByte = RocksDBService.get(CrossChainConstant.DB_NAME_CONSUME_LANGUAGE,CrossChainConstant.DB_NAME_CONSUME_LANGUAGE.getBytes());
        if (languageByte == null){
            return null;
        }
        return ByteUtils.asString(languageByte);
    }
}
