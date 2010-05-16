package com.googlecode.jeeunit;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.googlecode.jeeunit.spi.BeanManagerProvider;

public class BeanManagerLookup {

    private static final String BEAN_MANAGER_JNDI = "java:comp/BeanManager";
    private static BeanManager mgr;

    public static BeanManager getBeanManager() {
        if (mgr == null) {
            getBeanManagerFromJndi();
        }
        if (mgr == null) {
            ServiceLoader<BeanManagerProvider> loader = ServiceLoader
                    .load(BeanManagerProvider.class);
            Iterator<BeanManagerProvider> it = loader.iterator();

            while (mgr == null && it.hasNext()) {
                BeanManagerProvider provider = it.next();
                mgr = provider.getBeanManager();
            }
        }

        return mgr;
    }

    private static void getBeanManagerFromJndi() {
        try {
            InitialContext ctx = new InitialContext();
            mgr = (BeanManager) ctx.lookup(BEAN_MANAGER_JNDI);
        } catch (NamingException e) {
            mgr = null;
        }
    }
}
