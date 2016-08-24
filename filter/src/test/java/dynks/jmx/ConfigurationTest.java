package dynks.jmx;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;


/**
 * @author jszczepankiewicz
 * @since 2016-01-02
 */
public class ConfigurationTest {

  @Test
  public void exposeAllConfiguration() {

    //  given
    Configuration config = new Configuration(ConfigFactory.load("dynks-test"));

    //  when


    //  then
  }

}