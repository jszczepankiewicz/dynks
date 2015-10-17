package dynks.redis;

import dynks.cache.CacheRegion;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.valueOf;

/**
 * Lua command to delete all entries by given pattern and return number of deleted entries. It is loaded using optimum
 * evalsha command with autodetection of loading even when redis instance will go down and repeated submittion
 * of lua script to redis will be required.
 *
 * @author jszczepankiewicz
 * @since 2015-10-10
 */
public class DeleteAllEntriesInRegionCommand {

  private Map<CacheRegion, String> cachedEvictionWildcardPatterns = new ConcurrentHashMap<>();
  private String lastScriptSha = "firstUseWillThrowException";

  private final static String SCRIPT = "" +
          "local p = KEYS[1]\n" + //  pattern
          "local l = KEYS[2]\n" + //  maxEntriesDeletedInOneBatch
          "local c = 0\n" +
          "local vl = redis.call('keys', p)\n" +
          "if vl then\n" +
          "  for i = 1, #vl do\n" +
          "    if c == l then " +
          "      return c\n" +
          "    end\n" +
          "    redis.call('del', vl[i])\n" +
          "    c = c + 1\n" +
          "  end\n" +
          "end\n" +
          "return c";


  public Long execute(Jedis jedis, CacheRegion region, final int maxEntriesDeletedInOneBatch) {

    long allRemoved = 0;
    long lastBatchRemoved = 0;
    //  need to be converted to String for Lua
    final String limit = valueOf(maxEntriesDeletedInOneBatch);

    do {
      lastBatchRemoved = executeOneBatch(jedis, region, limit);
      allRemoved += lastBatchRemoved;
    } while (lastBatchRemoved > 0);

    return allRemoved;
  }

  private Long executeOneBatch(Jedis jedis, CacheRegion region, final String maxEntriesDeletedInOneBatch) {

    Long affected;
    final String pattern = getRegionEvictionPattern(region);

    try {
      affected = (Long) jedis.evalsha(lastScriptSha, 2, pattern, maxEntriesDeletedInOneBatch);
    } catch (JedisDataException e) {
      //  check whether script submission to backend is required
      if (e.getMessage().startsWith("NOSCRIPT")) {
        lastScriptSha = new String(jedis.scriptLoad(SCRIPT.getBytes()));
        affected = (Long) jedis.evalsha(lastScriptSha, 2, pattern, maxEntriesDeletedInOneBatch);
      } else {
        throw e;
      }
    }

    return affected;
  }

  private String getRegionEvictionPattern(CacheRegion region) {

    String pattern = cachedEvictionWildcardPatterns.get(region);

    if (pattern == null) {
      pattern = region.getKeyStrategy().wildcardKeyFor(region);
      cachedEvictionWildcardPatterns.put(region, pattern);
    }

    return pattern;
  }
}
