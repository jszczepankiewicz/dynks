package dynks.cache.test.integration;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static dynks.http.ETag.ETAG_REQUEST_HEADER;
import static dynks.http.ETag.ETAG_RESPONSE_HEADER;
import static dynks.http.HttpMethod.GET;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.util.Collections.EMPTY_MAP;
import static org.apache.http.impl.client.HttpClients.createDefault;
import static org.assertj.core.util.Preconditions.checkNotNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Http client using in integration testing.
 *
 * @author jszczepankiewicz
 * @since 2015-09-02
 */
public class Client {

  public static final int OK = 200;
  public static final int NOT_MODIFIED = 304;

  private static final Logger LOG = getLogger(Client.class);

  /**
   * Wait till http server for integration testing will be responsive or timeout with TimeoutException
   *
   * @param timeoutSec
   * @throws TimeoutException
   */
  public void waitTillServerReady(int timeoutSec) throws TimeoutException {

    LOG.info("Warming up connection to http server for integration testing for max {} seconds...", timeoutSec);

    long timeoutMs = currentTimeMillis() + (timeoutSec * 1000);

    while (true) {

      try {
        ServerResponse reply = requestTo("api/v1/uncached/testWarmUp", GET);
        //  assuming ready to invoke requests.
        LOG.info("Ready for IT testing, last response code: {}", reply.getResponseCode());
        return;
      } catch (Exception ex) {

        LOG.debug("Connection not ready yet: {}", ex.getMessage());

        try {
          if (currentTimeMillis() >= timeoutMs) {
            break;
          }
          sleep(1000l);
        } catch (InterruptedException e) {
          //  ignore
        }
      }
    }

    throw new TimeoutException("Http connection to test servlet not ready after " + timeoutSec + " sec. Please check that nothing blocks jetty from starting up.");
  }

  public ServerResponse requestTo(String uri, String method, Map<String, String> params) throws IOException, URISyntaxException {
    return requestTo(uri, method, null, null, params);
  }

  public ServerResponse requestTo(String uri, String method) throws IOException, URISyntaxException {
    return requestTo(uri, method, null, null, EMPTY_MAP);
  }

  public ServerResponse requestTo(String uri, String method, String expectedContentType, String clientEtag) throws IOException, URISyntaxException {
    return requestTo(uri, method, expectedContentType, clientEtag, EMPTY_MAP);

  }

  public ServerResponse requestTo(String uri, String method, String expectedContentType, String clientEtag, Map<String, String> params) throws IOException, URISyntaxException {

    checkNotNullOrEmpty(uri);
    checkNotNullOrEmpty(method);

    CloseableHttpClient httpclient = createDefault();

    try {
      HttpRequestBase request = of(method, uri, params);

      if (clientEtag != null) {
        request.addHeader(ETAG_REQUEST_HEADER, clientEtag);
      }

      if (expectedContentType != null) {
        request.addHeader("Accept", expectedContentType);
      }

      System.out.println("Executing request " + request.getRequestLine());

      // Create a custom response handler
      ResponseHandler<ServerResponse> responseHandler = response -> {

        String etagValue = null;

        Header etag = response.getFirstHeader(ETAG_RESPONSE_HEADER);
        if (etag != null) {
          etagValue = etag.getValue();
        }

        String contentTypeValue = null;
        Header contentType = response.getFirstHeader("Content-Type");
        if (contentType != null) {
          contentTypeValue = contentType.getValue();
        }

        String payload = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          payload = EntityUtils.toString(entity);
        }

        return new ServerResponse(payload, response.getStatusLine().getStatusCode(), contentTypeValue, etagValue);
      };

      ServerResponse responseBody = httpclient.execute(request, responseHandler);

      System.out.println("----------------------------------------");
      System.out.println(responseBody);

      return responseBody;

    } finally {
      httpclient.close();
    }
  }


  private HttpRequestBase of(String method, String relativeUrl, Map<String, String> params) throws URISyntaxException {

    URIBuilder builder = new URIBuilder();

    builder.setScheme("http")
            .setHost("localhost")
            .setPort(8080)
            .setPath("/integration-tests/" + relativeUrl);

    params.forEach((k, v) -> builder.addParameter(k, v));

    switch (method.toLowerCase()) {
      case "get":
        return new HttpGet(builder.build());
      case "post":
        return new HttpPost(builder.build());
      case "put":
        return new HttpPut(builder.build());
      case "delete":
        return new HttpDelete(builder.build());
      default:
        throw new IllegalArgumentException("Unsupported method: " + method);
    }
  }

}
