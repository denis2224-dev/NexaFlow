package com.nexaflow.billing;

import com.nexaflow.billing.config.AsyncSyncConfiguration;
import com.nexaflow.billing.config.DatabaseTestcontainer;
import com.nexaflow.billing.config.JacksonConfiguration;
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
        BillingServiceApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.nexaflow.billing.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}
