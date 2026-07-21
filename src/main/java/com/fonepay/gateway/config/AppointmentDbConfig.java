package com.fonepay.gateway.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.fonepay.gateway.appointment.repository",
        entityManagerFactoryRef = "appointmentEntityManagerFactory",
        transactionManagerRef = "appointmentTransactionManager"
)
public class AppointmentDbConfig {

    @Bean(name = "appointmentDataSourceProperties")
    @ConfigurationProperties("spring.datasource.appointment")
    public DataSourceProperties appointmentDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "appointmentDataSource")
    public DataSource appointmentDataSource() {
        return appointmentDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "appointmentEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean appointmentEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("appointmentDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");

        return builder
                .dataSource(dataSource)
                .packages("com.fonepay.gateway.appointment.entity")
                .persistenceUnit("appointmentdb")
                .properties(properties)
                .build();
    }

    @Bean(name = "appointmentTransactionManager")
    public PlatformTransactionManager appointmentTransactionManager(
            @Qualifier("appointmentEntityManagerFactory") LocalContainerEntityManagerFactoryBean appointmentEntityManagerFactory) {
        return new JpaTransactionManager(appointmentEntityManagerFactory.getObject());
    }

    @Bean(name = "appointmentFlyway", initMethod = "migrate")
    public Flyway appointmentFlyway(@Qualifier("appointmentDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/appointmentdb")
                .baselineOnMigrate(true)
                .cleanDisabled(false)
                .load();
        flyway.clean();
        return flyway;
    }
}
