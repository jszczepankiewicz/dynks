package dynks.cache;

import dynks.URIMatcher;
import dynks.cache.CacheRegion.Cacheability;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dynks.cache.NamespacedURIKeyStrategy.keyStrategyWithEmptyNamespace;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;

/**
 * Created by jszczepankiewicz on 2015-03-26.
 */
public class CacheByURIRegionRepository implements CacheRegionRepository {

  private final Map<URIMatcher, CacheRegion> regions;
  private final Map<String, CacheRegion> regionsById;
  private final Set<URIMatcher> uris;

  /**
   * Indicates region that will not be cached.
   */
  public static final CacheRegion PASSTHROUGH;

  static {
    PASSTHROUGH = new CacheRegion("_passthrough", 0, null, Cacheability.PASSTHROUGH, keyStrategyWithEmptyNamespace());
  }

  public CacheByURIRegionRepository(Map<URIMatcher, CacheRegion> regions) {

    if (regions == null) {
      throw new NullPointerException("List of cache regions should not be null");
    }

    this.regions = unmodifiableMap(regions);
    this.regionsById = unmodifiableMap(regions.values().stream().collect(toMap(CacheRegion::getId, region -> region)));
    this.uris = regions.keySet();
  }

  @Override
  public Optional<CacheRegion> getById(String id) {

    CacheRegion region = regionsById.get(id);

    if (region == null) {
      return empty();
    }

    return Optional.of(region);
  }

  @Override
  public CacheRegion getfor(final HttpServletRequest request) {

    final String requestURI = request.getRequestURI();

    for (URIMatcher matcher : uris) {
      if (matcher.matches(requestURI)) {
        return regions.get(matcher);
      }
    }

    //  matching not found, assuming no caching for given request
    return PASSTHROUGH;
  }

  public Map<URIMatcher, CacheRegion> getRegions() {
    return regions;
  }
}
