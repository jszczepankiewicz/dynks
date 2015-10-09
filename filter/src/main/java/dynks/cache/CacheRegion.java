package dynks.cache;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static dynks.cache.CacheRegion.Cacheability.CACHED;
import static java.util.Objects.hash;

/**
 * Created by jszczepankiewicz on 2015-03-26.
 */
public class CacheRegion {

    public enum Cacheability {
        CACHED,
        PASSTHROUGH
    }

    private final long ttl;
    private final TimeUnit ttlUnit;
    private final Cacheability cacheability;
    private final int hashCode;
    private final KeyStrategy keyStrategy;

    public CacheRegion(long ttl, TimeUnit ttlUnit, Cacheability cacheability, KeyStrategy keyStrategy) {
        this.ttl = ttl;
        this.ttlUnit = ttlUnit;
        this.cacheability = cacheability;
        this.keyStrategy = keyStrategy;
        //  precomputed since all components immutable
        this.hashCode = hash(ttl, ttlUnit, cacheability, keyStrategy);
    }

    public CacheRegion(long ttl, TimeUnit ttlUnit, KeyStrategy keyStrategy) {

        this.ttl = ttl;
        this.ttlUnit = ttlUnit;
        this.keyStrategy = keyStrategy;
        this.cacheability = CACHED;

        this.hashCode = hash(ttl, ttlUnit, cacheability);
    }

    public long getTtl() {
        return ttl;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheRegion that = (CacheRegion) o;
        return Objects.equals(ttl, that.ttl) &&
                Objects.equals(ttlUnit, that.ttlUnit) &&
                Objects.equals(cacheability, that.cacheability) &&
                Objects.equals(keyStrategy, this.keyStrategy);

    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
