package com.example.inventappluis370.ui.login

import com.example.inventappluis370.data.model.LoginRequest
import com.example.inventappluis370.data.model.LoginResponse
import com.example.inventappluis370.data.model.Usuario
import com.example.inventappluis370.domain.repository.AuthRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

@ExperimentalCoroutinesApi
class AuthViewModelTest {

    // Usamos un dispatcher de test para controlar las corutinas
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        // Establecemos el dispatcher principal para los tests de ViewModels
        Dispatchers.setMain(testDispatcher)
        // Creamos un mock del repositorio
        authRepository = mock()
        // Creamos la instancia del ViewModel con el repositorio mockeado
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        // Reseteamos el dispatcher principal
        Dispatchers.resetMain()
    }

    @Test
    fun `login success updates uiState to Success`() = runTest {
        // Given
        val loginRequest = LoginRequest("test@test.com", "password")
        val mockUser = Usuario(
            idPersona = "USR-1",
            nombre = "Test User",
            email = "test@test.com",
            telefono = null,
            tipo = "Administrador",
            idEmpresa = null,
            validadoPorGerente = false,
            recibirNotificaciones = true,
            tiposNotificacion = emptyList()
        )
        val mockResponse = LoginResponse(
            usuario = mockUser,
            token = "token",
            tipo = "Administrador",
            expiresAt = "2026-01-09T00:00:00Z"
        )
        whenever(authRepository.login(loginRequest)).thenReturn(Result.success(mockResponse))

        // When
        viewModel.login(loginRequest.email, loginRequest.contrasena)
        // Avanzamos el dispatcher para que la corutina se complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isInstanceOf(LoginUiState.Success::class.java)
    }

    @Test
    fun `login failure updates uiState to Error`() = runTest {
        // Given
        val loginRequest = LoginRequest("test@test.com", "password")
        val errorMessage = "Credenciales incorrectas"
        whenever(authRepository.login(loginRequest)).thenReturn(Result.failure(IOException(errorMessage)))

        // When
        viewModel.login(loginRequest.email, loginRequest.contrasena)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(LoginUiState.Error::class.java)
        assertThat((state as LoginUiState.Error).message).isEqualTo(errorMessage)
    }
}
