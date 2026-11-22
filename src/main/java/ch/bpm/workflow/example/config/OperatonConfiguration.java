package ch.bpm.workflow.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
/*
 * Ensures a single PropertySourcesPlaceholderConfigurer in the application context. Some
 * Operaton Spring Boot components register their own placeholder configurer, which
 * otherwise clashes with the Spring Boot auto-configured one.
 */
public class OperatonConfiguration {

    @Bean
    @Primary
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
