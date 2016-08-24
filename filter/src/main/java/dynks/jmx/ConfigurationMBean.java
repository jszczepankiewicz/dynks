package dynks.jmx;

/**
 * JMX bean for interacting with dynks configuration.
 *
 * @author jszczepankiewicz
 * @since 2016-01-02
 */
public interface ConfigurationMBean {

  String CONFIGURATION_JMX_NAME = "dynks:type=Configuration";

  /**
   * Retrieve <pre>dynks.storage</pre>
   *
   * @return
   */
  String getStorage();

  /**
   * Retrieve <pre>dynks.hardenedMode</pre>
   *
   * @return true if hardenedMode is on
   */
  boolean isHardenedMode();

  /**
   * Retrieve <pre>dynks.namespace</pre>
   *
   * @return name of namespace used
   */
  String getNamespace();

  /**
   * Retrieve <pre>dynks.ignoreNoRegionsConfigured</pre>
   *
   * @return true if dynks will start successfully without regions configured
   */
  boolean isIgnoreNoRegionsConfigured();

  /**
   * Retrieve <pre>dynks.redis.host</pre>
   *
   * @return hostname for redis server
   */
  String getRedisHost();

  /**
   * Retrieve <pre>dynks.redis.port</pre>
   *
   * @return port on which redis server is running
   */
  int getRedisPort();

  /**
   * Retrieve <pre>dynks.redis.maxEntriesDeletedInOneBatch</pre>
   *
   * @return number of entities deleted in one batch during region invalidation
   */
  int getRedisMaxEntriesDeletedInOneBatch();

  /**
   * Retrieves <pre>dynks.regions</pre> as json
   *
   * @return json describing regions
   */
  String getRegionsJson();


}
