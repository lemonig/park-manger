package com.ldz.park.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;

@Configuration
//以 mysql 开头的属性都会被映射到当前类的对应属性上
@ConfigurationProperties(prefix = "mysql")
public class MySQLConfiguration {


    @NotNull
    private String user;

    @NotNull
    private String password;

    @NotNull
    private String url;


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Bean
    public DataSource dataSource()throws SQLException{
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(getUser());
        hikariConfig.setPassword(getPassword());
        hikariConfig.setJdbcUrl(getUrl());
        hikariConfig.setPoolName("ServerHikariCP");
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setConnectionTimeout(80 * 1000);
        hikariConfig.setIdleTimeout(80 * 1000);
        hikariConfig.setMaxLifetime(100 * 1000);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setJdbcUrl(getUrl() + "&allowMultiQueries=true");
        return new HikariDataSource(hikariConfig);
    }
}
