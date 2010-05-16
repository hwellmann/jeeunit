package com.googlecode.jeeunit.example.test.se;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceContextProvider {

    protected EntityManager em;

    @Produces
    public EntityManager getEntityManager() throws IOException {
        if (em == null) {
            createEntityManager();
        }
        return em;
    }

    private void createEntityManager() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("javax.persistence.jdbc.driver",
                "org.apache.derby.jdbc.ClientDriver");
        props.put("javax.persistence.jdbc.url",
                "jdbc:derby://localhost/sun-appserv-samples;create=true");
        props.put("javax.persistence.jdbc.user", "APP");
        props.put("javax.persistence.jdbc.password", "APP");
        props.put("hibernate.hbm2ddl.auto", "create");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(
                "library", props);
        em = emf.createEntityManager();
    }
}
