package dynks.cache;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jszczepankiewicz on 2015-03-26.
 */
public interface ResponseCachePolicy {

    CacheRegion getfor(HttpServletRequest request);

}
