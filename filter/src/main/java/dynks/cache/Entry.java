package dynks.cache;

import java.util.HashMap;

/**
 * Cache content extending directly from HashMap to being able to be provided directly without conversion to jedis.
 */
public class Entry extends HashMap<String, String> {

    public static final String PAYLOAD = "v";
    public static final String ETAG = "e";
    public static final String CONTENT_TYPE = "c";
    public static final String ENCODING = "d";


    public Entry(String content, String etag, String contentType, String encoding) {

        super(3);

        if (content == null) {
            throw new NullPointerException("Content to put into cache should not be null");
        }

        if (etag == null) {
            throw new NullPointerException("Etag to put into cache should not be null");
        }

        if (contentType == null) {
            throw new NullPointerException("Content type should not be null");
        }

        if (encoding == null) {
            throw new NullPointerException("Encoding should not be null");
        }

        this.put(ETAG, etag);
        this.put(PAYLOAD, content);
        this.put(CONTENT_TYPE, contentType);
        this.put(ENCODING, encoding);
    }

    public String getContent() {
        return get(PAYLOAD);
    }

    public String getEtag() {
        return get(ETAG);
    }

    public String getContentType() {
        return get(CONTENT_TYPE);
    }

    public String getEncoding() {
        return get(ENCODING);
    }
}
