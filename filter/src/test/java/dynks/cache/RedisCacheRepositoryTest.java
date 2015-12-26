package dynks.cache;

import com.typesafe.config.ConfigFactory;
import dynks.cache.test.CauseMatcher;
import dynks.redis.RedisCacheRepository;
import dynks.redis.RedisCacheRepositoryConfigBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static dynks.cache.Entry.*;
import static dynks.cache.TestValues.UTF8;
import static dynks.cache.TestValues.UTF8_JSON;
import static dynks.cache.test.DynksAssertions.assertThat;
import static dynks.http.ETag.SIZEOF_ETAG;
import static dynks.http.ETag.of;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.core.util.Preconditions.checkNotNullOrEmpty;
import static org.junit.rules.ExpectedException.none;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Integration test that will assume that redis server for testing will be run on localhost on default port.
 * Every execution will do the cleanup on before test.
 */
public class RedisCacheRepositoryTest {

  @Rule
  public ExpectedException thrown = none();

  private static final Logger LOG = getLogger(RedisCacheRepositoryTest.class);

  private static final String JSON_SAVED = "{\"yourName\":\"alice\"}";
  private static final String KEY = "someKeyValue";

  private RedisCacheRepository repo;
  private StringBuilder etagBuilder = new StringBuilder(SIZEOF_ETAG);

  @Before
  public void cleanUpRedis() {
    repo = RedisCacheRepositoryConfigBuilder.build(ConfigFactory.load());

    //  clean up potential hashes
    try {
      getJedis().del(KEY);
    } catch (JedisConnectionException e) {
      LOG.error("connection exception while preparing integration test. Are you sure there is redis listening on default port on localhost?, details: ", e);
      throw e;
    }
  }

  @Test
  public void throwIAEOnNulledKeyForFetchIfChanged() throws CacheRepositoryException {

    //  then
    thrown.expect(CacheRepositoryException.class);
    thrown.expectCause(new CauseMatcher(IllegalArgumentException.class, "Key to upsert should not be null"));

    //  when
    repo.fetchIfChanged(null, "someEtag");
  }

  @Test
  public void throwIAEOnNulledKeyForFetchIfChangedNulledEtag() throws CacheRepositoryException {

    //  then
    thrown.expect(CacheRepositoryException.class);
    thrown.expectCause(new CauseMatcher(IllegalArgumentException.class, "Key to upsert should not be null"));

    //  when
    repo.fetchIfChanged(null, null);
  }

  @Test
  public void throwIAEOnEmptyKeyForFetchIfChangedNulledEtag() throws CacheRepositoryException {

    //  then
    thrown.expect(CacheRepositoryException.class);
    thrown.expectCause(new CauseMatcher(IllegalArgumentException.class, "Key to upsert should not be empty"));

    //  when
    repo.fetchIfChanged(" ", null);
  }

  @Test
  public void throwIAEOnEmptyKeyForFetchIfChanged() throws CacheRepositoryException {

    //  then
    thrown.expect(CacheRepositoryException.class);
    thrown.expectCause(new CauseMatcher(IllegalArgumentException.class, "Key to upsert should not be empty"));

    //  when
    repo.fetchIfChanged(" ", "someEtag");
  }

  @Test
  public void upsertValueIfNotExistForEthernalLife() throws CacheRepositoryException {

    //  given
    String etag = of(JSON_SAVED, etagBuilder);

    //  when
    repo.upsert(KEY, JSON_SAVED, etag, UTF8_JSON, UTF8, regionFor(0, HOURS));

    //  then
    assertValueExist(KEY, etag, JSON_SAVED, UTF8_JSON, UTF8);

  }

  @Test
  public void upsertValueIfNotExist() throws CacheRepositoryException {

    //  given
    String etag = of(JSON_SAVED, etagBuilder);

    //  when
    repo.upsert(KEY, JSON_SAVED, etag, UTF8_JSON, UTF8, regionFor(999, HOURS));

    //  then
    assertValueExist(KEY, etag, JSON_SAVED, UTF8_JSON, UTF8);
  }

  @Test
  public void utf8UpsertShouldReturnCorrectValuesFromCache() throws CacheRepositoryException {

    //  given
    String payload = "ąśćźżęłóĄŚĆŻŹĘŁÓ";

    //  when
    repo.upsert(KEY, payload, "etag1", UTF8_JSON, UTF8, regionFor(999, HOURS));
    CacheQueryResult result = repo.fetchIfChanged(KEY, null);

    //  then
    assertThat(result.getPayload()).isEqualTo(payload);

  }

