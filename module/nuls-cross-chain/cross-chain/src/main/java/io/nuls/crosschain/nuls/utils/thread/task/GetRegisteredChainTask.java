package io.nuls.crosschain.nuls.utils.thread.task;
import io.nuls.crosschain.base.message.GetRegisteredChainMessage;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理主网发送的所有注册链交易消息线程
 * @author tag
 * 2019/5/20
 */
public class GetRegisteredChainTask implements Runnable{
    private ChainManager chainManager;

    public GetRegisteredChainTask(ChainManager chainManager){
        this.chainManager = chainManager;
    }

    @Override
    public void run() {
        if(chainManager.getChainMap() != null && chainManager.getChainMap().size() > 0){
            GetRegisteredChainMessage message = new GetRegisteredChainMessage();
            Chain chain = chainManager.getChainMap().values().iterator().next();
            int chainId = chain.getChainId();
            chainManager.getRegisteredCrossChainList().clear();
            NetWorkCall.broadcast(chainId, message, NulsCrossChainConstant.GET_REGISTERED_CHAIN_MESSAGE,true);
            try {
                int linkedNode = NetWorkCall.getAvailableNodeAmount(chainId, true);
                int tryCount = 0;
                while (tryCount < NulsCrossChainConstant.BYZANTINE_TRY_COUNT){
                    if(chainManager.getRegisteredChainMessageList().size() < linkedNode){
                        Thread.sleep(2000);
                        tryCount++;
                    }else{
                        break;
                    }
                }
                if(chainManager.getRegisteredChainMessageList().size() == 0){
                    return;
                }
                Map<RegisteredChainMessage,Integer> registeredChainMessageMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
                boolean reset = false;
                for (RegisteredChainMessage registeredChainMessage:chainManager.getRegisteredChainMessageList()) {
                    if(!registeredChainMessageMap.containsKey(registeredChainMessage)){
                        registeredChainMessageMap.put(registeredChainMessage, 1);
                    }else{
                        int count = registeredChainMessageMap.get(registeredChainMessage)+1;
                        if(count >= chainManager.getRegisteredChainMessageList().size()/2){
                            chainManager.setRegisteredCrossChainList(registeredChainMessage.getChainInfoList());
                            reset = true;
                            break;
                        }
                        registeredChainMessageMap.put(registeredChainMessage, count);
                    }
                }
                if(!reset){
                    int maxCount = 0;
                    RegisteredChainMessage realMessage = new RegisteredChainMessage();
                    for (Map.Entry<RegisteredChainMessage,Integer> entry:registeredChainMessageMap.entrySet()) {
                        int value = entry.getValue();
                        RegisteredChainMessage key = entry.getKey();
                        if(value >maxCount){
                            realMessage = key;
                        }
                        if(value == maxCount && key.getChainInfoList().size() > realMessage.getChainInfoList().size()){
                            realMessage = key;
                        }
                    }
                    chainManager.setRegisteredCrossChainList(realMessage.getChainInfoList());
                }
                chainManager.getRegisteredChainMessageList().clear();
            }catch (Exception e){
                chain.getLogger().error(e);
            }
        }
    }
}
