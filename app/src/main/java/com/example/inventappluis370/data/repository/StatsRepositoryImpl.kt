package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.remote.StatsApiService
import com.example.inventappluis370.domain.model.*
import com.example.inventappluis370.domain.repository.StatsRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val api: StatsApiService,
) : StatsRepository {

    private data class CacheKey(val module: String, val period: StatsPeriod, val from: String?, val to: String?)
    private data class CacheEntry(val value: ModuleStats, val expiresAtMs: Long)

    private val mutex = Mutex()
    private val cache = mutableMapOf<CacheKey, CacheEntry>()

    private val ttlMs = 5 * 60 * 1000L

    override suspend fun getStatsCached(module: String, period: StatsPeriod, from: String?, to: String?): ModuleStats {
        val key = CacheKey(module, period, from, to)
        val now = System.currentTimeMillis()

        mutex.withLock {
            val hit = cache[key]
            if (hit != null && hit.expiresAtMs > now) return hit.value
        }

        val dto = api.getStats(module = module, period = period.apiValue, from = from, to = to)
        val result = ModuleStats(
            module = dto.module ?: module,
            period = period,
            total = dto.total ?: 0,
            buckets = dto.buckets.mapNotNull { b ->
                val label = b.label ?: return@mapNotNull null
                StatsBucket(label = label, count = b.count ?: 0)
            }
        )

        mutex.withLock {
            cache[key] = CacheEntry(result, now + ttlMs)
        }

        return result
    }
}
