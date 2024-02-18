package io.nuls.provider.model.form;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * @author: Charlie
 * @date: 2018/10/29
 */
@ApiModel(description = "backupsketstoreform")
public class AccountKeyStoreBackup {
    @ApiModelProperty(description = "password", required = true)
    private String password;
    @ApiModelProperty(description = "File path")
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
