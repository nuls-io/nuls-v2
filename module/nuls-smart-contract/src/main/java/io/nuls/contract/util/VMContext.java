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
import io.nuls.contract.rpc.call.ConsensusCall;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.INITIAL_STATE_ROOT;

/**
 * @Author: PierreLuo
 * @Date: 2018/5/2
 */
@Component
public class VMContext {

    @Autowired
    private ContractHelper contractHelper;

    private static Map<String, ProgramMethod> NRC20_METHODS = null;
    private static Map<String, ProgramMethod> NRC721_METHODS = null;
    private static ProgramMethod NRC721_OVERLOAD_METHOD_SAFE_DATA = null;
    private static ProgramMethod NRC721_OVERLOAD_METHOD_SAFE = null;

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
            header = BlockCall.getBlockHeader(chainId, height);
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
            if (header != null) {
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
        BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeader(chainId);
        if (blockHeader == null) {
            // edit by pierre at 2019-10-24 如果为空，说明是验证合约时，合约虚拟机调用此方法，此时需要手工设置当前打包区块数据，可手工设置的数据有区块高度和区块时间
            BlockHeaderDto header = getNewestBlockHeader(chainId);
            if(header != null) {
                header.setHeight(header.getHeight() + 1);
                header.setTime(header.getTime() + 10);
            }
            return header;
        }
        return new BlockHeaderDto(chainId, blockHeader);
    }

    /**
     * 查询可用余额
     *
     * @param address     合约地址
     * @param blockHeight 区块高度, 如果不传, 则按主链最新高度查询
     */
    public ContractBalance getBalance(int chainId, byte[] address) {
        ContractBalance balance = contractHelper.getBalance(chainId, address);
        return balance;
    }

    /**
     * 查询总余额
     *
     * @param address     合约地址
     * @param blockHeight 区块高度, 如果不传, 则按主链最新高度查询
     */
    public BigInteger getTotalBalance(int chainId, byte[] address) {
        ContractBalance balance = contractHelper.getBalance(chainId, address);
        if (balance != null) {
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

    public static Map<String, ProgramMethod> getNrc721Methods() {
        return NRC721_METHODS;
    }

    public static void setNrc721Methods(Map<String, ProgramMethod> nrc721Methods) {
        NRC721_METHODS = nrc721Methods;
    }

    public static ProgramMethod getNrc721OverloadMethodSafeData() {
        return NRC721_OVERLOAD_METHOD_SAFE_DATA;
    }

    public static void setNrc721OverloadMethodSafeData(ProgramMethod nrc721OverloadMethodSafeData) {
        NRC721_OVERLOAD_METHOD_SAFE_DATA = nrc721OverloadMethodSafeData;
    }

    public static ProgramMethod getNrc721OverloadMethodSafe() {
        return NRC721_OVERLOAD_METHOD_SAFE;
    }

    public static void setNrc721OverloadMethodSafe(ProgramMethod nrc721OverloadMethodSafe) {
        NRC721_OVERLOAD_METHOD_SAFE = nrc721OverloadMethodSafe;
    }

    public long getBestHeight(int chainId) {
        BlockHeader currentBlockHeader = contractHelper.getBatchInfoCurrentBlockHeader(chainId);
        if (currentBlockHeader != null) {
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

    public String getRandomSeed(int chainId, long endHeight, int count, String algorithm) {
        try {
            return ConsensusCall.getRandomSeedByCount(chainId, endHeight, count, algorithm);
        } catch (NulsException e) {
            throw new RuntimeException(e.format());
        }
    }

    public String getRandomSeed(int chainId, long startHeight, long endHeight, String algorithm) {
        try {
            return ConsensusCall.getRandomSeedByHeight(chainId, startHeight, endHeight, algorithm);
        } catch (NulsException e) {
            throw new RuntimeException(e.format());
        }
    }

    public List<String> getRandomSeedList(int chainId, long endHeight, int seedCount) {
        try {
            return ConsensusCall.getRandomRawSeedsByCount(chainId, endHeight, seedCount);
        } catch (NulsException e) {
            throw new RuntimeException(e.format());
        }
    }

    public List<String> getRandomSeedList(int chainId, long startHeight, long endHeight) {
        try {
            return ConsensusCall.getRandomRawSeedsByHeight(chainId, startHeight, endHeight);
        } catch (NulsException e) {
            throw new RuntimeException(e.format());
        }
    }

    public long getCustomMaxViewGasLimit(int chainId) {
        return contractHelper.getChain(chainId).getConfig().getMaxViewGas();
    }
}
