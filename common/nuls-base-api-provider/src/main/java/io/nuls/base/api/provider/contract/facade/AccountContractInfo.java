package io.nuls.base.api.provider.contract.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 16:57
 * @Description: 功能描述
 */
public class AccountContractInfo {

    private String contractAddress;

    private boolean isCreate;

    private String createTime;

    private long height;

    private long confirmCount;

    private String remarkName;

    private int status;

    private String msg;

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"contractAddress\":\"")
                .append(contractAddress).append('\"')
                .append(",\"isCreate\":")
                .append(isCreate)
                .append(",\"createTime\":\"")
                .append(createTime).append('\"')
                .append(",\"height\":")
                .append(height)
                .append(",\"confirmCount\":")
                .append(confirmCount)
                .append(",\"remarkName\":\"")
                .append(remarkName).append('\"')
                .append(",\"status\":")
                .append(status)
                .append(",\"msg\":\"")
                .append(msg).append('\"')
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountContractInfo)) return false;

        AccountContractInfo that = (AccountContractInfo) o;

        if (isCreate != that.isCreate) return false;
        if (height != that.height) return false;
        if (confirmCount != that.confirmCount) return false;
        if (status != that.status) return false;
        if (contractAddress != null ? !contractAddress.equals(that.contractAddress) : that.contractAddress != null)
            return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (remarkName != null ? !remarkName.equals(that.remarkName) : that.remarkName != null) return false;
        return msg != null ? msg.equals(that.msg) : that.msg == null;
    }

    @Override
    public int hashCode() {
        int result = contractAddress != null ? contractAddress.hashCode() : 0;
        result = 31 * result + (isCreate ? 1 : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (int) (height ^ (height >>> 32));
        result = 31 * result + (int) (confirmCount ^ (confirmCount >>> 32));
        result = 31 * result + (remarkName != null ? remarkName.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        return result;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public void setCreate(boolean create) {
        isCreate = create;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getConfirmCount() {
        return confirmCount;
    }

    public void setConfirmCount(long confirmCount) {
        this.confirmCount = confirmCount;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
