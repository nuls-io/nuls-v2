package io.nuls.block.thread;

import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.TestMessageV1;
import io.nuls.block.message.TestMessageV2;
import io.nuls.block.message.TestMessageV3;
import io.nuls.block.rpc.call.NetworkCall;

import java.util.List;

import static io.nuls.block.constant.CommandConstant.*;

public class MsgTestThread extends Thread {

    @Override
    public void run() {
        while (true) {
            List<Integer> chainIds = ContextManager.CHAIN_ID_LIST;
            int count = 0;
            for (Integer chainId : chainIds) {
                TestMessageV1 messageV1 = new TestMessageV1(count++);
                NetworkCall.broadcast(chainId, messageV1, TEST_1);
                TestMessageV2 messageV2 = new TestMessageV2(count++);
                NetworkCall.broadcast(chainId, messageV2, TEST_2);
                TestMessageV3 messageV3 = new TestMessageV3(count++);
                NetworkCall.broadcast(chainId, messageV3, TEST_3);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
