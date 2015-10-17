package dynks.cache;

import javax.servlet.http.HttpServletRequest;

/**
 * Strategy responsible for building unique keys for storing cache results.
 *
 * @author jszczepankiewicz
 * @since 2015-04-17
 */
public interface KeyStrategy {

  /**
   * Build key for given request and matched region.
   *
   * @param request that key should be build against.
   * @param region  matched for given request.
   * @return
   */
  String keyFor(HttpServletRequest request, CacheRegion region);

  /**
   * Retrieve redis wildcard expression that can be used to obtain all keys for given region.
   *
   * @param region for which expression should be retrieved.
   * @return
   */
  String wildcardKeyFor(CacheRegion region);
}
