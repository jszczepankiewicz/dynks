package dynks.cache;


import dynks.URIMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static dynks.cache.CacheRegion.Cacheability.CACHED;
import static dynks.cache.CacheRegion.Cacheability.PASSTHROUGH;
import static dynks.cache.test.DynksAssertions.assertThat;
import static java.util.Collections.EMPTY_MAP;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jszczepankiewicz on 2015-03-26.
 */
public class CacheByURIRegionRepositoryTest {

  private static final String SOME_CACHED_URI = "/api/v1/bestsellers/423435";
  private static final String SOME_CACHED_URI_PATTERN = "/api/v1/bestsellers/{D}";

  private static final KeyStrategy NAMESPACED_TEST_KEY_STRATEGY = new NamespacedURIKeyStrategy("");
  private static final CacheRegion REGION_100_MINUTES = new CacheRegion("100minutes", 100, MINUTES, NAMESPACED_TEST_KEY_STRATEGY);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void throwNPEForNulledRegionList() {

    //  then
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("List of cache regions should not be null");

    //  when
    new CacheByURIRegionRepository(null);
  }

  @Test
  public void tolerateEmptyListOfRegions() {

    new CacheByURIRegionRepository(EMPTY_MAP);
  }

  @Test
  public void returnOptionalFalseForRegionNotFoundById() {

    //  given
    CacheByURIRegionRepository policy = new CacheByURIRegionRepository(havingCacheRegions());

    //  when
    Optional<CacheRegion> region = policy.getById("nonexistingid");

    //  then
    assertThat(region).isEmpty();
  }

  @Test
  public void returnRegionById() {

    //  given
    CacheByURIRegionRepository policy = new CacheByURIRegionRepository(havingCacheRegions());

    //  when
    Optional<CacheRegion> region = policy.getById(REGION_100_MINUTES.getId());

    //  then
    assertThat(region).contains(REGION_100_MINUTES);
  }

  @Test
  public void returnPassthroughRegionForURINotMatched() {

    //  given
    CacheByURIRegionRepository policy = new CacheByURIRegionRepository(havingCacheRegions());
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("someUnRegisteredURI");

    //  when
    CacheRegion region = policy.getfor(request);

    //  then
    assertThat(region).hasTtl(0).hasTtlUnit(null).hasVolatility(PASSTHROUGH);

  }

  @Test
  public void returnMatchedRegionForURI() {

    //  given
    CacheByURIRegionRepository policy = new CacheByURIRegionRepository(havingCacheRegions());
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn(SOME_CACHED_URI);

    //  when
    CacheRegion region = policy.getfor(request);

    //  then
    assertThat(region).hasTtl(100).hasTtlUnit(MINUTES).hasVolatility(CACHED);
  }

  private Map<URIMatcher, CacheRegion> havingCacheRegions() {
    Map<URIMatcher, CacheRegion> regions = new ConcurrentHashMap<>();
    CacheRegion region2 = new CacheRegion("30seconds", 30, SECONDS, NAMESPACED_TEST_KEY_STRATEGY);
    regions.put(new URIMatcher(SOME_CACHED_URI_PATTERN), REGION_100_MINUTES);
    regions.put(new URIMatcher("/something"), region2);
    return regions;
  }


}