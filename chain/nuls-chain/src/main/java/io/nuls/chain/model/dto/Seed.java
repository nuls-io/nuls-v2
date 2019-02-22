package io.nuls.chain.model.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;

/**
 * @author tangyi
 * @date 2018/11/7
 */
@Data
@ToString
public class Seed{

    private String ip;
    private int port;


}
