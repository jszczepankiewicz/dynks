package dynks.cache;


import dynks.URIMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dynks.cache.test.DynksAssertions.assertThat;
import static dynks.cache.CacheRegion.Cacheability.CACHED;
import static dynks.cache.CacheRegion.Cacheability.PASSTHROUGH;
import static java.util.Collections.EMPTY_MAP;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jszczepankiewicz on 2015-03-26.
 */
public class ResponseCacheByURIPolicyTest {

    private static final String SOME_CACHED_URI = "/api/v1/bestsellers/423435";
    private static final String SOME_CACHED_URI_PATTERN = "/api/v1/bestsellers/{D}";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throwNPEForNulledRegionList() {

        //  then
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("List of cache regions should not be null");

        //  when
        new ResponseCacheByURIPolicy(null);
    }

    @Test
    public void tolerateEmptyListOfRegions() {

        new ResponseCacheByURIPolicy(EMPTY_MAP);
    }

    @Test
    public void returnPassthroughRegionForURINotMatched() {

        //  given
        ResponseCacheByURIPolicy policy = new ResponseCacheByURIPolicy(havingCacheRegions());
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
        ResponseCacheByURIPolicy policy = new ResponseCacheByURIPolicy(havingCacheRegions());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(SOME_CACHED_URI);

        //  when
        CacheRegion region = policy.getfor(request);

        //  then
        assertThat(region).hasTtl(100).hasTtlUnit(MINUTES).hasVolatility(CACHED);
    }

    private Map<URIMatcher, CacheRegion> havingCacheRegions() {
        Map<URIMatcher, CacheRegion> regions = new ConcurrentHashMap<>();
        KeyStrategy keyStrategy = new NamespacedURIKeyStrategy("");
        CacheRegion region1 = new CacheRegion(100, MINUTES, keyStrategy);
        CacheRegion region2 = new CacheRegion(30, SECONDS, keyStrategy);
        regions.put(new URIMatcher(SOME_CACHED_URI_PATTERN), region1);
        regions.put(new URIMatcher("/something"), region2);
        return regions;
    }


}