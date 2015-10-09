package dynks.cache;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jszczepankiewicz
 * @since 2015-04-17
 */
public interface KeyStrategy {
    String keyFor(HttpServletRequest request);
}
