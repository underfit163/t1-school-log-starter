package ru.t1.logstarter.configuration;


import org.slf4j.event.Level;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logging.service")
public record LogProperties(String enabled, Level filterLevel, Level logLevel) {

    public LogProperties {
        // Устанавливаем значение по умолчанию, если level не указан
        if (filterLevel == null) {
            filterLevel = Level.INFO;
        }

        if (logLevel == null) {
            logLevel = Level.INFO;
        }
    }
}
