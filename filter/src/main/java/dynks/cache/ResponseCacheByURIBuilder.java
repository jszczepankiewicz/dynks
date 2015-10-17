package dynks.cache;

import com.typesafe.config.Config;
import dynks.URIMatcher;
import org.slf4j.Logger;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Builder for creation of ResponseCacheByURIPolicy object that is using configuration in file stored in classpath.
 */
public class ResponseCacheByURIBuilder {

  private static final Logger LOG = getLogger(ResponseCacheByURIBuilder.class);

  public static CacheByURIRegionRepository build(Config config) {

    List<? extends Config> configuredRegions = config.getConfigList("dynks.regions");
    Map<URIMatcher, CacheRegion> regions = new HashMap<>(configuredRegions.size());
    String namespace = config.getString("dynks.namespace");
    NamespacedURIKeyStrategy keyStrategy = new NamespacedURIKeyStrategy(namespace);
    Set<String> regionIds = new HashSet<>();

    for (Config region : configuredRegions) {

      if (!region.hasPath("id")) {
        throw new IllegalArgumentException("Region id should be provided");
      }

      String id = region.getString("id");

      if (regionIds.contains(id)) {
        throw new IllegalArgumentException("Regions should have unique names but found duplicated region with name '" + id + "'");
      }

      if (id.contains(":")) {
        throw new IllegalArgumentException("Region id should not contain colon but found in '" + id + "'");
      }

      regionIds.add(id);

      if (id.trim().isEmpty()) {
        throw new IllegalArgumentException("Region id should not be empty");
      }
      if (id.trim().startsWith("_")) {
        throw new IllegalArgumentException("Region id should not start with underscore");
      }

      //  for now only URIKeyStrategy supported
      CacheRegion cached = new CacheRegion(id, region.getDuration("ttl", MILLISECONDS), MILLISECONDS, keyStrategy);
      String url = region.getString("pattern");
      regions.put(new URIMatcher(url), cached);
      LOG.debug("Loaded cached region against: {} with ttl: {} {}", url, cached.getTtl(), cached.getTtlUnit());
    }

    LOG.info("Configured {} cached URL regions that will be stored with '{}' namespace", configuredRegions.size(), namespace);

    return new CacheByURIRegionRepository(regions);
  }
}
