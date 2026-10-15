# Trato Hecho – Cotizaciones Móviles

Aplicación Android nativa (Kotlin + Jetpack Compose) del caso **Trato Hecho** de la consultora Prismatec Ltda.
Lleva a terreno lo que hoy ofrece la plataforma web: gestionar clientes, armar y enviar cotizaciones y hacerles
seguimiento, incluso **sin conexión estable**.

- **Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles (Duoc UC)
- **Evaluación:** Evaluación Parcial 2 – Encargo
- **Estudiante:** Joaquin Cabrera

---

## Problema que resuelve

Quien atiende al cliente toma nota en la visita y recién en la oficina arma el presupuesto, lo que demora el envío
y deja precios o tipos de cambio desactualizados. Con la app, la cotización **se arma y se envía el mismo día de la
visita**, con el valor del dólar o la UF actualizado, y cada empresa sigue el ciclo completo de sus cotizaciones.

## Funcionalidades implementadas

| Módulo | Qué hace |
|---|---|
| **Login y sesión** | Ingreso con correo y clave. La empresa (tenant) se obtiene desde la API. La sesión queda guardada en DataStore. |
| **Inicio (indicadores)** | Monto cotizado del mes, cotizaciones enviadas, tasa de aceptación, monto facturado, cotizaciones por estado y ranking de vendedores (supervisor y administrador). |
| **Clientes** | Lista con búsqueda, detalle, formulario con validación de RUT chileno, correo y teléfono, y contactos del cliente. Funciona sin conexión. Cada cliente tiene un vendedor a cargo, que el administrador o el supervisor pueden **reasignar** (las cotizaciones existentes siguen siendo de quien las hizo). |
| **Catálogo** | Productos/servicios, tipos de unidad e impuestos. El administrador los crea, edita y elimina; el resto solo los consulta. |
| **Cotizaciones** | Ítems desde el catálogo, descuento por ítem y global (% o monto), impuesto por ítem, moneda CLP/USD/UF con valor de mindicador.cl, validez en días y notas. Borradores sin conexión con número temporal `TMP-xxxx`; la API asigna el correlativo `COT-xxxx`. |
| **Envío por correo** | Genera el PDF de la cotización y abre la app de correo del teléfono con el PDF adjunto y el correo del cliente. Sin señal, la cotización queda **en cola** y llega una notificación cuando está lista para enviar. |
| **Seguimiento** | Estados Borrador, Enviada, Aceptada, Rechazada, Facturada, Vencida, Anulada y Reemplazada. Nueva versión (`COT-0012 v2`), anulación y notificación cuando el cliente responde. |
| **Ajustes (administrador)** | CRUD de usuarios (rol, activo, clave) y datos de la empresa como emisor. |

### Perfiles incluidos en esta entrega

| Perfil | Permisos |
|---|---|
| Vendedor | Gestiona sus propios clientes (los que tiene a cargo) y sus cotizaciones. |
| Supervisor | Ve y modifica las cotizaciones y clientes de los vendedores, reasigna clientes entre vendedores; ve indicadores y ranking. |
| Administrador | Parametriza catálogo, impuestos, usuarios y datos de la empresa. Consulta todas las cotizaciones y reasigna clientes entre vendedores. |

El perfil **Facturación** (órdenes de compra y facturas) queda para la Evaluación Final Transversal.

## Requisitos de la evaluación y dónde se cumplen

| Requisito | Implementación |
|---|---|
| Diseño visual con Material 3 | `ui/theme` (paleta propia, tipografía y formas), Scaffold, TopAppBar, NavigationBar, Cards, Chips, SegmentedButtons. |
| Formularios validados con íconos | Login, cliente, contacto, cotización, ítem, producto, unidad, impuesto, usuario y empresa. Cada campo muestra ícono, error animado e ícono de error/válido (`ui/components/CampoTexto.kt`). |
| Validaciones desde la lógica | Reglas en `utils/Validaciones.kt` y `utils/ValidadorRut.kt`, ejecutadas desde los ViewModel (UiState + Errores). |
| Navegación funcional | `navigation/AppNavigation.kt` (login ↔ principal) y navegación anidada en `ui/screens/principal/PrincipalScreen.kt` con barra inferior por rol. |
| Gestión de estado | `StateFlow` + `collectAsStateWithLifecycle`, `SavedStateHandle`, eventos con `SharedFlow`. |
| Persistencia local | **Room** (clientes, contactos, catálogo, cotizaciones e ítems) y **DataStore** (sesión y último valor del dólar/UF). |
| Recursos nativos | **Estado de red** (`nativo/MonitorRed.kt`, ConnectivityManager), **notificaciones** con permiso en tiempo de ejecución (`nativo/NotificacionesHelper.kt`) y **compartir/correo** con FileProvider y PDF generado con PdfDocument (`nativo/CompartirHelper.kt`, `nativo/GeneradorPdf.kt`). |
| Animaciones | `AnimatedVisibility` (errores, banners), `animateColorAsState` (estado), `animateFloatAsState` (indicadores y barras), `AnimatedContent` (total, pestañas, botón con loader), `animateContentSize`, `animateItem` en listas y transiciones entre pantallas. |
| Arquitectura MVVM modular | Ver estructura abajo. Inyección de dependencias con **Hilt**. |

