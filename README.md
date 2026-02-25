# InventApp Luis370 - Client Mobile

Sistema integral de gestion de inventario y servicios tecnicos desarrollado para Android. Esta aplicacion actua como el cliente movil para la API Luis370, permitiendo un control total sobre activos, personal y procesos operativos.

## 🚀 Caracteristicas del Sistema

*   **Gestion de Activos (CRUD):** Control completo de Empresas, Usuarios, Equipos y Repuestos.
*   **Modulo de Servicios & Mano de Obra:** Seguimiento de servicios tecnicos con registro detallado de "Partes de Trabajo" (minutos trabajados, tipo de tarea y costos).
*   **Control de Inventario:** Historial de entradas y salidas vinculado a niveles criticos de stock.
*   **Seguridad RBAC:** Control de acceso basado en roles (Administrador, Gerente, Tecnico, Cliente, Empresa). La interfaz se adapta dinamicamente segun los permisos del usuario.
*   **Gestion de Garantias & RMA:** Flujo de validacion tecnica y seguimiento de retornos de mercancia.
*   **Notificaciones & Reportes:** Sistema de avisos configurables y generacion de reportes operativos.

## 🛠️ Stack Tecnologico

*   **UI:** Jetpack Compose con Material Design 3 (Componentes estables).
*   **Arquitectura:** MVVM (Model-View-ViewModel) + Clean Architecture.
*   **Inyeccion de Dependencias:** Dagger Hilt.
*   **Networking:** Retrofit 2 & OkHttp 4 con interceptores personalizados.
*   **Serializacion:** Moshi (con adaptadores de tipos personalizados).
*   **Reactividad:** Kotlin Coroutines & StateFlow.
*   **UX:** Accompanist SwipeRefresh (Pull-to-refresh nativo).

## 📡 Especificaciones de Integracion (Luis370 API)

La aplicacion implementa una capa de red robusta disenada para la maxima consistencia con el backend:

1.  **Identificadores de Negocio:** Uso exclusivo de IDs descriptivos (`id_persona`, `id_empresa`, `id_servicio`, etc.) ignorando el `_id` interno de base de datos para la logica de negocio.
2.  **Gestion de Sesion:** Manejo de `Bearer Token` con renovacion automatica basada en el encabezado `X-Token-Expires-At`.
3.  **Resiliencia de Datos:** Las operaciones de escritura (`POST`/`PUT`) se validan mediante codigos de estado HTTP, permitiendo el funcionamiento incluso con cuerpos de respuesta parciales.
4.  **ApiErrorParser:** Captura y muestra mensajes de validacion especificos del servidor (ej. "Email ya registrado").

## ⚙️ Instalacion y Configuracion

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/carloss856/ps3-app-Luis370.git
    ```

2.  **Configurar Base URL (NO tocar codigo):**

    Este proyecto usa *Build Variants* para elegir automaticamente el host del backend:

    - **Emulador (AVD)** → variant **`emulatorDebug`**
      - `BASE_URL = http://10.0.2.2:8000/api/`
      - Motivo: `10.0.2.2` es el alias del emulador para llegar al `localhost` de tu PC.

    - **Telefono fisico por USB** → variant **`deviceDebug`**
      - `BASE_URL (default) = http://10.0.2.2:8000/api/`
      - Si luego necesitas telefono fisico con reverse, cambialo en `gradle.properties`:
        - `INVENTAPP_BASE_URL_DEVICE=http://127.0.0.1:8000/api/`
      - Requiere ejecutar (una vez por conexion):
        ```bash
        adb reverse tcp:8000 tcp:8000
        ```
      - Motivo: con `adb reverse`, el `127.0.0.1:8000` del telefono se “tunea” hacia el `localhost:8000` de tu PC.

    En Android Studio: **Build > Select Build Variant…** y elige `emulatorDebug` o `deviceDebug`.
    Tambien puedes editar ambas URLs en `gradle.properties`:
    - `INVENTAPP_BASE_URL_EMULATOR=http://10.0.2.2:8000/api/`
    - `INVENTAPP_BASE_URL_DEVICE=http://10.0.2.2:8000/api/`

3.  **Diagnostico rapido (sin logcat):**

    En la pantalla de Login, en modo **DEBUG**, existe el boton **“Diagnostico API (/login)”**.
    El resultado incluye el `baseUrl=...` para confirmar que variant esta corriendo y si el error es:

    - `CONNECT (...) failed to connect` → host/puerto inaccesible (variant equivocado o backend no expuesto)
    - `TIMEOUT (...)` → backend no responde / firewall / reverse no aplicado

4.  **Sincronizar & Compilar:**

    Realiza un `Gradle Sync` y compila el proyecto (`Build > Rebuild Project`) para generar Hilt y Moshi.

## 📂 Estructura del Proyecto

*   `ui/`: Pantallas organizadas por modulos (servicios, equipos, repuestos, etc.).
*   `data/`: Modelos de datos (`Request`/`Response`), APIs y Repositorios.
*   `domain/`: Logica de permisos (`PermissionManager`) e interfaces.
*   `di/`: Modulos de inyeccion de dependencias.

---
Desarrollado por **Carlos Subero** - 2024.
