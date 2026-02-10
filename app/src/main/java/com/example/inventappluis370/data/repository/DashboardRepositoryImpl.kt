package com.example.inventappluis370.data.repository

import com.example.inventappluis370.data.remote.DashboardApiService
import com.example.inventappluis370.domain.model.*
import com.example.inventappluis370.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApiService,
) : DashboardRepository {

    override suspend fun getDashboard(): DashboardData {
        val dto = api.getDashboard()

        val cards = dto.cards.map {
            DashboardKpiCard(
                key = it.key,
                title = it.title,
                value = it.value,
            )
        }

        val lists = dto.lists?.let { l ->
            DashboardLists(
                repuestosCriticos = l.repuestosCriticos.orEmpty().mapNotNull { r ->
                    val id = r.idRepuesto ?: return@mapNotNull null
                    RepuestoCritico(
                        idRepuesto = id,
                        nombreRepuesto = r.nombreRepuesto.orEmpty(),
                        cantidadDisponible = r.cantidadDisponible,
                        nivelCritico = r.nivelCritico,
                    )
                },
                notificacionesRecientes = l.notificacionesRecientes.orEmpty().mapNotNull { n ->
                    val id = n.idNotificacion ?: return@mapNotNull null
                    NotificacionReciente(
                        idNotificacion = id,
                        asunto = n.asunto,
                        estadoEnvio = n.estadoEnvio,
                        fechaEnvio = n.fechaEnvio,
                    )
                }
            )
        }

        return DashboardData(
            role = dto.role,
            cards = cards,
            lists = lists,
        )
    }
}
