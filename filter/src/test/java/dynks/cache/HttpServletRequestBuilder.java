package dynks.cache;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static dynks.http.ETag.ETAG_REQUEST_HEADER;

/**
 * Creates HttpServletRequest for usage in tests using mockito mocking.
 *
 * @author jszczepankiewicz
 * @since 2015-06-14
 */
public class HttpServletRequestBuilder {


    private final String url;
    private String method;
    private String contentType;
    private String etagStored;

    public HttpServletRequestBuilder(String url){
        this.url = url;
    }

    public HttpServletRequestBuilder(String url, String method){
        this.url = url;
        this.method = method;
    }

    public HttpServletRequestBuilder contentType(String contentType){
        this.contentType = contentType;
        return this;
    }

    public HttpServletRequestBuilder etagStored(String etagStored){
        this.etagStored = etagStored;
        return this;
    }

    public HttpServletRequestBuilder withEmptyClientCache(){
        this.etagStored = null;
        return this;
    }


    public HttpServletRequest build(){
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        StringBuffer urlBuffer = new StringBuffer(url.length());
        urlBuffer.append(url);
        //when(request.getRequestURL()).thenReturn(urlBuffer);
        when(request.getRequestURI()).thenReturn(urlBuffer.toString());

        if(contentType != null){
            when(request.getContentType()).thenReturn(contentType);
        }

        //  this is probably only for newest servlet api
        when(request.getHeader(ETAG_REQUEST_HEADER)).thenReturn(etagStored);

        return request;
    }

}
