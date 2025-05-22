package edu.upc.essi.dtim.odin;

import edu.upc.essi.dtim.CyclopsLTS.lts.LTS;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.nextiaInterfaces.cyclopsLTS.LTSSingleton;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class OdinApplication {
    private static final Logger logger = LoggerFactory.getLogger(OdinApplication.class);
    private static AppConfig appConfig;

    public OdinApplication(@Autowired AppConfig appConfig) {
        OdinApplication.appConfig = appConfig;
    }

    public static void main(String[] args) {
        // Initialize Spring Boot application
        SpringApplication.run(OdinApplication.class, args);

        // Initialize data layer
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        // dl.initialize(); // Mock call to initialize the DataLayer systems. Uncomment in production.

        // Initialize LTS
        LTS lts = LTSSingleton.getInstance(appConfig);
        // lts.initialize(); // Mock call to initialize the LTS systems. Uncomment in production.

        logger.info("Application started. Ready to receive API requests.");
    }

    // Do not remove
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
