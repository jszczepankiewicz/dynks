package dynks.cache.test.integration;

import net.jcip.annotations.Immutable;

/**
 * Response representing http response from remote application.
 * @author jszczepankiewicz
 * @since 2015-09-02
 */
@Immutable
public class ServerResponse {

    private final String payload;
    private final int responseCode;
    private final String contentType;
    private final String etag;

    public ServerResponse(String payload, int responseCode, String contentType, String etag) {
        this.payload = payload;
        this.responseCode = responseCode;
        this.contentType = contentType;
        this.etag = etag;
    }

    public String getPayload() {
        return payload;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getEtag() {
        return etag;
    }

    @Override
    public String toString() {
        return "ServerResponse{" +
                "payload='" + payload + '\'' +
                ", responseCode=" + responseCode +
                ", contentType='" + contentType + '\'' +
                ", etag='" + etag + '\'' +
                '}';
    }
}
