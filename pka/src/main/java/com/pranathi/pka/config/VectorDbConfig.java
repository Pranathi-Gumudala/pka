
package com.pranathi.pka.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
public class VectorDbConfig {

    // Primary application database: MySQL
    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    // Separate database for RAG vectors: PostgreSQL
    @Bean(name = "vectorDataSource")
    public DataSource vectorDataSource(
            @Value("${app.vector-db.url}") String url,
            @Value("${app.vector-db.username}") String username,
            @Value("${app.vector-db.password}") String password) {

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "vectorJdbcTemplate")
    public JdbcTemplate vectorJdbcTemplate(
            @Qualifier("vectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
}}
