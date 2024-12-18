# t1-school-log-starter

### Разработка собственного Spring Boot стартера

**Цель:** Создать Spring Boot стартер, который будет добавлять функциональность логирования HTTP-запросов и ответов в приложении.

• Логирование реализуйте через Aspect (перенесите то что есть в стартер, старый аспект выпилить):
https://github.com/underfit163/t1-school/pull/3

• Добавьте возможность настройки логирования через параметры в application.properties или application.yml:
```
logging:
    config: classpath:/config/logback-spring.xml
    service:
        enabled: false
        filter-level: info
        log-level: info
    http:
        enabled: true
        filter-level: trace
        log-level: trace
```
• Например, разрешите пользователям включать или отключать логирование, а также выбирать уровень детализации логов.