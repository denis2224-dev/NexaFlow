package com.nexaflow.userservice;

import com.nexaflow.userservice.config.AsyncSyncConfiguration;
import com.nexaflow.userservice.config.DatabaseTestcontainer;
import com.nexaflow.userservice.config.JacksonConfiguration;
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
        UserServiceApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.nexaflow.userservice.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}
