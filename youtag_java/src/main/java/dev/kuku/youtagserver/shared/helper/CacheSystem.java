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
    //TODO Cache system with limit
    private final ConcurrentHashMap<String, Map<String, Object>> cacheStore = new ConcurrentHashMap<>();

    public void cache(String uniqueKey, Object objectToCache) {
        if (objectToCache == null) {
            log.warn("Skipping caching as objectToCache is null");
            return;
        }
        log.info("caching key {}  in {} to store: {}", uniqueKey, objectToCache.getClass().getName(), objectToCache);
        getStore(objectToCache.getClass().getName()).put(uniqueKey, objectToCache);
    }

    public <T> T getObject(String uniqueKey, Class<T> objClass) {
        T obj = (T) getStore(objClass.getName()).get(uniqueKey);
        if (obj == null) {
            log.info("Failed to find obj in cache: {}", uniqueKey);
        } else {
            log.info("Found obj in cache: {}", uniqueKey);
        }
        return obj;
    }

    public void evict(String uniqueKey, Class<?> objClass) {
        log.info("Removing key {} from store {}", uniqueKey, objClass.getName());
        if (cacheStore.containsKey(objClass.getName())) {
            cacheStore.get(objClass.getName()).remove(uniqueKey);
            //If cacheStore is empty then remove the store
            if (cacheStore.get(objClass.getName()).isEmpty()) {
                cacheStore.remove(objClass.getName());
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
