package ch.bpm.workflow.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.operaton.bpm.engine.ProcessEngineConfiguration;
import org.operaton.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.operaton.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import org.operaton.bpm.spring.boot.starter.property.OperatonBpmProperties;
import org.operaton.bpm.spring.boot.starter.rest.OperatonJerseyResourceConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

import static ch.bpm.workflow.example.common.LogMessage.READING_CONFIG_CLASS;
import static ch.bpm.workflow.example.config.security.camunda.CamundaLdapConfiguration.printLdapIdentityProviderPlugin;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile({ "local" })
public class InitializingBeanImpl implements InitializingBean {

    private static final String SEPARATOR = "---------------------------------------------------------------------------";

    private static final String MANAGER_PASSWORD_FIELD = "managerPassword";

    private final LdapIdentityProviderPlugin ldapIdentityProviderPlugin;

    private final ProcessEngineConfiguration processEngineConfiguration;

    private final OperatonBpmProperties operatonBpmProperties;

    private final OperatonJerseyResourceConfig operatonJerseyResourceConfig;

    @Override
    public void afterPropertiesSet() {
        log.debug(READING_CONFIG_CLASS.getMessage(), ldapIdentityProviderPlugin.getClass().getName(),
                printLdapIdentityProviderPlugin(ldapIdentityProviderPlugin));
        log.debug(READING_CONFIG_CLASS.getMessage(), ldapIdentityProviderPlugin.getClass().getName(), SEPARATOR);

        log.debug(READING_CONFIG_CLASS.getMessage(), processEngineConfiguration.getClass().getName(),
                printSpringProcessEngineConfiguration(processEngineConfiguration));
        log.debug(READING_CONFIG_CLASS.getMessage(), processEngineConfiguration.getClass().getName(), SEPARATOR);

        log.debug(READING_CONFIG_CLASS.getMessage(), operatonBpmProperties.getClass().getName(),
                printOperatonBpmProperties(operatonBpmProperties));
        log.debug(READING_CONFIG_CLASS.getMessage(), operatonBpmProperties.getClass().getName(), SEPARATOR);

        log.debug(READING_CONFIG_CLASS.getMessage(), operatonBpmProperties.getClass().getName(),
                printOperatonBpmProperties(operatonBpmProperties));
        log.debug(READING_CONFIG_CLASS.getMessage(), operatonBpmProperties.getClass().getName(), SEPARATOR);

        log.debug(READING_CONFIG_CLASS.getMessage(), operatonJerseyResourceConfig.getClass().getName(),
                printOperatonJerseyResourceConfig(operatonJerseyResourceConfig));
        log.debug(READING_CONFIG_CLASS.getMessage(), operatonJerseyResourceConfig.getClass().getName(), SEPARATOR);
    }

    private static String printSpringProcessEngineConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
        SpringProcessEngineConfiguration springProcessEngineConfiguration = (SpringProcessEngineConfiguration) processEngineConfiguration;

        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(springProcessEngineConfiguration,
                ToStringStyle.MULTI_LINE_STYLE) {
            @Override
            protected boolean accept(Field field) {
                return !field.getName().equals(MANAGER_PASSWORD_FIELD);
            }
        };
        return builder.toString();
    }

    private static String printOperatonBpmProperties(OperatonBpmProperties operatonBpmProperties) {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(operatonBpmProperties,
                ToStringStyle.MULTI_LINE_STYLE) {
            @Override
            protected boolean accept(Field field) {
                return !field.getName().equals(MANAGER_PASSWORD_FIELD);
            }
        };
        return builder.toString();
    }

    private static String printOperatonJerseyResourceConfig(OperatonJerseyResourceConfig operatonJerseyResourceConfig) {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(operatonJerseyResourceConfig,
                ToStringStyle.MULTI_LINE_STYLE) {
            @Override
            protected boolean accept(Field field) {
                return !field.getName().equals(MANAGER_PASSWORD_FIELD);
            }
        };
        return builder.toString();
    }

}
