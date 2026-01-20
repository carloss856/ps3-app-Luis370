package com.example.inventappluis370.di

import android.util.Log
import com.example.inventappluis370.BuildConfig
import com.example.inventappluis370.data.remote.*
import com.example.inventappluis370.domain.repository.TokenRepository
import com.example.inventappluis370.core.network.TokenExtendManager
import com.example.inventappluis370.core.network.LenientDoubleAdapter
import com.example.inventappluis370.core.network.LenientIntAdapter
import com.example.inventappluis370.core.network.LenientStringListAdapter
import com.example.inventappluis370.core.network.LenientAnyToStringAdapter
import com.example.inventappluis370.core.network.MongoIdAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TOKEN_EXPIRES_HEADER = "X-Token-Expires-At"
    private const val SKIP_TOKEN_EXTEND_HEADER = "X-Skip-Token-Extend"

    private val PUBLIC_ROUTE_SUFFIXES = setOf(
        "/register",
        "/login",
        "/password/forgot",
        "/password/verify",
        "/password/reset"
    )

    private fun isPublicRoute(path: String): Boolean {
        val normalized = if (path.startsWith("/api/")) path.removePrefix("/api") else path
        return PUBLIC_ROUTE_SUFFIXES.any { normalized == it }
    }

    private fun isTokenExtendPath(path: String): Boolean {
        val normalized = if (path.startsWith("/api/")) path.removePrefix("/api") else path
        return normalized == "/token/extend"
    }

    private const val TAG = "NetworkModule"

    @Provides
    @Singleton
    @Named("default")
    fun provideOkHttpClient(
        tokenRepository: TokenRepository,
        tokenExtendManager: TokenExtendManager
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("Accept", "application/json")

                val path = originalRequest.url.encodedPath

                val skipExtend = originalRequest.header(SKIP_TOKEN_EXTEND_HEADER) == "1"
                val isExtendPath = isTokenExtendPath(path)
                val isPublic = isPublicRoute(path)

                if (!isPublic) {
                    val token = tokenRepository.getToken()
                    if (token != null) {
                        requestBuilder.header("Authorization", "Bearer $token")
                    }
                }

                val request = requestBuilder.build()
                val response = chain.proceed(request)

                response.header(TOKEN_EXPIRES_HEADER)?.let { newExpiresAt ->
                    tokenRepository.updateExpiration(newExpiresAt)
                }

                if (response.code == 401) {
                    tokenRepository.clearSession()
                }

                // Importante: NO intentar auto-extend en rutas públicas (login/register/etc.)
                // y nunca dejar que una excepción en este proceso tumbe la app.
                if (!isPublic && !skipExtend && !isExtendPath) {
                    try {
                        tokenExtendManager.maybeExtendToken()
                    } catch (t: Throwable) {
                        Log.e(TAG, "maybeExtendToken() falló para path=$path", t)
                    }
                }

                response
            }
            .build()
    }

    /**
     * Cliente HTTP dedicado para AuthApiService, sin auto-extend para evitar ciclos de DI.
     * Aún así agrega Authorization para rutas protegidas (logout, token/extend) y maneja 401.
     */
    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(tokenRepository: TokenRepository): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/json")

            val path = originalRequest.url.encodedPath

            if (!isPublicRoute(path)) {
                tokenRepository.getToken()?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            }

            val response = chain.proceed(requestBuilder.build())

            response.header(TOKEN_EXPIRES_HEADER)?.let { tokenRepository.updateExpiration(it) }
            if (response.code == 401) tokenRepository.clearSession()

            response
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            // Adapters lenientes para datos inconsistentes (Number o String numérico)
            .add(LenientIntAdapter)
            .add(LenientDoubleAdapter)
            // Para campos que canónicamente son arrays pero en docs legacy vienen como string
            .add(LenientStringListAdapter)
            // Para campos String anotados con @LenientString que a veces llegan como Number/Boolean (legacy)
            .add(LenientAnyToStringAdapter)
            // Para _id de Mongo: puede venir como string o como {"$oid":"..."}
            .add(MongoIdAdapter)
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @Named("default")
    fun provideRetrofit(@Named("default") okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@Named("auth") authOkHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("auth") authRetrofit: Retrofit): AuthApiService =
        authRetrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideServicioApiService(@Named("default") retrofit: Retrofit): ServicioApiService = retrofit.create(ServicioApiService::class.java)

    @Provides
    @Singleton
    fun provideRepuestoApiService(@Named("default") retrofit: Retrofit): RepuestoApiService = retrofit.create(RepuestoApiService::class.java)

    @Provides
    @Singleton
    fun provideSolicitudRepuestoApiService(@Named("default") retrofit: Retrofit): SolicitudRepuestoApiService = retrofit.create(SolicitudRepuestoApiService::class.java)

    @Provides
    @Singleton
    fun provideGarantiaApiService(@Named("default") retrofit: Retrofit): GarantiaApiService = retrofit.create(GarantiaApiService::class.java)

    @Provides
    @Singleton
    fun provideReporteApiService(@Named("default") retrofit: Retrofit): ReporteApiService = retrofit.create(ReporteApiService::class.java)

    @Provides
    @Singleton
    fun provideInventarioApiService(@Named("default") retrofit: Retrofit): InventarioApiService = retrofit.create(InventarioApiService::class.java)

    @Provides
    @Singleton
    fun provideUsuarioApiService(@Named("default") retrofit: Retrofit): UsuarioApiService = retrofit.create(UsuarioApiService::class.java)

    @Provides
    @Singleton
    fun provideEmpresaApiService(@Named("default") retrofit: Retrofit): EmpresaApiService = retrofit.create(EmpresaApiService::class.java)

    @Provides
    @Singleton
    fun provideEquipoApiService(@Named("default") retrofit: Retrofit): EquipoApiService = retrofit.create(EquipoApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificacionApiService(@Named("default") retrofit: Retrofit): NotificacionApiService = retrofit.create(NotificacionApiService::class.java)

    @Provides
    @Singleton
    fun provideRmaApiService(@Named("default") retrofit: Retrofit): RmaApiService = retrofit.create(RmaApiService::class.java)

    @Provides
    @Singleton
    fun provideTarifaServicioApiService(@Named("default") retrofit: Retrofit): TarifaServicioApiService = retrofit.create(TarifaServicioApiService::class.java)

    @Provides
    @Singleton
    fun providePropiedadEquipoApiService(@Named("default") retrofit: Retrofit): PropiedadEquipoApiService = retrofit.create(PropiedadEquipoApiService::class.java)

    @Provides
    @Singleton
    fun provideAutenticacionUsuarioApiService(@Named("default") retrofit: Retrofit): AutenticacionUsuarioApiService =
        retrofit.create(AutenticacionUsuarioApiService::class.java)
}
