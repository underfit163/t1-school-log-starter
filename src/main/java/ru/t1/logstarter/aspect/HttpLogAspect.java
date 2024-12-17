package ru.t1.logstarter.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import ru.t1.logstarter.configuration.HttpLogProperties;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

@Aspect
public class HttpLogAspect {
    private final Logger logger;
    private final HttpLogProperties httpLogProperties;

    public HttpLogAspect(HttpLogProperties httpLogProperties) {
        this.logger = LoggerFactory.getLogger(HttpLogAspect.class);
        this.httpLogProperties = httpLogProperties;
    }

    @Pointcut("@annotation(ru.t1.logstarter.aspect.annotation.HttpLogExecution)")
    public void httpLogMethods() {
    }

    @Before("httpLogMethods()")
    public void logRequest() {

        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) return;

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n=== HTTP REQUEST ===\n");
        logMessage.append("Method: ").append(request.getMethod()).append("\n");
        logMessage.append("Path: ").append(request.getRequestURI()).append("\n");

        // Логирование параметров запроса
        if (!request.getParameterMap().isEmpty()) {
            logMessage.append("Query Params: ").append(request.getParameterMap()).append("\n");
        }

        // Логирование заголовков
        logMessage.append("Headers:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logMessage.append("  ").append(headerName).append(": ")
                    .append(request.getHeader(headerName)).append("\n");
        }

        // Логирование тела запроса
        if (request.getMethod().equalsIgnoreCase("POST") ||
            request.getMethod().equalsIgnoreCase("PUT")) {
            try {
                String body = new String(((ContentCachingRequestWrapper) request).getContentAsByteArray());
                logMessage.append("Body: ").append(body).append("\n");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logMessage.append("Body: [unreadable]").append("\n");
            }
        }

        logMessage.append("====================");
        logAtLevel(logMessage.toString());
    }

    @AfterReturning(pointcut = "httpLogMethods()", returning = "result")
    public void logResponse(Object result) {

        HttpServletResponse response = getCurrentHttpResponse();
        if (response == null) return;

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n=== HTTP RESPONSE ===\n");
        logMessage.append("Status: ").append(response.getStatus()).append("\n");

        // Логирование заголовков ответа
        response.getHeaderNames().forEach(headerName ->
                logMessage.append("  ").append(headerName).append(": ")
                        .append(response.getHeader(headerName)).append("\n")
        );

        // Логирование тела ответа
        logMessage.append("Body: ").append(result).append("\n");
        logMessage.append("====================");
        logAtLevel(logMessage.toString());
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attributes != null) ? attributes.getRequest() : null;
    }

    private HttpServletResponse getCurrentHttpResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attributes != null) ? attributes.getResponse() : null;
    }

    private void logAtLevel(String message, Object... args) {
        logger.atLevel(httpLogProperties.logLevel()).log(message, args);
    }
}
