package com.googlecode.jeeunit.example.test.spring;

import javax.sql.DataSource;

import org.hibernate.ejb.HibernatePersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.googlecode.jeeunit.example.service.LibraryService;

@Configuration
public class SpringTestConfig {

    @Bean
    public LibraryService libraryService() {
        return new LibraryService();
    }

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource bean = new SimpleDriverDataSource(new org.apache.derby.jdbc.ClientDriver(),
                "jdbc:derby://localhost/sun-appserv-samples;create=true");

        return bean;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
      JpaTransactionManager tm = new JpaTransactionManager(entityManagerFactory().getObject());
      return tm;
    }
    

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        bean.setPersistenceProvider(new HibernatePersistence());
        bean.setPersistenceXmlLocation("classpath:META-INF/test-persistence.xml");
        return bean;
    }
}