  @Test
  public void upsertValueEvenIfKeyExistsWithDifferentEtag() throws CacheRepositoryException {

    //  given
    havingEntryCached(KEY, JSON_SAVED, "someetagxyz", UTF8_JSON, UTF8);
    final String newContent = "[]";
    final String newEtag = of(newContent, etagBuilder);

    //  when
    repo.upsert(KEY, newContent, newEtag, UTF8_JSON, UTF8, regionFor(999, HOURS));

    //  then
    assertValueExist(KEY, newEtag, newContent, UTF8_JSON, UTF8);
  }

  //  FIXME: share with hardened test
  private CacheRegion regionFor(long ttl, TimeUnit unit) {
    return new CacheRegion("test", ttl, unit, new NamespacedURIKeyStrategy("test"));
  }

  /*
   * there is no value for given key, CacheResult will return: upsertNeeded: true, payload: null, storedEtag: null
   */
  @Test
  public void detectMissingEntryOnFetch() throws CacheRepositoryException {

    //  given
    final String key = "sk1";
    final String etag = "se1";

    //  when
    CacheQueryResult result = repo.fetchIfChanged(key, etag);

    //  then
    assertThat(result).hasPayload(null).hasStoredEtag(null).isUpsertNeeded();
  }

  /**
   * client does not provide etag (null), but value in cache also does not exist. Should return:
   * upsertNeeded: true, payload containsCacheable, storedEtag
   */

  @Test
  public void returnContentClientFirstTimeContentNotYetCached() throws CacheRepositoryException {

    //  when
    CacheQueryResult result = repo.fetchIfChanged(KEY, null);

    //  then
    assertThat(result).hasPayload(null).hasStoredEtag(null).isUpsertNeeded();

  }

  /*
   * there is value for given key but etag == null, CacheResult will return: upsertNeeded: false, payload containsCacheable the value,
   * storedEtag: corresponding etag value associated with given key
   */
  @Test
  public void returnEntryFromCacheWhenEtagUnknown() throws CacheRepositoryException {

    //  given
    String etagExisting = of(JSON_SAVED, etagBuilder);
    havingEntryCached(KEY, JSON_SAVED, etagExisting, UTF8_JSON, UTF8);

    //  when
    CacheQueryResult result = repo.fetchIfChanged(KEY, null);

    //  then
    assertThat(result).hasPayload(JSON_SAVED).hasStoredEtag(etagExisting).isUpsertNotNeeded();
  }

  /*
   *  there is value for given key and etag == storedEtag for given value, CacheResult will return: upsertNeeded: false,
   *  payload contain null, storedEtag: null
   */
  @Test
  public void fetchNotNeededAsCachedVersionNotChanged() throws CacheRepositoryException {

    //  given
    String etagExisting = of(JSON_SAVED, etagBuilder);
    havingEntryCached(KEY, JSON_SAVED, etagExisting, UTF8_JSON, UTF8);

    //  when
    CacheQueryResult result = repo.fetchIfChanged(KEY, etagExisting);

    //  then
    assertThat(result).hasPayload(null).hasStoredEtag(null).isUpsertNotNeeded();
  }

  /*
   * there is value for given key and etag != storedEtag for given value, CacheResult will return: upsertNeeded: false,
   * payload contain latest version, storedEtag: etag corresponding with given value
   */
  @Test
  public void fetchReturnedNewerEntry() throws CacheRepositoryException {

    //  given
    String etagExisting = of(JSON_SAVED, etagBuilder);
    havingEntryCached(KEY, JSON_SAVED, etagExisting, UTF8_JSON, UTF8);

    //  when
    CacheQueryResult result = repo.fetchIfChanged(KEY, "someolderetag");

    //  then
    assertThat(result).hasPayload(JSON_SAVED).hasStoredEtag(etagExisting).isUpsertNotNeeded();
  }

  @Test
  public void evictMassiveRegionWithSmallestMaxDeletedInOneBatch() throws CacheRepositoryException {

    //  given
    CacheRegion perf = forRegion("perf");
    for (int i = 0; i < 16003; i++) {
      repo.upsert("tst:perf:perf" + i, "something unusually great abcdefgxihsdfrodfl", "something", UTF8_JSON, UTF8, perf);
    }

    //  when
    long removed = repo.evictRegion(perf, 1);

    //  then
    assertThat(removed).isEqualTo(16003);
    assertValueNotExist("tst:perf:perf0");
    assertValueNotExist("tst:perf:perf15000");

  }

