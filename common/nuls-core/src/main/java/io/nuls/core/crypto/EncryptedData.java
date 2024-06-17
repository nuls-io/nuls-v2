/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.core.crypto;

import java.util.Arrays;
import java.util.Objects;

/**
 * Encrypted data
 *
 * @author ln
 */
public final class EncryptedData {
    public static byte[] DEFAULT_IV = new byte[16];
    /**
     * iv
     */
    private byte[] initialisationVector;
    /**
     * Encryption result
     */
    private byte[] encryptedBytes;

    public EncryptedData(byte[] initialisationVector, byte[] encryptedBytes) {
        this.initialisationVector = Arrays.copyOf(initialisationVector, initialisationVector.length);
        this.encryptedBytes = Arrays.copyOf(encryptedBytes, encryptedBytes.length);
    }

    public EncryptedData(byte[] encryptedBytes) {
        this(DEFAULT_IV, encryptedBytes);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EncryptedData other = (EncryptedData) o;
        return Arrays.equals(encryptedBytes, other.encryptedBytes) && Arrays.equals(initialisationVector, other.initialisationVector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(encryptedBytes), Arrays.hashCode(initialisationVector));
    }

    @Override
    public String toString() {
        return "EncryptedData [initialisationVector=" + Arrays.toString(initialisationVector)
                + ", encryptedPrivateKey=" + Arrays.toString(encryptedBytes) + "]";
    }

    public byte[] getInitialisationVector() {
        return initialisationVector;
    }

    public byte[] getEncryptedBytes() {
        return encryptedBytes;
    }

    public void setInitialisationVector(byte[] initialisationVector) {
        this.initialisationVector = initialisationVector;
    }

    public void setEncryptedBytes(byte[] encryptedBytes) {
        this.encryptedBytes = encryptedBytes;
    }
}
