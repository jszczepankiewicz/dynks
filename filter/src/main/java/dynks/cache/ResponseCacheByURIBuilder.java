package dynks.cache;

import dynks.URIMatcher;
import com.typesafe.config.Config;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Builder for creation of ResponseCacheByURIPolicy object that is using configuration in file stored in classpath.
 */
public class ResponseCacheByURIBuilder {

    private static final Logger LOG = getLogger(ResponseCacheByURIBuilder.class);

    public static ResponseCacheByURIPolicy build(Config config) {

        List<? extends Config> configuredRegions = config.getConfigList("dynks.regions");
        Map<URIMatcher, CacheRegion> regions = new HashMap<>(configuredRegions.size());
        String namespace = config.getString("dynks.namespace");
        NamespacedURIKeyStrategy keyStrategy = new NamespacedURIKeyStrategy(namespace);

        for (Config region : configuredRegions) {
            //  for now only URIKeyStrategy supported
            CacheRegion cached = new CacheRegion(region.getDuration("ttl", MILLISECONDS), MILLISECONDS,keyStrategy);
            String url = region.getString("pattern");
            regions.put(new URIMatcher(url), cached);
            LOG.debug("Loaded cached region against: {} with ttl: {} {}", url, cached.getTtl(), cached.getTtlUnit());
        }

        LOG.info("Configured {} cached URL regions that will be stored with '{}' namespace", configuredRegions.size(), namespace);

        return new ResponseCacheByURIPolicy(regions);
    }
}
