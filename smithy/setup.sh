#!/bin/bash
# ─────────────────────────────────────────────────────────────
# setup.sh — Genera el Gradle Wrapper dentro del proyecto
# Ejecutar UNA SOLA VEZ después de descomprimir el ZIP
#
# Requisitos: Java 17+ y conexión a internet
# Uso:        cd mini-github-smithy && bash setup.sh
# ─────────────────────────────────────────────────────────────

set -e

echo "╔══════════════════════════════════════════╗"
echo "║   Mini-GitHub Smithy — Setup inicial     ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# 1. Verificar Java
if ! command -v java &> /dev/null; then
    echo "❌ Java no encontrado. Instala Java 17+ y vuelve a ejecutar."
    exit 1
fi
JAVA_VER=$(java -version 2>&1 | head -1)
echo "✅ Java encontrado: $JAVA_VER"

# 2. Verificar que estamos en el directorio correcto
if [ ! -f "smithy-build.json" ]; then
    echo "❌ Ejecuta este script desde la raíz del proyecto (donde está smithy-build.json)"
    exit 1
fi

# 3. Descargar Gradle si no está disponible
if ! command -v gradle &> /dev/null; then
    echo "⬇️  Gradle no encontrado, descargando versión 8.10.2..."
    GRADLE_VERSION="8.10.2"
    GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
    GRADLE_ZIP="/tmp/gradle-${GRADLE_VERSION}.zip"
    GRADLE_DIR="/tmp/gradle-${GRADLE_VERSION}"

    curl -fsSL "$GRADLE_URL" -o "$GRADLE_ZIP"
    unzip -q "$GRADLE_ZIP" -d /tmp/
    GRADLE_CMD="/tmp/gradle-${GRADLE_VERSION}/bin/gradle"
else
    GRADLE_CMD="gradle"
fi

# 4. Generar el Gradle Wrapper
echo "⚙️  Generando Gradle Wrapper..."
$GRADLE_CMD wrapper --gradle-version 8.10.2

echo ""
echo "✅ Wrapper generado. Archivos creados:"
echo "   - gradlew"
echo "   - gradlew.bat"
echo "   - gradle/wrapper/gradle-wrapper.jar"
echo ""
echo "🔨 Ahora puedes ejecutar el build con:"
echo "   ./gradlew build"
echo ""
echo "📄 O solo validar el modelo Smithy:"
echo "   ./gradlew smithyBuild"
