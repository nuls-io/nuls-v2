package io.nuls.core.model;
import io.nuls.core.basic.VarInt;
import io.nuls.core.log.Log;

import java.util.HashMap;
import java.util.Map;

public class LogBackObjTest {
    public static void main(String[] args){
        Map<String, VarInt> map = new HashMap<>();
        map.put("test", new VarInt(1000));
        Log.debug("logBack result{},param 2 {}",map,map);
    }
}
