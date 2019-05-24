package io.nuls.test.utils;

import io.nuls.base.data.NulsHash;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.parse.SerializeUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Niels
 */
public class MerkleTest {

    /**
     * 获取关键路径上需要的hash列表
     *
     * @param hashList
     * @return
     */
    public List<MerkleNodeHash> getMerkleNodeHashList(NulsHash nodeHash, List<NulsHash> hashList) {
        List<MerkleNodeHash> result = new ArrayList<>();
        int needIndex = -1;
        int levelOffset = 0;
        for (int levelSize = hashList.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            for (int left = 0; left < levelSize; left += 2) {
                int right = Math.min(left + 1, levelSize - 1);

                int first = levelOffset + left;
                int second = levelOffset + right;
                byte[] firstBytes = hashList.get(first).getBytes();
                byte[] secondBytes = hashList.get(second).getBytes();
                if ((-1 == needIndex && Arrays.equals(firstBytes, nodeHash.getBytes()))) {
                    result.add(new MerkleNodeHash(false, secondBytes));
                    needIndex = hashList.size();
                } else if (-1 == needIndex && Arrays.equals(secondBytes, nodeHash.getBytes())) {
                    result.add(new MerkleNodeHash(true, firstBytes));
                    needIndex = hashList.size();
                } else if (first == needIndex) {
                    result.add(new MerkleNodeHash(false, secondBytes));
                    needIndex = hashList.size();
                } else if (second == needIndex) {
                    result.add(new MerkleNodeHash(true, firstBytes));
                    needIndex = hashList.size();
                }
                byte[] leftBytes = ByteUtils.reverseBytes(firstBytes);
                byte[] rightBytes = ByteUtils.reverseBytes(secondBytes);
                byte[] whole = new byte[leftBytes.length + rightBytes.length];
                System.arraycopy(leftBytes, 0, whole, 0, leftBytes.length);
                System.arraycopy(rightBytes, 0, whole, leftBytes.length, rightBytes.length);
                NulsHash digest = NulsHash.calcHash(whole);
                hashList.add(digest);
            }
            levelOffset += levelSize;
        }
        return result;
    }

    @Test
    public void testMerkleProof() {
        List<NulsHash> hashList = createTxHashList(4111);
        NulsHash merkelRoot = NulsHash.calcMerkleHash(hashList);
        NulsHash txHash = hashList.get(211);
        List<MerkleNodeHash> hashes = getMerkleNodeHashList(txHash, hashList);
        NulsHash _root = calcMerkleHashByLeafe(txHash, hashes);
        assertEquals(merkelRoot, _root);
        System.out.println("Success");
    }

    private NulsHash calcMerkleHashByLeafe(NulsHash txHash, List<MerkleNodeHash> hashes) {
        if (null == hashes) {
            return null;
        }
        NulsHash start = txHash;
        for (MerkleNodeHash hash : hashes) {
            byte[] left, right;
            if (hash.isFirst()) {
                left = hash.getBytes();
                right = start.getBytes();
            } else {
                left = start.getBytes();
                right = hash.getBytes();
            }
            byte[] leftBytes = ByteUtils.reverseBytes(left);
            byte[] rightBytes = ByteUtils.reverseBytes(right);

            byte[] whole = new byte[leftBytes.length + rightBytes.length];
            System.arraycopy(leftBytes, 0, whole, 0, leftBytes.length);
            System.arraycopy(rightBytes, 0, whole, leftBytes.length, rightBytes.length);
            NulsHash digest = NulsHash.calcHash(whole);
            start = digest;
        }
        return new NulsHash(Sha256Hash.wrap(start.getBytes()).getBytes());
    }


    private List<NulsHash> createTxHashList(int size) {
        Random random = new Random();

        List<NulsHash> hashList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            hashList.add(NulsHash.calcHash(SerializeUtils.uint64ToByteArray(random.nextInt())));
        }
        return hashList;
    }

    static class MerkleNodeHash extends NulsHash {
        boolean first;

        protected MerkleNodeHash(boolean first, byte[] bytes) {
            super(bytes);
            this.first = first;
        }

        public boolean isFirst() {
            return first;
        }
    }
}
