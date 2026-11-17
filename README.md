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
| **Inicio** | Arriba, una franja con el mes (ganado, enviadas, aceptación; montos ocultables). Luego, centrado en las **cotizaciones**: una sola acción principal según el rol (*Nueva cotización* para el vendedor, *Nuevo cliente* para el administrador), buscador y la sección **Por atender** con pestañas y su conteo: *Por vencer* (3 días, en ámbar), *Esperando respuesta*, *Borradores* y *En cola* (solo si hay). Al final, el ranking de vendedores (supervisor y administrador). Cerrar sesión está en el menú del avatar. Se actualiza deslizando hacia abajo. |
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
| Animaciones | Línea del **tracker** que se llena al aparecer, efecto de presión (escala 0.97) en tarjetas y botones, pestañas de la barra inferior y del inicio con cambio de color, `AnimatedVisibility` (errores, banners), `animateColorAsState` (estado), `animateFloatAsState` (indicadores y barras), `AnimatedContent` (total, pestañas, botón con loader), `animateContentSize`, `animateItem` en listas y transiciones entre pantallas. |
| Arquitectura MVVM modular | Ver estructura abajo. Inyección de dependencias con **Hilt**. |

## Diseño de la interfaz

La interfaz toma como referencia una app de seguimiento de envíos: cada cotización se sigue como un envío.

- **Monocromo de alto contraste** (pensado para usarse a pleno sol) con el ámbar de la marca como único acento; colores de estado suaves para las cotizaciones.
- El inicio responde una pregunta: **qué cotizaciones necesitan acción hoy**. Botones tipo píldora, tarjetas grises de 24 dp (cliente y total primero, número y estado debajo) y **barra inferior flotante** con ícono y nombre siempre visibles.
- Revisada con las heurísticas de Nielsen: estado visible (conteos, filtro *Todas*), prevención de errores (confirmar antes de descartar cambios o anular, acciones destructivas en rojo), reconocer antes que recordar (etiquetas en la barra) y diseño minimalista (sin etiquetas redundantes).
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
│   ├── repository/     Un repositorio por módulo (local primero, luego API)
│   └── sync/           GestorSincronizacion (al volver la señal y cada 15 s)
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

### Servidor (API) y panel web

La API de Trato Hecho la responde un servidor propio, en la carpeta [`backend/`](backend/) (Kotlin + Ktor),
desplegado con Docker en un servidor casero y publicado en **https://tratohecho-api.joaqo.dev** mediante
Cloudflare Tunnel. La app lo consume con Retrofit igual que a cualquier API real.

```
App Android ──┐                                     Servidor (Docker Compose)
              ├──► https://tratohecho-api.joaqo.dev ──► Caddy ──► tratohecho-api (Ktor, :8080)
Navegador ────┘     (Cloudflare Tunnel, HTTPS)                     ├─ /v1/...  API REST de la app
                                                                   ├─ /panel   panel web
                                                                   ├─ /salud   estado del servidor
                                                                   └─► tratohecho-db (PostgreSQL 17)
```

En desarrollo el mismo servidor corre en el computador con SQLite y se publica con ngrok (`backend/iniciar.sh`).

El servidor:

- Filtra todo por empresa (multi-tenant) y por rol.
- Asigna el correlativo `COT-xxxx` por empresa.
- Marca como **vencidas** las cotizaciones que pasaron su validez.
- Si el cliente aceptó una cotización mientras el vendedor la anulaba sin señal, gana la aceptación.

El **panel web** (`/panel`) simula lo que ocurre fuera de la app. Se ingresa con `panel@tratohecho.cl` y la clave
definida en `PANEL_CLAVE` (en desarrollo, `Trato123`); se elige la empresa y desde ahí:

- **Acepta o rechaza** las cotizaciones enviadas (como el cliente en la página pública).
- **Factura** las cotizaciones aceptadas (como la empresa).
- Ve todas las cotizaciones y clientes, y la **última conexión** de cada usuario de la app.

La app consulta el servidor cada **15 segundos**, así la respuesta del cliente o la factura le llegan sola con una
notificación.

El valor del **dólar y la UF** sí se obtiene de la API pública real **https://findic.cl** (sin clave). Cada valor obtenido se guarda: sin conexión se puede seguir cotizando en USD o UF con el último valor descargado, y la app lo advierte.

### Funcionamiento sin conexión

1. Clientes y cotizaciones se guardan primero en Room, marcados como *pendientes*.
2. `MonitorRed` escucha la red con `ConnectivityManager` mientras el proceso de la app está vivo (también en segundo plano).
3. Al volver la señal (y cada 15 segundos), `GestorSincronizacion` sube los pendientes, envía las cotizaciones en cola y baja los datos actualizados.
4. Si una cotización en cola quedó lista, un cliente respondió o se facturó, se muestra una notificación.

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

