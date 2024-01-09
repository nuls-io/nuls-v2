package io.nuls.provider.model.dto;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * @author Niels
 */
@ApiModel
public class RandomSeedDTO {

    @ApiModelProperty(description = "Generate random seeds")
    private String seed;
    @ApiModelProperty(description = "Algorithm identification")
    private String algorithm;
    @ApiModelProperty(description = "Original number of seeds")
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
