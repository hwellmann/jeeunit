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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class CdiJUnitRunner extends BlockJUnit4ClassRunner {

	private static final String BEAN_MANAGER_JNDI = "java:comp/BeanManager";

	private static BeanManager mgr;

	public CdiJUnitRunner(Class<?> klass) throws InitializationError {
		super(klass);
		lookupBeanManager();
	}

	@Override
	protected Object createTest() throws Exception {
		Object test = super.createTest();
		inject(test);
		return test;
	}

	@SuppressWarnings("unchecked")
	private void inject(Object test) {
		AnnotatedType annotatedType = mgr.createAnnotatedType(test.getClass());
		InjectionTarget target = mgr.createInjectionTarget(annotatedType);
		CreationalContext context = mgr.createCreationalContext(null);
		target.inject(test, context);
	}

	private static BeanManager lookupBeanManager() throws InitializationError {
		if (mgr == null) {
			try {
				InitialContext ctx = new InitialContext();
				mgr = (BeanManager) ctx.lookup(BEAN_MANAGER_JNDI);
			} catch (NamingException e) {
				throw new InitializationError(e);
			}
		}
		return mgr;
	}
}
