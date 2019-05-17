package io.nuls.core.crypto;
/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 * Copyright 2014-2016 the libsecp256k1 contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedBytes;
import io.nuls.core.basic.VarInt;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.parse.SerializeUtils;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.math.ec.FixedPointUtil;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.bouncycastle.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Comparator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Represents an elliptic curve public and (optionally) private key, usable for digital signatures but not encryption.
 * Creating a new ECKey with the empty constructor will generate a new random keypair. Other static methods can be used
 * when you already have the public or private parts. If you create a key with only the public part, you can check
 * signatures but not create them.</p>
 *
 * <p>ECKey also provides access to Bitcoin Core compatible text message signing, as accessible via the UI or JSON-RPC.
 * This is slightly different to signing raw bytes - if you want to sign your own data and it won't be exposed as
 * text to people, you don't want to use this. If in doubt, ask on the mailing list.</p>
 *
 * <p>The ECDSA algorithm supports <i>key recovery</i> in which a signature plus a couple of discriminator bits can
 * be reversed to find the public key used to calculate it. This can be convenient when you have a message and a
 * signature and want to find out who signed it, rather than requiring the user to provide the expected identity.</p>
 *
 * <p>This class supports a variety of serialization forms. The methods that accept/return byte arrays serialize
 * private keys as raw byte arrays and public keys using the SEC standard byte encoding for public keys. Signatures
 * are encoded using ASN.1/DER inside the Bitcoin protocol.</p>
 *
 * <p>A key can be <i>compressed</i> or <i>uncompressed</i>. This refers to whether the public key is represented
 * when encoded into bytes as an (x, y) coordinate on the elliptic curve, or whether it's represented as just an X
 * co-ordinate and an extra byte that carries a sign bit. With the latter form the Y coordinate can be calculated
 * dynamically, however, <b>because the binary serialization is different the address of a key changes if its
 * compression status is changed</b>. If you deviate from the defaults it's important to understand this: money sent
 * to a compressed version of the key will have a different address to the same key in uncompressed form. Whether
 * a public key is compressed or not is recorded in the SEC binary serialisation format, and preserved in a flag in
 * this class so round-tripping preserves state. Unless you're working with old software or doing unusual things, you
 * can usually ignore the compressed/uncompressed distinction.</p>
 */
public class ECKey {
    private static final Logger log = LoggerFactory.getLogger(ECKey.class);
    public static final int SIGNUM = 1;
    /**
     * Sorts oldest keys first, newest last.
     */
    public static final Comparator<ECKey> AGE_COMPARATOR = new Comparator<ECKey>() {

        @Override
        public int compare(ECKey k1, ECKey k2) {
            if (k1.creationTimeSeconds == k2.creationTimeSeconds) {
                return 0;
            } else {
                return k1.creationTimeSeconds > k2.creationTimeSeconds ? 1 : -1;
            }
        }
    };

    /**
     * Compares pub key bytes using {@link com.google.common.primitives.UnsignedBytes#lexicographicalComparator()}
     */
    public static final Comparator<ECKey> PUBKEY_COMPARATOR = new Comparator<ECKey>() {
        private Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();

        @Override
        public int compare(ECKey k1, ECKey k2) {
            return comparator.compare(k1.getPubKey(), k2.getPubKey());
        }
    };

    // The parameters of the secp256k1 curve that Bitcoin uses.
    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

    /**
     * The parameters of the secp256k1 curve that Bitcoin uses.
     */
    public static final ECDomainParameters CURVE;

    /**
     * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a signature. If you aren't
     * sure what this is about, you can ignore it.
     */
    public static final BigInteger HALF_CURVE_ORDER;

    private static final SecureRandom secureRandom;

    static {
        // Init proper random number generator, as some old Android installations have bugs that make it unsecure.
        if (HexUtil.isAndroidRuntime()) {
            new LinuxSecureRandom();
        }

        // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations.
        FixedPointUtil.precompute(CURVE_PARAMS.getG());
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
                CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
        secureRandom = new SecureRandom();
    }

    // The two parts of the key. If "priv" is set, "pub" can always be calculated. If "pub" is set but not "priv", we
    // can only verify signatures not make them.
    protected final BigInteger priv;  // A field element.
    protected final LazyECPoint pub;

    // Creation time of the key in seconds since the epoch, or zero if the key was deserialized from a version that did
    // not have this field.
    protected long creationTimeSeconds;

