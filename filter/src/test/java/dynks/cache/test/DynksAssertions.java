package dynks.cache.test;

import dynks.HttpServletResponseAssert;
import dynks.JedisPoolConfigAssert;
import dynks.cache.*;
import dynks.cache.CacheRegionAssert;
import org.assertj.core.api.Assertions;
import redis.clients.jedis.JedisPoolConfig;
import dynks.cache.test.integration.ServerResponse;
import dynks.cache.test.integration.ServerResponseAssert;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by jszczepankiewicz on 2015-03-26.
 */
public class DynksAssertions extends Assertions {

    /**
     * An entry point for ServerResponseAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myServerResponse)</code> and get specific assertion with code completion.
     * @param actual the ServerResponse we want to make assertions on.
     * @return a new <code>{@link ServerResponseAssert}</code>
     */
    public static ServerResponseAssert assertThat(ServerResponse actual) {
        return new ServerResponseAssert(actual);
    }

    public static HttpServletResponseAssert assertThat(HttpServletResponse actual) {return new HttpServletResponseAssert(actual);}

    /**
     * An entry point for CacheRegionAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myCacheRegion)</code> and get specific assertion with code completion.
     * @param actual the CacheRegion we want to make assertions on.
     * @return a new <code>{@link CacheRegionAssert}</code>
     */
    public static CacheRegionAssert assertThat(CacheRegion actual) {
        return new CacheRegionAssert(actual);
    }

    /**
     * An entry point for JedisPoolConfigAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myJedisPoolConfig)</code> and get specific assertion with code completion.
     * @param actual the JedisPoolConfig we want to make assertions on.
     * @return a new <code>{@link JedisPoolConfigAssert}</code>
     */
    public static JedisPoolConfigAssert assertThat(JedisPoolConfig actual) {
        return new JedisPoolConfigAssert(actual);
    }

    /**
     * An entry point for EntryAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myEntry)</code> and get specific assertion with code completion.
     * @param actual the Entry we want to make assertions on.
     * @return a new <code>{@link EntryAssert}</code>
     */
    public static EntryAssert assertThat(Entry actual) {
        return new EntryAssert(actual);
    }

    /**
     * An entry point for CacheResultAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myEntry)</code> and get specific assertion with code completion.
     * @param actual the CacheResult we want to make assertions on.
     * @return a new <code>{@link CacheResultAssert}</code>
     */
    public static CacheResultAssert assertThat(CacheQueryResult actual) {
        return new CacheResultAssert(actual);
    }
}
