package dynks.jmx;

import com.typesafe.config.Config;
import org.slf4j.Logger;

import javax.management.*;

import static dynks.jmx.ConfigurationMBean.CONFIGURATION_JMX_NAME;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Jmx for dynks library.
 *
 * @author jszczepankiewicz
 * @since 2016-01-02
 */
public class JmxServer {

  private static final Logger LOG = getLogger(JmxServer.class);

  private final Configuration configuration;

  public JmxServer(Config config) {
    this.configuration = new Configuration(config);
  }

  public void start() {

    try {
      StandardMBean mbean = new StandardMBean(configuration, ConfigurationMBean.class);
      getPlatformMBeanServer().registerMBean(mbean, new ObjectName(CONFIGURATION_JMX_NAME));

    } catch (MalformedObjectNameException | MBeanRegistrationException | InstanceAlreadyExistsException |
            NotCompliantMBeanException e) {
      throw new IllegalStateException("Exception during JMX registration", e);
    }

    LOG.info("Successfully registered mbean: {}", CONFIGURATION_JMX_NAME);
  }

  /**
   * Unregister mbean to clean up references.
   */
  public void dispose() {
    try {
      getPlatformMBeanServer().unregisterMBean(new ObjectName(CONFIGURATION_JMX_NAME));
      LOG.info("Successfully unregistered mbean: {}", CONFIGURATION_JMX_NAME);
    } catch (InstanceNotFoundException | MBeanRegistrationException | MalformedObjectNameException e) {
      LOG.warn("Exception while unregistering mbean", e);
    }
  }

}
