cd ../../NextiaCore
./gradlew.bat uberJar

cd ../NextiaDataLayer
./gradlew.bat uberJar

cd ../NextiaJD2
./gradlew.bat uberJar

cd ../NextiaBS
./gradlew.bat uberJar

cd ../NextiaDI
./gradlew.bat uberJar

echo "Archivo copiado correctamente."