## Arquitectura

Patrón **MVVM** con repositorios, en un solo módulo `app` organizado por paquetes:

```
cl.duoc.tratohecho/
├── data/
│   ├── local/          Room (entity/, dao/, AppDatabase) y DataStore (sesión, indicadores)
│   ├── remote/         Retrofit: TratoHechoApi, MindicadorApi, DTOs
│   │   └── fake/       API simulada: FakeApiInterceptor, ServidorSimulado, DatosSemilla
│   ├── repository/     Un repositorio por módulo (local primero, luego API)
│   └── sync/           GestorSincronizacion (sube pendientes al volver la señal)
├── domain/             Reglas de negocio puras: CalculadoraCotizacion, CalculadoraIndicadores
│   └── model/          Enums y modelos (Rol, EstadoCotizacion, Moneda, Sesion…)
├── di/                 Módulos de Hilt (base de datos y red)
├── nativo/             Recursos del dispositivo: red, notificaciones, PDF y compartir
├── navigation/         Rutas (AppRoutes) y navegación raíz
├── ui/
│   ├── components/     Composables reutilizables
│   ├── screens/        login, principal, inicio, cotizaciones, clientes, catalogo, ajustes
│   └── theme/          Color.kt, Theme.kt, Type.kt, Shape.kt
├── utils/              Validaciones, RUT y formatos
└── viewmodel/          Un ViewModel + UiState por pantalla
```

### API simulada

El caso indica que la app consume la API de Trato Hecho, que no está disponible. Para la evaluación se **simula
dentro de la app**: Retrofit hace las peticiones normalmente y `FakeApiInterceptor` (OkHttp) las responde desde
`ServidorSimulado`, con demora y códigos HTTP reales. El servidor simulado:

- Filtra todo por empresa (multi-tenant) y por rol.
- Asigna el correlativo `COT-xxxx` por empresa.
- Marca como **vencidas** las cotizaciones que pasaron su validez.
- Simula la respuesta del cliente en la página pública: las cotizaciones enviadas desde la app se aceptan o
  rechazan solas **2 minutos** después (también existe el botón *Simular respuesta del cliente* en el detalle).
- Si el teléfono no tiene conexión, la petición falla igual que con una API real.

El valor del **dólar y la UF** sí se obtiene de la API pública real **https://mindicador.cl**.

### Funcionamiento sin conexión

1. Clientes y cotizaciones se guardan primero en Room, marcados como *pendientes*.
2. `MonitorRed` escucha la red con `ConnectivityManager` mientras el proceso de la app está vivo (también en segundo plano).
3. Al volver la señal, `GestorSincronizacion` sube los pendientes, envía las cotizaciones en cola y baja los datos actualizados.
4. Si una cotización en cola quedó lista, o un cliente respondió, se muestra una notificación.

## Usuarios de prueba

Todos usan la clave **`Trato123`**.

| Empresa | Perfil | Correo |
|---|---|---|
| Constructora Andes SpA | Administrador | admin@andes.cl |
| Constructora Andes SpA | Supervisor | supervisor@andes.cl |
| Constructora Andes SpA | Vendedor | camila@andes.cl |
| Constructora Andes SpA | Vendedor | diego@andes.cl |
| Climatiza Sur Ltda. | Administrador | admin@climatiza.cl |
| Climatiza Sur Ltda. | Supervisor | supervisor@climatiza.cl |
| Climatiza Sur Ltda. | Vendedor | tomas@climatiza.cl |
| Climatiza Sur Ltda. | Vendedor | valentina@climatiza.cl |

Cada empresa solo ve su propia información. Los correos de clientes son de prueba (`@example.com`).

## Cómo ejecutar

1. Clonar el repositorio y abrir la carpeta `TratoHecho` en **Android Studio** (2026.1 o superior).
2. Esperar la sincronización de Gradle (JDK 17+ incluido en Android Studio).
3. Ejecutar la configuración `app` en un emulador o teléfono con **Android 8.0 (API 26)** o superior.
   Para probar el envío por correo se recomienda un emulador con Google Play (Gmail instalado).
4. Ingresar con alguno de los usuarios de prueba.

Para probar el modo sin conexión: activar el **modo avión** del emulador, crear un cliente o una cotización y
presionar *Guardar y enviar*. Al desactivar el modo avión la cotización se sincroniza y llega la notificación.

Para volver a los datos iniciales: *Configuración del teléfono → Apps → Trato Hecho → Almacenamiento → Borrar datos*.

## Tecnologías

Kotlin 2.2 · Jetpack Compose + Material 3 · Navigation Compose · ViewModel + StateFlow · Hilt · Room · DataStore ·
Retrofit + OkHttp + Gson · PdfDocument · FileProvider · NotificationCompat · ConnectivityManager.

## Alcance

No incluye la administración del SaaS (alta de empresas, cobro de suscripciones) ni la página pública donde el cliente
acepta o rechaza la cotización, según las restricciones del caso. El perfil de facturación se desarrollará en la EFT.
