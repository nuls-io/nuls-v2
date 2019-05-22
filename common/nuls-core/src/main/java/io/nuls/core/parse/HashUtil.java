package io.nuls.core.parse;

import io.nuls.core.crypto.HexUtil;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.model.ByteUtils;

import java.util.Arrays;
import java.util.List;

public class HashUtil {

    public static final int HASH_LENGTH = 32;

    public static String toHex(byte[] hash) {
        return HexUtil.encode(hash);
    }

    public static byte[] toBytes(String hash) {
        return HexUtil.decode(hash);
    }

    public static boolean validHash(String hex) {
        try {
            if (hex.length() != HASH_LENGTH) {
                return false;
            }
            toBytes(hex);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static byte[] calcHash(byte[] data) {
        return Sha256Hash.hashTwice(data);
    }

    public static byte[] calcMerkleHash(List<byte[]> hashList) {
        int levelOffset = 0;
        for (int levelSize = hashList.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            for (int left = 0; left < levelSize; left += 2) {
                int right = Math.min(left + 1, levelSize - 1);
                byte[] leftBytes = ByteUtils.reverseBytes(hashList.get(levelOffset + left));
                byte[] rightBytes = ByteUtils.reverseBytes(hashList.get(levelOffset + right));
                byte[] whole = new byte[leftBytes.length + rightBytes.length];
                System.arraycopy(leftBytes, 0, whole, 0, leftBytes.length);
                System.arraycopy(rightBytes, 0, whole, leftBytes.length, rightBytes.length);
                hashList.add(calcHash(whole));
            }
            levelOffset += levelSize;
        }
        byte[] bytes = hashList.get(hashList.size() - 1);
        Sha256Hash merkleHash = Sha256Hash.wrap(bytes);
        return merkleHash.getBytes();
    }

    public static boolean equals(byte[] hash1, byte[] hash2) {
        return Arrays.equals(hash1, hash2);
    }
}
