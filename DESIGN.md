---
name: Trato Hecho
description: Cotizaciones en terreno que se siguen como un envío; monocromo de alto contraste con ámbar como único acento.
colors:
  negro: "#0A0A0A"
  blanco: "#FFFFFF"
  gris-50: "#F7F7F8"
  gris-100: "#F4F4F5"
  gris-150: "#EDEDF0"
  gris-200: "#E6E6EA"
  gris-300: "#D4D4D8"
  gris-500: "#6B6B72"
  gris-700: "#3F3F46"
  en-hero-suave: "#B4B4BB"
  carbon: "#0B0B0C"
  carbon-1: "#141416"
  carbon-2: "#1C1C1F"
  carbon-3: "#242428"
  carbon-4: "#2C2C31"
  humo: "#F5F5F6"
  humo-suave: "#A1A1AA"
  ambar: "#E8963A"
  ambar-claro: "#FFF1DE"
  ambar-oscuro: "#5C3300"
  ambar-noche: "#FFB870"
  error: "#D92D20"
  error-noche: "#FF8A80"
  estado-borrador-fondo: "#EFEFF1"
  estado-borrador-texto: "#52525B"
  estado-enviada-fondo: "#E8F0FE"
  estado-enviada-texto: "#2563EB"
  estado-aceptada-fondo: "#E3F6EA"
  estado-aceptada-texto: "#15803D"
  estado-rechazada-fondo: "#FDECEC"
  estado-rechazada-texto: "#C62828"
  estado-facturada-fondo: "#F1EBFD"
  estado-facturada-texto: "#6D28D9"
  estado-vencida-fondo: "#FFF1DE"
  estado-vencida-texto: "#B45309"
  estado-anulada-fondo: "#E4E4E7"
  estado-anulada-texto: "#3F3F46"
  estado-reemplazada-fondo: "#F4F4F5"
  estado-reemplazada-texto: "#71717A"
  estado-enviada-fondo-noche: "#172554"
  estado-enviada-texto-noche: "#93C5FD"
  estado-aceptada-fondo-noche: "#052E16"
  estado-aceptada-texto-noche: "#86EFAC"
  estado-rechazada-fondo-noche: "#3B0A0A"
  estado-rechazada-texto-noche: "#FCA5A5"
  estado-facturada-fondo-noche: "#2E1065"
  estado-facturada-texto-noche: "#C4B5FD"
  estado-vencida-fondo-noche: "#3A2205"
typography:
  display:
    fontFamily: "Roboto (fuente del sistema, FontFamily.Default), sans-serif"
    fontSize: "34sp"
    fontWeight: 700
    lineHeight: "40sp"
    letterSpacing: "-0.8sp"
    fontFeature: "tnum"
  headline:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "24sp"
    fontWeight: 700
    lineHeight: "30sp"
    letterSpacing: "-0.4sp"
  title-large:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "20sp"
    fontWeight: 600
    lineHeight: "26sp"
    letterSpacing: "-0.2sp"
  title-medium:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "22sp"
    fontFeature: "tnum"
  title-small:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "14sp"
    fontWeight: 600
    lineHeight: "20sp"
  body-large:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
  body-medium:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
  body-small:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "12sp"
    fontWeight: 400
    lineHeight: "16sp"
  label-large:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "15sp"
    fontWeight: 600
    lineHeight: "20sp"
  label-medium:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "16sp"
  label-small:
    fontFamily: "Roboto (fuente del sistema), sans-serif"
    fontSize: "11sp"
    fontWeight: 500
    lineHeight: "14sp"
rounded:
  xs: "8dp"
  sm: "12dp"
  md: "16dp"
  lg: "24dp"
  xl: "28dp"
  pildora: "50%"
spacing:
  xs: "4dp"
  sm: "8dp"
  gap: "10dp"
  md: "12dp"
  lg: "16dp"
  tarjeta: "18dp"
  margen: "20dp"
  xl: "24dp"
  reserva-barra: "96dp"
