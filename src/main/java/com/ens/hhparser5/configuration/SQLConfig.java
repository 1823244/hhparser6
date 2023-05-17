package com.ens.hhparser5.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class SQLConfig {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String pass;

    private Logger logger = LoggerFactory.getLogger(SQLConfig.class);

    @Bean
    public Connection getConnection() {
        //if (sqlConnection == null) {
        Connection sqlConnection;
            try {
                //Class.forName("org.postgresql.Driver");
                //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                sqlConnection = DriverManager.getConnection(url, user, pass);
            } catch (SQLException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        //}
        return sqlConnection;
    }

}
