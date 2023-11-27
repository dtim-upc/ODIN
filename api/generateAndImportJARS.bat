cd ../../NextiaCore
./gradlew --stop &
wait

./gradlew.bat uberJar
wait

cd ../NextiaDataLayer
./gradlew.bat uberJar

cd ../NextiaJD2
./gradlew.bat uberJar

cd ../NextiaBS
./gradlew.bat uberJar

cd ../NextiaDI
./gradlew.bat uberJar

echo "Archivo copiado correctamente."
