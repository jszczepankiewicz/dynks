package dynks;

import dynks.cache.CachedContent;
import org.assertj.core.api.AbstractAssert;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static dynks.http.ETag.ETAG_RESPONSE_HEADER;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author jszczepankiewicz
 * @since 2015-06-14
 */
public class HttpServletResponseAssert extends AbstractAssert<HttpServletResponseAssert, HttpServletResponse> {

    public HttpServletResponseAssert(HttpServletResponse actual) {
        super(actual, HttpServletResponseAssert.class);
    }

    public static HttpServletResponseAssert assertThat(HttpServletResponse actual) {
        return new HttpServletResponseAssert(actual);
    }

    public HttpServletResponseAssert hasStatus(int code) {
        isNotNull();

        verify(actual, times(1)).setStatus(code);
        return this;
    }

    /**
     * Assert that response was not altered by caching layer by checking that following methods were NOT called:
     * <ul>
     * <li>setStatus()</li>
     * <li>setContentType()</li>
     * <li>addCookie()</li>
     * <li>getOutputStream()</li>
     * <li>getWriter()</li>
     * </ul>
     *
     * @return
     */
    public HttpServletResponseAssert hasNotBeenAltered() throws IOException {

        isNotNull();

        verify(actual, never()).setStatus(anyInt());

        verify(actual, never()).setContentType(anyString());
        verify(actual, never()).addCookie(anyObject());
        verify(actual, never()).getOutputStream();
        verify(actual, never()).getWriter();

        return this;
    }

    public HttpServletResponseAssert hasContentType(String contentType) {
        isNotNull();

        String assertjErrorMessage = "\nExpected content-type of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        String actualContentType = actual.getContentType();

        if (contentType == null) {
            if (actualContentType != null) {
                failWithMessage(assertjErrorMessage, actual, "null", actualContentType);
            }
        } else {
            if (!contentType.equals(actualContentType)) {
                failWithMessage(assertjErrorMessage, actual, contentType, actualContentType);
            }
        }

        return this;

    }

    public HttpServletResponseAssert hasEtag(String etag) {

        isNotNull();

        String assertjErrorMessage = "\nExpected status of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        String actualEtag = actual.getHeader(ETAG_RESPONSE_HEADER);

        if (etag == null) {
            if (actualEtag != null) {
                failWithMessage(assertjErrorMessage, actual, "null", actualEtag);
            }
        } else {
            if (!etag.equals(actualEtag)) {
                failWithMessage(assertjErrorMessage, actual, etag, actualEtag);
            }
        }

        return this;
    }

    /**
     * Assert empty payload + HTTP 304 Not Modified returned.
     */
    public HttpServletResponseAssert hasNotModified() {
        isNotNull();

        hasStatus(SC_NOT_MODIFIED);
        hasEmptyPayload();
        return this;
    }

    public HttpServletResponseAssert hasEmptyPayload() {
        throw new UnsupportedOperationException("implement me");
    }

    public HttpServletResponseAssert hasPayload(String payload) {

        isNotNull();

        String assertjErrorMessage = "\nExpected status of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        return null;
    }

    /**
     * Asserts payload + etag + response content type + http 200 on response code.
     *
     * @param content
     * @return
     */
    public HttpServletResponseAssert containsCacheable(CachedContent content) {

        isNotNull();

        hasContentType(content.getContentType());
        hasEtag(content.getStoredEtag());
        hasPayload(content.getPayload());
        hasStatus(SC_OK);

        return this;
    }

    /**
     * Assert:
     * <ul>
     * <li>Response code 200</li>
     * <li>Lack of "ETag" header</li>
     * <li>Content type equals to set in expected</li>
     * <li>Payload equals to expected</li>
     * </ul>
     *
     * @param expected
     */
    public HttpServletResponseAssert containsNonCacheable(CachedContent expected) {

        isNotNull();

        hasStatus(SC_OK);
        hasEtag(null);
        hasContentType(expected.getContentType());
        hasPayload(expected.getPayload());

        return this;
    }
}
