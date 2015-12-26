package dynks.cache;

/**
 * Exception indicating error while operating on undergoing repository. Further operations on repository
 * may or may not be consistent. This error itself does not indicate whether error is persistent
 * or not.
 *
 * @author jszczepankiewicz
 * @since 2015-12-21
 */
public class CacheRepositoryException extends Exception {
  public CacheRepositoryException(Exception cause) {
    super(cause);
  }
}
