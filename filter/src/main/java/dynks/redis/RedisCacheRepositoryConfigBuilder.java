package dynks.redis;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPoolConfig;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author jszczepankiewicz
 * @since 2015-04-01.
 */
public class RedisCacheRepositoryConfigBuilder {

  private static final Logger LOG = getLogger(RedisCacheRepositoryConfigBuilder.class);
  public static final int DEFAULT_MAX_ENTRIES_DELETED_IN_ONE_BATCH = 1000;

  public static RedisCacheRepository build(Config config) {

    String host = config.getString("dynks.redis.host");
    int port = config.getInt("dynks.redis.port");
    LOG.info("Will connect to redis at {}:{}", host, port);

    JedisPoolConfig poolConfig = new JedisPoolConfig();
    final String testOnBorrow = "dynks.redis.pool.testConnectionOnBorrow";
    if (config.hasPath(testOnBorrow)) {
      poolConfig.setTestOnBorrow(config.getBoolean(testOnBorrow));
    }
    LOG.debug("\tTestOnBorrow: {}", poolConfig.getTestOnBorrow());

    final String testOnReturn = "dynks.redis.pool.testConnectionOnReturn";
    if (config.hasPath(testOnReturn)) {
      poolConfig.setTestOnReturn(config.getBoolean(testOnReturn));
    }
    LOG.debug("\tTestOnReturn: {}", poolConfig.getTestOnReturn());

    final String testWhileIdle = "dynks.redis.pool.testWhileIdle";
    if (config.hasPath(testWhileIdle)) {
      poolConfig.setTestWhileIdle(config.getBoolean(testWhileIdle));
    }
    LOG.debug("\tTestWhileIdle: {}", poolConfig.getTestWhileIdle());

    final String maxConnections = "dynks.redis.pool.maxTotalConnectionsToCache";
    if (config.hasPath(maxConnections)) {
      poolConfig.setMaxTotal(config.getInt(maxConnections));
    }
    LOG.debug("\tMaxTotal: {}", poolConfig.getMaxTotal());

    final String maxIdle = "dynks.redis.pool.maxIdle";
    if (config.hasPath(maxIdle)) {
      poolConfig.setMaxIdle(config.getInt(maxIdle));
    }
    LOG.debug("\tMaxIdle: {}", poolConfig.getMaxIdle());

    final String minIdle = "dynks.redis.pool.minIdle";
    if (config.hasPath(minIdle)) {
      poolConfig.setMinIdle(config.getInt(minIdle));
    }
    LOG.debug("\tMinIdle: {}", poolConfig.getMinIdle());

    final String numTestsPerEviction = "dynks.redis.pool.numberOfTestsPerEvictionRun";
    if (config.hasPath(numTestsPerEviction)) {
      poolConfig.setNumTestsPerEvictionRun(config.getInt(numTestsPerEviction));
    }
    LOG.debug("\tNumTestsPerEvictionRun: {}", poolConfig.getNumTestsPerEvictionRun());

    final String timeBetweenEviction = "dynks.redis.pool.msBetweenEvictionRuns";
    if (config.hasPath(timeBetweenEviction)) {
      poolConfig.setTimeBetweenEvictionRunsMillis(config.getInt(timeBetweenEviction));
    }

    LOG.debug("\tTimeBetweenEvictionRunsMillis: {}", poolConfig.getTimeBetweenEvictionRunsMillis());

    final int maxEntriesDeletedInOneBatch = config.hasPath("dynks.redis.maxEntriesDeletedInOneBatch") ? config.getInt("dynks.redis.maxEntriesDeletedInOneBatch")
            : DEFAULT_MAX_ENTRIES_DELETED_IN_ONE_BATCH;
    if (maxEntriesDeletedInOneBatch < 1) {
      throw new IllegalArgumentException("maxEntriesDeletedInOneBatch should be >=1 but is '" + maxEntriesDeletedInOneBatch + "'");
    }
    LOG.debug("\tMaxEntriesDeletedInOneBatch: {}", maxEntriesDeletedInOneBatch);

    return new RedisCacheRepository(poolConfig, host, port, maxEntriesDeletedInOneBatch);
  }
}
