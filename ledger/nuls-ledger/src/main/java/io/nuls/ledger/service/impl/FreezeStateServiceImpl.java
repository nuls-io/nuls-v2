/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.service.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.model.FreezeHeightState;
import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.thread.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by wangkun23 on 2018/12/4.
 */
@Service
public class FreezeStateServiceImpl implements FreezeStateService {

    final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    AccountStateService accountStateService;

    /**
     * 当收到一个确认区块时处理该逻辑
     *
     * @param blockHeader
     */
    @Override
    public void syncBlock(BlockHeader blockHeader) {
        //TODO.. 重新用一张表结构存储存储高度锁定的数据
        //用一张表存储时间锁定的数据
    }
}
