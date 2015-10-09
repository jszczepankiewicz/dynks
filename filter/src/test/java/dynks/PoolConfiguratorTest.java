package dynks;

import dynks.redis.PoolConfigurator;
import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.FilterConfig;

import static java.lang.Integer.valueOf;
import static java.lang.String.valueOf;
import static dynks.JedisPoolConfigAssert.assertThat;
import static dynks.redis.PoolConfigurator.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PoolConfiguratorTest {

    private static final String CUSTOM_TEST_BORROW = valueOf(!DEFAULT_TEST_CONNECTION_ON_BORROW);
    private static final String CUSTOM_TEST_RETURN = valueOf(!DEFAULT_TEST_CONNECTION_ON_RETURN);
    private static final String CUSTOM_TEST_WHILE_IDLE = valueOf(!DEFAULT_TEST_WHILE_IDLE);
    private static final String CUSTOM_MAX_CONNECTIONS = "99";
    private static final String CUSTOM_MAX_IDLE = "10";
    private static final String CUSTOM_MIN_IDLE = "7";
    private static final String CUSTOM_NUM_TESTS_PER_EVICTION_RUN = "333";
    private static final String CUSTOM_TIME_BETWEEN_EVICTION_RUNS_MILLIS = "977";

    @Test
    public void shouldInitializePoolWithDefaultValues() {

        //  given & when
        JedisPoolConfig pool = PoolConfigurator.configure(withNothingCustomized());

        //  then
        assertThat(pool)
                .hasMaxIdle(DEFAULT_MAX_IDLE)
                .hasMinIdle(DEFAULT_MIN_IDLE)
                .hasMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS_TO_CACHE)
                .hasNumTestsPerEvictionRun(DEFAULT_NUM_TESTS_PER_EVICTION_RUN)
                .hasTimeBetweenEvictionRunsMillis(DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS)
                .isTestOnBorrow()
                .isNotTestOnReturn()
                .isNotTestWhileIdle();
    }

    @Test
    public void shouldInitializePoolWithProvidedValues() {

        //  given & when
        JedisPoolConfig pool = PoolConfigurator.configure(withAllCustomized());

        //  then
        assertThat(pool)
                .hasMaxIdle(valueOf(CUSTOM_MAX_IDLE))
                .hasMinIdle(valueOf(CUSTOM_MIN_IDLE))
                .hasMaxTotal(valueOf(CUSTOM_MAX_CONNECTIONS))
                .hasNumTestsPerEvictionRun(valueOf(CUSTOM_NUM_TESTS_PER_EVICTION_RUN))
                .hasTimeBetweenEvictionRunsMillis(valueOf(CUSTOM_TIME_BETWEEN_EVICTION_RUNS_MILLIS))
                .isNotTestOnBorrow()
                .isTestOnReturn()
                .isTestWhileIdle();
    }

    private FilterConfig withNothingCustomized() {
        FilterConfig config = mock(FilterConfig.class);
        when(config.getInitParameter(any(String.class))).thenReturn(null);
        return config;
    }

    private FilterConfig withAllCustomized() {
        FilterConfig config = mock(FilterConfig.class);
        when(config.getInitParameter(MAX_TOTAL_CONNECTIONS_TO_CACHE)).thenReturn(CUSTOM_MAX_CONNECTIONS);
        when(config.getInitParameter(MAX_IDLE)).thenReturn(CUSTOM_MAX_IDLE);
        when(config.getInitParameter(TEST_CONNECTION_ON_BORROW)).thenReturn(CUSTOM_TEST_BORROW);
        when(config.getInitParameter(TEST_CONNECTION_ON_RETURN)).thenReturn(CUSTOM_TEST_RETURN);
        when(config.getInitParameter(MIN_IDLE)).thenReturn(CUSTOM_MIN_IDLE);
        when(config.getInitParameter(TEST_WHILE_IDLE)).thenReturn(CUSTOM_TEST_WHILE_IDLE);
        when(config.getInitParameter(NUM_TESTS_PER_EVICTION_RUN)).thenReturn(CUSTOM_NUM_TESTS_PER_EVICTION_RUN);
        when(config.getInitParameter(TIME_BETWEEN_EVICTION_RUNS_MILLIS)).thenReturn(CUSTOM_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        return config;
    }

}