    protected EncryptedData encryptedPrivateKey;

    private byte[] pubKeyHash;

    /**
     * Generates an entirely new keypair. Point compression is used so the resulting public key will be 33 bytes
     * (32 for the co-ordinate and 1 byte to represent the y bit).
     */
    public ECKey() {
        this(secureRandom);
    }

    /**
     * Generates an entirely new keypair with the given {@link SecureRandom} object. Point compression is used so the
     * resulting public key will be 33 bytes (32 for the co-ordinate and 1 byte to represent the y bit).
     */
    public ECKey(SecureRandom secureRandom) {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        priv = privParams.getD();
        pub = new LazyECPoint(CURVE.getCurve(), pubParams.getQ().getEncoded(true));
        creationTimeSeconds = System.currentTimeMillis();
    }

    protected ECKey(@Nullable BigInteger priv, ECPoint pub) {
        this(priv, new LazyECPoint(checkNotNull(pub)));
    }

    protected ECKey(@Nullable BigInteger priv, LazyECPoint pub) {
        if (priv != null) {
            checkArgument(priv.bitLength() <= 32 * 8, "private key exceeds 32 bytes: %s bits", priv.bitLength());
            // Try and catch buggy callers or bad key imports, etc. Zero and one are special because these are often
            // used as sentinel values and because scripting languages have a habit of auto-casting true and false to
            // 1 and 0 or vice-versa. Type confusion bugs could therefore result in private keys with these values.
            checkArgument(!priv.equals(BigInteger.ZERO));
            checkArgument(!priv.equals(BigInteger.ONE));
        }
        this.priv = priv;
        this.pub = checkNotNull(pub);
    }

    /**
     * Utility for compressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     */
    public static ECPoint compressPoint(ECPoint point) {
        return getPointWithCompression(point, true);
    }

    public static LazyECPoint compressPoint(LazyECPoint point) {
        return point.isCompressed() ? point : new LazyECPoint(compressPoint(point.get()));
    }

    /**
     * Utility for decompressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     */
    public static ECPoint decompressPoint(ECPoint point) {
        return getPointWithCompression(point, false);
    }

    public static LazyECPoint decompressPoint(LazyECPoint point) {
        return !point.isCompressed() ? point : new LazyECPoint(decompressPoint(point.get()));
    }

    private static ECPoint getPointWithCompression(ECPoint point, boolean compressed) {
        if (point.isCompressed() == compressed) {
            return point;
        }
        point = point.normalize();
        BigInteger x = point.getAffineXCoord().toBigInteger();
        BigInteger y = point.getAffineYCoord().toBigInteger();
        return CURVE.getCurve().createPoint(x, y, compressed);
    }

    /**
     * Construct an ECKey from an ASN.1 encoded private key. These are produced by OpenSSL and stored by Bitcoin
     * Core in its wallet. Note that this is slow because it requires an EC point multiply.
     */
    public static ECKey fromASN1(byte[] asn1privkey) {
        return extractKeyFromASN1(asn1privkey);
    }

    /**
     * Creates an ECKey given the private key only. The public key is calculated from it (this is slow). The resulting
     * public key is compressed.
     */
    public static ECKey fromPrivate(BigInteger privKey) {
        return fromPrivate(privKey, true);
    }

