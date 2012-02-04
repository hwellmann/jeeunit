/*
 * Copyright 2012 Harald Wellmann
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
package com.googlecode.jeeunit.example.test.se;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.EntityManager;

import org.junit.Test;

import com.googlecode.jeeunit.openwebbeans.OpenWebBeansContainer;

public class OwbTest {

    
    @Test
    public void launchContainer() throws Exception {
        OpenWebBeansContainer container = OpenWebBeansContainer.getInstance();
        container.launch();
        BeanManager beanManager = container.getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(EntityManager.class);
        assertThat(beans.isEmpty(), is(false));
        container.shutdown();
    }
}
