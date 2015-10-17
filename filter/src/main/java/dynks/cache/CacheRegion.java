package dynks.cache;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static dynks.cache.CacheRegion.Cacheability.CACHED;
import static java.util.Objects.hash;

/**
 * Contains information whether specific region is cacheable. If it is then what is ttl and what is key strategy for building this region.
 *
 * @author jszczepankiewicz
 * @since 2015-03-26.
 */
public class CacheRegion {

  public enum Cacheability {
    CACHED,
    PASSTHROUGH
  }

  private final long ttl;
  private final int ttlInSeconds;
  private final TimeUnit ttlUnit;
  private final Cacheability cacheability;
  private final int hashCode;
  private final KeyStrategy keyStrategy;
  private final String id;

  public CacheRegion(String id, long ttl, TimeUnit ttlUnit, Cacheability cacheability, KeyStrategy keyStrategy) {
    this.id = id;
    this.ttl = ttl;
    this.ttlUnit = ttlUnit;
    this.ttlInSeconds = (ttl > 0 ? ((int) ttlUnit.toSeconds(ttl)) : 0);
    this.cacheability = cacheability;
    this.keyStrategy = keyStrategy;
    //  precomputed since all components immutable
    this.hashCode = hash(id, ttl, ttlUnit, cacheability, keyStrategy);
  }

  public CacheRegion(String id, long ttl, TimeUnit ttlUnit, KeyStrategy keyStrategy) {

    this.id = id;
    this.ttl = ttl;
    this.ttlUnit = ttlUnit;
    this.ttlInSeconds = (ttl > 0 ? ((int) ttlUnit.toSeconds(ttl)) : 0);
    this.keyStrategy = keyStrategy;
    this.cacheability = CACHED;
    this.hashCode = hash(id, ttl, ttlUnit, cacheability);
  }

  public long getTtl() {
    return ttl;
  }

  public int getTtlInSeconds() {
    return ttlInSeconds;
  }

  public TimeUnit getTtlUnit() {
    return ttlUnit;
  }

  public Cacheability getCacheability() {
    return cacheability;
  }

  public KeyStrategy getKeyStrategy() {
    return keyStrategy;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CacheRegion that = (CacheRegion) o;
    return
            Objects.equals(id, that.id) &&
                    Objects.equals(ttl, that.ttl) &&
                    Objects.equals(ttlUnit, that.ttlUnit) &&
                    Objects.equals(cacheability, that.cacheability) &&
                    Objects.equals(keyStrategy, this.keyStrategy);

  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}
