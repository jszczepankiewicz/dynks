package dynks.cache;

/**
 * Repository for accessing caching layer. It is critical to properly implement all methods that may potentially fail
 * due to persistence layer to throw CacheRepositoryException in case of any underlying problems so that Hardened mode
 * will be properly handled by the CachingFilter. Please take a look at RedisCacheRepositoryHardenedTest to see example
 * test against that.
 *
 * @author jszczepankiewicz
 * @since 2015-04-01.
 */
public interface CacheRepository {

  /**
   * Attempt to get value identified by key. The following return states are possible:
   * <ul>
   * <li>there is no value for given key, CacheResult will return: upsertNeeded: true, payload: null, storedEtag: null</li>
   * <li>there is value for given key but etag == null, CacheResult will return: upsertNeeded: false, payload contains the value,
   * storedEtag: corresponding etag value associated with given key</li>
   * <li>there is value for given key and etag == storedEtag for given value, CacheResult will return: upsertNeeded: false,
   * payload contain null, storedEtag: null</li>
   * <li>there is value for given key and etag != storedEtag for given value, CacheResult will return: upsertNeeded: false,
   * payload contain latest version, storedEtag: etag corresponding with given value</li>
   * <li>client does not provide etag (null): upsertNeeded: depends on whether cache contains value</li>
   * </ul>
   *
   * @param key
   * @param etag
   * @return
   */
  CacheQueryResult fetchIfChanged(String key, String etag) throws CacheRepositoryException;

  /**
   * Insert or update value identified by key and mark with given etag regardless of existing etag value.
   *
   * @param key         value identifier (not null)
   * @param content     value itself
   * @param etag        etag value used as hash for version (not null)
   * @param contentType contentType
   * @param encoding    encoding of content
   */
  void upsert(String key, String content, String etag, String contentType, String encoding, CacheRegion region) throws CacheRepositoryException;

  /**
   * Remove single value identified by key. WARNING: current implementation does NOT remove any tracking of this
   * key in index for evictableById regions.
   *
   * @param key value identifier (not null)
   */
  void remove(String key) throws CacheRepositoryException;

  /**
   * Evicts all entries belonging to given region.
   * WARNING: this is blocking operation with time spent proportional to entries to delete.
   *
   * @param region to be purged
   * @return number of removed entries
   */
  long evictRegion(CacheRegion region) throws CacheRepositoryException;

  /**
   * Evict all entries from given region using specified max entries deleted in one batch. Removing entries
   * from region is implemented in batches that are removing up till maxEntriesDeletedInOneBatch entries till
   * there is no single entry from given region. This operation will be translated to at least one invocation of
   * redis command.
   * <p>
   * WARNING: blocking operation with time spent proportional to entries being deleted.
   *
   * @param region                      to be purged
   * @param maxEntriesDeletedInOneBatch maximum number of units from given region that will be removed in one
   *                                    redis command.
   * @return number of removed entries
   */
  long evictRegion(CacheRegion region, int maxEntriesDeletedInOneBatch) throws CacheRepositoryException;

  /**
   * Clean up resources.
   */
  void dispose();

  /**
   * TODO:
   *
   * @return
   */
  int getMaxEntriesDeletedInOneBatch();
}
