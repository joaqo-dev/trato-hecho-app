# Trato Hecho – Cotizaciones Móviles

Aplicación Android nativa (Kotlin + Jetpack Compose) del caso **Trato Hecho** de la consultora Prismatec Ltda.
Lleva a terreno lo que hoy ofrece la plataforma web: gestionar clientes, armar y enviar cotizaciones y hacerles
seguimiento, incluso **sin conexión estable**.

- **Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles (Duoc UC)
- **Evaluación:** Evaluación Parcial 2 – Encargo
- **Estudiante:** Joaquin Cabrera

La documentación completa del proyecto (perfiles, arquitectura, archivos, reglas de negocio y cumplimiento de la pauta) está en [DOCUMENTACION.md](DOCUMENTACION.md).

---

## Problema que resuelve

Quien atiende al cliente toma nota en la visita y recién en la oficina arma el presupuesto, lo que demora el envío
y deja precios o tipos de cambio desactualizados. Con la app, la cotización **se arma y se envía el mismo día de la
visita**, con el valor del dólar o la UF actualizado, y cada empresa sigue el ciclo completo de sus cotizaciones.

## Funcionalidades implementadas

| Módulo | Qué hace |
|---|---|
| **Login y sesión** | Ingreso con correo y clave. La empresa (tenant) se obtiene desde la API. La sesión queda guardada en DataStore. |
| **Inicio** | Tarjeta principal con lo **ganado este mes** (se puede ocultar) y lo que requiere atención: cotizaciones por responder (y su monto), por vencer en 3 días y borradores sin enviar; accesos a *Cotizar* y *Nuevo cliente*. Debajo: buscador, la cotización en curso con su tracker, las 2 actividades más recientes, un resumen (aceptación, enviadas del mes, facturado) y el ranking de vendedores (supervisor y administrador). Se actualiza deslizando hacia abajo. |
| **Clientes** | Los clientes son de la empresa y el **administrador** los registra, edita, asigna a un vendedor y da de baja (funciona sin conexión). Cada vendedor ve solo los clientes que tiene asignados y puede actualizar su correo, teléfono y personas de contacto. Validación de RUT chileno, correo y teléfono. **Nunca se borran datos**: un cliente dado de baja deja de poder cotizarse pero conserva su historial. |
| **Catálogo** | Productos/servicios, tipos de unidad e impuestos. El administrador los crea, edita y elimina; el resto solo los consulta. |
| **Cotizaciones** | Ítems desde el catálogo, descuento por ítem y global (% o monto), impuesto por ítem (lo trae cada producto del catálogo y no se modifica al cotizar), moneda CLP/USD/UF con valor de findic.cl, validez en días y notas. Borradores sin conexión con número temporal `TMP-xxxx`; la API asigna el correlativo `COT-xxxx`. |
| **Envío por correo** | Genera el PDF de la cotización y abre la app de correo del teléfono con el PDF adjunto y el correo del cliente. Sin señal, la cotización queda **en cola** y llega una notificación cuando está lista para enviar. |
| **Seguimiento** | Cada cotización muestra un **tracker** de su ciclo (Creada → Enviada → Respuesta → Facturada) y, en el detalle, un **historial** con fecha y hora de cada evento. Estados Borrador, Enviada, Aceptada, Rechazada, Facturada, Vencida, Anulada y Reemplazada. Nueva versión (`COT-0012 v2`), anulación y notificación cuando el cliente responde. |
| **Contacto rápido** | Desde el detalle de la cotización o del cliente se llama (abre el marcador) o se escribe un correo al contacto con un toque. |
| **Ajustes (administrador)** | Usuarios (crear, editar rol y clave, dar de baja) y datos de la empresa como emisor. Los usuarios no se borran: se dan de baja. Si se da de baja a un vendedor con clientes, la app advierte y obliga a **reasignarlos** a otro vendedor antes de guardar. |

### Perfiles incluidos en esta entrega

| Perfil | Permisos |
|---|---|
| Vendedor | Cotiza a los clientes que tiene asignados (no ve los de otros vendedores) y actualiza su información de contacto. |
| Supervisor | Hace seguimiento y puede modificar las cotizaciones de los vendedores; consulta todos los clientes; ve indicadores y ranking. |
| Administrador | Parametriza catálogo, impuestos, usuarios y datos de la empresa; gestiona los clientes (crear, editar, asignar vendedor, dar de baja). Consulta todas las cotizaciones. |

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
| Recursos nativos | **Teléfono y correo** (`nativo/ContactoHelper.kt`, marcador con `ACTION_DIAL` sin pedir permiso de llamadas), **estado de red** (`nativo/MonitorRed.kt`, ConnectivityManager), **notificaciones** con permiso en tiempo de ejecución (`nativo/NotificacionesHelper.kt`) y **compartir/correo** con FileProvider y PDF generado con PdfDocument (`nativo/CompartirHelper.kt`, `nativo/GeneradorPdf.kt`). |
| Animaciones | Línea del **tracker** que se llena al aparecer, efecto de presión (escala 0.97) en tarjetas y botones, píldora de la barra inferior que se expande con resorte, `AnimatedVisibility` (errores, banners), `animateColorAsState` (estado), `animateFloatAsState` (indicadores y barras), `AnimatedContent` (total, pestañas, botón con loader), `animateContentSize`, `animateItem` en listas y transiciones entre pantallas. |
| Arquitectura MVVM modular | Ver estructura abajo. Inyección de dependencias con **Hilt**. |

## Diseño de la interfaz

La interfaz toma como referencia una app de seguimiento de envíos: cada cotización se sigue como un envío.

- **Monocromo de alto contraste** (pensado para usarse a pleno sol) con el ámbar de la marca como único acento; colores de estado suaves para las cotizaciones.
- **Tarjeta principal negra**, botones tipo píldora, tarjetas grises de 24 dp y **barra inferior flotante** con la sección activa en una píldora negra.
- **Tracker del ciclo** en cada tarjeta y **línea de tiempo** en el detalle.
- **Tema claro y oscuro.**
- Animaciones cortas (120–280 ms) con curvas *ease-out* marcadas; nada se anima desde cero.

Los colores, tipografía y formas están en `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`, `Shape.kt`) y las curvas y duraciones de animación en `Movimiento.kt`.

## Arquitectura

Patrón **MVVM** con repositorios, en un solo módulo `app` organizado por paquetes:

```
cl.duoc.tratohecho/
├── data/
│   ├── local/          Room (entity/, dao/, AppDatabase) y DataStore (sesión, indicadores)
│   ├── remote/         Retrofit: TratoHechoApi, IndicadoresApi, DTOs
│   │   └── fake/       API simulada: FakeApiInterceptor, ServidorSimulado, DatosSemilla
│   ├── repository/     Un repositorio por módulo (local primero, luego API)
│   └── sync/           GestorSincronizacion (sube pendientes al volver la señal)
├── domain/             Reglas de negocio puras: CalculadoraCotizacion, CalculadoraIndicadores
│   └── model/          Enums y modelos (Rol, EstadoCotizacion, Moneda, Sesion…)
├── di/                 Módulos de Hilt (base de datos y red)
├── nativo/             Recursos del dispositivo: red, notificaciones, PDF, compartir, teléfono y correo
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

El valor del **dólar y la UF** sí se obtiene de la API pública real **https://findic.cl** (sin clave). Cada valor obtenido se guarda: sin conexión se puede seguir cotizando en USD o UF con el último valor descargado, y la app lo advierte.

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
