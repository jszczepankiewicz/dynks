package dynks.jmx;

import com.typesafe.config.Config;

/**
 * @author jszczepankiewicz
 * @since 2016-01-02
 */
public class Configuration implements ConfigurationMBean {

  private static final String STORAGE = "dynks.storage";
  public static final String HARDENED_MODE = "dynks.hardenedMode";
  public static final String NAMESPACE = "dynks.namespace";
  public static final String IGNORE_NO_REGIONS_CONFIGURED = "dynks.ignoreNoRegionsConfigured";
  public static final String REDIS_HOST = "dynks.redis.host";
  public static final String REDIS_PORT = "dynks.redis.port";
  public static final String REGIONS = "dynks.regions";
  public static final String REDIS_MAX_ENTRIES_DELETED_IN_ONE_BATCH = "dynks.redis.maxEntriesDeletedInOneBatch";

  private final Config config;

  public Configuration(Config config) {
    this.config = config;
  }

  @Override
  public String getStorage() {
    return config.getString(STORAGE);
  }

  @Override
  public boolean isHardenedMode() {
    return config.getBoolean(HARDENED_MODE);
  }

  @Override
  public String getNamespace() {
    return config.getString(NAMESPACE);
  }

  @Override
  public boolean isIgnoreNoRegionsConfigured() {
    return config.getBoolean(IGNORE_NO_REGIONS_CONFIGURED);
  }

  @Override
  public String getRedisHost() {
    return config.getString(REDIS_HOST);
  }

  @Override
  public int getRedisPort() {
    return config.getInt(REDIS_PORT);
  }

  @Override
  public int getRedisMaxEntriesDeletedInOneBatch() {
    return config.getInt(REDIS_MAX_ENTRIES_DELETED_IN_ONE_BATCH);
  }

  @Override
  public String getRegionsJson() {

    StringBuilder out = new StringBuilder(200);

    int counter = 0;
    for (Config region : config.getConfigList(REGIONS)) {

      if (counter++ > 0) {
        out.append(",\n");
      }

      out.append('{');
      boolean found = false;
      if (region.hasPath("id")) {
        out.append("id=\"");
        out.append(region.getString("id"));
        out.append('"');
        found = true;
      } else {
        found = false;
      }

      if (region.hasPath("ttl")) {
        if (found) {
          out.append(", ");
        }

        out.append("ttl=\"");
        out.append(region.getDuration("ttl"));
        out.append('"');
        found = true;
      } else {
        found = false;
      }

      if (region.hasPath("pattern")) {
        if (found) {
          out.append(", ");
        }

        out.append("pattern=\"");
        out.append(region.getString("pattern"));
        out.append('"');
        found = true;
      } else {
        found = false;
      }

      out.append("}\n");

    }

    return out.toString();
  }

}
