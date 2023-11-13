package edu.upc.essi.dtim.odin;

import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLoadingSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@PropertySource("classpath:credentials.properties")
public class OdinApplication {
    private static final Logger logger = LoggerFactory.getLogger(OdinApplication.class);
    private static AppConfig appConfig;

    public OdinApplication(@Autowired AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public static void main(String[] args) {
        // Inicia la aplicación Spring Boot
        SpringApplication.run(OdinApplication.class, args);

        //INICIALIZA DATALAYER
        DataLoading dl = DataLoadingSingleton.getInstance(appConfig.getDataLayerPath());

        // Registra un mensaje en el registro de eventos cuando la aplicación se inicia correctamente
        logger.info("Application started. Ready to receive API requests.");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
