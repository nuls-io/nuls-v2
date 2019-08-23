package io.nuls.api.model.po.db;

public class BlockHexInfo {

    private long height;

    private String blockHex;

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
}
