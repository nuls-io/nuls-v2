/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.tools.cache;

import io.nuls.tools.cache.listener.NulsCacheListener;
import io.nuls.tools.cache.manager.EhCacheManager;
import io.nuls.tools.cache.model.CacheMapParams;
import org.ehcache.Cache;
import org.ehcache.spi.copy.Copier;

import java.io.Serializable;
import java.util.*;

/**
 * 缓存使用的键值对存储结构，提供一些基本的方法
 * The key values used by the cache provide some basic methods for storing the structure.
 *
 * @author Niels
 */
public class CacheMap<K, V> {

    private EhCacheManager cacheManager = EhCacheManager.getInstance();

    private final String cacheName;

    public CacheMap(String cacheName, int heapMb, Class keyType, Class<? extends Serializable> valueType, Copier<V> valueCopier) {
        this(cacheName, heapMb, keyType, valueType, 0, 0, valueCopier);
    }

    public CacheMap(String cacheName, int heapMb, Class keyType, Class<? extends Serializable> valueType) {
        this(cacheName, heapMb, keyType, valueType, 0, 0, null);
    }

    public CacheMap(String cacheName, int heapMb, Class keyType, Class<? extends Serializable> valueType, int timeToLiveSeconds, int timeToIdleSeconds, Copier<V> valueCopier) {
        this(cacheName, heapMb, keyType, valueType, timeToLiveSeconds, timeToIdleSeconds, null, valueCopier);
    }

    public CacheMap(String cacheName, int heapMb, Class keyType, Class<? extends Serializable> valueType, int timeToLiveSeconds, int timeToIdleSeconds) {
        this(cacheName, heapMb, keyType, valueType, timeToLiveSeconds, timeToIdleSeconds, null, null);
    }

    /**
     * CacheMap构造方法
     *
     * @param cacheName         缓存别名
     * @param heapMb            缓存区大小
     * @param keyType           键类型
     * @param valueType         值类型
     * @param timeToLiveSeconds 对象在失效前允许存活时间
     * @param timeToIdleSeconds 对象在失效前的允许闲置时间
     * @param listener          监听器
     * @param valueCopier       Copier
     */
    public CacheMap(String cacheName, int heapMb, Class keyType, Class<? extends Serializable> valueType, int timeToLiveSeconds, int timeToIdleSeconds, NulsCacheListener listener, Copier<V> valueCopier) {
        this(cacheName, new CacheMapParams(heapMb, keyType, valueType, timeToLiveSeconds, timeToIdleSeconds, listener, valueCopier));
    }

    public CacheMap(String cacheName, CacheMapParams params) {
        this.cacheName = cacheName;
        this.cacheManager.createCache(cacheName, params);
    }

    /**
     * 获取当前缓存对象缓存的键个数
     *
     * @return int
     */
    @Deprecated
    public int size() {
        return this.keySet().size();
    }

    /**
     * 判断当前换缓存是否为空
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return this.keySet().isEmpty();
    }

    /**
     * 查看当前缓存是否包含指定键
     *
     * @param key 键
     * @return boolean
     */
    public boolean containsKey(K key) {
        Cache cache = this.cacheManager.getCache(cacheName);
        if (cache == null) {
            return false;
        }
        return cache.containsKey(key);
    }

    /**
     * 查看当前缓存是否存在指定值
     *
     * @param value 值
     * @return boolean
     */
    public boolean containsValue(V value) {
        List<V> vlist = this.values();
        return vlist.contains(value);
    }


    /**
     * 从缓存中获取指定键对应的值
     *
     * @param key 键
     * @return V 值
     */
    public V get(K key) {
        if (null == cacheManager.getCache(cacheName) || null == key) {
            return null;
        }
        return ((V) cacheManager.getCache(cacheName).get(key));
    }


    /**
     * 缓存键值对
     *
     * @param key   键
     * @param value 值
     */
    public void put(K key, V value) {
        Object valueObj = value;
        if (null == cacheManager.getCache(cacheName)) {
            throw new RuntimeException("Cache not exist!");
        }
        cacheManager.getCache(cacheName).put(key, valueObj);
    }

    /**
     * 从缓存中删除指定键的记录
     *
     * @param key 键
     */
    public void remove(K key) {
        if (null == cacheManager.getCache(cacheName)) {
            return;
        }
        cacheManager.getCache(cacheName).remove(key);
    }

    /**
     * 清空缓存
     */
    public void clear() {
        if (null == cacheManager.getCache(cacheName)) {
            return;
        }
        cacheManager.getCache(cacheName).clear();
    }

    /**
     * 获取缓存中所有的键
     */
    public Set<K> keySet() {
        Cache cache = this.cacheManager.getCache(cacheName);
        if (null == cache) {
            return new HashSet<>();
        }
        Iterator it = cache.iterator();
        Set<K> set = new HashSet<>();
        while (it.hasNext()) {
            Cache.Entry<K, V> entry = (Cache.Entry<K, V>) it.next();
            set.add((K) entry.getKey());
        }
        return set;
    }

    /**
     * 获取线程中所有的值
     */
    public List<V> values() {
        if (cacheManager == null || null == cacheManager.getCache(cacheName)) {
            return new ArrayList<>();
        }
        Iterator it = cacheManager.getCache(cacheName).iterator();
        List<V> list = new ArrayList<>();
        while (it.hasNext()) {
            Cache.Entry<K, V> entry = (Cache.Entry<K, V>) it.next();
            V t = entry.getValue();
            list.add(t);
        }
        return list;
    }

    /**
     * 销毁当前缓存
     */
    public void destroy() {
        this.cacheManager.removeCache(cacheName);
    }
}
