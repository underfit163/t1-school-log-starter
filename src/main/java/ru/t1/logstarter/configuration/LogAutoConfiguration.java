package ru.t1.logstarter.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import ru.t1.logstarter.aspect.HttpLogAspect;
import ru.t1.logstarter.aspect.LogAspect;

import java.io.IOException;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({LogProperties.class, HttpLogProperties.class})
public class LogAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "logging.service", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LogAspect logAspect(LogProperties LogProperties) {
        Logger logger = (Logger) LoggerFactory.getLogger(LogAspect.class);

        logger.setLevel(Level.convertAnSLF4JLevel(LogProperties.filterLevel()));
        return new LogAspect(LogProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging.http", name = "enabled", havingValue = "true", matchIfMissing = true)
    public HttpLogAspect httpLogAspect(HttpLogProperties httpLogProperties) {
        Logger logger = (Logger) LoggerFactory.getLogger(HttpLogAspect.class);

        logger.setLevel(Level.convertAnSLF4JLevel(httpLogProperties.filterLevel()));
        return new HttpLogAspect(httpLogProperties);
    }

    @Bean
    public OncePerRequestFilter requestCachingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws IOException, ServletException {
                HttpServletRequest wrappedRequest = new ContentCachingRequestWrapper(request); //для чтения тела запроса
                filterChain.doFilter(wrappedRequest, response);
            }
        };
    }
}
