# Trato Hecho – Documentación del proyecto

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles (Duoc UC)
**Evaluación:** Evaluación Parcial 2 – Encargo
**Estudiante:** Joaquin Cabrera
**Plataforma:** Android nativo · Kotlin · Jetpack Compose · Material 3

Este documento explica el proyecto completo: el caso, los perfiles, las funcionalidades, la arquitectura, qué hace cada archivo y cómo se cumple la pauta de evaluación. Para ejecutar la app basta con el [README](README.md).

---

## Índice

1. [El caso](#1-el-caso)
2. [Perfiles de usuario](#2-perfiles-de-usuario)
3. [Funcionalidades](#3-funcionalidades)
4. [Arquitectura](#4-arquitectura)
5. [Estructura de carpetas](#5-estructura-de-carpetas)
6. [Pantallas y archivos detrás de cada una](#6-pantallas-y-archivos-detrás-de-cada-una)
7. [Servidor, panel web y API real](#7-servidor-panel-web-y-api-real)
8. [Funcionamiento sin conexión](#8-funcionamiento-sin-conexión)
9. [Reglas de negocio](#9-reglas-de-negocio)
10. [Validaciones de formularios](#10-validaciones-de-formularios)
11. [Recursos nativos y permisos](#11-recursos-nativos-y-permisos)
12. [Animaciones](#12-animaciones)
13. [Diseño visual](#13-diseño-visual)
14. [Cumplimiento de la evaluación](#14-cumplimiento-de-la-evaluación)
15. [Cómo ejecutar](#15-cómo-ejecutar)
16. [Preguntas frecuentes](#16-preguntas-frecuentes)

---

## 1. El caso

**Prismatec Ltda.** ofrece **Trato Hecho**, una plataforma web SaaS *multi-tenant* de cotizaciones: muchas empresas usan el mismo sistema y cada una (tenant) ve solo su propia información.

**Problema:** el vendedor visita al cliente, toma nota y recién de vuelta en la oficina arma el presupuesto y lo envía días después, a veces con precios o tipos de cambio desactualizados. Se pierden oportunidades y credibilidad.

**Solución pedida:** una app móvil nativa que, a través de la misma API de la web, permita en terreno:

- gestionar clientes y sus contactos;
- armar cotizaciones con ítems, descuentos, impuestos y moneda (CLP, USD o UF);
- enviarlas por correo con el documento adjunto;
- hacerles seguimiento hasta la aceptación y la facturación;
- trabajar **sin conexión estable** (borradores y clientes offline, sincronización automática al recuperar señal);
- ver indicadores del negocio.

**Fuera del alcance (punto 5.1 del caso):** la administración del SaaS (alta de empresas, cobro) y la página pública donde el cliente final acepta o rechaza la cotización (ya existe; la app solo consulta el resultado).

---

## 2. Perfiles de usuario

### Lo que dice el caso (punto 3.1)

> El **administrador** de cada empresa cliente parametriza catálogos, impuestos y usuarios, y ve todas las cotizaciones de su empresa. El **supervisor** revisa y hace seguimiento de las cotizaciones del equipo. El **usuario** gestiona sus propios clientes y cotizaciones. El perfil de **facturación** registra y consulta órdenes de compra y facturas sobre las cotizaciones aceptadas.

### Cómo quedó en la app

| Perfil | Puede hacer | Menú inferior |
|---|---|---|
| **Vendedor** | Ve solo los clientes que el administrador le asignó (no los de otros vendedores). Crea, envía, versiona y anula sus cotizaciones. De sus clientes solo actualiza correo, teléfono y personas de contacto. | Inicio, Cotizaciones, Clientes, Catálogo (solo productos) |
| **Supervisor** | Hace seguimiento y puede modificar las cotizaciones de todos los vendedores. Consulta todos los clientes (sin editarlos). Ve indicadores y ranking. | Inicio, Cotizaciones, Clientes, Catálogo (solo productos) |
| **Administrador** | Mantiene catálogo (productos, unidades, impuestos), usuarios y datos de la empresa. Gestiona los clientes: crea, edita, asigna vendedor, reasigna y da de baja. Consulta todas las cotizaciones. | Inicio, Cotizaciones, Clientes, Catálogo, Ajustes |
| Facturación | Queda para la Evaluación Final Transversal (EFT). | — |

### Decisiones tomadas donde el caso no dice nada

- Los **clientes son de la empresa** y el administrador los asigna a cada vendedor.
- El supervisor **no aprueba** cotizaciones, solo hace seguimiento (y puede modificarlas).
- El catálogo incluye **productos con precio**, además de unidades e impuestos.
- **Nunca se borran datos:** clientes y usuarios solo se dan de baja (baja lógica).
- Si se da de baja a un vendedor con clientes, la app **obliga a reasignarlos** a otro vendedor antes de guardar.
- Al reasignar un cliente, sus cotizaciones anteriores siguen siendo del vendedor que las hizo.

**Regla común (punto 5.1):** cada empresa accede solo a su propia información. El servidor identifica la empresa por el token del usuario y filtra todo por ella.

---

## 3. Funcionalidades

| Módulo | Qué hace |
|---|---|
| **Login y sesión** | Ingreso con correo y clave validados. La sesión queda guardada (DataStore) para no pedir login cada vez. |
| **Inicio** | Tarjeta principal con lo **ganado este mes** (ocultable) y lo que requiere atención: cotizaciones **por responder** (y su monto), **por vencer** en 3 días y **borradores** sin enviar. Accesos rápidos según el rol, buscador, cotización en curso con su tracker, las 2 actividades más recientes, resumen (aceptación, enviadas del mes, facturado) y ranking de vendedores. Se actualiza deslizando hacia abajo. |
| **Cotizaciones** | Lista con buscador y filtros por estado. Formulario con cliente y contacto, moneda CLP/USD/UF (valor real del día), ítems desde el catálogo (el impuesto viene fijo del producto), descuentos opcionales por ítem y global (% o monto), validez y notas, con el total siempre visible. Detalle con tracker, historial, envío por correo, ver PDF, nueva versión, anulación y contacto rápido. |
| **Clientes** | Lista con buscador. Formulario con RUT que se formatea mientras se escribe (`76.123.456-0`) y valida el dígito verificador. Contactos del cliente. Permisos según el rol (ver sección 2). Baja lógica y reactivación. |
| **Catálogo** | Productos, unidades e impuestos. El administrador los mantiene; vendedor y supervisor ven solo los productos. |
| **Ajustes** | Usuarios (crear, editar rol y clave, dar de baja con traspaso de clientes) y datos de la empresa como emisor (aparecen en el PDF). |
| **Sin conexión** | Todo se lee del teléfono; lo creado offline queda pendiente y se sincroniza solo al volver la señal (y cada 15 segundos). Los envíos quedan en cola y avisan con una notificación. |

---

## 4. Arquitectura

Patrón **MVVM** (Model – View – ViewModel) en un solo módulo `app`, organizado en paquetes según su responsabilidad.

| Capa | Responsabilidad | Carpeta |
|---|---|---|
| **Vista** | Dibuja la pantalla y avisa lo que hace el usuario. No valida ni calcula. | `ui/screens`, `ui/components`, `ui/theme` |
| **ViewModel** | Guarda el estado de la pantalla (`UiState` en un `StateFlow`), valida y llama a los repositorios. | `viewmodel` |
| **Dominio** | Reglas de negocio puras: totales, indicadores, tracker, historial. | `domain` |
| **Datos** | Base local (Room y DataStore), API (Retrofit) y repositorios que deciden de dónde salen los datos. | `data` |
| **Recursos nativos** | Red, notificaciones, PDF, correo, teléfono. | `nativo` |
| **Inyección de dependencias** | Hilt crea y entrega los objetos (base de datos, API, repositorios). | `di` |

### Flujo de datos

```
Pantalla (Compose)
   │  el usuario escribe o toca un botón          ▲ la pantalla se redibuja sola
   ▼                                              │ (observa el StateFlow)
ViewModel ── valida con utils/Validaciones ───────┘
   │
   ▼
Repositorio ──► Room (base local en el teléfono)   ← siempre primero
   │
   └────────► API (Retrofit) cuando hay señal     ← sincronización
```

### Patrón de los formularios (UiState + errores)

```kotlin
data class UnidadFormUiState(
    val nombre: String = "",
    val errorNombre: String? = null,   // null = sin error
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false
)

// ViewModel
private val _estado = MutableStateFlow(UnidadFormUiState())
val estado: StateFlow<UnidadFormUiState> = _estado.asStateFlow()
fun onNombreChange(v: String) = _estado.update {
    it.copy(nombre = v, errorNombre = if (it.intentoGuardar) validarNombre(v) else null)
}

// Pantalla
val estado by viewModel.estado.collectAsStateWithLifecycle()
CampoTexto(valor = estado.nombre, onValorChange = viewModel::onNombreChange, error = estado.errorNombre)
```

Los errores aparecen al presionar "Guardar" y desde ese momento se actualizan en tiempo real con cada tecla.

### Librerías

| Librería | Uso |
|---|---|
| Jetpack Compose + Material 3 | Interfaz |
| Navigation Compose | Navegación entre pantallas |
| ViewModel + StateFlow | Estado de cada pantalla |
| Hilt | Inyección de dependencias |
| Room | Base de datos local (SQLite) |
| DataStore | Sesión y último valor del dólar/UF |
| Retrofit + OkHttp + Gson | Llamadas a la API |

---

## 5. Estructura de carpetas

Paquete base `cl.duoc.tratohecho` (`app/src/main/java/cl/duoc/tratohecho/`).

```
├── MainActivity.kt           Punto de entrada de la interfaz
├── TratoHechoApp.kt          Arranca Hilt, notificaciones, monitor de red y sincronización
├── data/
│   ├── local/                Room (AppDatabase, entity/, dao/) y DataStore (sesión, indicadores)
│   ├── remote/               Retrofit: TratoHechoApi, IndicadoresApi, AuthInterceptor, DTOs
│   ├── repository/           Un repositorio por módulo
│   └── sync/                 GestorSincronizacion
├── domain/                   CalculadoraCotizacion, CalculadoraIndicadores, ProgresoCotizacion, HistorialCotizacion
│   └── model/                Rol, EstadoCotizacion, Moneda, TipoDescuento, Sesion, Indicadores…
├── di/                       DatabaseModule, NetworkModule (Hilt)
├── nativo/                   MonitorRed, NotificacionesHelper, GeneradorPdf, CompartirHelper, ContactoHelper
├── navigation/               AppRoutes, AppNavigation
├── ui/
│   ├── theme/                Color, Theme, Type, Shape, Movimiento
│   ├── components/           Componentes reutilizables
│   └── screens/              login, principal, inicio, cotizaciones, clientes, catalogo, ajustes
├── utils/                    Validaciones, ValidadorRut, RutVisualTransformation, Formatos
└── viewmodel/                Un ViewModel por pantalla
```

### Archivos de configuración

| Archivo | Qué hace |
|---|---|
| `settings.gradle.kts` | Nombre del proyecto y repositorios de librerías |
| `build.gradle.kts` (raíz) | Plugins: Android, Compose, KSP, Hilt |
| `app/build.gradle.kts` | Paquete, `minSdk 26`, `targetSdk 37`, dependencias |
| `gradle/libs.versions.toml` | Versiones de todas las librerías en un solo lugar |
| `app/src/main/AndroidManifest.xml` | Permisos, actividad principal, FileProvider y `<queries>` |
| `res/xml/file_paths.xml` | Carpeta que el FileProvider comparte (los PDF) |

### Base de datos local (Room, 9 tablas)

| Entidad | Contenido |
|---|---|
| `EmpresaEntity` | Datos del emisor |
| `UsuarioEntity` | Nombre, correo, rol, activo |
| `ClienteEntity` | RUT, razón social, giro, dirección, correo, teléfono, vendedor a cargo, activo, pendiente de sincronizar |
| `ContactoEntity` | Contactos del cliente |
| `UnidadEntity`, `ImpuestoEntity`, `ProductoEntity` | Catálogo |
| `CotizacionEntity` | Número, versión, cliente, vendedor, moneda, tipo de cambio, validez, descuento global, notas, estado, total, fechas (creación, envío, vencimiento, respuesta y facturación), pendiente y en cola |
| `ItemCotizacionEntity` | Líneas de la cotización |

Los DAO (`ClienteDao`, `CotizacionDao`, `CatalogoDao`, `UsuarioDao`, `EmpresaDao`) devuelven `Flow`, así las pantallas se actualizan solas cuando cambian los datos.

---

## 6. Pantallas y archivos detrás de cada una

Cada pantalla sigue la misma cadena: **Pantalla** (dibuja) → **ViewModel** (estado y decisiones) → **Repositorio** → **Room / API**.

| Pantalla (`ui/screens/…`) | ViewModel | Repositorios y otros archivos |
|---|---|---|
| `login/LoginScreen` | `LoginViewModel` | `AuthRepository`, `SesionDataStore`, `Validaciones`, `GestorSincronizacion` |
| `principal/PrincipalScreen` (contenedor, barra por rol, avisos de conexión) | `PrincipalViewModel` | `AuthRepository`, `MonitorRed`, `GestorSincronizacion`, `Seccion.kt` |
| `inicio/InicioScreen` | `InicioViewModel` | `CotizacionRepository`, `CalculadoraIndicadores` |
| `cotizaciones/CotizacionesScreen` | `CotizacionesViewModel` | `CotizacionRepository` |
| `cotizaciones/CotizacionFormScreen` | `CotizacionFormViewModel` | `CotizacionRepository`, `ClienteRepository`, `CatalogoRepository`, `IndicadoresEconomicosRepository`, `CalculadoraCotizacion` |
| `cotizaciones/CotizacionDetalleScreen` | `CotizacionDetalleViewModel` | `CotizacionRepository`, `ClienteRepository`, `EmpresaRepository`, `GeneradorPdf`, `CompartirHelper`, `ContactoHelper`, `ProgresoCotizacion`, `HistorialCotizacion` |
| `clientes/ClientesScreen` | `ClientesViewModel` | `ClienteRepository` |
| `clientes/ClienteDetalleScreen` | `ClienteDetalleViewModel` | `ClienteRepository`, `CotizacionRepository`, `ContactoHelper` |
| `clientes/ClienteFormScreen` | `ClienteFormViewModel` | `ClienteRepository`, `UsuarioRepository`, `Validaciones`, `ValidadorRut`, `RutVisualTransformation` |
| `catalogo/CatalogoScreen` | `CatalogoViewModel` | `CatalogoRepository` |
| `catalogo/ProductoFormScreen`, `UnidadFormScreen`, `ImpuestoFormScreen` | `ProductoFormViewModel`, `UnidadFormViewModel`, `ImpuestoFormViewModel` | `CatalogoRepository` |
| `ajustes/AjustesScreen` | — (usa la sesión) | — |
| `ajustes/UsuariosScreen` | `UsuariosViewModel` | `UsuarioRepository` |
| `ajustes/UsuarioFormScreen` | `UsuarioFormViewModel` | `UsuarioRepository`, `ClienteRepository` (traspaso de clientes) |
| `ajustes/EmpresaFormScreen` | `EmpresaFormViewModel` | `EmpresaRepository` |

### Componentes reutilizables (`ui/components/`)

| Componente | Uso |
|---|---|
| `CampoTexto` | Campo con ícono, error animado y marca de válido |
| `CampoSelector` | Selector en hoja inferior con buscador |
| `SelectorOpciones` | Botones segmentados (moneda, tipo de descuento, rol) |
| `SeccionDescuento` | Descuento opcional y plegable |
| `BotonCarga`, `BotonSecundario`, `BotonFlotante` | Botones tipo píldora |
| `BarraNavegacionFlotante` | Barra inferior flotante |
| `TrackerCotizacion` | Avance del ciclo de la cotización |
| `LineaTiempo` | Historial de eventos |
| `TarjetaCotizacion`, `TarjetaContacto`, `TarjetaTotales`, `FilaItem` | Tarjetas de lista, contacto, totales e ítems |
| `ChipEstado` | Etiqueta de color por estado |
| `MenuAcciones` | Menú ⋮ de cada fila o pantalla |
| `ContenidoActualizable`, `CargandoDatos` | Deslizar para actualizar y rueda de carga |
| `BannerSinConexion`, `MensajeError`, `EstadoVacio`, `DialogoConfirmacion` | Avisos, errores, listas vacías y confirmaciones |

---

## 7. Servidor, panel web y API real

### Servidor de Trato Hecho (`backend/`)

La API de Trato Hecho no existe para el curso, así que se construyó un **servidor propio** en la carpeta `backend/` (Kotlin + **Ktor** + **SQLite**) y se publica en internet con **ngrok**. La app lo consume con **Retrofit** como a cualquier API real: hay demoras reales, errores reales sin señal y códigos HTTP reales (200, 401, 403, 404, 409).

```
App Android ──┐
              ├──► https://<dominio>.ngrok-free.dev ──► servidor en el computador (puerto 8080)
Navegador ────┘                                        ├─ /v1/...  API REST de la app
                                                       ├─ /panel   panel web
                                                       └─ backend/tratohecho.db (SQLite)

Repositorio → Retrofit (TratoHechoApi) → AuthInterceptor (agrega el token) → internet (ngrok) → ServidorTratoHecho
```

| Archivo (`backend/src/main/kotlin/…/servidor/`) | Qué hace |
|---|---|
| `Main.kt` | Arranca Ktor, recibe las rutas `/v1/...` y muestra cada petición en la terminal |
| `ServidorTratoHecho.kt` | Reglas del servidor (permisos, multi-tenant, correlativo, vencimiento) y acciones del panel |
| `datos/AlmacenSqlite.kt` | Guarda los datos en SQLite: una tabla por entidad con el registro en JSON |
| `datos/DatosSemilla.kt` | Datos de prueba (2 empresas) que se cargan la primera vez |
| `modelo/Modelos.kt` | Los mismos modelos de la app, sin anotaciones de Room |
| `panel/PanelRutas.kt` y `resources/panel/index.html` | Panel web |
| `backend/iniciar.sh` | Levanta el servidor y ngrok con un solo comando |

La URL del servidor se configura en `local.properties` (`tratohecho.api.url`) y llega a la app por `BuildConfig.URL_API`. A cada petición se le agrega el encabezado `ngrok-skip-browser-warning` para saltar la página de aviso de ngrok.

| Endpoint | Qué hace | Quién puede |
|---|---|---|
| `POST auth/login` | Valida correo y clave | Todos |
| `GET/PUT empresa` | Datos del emisor | Leer: todos · Editar: admin |
| `GET/PUT usuarios` | Usuarios (no se borran: se dan de baja) | Admin |
| `POST usuarios/{id}/traspasar-clientes` | Pasa los clientes de un vendedor a otro | Admin |
| `GET vendedores` | Vendedores activos | Admin y supervisor |
| `GET/PUT/DELETE unidades, impuestos, productos` | Catálogo | Leer: todos · Editar: admin |
| `GET/PUT clientes` | Clientes con contactos | Ver: todos (vendedor solo los suyos) · Editar: admin; vendedor solo contacto |
| `POST clientes/{id}/reasignar` | Cambia el vendedor a cargo | Admin |
| `GET/PUT cotizaciones` | Cotizaciones con ítems | Vendedor (las suyas) y supervisor |

Reglas del servidor:

- filtra todo por empresa (multi-tenant) y por rol;
- asigna el correlativo `COT-0012` por empresa (offline la cotización usa `TMP-1234`);
- una cotización enviada no cambia: solo se anula o se reemplaza por una nueva versión (`COT-0012 v2`);
- marca como **Vencida** la que pasa su validez sin respuesta;
- si el cliente aceptó una cotización mientras el vendedor la anulaba sin señal, gana la aceptación;
- nunca borra clientes ni usuarios, y no deja dar de baja a un vendedor con clientes sin reasignarlos.

### Panel web (`/panel`)

Simula lo que pasa **fuera de la app**, que el caso deja fuera del alcance: la página pública donde el cliente responde y el registro de la factura. Tiene su propio usuario (`panel@tratohecho.cl` / `Trato123`) y ve todas las empresas.

- **Esperando respuesta del cliente:** botones *Aceptar* y *Rechazar* para cada cotización enviada.
- **Aceptadas, por facturar:** botón *Facturar* (la cotización pasa a **Facturada** con su fecha).
- **Usuarios y última conexión:** punto verde si la app de ese usuario habló con el servidor en los últimos 30 segundos; sirve para ver cuándo un teléfono quedó sin señal.
- Tablas de todas las cotizaciones (con buscador y filtro por estado) y de los clientes.
- **Reiniciar datos:** vuelve a los datos de prueba.
- Se actualiza solo cada 3 segundos.

| Ruta del panel | Qué hace |
|---|---|
| `POST panel/api/login` | Ingreso al panel |
| `GET panel/api/empresas` y `GET panel/api/empresas/{id}` | Resumen y datos de cada empresa |
| `POST panel/api/cotizaciones/{id}/aceptar` · `/rechazar` | El cliente responde |
| `POST panel/api/cotizaciones/{id}/facturar` | La empresa factura |
| `POST panel/api/reiniciar` | Vuelve a los datos de prueba |

### API real: dólar y UF

El valor del día se obtiene de la API pública **https://findic.cl/api/** (sin clave). Cada sincronización guarda el valor; sin señal se puede seguir cotizando en USD o UF con el último valor guardado y la app lo advierte.

---

## 8. Funcionamiento sin conexión

- **Local primero:** todo lo que se ve sale de Room, por eso la app abre al instante aunque no haya internet.
- Lo creado o editado offline queda marcado como **pendiente** (ícono de nube).
- `MonitorRed` (ConnectivityManager) avisa cuando vuelve la señal y `GestorSincronizacion` sincroniza solo, incluso con la app en segundo plano.
- Además, `GestorSincronizacion` consulta el servidor **cada 15 segundos** (sin mostrar el aviso de "sincronizando"), así las respuestas de los clientes y las facturas registradas en el panel llegan solas.

| Funciona sin conexión | Requiere conexión |
|---|---|
| Ver lo descargado | Iniciar sesión |
| Crear y editar clientes (admin) y su contacto (vendedor) | Mantener catálogo, usuarios y empresa |
| Armar y editar borradores | Reasignar clientes y dar de baja vendedores |
| "Enviar" una cotización (queda en cola) | Recibir respuestas de clientes y facturas |
| Cotizar en USD/UF con el último valor guardado | |
| Ver el PDF de una cotización emitida | |

**Pasos de la sincronización:** sube clientes pendientes → sube cotizaciones pendientes (las que estaban en cola se envían y reciben su número) → baja empresa, catálogo, clientes, cotizaciones y usuarios → detecta respuestas y facturas nuevas y avisa con notificación → guarda el dólar y la UF del día (este último paso no se hace en la consulta de cada 15 segundos).

**Cola de envío:** sin señal la cotización queda "En cola" con número temporal. Al volver la señal se envía sola, recibe su número `COT-xxxx` y llega una notificación "lista para enviar" que abre el correo con el PDF.

---

## 9. Reglas de negocio

### Estados de la cotización

| Estado | Significado |
|---|---|
| **Borrador** | Se está armando; se puede editar |
| **En cola** (marca) | Se envió sin señal; sale sola al volver la conexión |
| **Enviada** | Espera respuesta del cliente |
| **Aceptada** / **Rechazada** | El cliente respondió en la página pública (en el proyecto, desde el panel web) |
| **Vencida** | Pasó la validez sin respuesta |
| **Facturada** | Se facturó desde el panel web (el caso la nombra en 2.4, 3.1 y 4.1; el perfil de facturación es de la EFT) |
| **Anulada** | Se canceló |
| **Reemplazada** | Existe una versión más nueva |

### Cálculo de totales

1. Subtotal del ítem = cantidad × precio − descuento del ítem.
2. Subtotal = suma de los ítems.
3. Neto = subtotal − descuento global.
4. Impuestos = impuesto de cada ítem sobre su parte del neto.
5. Total = neto + impuestos (CLP sin decimales; USD y UF con 2).

**Ejemplo:** 80 m² a $6.500 con 10% de descuento en el ítem y 5% global → ítem $468.000 → descuento global −$23.400 → neto $444.600 → IVA 19% $84.474 → **total $529.074**.

En USD o UF, el precio del catálogo (en pesos) se divide por el valor del dólar o la UF y se muestra también el equivalente en pesos.

### Otras reglas

- El impuesto de cada ítem lo define el administrador en el producto; no se cambia al cotizar.
- Solo se puede cotizar a clientes activos.
- Las métricas del inicio cuentan en pesos y no repiten versiones reemplazadas.

---

## 10. Validaciones de formularios

Todas están en `utils/Validaciones.kt` y se ejecutan desde los ViewModel; la pantalla solo muestra el error.

| Regla | Dónde | Mensaje de ejemplo |
|---|---|---|
| Obligatorio y largo | Nombres, razón social, giro, dirección | "La razón social debe tener al menos 3 caracteres" |
| Correo válido | Login, cliente, contacto, usuario, empresa | "Ingresa un correo válido (ej: nombre@empresa.cl)" |
| RUT chileno (módulo 11) | Cliente, empresa | "RUT inválido, revisa el dígito verificador" |
| RUT no repetido | Cliente | "Ya existe un cliente con este RUT" |
| Teléfono +56 y 9 dígitos | Cliente, contacto, empresa | "Usa el formato +56 9 1234 5678" |
| Clave segura y confirmación | Usuario | "Las claves no coinciden" |
| Número positivo | Precio, cantidad | "El precio debe ser mayor a 0" |
| Rango | Validez 1–90, impuesto 0–100 | "La validez debe estar entre 1 y 90" |
| Descuento máximo | Ítem y global | "El descuento no puede superar el 100%" |
| Selección obligatoria | Cliente, producto, unidad, impuesto, vendedor | "Selecciona un cliente" |
| Al menos un ítem | Cotización | "Agrega al menos un ítem" |

**Dígito verificador del RUT:** cada dígito del cuerpo, de derecha a izquierda, se multiplica por 2, 3, 4, 5, 6, 7, 2, 3…; se suman; el verificador es 11 − (suma mod 11), donde 11 = "0" y 10 = "K".

---

## 11. Recursos nativos y permisos

### Permisos (`AndroidManifest.xml`)

| Permiso | Para qué | Tipo |
|---|---|---|
| `INTERNET` | Servidor de Trato Hecho y findic.cl | Normal (se concede al instalar) |
| `ACCESS_NETWORK_STATE` | Saber si hay señal | Normal |
| `POST_NOTIFICATIONS` | Notificaciones | Peligroso: en Android 13+ se pide en tiempo de ejecución |

El permiso de notificaciones se solicita en `ui/screens/principal/PrincipalScreen.kt` con `rememberLauncherForActivityResult(RequestPermission())`. Si el usuario lo rechaza, la app sigue funcionando y lo explica.

En el manifiesto también están `network_security_config.xml` (solo permite HTTP sin cifrar hacia el servidor local; con ngrok todo va por HTTPS), el **FileProvider** (comparte el PDF sin exponer la ruta real del archivo) y `<queries>` (permite encontrar las apps de correo y el marcador).

### Recursos usados (la pauta pide al menos 2)

| Recurso | API de Android | Archivo | Uso |
|---|---|---|---|
| Estado de la red | `ConnectivityManager` | `nativo/MonitorRed.kt` | Aviso sin conexión y sincronización automática |
| Notificaciones | `NotificationCompat`, `PendingIntent` | `nativo/NotificacionesHelper.kt` | "Lista para enviar", "aceptada/rechazada" y "facturada" |
| Compartir por correo | `Intent.ACTION_SEND` + FileProvider | `nativo/CompartirHelper.kt` | Envío de la cotización con PDF |
| Visor de archivos | `Intent.ACTION_VIEW` | `nativo/CompartirHelper.kt` | "Ver PDF" |
| Teléfono y correo | `ACTION_DIAL`, `mailto` | `nativo/ContactoHelper.kt` | Contacto rápido (abre el marcador, no llama directo) |

El PDF se genera con `PdfDocument` (API nativa) en `nativo/GeneradorPdf.kt`.

---

## 12. Animaciones

| Animación | Dónde | Cómo |
|---|---|---|
| Línea del tracker que se llena | Inicio, listas, detalle | `animateFloatAsState` |
| Tarjetas y botones se achican al presionar | Toda la app | `animateFloatAsState` + `graphicsLayer` (`Presion.kt`) |
| Píldora de la barra con resorte | Barra inferior | `AnimatedVisibility` + `spring` |
| Errores que aparecen y desaparecen | Formularios | `AnimatedVisibility` |
| Color del estado | Chips | `animateColorAsState` |
| Total que se desliza al cambiar | Formulario y detalle | `AnimatedContent` |
| Montos que cuentan hasta su valor | Tarjeta del inicio | `animateFloatAsState` |
| Botón que se vuelve rueda de carga | Ingresar, guardar, enviar | `AnimatedContent` |
| Avisos de conexión | Parte superior | `AnimatedVisibility` |
| Descuento que se despliega | Formulario | `AnimatedContent` |
| Elementos de lista que se acomodan | Listas | `Modifier.animateItem()` |
| Transiciones entre pantallas | Navegación | `enterTransition` / `exitTransition` |

Curvas y duraciones centralizadas en `ui/theme/Movimiento.kt` (120–280 ms; el tracker 700 ms).

---

## 13. Diseño visual

- Paleta **monocroma de alto contraste** (pensada para leerse a pleno sol) con el **ámbar** de la marca como único acento (en cola, por vencer).
- Colores suaves por estado: azul enviada, verde aceptada, rojo rechazada, violeta facturada, ámbar vencida.
- Tarjeta principal negra, botones tipo píldora, tarjetas grises de 24 dp y barra inferior flotante.
- Tracker del ciclo en cada tarjeta y línea de tiempo en el detalle.
- Tema claro y oscuro según la configuración del teléfono.
- Selectores en hoja inferior; acciones de cada fila en un menú ⋮.
- Toques de al menos 48 dp; textos en `sp`.

Definido en `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`, `Shape.kt`, `Movimiento.kt`).

---

## 14. Cumplimiento de la evaluación

### Requisitos del encargo

| Requisito | Cómo se cumple |
|---|---|
| Diseño visual con Material 3 | Tema propio sobre Material 3, componentes M3 en todas las pantallas, tema claro y oscuro |
| Formularios validados con íconos y mensajes | 10 formularios: login, cliente, contacto, cotización, ítem, producto, unidad, impuesto, usuario y empresa |
| Validaciones manejadas desde la lógica | `utils/Validaciones.kt` ejecutadas en los ViewModel |
| Navegación funcional | Navigation Compose: navegación raíz (login ↔ principal) y anidada, rutas en `AppRoutes`, barra por rol |
| Gestión de estado | ViewModel + `StateFlow` + `collectAsStateWithLifecycle`, `SharedFlow` para eventos, Hilt |
| Almacenamiento local | Room (9 tablas) y DataStore; funciona sin conexión |
| Recursos nativos (mínimo 2) | 5 recursos (sección 11) |
| Animaciones | Sección 12 |
| Estructura modular y MVVM | Paquetes por responsabilidad (sección 5) |
| Repositorio en GitHub | Repositorio público con commits técnicos y README |

### Rúbrica

| Indicador | Peso | Evidencia |
|---|---|---|
| IE 2.1.1 Interfaz y navegación | 15% | Secciones 6 y 13 |
| IE 2.1.2 Formularios con validación visual | 15% | Sección 10 |
| IE 2.2.1 Validación centralizada y desacoplada | 10% | `Validaciones.kt` + ViewModels |
| IE 2.2.2 Animaciones | 10% | Sección 12 |
| IE 2.3.1 Modularidad y persistencia local | 15% | Secciones 4, 5 y 8 |
| IE 2.3.2 GitHub y planificación | 20% | Repositorio y commits; planificación aparte |
| IE 2.4.1 Recursos nativos | 15% | Sección 11 |

---

## 15. Cómo ejecutar

**Una sola vez:** crear una cuenta en ngrok, instalarlo (`brew install ngrok`), guardar el token (`ngrok config add-authtoken <TOKEN>`) y agregar en `local.properties` la URL del dominio fijo gratuito:

```
tratohecho.api.url=https://<tu-dominio>.ngrok-free.dev/v1/
```

**Cada vez:**

1. Levantar el servidor y ngrok: `./backend/iniciar.sh <tu-dominio>.ngrok-free.dev` (dejar la terminal abierta).
2. Abrir el panel en `https://<tu-dominio>.ngrok-free.dev/panel`.
3. Abrir el proyecto en **Android Studio**, iniciar un emulador con Android 8.0 (API 26) o superior (recomendado: con Google Play, para tener Gmail) y presionar **▶ Run**.
4. Ingresar con un usuario de prueba (todos con clave `Trato123`):

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

Para volver a los datos iniciales: botón **Reiniciar datos** del panel y, en el teléfono, Ajustes → Apps → Trato Hecho → Almacenamiento → Borrar datos.

**Probar la sincronización híbrida:** activar el modo avión y enviar una cotización como vendedor (queda *En cola* con número `TMP-…`). Al desactivar el modo avión llega al panel con su número `COT-…` y aparece la notificación "lista para enviar". Luego, en el panel, *Aceptar* y *Facturar*: antes de 15 segundos llega cada notificación al teléfono.

---

## 16. Preguntas frecuentes

**¿Por qué MVVM?**
Separa la interfaz de la lógica: la pantalla solo dibuja, el ViewModel valida y decide, y los repositorios manejan los datos. El código queda ordenado, se puede probar por partes y el estado sobrevive a la rotación del teléfono.

**¿Los ViewModels son las pantallas?**
No. La pantalla (`XScreen`) dibuja; el ViewModel (`XViewModel`) guarda el estado y toma las decisiones. La pantalla observa el estado y se redibuja sola cuando cambia.

**¿Dónde se validan los formularios?**
En el ViewModel, usando `utils/Validaciones.kt`. La pantalla solo muestra el error que viene en el estado.

**¿Qué guarda Room y qué guarda DataStore?**
Room guarda datos con estructura y relaciones (clientes, contactos, catálogo, cotizaciones, ítems). DataStore guarda datos simples: la sesión y el último valor del dólar y la UF.

**¿Cómo se reemplazó la API de Trato Hecho?**
Con un servidor propio en `backend/` (Ktor + SQLite) que aplica las mismas reglas que tendría la plataforma, publicado en internet con ngrok. La app no sabe que es un servidor de prueba: si existiera la API real, solo se cambiaría la URL en `local.properties`.

**¿Para qué sirve el panel web?**
Para simular lo que el caso deja fuera de la app: el cliente que acepta o rechaza en la página pública y la empresa que factura. Así se puede probar el ciclo completo de la cotización y las notificaciones.

**¿Cómo se entera la app de una respuesta o una factura?**
`GestorSincronizacion` consulta el servidor cada 15 segundos y al volver la señal. Compara el estado de cada cotización antes y después, y si cambió a Aceptada, Rechazada o Facturada muestra una notificación.

**¿Por qué ngrok y no la IP del computador?**
ngrok da una URL pública con HTTPS, así la app funciona igual en el emulador y en un teléfono físico con datos móviles. Además, Android 17 bloquea por defecto la conexión de las apps a la red local.

**¿Qué pasa si no hay internet?**
La app sigue funcionando con los datos de Room. Lo nuevo queda pendiente y se sincroniza solo al volver la señal; los envíos quedan en cola y avisan con una notificación.

**¿Cómo se asegura que una empresa no vea datos de otra?**
El servidor identifica al usuario por su token y filtra todo por su empresa. Además, al iniciar sesión se borran los datos locales de cualquier empresa anterior.

**¿Qué pasa si un vendedor deja la empresa?**
El administrador lo da de baja (nunca se borra). Si tiene clientes, la app muestra una advertencia y obliga a reasignarlos a otro vendedor antes de guardar. Sus cotizaciones quedan en el historial.

**¿Por qué el vendedor no puede crear clientes?**
Los clientes son de la empresa y el administrador los asigna. El vendedor solo actualiza la información de contacto de los clientes que tiene asignados.

**¿Por qué el impuesto no se puede cambiar en el ítem?**
Lo define el administrador en cada producto del catálogo; así el vendedor no puede vender con IVA algo exento ni al revés.

**¿Dónde se piden los permisos?**
Se declaran en el `AndroidManifest.xml`. Las notificaciones se piden en tiempo de ejecución en `PrincipalScreen`. Correo, visor y marcador se usan con Intents, que no requieren permiso.

**¿Qué es Hilt?**
La librería de inyección de dependencias: crea los objetos (base de datos, API, repositorios) y los entrega a los ViewModel por el constructor, sin crearlos a mano.
