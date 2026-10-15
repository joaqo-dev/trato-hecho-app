# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Equipos comerciales de las empresas clientes (tenants) de la plataforma SaaS Trato Hecho de Prismatec Ltda. El usuario principal es el **vendedor en terreno**: visita al cliente, muchas veces sin conexión estable y con el teléfono en una mano, y necesita armar y enviar la cotización durante la misma visita. Le siguen el **supervisor** (sigue y corrige las cotizaciones del equipo, reasigna clientes) y el **administrador** de cada empresa (parametriza catálogo, impuestos, usuarios y datos del emisor). El perfil de facturación queda para una etapa posterior.

## Product Purpose

Llevar al teléfono lo que la plataforma web ya cubre: gestionar clientes, armar y enviar cotizaciones por correo con el PDF adjunto y hacerles seguimiento hasta la aceptación y la facturación. Éxito: la cotización se arma y se envía el mismo día de la visita, con precios y valor del dólar/UF actualizados, y cada empresa ve el ciclo completo de sus cotizaciones e indicadores de su negocio.

## Positioning

Cotizar en terreno sin depender de la señal: borradores y clientes se guardan en el teléfono, el envío queda en cola y sale solo al recuperar la conexión, con aviso al vendedor.

## Operating Context

- Uso en terreno, a pleno sol o en interiores de clientes; conectividad intermitente.
- El cliente final acepta o rechaza en una página pública que ya existe (fuera de la app); la app solo consulta el resultado.
- Envío por la app de correo del teléfono con el PDF adjunto.
- Valores de USD y UF desde findic.cl (con último valor guardado si no hay señal).
- Multi-tenant: cada empresa ve solo su información.

## Capabilities and Constraints

- Android nativo, Kotlin + Jetpack Compose + Material 3, MVVM, Room, DataStore, Hilt. Un solo módulo `app` con paquetes por funcionalidad.
- La API de Trato Hecho está simulada dentro de la app (interceptor de OkHttp) con fines de evaluación.
- Ciclo de la cotización: Borrador → Enviada → Aceptada / Rechazada / Vencida → Facturada; además Anulada y Reemplazada (por versiones).
- Contexto académico: Evaluación Parcial 2 de DSY1105 (Duoc UC). El código debe verse escrito por un estudiante, usando lo visto en la asignatura, sin complejidad innecesaria.

## Brand Commitments

- Nombre: **Trato Hecho**. Idioma: español de Chile.
- Dirección visual pinned por el usuario (14-10-2026): inspirada en una app de envíos/tracking de referencia — monocromo (negro, blancos, grises) con el ámbar de la marca como único acento, tarjeta principal negra, botones tipo píldora, barra inferior en píldora flotante, tracker con checkpoints y timeline de eventos. Tema claro y oscuro.

## Evidence on Hand

- Caso `Caso_DSY1105_Trato Hecho.pdf` y pauta `evaluacion.pdf` en la carpeta superior.
- Datos de demostración: dos empresas de prueba (Constructora Andes SpA y Climatiza Sur Ltda.) generadas en `DatosSemilla.kt`; todo dato de clientes es sintético (`@example.com`).

## Product Principles

1. Lo que el vendedor necesita en la visita, a un toque: nueva cotización, cliente, envío.
2. La falta de señal nunca bloquea el trabajo; el estado de sincronización siempre se ve.
3. El estado de cada cotización se entiende de un vistazo, sin abrirla.
4. Validar con mensajes que dicen el problema y cómo arreglarlo, campo por campo.

## Accessibility & Inclusion

Toques de al menos 48 dp, tipografía en sp que respeta la escala del sistema, contraste legible a pleno sol, animaciones que respetan la configuración del sistema.
