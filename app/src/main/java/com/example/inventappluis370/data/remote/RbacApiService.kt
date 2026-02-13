package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.RbacResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * RBAC (deny-by-default) equivalente al web.
 * GET /api/rbac -> { schemaVersion, role, modules: {moduleKey:[actions]}, routes:[route.name] }
 */
interface RbacApiService {

    @GET("rbac")
    suspend fun getRbac(): Response<RbacResponse>
}
