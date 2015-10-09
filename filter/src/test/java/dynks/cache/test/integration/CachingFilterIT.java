package dynks.cache.test.integration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static dynks.cache.TestValues.UTF8_JSON;
import static dynks.cache.test.DynksAssertions.assertThat;
import static dynks.http.HttpMethod.*;

/**
 * Acceptance tests for caching filter. After trying to build proper integration tests for CachingFiler using PowerMock
 * I realized that building such tests will require too much logic copied from CachingFilter to the tests infrastructure.
 * This will increase by much the risk that such tests will become less maintenable and will become false positive / negative.
 * Thus this tests arise. The principles of these tests are as following:
 * <ul>
 *     <li>all tests will interact with true servlet application hosted on jetty that will use real socket port
 *     (on 0.0.0.0) and will be setup by gradle just before firing integration tests. This should be doable on travis-ci</li>
 *     <li>all tests are fired using only one setUp phase. This is needed as there is no easy way to invoke redeploy between tests</li>
 *     <li>all tests should use separate regions to minimize risks of interference</li>
 *     <li>all tests interact though real http connection using http client</li>
 *     <li>all test asserts on real http responses thus if tests want to check whether something was cached by last request they
 *     had to go into conversational state by first invoking action and subsequently invoke the same and assert on second response.
 *     Cause there is no direct way to check the state on server.</li>
 * </ul>
 * @author jszczepankiewicz
 * @since 2015-09-02
 */
public class CachingFilterIT {

    private Client client = new Client();

    private static final int OK = 200;
    private static final int NOT_MODIFIED = 304;
    private static final String NOT_SET = null;
    private static final ServerPayloadCondition nonEmptyPayload = new ServerPayloadCondition();

    //  divide into caching passthrough tests & non passthrough

    @BeforeClass
    public static void waitTillJettyFromGradleWillBeReady() throws TimeoutException {
        /*
        Gradle fires jetty as daemon asynchronously before running tests here. We need to
        wait (or timeout) on jetty by looking into proper response
         */
      new Client().waitTillServerReady(5);
    }

    @Test
    public void deleteRequestShouldBypassCache() throws IOException{

        //  when
        ServerResponse response = client.requestTo("api/v1/cached/deleteRequestShouldBypassCache", DELETE);

        //  then
        assertThat(response)
                .hasContentType(UTF8_JSON)
                .hasEtag(NOT_SET)
                .hasResponseCode(OK)
                .is(nonEmptyPayload)
                .isFor("api/v1/cached/deleteRequestShouldBypassCache");

    }

    @Test
    public void postRequestShouldBypassCache() throws IOException {

        //  when
        ServerResponse response = client.requestTo("api/v1/cached/postRequestShouldBypassCache", POST);

        //  then
        assertThat(response)
                .hasContentType(UTF8_JSON)
                .hasEtag(NOT_SET)
                .hasResponseCode(OK)
                .is(nonEmptyPayload)
                .isFor("api/v1/cached/postRequestShouldBypassCache");

    }

    @Test
    public void getOnCachedRegionShouldReturnEtag() throws IOException{

        //  when
        ServerResponse response = client.requestTo("api/v1/cached/getOnCachedRegionShouldReturnEtag", GET);

        //  then
        assertThat(response)
                .hasContentType(UTF8_JSON)
                .hasEtagSet()
                .hasResponseCode(OK)
                .is(nonEmptyPayload)
                .isFor("api/v1/cached/getOnCachedRegionShouldReturnEtag");

    }

    @Test
    public void repeadedGetOnCachedRegionShouldReturnNonModified() throws IOException{

        //  when
        ServerResponse response1 = client.requestTo("api/v1/cached/repeadedGetOnCachedRegionShouldReturnNonModified", GET);
        ServerResponse response2 = client.requestTo("api/v1/cached/repeadedGetOnCachedRegionShouldReturnNonModified", GET, null, response1.getEtag());

        //  then
        assertThat(response1)
                .hasContentType(UTF8_JSON)
                .hasEtagSet()
                .hasResponseCode(OK)
                .is(nonEmptyPayload)
                .isFor("api/v1/cached/repeadedGetOnCachedRegionShouldReturnNonModified");

        assertThat(response2)
                .hasContentType(null)   // TODO: change to "hasContentTypeNotSet
                .hasEtag(null)
                .hasResponseCode(NOT_MODIFIED)
                .hasEmptyPayload();
    }

    @Test
    public void getToCachedResourceWithNonExistingEtagShouldReturnFreshValue() throws IOException{

        //  when
        ServerResponse response1 = client.requestTo("api/v1/cached/getToCachedResourceWithNonExistingEtagShouldReturnFreshValue", GET);
        ServerResponse response2 = client.requestTo("api/v1/cached/getToCachedResourceWithNonExistingEtagShouldReturnFreshValue", GET, null, "someNonExistingEtag");

        //  then
        assertThat(response1)
                .hasContentType(UTF8_JSON)
                .hasEtagSet()
                .hasResponseCode(OK)
                .is(nonEmptyPayload)
                .isFor("api/v1/cached/getToCachedResourceWithNonExistingEtagShouldReturnFreshValue");

        //  we should have full response with same content and etag returned
        //  as new value
      assertThat(response2)
                .hasContentType(UTF8_JSON)
                .hasEtagSet()
                .hasResponseCode(OK)
                .is(nonEmptyPayload)
                .isFor("api/v1/cached/getToCachedResourceWithNonExistingEtagShouldReturnFreshValue");

        assertThat(response1.getEtag()).isEqualTo(response2.getEtag());
        assertThat(response1.getPayload()).isEqualTo(response2.getPayload());


    }

  @Test
  public void repeatedGetOnCachedRegionFromDifferentClientsShouldReturnSameValue() throws IOException {
    //  when
    ServerResponse response1 = client.requestTo("api/v1/cached/repeatedGetOnCachedRegionFromDifferentClientsShouldReturnSameValue", GET);
    ServerResponse response2 = client.requestTo("api/v1/cached/repeatedGetOnCachedRegionFromDifferentClientsShouldReturnSameValue", GET);

    //  then
    assertThat(response1)
            .hasContentType(UTF8_JSON)
            .hasEtagSet()
            .hasResponseCode(OK)
            .is(nonEmptyPayload)
            .isFor("api/v1/cached/repeatedGetOnCachedRegionFromDifferentClientsShouldReturnSameValue");

    //  we should have full response with same content and etag returned
    //  as new value
    assertThat(response2)
            .hasContentType(UTF8_JSON)
            .hasEtagSet()
            .hasResponseCode(OK)
            .is(nonEmptyPayload)
            .isFor("api/v1/cached/repeatedGetOnCachedRegionFromDifferentClientsShouldReturnSameValue");

    assertThat(response1.getEtag()).isEqualTo(response2.getEtag());
    assertThat(response1.getPayload()).isEqualTo(response2.getPayload());
  }

}
