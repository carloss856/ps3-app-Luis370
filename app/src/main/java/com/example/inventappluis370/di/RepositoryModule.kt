package com.example.inventappluis370.di

import com.example.inventappluis370.data.repository.*
import com.example.inventappluis370.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindServicioRepository(servicioRepositoryImpl: ServicioRepositoryImpl): ServicioRepository

    @Binds
    @Singleton
    abstract fun bindRepuestoRepository(repuestoRepositoryImpl: RepuestoRepositoryImpl): RepuestoRepository

    @Binds
    @Singleton
    abstract fun bindSolicitudRepuestoRepository(solicitudRepuestoRepositoryImpl: SolicitudRepuestoRepositoryImpl): SolicitudRepuestoRepository

    @Binds
    @Singleton
    abstract fun bindGarantiaRepository(garantiaRepositoryImpl: GarantiaRepositoryImpl): GarantiaRepository

    @Binds
    @Singleton
    abstract fun bindReporteRepository(reporteRepositoryImpl: ReporteRepositoryImpl): ReporteRepository

    @Binds
    @Singleton
    abstract fun bindInventarioRepository(inventarioRepositoryImpl: InventarioRepositoryImpl): InventarioRepository

    @Binds
    @Singleton
    abstract fun bindUsuarioRepository(usuarioRepositoryImpl: UsuarioRepositoryImpl): UsuarioRepository

    @Binds
    @Singleton
    abstract fun bindEmpresaRepository(empresaRepositoryImpl: EmpresaRepositoryImpl): EmpresaRepository

    @Binds
    @Singleton
    abstract fun bindEquipoRepository(equipoRepositoryImpl: EquipoRepositoryImpl): EquipoRepository

    @Binds
    @Singleton
    abstract fun bindNotificacionRepository(notificacionRepositoryImpl: NotificacionRepositoryImpl): NotificacionRepository

    @Binds
    @Singleton
    abstract fun bindRmaRepository(rmaRepositoryImpl: RmaRepositoryImpl): RmaRepository

    @Binds
    @Singleton
    abstract fun bindTarifaServicioRepository(tarifaServicioRepositoryImpl: TarifaServicioRepositoryImpl): TarifaServicioRepository
    
    @Binds
    @Singleton
    abstract fun bindPropiedadEquipoRepository(propiedadEquipoRepositoryImpl: PropiedadEquipoRepositoryImpl): PropiedadEquipoRepository

    @Binds
    @Singleton
    abstract fun bindAutenticacionUsuarioRepository(
        impl: AutenticacionUsuarioRepositoryImpl
    ): AutenticacionUsuarioRepository
}
