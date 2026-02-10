package com.example.inventappluis370.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class GetDashboardItemsUseCaseTest {

    private lateinit var getDashboardItemsUseCase: GetDashboardItemsUseCase

    @Before
    fun setUp() {
        getDashboardItemsUseCase = GetDashboardItemsUseCase()
    }

    @Test
    fun `invoke with Admin role returns all items`() {
        // Given
        val adminRole = "Administrador"

        // When
        val items = getDashboardItemsUseCase(adminRole)

        // Then
        // Admin: 6 comunes + 6 admin/gerente + 1 config = 13 items
        assertThat(items).hasSize(13)
        assertThat(items.any { it.title == "Usuarios" }).isTrue()
        assertThat(items.any { it.title == "Inventario" }).isTrue()
        assertThat(items.any { it.title == "Permisos" }).isTrue()
    }

    @Test
    fun `invoke with Tecnico role returns specific items`() {
        // Given
        val tecnicoRole = "Técnico"

        // When
        val items = getDashboardItemsUseCase(tecnicoRole)

        // Then
        // Un técnico debería ver las 6 opciones comunes + 1 de config = 7 items
        // (No ve Usuarios, Inventario, Garantías, Reportes)
        assertThat(items).hasSize(7)
        assertThat(items.any { it.title == "Usuarios" }).isFalse()
        assertThat(items.any { it.title == "Inventario" }).isFalse()
        assertThat(items.any { it.title == "Servicios" }).isTrue()
    }

     @Test
    fun `invoke with null role returns only common items`() {
        // Given
        val nullRole = null

        // When
        val items = getDashboardItemsUseCase(nullRole)

        // Then
        // Sin rol, solo debería ver las 6 opciones comunes + 1 de config = 7 items
        assertThat(items).hasSize(7)
        assertThat(items.any { it.title == "Usuarios" }).isFalse()
    }
}
