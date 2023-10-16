package edu.upc.essi.dtim.odin.frontend;

import edu.upc.essi.dtim.odin.bootstrapping.SourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    @GetMapping("/formSchema")
    public String getFormSchema() {
        logger.info("FORMSCHEMA ASKED");
        try {
            // Especifica la ruta completa al archivo en el sistema de archivos local.
            String filePath = "C:\\Users\\victo\\Documents\\GitHub\\ODIN\\api\\src\\main\\resources\\frontend-schemas\\DatasetForm.json";
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists()) {
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                String formSchema = new String(bytes);
                logger.info("FORMSCHEMA RETRIEVED");
                return formSchema;
            } else {
                logger.error("FORMSCHEMA not found");
                // El archivo no existe en la ubicación especificada.
                return "El archivo no se encontró.";
            }
        } catch (Exception e) {
            logger.error("FORMSCHEMA ERROR: " + e.toString());
            // Manejar errores apropiadamente
            e.printStackTrace();
            return null;
        }
    }
}
