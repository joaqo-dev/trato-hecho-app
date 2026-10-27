#!/bin/bash
# Desarrollo local: levanta el servidor de Trato Hecho en este computador y lo publica con ngrok.
# (En el servidor de producción se usa Docker: ver compose.yml en la raíz del repo.)
# Uso:  ./backend/iniciar.sh tu-dominio.ngrok-free.app
# (el dominio fijo gratuito está en https://dashboard.ngrok.com/domains)
set -e
cd "$(dirname "$0")/.."

DOMINIO="${1:-$NGROK_DOMINIO}"
if [ -z "$DOMINIO" ]; then
  echo "Falta el dominio de ngrok. Uso: ./backend/iniciar.sh tu-dominio.ngrok-free.app"
  exit 1
fi

# Gradle necesita Java; se usa el que trae Android Studio si no hay otro
if [ -z "$JAVA_HOME" ] && [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi

# En el computador se usa SQLite (backend/tratohecho.db) y la clave de prueba del panel
export PANEL_CLAVE="${PANEL_CLAVE:-Trato123}"

echo "Iniciando el servidor en http://localhost:8080 ..."
./gradlew -p backend run -q --console=plain &
SERVIDOR=$!
trap 'kill $SERVIDOR 2>/dev/null' EXIT

until curl -s -o /dev/null http://localhost:8080/panel; do sleep 1; done

echo ""
echo "  API para la app:  https://$DOMINIO/v1/"
echo "  Panel web:        https://$DOMINIO/panel"
echo ""
ngrok http --url="https://$DOMINIO" 8080 --log=stdout --log-level=warn
