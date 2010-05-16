package com.googlecode.jeeunit.spi;

import javax.enterprise.inject.spi.BeanManager;

public interface BeanManagerProvider {
	BeanManager getBeanManager();
}
