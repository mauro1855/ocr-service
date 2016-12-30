package com.github.mauro1855.ocrservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by pereirat on 07/12/2016.
 */
@Configuration
public class DBConfig {
    @Bean
    public NamedParameterJdbcTemplate ocrServiceJdbcTemplate(@Autowired DataSource datasource)
    {
        return new NamedParameterJdbcTemplate(datasource);
    }
}
