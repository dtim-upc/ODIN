#!/bin/bash

# Obtén el directorio del script actual
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "$SCRIPT_DIR"

# Verifica el sistema operativo
if [[ "$OSTYPE" == "linux-gnu" || "$OSTYPE" == "msys" ]]; then
    # Configuración específica para Linux y Git Bash en Windows
    GRADLE_WRAPPER="./gradlew"
    COPY_COMMAND="cp"
else
    echo "Sistema operativo no compatible: $OSTYPE"
    exit 1
fi

# Define los paths de los proyectos Gradle (relativos al directorio del script)
proyecto1="../../NextiaCore"

# Define la tarea específica que deseas ejecutar en cada proyecto
tarea="uberJar"

# Define el path donde deseas copiar el JAR después de compilar (relativo al directorio del script)
destino="../ODIN/api/lib"

# Función para compilar y ejecutar tarea específica en un proyecto Gradle
compilar_y_ejecutar() {
    echo "CAMBIANDO PROYECTO A $1"
    cd "$1" || exit
    echo "PROYECTO $1 ENCONTRADO"
    echo "GENERANDO JAR $1"
    pwd
    ls -l   # Listar archivos con detalles
    $GRADLE_WRAPPER "$2"
    echo "JAR $1 GENERADO"
    cd - || exit
}

# Compilar y ejecutar tarea para cada proyecto
compilar_y_ejecutar "$proyecto1" "$tarea"

# Copiar el JAR a la ruta de destino
$COPY_COMMAND "$proyecto1/build/libs/nextiacore-0.0.2-SNAPSHOT-uber.jar" "$destino"

echo "Proceso completado."

