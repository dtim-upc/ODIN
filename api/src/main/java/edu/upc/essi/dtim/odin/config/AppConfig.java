package edu.upc.essi.dtim.odin.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {
    public AppConfig() {
        // Constructor implementation is empty.
        // No additional logic is required at the moment.
    }

    @Value("${dataStorage.DataBaseType}")
    public String DBTypeProperty;

    @Value("${dataStorage.diskPath}")
    public String diskPath;

    @Value("${dataStorage.JenaPath}")
    private String JenaPath;
}
