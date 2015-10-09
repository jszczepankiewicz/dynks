package dynks.cache;

import com.typesafe.config.ConfigFactory;
import dynks.http.ETag;
import dynks.redis.RedisCacheRepository;
import dynks.redis.RedisCacheRepositoryConfigBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;

import static dynks.cache.Entry.*;
import static dynks.cache.TestValues.UTF8;
import static dynks.cache.TestValues.UTF8_JSON;
import static dynks.cache.test.DynksAssertions.assertThat;
import static dynks.http.ETag.SIZEOF_ETAG;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.assertj.core.data.MapEntry.entry;
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
  public void throwIAEOnNulledKeyForFetchIfChanged() {

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Key to upsert should not be null");

    //  when
    repo.fetchIfChanged(null, "someEtag");
  }

  @Test
  public void throwIAEOnNulledKeyForFetchIfChangedNulledEtag() {

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Key to upsert should not be null");

    //  when
    repo.fetchIfChanged(null, null);
  }

  @Test
  public void throwIAEOnEmptyKeyForFetchIfChangedNulledEtag() {

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Key to upsert should not be empty");

    //  when
    repo.fetchIfChanged(" ", null);
  }

  @Test
  public void throwIAEOnEmptyKeyForFetchIfChanged() {

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Key to upsert should not be empty");

    //  when
    repo.fetchIfChanged(" ", "someEtag");
  }

  @Test
  public void upsertValueIfNotExist() {

    //  given
    String etag = ETag.of(JSON_SAVED, etagBuilder);

    //  when
    repo.upsert(KEY, JSON_SAVED, etag, UTF8_JSON, UTF8, 999, HOURS);

    //  then
    assertValueExist(KEY, etag, JSON_SAVED, UTF8_JSON, UTF8);

  }

  @Test
  public void utf8UpsertShouldReturnCorrectValuesFromCache() {

    //  given
    String payload = "ąśćźżęłóĄŚĆŻŹĘŁÓ";

    //  when
    repo.upsert(KEY, payload, "etag1", UTF8_JSON, UTF8, 999, HOURS);
    CacheQueryResult result = repo.fetchIfChanged(KEY, null);

    //  then
    assertThat(result.getPayload()).isEqualTo(payload);

  }

  @Test
  public void upsertValueEvenIfKeyExistsWithDifferentEtag() {

    //  given
    havingEntryCached(KEY, JSON_SAVED, "someetagxyz", UTF8_JSON, UTF8);
    final String newContent = "[]";
    final String newEtag = ETag.of(newContent, etagBuilder);

    //  when
    repo.upsert(KEY, newContent, newEtag, UTF8_JSON, UTF8, 999, HOURS);

    //  then
    assertValueExist(KEY, newEtag, newContent, UTF8_JSON, UTF8);
  }


  /*
   * there is no value for given key, CacheResult will return: upsertNeeded: true, payload: null, storedEtag: null
   */
  @Test
  public void detectMissingEntryOnFetch() {

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
  public void returnContentClientFirstTimeContentNotYetCached() {

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
  public void returnEntryFromCacheWhenEtagUnknown() {

    //  given
    String etagExisting = ETag.of(JSON_SAVED, etagBuilder);
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
  public void fetchNotNeededAsCachedVersionNotChanged() {

    //  given
    String etagExisting = ETag.of(JSON_SAVED, etagBuilder);
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
  public void fetchReturnedNewerEntry() {

    //  given
    String etagExisting = ETag.of(JSON_SAVED, etagBuilder);
    havingEntryCached(KEY, JSON_SAVED, etagExisting, UTF8_JSON, UTF8);

    //  when
    CacheQueryResult result = repo.fetchIfChanged(KEY, "someolderetag");

    //  then
    assertThat(result).hasPayload(JSON_SAVED).hasStoredEtag(etagExisting).isUpsertNotNeeded();
  }


  @Ignore
  @Test
  public void removeKeyIfExist() {

  }

  //  test utils

  private Jedis getJedis() {
    return new Jedis("localhost");
  }

  private void havingEntryCached(String key, String content, String etag, String contentType, String encoding) {
    Jedis jedis = getJedis();
    jedis.hmset(key, new Entry(content, etag, contentType, encoding));
  }

  private void assertValueExist(String key, String expectedEtag, String expectedContent, String expectedContentType, String expectedEncoding) {

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