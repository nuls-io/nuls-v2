package io.nuls.ledger.serializers;

/**
 * Converter from one type to another and vice versa
 * <p>
 * Created by wangkun23 on 2018/11/30.
 */
public interface Serializer<T, S> {
    /**
     * Converts T ==> S
     * Should correctly handle null parameter
     */
    S serialize(T object);

    /**
     * Converts S ==> T
     * Should correctly handle null parameter
     */
    T deserialize(S stream);
}
