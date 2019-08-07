package io.nuls.provider.model.form;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/10/29
 */
@ApiModel(description = "备份ketstore表单")
public class AccountKeyStoreBackup {
    @ApiModelProperty(description = "密码", required = true)
    private String password;
    @ApiModelProperty(description = "文件路径")
    private String path;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
