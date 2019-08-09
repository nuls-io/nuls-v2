package io.nuls.crosschain.nuls.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 主网验证人变更广播失败信息类
 * Main Network Verifier Change Broadcast Failure Information Class
 *
 * @author tag
 * 2019/6/28
 */
public class VerifierChangeSendFailPO extends BaseNulsData {
    private Set<Integer> chains = new HashSet<>();

    public VerifierChangeSendFailPO() {

    }

    public VerifierChangeSendFailPO(Set<Integer> chains) {
        this.chains = chains;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        if (chains != null && chains.size() > 0) {
            for (Integer chainId : chains) {
                stream.writeUint16(chainId);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int course;
        Set<Integer> chains = new HashSet<>();
        while (!byteBuffer.isFinished()) {
            course = byteBuffer.getCursor();
            byteBuffer.setCursor(course);
            chains.add(byteBuffer.readUint16());
        }
        this.chains = chains;
    }

    @Override
    public int size() {
        int size = 0;
        if (chains != null && chains.size() > 0) {
            size += SerializeUtils.sizeOfUint16() * chains.size();
        }
        return size;
    }

    public Set<Integer> getChains() {
        return chains;
    }

    public void setChains(Set<Integer> chains) {
        this.chains = chains;
    }
}
