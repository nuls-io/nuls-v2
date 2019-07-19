package io.nuls.poc.model.dto;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * @author Niels
 */
@ApiModel
public class RandomSeedDTO {

    @ApiModelProperty(description = "生成的随机种子")
    private String seed;
    @ApiModelProperty(description = "算法标识")
    private String algorithm;
    @ApiModelProperty(description = "原始种子个数")
    private int count;

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
