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

/**
 * This class provides (almost all of) the Spring application context required for testing
 * our example in Spring.
 * <p>
 * Since Spring 3.0.x still has a small number of configuration settings which are supported in
 * XML syntax only, we take an XML-centric approach, where the application is bootstrapped via a
 * traditional XML context which contains the XML-only settings and delegates all the rest to
 * this class.
 * <p>
 * The <code>@Configuration</code> annotation instructs Spring to scan this class for bean
 * provider methods with a <code>@Bean</code> annotation. Each such method yields a singleton
 * Spring bean with its type and name defined by the method return type and name, respectively.
 * 
 * @author hwellmann
 *
 */
@Configuration
public class SpringTestConfig {

    @Bean
    public LibraryService libraryService() {
        return new LibraryService();
    }

    @Bean
    public DataSource dataSource() {
        return new SimpleDriverDataSource(new org.apache.derby.jdbc.ClientDriver(),
                "jdbc:derby://localhost/sun-appserv-samples;create=true");
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
      return new JpaTransactionManager(entityManagerFactory().getObject());
    }
    

    /**
     * This bean is not the EntityManagerFactory itself, but a Spring factory for JPA 
     * EntityManagerFactories. The actual EntityManagerFactory can be obtained by invoking
     * <code>getObject()</code>.
     * <p>
     * The Spring factory lets us override settings from the default <code>persistence.xml</code>.
     * In particular, in the test environment we cannot access the data source via JNDI, and there
     * is no JTA transaction manager. This is why we have to use an alternative 
     * <code>test-persistence.xml</code>.
     * <p>
     * TODO: It would be nice to avoid the <code>test-persistence.xml</code> and to provide all the
     * required overrides in this bean.
     * 
     * @return
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        bean.setPersistenceProvider(new HibernatePersistence());
        bean.setPersistenceXmlLocation("classpath:META-INF/test-persistence.xml");
        return bean;
    }
}
