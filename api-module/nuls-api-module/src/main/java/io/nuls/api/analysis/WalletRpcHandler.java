package io.nuls.api.analysis;

import io.nuls.api.constant.CommandConstant;
import io.nuls.api.constant.Constant;
import io.nuls.api.model.po.db.BlockInfo;
import io.nuls.api.rpc.RpcCall;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;

import java.util.HashMap;
import java.util.Map;

public class WalletRpcHandler {

    public static BlockInfo getBlockInfo(int chainID, long height) {
        Map<String, Object> params = new HashMap<>(Constant.INIT_CAPACITY_8);
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainID);
        params.put("height", height);
        try {
            String blockHex = (String) RpcCall.request(ModuleE.BL.abbr, CommandConstant.GET_BLOCK_BY_HEIGHT, params);
            byte[] bytes = HexUtil.decode(blockHex);
            Block block = new Block();
            block.parse(new NulsByteBuffer(bytes));
            return AnalysisHandler.toBlockInfo(block, chainID);
        } catch (NulsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
