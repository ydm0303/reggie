package com.reggie.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LockProviderConfiuration {

    @Autowired
    DataSource dataSource;

    @Bean
    public LockProvider lockProvider () {
        return new JdbcTemplateLockProvider(dataSource);
    }
}
