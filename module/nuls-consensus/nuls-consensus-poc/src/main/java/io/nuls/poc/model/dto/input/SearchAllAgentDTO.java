package io.nuls.poc.model.dto.input;

/**
 * 查询所有节点参数类
 * Query all node parameter classes
 *
 * @author tag
 * 2018/11/12
 * */
public class SearchAllAgentDTO {
    private int chainId;
    private int pageNumber;
    private int pageSize;
    private String keyWord;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }
}
