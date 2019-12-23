package io.nuls.api.model.po;

import java.util.List;

public class BlockHexInfo {

    private long height;

    private String blockHex;

    private List<String> contractHashList;

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getBlockHex() {
        return blockHex;
    }

    public void setBlockHex(String blockHex) {
        this.blockHex = blockHex;
    }

    public List<String> getContractHashList() {
        return contractHashList;
    }

    public void setContractHashList(List<String> contractHashList) {
        this.contractHashList = contractHashList;
    }
}
