package dynks.cache;

/**
 * Content cached in repository.
 *
 * @author jszczepankiewicz
 * @since 2015-06-14
 */
public class CachedContent {
  protected final String payload;
  protected final String storedEtag;
  protected final String contentType;
  protected final String encoding;

  public CachedContent(String storedEtag, String payload, String contentType, String encoding) {
    this.storedEtag = storedEtag;
    this.payload = payload;
    this.contentType = contentType;
    this.encoding = encoding;
  }

  public String getPayload() {
    return payload;
  }

  public String getStoredEtag() {
    return storedEtag;
  }

  public String getContentType() {
    return contentType;
  }

  public String getEncoding() {
    return encoding;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CachedContent that = (CachedContent) o;

    if (payload != null ? !payload.equals(that.payload) : that.payload != null) return false;
    if (storedEtag != null ? !storedEtag.equals(that.storedEtag) : that.storedEtag != null) return false;
    if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) return false;
    return !(contentType != null ? !contentType.equals(that.contentType) : that.contentType != null);

  }

  @Override
  public int hashCode() {
    int result = payload != null ? payload.hashCode() : 0;
    result = 31 * result + (storedEtag != null ? storedEtag.hashCode() : 0);
    result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
    result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
    return result;
  }
}
