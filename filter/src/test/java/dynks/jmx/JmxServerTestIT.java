package dynks.jmx;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Test;

import javax.management.*;

import static dynks.jmx.ConfigurationMBean.CONFIGURATION_JMX_NAME;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test covering creation of jmx and exposing functionality through that.
 * All tests are using local access to JMX mbean. No remote bean access is tested
 * to lower the complexity of setup.
 *
 * @author jszczepankiewicz
 * @since 2016-01-02
 */
public class JmxServerTestIT {

  private JmxServer server;

  private void withOnlyRequiredAttributes() {
    Config conf = ConfigFactory.load("dynks-test");
    server = new JmxServer(conf);
    server.start();
  }

  @Test
  public void exportStorage() {

    //  given
    withOnlyRequiredAttributes();

    //  than
    assertThat(stringAttribute("Storage")).isEqualTo("REDIS");
  }

  @Test
  public void exportHardenedMode() {

    //  given
    withOnlyRequiredAttributes();

    //  then
    assertThat(boolAttribute("HardenedMode")).isTrue();
  }

  @Test
  public void exportNamespace() {

    //  given
    withOnlyRequiredAttributes();

    //  then
    assertThat(stringAttribute("Namespace")).isEqualTo("rm-test");
  }

  @Test
  public void exportIsIgnoreNoRegionsConfigured() {

    //  given
    withOnlyRequiredAttributes();

    //  then
    assertThat(boolAttribute("IgnoreNoRegionsConfigured")).isFalse();
  }

  @Test
  public void exportRedisHost() {

    //  given
    withOnlyRequiredAttributes();

    //  then
    assertThat(stringAttribute("RedisHost")).isEqualTo("192.168.0.21");
  }

  @Test
  public void exportRedisPort() {

    //  given
    withOnlyRequiredAttributes();

    //  then
    assertThat(intAttribute("RedisPort")).isEqualTo(222);
  }

  @Test
  public void exportRedisMaxEntriesDeletedInOneBatch() {

    //  given
    withOnlyRequiredAttributes();

    //  then
    assertThat(intAttribute("RedisMaxEntriesDeletedInOneBatch")).isEqualTo(2000);
  }

  @Test
  public void exportRegionsJson() {

    //  given
    withOnlyRequiredAttributes();

    //  then
    assertThat(stringAttribute("RegionsJson")).isEqualTo("{id=\"bestsellers\", ttl=\"PT30M\", pattern=\"/api/v1/bestsellers/{D}\"}" +
            "\n,\n" +
            "{id=\"users\", ttl=\"PT2M9S\", pattern=\"/api/v1/users/{S}\"}" +
            "\n,\n" +
            "{id=\"events\", ttl=\"PT0.004S\", pattern=\"/api/v1/events/{D}\"}\n");
  }

  @After
  public void unregisterBean() {
    if (server != null) {
      server.dispose();
    }
  }

  private int intAttribute(String name) {
    return (int) getAttribute(name);
  }

  private boolean boolAttribute(String name) {
    return (boolean) getAttribute(name);
  }

  private String stringAttribute(String name) {
    return (String) getAttribute(name);
  }

  private Object getAttribute(String name) {
    try {
      return getPlatformMBeanServer().getAttribute(new ObjectName(CONFIGURATION_JMX_NAME), name);
    } catch (MalformedObjectNameException | MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException e) {
      throw new IllegalStateException("Error while retrieving jmx attribute", e);
    }
  }
}