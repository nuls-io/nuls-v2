package io.nuls.contract.util;

import io.nuls.base.basic.AddressTool;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.dto.BlockHeaderDto;

import java.math.BigInteger;
import java.util.List;

public class VMContextMock extends VMContext {

    @Override
    public BlockHeaderDto getBlockHeader(int chainId, String hash) {
        return newDto(chainId);
    }

    protected BlockHeaderDto newDto(int chainId) {
        BlockHeaderDto dto = new BlockHeaderDto();
        dto.setHash("00204ed6f9ea133cf5e40edc6c9e9a6a69a4e5e0045bba008b6f157c4765f3b87ce4");
        dto.setPreHash("002079d03c0ae201f3d56714f3df6d27e7015c143dfea019f02830d9e651c8de460e");
        dto.setHeight(chainId);
        dto.setTime(1552988615800L);
        dto.setPackingAddress(AddressTool.getAddress("tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL"));
        return dto;
    }

    @Override
    public BlockHeaderDto getBlockHeader(int chainId, long height) {
        return newDto(chainId);
    }

    @Override
    public BlockHeaderDto getNewestBlockHeader(int chainId) {
        return newDto(chainId);
    }

    @Override
    public BlockHeaderDto getCurrentBlockHeader(int chainId) {
        return newDto(chainId);
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }

    @Override
    public ContractBalance getBalance(int chainId, byte[] address) {
        return ContractBalance.newInstance();
    }

    @Override
    public BigInteger getTotalBalance(int chainId, byte[] address) {
        return BigInteger.valueOf(chainId);
    }

    @Override
    public long getBestHeight(int chainId) {
        return chainId;
    }

    @Override
    public String getRandomSeed(int chainId, long endHeight, int count, String algorithm) {
        return super.getRandomSeed(chainId, endHeight, count, algorithm);
    }

    @Override
    public String getRandomSeed(int chainId, long startHeight, long endHeight, String algorithm) {
        return super.getRandomSeed(chainId, startHeight, endHeight, algorithm);
    }

    @Override
    public List<String> getRandomSeedList(int chainId, long endHeight, int seedCount) {
        return super.getRandomSeedList(chainId, endHeight, seedCount);
    }

    @Override
    public List<String> getRandomSeedList(int chainId, long startHeight, long endHeight) {
        return super.getRandomSeedList(chainId, startHeight, endHeight);
    }

    @Override
    public long getCustomMaxViewGasLimit(int chainId) {
        return chainId;
    }
}