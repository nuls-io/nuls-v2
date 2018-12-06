package io.nuls.poc.model.dto.input;

/**
 * 区块验证参数
 * Block validation parameter
 *
 * @author tag
 * 2018/11/12
 * */
public class ValidBlockDTO {
    private int chainId;
    private boolean isDownload;
    private String block;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }
}
