package com.ens.hhparser5.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceConfig {

    @Autowired
    DataSourceProperties dataSourceProperties;


    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource ds() {
        DataSource ds;
        try {
            ds = dataSourceProperties.initializeDataSourceBuilder().build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ds;
    }

    /*@Bean
    @ConfigurationProperties("spring.datasource")
    public HikariDataSource dsHikari() {
        HikariDataSource ds;
        try {
            //ds = dataSourceProperties.initializeDataSourceBuilder().build();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dataSourceProperties.getUrl());
            config.setUsername(dataSourceProperties.getUsername());
            config.setPassword(dataSourceProperties.getPassword());
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "100");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            ds = new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ds;
    }*/

}
