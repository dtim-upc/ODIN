cd ../../NextiaCore
./gradlew.bat ubeJar

cd ../NextiaDataLayer
./gradlew.bat ubeJar

cd ../NextiaJD2
./gradlew.bat ubeJar

cd ../NextiaBS
./gradlew.bat ubeJar

cd ../NextiaDI
./gradlew.bat ubeJar

echo "Archivo copiado correctamente."
