/*
 * Copyright 2010 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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

    
    /**
     * Tries to look up bean manager first from JNDI, and failing that
     * from any service providers that might be registered using
     * META-INF/services.
     * 
     * @return bean manager, or null
     */
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
