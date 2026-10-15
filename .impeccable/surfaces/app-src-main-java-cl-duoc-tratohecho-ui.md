---
version: 1
slug: "app-src-main-java-cl-duoc-tratohecho-ui"
primary_target: "app/src/main/java/cl/duoc/tratohecho/ui"
related_targets: []
---

# Surface: app Trato Hecho (todas las pantallas)

Scope: shell completo de la app Android (login, inicio, cotizaciones, clientes, catálogo, ajustes). Mode: Operate.
Audiencia/tarea: vendedor en terreno que arma y envía cotizaciones en la visita; supervisor y administrador que siguen y parametrizan.
Restricciones: Material 3 nativo, controles estándar, código de nivel estudiante (Compose básico), tema claro y oscuro.
Dirección: pinned por el usuario con imagen de referencia (app de tracking monocroma). No se corrió concept-seed por ser dirección fijada por el usuario.

## Direction contract

THESIS: Cada cotización es un envío que se sigue: su ciclo de vida se lee como un tracker de checkpoints, no como una etiqueta suelta. Rechaza la app de formularios gris con tarjetas iguales y una barra Material sin carácter.

OWN-WORLD: Monocromo de alto contraste: fondo blanco (oscuro: #0B0B0C), tarjetas gris claro #F4F4F5 con radio 24dp, una tarjeta "hero" negra por pantalla, botones píldora negros, chips de estado suaves (azul enviada, verde aceptada, rojo rechazada, violeta facturada, ámbar vencida/en cola). Ámbar #E8963A solo como acento. Tipografía del sistema con escalones de peso claros.

STORY: El vendedor abre la app, ve cuánto lleva cotizado y su cotización en curso, toca "Nueva cotización", la arma y la envía; luego sigue su avance en el tracker y contacta al cliente desde el detalle.

FIRST VIEWPORT: Inicio: saludo + empresa arriba; tarjeta negra con "Cotizado este mes" grande, ojo para ocultar montos y dos píldoras blancas (Nueva cotización / Nuevo cliente); buscador píldora; "Cotización en curso" con tracker; "Actividad reciente". Barra inferior flotante en píldora con la pestaña activa negra (ícono + texto).

FORM: Referencia del usuario (app de tracking de envíos), posición 1 de 1 (pinned). Seed key: pinned-by-user. Signature move: tracker del ciclo Creada → Enviada → Respuesta (Aceptada/Rechazada/Vencida) → Facturada, completo en Inicio y detalle y compacto en cada tarjeta de lista con checkpoints y línea que se llena animada; timeline de eventos en el detalle.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
