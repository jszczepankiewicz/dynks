package dynks.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.lang.String.valueOf;
import static dynks.Strings.recycle;
import static java.lang.System.currentTimeMillis;

/**
 * Created by jszczepankiewicz on 2015-04-06.
 */
public class ETag {

    public static final String ETAG_REQUEST_HEADER = "If-None-Match";
    public static final String ETAG_RESPONSE_HEADER = "ETag";

    public static final int SIZEOF_ETAG = valueOf(Long.MAX_VALUE + '-' + Integer.MAX_VALUE).length();

    /**
     * Returns fast etag for given String.
     * @param value
     * @param builder buffer (might be non-empty, will be recycled) with internal buffer size to be at least SIZEOF_ETAG
     *                to prevent reallocation of string buffer
     * @return
     */
    public static String of(String value, StringBuilder builder){

        if(value == null){
            throw new NullPointerException("Value for etag calculation should not be null");
        }

        recycle(builder);

        builder.append(currentTimeMillis());
        builder.append('-');
        builder.append(value.hashCode());

        return builder.toString();

    }

    public static String getFrom(HttpServletRequest request){
        return request.getHeader(ETAG_REQUEST_HEADER);
    }


    public static void writeIn(HttpServletResponse response, String value){
        response.addHeader(ETAG_RESPONSE_HEADER, value);
    }


}
