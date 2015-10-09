package dynks.cache;

import dynks.URIMatcher;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

import static dynks.cache.test.DynksAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by jszczepankiewicz on 2015-03-26.
 */
public class ResponseCacheByURIBuilderTest {

    @Test
    public void shouldLoadConfigurationFromFileOnClasspath() {

        //  given
        Config conf = ConfigFactory.load("dynks-test");
        KeyStrategy keyStrategy = new NamespacedURIKeyStrategy("tst");

        //  when
        ResponseCacheByURIPolicy policy = ResponseCacheByURIBuilder.build(conf);

        //  then
        assertThat(policy.getRegions()).isNotEmpty().hasSize(3);
        assertThat(policy.getRegions().get(new URIMatcher("/api/v1/bestsellers/{D}"))).isEqualTo(new CacheRegion(1800000, TimeUnit.MILLISECONDS,keyStrategy));
        assertThat(policy.getRegions().get(new URIMatcher("/api/v1/users/{S}"))).isEqualTo(new CacheRegion(129000, TimeUnit.MILLISECONDS,keyStrategy));
        assertThat(policy.getRegions().get(new URIMatcher("/api/v1/events/{D}"))).isEqualTo(new CacheRegion(4, TimeUnit.MILLISECONDS,keyStrategy));
    }

    private HttpServletRequest forURI(final String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

}