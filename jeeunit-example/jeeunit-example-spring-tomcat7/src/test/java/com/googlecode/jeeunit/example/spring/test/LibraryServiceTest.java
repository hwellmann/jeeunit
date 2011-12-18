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
package com.googlecode.jeeunit.example.spring.test;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.googlecode.jeeunit.Transactional;
import com.googlecode.jeeunit.example.service.LibraryService;
import com.googlecode.jeeunit.example.spring.web.ServiceSpringConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceSpringConfig.class)
@ActiveProfiles("test")
@Transactional
public class LibraryServiceTest {

    @Inject
    private LibraryService libraryService;
    
    @Before
    public void setUp() {
        libraryService.fillLibrary();        
    }
    
    @Test
    public void checkNumAuthors() {
        libraryService.fillLibrary();
        assertEquals(2, libraryService.getNumAuthors());
    }
}
