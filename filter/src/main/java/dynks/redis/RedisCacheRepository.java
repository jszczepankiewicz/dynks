package dynks.redis;

import dynks.cache.CacheQueryResult;
import dynks.cache.CacheRegion;
import dynks.cache.CacheRepository;
import dynks.cache.CacheRepositoryException;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

import java.util.Map;

import static dynks.cache.Entry.*;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author jszczepankiewicz
 * @since 2015-04-01.
 */
public class RedisCacheRepository implements CacheRepository {

  private static final Logger LOG = getLogger(RedisCacheRepository.class);

  private final DeleteAllEntriesInRegionCommand deleteCommand = new DeleteAllEntriesInRegionCommand();
  private final JedisPool pool;
  private final JedisPoolConfig poolConfig;
  private final String host;
  private final int port;
  private final int maxEntriesDeletedInOneBatch;

  public static final CacheQueryResult NO_RESULT_FOUND = new CacheQueryResult(true, null, null, null, null);
  private static final CacheQueryResult RESULT_FOUND_BUT_NOT_CHANGED = new CacheQueryResult(false, null, null, null, null);

  /**
   * Constructor of RedisCacheRepository. It should be created only internally so that default access specified.
   * TODO: do we really need all of this passed / stored at field level?
   *
   * @param poolConfig
   * @param host
   * @param port
   * @param maxEntriesDeletedInOneBatch
   */
  RedisCacheRepository(JedisPoolConfig poolConfig, String host, int port, int maxEntriesDeletedInOneBatch, JedisPool pool) {

    this.host = host;
    this.port = port;
    this.poolConfig = poolConfig;
    this.pool = pool;
    this.maxEntriesDeletedInOneBatch = maxEntriesDeletedInOneBatch;
  }

  @Override
  public int getMaxEntriesDeletedInOneBatch() {
    return maxEntriesDeletedInOneBatch;
  }

  /**
   * Return entry assuming exist. If not then it reacts as it would not exist.
   *
   * @param key
   * @return
   */
  private CacheQueryResult getEntryAssumingCached(Jedis jedis, String key) {

    Map<String, String> out = jedis.hgetAll(key);

        /*
            According to the documentation of redis hgetAll should return null when
            key not found. But at least version 2.8.19 returns empty map for not existing key
            that's why I apply double check.
         */
    if (out == null || out.isEmpty()) {
      return NO_RESULT_FOUND;
    }

    return new CacheQueryResult(false, out.get(PAYLOAD), out.get(ETAG), out.get(CONTENT_TYPE), out.get(ENCODING));
  }

  @Override
  public CacheQueryResult fetchIfChanged(String key, String etag) throws CacheRepositoryException {

    try {

      if (key == null) {
        throw new IllegalArgumentException("Key to upsert should not be null");
      }

      if (key.trim().length() == 0) {
        throw new IllegalArgumentException("Key to upsert should not be empty");
      }

      try (Jedis jedis = pool.getResource()) {

        //  client does not have any version, query for both content + etag
        if (etag == null) {
          return getEntryAssumingCached(jedis, key);
        }

        // get value of etag assuming key exists. This is less costly as checking if key exists and get
        String cachedEtag = jedis.hget(key, ETAG);

        if (cachedEtag == null) {
          return NO_RESULT_FOUND;
        }

        if (cachedEtag.equals(etag)) {
          return RESULT_FOUND_BUT_NOT_CHANGED;
        }

        /*
          entry in cache different, we assume cached entry is newer than on client side
          we need also to take into consideration that durint last check above entry expired
          thus may not exist when queried for full content.
        */
        return getEntryAssumingCached(jedis, key);
      }
    } catch (Exception e) {
      throw new CacheRepositoryException(e);
    }
  }

  @Override
  public void upsert(String key, String content, String etag, String contentType, String encoding, CacheRegion region) throws CacheRepositoryException {
    try {
      try (Jedis jedis = pool.getResource()) {

        if (region.getTtl() == 0) {
          jedis.hmset(key, new dynks.cache.Entry(content, etag, contentType, encoding));
        } else {
          Transaction t = jedis.multi();
          t.hmset(key, new dynks.cache.Entry(content, etag, contentType, encoding));
          t.expire(key, region.getTtlInSeconds());
          t.exec();
        }
      }
    } catch (Exception e) {
      throw new CacheRepositoryException(e);
    }
  }

  @Override
  public void remove(String key) throws CacheRepositoryException {

    try {
      try (Jedis jedis = pool.getResource()) {
        jedis.del(key);
      }
    } catch (Exception e) {
      throw new CacheRepositoryException(e);
    }
  }

  @Override
  public long evictRegion(CacheRegion region) throws CacheRepositoryException {
    try {
      return evictRegion(region, maxEntriesDeletedInOneBatch);
    } catch (CacheRepositoryException e) {
      throw e;
    } catch (Exception e) {
      throw new CacheRepositoryException(e);
    }
  }

  @Override
  public long evictRegion(CacheRegion region, int maxEntriesDeletedInOneBatch) throws CacheRepositoryException {
    try {
      long start = nanoTime();
      Long removed = 0L;
      try (Jedis jedis = pool.getResource()) {
        removed = deleteCommand.execute(jedis, region, maxEntriesDeletedInOneBatch);
      }

      LOG.debug("Evicted {} entries from region '{}' in {} ms ", removed, region.getId(), NANOSECONDS.toMillis(nanoTime() - start));
      return removed;
    } catch (Exception e) {
      throw new CacheRepositoryException(e);
    }
  }


  @Override
  public void dispose() {
    LOG.info("Disposing redis connection pool...");
    pool.destroy();
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
