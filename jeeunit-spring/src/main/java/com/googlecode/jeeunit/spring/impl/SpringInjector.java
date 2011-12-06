/*
 * Copyright 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.jeeunit.spring.impl;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import com.googlecode.jeeunit.spi.Injector;

/**
 * Uses a Spring bean factory to injects fields into the given target.
 * 
 * @author hwellmann
 *
 */
public class SpringInjector implements Injector {
    
    private AutowireCapableBeanFactory beanFactory;

    public SpringInjector(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void injectFields(Object target) {
        beanFactory.autowireBean(target);
    }

}
