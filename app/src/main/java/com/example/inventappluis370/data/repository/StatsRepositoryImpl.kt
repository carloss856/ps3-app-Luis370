package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.remote.StatsApiService
import com.example.inventappluis370.domain.model.*
import com.example.inventappluis370.domain.repository.StatsRepository
import com.example.inventappluis370.domain.repository.UsuarioRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val api: StatsApiService,
    private val usuarioRepository: UsuarioRepository,
) : StatsRepository {

    private data class CacheKey(val module: String, val period: StatsPeriod, val from: String?, val to: String?)
    private data class CacheEntry(val value: ModuleStats, val expiresAtMs: Long)

    private val mutex = Mutex()
    private val cache = mutableMapOf<CacheKey, CacheEntry>()

    private val ttlMs = 5 * 60 * 1000L

    private val moduleAliases = mapOf(
        "usuarios" to listOf("usuarios", "users", "autenticacion-usuarios")
    )

    override suspend fun getStatsCached(module: String, period: StatsPeriod, from: String?, to: String?): ModuleStats {
        val key = CacheKey(module, period, from, to)
        val now = System.currentTimeMillis()

        mutex.withLock {
            val hit = cache[key]
            if (hit != null && hit.expiresAtMs > now) return hit.value
        }

        val aliases = moduleAliases[module] ?: listOf(module)
        var firstResult: ModuleStats? = null
        var resolved: ModuleStats? = null
        for (candidate in aliases) {
            val candidateResult = fetchModuleStats(
                requestedModule = module,
                apiModule = candidate,
                period = period,
                from = from,
                to = to
            )
            if (firstResult == null) firstResult = candidateResult
            if (candidateResult.total > 0 || candidateResult.buckets.isNotEmpty()) {
                resolved = candidateResult
                break
            }
        }
        val result = resolved ?: firstResult ?: ModuleStats(
            module = module,
            period = period,
            total = 0,
            buckets = emptyList()
        )

        mutex.withLock {
            cache[key] = CacheEntry(result, now + ttlMs)
        }

        return result
    }

    private suspend fun fetchModuleStats(
        requestedModule: String,
        apiModule: String,
        period: StatsPeriod,
        from: String?,
        to: String?
    ): ModuleStats {
        return try {
            val dto = api.getStats(module = apiModule, period = period.apiValue, from = from, to = to)
            ModuleStats(
                module = requestedModule,
                period = period,
                total = dto.total ?: 0,
                buckets = dto.buckets.mapNotNull { b ->
                    val label = b.label ?: return@mapNotNull null
                    StatsBucket(label = label, count = b.count ?: 0)
                }
            )
        } catch (e: HttpException) {
            if (e.code() == 404 && requestedModule == "usuarios") {
                val total = usuarioRepository.getUsers().getOrNull()?.size ?: 0
                ModuleStats(
                    module = requestedModule,
                    period = period,
                    total = total,
                    buckets = emptyList()
                )
            } else {
                throw e
            }
        }
    }
}

