package com.arenarpg.backend.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** Cria o pool somente no perfil postgres, sem exigir banco no perfil padrão. */
@Configuration
@Profile("postgres")
public class PostgresDataSourceConfiguration {
    @Bean
    DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.hikari.initialization-fail-timeout:-1}") long initializationFailTimeout,
            @Value("${spring.datasource.hikari.connection-timeout:3000}") long connectionTimeout) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setSchema("arena");
        config.setInitializationFailTimeout(initializationFailTimeout);
        config.setConnectionTimeout(connectionTimeout);
        return new HikariDataSource(config);
    }
}