    /**
     * Creates an ECKey given the private key only. The public key is calculated from it (this is slow), either
     * compressed or not.
     */
    public static ECKey fromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = publicPointFromPrivate(privKey);
        return new ECKey(privKey, getPointWithCompression(point, compressed));
    }

    /**
     * Creates an ECKey given the private key only. The public key is calculated from it (this is slow). The resulting
     * public key is compressed.
     */
    public static ECKey fromPrivate(byte[] privKeyBytes) {
        return fromPrivate(new BigInteger(1, privKeyBytes));
    }

    /**
     * Creates an ECKey given the private key only. The public key is calculated from it (this is slow), either
     * compressed or not.
     */
    public static ECKey fromPrivate(byte[] privKeyBytes, boolean compressed) {
        return fromPrivate(new BigInteger(1, privKeyBytes), compressed);
    }

    /**
     * Creates an ECKey that simply trusts the caller to ensure that point is really the result of multiplying the
     * generator point by the private key. This is used to speed things up when you know you have the right values
     * already. The compression state of pub will be preserved.
     */
    public static ECKey fromPrivateAndPrecalculatedPublic(BigInteger priv, ECPoint pub) {
        return new ECKey(priv, pub);
    }

    /**
     * Creates an ECKey that simply trusts the caller to ensure that point is really the result of multiplying the
     * generator point by the private key. This is used to speed things up when you know you have the right values
     * already. The compression state of the point will be preserved.
     */
    public static ECKey fromPrivateAndPrecalculatedPublic(byte[] priv, byte[] pub) {
        checkNotNull(priv);
        checkNotNull(pub);
        return new ECKey(new BigInteger(1, priv), CURVE.getCurve().decodePoint(pub));
    }

    /**
     * Creates an ECKey that cannot be used for signing, only verifying signatures, from the given point. The
     * compression state of pub will be preserved.
     */
    public static ECKey fromPublicOnly(ECPoint pub) {
        return new ECKey(null, pub);
    }

    /**
     * 根据EncryptedData和公钥生成ECKey
     *
     * @param encryptedPrivateKey 私钥封装类
     * @param pubKey              公钥
     * @return ECKey
     */
    public static ECKey fromEncrypted(EncryptedData encryptedPrivateKey, byte[] pubKey) {
        ECKey key = fromPublicOnly(pubKey);
        ObjectUtils.canNotEmpty(encryptedPrivateKey, "encryptedPrivateKey can not null!");
        key.encryptedPrivateKey = encryptedPrivateKey;
        return key;
    }

    /**
     * Creates an ECKey that cannot be used for signing, only verifying signatures, from the given encoded point.
     * The compression state of pub will be preserved.
     */
    public static ECKey fromPublicOnly(byte[] pub) {
        return new ECKey(null, CURVE.getCurve().decodePoint(pub));
    }

    /**
     * Returns a copy of this key, but with the public point represented in uncompressed form. Normally you would
     * never need this: it's for specialised scenarios or when backwards compatibility in encoded form is necessary.
     */
    public ECKey decompress() {
        if (!pub.isCompressed()) {
            return this;
        } else {
            return new ECKey(priv, decompressPoint(pub.get()));
        }
    }

    /**
     * Creates an ECKey given only the private key bytes. This is the same as using the BigInteger constructor, but
     * is more convenient if you are importing a key from elsewhere. The public key will be automatically derived
     * from the private key.
     */
    @Deprecated
    public ECKey(@Nullable byte[] privKeyBytes, @Nullable byte[] pubKey) {
        this(privKeyBytes == null ? null : new BigInteger(1, privKeyBytes), pubKey);
    }


    /**
     * Creates an ECKey given either the private key only, the public key only, or both. If only the private key
     * is supplied, the public key will be calculated from it (this is slow). If both are supplied, it's assumed
     * the public key already correctly matches the private key. If only the public key is supplied, this ECKey cannot
     * be used for signing.
     *
     * @param compressed If set to true and pubKey is null, the derived public key will be in compressed form.
     */
    @Deprecated
    public ECKey(@Nullable BigInteger privKey, @Nullable byte[] pubKey, boolean compressed) {
        if (privKey == null && pubKey == null) {
            throw new IllegalArgumentException("ECKey requires at least private or public key");
        }
        this.priv = privKey;
        if (pubKey == null) {
            // Derive public from private.
            ECPoint point = publicPointFromPrivate(privKey);
            point = getPointWithCompression(point, compressed);
            this.pub = new LazyECPoint(point);
        } else {
            // We expect the pubkey to be in regular encoded form, just as a BigInteger. Therefore the first byte is
            // a special marker byte.
            // TODO: This is probably not a useful API and may be confusing.
            this.pub = new LazyECPoint(CURVE.getCurve(), pubKey);
        }
    }

    /**
     * Creates an ECKey given either the private key only, the public key only, or both. If only the private key
     * is supplied, the public key will be calculated from it (this is slow). If both are supplied, it's assumed
     * the public key already correctly matches the public key. If only the public key is supplied, this ECKey cannot
     * be used for signing.
     */
    @Deprecated
    private ECKey(@Nullable BigInteger privKey, @Nullable byte[] pubKey) {
        this(privKey, pubKey, false);
    }

    /**
     * Returns true if this key doesn't have unencrypted access to private key bytes. This may be because it was never
     * given any private key bytes to begin with (a watching key), or because the key is encrypted. You can use
     */
    public boolean isPubKeyOnly() {
        return priv == null;
    }

    /**
     * Returns true if this key has unencrypted access to private key bytes. Does the opposite of
     * {@link #isPubKeyOnly()}.
     */
    public boolean hasPrivKey() {
        return priv != null;
    }

    /**
     * Returns public key bytes from the given private key. To convert a byte array into a BigInteger,
     * use {@code new BigInteger(1, bytes);}
     */
    public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = publicPointFromPrivate(privKey);
        return point.getEncoded(compressed);
    }

    /**
     * Returns public key point from the given private key. To convert a byte array into a BigInteger,
     * use {@code new BigInteger(1, bytes);}
     */
    public static ECPoint publicPointFromPrivate(BigInteger privKey) {
        /*
         * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group order,
         * but that could change in future versions.
         */
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }

    /**
     * Gets the hash160 form of the public key (as seen in addresses).
     */
    public byte[] getPubKeyHash() {
        if (pubKeyHash == null) {
            pubKeyHash = SerializeUtils.sha256hash160(this.pub.getEncoded());
        }
        return pubKeyHash;
    }

    /**
     * Gets the raw public key value. This appears in transaction scriptSigs. Note that this is <b>not</b> the same
     * as the pubKeyHash/address.
     */
    public byte[] getPubKey() {
        return pub.getEncoded();
    }

    /**
     * Gets the public key in the form of an elliptic curve point object from Bouncy Castle.
     */
    public ECPoint getPubKeyPoint() {
        return pub.get();
    }

    /**
     * Gets the private key in the form of an integer field element. The public key is derived by performing EC
     * point addition this number of times (i.e. point multiplying).
     *
     * @throws java.lang.IllegalStateException if the private key bytes are not available.
     */
    public BigInteger getPrivKey() {
        if (priv == null) {
            throw new MissingPrivateKeyException();
        }
        return priv;
    }

    public static class MissingPrivateKeyException extends RuntimeException {
        private static final long serialVersionUID = 2789844760773725676L;
    }

    /**
     * Returns whether this key is using the compressed form or not. Compressed pubkeys are only 33 bytes, not 64.
     */
    public boolean isCompressed() {
        return pub.isCompressed();
    }

    /**
     * Groups the two components that make up a signature, and provides a way to encode to DER form, which is
     * how ECDSA signatures are represented when embedded in other data structures in the Bitcoin protocol. The raw
     * components can be useful for doing further EC maths on them.
     */
    public static class ECDSASignature {
        /**
         * The two components of the signature.
         */
        public final BigInteger r, s;

        /**
         * Constructs a signature with the given components. Does NOT automatically canonicalise the signature.
         */
        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        /**
         * Returns true if the S component is "low", that means it is below {@link ECKey#HALF_CURVE_ORDER}. See <a
         * href="https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#Low_S_values_in_signatures">BIP62</a>.
         */
        public boolean isCanonical() {
            return s.compareTo(HALF_CURVE_ORDER) <= 0;
        }

        /**
         * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
         * the same message. However, we dislike the ability to modify the bits of a Bitcoin transaction after it's
         * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
         * considered legal and the other will be banned.
         */
        public ECDSASignature toCanonicalised() {
            if (!isCanonical()) {
                // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
                // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
                //    N = 10
                //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
                //    10 - 8 == 2, giving us always the latter solution, which is canonical.
                return new ECDSASignature(r, CURVE.getN().subtract(s));
            } else {
                return this;
            }
        }

        /**
         * DER is an international standard for serializing data structures which is widely used in cryptography.
         * It's somewhat like protocol buffers but less convenient. This method returns a standard DER encoding
         * of the signature, as recognized by OpenSSL and other libraries.
         */
        public byte[] encodeToDER() {
            try {
                return derByteStream().toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
        }

        /**
         * @throws Exception if the signature is unparseable in some way.
         */
        public static ECDSASignature decodeFromDER(byte[] bytes) {
            ASN1InputStream decoder = null;
            try {
                // BouncyCastle by default is strict about parsing ASN.1 integers. We relax this check, because some
                // Bitcoin signatures would not parse.
                Properties.setThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer", true);
                decoder = new ASN1InputStream(bytes);
                final ASN1Primitive seqObj = decoder.readObject();
                if (seqObj == null) {
                    throw new RuntimeException("Reached past end of ASN.1 stream.");
                }
                if (!(seqObj instanceof DLSequence)) {
                    throw new RuntimeException("Read unexpected class: " + seqObj.getClass().getName());
                }
                final DLSequence seq = (DLSequence) seqObj;
                ASN1Integer r, s;
                try {
                    r = (ASN1Integer) seq.getObjectAt(0);
                    s = (ASN1Integer) seq.getObjectAt(1);
                } catch (ClassCastException e) {
                    throw new RuntimeException(e);
                }
                // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
                // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
                return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (decoder != null) {
                    try {
                        decoder.close();
                    } catch (IOException x) {
                    }
                }
                Properties.removeThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer");
            }
        }

        protected ByteArrayOutputStream derByteStream() throws IOException {
            // Usually 70-72 bytes.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(r));
            seq.addObject(new ASN1Integer(s));
            seq.close();
            return bos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ECDSASignature other = (ECDSASignature) o;
            return r.equals(other.r) && s.equals(other.s);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(r, s);
        }
    }

    /**
     * If this global variable is set to true, sign() creates a dummy signature and verify() always returns true.
     * This is intended to help accelerate unit tests that do a lot of signing/verifying, which in the debugger
     * can be painfully slow.
     */
    @VisibleForTesting
    public static boolean FAKE_SIGNATURES = false;

    /**
     * 用私钥对数据进行签名
     *
     * @param hash 需签名数据
     * @return byte[] 签名
     */
    public byte[] sign(byte[] hash) {
        return sign(hash, null);
    }

    /**
     * 用私钥对数据进行签名
     *
     * @param hash   需签名数据
     * @param aesKey 私钥
     * @return byte[] 签名
     */
    public byte[] sign(Sha256Hash hash, BigInteger aesKey) {
        return doSign(hash.getBytes(), priv);
    }

    /**
     * 用私钥对数据进行签名
     *
     * @param hash   需签名数据
     * @param aesKey 私钥
     * @return byte[] 签名
     */
    public byte[] sign(byte[] hash, BigInteger aesKey) {
        return doSign(hash, priv);
    }

    /**
     * 用私钥对数据进行签名
     *
     * @param input                需签名数据
     * @param privateKeyForSigning 私钥
     * @return byte[] 签名
     */
    protected byte[] doSign(byte[] input, BigInteger privateKeyForSigning) {
        HexUtil.checkNotNull(privateKeyForSigning);
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(input);
        return new ECKey.ECDSASignature(components[0], components[1]).toCanonicalised().encodeToDER();
    }


    /**
     * <p>Verifies the given ECDSA signature against the message bytes using the public key bytes.</p>
     *
     * <p>When using native ECDSA verification, data must be 32 bytes, and no element may be
     * larger than 520 bytes.</p>
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     * @param pub       The public key bytes to use.
     */
    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException e) {
            log.error("Caught NPE inside bouncy castle", e);
            return false;
        }
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     * @param pub       The public key bytes to use.
     * @throws Exception if the signature is unparseable in some way.
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        return verify(data, ECKey.ECDSASignature.decodeFromDER(signature), pub);
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param hash      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     * @throws Exception if the signature is unparseable in some way.
     */
    public boolean verify(byte[] hash, byte[] signature) throws Exception {
        return ECKey.verify(hash, signature, getPubKey());
    }

    /**
     * Verifies the given R/S pair (signature) against a hash using the public key.
     */
    public boolean verify(Sha256Hash sigHash, ECDSASignature signature) {
        return ECKey.verify(sigHash.getBytes(), signature, getPubKey());
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key, and throws an exception
     * if the signature doesn't match
     *
     * @throws Exception                        if the signature is unparseable in some way.
     * @throws java.security.SignatureException if the signature does not match.
     */
    public void verifyOrThrow(byte[] hash, byte[] signature) throws Exception, SignatureException {
        if (!verify(hash, signature)) {
            throw new SignatureException();
        }
    }

    /**
     * Verifies the given R/S pair (signature) against a hash using the public key, and throws an exception
     * if the signature doesn't match
     *
     * @throws java.security.SignatureException if the signature does not match.
     */
    public void verifyOrThrow(Sha256Hash sigHash, ECDSASignature signature) throws SignatureException {
        if (!ECKey.verify(sigHash.getBytes(), signature, getPubKey())) {
            throw new SignatureException();
        }
    }

    /**
     * Returns true if the given pubkey is canonical, i.e. the correct length taking into account compression.
     */
    public static boolean isPubKeyCanonical(byte[] pubkey) {
        if (pubkey.length < 33) {
            return false;
        }
        if (pubkey[0] == 0x04) {
            // Uncompressed pubkey
            if (pubkey.length != 65) {
                return false;
            }
        } else if (pubkey[0] == 0x02 || pubkey[0] == 0x03) {
            // Compressed pubkey
            if (pubkey.length != 33) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private static ECKey extractKeyFromASN1(byte[] asn1privkey) {
        // To understand this code, see the definition of the ASN.1 format for EC private keys in the OpenSSL source
        // code in ec_asn1.c:
        //
        // ASN1_SEQUENCE(EC_PRIVATEKEY) = {
        //   ASN1_SIMPLE(EC_PRIVATEKEY, version, LONG),
        //   ASN1_SIMPLE(EC_PRIVATEKEY, privateKey, ASN1_OCTET_STRING),
        //   ASN1_EXP_OPT(EC_PRIVATEKEY, parameters, ECPKPARAMETERS, 0),
        //   ASN1_EXP_OPT(EC_PRIVATEKEY, publicKey, ASN1_BIT_STRING, 1)
        // } ASN1_SEQUENCE_END(EC_PRIVATEKEY)
        //
        ASN1InputStream decoder = null;
        try {
            decoder = new ASN1InputStream(asn1privkey);
            DLSequence seq = (DLSequence) decoder.readObject();
            checkArgument(decoder.readObject() == null, "Input contains extra bytes");
            decoder.close();

            checkArgument(seq.size() == 4, "Input does not appear to be an ASN.1 OpenSSL EC private key");

            checkArgument(((ASN1Integer) seq.getObjectAt(0)).getValue().equals(BigInteger.ONE),
                    "Input is of wrong version");

            byte[] privbits = ((ASN1OctetString) seq.getObjectAt(1)).getOctets();
            BigInteger privkey = new BigInteger(1, privbits);

            ASN1TaggedObject pubkey = (ASN1TaggedObject) seq.getObjectAt(3);
            checkArgument(pubkey.getTagNo() == 1, "Input has 'publicKey' with bad tag number");
            byte[] pubbits = ((DERBitString) pubkey.getObject()).getBytes();
            checkArgument(pubbits.length == 33 || pubbits.length == 65, "Input has 'publicKey' with invalid length");
            int encoding = pubbits[0] & 0xFF;
            // Only allow compressed(2,3) and uncompressed(4), not infinity(0) or hybrid(6,7)
            checkArgument(encoding >= 2 && encoding <= 4, "Input has 'publicKey' with invalid encoding");

            // Now sanity check to ensure the pubkey bytes match the privkey.
            boolean compressed = (pubbits.length == 33);
            ECKey key = new ECKey(privkey, null, compressed);
            if (!Arrays.equals(key.getPubKey(), pubbits)) {
                throw new IllegalArgumentException("Public key in ASN.1 structure does not match private key.");
            }
            return key;
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen, reading from memory stream.
        } finally {
            if (decoder != null) {
                try {
                    decoder.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * Returns the recovery ID, a byte with value between 0 and 3, inclusive, that specifies which of 4 possible
     * curve points was used to sign a message. This value is also referred to as "v".
     *
     * @throws RuntimeException if no recovery ID can be found.
     */
    public byte findRecoveryId(Sha256Hash hash, ECDSASignature sig) {
        byte recId = -1;
        for (byte i = 0; i < 4; i++) {
            ECKey k = ECKey.recoverFromSignature(i, sig, hash, isCompressed());
            if (k != null && k.pub.equals(pub)) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        }
        return recId;
    }

    /**
     * <p>Given the components of a signature and a selector value, recover and return the public key
     * that generated the signature according to the algorithm in SEC1v2 section 4.1.6.</p>
     *
     * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the correct one. Because
     * the key recovery operation yields multiple potential keys, the correct key must either be stored alongside the
     * signature, or you must be willing to try each recId in turn until you find one that outputs the key you are
     * expecting.</p>
     *
     * <p>If this method returns null it means recovery was not possible and recId should be iterated.</p>
     *
     * <p>Given the above two points, a correct usage of this method is inside a for loop from 0 to 3, and if the
     * output is null OR a key that is not the one you expect, you try again with the next recId.</p>
     *
     * @param recId      Which possible key to recover.
     * @param sig        the R and S components of the signature, wrapped.
     * @param message    Hash of the data that was signed.
     * @param compressed Whether or not the original pubkey was compressed.
     * @return An ECKey containing only the public part, or null if recovery wasn't possible.
     */
    @Nullable
    public static ECKey recoverFromSignature(int recId, ECDSASignature sig, Sha256Hash message, boolean compressed) {
        Preconditions.checkArgument(recId >= 0, "recId must be positive");
        Preconditions.checkArgument(sig.r.signum() >= 0, "r must be positive");
        Preconditions.checkArgument(sig.s.signum() >= 0, "s must be positive");
        Preconditions.checkNotNull(message);
        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        BigInteger n = CURVE.getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion routine
        //        specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
        //        conversion routine specified in Section 2.3.4. If this conversion routine outputs “invalid”, then
        //        do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
        // So it's encoded in the recId.
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers responsibility).
        if (!R.multiply(n).isInfinity()) {
            return null;
        }
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        BigInteger e = message.toBigInteger();
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n). In the above equation
        // ** is point multiplication and + is point addition (the EC group operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
        return ECKey.fromPublicOnly(q.getEncoded(compressed));
    }

    /**
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    /**
     * Returns a 32 byte array containing the private key.
     */
    public byte[] getPrivKeyBytes() {
        return SerializeUtils.bigIntegerToBytes(getPrivKey(), 32);
    }


    /**
     * Returns the creation time of this key or zero if the key was deserialized from a version that did not store
     * that data.
     */
    public long getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    /**
     * Sets the creation time of this key. Zero is a convention to mean "unavailable". This method can be useful when
     * you have a raw key you are importing from somewhere else.
     */
    public void setCreationTimeSeconds(long newCreationTimeSeconds) {
        if (newCreationTimeSeconds < 0) {
            throw new IllegalArgumentException("Cannot set creation time to negative value: " + newCreationTimeSeconds);
        }
        creationTimeSeconds = newCreationTimeSeconds;
    }


    /**
     * A wrapper for {@link #getPrivKeyBytes()} that returns null if the private key bytes are missing or would have
     * to be derived (for the HD key case).
     */
    @Nullable
    public byte[] getSecretBytes() {
        if (hasPrivKey()) {
            return getPrivKeyBytes();
        } else {
            return null;
        }
    }

    /**
     * An alias for {@link #getEncryptedPrivateKey()}
     */
    @Nullable
    public EncryptedData getEncryptedData() {
        return getEncryptedPrivateKey();
    }

    /**
     * Returns the the encrypted private key bytes and initialisation vector for this ECKey, or null if the ECKey
     * is not encrypted.
     */
    @Nullable
    public EncryptedData getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(EncryptedData encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    @Override
    public int hashCode() {
        return pub.hashCode();
    }


    public String getPrivateKeyAsHex() {
        return HexUtil.encode(getPrivKeyBytes());
    }

    public String getPublicKeyAsHex() {
        return HexUtil.encode(pub.getEncoded());
    }

    /**
     * The string that prefixes all text messages signed using Bitcoin keys.
     */
    private static final String BITCOIN_SIGNED_MESSAGE_HEADER = "Bitcoin Signed Message:\n";
    private static final byte[] BITCOIN_SIGNED_MESSAGE_HEADER_BYTES = BITCOIN_SIGNED_MESSAGE_HEADER.getBytes(StandardCharsets.UTF_8);

    /**
     * <p>Given a textual message, returns a byte buffer formatted as follows:</p>
     * <p>{@code [24] "Bitcoin Signed Message:\n" [message.length as a varint] message}</p>
     */
    private static byte[] formatMessageForSigning(String message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES.length);
            bos.write(BITCOIN_SIGNED_MESSAGE_HEADER_BYTES);
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            VarInt size = new VarInt(messageBytes.length);
            bos.write(size.encode());
            bos.write(messageBytes);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }


    /**
     * 验证16进制私钥字符串是否正确
     * @return  boolean 正确返回true,否则返回false
     * */
    public static boolean isValidPrivteHex(String privateHex) {
        int len = privateHex.length();
        if (len % 2 == 1) {
            return false;
        }

        if (len < 60 || len > 66) {
            return false;
        }
        return true;
    }
}