components:
  boton-principal:
    backgroundColor: "{colors.negro}"
    textColor: "{colors.blanco}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pildora}"
    height: "56dp"
    width: "100%"
  boton-secundario:
    backgroundColor: "transparent"
    textColor: "{colors.negro}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pildora}"
    height: "52dp"
    width: "100%"
  boton-secundario-destructivo:
    textColor: "{colors.error}"
  boton-flotante:
    backgroundColor: "{colors.negro}"
    textColor: "{colors.blanco}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pildora}"
  tarjeta-hero:
    backgroundColor: "{colors.negro}"
    textColor: "{colors.blanco}"
    rounded: "{rounded.xl}"
    padding: "20dp"
  boton-hero:
    backgroundColor: "{colors.gris-100}"
    textColor: "{colors.negro}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pildora}"
    padding: "0 12dp"
  tarjeta:
    backgroundColor: "{colors.gris-100}"
    textColor: "{colors.negro}"
    rounded: "{rounded.lg}"
    padding: "18dp"
  bloque-detalle:
    backgroundColor: "{colors.gris-100}"
    rounded: "{rounded.lg}"
    padding: "8dp 18dp"
  chip-estado-enviada:
    backgroundColor: "{colors.estado-enviada-fondo}"
    textColor: "{colors.estado-enviada-texto}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.pildora}"
    padding: "7dp 14dp"
  chip-estado-aceptada:
    backgroundColor: "{colors.estado-aceptada-fondo}"
    textColor: "{colors.estado-aceptada-texto}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.pildora}"
    padding: "7dp 14dp"
  chip-estado-en-cola:
    backgroundColor: "{colors.ambar-claro}"
    textColor: "{colors.estado-vencida-texto}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.pildora}"
    padding: "7dp 14dp"
  campo-texto:
    backgroundColor: "transparent"
    rounded: "{rounded.md}"
    width: "100%"
  campo-busqueda:
    backgroundColor: "{colors.blanco}"
    textColor: "{colors.gris-500}"
    typography: "{typography.body-large}"
    rounded: "{rounded.pildora}"
    height: "56dp"
    padding: "0 16dp"
  barra-navegacion:
    backgroundColor: "{colors.blanco}"
    rounded: "{rounded.pildora}"
    padding: "6dp"
  barra-navegacion-item-activo:
    backgroundColor: "{colors.negro}"
    textColor: "{colors.blanco}"
    typography: "{typography.label-large}"
    rounded: "{rounded.pildora}"
    height: "52dp"
    padding: "0 15dp"
  barra-navegacion-item:
    backgroundColor: "{colors.blanco}"
    textColor: "{colors.gris-500}"
    rounded: "{rounded.pildora}"
    height: "52dp"
    width: "52dp"
---

# Design System: Trato Hecho

## Overview

**Creative North Star: "La guía de despacho"**

Cada cotización es un envío que se sigue. El sistema toma el lenguaje de las apps de seguimiento de paquetes: un número de guía grande, un estado en píldora, un tracker de checkpoints que se llena y una línea de tiempo de eventos. Todo lo demás se retira a un monocromo de alto contraste (negro, blanco y una escalera corta de grises fríos) pensado para leerse a pleno sol, con el teléfono en una mano.

