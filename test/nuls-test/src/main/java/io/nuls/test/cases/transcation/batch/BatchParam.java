package io.nuls.test.cases.transcation.batch;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-25 11:10
 * @Description: 功能描述
 */
public class BatchParam {

    Boolean reverse = false;

    Long count;

    String formAddressPriKey;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getFormAddressPriKey() {
        return formAddressPriKey;
    }

    public void setFormAddressPriKey(String formAddressPriKey) {
        this.formAddressPriKey = formAddressPriKey;
    }

    public Boolean getReverse() {
        return reverse;
    }

    public void setReverse(Boolean reverse) {
        this.reverse = reverse;
    }
}
