package dynks.cache;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Repository for managing cache regions.
 *
 * @author jszczepankiewicz
 * @since 2015-03-26
 */
public interface CacheRegionRepository {

  /**
   * Retrieve region corresponding to given request.
   *
   * @param request
   * @return
   */
  CacheRegion getfor(HttpServletRequest request);

  /**
   * Retrieve region by id.
   *
   * @param id of region
   * @return
   */
  Optional<CacheRegion> getById(String id);

}
