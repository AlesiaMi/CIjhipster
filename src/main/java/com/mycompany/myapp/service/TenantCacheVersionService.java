package com.mycompany.myapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TenantCacheVersionService {

    private static final Logger LOG = LoggerFactory.getLogger(TenantCacheVersionService.class);

    private static final String USER_VERSION_PREFIX = "tenant-cache-version:user:";
    private static final String GLOBAL_VERSION_KEY = "tenant-cache-version:global";

    private final StringRedisTemplate redisTemplate;

    public TenantCacheVersionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long getVersion(Long ownerId) {
        String value = redisTemplate.opsForValue().get(versionKey(ownerId));

        if (value == null) {
            return 0L;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid tenant cache version '{}' for owner {}", value, ownerId);
            return 0L;
        }
    }

    public String namespace(Long ownerId) {
        long version = getVersion(ownerId);

        if (ownerId == null) {
            return "global:v" + version;
        }

        return "owner:" + ownerId + ":v" + version;
    }

    public void invalidateAfterCommit(Long ownerId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        invalidate(ownerId);
                    }
                }
            );
        } else {
            invalidate(ownerId);
        }
    }

    /**
     * Инвалидирует кэш конкретного владельца и глобальный ADMIN-кэш.
     */
    public void invalidate(Long ownerId) {
        if (ownerId != null) {
            Long userVersion = redisTemplate.opsForValue().increment(USER_VERSION_PREFIX + ownerId);

            LOG.debug("Tenant cache invalidated for owner {}. New version: {}", ownerId, userVersion);
        }

        Long globalVersion = redisTemplate.opsForValue().increment(GLOBAL_VERSION_KEY);

        LOG.debug("Global cache invalidated. New version: {}", globalVersion);
    }

    public void invalidateGlobal() {
        Long globalVersion = redisTemplate.opsForValue().increment(GLOBAL_VERSION_KEY);

        LOG.debug("Global cache invalidated. New version: {}", globalVersion);
    }

    private String versionKey(Long ownerId) {
        return ownerId == null ? GLOBAL_VERSION_KEY : USER_VERSION_PREFIX + ownerId;
    }
}
