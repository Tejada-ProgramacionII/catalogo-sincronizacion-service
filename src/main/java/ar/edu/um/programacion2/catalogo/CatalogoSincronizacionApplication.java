package ar.edu.um.programacion2.catalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CatalogoSincronizacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogoSincronizacionApplication.class, args);
    }

}
