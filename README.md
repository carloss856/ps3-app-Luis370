# InventApp Luis370 - Client Mobile

Sistema integral de gestión de inventario y servicios técnicos desarrollado para Android. Esta aplicación actúa como el cliente móvil para la API Luis370, permitiendo un control total sobre activos, personal y procesos operativos.

## 🚀 Características del Sistema

*   **Gestión de Activos (CRUD):** Control completo de Empresas, Usuarios, Equipos y Repuestos.
*   **Módulo de Servicios & Mano de Obra:** Seguimiento de servicios técnicos con registro detallado de "Partes de Trabajo" (minutos trabajados, tipo de tarea y costos).
*   **Control de Inventario:** Historial de entradas y salidas vinculado a niveles críticos de stock.
*   **Seguridad RBAC:** Control de acceso basado en roles (Administrador, Gerente, Técnico, Cliente, Empresa). La interfaz se adapta dinámicamente según los permisos del usuario.
*   **Gestión de Garantías & RMA:** Flujo de validación técnica y seguimiento de retornos de mercancía.
*   **Notificaciones & Reportes:** Sistema de avisos configurables y generación de reportes operativos.

## 🛠️ Stack Tecnológico

*   **UI:** Jetpack Compose con Material Design 3 (Componentes estables).
*   **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture.
*   **Inyección de Dependencias:** Dagger Hilt.
*   **Networking:** Retrofit 2 & OkHttp 4 con interceptores personalizados.
*   **Serialización:** Moshi (con adaptadores de tipos personalizados).
*   **Reactividad:** Kotlin Coroutines & StateFlow.
*   **UX:** Accompanist SwipeRefresh (Pull-to-refresh nativo).

## 📡 Especificaciones de Integración (Luis370 API)

La aplicación implementa una capa de red robusta diseñada para la máxima consistencia con el backend:

1.  **Identificadores de Negocio:** Uso exclusivo de IDs descriptivos (`id_persona`, `id_empresa`, `id_servicio`, etc.) ignorando el `_id` interno de base de datos para la lógica de negocio.
2.  **Gestión de Sesión:** Manejo de `Bearer Token` con renovación automática basada en el encabezado `X-Token-Expires-At`.
3.  **Resiliencia de Datos:** Las operaciones de escritura (`POST`/`PUT`) se validan mediante códigos de estado HTTP, permitiendo el funcionamiento incluso con cuerpos de respuesta parciales.
4.  **ApiErrorParser:** Captura y muestra mensajes de validación específicos del servidor (ej. "Email ya registrado").

## ⚙️ Instalación y Configuración

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/carloss856/ps3-app-Luis370.git
    ```
2.  **Configurar Base URL:**
    Ajusta la URL del backend en `NetworkModule.kt` o mediante variables de entorno en `BuildConfig`.
3.  **Sincronizar & Compilar:**
    Realiza un `Gradle Sync` y compila el proyecto (`Build > Rebuild Project`) para generar los adaptadores de Hilt y Moshi.

## 📂 Estructura del Proyecto

*   `ui/`: Pantallas organizadas por módulos (servicios, equipos, repuestos, etc.).
*   `data/`: Modelos de datos (`Request`/`Response`), APIs y Repositorios.
*   `domain/`: Lógica de permisos (`PermissionManager`) e interfaces.
*   `di/`: Módulos de inyección de dependencias.

---
Desarrollado por **Carlos Subero** - 2024.
