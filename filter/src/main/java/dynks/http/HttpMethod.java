package dynks.http;

/**
 * Http methods. As the time of writing there are no constants for these.
 * @author jszczepankiewicz
 * @since 2015-06-14
 */
public class HttpMethod {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String HEAD = "HEAD";
    public static final String OPTIONS = "OPTIONS";

    private HttpMethod(){
        //  no instances allowed
    }

}
