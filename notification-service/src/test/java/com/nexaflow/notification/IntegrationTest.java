package com.nexaflow.notification;

import com.nexaflow.notification.config.AsyncSyncConfiguration;
import com.nexaflow.notification.config.DatabaseTestcontainer;
import com.nexaflow.notification.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        NotificationServiceApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.nexaflow.notification.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}
