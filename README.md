# InventApp Luis370 - Client Mobile

Sistema integral de gestiÃ³n de inventario y servicios tÃ©cnicos desarrollado para Android. Esta aplicaciÃ³n actÃºa como el cliente mÃ³vil para la API Luis370, permitiendo un control total sobre activos, personal y procesos operativos.

## ðŸš€ CaracterÃ­sticas del Sistema

*   **GestiÃ³n de Activos (CRUD):** Control completo de Empresas, Usuarios, Equipos y Repuestos.
*   **MÃ³dulo de Servicios & Mano de Obra:** Seguimiento de servicios tÃ©cnicos con registro detallado de "Partes de Trabajo" (minutos trabajados, tipo de tarea y costos).
*   **Control de Inventario:** Historial de entradas y salidas vinculado a niveles crÃ­ticos de stock.
*   **Seguridad RBAC:** Control de acceso basado en roles (Administrador, Gerente, TÃ©cnico, Cliente, Empresa). La interfaz se adapta dinÃ¡micamente segÃºn los permisos del usuario.
*   **GestiÃ³n de GarantÃ­as & RMA:** Flujo de validaciÃ³n tÃ©cnica y seguimiento de retornos de mercancÃ­a.
*   **Notificaciones & Reportes:** Sistema de avisos configurables y generaciÃ³n de reportes operativos.

## ðŸ› ï¸ Stack TecnolÃ³gico

*   **UI:** Jetpack Compose con Material Design 3 (Componentes estables).
*   **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture.
*   **InyecciÃ³n de Dependencias:** Dagger Hilt.
*   **Networking:** Retrofit 2 & OkHttp 4 con interceptores personalizados.
*   **SerializaciÃ³n:** Moshi (con adaptadores de tipos personalizados).
*   **Reactividad:** Kotlin Coroutines & StateFlow.
*   **UX:** Accompanist SwipeRefresh (Pull-to-refresh nativo).

## ðŸ“¡ Especificaciones de IntegraciÃ³n (Luis370 API)

La aplicaciÃ³n implementa una capa de red robusta diseÃ±ada para la mÃ¡xima consistencia con el backend:

1.  **Identificadores de Negocio:** Uso exclusivo de IDs descriptivos (`id_persona`, `id_empresa`, `id_servicio`, etc.) ignorando el `_id` interno de base de datos para la lÃ³gica de negocio.
2.  **GestiÃ³n de SesiÃ³n:** Manejo de `Bearer Token` con renovaciÃ³n automÃ¡tica basada en el encabezado `X-Token-Expires-At`.
3.  **Resiliencia de Datos:** Las operaciones de escritura (`POST`/`PUT`) se validan mediante cÃ³digos de estado HTTP, permitiendo el funcionamiento incluso con cuerpos de respuesta parciales.
4.  **ApiErrorParser:** Captura y muestra mensajes de validaciÃ³n especÃ­ficos del servidor (ej. "Email ya registrado").

## âš™ï¸ InstalaciÃ³n y ConfiguraciÃ³n

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/carloss856/ps3-app-Luis370.git
    ```

2.  **Configurar Base URL (NO tocar cÃ³digo):**

    Este proyecto usa *Build Variants* para elegir automÃ¡ticamente el host del backend:

    - **Emulador (AVD)** â†’ variant **`emulatorDebug`**
      - `BASE_URL = http://10.0.2.2:8000/api/`
      - Motivo: `10.0.2.2` es el alias del emulador para llegar al `localhost` de tu PC.

    - **TelÃ©fono fÃ­sico por USB** â†’ variant **`deviceDebug`**
      - `BASE_URL (default) = http://10.0.2.2:8000/api/`
      - Si luego necesitas telefono fisico con reverse, cambialo en `gradle.properties`:
        - `INVENTAPP_BASE_URL_DEVICE=http://127.0.0.1:8000/api/`
      - Requiere ejecutar (una vez por conexiÃ³n):
        ```bash
        adb reverse tcp:8000 tcp:8000
        ```
      - Motivo: con `adb reverse`, el `127.0.0.1:8000` del telÃ©fono se â€œtuneaâ€ hacia el `localhost:8000` de tu PC.

    En Android Studio: **Build > Select Build Variantâ€¦** y elige `emulatorDebug` o `deviceDebug`.
    Tambien puedes editar ambas URLs en `gradle.properties`:
    - `INVENTAPP_BASE_URL_EMULATOR=http://10.0.2.2:8000/api/`
    - `INVENTAPP_BASE_URL_DEVICE=http://10.0.2.2:8000/api/`

3.  **DiagnÃ³stico rÃ¡pido (sin logcat):**

    En la pantalla de Login, en modo **DEBUG**, existe el botÃ³n **â€œDiagnÃ³stico API (/login)â€**.
    El resultado incluye el `baseUrl=...` para confirmar quÃ© variant estÃ¡ corriendo y si el error es:

    - `CONNECT (...) failed to connect` â†’ host/puerto inaccesible (variant equivocado o backend no expuesto)
    - `TIMEOUT (...)` â†’ backend no responde / firewall / reverse no aplicado

4.  **Sincronizar & Compilar:**

    Realiza un `Gradle Sync` y compila el proyecto (`Build > Rebuild Project`) para generar Hilt y Moshi.

## ðŸ“‚ Estructura del Proyecto

*   `ui/`: Pantallas organizadas por mÃ³dulos (servicios, equipos, repuestos, etc.).
*   `data/`: Modelos de datos (`Request`/`Response`), APIs y Repositorios.
*   `domain/`: LÃ³gica de permisos (`PermissionManager`) e interfaces.
*   `di/`: MÃ³dulos de inyecciÃ³n de dependencias.

---
Desarrollado por **Carlos Subero** - 2024.
