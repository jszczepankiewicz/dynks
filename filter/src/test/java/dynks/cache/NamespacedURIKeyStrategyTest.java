package dynks.cache;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static dynks.cache.test.DynksAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jszczepankiewicz
 * @since 2015-04-17
 */
public class NamespacedURIKeyStrategyTest {

    private static final String NAMESPACE = "rm";

    @Test
    public void twoInstancesShouldBeEqual(){

        //  given
        NamespacedURIKeyStrategy keyStrategy1 = new NamespacedURIKeyStrategy(NAMESPACE);
        NamespacedURIKeyStrategy keyStrategy2 = new NamespacedURIKeyStrategy(NAMESPACE);

        //  when
        int hashCode1 = keyStrategy1.hashCode();
        int hashCode2 = keyStrategy2.hashCode();

        //  then
        assertThat(hashCode1).isEqualTo(hashCode2);
        assertThat(keyStrategy1).isEqualTo(keyStrategy2);
    }

    @Test
    public void generatedKeyShouldBeEqualToRequestURIWithNamespace(){

        //  given
        final String uri = "/v1/superduper/xyz";
        final HttpServletRequest req = forURI(uri);
        final KeyStrategy keyStrategy = new NamespacedURIKeyStrategy(NAMESPACE);

        //  when
        String key = keyStrategy.keyFor(req);

        //  then
        assertThat(key).isEqualTo(NAMESPACE + ":" + uri);
    }

    private HttpServletRequest forURI(final String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

}