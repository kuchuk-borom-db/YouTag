package dev.kuku.youtagserver.shared.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheSystem {
    private final ConcurrentHashMap<String, Map<String, Object>> cacheStore = new ConcurrentHashMap<>();

    public void cache(String cacheName, String uniqueKey, Object objectToCache) {
        log.info("caching key {} to store {} : {}", cacheName, uniqueKey, objectToCache);
        getStore(cacheName).put(uniqueKey, objectToCache);
    }

    public Object getObject(String cacheName, String uniqueKey) {
        var obj = getStore(cacheName).get(uniqueKey);
        log.info("Found object {} in cache", obj);
        return obj;
    }

    public void evict(String cacheName, String uniqueKey) {
        log.info("Removing key {} from store {}", uniqueKey, cacheName);
        if (cacheStore.containsKey(cacheName)) {
            cacheStore.get(cacheName).remove(cacheName);
            if (cacheStore.get(cacheName).isEmpty()) {
                cacheStore.remove(cacheName);
            }
        }
    }


    private Map<String, Object> getStore(String name) {
        if (!cacheStore.containsKey(name)) {
            cacheStore.put(name, new HashMap<>());
        }
        return cacheStore.get(name);
    }
}
