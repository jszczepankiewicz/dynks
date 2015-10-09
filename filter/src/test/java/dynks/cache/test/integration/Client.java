package dynks.cache.test.integration;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static dynks.http.ETag.ETAG_REQUEST_HEADER;
import static dynks.http.ETag.ETAG_RESPONSE_HEADER;
import static dynks.http.HttpMethod.GET;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
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

  public ServerResponse requestTo(String uri, String method) throws IOException {
    return requestTo(uri, method, null, null);
  }

  public ServerResponse requestTo(String uri, String method, String expectedContentType, String clientEtag) throws IOException {

    checkNotNullOrEmpty(uri);
    checkNotNullOrEmpty(method);

    CloseableHttpClient httpclient = createDefault();

    try {
      HttpRequestBase request = of(method, toLocalhost(uri));

      if (clientEtag != null) {
        request.addHeader(ETAG_REQUEST_HEADER, clientEtag);
      }

      if (expectedContentType != null) {
        request.addHeader("Accept", expectedContentType);
      }

      System.out.println("Executing request " + request.getRequestLine());

      // Create a custom response handler
      ResponseHandler<ServerResponse> responseHandler = new ResponseHandler<ServerResponse>() {

        @Override
        public ServerResponse handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {

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
        }
      };

      ServerResponse responseBody = httpclient.execute(request, responseHandler);

      System.out.println("----------------------------------------");
      System.out.println(responseBody);

      return responseBody;

    } finally {
      httpclient.close();
    }
  }


  private HttpRequestBase of(String method, String url) {

    switch (method.toLowerCase()) {
      case "get":
        return new HttpGet(url);
      case "post":
        return new HttpPost(url);
      case "put":
        return new HttpPut(url);
      case "delete":
        return new HttpDelete(url);
      default:
        throw new IllegalArgumentException("Unsupported method: " + method);
    }
  }

  private String toLocalhost(String relativeUrl) {
    return "http://0.0.0.0:8080/integration-tests/" + relativeUrl;
  }

}
