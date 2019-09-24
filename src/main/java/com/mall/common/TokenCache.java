package com.mall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * 设置token缓存
 */
public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    //google guva LUR
    // initialCapacity 初始化容量，
    // maximumSize 最大容量 当超过最大容量就使用LUR算法
    //expireAfterAccess 缓存有效期 12小时
    private static LoadingCache<String, String> loadingCache =
            CacheBuilder.newBuilder().initialCapacity(1000)
                    .maximumSize(10000)
                    .expireAfterAccess(12, TimeUnit.HOURS)
                    .build(new CacheLoader<String, String>() {
                        //默认的数据加载实现，当调用get曲直的时候，如果key没有对应的值，就调用这个方法进行加载
                        @Override
                        public String load(String s) throws Exception {
                            return "null";
                        }
                    });

    public static void setKey(String key, String value) {
        loadingCache.put(key, value);
    }

    public static String getKey(String key) {
        String value = null;

        try {
            value = loadingCache.get(key);
            if (value.equals("null")) {
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("localCache get error", e);
        }
        return null;
    }

    public static void cleatToken(String key) {
        loadingCache.put(key, null);
    }

    public static void cleatToken() {
        loadingCache.cleanUp();
    }


}
