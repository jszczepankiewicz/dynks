package dynks.redis;

import dynks.cache.CacheQueryResult;
import dynks.cache.CacheRepository;
import dynks.cache.Entry;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static dynks.cache.Entry.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by jszczepankiewicz on 2015-04-01.
 */
public class RedisCacheRepository implements CacheRepository {

    private static final Logger LOG = getLogger(RedisCacheRepository.class);
    private final JedisPool pool;
    private final JedisPoolConfig poolConfig;
    private final String host;
    private final int port;

    public static final CacheQueryResult NO_RESULT_FOUND = new CacheQueryResult(true, null, null, null, null);
    private static final CacheQueryResult RESULT_FOUND_BUT_NOT_CHANGED = new CacheQueryResult(false, null, null, null, null);

    public RedisCacheRepository(JedisPoolConfig poolConfig, String host, int port){

        this.host = host;
        this.port = port;
        this.poolConfig = poolConfig;
        this.pool = new JedisPool(poolConfig, host, port);
    }

    /**
     * Return entry assuming exist. If not then it reacts as it would not exist.
     * @param key
     * @return
     */
    private CacheQueryResult getEntryAssumingCached(Jedis jedis, String key){

        Map<String, String> out = jedis.hgetAll(key);

        /*
            According to the documentation of redis hgetAll should return null when
            key not found. But at least version 2.8.19 returns empty map for not existing key
            that's why I apply double check.
         */
        if(out == null || out.isEmpty()){
            return NO_RESULT_FOUND;
        }

        return new CacheQueryResult(false, out.get(PAYLOAD), out.get(ETAG), out.get(CONTENT_TYPE), out.get(ENCODING));
    }

    @Override
    public CacheQueryResult fetchIfChanged(String key, String etag) {

        if(key == null){
            throw new IllegalArgumentException("Key to upsert should not be null");
        }

        if(key.trim().length()==0){
            throw new IllegalArgumentException("Key to upsert should not be empty");
        }

        try (Jedis jedis = pool.getResource()) {

            //  client does not have any version, query for both content + etag
            if(etag == null){
                return getEntryAssumingCached(jedis, key);
            }

            /*
                get value of etag assuming key exists. This is less costly as checking if key exists and get
             */
            String cachedEtag = jedis.hget(key, ETAG);

            System.out.println("cachedEtag: " + cachedEtag);

            if(cachedEtag == null){
                return NO_RESULT_FOUND;
            }

            if(cachedEtag.equals(etag)){
                return RESULT_FOUND_BUT_NOT_CHANGED;
            }

            /*
              entry in cache different, we assume cached entry is newer than on client side
              we need also to take into consideration that durint last check above entry expired
              thus may not exist when queried for full content.
             */
            return getEntryAssumingCached(jedis, key);
        }
    }

    @Override
    public void upsert(String key, String content, String etag, String contentType, String encoding, long ttl, TimeUnit ttlUnit) {

        try (Jedis jedis = pool.getResource()) {
            // TODO: transform to multicomand to improve perfomance
            jedis.hmset(key, new Entry(content, etag, contentType, encoding));
            if(ttl>0){
                jedis.expire(key, (int)ttlUnit.toSeconds(ttl));
            }
        }
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public void dispose() {
        LOG.info("Destroying redis connection pool...");

        if(pool != null){
            pool.destroy();
        }
    }

    public JedisPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
