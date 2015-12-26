package dynks.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static dynks.redis.RedisCacheRepositoryConfigBuilder.DEFAULT_MAX_ENTRIES_DELETED_IN_ONE_BATCH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Accessible in tests only builder for RedisCacheRepository.
 *
 * @author jszczepankiewicz
 * @since 2015-12-25
 */
public class RedisCacheRepositoryTestBuilder {

  public static class Builder {

    private JedisPool pool;
    private final Jedis jedis;
    private String host = "localhost";
    private int port = 99999;
    private int maxEntriesDeletedInOneBatch = DEFAULT_MAX_ENTRIES_DELETED_IN_ONE_BATCH;
    private JedisPoolConfig poolConfig = new JedisPoolConfig();

    public Builder(Jedis jedis) {
      this.jedis = jedis;
    }

    public RedisCacheRepository build() {
      pool = mock(JedisPool.class);
      when(pool.getResource()).thenReturn(jedis);
      return new RedisCacheRepository(poolConfig, host, port, maxEntriesDeletedInOneBatch, pool);
    }

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder maxEntriesDeletedInOneBatch(int maxEntriesDeletedInOneBatch) {
      this.maxEntriesDeletedInOneBatch = maxEntriesDeletedInOneBatch;
      return this;
    }

    public Builder poolConfig(JedisPoolConfig jedisPoolConfig) {
      this.poolConfig = poolConfig;
      return this;
    }
  }
}
