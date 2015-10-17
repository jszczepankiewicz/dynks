package dynks;

import dynks.cache.CacheRegion;
import dynks.cache.CacheRegionRepository;
import dynks.cache.CacheRepository;

import java.util.Optional;

/**
 * Client used to communicate from withing same classloader (application) with dynks web cache.
 * It is thread safe client implemented as singleton and instance may be stored as it is not changing
 * throughout the lifecycle of application. Singleton is initialized by CachingServlet
 * during the configuration phase of servlet filter initialized by servlet container.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * Frontend frontend = Frontend.get();
 * long evicted = frontend.evictRegion(region);
 * }
 * </pre>
 *
 * @author jszczepankiewicz
 * @since 2015-10-10
 */
public class Frontend {

  private static Frontend instance;
  private final CacheRepository repository;
  private final CacheRegionRepository regionRepository;

  /**
   * Internal constructor do not access from application code.
   *
   * @param repository
   * @param regionRepository
   */
  private Frontend(CacheRepository repository, CacheRegionRepository regionRepository) {
    this.repository = repository;
    this.regionRepository = regionRepository;
  }

  /**
   * Initializes frontend as singleton. Should be invoked exactly once just after ServletFilter is
   * configured. Do not access from application code.
   *
   * @param repository
   * @param regionRepository
   */
  public static void initialize(CacheRepository repository, CacheRegionRepository regionRepository) {

    if (repository == null) {
      throw new NullPointerException("CacheRepository to register should not be null");
    }

    if (regionRepository == null) {
      throw new NullPointerException("CacheRegionRepository to register should not be null");
    }

    synchronized (Frontend.class) {

      if (instance != null) {
        throw new IllegalStateException("Frontend already initialized");
      }

      instance = new Frontend(repository, regionRepository);
    }
  }

  /**
   * Access frontend singleton to be used in application code. The only permitted way to obtain
   * Frontend instance.
   *
   * @return dynks client interface
   */
  public static Frontend get() {

    if (instance == null) {
      throw new IllegalStateException("Frontend not yet initialized :(. This indicates serious internal problem.");
    }

    return instance;
  }

  /**
   * Retrieves CacheRegion by given id or throws IllegalArgumentException if can not found or id is invalid (empty or null).
   *
   * @param id
   * @return
   */
  private CacheRegion resolveRegion(String id) {

    if (id == null) {
      throw new IllegalArgumentException("Id of region should not be null");
    }

    if (id.trim().length() == 0) {
      throw new IllegalArgumentException("Id of region should not be empty");
    }

    Optional<CacheRegion> region = regionRepository.getById(id);

    if (!region.isPresent()) {
      throw new IllegalArgumentException("Attempt to evict not existing region: '" + id + "', please check id and try again");
    }

    return region.get();
  }

  /**
   * Evicts all entries from within given region and returns number of affected entries. Redis does not support
   * directly removal of multiple tuples in one command. Thus operation under the hood is implemented as
   * Lua script executed on server. Removing large number of entries may cause the script to timeout. Redis has built-in
   * prevention for timeout by default set to 5sec (lua-time-limit in redis config). There is inherent risk that
   * removal of large number of entries may hit timeout limit. To overcome that dynks is deleting entries in multiple
   * batches. Based on hardware resources, usage of resources during the operation, Redis configuration maximum
   * number of entries that can be deleted in one batch may be different. By lowering number of entries per batch risk of
   * timeout will decline but at the same time total duraction of eviction will raise (cause more attempts to retrieve
   * keys to delete + more Lua script invocation will occur). If not defined there is default value of <pre>maxEntriesDeletedInOneBatch</pre>
   * used defined by {@link dynks.redis.RedisCacheRepositoryConfigBuilder#DEFAULT_MAX_ENTRIES_DELETED_IN_ONE_BATCH}.
   * Value of maxEntriesDeletedInOneBatch is resolved in following way:
   * <ol>
   * <li>taken from optional argument to evictRegion</li>
   * <li>taken from dynks.conf file from optional param (<pre>dynks.redis.maxEntriesDeletedInOneBatch</pre>)</li>
   * <li>if above will not be successful use default value defined by {@link dynks.redis.RedisCacheRepositoryConfigBuilder#DEFAULT_MAX_ENTRIES_DELETED_IN_ONE_BATCH}</li>
   * </ol>
   *
   * @param id
   * @return
   */
  public long evictRegion(String id) {
    return repository.evictRegion(resolveRegion(id));
  }

  /**
   * Evicts region with specified maximum number of deleted units per one delete batch.
   *
   * @param id
   * @param maxEntriesDeletedInOneBatch
   * @return
   * @see Frontend#evictRegion(String) for more details
   */
  public long evictRegion(String id, int maxEntriesDeletedInOneBatch) {

    if (maxEntriesDeletedInOneBatch < 1) {
      throw new IllegalArgumentException("maxEntriesDeletedInOneBatch should not be at least 1");
    }

    return repository.evictRegion(resolveRegion(id));
  }

  public int getDefaultMaxEntriesDeletedInOneBatch() {
    return repository.getMaxEntriesDeletedInOneBatch();
  }

}
