package dynks.redis;

import dynks.cache.CacheRegion;
import dynks.cache.CacheRepositoryException;
import dynks.cache.Entry;
import dynks.cache.NamespacedURIKeyStrategy;
import dynks.redis.RedisCacheRepositoryTestBuilder.Builder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for RedisCacheRepository assuming errors are occuring in repository layer.
 *
 * @author jszczepankiewicz
 * @since 2015-12-25
 */
public class RedisCacheRepositoryHardenedTest {

  private Jedis jedis;

  @Rule
  public ExpectedException thrown = none();

  @Before
  public void setUp() {
    jedis = mock(Jedis.class);
    thrown.expect(CacheRepositoryException.class);
    thrown.expectCause(isA(RuntimeException.class));
  }

  @Test
  public void throwCREonExceptionInFetchIfChangedDueToJedisHGetAll() throws CacheRepositoryException {

    //  given
    when(jedis.hgetAll(anyString())).thenThrow(jedisException());
    RedisCacheRepository repo = new Builder(jedis).build();

    //  when
    repo.fetchIfChanged("some", null);
  }

  @Test
  public void throwCREonExceptionInFetchIfChangedDueToJedisHGet() throws CacheRepositoryException {

    //  given
    when(jedis.hget(anyString(), anyString())).thenThrow(jedisException());
    RedisCacheRepository repo = new Builder(jedis).build();

    //  when
    repo.fetchIfChanged("some", "else");
  }

  @Test
  public void throwCREOnExceptionInRemoveDueToJedisDel() throws CacheRepositoryException {

    //  given
    when(jedis.del(anyString())).thenThrow(jedisException());
    RedisCacheRepository repo = new Builder(jedis).build();

    //  when
    repo.remove("something");
  }

  @Test
  public void throwCREOnExceptionInUpsertDueToJedisHmset() throws CacheRepositoryException {

    //  given
    when(jedis.hmset(anyString(), any(Entry.class))).thenThrow(jedisException());
    RedisCacheRepository repo = new Builder(jedis).build();

    //  when
    repo.upsert("key", "content", "etag", "plain/text", "ASCII", regionFor(0, HOURS));
  }

  @Test
  public void throwCREOnExceptionInUpsertDueToJedisMulti() throws CacheRepositoryException {

    //  given
    when(jedis.multi()).thenThrow(jedisException());
    RedisCacheRepository repo = new Builder(jedis).build();

    //  when
    repo.upsert("key", "content", "etag", "plain/text", "ASCII", regionFor(1, HOURS));
  }

  @Test
  public void throwCREOnExceptionInEvictRegionDueToJedisEvalsha() throws CacheRepositoryException {

    //  given
    when(jedis.evalsha(anyString(), anyInt(), anyString(), anyString())).thenThrow(jedisException());
    RedisCacheRepository repo = new Builder(jedis).build();

    //  when
    repo.evictRegion(regionFor(1, HOURS));
  }

  @Test
  public void throwCREOnExceptionInEvictRegionWithMaxEntriesDeletedInOneBatchDueToJedisEvalsha() throws CacheRepositoryException {

    //  given
    when(jedis.evalsha(anyString(), anyInt(), anyString(), anyString())).thenThrow(jedisException());
    RedisCacheRepository repo = new Builder(jedis).build();

    //  when
    repo.evictRegion(regionFor(1, HOURS), 100);
  }

  private Throwable jedisException() {
    return new RuntimeException("internal jedis exception");
  }

  private CacheRegion regionFor(long ttl, TimeUnit unit) {
    return new CacheRegion("test", ttl, unit, new NamespacedURIKeyStrategy("test"));
  }

}