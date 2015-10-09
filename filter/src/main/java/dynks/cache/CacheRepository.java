package dynks.cache;

import java.util.concurrent.TimeUnit;

/**
 * Created by jszczepankiewicz on 2015-04-01.
 */
public interface CacheRepository {

    /**
     * Attempt to get value identified by key. The following return states are possible:
     * <ul>
     * <li>there is no value for given key, CacheResult will return: upsertNeeded: true, payload: null, storedEtag: null</li>
     * <li>there is value for given key but etag == null, CacheResult will return: upsertNeeded: false, payload contains the value,
     * storedEtag: corresponding etag value associated with given key</li>
     * <li>there is value for given key and etag == storedEtag for given value, CacheResult will return: upsertNeeded: false,
     *  payload contain null, storedEtag: null</li>
     * <li>there is value for given key and etag != storedEtag for given value, CacheResult will return: upsertNeeded: false,
     * payload contain latest version, storedEtag: etag corresponding with given value</li>
     * <li>client does not provide etag (null) -> upsertNeeded: depends on whether cache contains value</li>
     * </ul>
     *
     *
     * @param key
     * @param etag
     * @return
     */
    CacheQueryResult fetchIfChanged(String key, String etag);

    /**
     * Insert or update value identified by key and mark with given etag regardless of existing etag value.
     *
     * @param key     value identifier (not null)
     * @param content value itself
     * @param etag    etag value used as hash for version (not null)
     * @param contentType contentType
     * @param encoding encoding of content
     */
    void upsert(String key, String content, String etag, String contentType, String encoding, long ttl, TimeUnit ttlUnit);

    /**
     * Remove value identified by key.
     *
     * @param key value identifier (not null)
     */
    void remove(String key);


    /**
     * Clean up resources.
     */
    void dispose();
}