La densidad es media y generosa en toques: tarjetas grises redondeadas de 24dp sobre fondo blanco, una sola tarjeta "hero" negra por pantalla que concentra la cifra y las acciones más usadas, y una barra de navegación en píldora flotante. El color de estado vive solo dentro de chips suaves y nodos del tracker; el ámbar de la marca aparece únicamente para decir "pendiente, en cola o sin señal". El tema oscuro no invierte la jerarquía: la superficie baja a carbón (#0B0B0C), la tarjeta hero sube a gris carbón y la acción principal pasa a píldora clara.

Se rechaza la app de formularios gris con tarjetas iguales y una barra Material sin carácter.

**Key Characteristics:**
- Monocromo de alto contraste; ámbar como único acento de marca.
- Una tarjeta hero de máximo contraste por pantalla.
- Píldoras para toda acción, búsqueda, estado y navegación.
- Tracker de checkpoints como pieza firma, completo o compacto.
- Profundidad por tono, no por sombra; la única sombra es la de la barra flotante.
- Movimiento corto y firme: presión a 0.97, curvas de salida fuertes.

## Colors

Monocromo frío con una escalera tonal Material bien definida, un acento ámbar escaso y una familia de pares suaves (fondo + texto) para los estados de la cotización.

### Primary
- **Negro Tinta** (negro): `primary` en tema claro. Fondo de la tarjeta hero, botón principal, botón flotante, pestaña activa de la barra, avatar de iniciales y línea llena del tracker. En oscuro el rol `primary` lo toma **Humo** (humo) y la acción principal se vuelve píldora clara con texto negro.

### Secondary
- **Ámbar Trato** (ambar; noche: ambar-noche): `secondary`. Íconos de "pendiente de sincronizar", valor de cambio servido desde caché, borde del nodo "en curso" del tracker, valor "Pendiente" en filas de detalle y el ícono de primer lugar del ranking.
- **Ámbar Crema** (ambar-claro) con **Ámbar Tostado** (ambar-oscuro): `secondaryContainer` / `onSecondaryContainer`; fondo del estado "En cola".

### Tertiary
- **Verde Aceptada** (estado-aceptada-texto): `tertiary`; además del estado Aceptada, marca el ícono de campo válido.

### Estados de cotización
Cada estado es un par fondo suave + texto saturado, con su versión nocturna (fondo muy oscuro + texto pastel). Borrador gris, Enviada azul, Aceptada verde, Rechazada rojo, Facturada violeta, Vencida ámbar, Anulada gris oscuro, Reemplazada gris claro. "En cola" (envío esperando señal) reutiliza Ámbar Crema con el texto de Vencida. El color de texto del par es también el color del nodo fallido en el tracker y del punto de alerta/error en la línea de tiempo.

### Neutral
- **Blanco** (blanco): fondo y `surface` en claro; fondo de la barra flotante y de los buscadores.
- **Gris Niebla** (gris-100): `surfaceContainer`; fondo de todas las tarjetas, bloques de detalle y botones circulares. En la tarjeta hero clara es también el fondo de sus botones píldora.
- **Gris Pista** (gris-200): `surfaceContainerHighest`; pista vacía del tracker y nodos pendientes.
- **Gris Borde** (gris-300): `outline`; borde de campos, botón secundario, buscadores, divisores y línea punteada de la línea de tiempo.
- **Gris Texto** (gris-500): `onSurfaceVariant`; etiquetas de dato ("Cliente", "Total"), fechas, íconos inactivos.
- **Gris Plata** (en-hero-suave): texto secundario dentro de la tarjeta hero clara.
- **Carbón** (carbon a carbon-4): fondo y escalera de contenedores en oscuro; la tarjeta hero oscura usa carbon-4 y la barra flotante oscura carbon-3.
- **Humo** (humo) y **Humo Suave** (humo-suave): texto principal y secundario en oscuro.

### Named Rules
**The Ámbar Pendiente Rule.** El ámbar significa "todavía no salió": en cola, sin sincronizar, valor desde caché, paso en curso. No se usa como decoración ni como color de botón.

**The Color Solo En Estado Rule.** Azul, verde, rojo y violeta existen solo dentro de chips de estado, nodos del tracker y puntos de la línea de tiempo. Las superficies y acciones son monocromas.

**The Un Solo Hero Rule.** Una sola superficie de máximo contraste (tarjeta hero) por pantalla.

## Typography

**Display Font:** Roboto, fuente del sistema Android (`FontFamily.Default`)
**Body Font:** Roboto, fuente del sistema Android

**Character:** Una sola familia nativa, decisión fijada por el usuario para la plataforma; la jerarquía se construye con escalones de peso marcados (Bold, SemiBold, Medium, Normal) y tracking negativo en los tamaños grandes. Todo en sp para respetar la escala del sistema.

### Hierarchy
- **Display** (Bold 700, 34sp, 40sp, -0.8sp, cifras tabulares): el monto "Ganado este mes" de la tarjeta hero.
- **Headline** (Bold 700, 24sp, 30sp, -0.4sp): títulos de la barra superior y de login, y el total destacado de TarjetaTotales (con cifras tabulares).
- **Title Large** (SemiBold 600, 20sp, 26sp, -0.2sp): saludo de inicio, número de cotización y razón social en la cabecera de detalle, total del formulario.
- **Title Medium** (SemiBold 600, 16sp, 22sp, cifras tabulares): títulos de sección, número de cotización en tarjeta y totales (en Bold).
- **Title Small** (SemiBold 600, 14sp, 20sp): hora y título de cada evento de la línea de tiempo, iniciales de avatar.
- **Body Large / Medium / Small** (Normal 400; 16/24, 14/20, 12/16): nombre de cliente, filas dato-valor (valor en SemiBold), etiquetas de dato y fechas.
- **Label Large** (SemiBold 600, 15sp, 20sp): texto de todo botón y de la pestaña activa.
- **Label Medium / Small** (Medium 500; 12/16, 11/14): chips de estado, títulos del tracker y sus fechas.

### Named Rules
**The Cifra Quieta Rule.** Montos y números de guía usan cifras tabulares ("tnum") para no bailar al animarse ni al compararse en columna.

**The Etiqueta Sobre Dato Rule.** Un dato se presenta como etiqueta gris en Body Small encima de su valor (estilo guía de despacho), en minúscula normal, nunca en mayúsculas espaciadas.

## Layout

Una columna de ancho completo con margen lateral de 20dp y separaciones de 10 a 16dp entre tarjetas (10dp entre tarjetas de lista, 16-20dp entre bloques). Las tarjetas acolchan 18dp; la tarjeta hero 20dp. Los títulos de sección llevan 12dp arriba y 4dp abajo, con una acción de texto opcional a la derecha ("Ver todas").

Las pantallas con barra de navegación reservan 96dp al final de su contenido (más 16dp en Inicio) para que la píldora flotante no tape ni el contenido ni el botón flotante. Las tarjetas de cotización organizan sus datos en dos columnas: cliente y fecha a la izquierda, total alineado a la derecha. Las filas dato-valor alinean el valor a la derecha. Todo objetivo táctil mide al menos 48dp (botones circulares 48dp, ítems de barra 52dp, botones 52-56dp).

## Elevation & Depth

Plano y tonal. La profundidad se construye con la escalera de contenedores (blanco, Gris Niebla, Gris Pista; en oscuro carbón 0 a 4) y con el contraste extremo de la tarjeta hero. Las tarjetas no tienen sombra ni borde.

### Shadow Vocabulary
- **Barra flotante** (`shadowElevation = 12dp`): solo la barra de navegación en píldora, que flota sobre el contenido que pasa por debajo.

### Named Rules
**The Plano Por Tono Rule.** Las superficies se separan por tono, no por sombra. La única sombra del sistema pertenece a la barra flotante.

## Shapes

Esquinas generosas y píldoras. Escala: 8, 12, 16, 24 y 28dp más píldora (50%). Tarjetas y bloques de detalle a 24dp; tarjeta hero a 28dp (el contrato hablaba de 24dp, el build se asentó en 28dp para el hero); campos de texto a 16dp. Botones, buscadores, chips, barra de navegación y su pestaña activa son píldoras. Avatares, botones de ícono y nodos del tracker son círculos. Los bordes son finos (1dp) y aparecen en campos, botón secundario, buscadores y en el borde negro de la tarjeta hero clara; los nodos del tracker llevan anillo de 2 a 2.5dp. La línea de tiempo une sus puntos con una línea punteada de 1.5dp (trazos de 6).

## Components

Todo elemento tocable responde a la presión achicándose a 0.97 en 120ms con EaseOutFuerte (`cubic-bezier(0.23, 1, 0.32, 1)`), además del ripple de Material donde existe.

### Buttons
- **Shape:** píldora completa.
- **Principal (BotonCarga):** fondo `primary` (negro en claro, humo en oscuro), texto Label Large, ancho completo, 56dp de alto, ícono opcional de 20dp a 10dp del texto. Mientras carga, mantiene su color pleno y cruza (fundido 200ms) a un indicador circular de 22dp. Deshabilitado: `primary` al 30%.
- **Secundario (BotonSecundario):** contorno de 1dp en Gris Borde, fondo transparente, 52dp de alto; variante destructiva con texto en color error.
- **Flotante (BotonFlotante):** píldora extendida `primary` con ícono y texto; una sola por pantalla de lista.
- **Botón hero:** píldora Gris Niebla (claro) o Humo (oscuro) con texto negro, en pareja dentro de la tarjeta hero.
- **Circular:** botón de ícono de 48dp sobre Gris Niebla para acciones de cabecera.

### Chips
- **ChipEstado:** píldora con el par fondo + texto del estado, Label Medium, 7dp x 14dp. El color cruza animado (280ms) cuando la cotización cambia de estado. "En cola" sustituye la etiqueta cuando el envío espera señal.

### Cards / Containers
- **Corner Style:** 24dp (tarjeta hero 28dp).
- **Background:** Gris Niebla; hero en negro (claro) o carbon-4 (oscuro).
- **Shadow Strategy:** ninguna (ver Elevation & Depth).
- **Border:** ninguno, salvo el borde de 1dp de la tarjeta hero en claro.
- **Internal Padding:** 18dp; hero 20dp; bloque de detalle 8dp vertical x 18dp horizontal con filas de 9dp.
- **TarjetaCotizacion:** etiqueta "N° de cotización" + número a la izquierda, chip de estado a la derecha; cliente y fecha a la izquierda, total en Bold a la derecha; cierra con el tracker compacto (o completo en la cotización en curso).
- **TarjetaContacto:** avatar de iniciales de 48dp (círculo `onSurface`), nombre y cargo, y dos botones circulares de 48dp: correo contorneado y llamada rellena.

### Inputs / Fields
- **CampoTexto:** campo contorneado Material, radio 16dp, borde Gris Borde en reposo, ícono inicial obligatorio. Error con ícono rojo al final y mensaje que aparece con fundido y expansión; campo válido con check verde (`tertiary`).
- **CampoBusqueda / AtajoBusqueda:** píldora de 56dp sobre `surface` con borde de 1dp, lupa a la izquierda y botón de limpiar. El atajo se ve igual pero navega a la lista.

### Navigation
- **BarraNavegacionFlotante:** píldora flotante (blanco en claro, carbon-3 en oscuro) con 6dp de relleno y sombra de 12dp. Los ítems inactivos son solo ícono en Gris Texto; el activo es una píldora `primary` que muestra ícono + nombre, con el nombre entrando por resorte (StiffnessMediumLow) y el color cruzando en 200ms.

### TrackerCotizacion (componente firma)
El ciclo Creada → Enviada → Respuesta (Aceptada / Rechazada / Vencida) → Facturada como tracker de envío. Nodos de 28dp (compacto 18dp) sobre una pista Gris Pista de 4dp (compacto 3dp) con extremos redondos; la parte cumplida se llena en `onSurface` animando de 0 al avance en 700ms con EaseInOutFuerte (`cubic-bezier(0.77, 0, 0.175, 1)`) al aparecer. Nodo completado: círculo de superficie con anillo y check en `onSurface`. Nodo en curso: fondo Ámbar Crema, anillo ámbar y reloj. Nodo fallido: par del estado (cruz, o reloj si venció). Nodo pendiente: punto Gris Pista al 64%. La versión completa lleva título (Label Medium) y fecha (Label Small) bajo cada nodo; la compacta solo nodos y línea. Cotizaciones anuladas o reemplazadas se muestran al 45% de opacidad.

### LineaTiempo
Historial del detalle: hora (Title Small) y fecha a la izquierda en 64dp, columna central de 36dp con línea punteada y punto de 24dp con ícono de 14dp, título y detalle a la derecha. Punto hecho en `onSurface`; pendiente en ámbar de cola; alerta y error con los pares de Vencida y Rechazada.

## Do's and Don'ts

### Do:
- **Do** dar a cada pantalla de inicio o resumen una sola tarjeta hero de máximo contraste (28dp, relleno 20dp) con la cifra en Display y como mucho dos acciones píldora.
- **Do** mostrar el estado de una cotización como tracker (completo o compacto) además del chip; el chip solo no basta.
- **Do** separar superficies por tono (blanco, Gris Niebla, Gris Pista; carbón 0 a 4 en oscuro).
- **Do** usar píldora para botones, buscadores, chips y navegación, y 24dp para tarjetas.
- **Do** aplicar efectoPresion (0.97, 120ms, EaseOutFuerte) a todo elemento tocable nuevo.
- **Do** usar cifras tabulares en montos y números de cotización.
- **Do** reservar 96dp al final de toda pantalla con barra flotante.
- **Do** definir cada estado nuevo como par fondo suave + texto saturado, con su versión nocturna.

### Don't:
- **Don't** usar el ámbar para nada que no sea "pendiente, en cola, sin sincronizar o en curso".
- **Don't** poner azul, verde, rojo o violeta en superficies, botones o encabezados; ese color pertenece a los estados.
- **Don't** agregar sombras a tarjetas; la única sombra es la de la barra flotante.
- **Don't** poner dos tarjetas hero en la misma pantalla.
- **Don't** usar colores dinámicos del sistema (Material You); la paleta propia es fija.
- **Don't** escribir etiquetas de dato en mayúsculas espaciadas sobre los valores.

## Acciones de fila

Las filas editables (ítems de la cotización, contactos, usuarios, catálogo) se editan tocando la fila completa; las demás acciones van en un menú ⋮ (`MenuAcciones`) con íconos de contorno, y solo la acción destructiva (eliminar/quitar) se pinta en rojo. No se ponen pares de íconos editar/borrar sueltos al lado de cada fila.
