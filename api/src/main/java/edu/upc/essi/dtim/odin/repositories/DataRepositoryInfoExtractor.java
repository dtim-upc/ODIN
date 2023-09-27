package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DataRepositoryInfoExtractor {

    public static List<DataRepositoryTypeInfo> extractDataRepositoryInfo(List<Class<? extends DataRepository>> dataRepositoryClasses) {
        List<DataRepositoryTypeInfo> dataRepositoryInfoList = new ArrayList<>();

        for (Class<? extends DataRepository> dataRepositoryClass : dataRepositoryClasses) {
            DataRepositoryTypeInfo info = new DataRepositoryTypeInfo();
            info.setName(dataRepositoryClass.getSimpleName()); // Nombre de la clase
            info.setLabel(getLabelForDataRepositoryClass(dataRepositoryClass)); // Etiqueta para mostrar en el desplegable

            Field[] fields = dataRepositoryClass.getDeclaredFields();
            for (Field field : fields) {
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.setName(field.getName());
                fieldInfo.setLabel(getLabelForField(field));
                fieldInfo.setType(field.getType().getSimpleName()); // Tipo del atributo
                info.addFieldInfo(fieldInfo);
            }

            dataRepositoryInfoList.add(info);
        }

        return dataRepositoryInfoList;
    }

    private static String getLabelForDataRepositoryClass(Class<? extends DataRepository> dataRepositoryClass) {
        // Aquí puedes implementar la lógica para obtener la etiqueta de la clase.
        // Por ejemplo, podrías usar anotaciones personalizadas en las clases.
        // En este ejemplo, simplemente usamos el nombre de la clase.
        return dataRepositoryClass.getSimpleName();
    }

    private static String getLabelForField(Field field) {
        // Aquí puedes implementar la lógica para obtener la etiqueta del campo.
        // Por ejemplo, podrías usar anotaciones personalizadas en los campos.
        // En este ejemplo, simplemente usamos el nombre del campo.
        return field.getName();
    }
}

