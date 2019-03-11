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
package io.nuls.contract.util;


import io.nuls.base.data.BlockHeader;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.dto.BlockHeaderDto;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

/**
 * @Author: PierreLuo
 * @Date: 2018/5/2
 */
@Component
public class VMContext {

    @Autowired
    private ContractHelper contractHelper;

    public static Map<String, ProgramMethod> NRC20_METHODS = null;

    /**
     * @param hash
     * @return
     * @throws NulsException
     * @throws IOException
     */
    public BlockHeaderDto getBlockHeader(int chainId, String hash) {
        if (chainId < 0 || StringUtils.isBlank(hash)) {
            Log.warn("parameter error.");
            return null;
        }
        BlockHeader header;
        try {
            header = BlockCall.getBlockHeader(chainId, hash);
            if (header == null) {
                return null;
            }
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }

        BlockHeaderDto resultHeader = new BlockHeaderDto(chainId, header);
        return resultHeader;
    }

    /**
     * @param height
     * @return
     * @throws NulsException
     * @throws IOException
     */
    public BlockHeaderDto getBlockHeader(int chainId, long height) {
        if (chainId < 0 || height < 0L) {
            Log.warn("parameter error.");
            return null;
        }
        BlockHeader header;
        try {
            header = BlockCall.getBlockHeader(chainId, chainId);
            if (header == null) {
                return null;
            }
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }

        BlockHeaderDto resultHeader = new BlockHeaderDto(chainId, header);
        return resultHeader;
    }

    /**
     * get the newest block header
     *
     * @return
     * @throws IOException
     */
    public BlockHeaderDto getNewestBlockHeader(int chainId) {
        try {
            BlockHeader header = BlockCall.getLatestBlockHeader(chainId);
            if(header != null) {
                return new BlockHeaderDto(chainId, header);
            }
        } catch (NulsException e) {
            Log.error(e);
        }
        return null;
    }

    /**
     * get the current block header
     *
     * @return
     * @throws IOException
     */
    public BlockHeaderDto getCurrentBlockHeader(int chainId) {
        BlockHeader blockHeader = contractHelper.getCurrentBlockHeader(chainId);
        if (blockHeader == null) {
            return getNewestBlockHeader(chainId);
        }
        return new BlockHeaderDto(chainId, blockHeader);
    }

    /**
     * 查询可用余额
     *
     * @param address     合约地址
     * @param blockHeight 区块高度, 如果不传, 则按主链最新高度查询
     */
    public BigInteger getBalance(int chainId, byte[] address) {
        ContractBalance balance = contractHelper.getBalance(chainId, address);
        if(balance != null) {
            return balance.getBalance();
        }
        return BigInteger.ZERO;
    }

    /**
     * 查询总余额
     *
     * @param address     合约地址
     * @param blockHeight 区块高度, 如果不传, 则按主链最新高度查询
     */
    public BigInteger getTotalBalance(int chainId, byte[] address) {
        ContractBalance balance = contractHelper.getBalance(chainId, address);
        if(balance != null) {
            return balance.getTotal();
        }
        return BigInteger.ZERO;
    }

    public static Map<String, ProgramMethod> getNrc20Methods() {
        return NRC20_METHODS;
    }

    public static void setNrc20Methods(Map<String, ProgramMethod> nrc20Methods) {
        NRC20_METHODS = nrc20Methods;
    }

    public long getBestHeight(int chainId) {
        BlockHeader currentBlockHeader = contractHelper.getCurrentBlockHeader(chainId);
        if(currentBlockHeader != null) {
            return currentBlockHeader.getHeight() - 1;
        } else {
            try {
                return BlockCall.getLatestHeight(chainId);
            } catch (NulsException e) {
                Log.error(e);
                return -1L;
            }
        }
    }

    public long getCustomMaxViewGasLimit() {
        //TODO pierre auto-generated method stub
        return 0;
    }
}
