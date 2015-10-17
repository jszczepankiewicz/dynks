package dynks.cache;

import com.typesafe.config.Config;
import dynks.URIMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

import static com.typesafe.config.ConfigFactory.load;
import static dynks.cache.test.DynksAssertions.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jszczepankiewicz
 * @since 2015-03-26.
 */
public class ResponseCacheByURIBuilderTest {

  @Rule
  public ExpectedException thrown = none();

  @Test
  public void throwIAEOnCreatingRegionWithEmptyId() {

    //  given
    Config conf = load("dynks-test-empty-id");

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Region id should not be empty");

    //  when
    ResponseCacheByURIBuilder.build(conf);
  }

  @Test
  public void throwIAEOnCreatingRegionWithMissingId() {

    //  given
    Config conf = load("dynks-test-missing-id");

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Region id should be provided");

    //  when
    ResponseCacheByURIBuilder.build(conf);
  }

  @Test
  public void throwIAEOnNonUniqueRegionId() {

    //  given
    Config conf = load("dynks-test-duplicated-id");

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Regions should have unique names but found duplicated region with name 'users'");

    //  when
    ResponseCacheByURIBuilder.build(conf);
  }

  @Test
  public void throwIAEOnCreatingRegionWithIdStartingWithUnderscore() {

    //  given
    Config conf = load("dynks-test-invalid-id");

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Region id should not start with underscore");

    //  when
    ResponseCacheByURIBuilder.build(conf);
  }

  @Test
  public void throwIAEOnCreatingRegionWithIdContainingColon() {

    //  given
    Config conf = load("dynks-test-colon-id");

    //  then
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Region id should not contain colon but found in 'users:'");

    //  when
    ResponseCacheByURIBuilder.build(conf);
  }

  @Test
  public void loadConfigurationFromFileOnClasspath() {

    //  given
    Config conf = load("dynks-test");
    KeyStrategy keyStrategy = new NamespacedURIKeyStrategy("tst");

    //  when
    CacheByURIRegionRepository policy = ResponseCacheByURIBuilder.build(conf);

    //  then
    assertThat(policy.getRegions()).isNotEmpty().hasSize(3);
    assertThat(policy.getRegions().get(new URIMatcher("/api/v1/bestsellers/{D}"))).isEqualTo(new CacheRegion("bestsellers", 1800000, TimeUnit.MILLISECONDS, keyStrategy));
    assertThat(policy.getRegions().get(new URIMatcher("/api/v1/users/{S}"))).isEqualTo(new CacheRegion("users", 129000, TimeUnit.MILLISECONDS, keyStrategy));
    assertThat(policy.getRegions().get(new URIMatcher("/api/v1/events/{D}"))).isEqualTo(new CacheRegion("events", 4, TimeUnit.MILLISECONDS, keyStrategy));
  }

  private HttpServletRequest forURI(final String uri) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn(uri);
    return request;
  }

}