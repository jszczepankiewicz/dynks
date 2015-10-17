package dynks.cache;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dynks.redis.RedisCacheRepository;
import dynks.redis.RedisCacheRepositoryConfigBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static dynks.cache.test.DynksAssertions.assertThat;
import static org.junit.rules.ExpectedException.none;

/**
 * @author jszczepankiewicz
 * @since 2015-04-01.
 */
public class RedisCacheRepositoryConfigBuilderTest {

  @Rule
  public ExpectedException thrown = none();

  @Test
  public void loadConfiguration() {

    //  given
    Config conf = ConfigFactory.load("dynks-test");

    //  when
    RedisCacheRepository repo = RedisCacheRepositoryConfigBuilder.build(conf);

    //  then
    assertThat(repo.getPoolConfig())
            .hasMaxIdle(6)
            .hasMinIdle(6)
            .hasMaxTotal(21)
            .hasNumTestsPerEvictionRun(11)
            .hasTimeBetweenEvictionRunsMillis(10001)
            .isNotTestOnBorrow()
            .isTestOnReturn()
            .isTestWhileIdle();

    assertThat(repo.getMaxEntriesDeletedInOneBatch()).isEqualTo(2000);
    assertThat(repo.getHost()).isEqualTo("192.168.0.21");
    assertThat(repo.getPort()).isEqualTo(222);

  }

  @Test
  public void loadDefaultConfiguration() {

    //  given
    //  in fact this is reading default values from default JedisConfig
    Config conf = ConfigFactory.load("dynks-default-test");

    //  when
    RedisCacheRepository repo = RedisCacheRepositoryConfigBuilder.build(conf);

    //  then
    assertThat(repo.getPoolConfig())
            .hasMaxIdle(8)
            .hasMinIdle(0)
            .hasMaxTotal(8)
            .hasNumTestsPerEvictionRun(-1)
            .hasTimeBetweenEvictionRunsMillis(30000)
            .isNotTestOnBorrow()
            .isNotTestOnReturn()
            .isTestWhileIdle();

    assertThat(repo.getHost()).isEqualTo("localhost");
    assertThat(repo.getPort()).isEqualTo(6379);
  }

  @Test
  public void throwIAEOnMaxEntriesDeletedInOneBatchBelowOne() {

    //  given
    Config conf = ConfigFactory.load("dynks-test-max-delete-invalid");

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("maxEntriesDeletedInOneBatch should be >=1 but is '0'");

    //  when
    RedisCacheRepositoryConfigBuilder.build(conf);
  }

  @Test
  public void loadDefaultMaxEntriesDeletedInOneBatchIfNotProvidedInConf() {

    //  given
    Config conf = ConfigFactory.load("dynks-test-max-delete-not-provided");

    //  when
    RedisCacheRepository repo = RedisCacheRepositoryConfigBuilder.build(conf);

    //  then
    assertThat(repo.getMaxEntriesDeletedInOneBatch()).isEqualTo(1000);
  }
}