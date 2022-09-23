package com.zubanoff.ml.tts.book.creator.config;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        basePackages = {"com.zubanoff.ml.tts.book.creator.dao"},
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@EnableTransactionManagement
public class DBConfig {
    @Primary
    @Bean(name = "jpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa")
    public Properties getJpaProperties(){
        return new Properties();
    }

    @LiquibaseDataSource
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManagerCrawler(EntityManagerFactory entityManagerFactory){
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean containerEntityManagerFactory(
            Properties jpaProperties, DataSource dataSource
    ){
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);

        Properties props = new Properties();
        props.putAll(jpaProperties);
        props.put("hibernate.jdbc.batch.size", 25);
        factoryBean.setJpaProperties(props);

        factoryBean.setPackagesToScan("com.zubanoff.ml.tts.book.creator.model");
        factoryBean.setPersistenceUnitName("ML_DB");

        return factoryBean;
    }
}
