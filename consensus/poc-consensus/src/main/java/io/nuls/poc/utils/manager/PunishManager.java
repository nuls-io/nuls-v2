package io.nuls.poc.utils.manager;

import io.nuls.poc.model.po.EvidencePo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PunishManager {
    /**
     * 记录出块地址PackingAddress，同一个高度发出了两个不同的块的证据
     * 下一轮正常则清零， 连续3轮将会被红牌惩罚
     */
    private Map<String, List<EvidencePo>> bifurcationEvidenceMap = new HashMap<>();


    /**
     * 控制该类为单例模式
     * */
    public static PunishManager instance = null;
    private PunishManager() { }
    private static Integer LOCK = 0;
    public static PunishManager getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new PunishManager();
            }
            return instance;
        }
    }


}