### Con el servidor publicado (lo normal)

1. Abrir el proyecto en **Android Studio** y agregar en `local.properties` (raíz del proyecto; no se sube a GitHub):
   ```
   tratohecho.api.url=https://tratohecho-api.joaqo.dev/v1/
   ```
2. Ejecutar la configuración `app` en un emulador o teléfono con **Android 8.0 (API 26)** o superior.
3. Ingresar con alguno de los usuarios de prueba. El panel está en https://tratohecho-api.joaqo.dev/panel.

### Con un servidor local (desarrollo)

1. Crear una cuenta gratis en [ngrok.com](https://ngrok.com), instalarlo (`brew install ngrok`), guardar el token
   (`ngrok config add-authtoken <TU_TOKEN>`) y copiar el dominio fijo gratuito (*dashboard.ngrok.com → Domains*).
2. En `local.properties`: `tratohecho.api.url=https://<tu-dominio>.ngrok-free.dev/v1/`
3. Levantar el servidor (SQLite en `backend/tratohecho.db`) y ngrok; se detiene con `Ctrl+C`:
   ```
   ./backend/iniciar.sh <tu-dominio>.ngrok-free.dev
   ```

> Sin `tratohecho.api.url` la app apunta a `http://10.0.2.2:8080/v1/` (el computador visto desde el emulador).
> En Android 17 el sistema bloquea por defecto la conexión de las apps a la red local, por eso se usa una URL pública.

### Probar la sincronización híbrida

1. Activar el **modo avión** del emulador y, como vendedor, presionar *Guardar y enviar* en una cotización:
   queda **En cola** con un número temporal (`TMP-1234`).
2. Desactivar el modo avión: la cotización llega al panel con su número `COT-xxxx` y aparece la notificación
   "lista para enviar".
3. En el panel, **Aceptar** la cotización: antes de 15 segundos llega al teléfono "¡Trato hecho! … fue aceptada".
4. En el panel, **Facturar**: llega "… fue facturada" y el tracker de la cotización queda completo.

Para volver a los datos iniciales: botón **Reiniciar datos** del panel. Las sesiones abiertas se cierran y la app
vuelve al login.

## Despliegue en el servidor

El servidor vive en `/srv/apps/trato-hecho` (clon de este repositorio) y se levanta con [`compose.yml`](compose.yml):

| Contenedor | Qué es | Redes |
|---|---|---|
| `tratohecho-api` | API Ktor + panel (imagen de [`backend/Dockerfile`](backend/Dockerfile), máx. 768 MB) | `web` (Caddy) e interna |
| `tratohecho-db` | PostgreSQL 17, datos en el volumen `db_data` | solo interna |

Los secretos van en `/srv/apps/trato-hecho/.env` (`chmod 600`), según [`.env.example`](.env.example):
`POSTGRES_PASSWORD` (generada con `openssl rand -hex 24`), `PANEL_CORREO` y `PANEL_CLAVE`.
Caddy lo expone en `http://192.168.18.196:8082` (red local) y en `tratohecho-api.joaqo.dev` (Cloudflare Tunnel),
según `/srv/proxy/sites/tratohecho.caddy`.

```bash
ssh joaqo@192.168.18.196
cd /srv/apps/trato-hecho

docker compose ps                          # estado (ambos deben decir "healthy")
docker compose logs -f --tail=100 api      # registro en vivo (cambios y errores)
git pull && docker compose up -d --build   # actualizar después de un push a main

# Respaldo de la base de datos
docker compose exec -T db pg_dump -U tratohecho --clean --if-exists tratohecho > ~/respaldo-tratohecho-$(date +%F).sql
# Restaurar un respaldo (reemplaza los datos actuales). La API guarda los datos en memoria,
# por eso se detiene antes y se vuelve a levantar después.
docker compose stop api
docker compose exec -T db psql -U tratohecho -d tratohecho < ~/respaldo-tratohecho-AAAA-MM-DD.sql
docker compose start api
```

El registro solo muestra los cambios y los errores; con `REGISTRAR_TODO=1` en `.env` muestra también cada consulta.

## Tecnologías

Kotlin 2.2 · Jetpack Compose + Material 3 · Navigation Compose · ViewModel + StateFlow · Hilt · Room · DataStore ·
Retrofit + OkHttp + Gson · PdfDocument · FileProvider · NotificationCompat · ConnectivityManager.
Servidor: Ktor · PostgreSQL (SQLite en desarrollo) · Docker Compose · Caddy · Cloudflare Tunnel · ngrok (desarrollo).

## Alcance

No incluye la administración del SaaS (alta de empresas, cobro de suscripciones) ni la página pública donde el cliente
acepta o rechaza la cotización, según las restricciones del caso (el panel web solo la simula para probar la app). El perfil de facturación se desarrollará en la EFT.
