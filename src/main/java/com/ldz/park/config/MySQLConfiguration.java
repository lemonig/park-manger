package com.ldz.park.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.validation.constraints.NotNull;  // Jakarta 版（SB3 标准）
import javax.sql.DataSource;

@Configuration
@ConfigurationProperties(prefix = "mysql")  // 保持您的自定义 prefix
public class MySQLConfiguration {

    @NotNull
    private String user;

    @NotNull
    private String password;

    @NotNull
    private String url;

    // getter/setter
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
    public DataSource dataSource() {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("数据库 URL 不能为空，请检查配置 mysql.url");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url + (url.contains("?") ? "&" : "?") + "allowMultiQueries=true");  // 安全拼接
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("ServerHikariCP");
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(80000);
        config.setIdleTimeout(80000);
        config.setMaxLifetime(100000);
        config.setMinimumIdle(5);

        return new HikariDataSource(config);
    }
}