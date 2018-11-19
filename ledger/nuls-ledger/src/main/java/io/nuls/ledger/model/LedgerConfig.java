package io.nuls.ledger.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@ToString
public class LedgerConfig {

    @Setter
    @Getter
    private String databaseDir;

    @Setter
    @Getter
    private Boolean databaseReset;

    @Setter
    @Getter
    private String projectVersion;

    @Setter
    @Getter
    private String projectVersionModifier;

    @Setter
    @Getter
    protected Integer databaseVersion;

    @Setter
    @Getter
    private Boolean syncEnabled;

}
