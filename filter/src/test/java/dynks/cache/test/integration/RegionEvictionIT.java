package dynks.cache.test.integration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static dynks.cache.TestValues.UTF8_JSON;
import static dynks.cache.test.DynksAssertions.assertThat;
import static dynks.cache.test.integration.Client.OK;
import static dynks.http.HttpMethod.DELETE;
import static dynks.http.HttpMethod.GET;
import static java.util.Collections.unmodifiableMap;

/**
 * Integration tests for region eviction functionality.
 *
 * @author jszczepankiewicz
 * @since 2015-10-14
 */
public class RegionEvictionIT {

  private Client client = new Client();

  @BeforeClass
  public static void waitTillJettyFromGradleWillBeReady() throws TimeoutException {
    new Client().waitTillServerReady(5);
  }

  @Test
  public void evictRegionWithDefaultMaxEntriesDeletedInOneBatch() throws IOException, URISyntaxException {

    //  given
    havingCachedAuthorsOfNumber(34);

    //  when
    ServerResponse response = client.requestTo("purger/evictRegionWithDefaultMaxEntriesDeletedInOneBatch", DELETE, evicting("authors"));

    //  then
    assertThat(response)
            .hasPayload("Evicted 34 entries from region: authors with maxEntriesDeletedInOneBatch: 1000")
            .hasResponseCode(OK);
  }

  @Test
  public void evictMassiveRegion() throws IOException, URISyntaxException {

    //  given
    havingCachedAuthorsOfNumber(140);

    //  when
    ServerResponse response = client.requestTo("purger/evictMassiveRegion", DELETE, evicting("authors", 15));

    //  then
    assertThat(response)
            .hasPayload("Evicted 140 entries from region: authors with maxEntriesDeletedInOneBatch: 15")
            .hasResponseCode(OK);
  }


  @Test
  public void evictEmptyRegion() throws IOException, URISyntaxException {

    //  given
    havingCachedAuthorsOfNumber(0);

    //  when
    ServerResponse response = client.requestTo("purger/evictEmptyRegion", DELETE, evicting("bestsellers", 15));

    //  then
    assertThat(response)
            .hasPayload("Evicted 0 entries from region: bestsellers with maxEntriesDeletedInOneBatch: 15")
            .hasResponseCode(OK);
  }

  private Map<String, String> evicting(String region) {
    return unmodifiableMap(new HashMap(1, 1.0f) {
      {
        put("region", region);
      }

      ;
    });
  }

  private Map<String, String> evicting(String region, int maxEntriesDeletedInOneBatch) {
    return unmodifiableMap(new HashMap(2, 1.0f) {
      {
        put("region", region);
        put("maxEntriesDeletedInOneBatch", String.valueOf(maxEntriesDeletedInOneBatch));
      }

      ;
    });
  }

  private void havingCachedAuthorsOfNumber(int numbers) throws IOException, URISyntaxException {

    for (int i = 0; i < numbers; i++) {

      ServerResponse response1 = client.requestTo("api/v1/authors/" + i, GET);

      assertThat(response1)
              .hasContentType(UTF8_JSON)
              .hasEtagSet()
              .hasResponseCode(OK)
              .is(CachingFilterIT.NON_EMPTY_CACHEABLE_PAYLOAD)
              .isFor("api/v1/authors/" + i);
    }
  }
}
