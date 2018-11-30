package io.nuls.ledger.serializers;

import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.model.FreezeState;
import io.nuls.ledger.utils.ByteUtil;
import io.nuls.ledger.utils.RLP;
import io.nuls.ledger.utils.RLPList;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.math.BigInteger;

/**
 * account state serializer
 * Created by wangkun23 on 2018/11/30.
 */
@Component
public class AccountStateSerializer implements Serializer<AccountState, byte[]> {

    @Autowired
    FreezeStateSerializer freezeStateSerializer;

    @Override
    public byte[] serialize(AccountState accountState) {
        byte[] chainId = RLP.encodeInt(accountState.getChainId());
        byte[] assetId = RLP.encodeInt(accountState.getAssetId());
        byte[] nonce = RLP.encodeBigInteger(BigInteger.valueOf(accountState.getNonce()));
        byte[] balance = RLP.encodeBigInteger(BigInteger.valueOf(accountState.getBalance()));
        byte[] freezeState = freezeStateSerializer.serialize(accountState.getFreezeState());
        return RLP.encodeList(chainId, assetId, nonce, balance, freezeState);
    }

    @Override
    public AccountState deserialize(byte[] stream) {
        if (stream == null || stream.length == 0) {
            return null;
        }

        AccountState accountState = new AccountState();
        RLPList items = (RLPList) RLP.decode2(stream).get(0);

        accountState.setChainId(ByteUtil.byteArrayToInt(items.get(0).getRLPData()));
        accountState.setAssetId(ByteUtil.byteArrayToInt(items.get(1).getRLPData()));
        accountState.setNonce(ByteUtil.byteArrayToLong(items.get(2).getRLPData()));
        accountState.setBalance(ByteUtil.byteArrayToLong(items.get(3).getRLPData()));
        accountState.setFreezeState(freezeStateSerializer.deserialize(items.get(4).getRLPData()));

        return accountState;
    }
}