  /**
   * This test may run > 10 sec
   */
  @Test
  public void evictMassiveRegionWithDefaultMaxDeletedInOneBatch() throws CacheRepositoryException {

    //  given
    CacheRegion perf = forRegion("perf");
    for (int i = 0; i < 110003; i++) {
      repo.upsert("tst:perf:perf" + i, "something unusually great abcdefgxihsdfrodfl", "something", UTF8_JSON, UTF8, perf);
    }

    //  when
    long removed = repo.evictRegion(perf);

    //  then
    assertThat(removed).isEqualTo(110003);
    assertValueNotExist("tst:perf:perf0");
    assertValueNotExist("tst:perf:perf110002");

  }

  @Test
  public void evictEmptyRegion() throws CacheRepositoryException {

    //  given
    CacheRegion emptyRegion = forRegion("emptyRegion");

    //  when
    long removed = repo.evictRegion(emptyRegion);

    //  then
    assertThat(removed).isEqualTo(0);
  }

  @Test
  public void evictAllEntriesFromRegion() throws CacheRepositoryException {

    //  given
    CacheRegion users = forRegion("users");
    CacheRegion logs = forRegion("logs");
    CacheRegion books = forRegion("books");

    repo.upsert("tst:users:user1", "somecontent", "someetag", UTF8_JSON, UTF8, users);
    repo.upsert("tst:users:user2", "somecontent", "someetag", UTF8_JSON, UTF8, users);
    repo.upsert("tst:logs:logs1", "something", "someetag", UTF8_JSON, UTF8, logs);
    repo.upsert("tst:logs:logs2", "something", "someetag", UTF8_JSON, UTF8, logs);
    repo.upsert("tst:books:book1", "something", "someetag", UTF8_JSON, UTF8, books);

    //  when
    repo.evictRegion(logs);
    repo.evictRegion(books);

    //  then
    assertValueNotExist("tst:logs:logs1");
    assertValueNotExist("tst:logs:logs2");
    assertValueExist("tst:users:user1");
    assertValueExist("tst:users:user2");
    assertValueNotExist("tst:books:book1");
  }


  @Test
  public void removeKeyIfExist() throws CacheRepositoryException {

    //  given
    String etagExisting = of(JSON_SAVED, etagBuilder);
    havingEntryCached(KEY, JSON_SAVED, etagExisting, UTF8_JSON, UTF8);

    //  when
    repo.remove(KEY);

    //  then
    assertValueNotExist(KEY);

  }

  //  test utils

  private Jedis getJedis() {
    return new Jedis("localhost");
  }

  //FIXME: invoke upsert inse
  private void havingEntryCached(String key, String content, String etag, String contentType, String encoding) {
    Jedis jedis = getJedis();
    jedis.hmset(key, new Entry(content, etag, contentType, encoding));
  }

  private CacheRegion forRegion(String id) {
    final NamespacedURIKeyStrategy strategy = new NamespacedURIKeyStrategy("tst");
    return new CacheRegion(id, 1800000, TimeUnit.MILLISECONDS, strategy);
  }

  private void assertValueNotExist(String key) {
    checkNotNullOrEmpty(key);

    Jedis jedis = getJedis();
    Map<String, String> out = jedis.hgetAll(key);

    assertThat(out).isEmpty();
  }

  private void assertValueExist(String key) {
    checkNotNullOrEmpty(key);
    Jedis jedis = getJedis();
    Map<String, String> out = jedis.hgetAll(key);

    assertThat(out).isNotEmpty()
            .hasSize(4);
  }

  private void assertValueExist(String key, String expectedEtag, String expectedContent, String expectedContentType, String expectedEncoding) {

    checkNotNullOrEmpty(key);
    Jedis jedis = getJedis();
    Map<String, String> out = jedis.hgetAll(key);

    assertThat(out).isNotEmpty()
            .hasSize(4)
            .contains(entry(PAYLOAD, expectedContent))
            .contains(entry(ETAG, expectedEtag))
            .contains(entry(CONTENT_TYPE, expectedContentType))
            .contains(entry(ENCODING, expectedEncoding))
    ;
  }

}