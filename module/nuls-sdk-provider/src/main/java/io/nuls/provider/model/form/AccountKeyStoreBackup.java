package io.nuls.provider.model.form;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import lombok.Data;

/**
 * @author: Charlie
 * @date: 2018/10/29
 */
@Data
@ApiModel(description = "备份ketstore表单")
public class AccountKeyStoreBackup {
    @ApiModelProperty(description = "密码", required = true)
    private String password;
    @ApiModelProperty(description = "文件路径")
    private String path;

}
