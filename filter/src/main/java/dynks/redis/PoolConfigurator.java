package dynks.redis;

import org.slf4j.Logger;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.FilterConfig;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Utility to help configuring Jedis pool.
 */
public class PoolConfigurator {

    private static final Logger LOG = getLogger(PoolConfigurator.class);

    public static final String MAX_TOTAL_CONNECTIONS_TO_CACHE = "MAX_TOTAL_CONNECTIONS_TO_CACHE";
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS_TO_CACHE = 20;
    public static final String TEST_CONNECTION_ON_BORROW = "TEST_CONNECTION_ON_BORROW";
    public static final boolean DEFAULT_TEST_CONNECTION_ON_BORROW = true;
    public static final String TEST_CONNECTION_ON_RETURN = "TEST_CONNECTION_ON_RETURN";
    public static final boolean DEFAULT_TEST_CONNECTION_ON_RETURN = false;
    public static final String MAX_IDLE = "MAX_IDLE";
    public static final int DEFAULT_MAX_IDLE = 5;
    public static final String MIN_IDLE = "MIN_IDLE";
    public static final int DEFAULT_MIN_IDLE = 5;
    public static final String TEST_WHILE_IDLE = "TEST_WHILE_IDLE";
    public static final boolean DEFAULT_TEST_WHILE_IDLE = false;
    public static final String NUM_TESTS_PER_EVICTION_RUN = "NUM_TESTS_PER_EVICTION_RUN";
    public static final int DEFAULT_NUM_TESTS_PER_EVICTION_RUN = 10;
    public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "TIME_BETWEEN_EVICTION_RUNS_MILLIS";
    public static final int DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 10000;

    public static JedisPoolConfig configure(FilterConfig config) {

        if (config == null) {
            throw new NullPointerException("FilterConfig passed to configure cache filter should not be null");
        }

        LOG.debug("Redis connection pool configuration:");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(getOrDefault(config, TEST_CONNECTION_ON_BORROW, DEFAULT_TEST_CONNECTION_ON_BORROW));
        LOG.debug("\tTestOnBorrow: {}", poolConfig.getTestOnBorrow());
        poolConfig.setTestOnReturn(getOrDefault(config, TEST_CONNECTION_ON_RETURN, DEFAULT_TEST_CONNECTION_ON_RETURN));
        LOG.debug("\tTestOnReturn: {}", poolConfig.getTestOnReturn());
        poolConfig.setTestWhileIdle(getOrDefault(config, TEST_WHILE_IDLE, DEFAULT_TEST_WHILE_IDLE));
        LOG.debug("\tTestWhileIdle: {}", poolConfig.getTestWhileIdle());
        poolConfig.setMaxTotal(getOrDefault(config, MAX_TOTAL_CONNECTIONS_TO_CACHE, DEFAULT_MAX_TOTAL_CONNECTIONS_TO_CACHE));
        LOG.debug("\tMaxTotal: {}", poolConfig.getMaxTotal());
        poolConfig.setMaxIdle(getOrDefault(config, MAX_IDLE, DEFAULT_MAX_IDLE));
        LOG.debug("\tMaxIdle: {}", poolConfig.getMaxIdle());
        poolConfig.setMinIdle(getOrDefault(config, MIN_IDLE, DEFAULT_MIN_IDLE));
        LOG.debug("\tMinIdle: {}", poolConfig.getMinIdle());
        poolConfig.setNumTestsPerEvictionRun(getOrDefault(config, NUM_TESTS_PER_EVICTION_RUN, DEFAULT_NUM_TESTS_PER_EVICTION_RUN));
        LOG.debug("\tNumTestsPerEvictionRun: {}", poolConfig.getNumTestsPerEvictionRun());
        poolConfig.setTimeBetweenEvictionRunsMillis(getOrDefault(config, TIME_BETWEEN_EVICTION_RUNS_MILLIS, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS));
        LOG.debug("\tTimeBetweenEvictionRunsMillis: {}", poolConfig.getTimeBetweenEvictionRunsMillis());

        return poolConfig;
    }

    private static int getOrDefault(FilterConfig config, String param, int defaultValue) {
        String val = config.getInitParameter(param);
        return (val != null) ? Integer.valueOf(val) : defaultValue;
    }

    private static boolean getOrDefault(FilterConfig config, String param, boolean defaultValue) {
        String val = config.getInitParameter(param);
        return (val != null) ? Boolean.valueOf(val) : defaultValue;
    }


}
