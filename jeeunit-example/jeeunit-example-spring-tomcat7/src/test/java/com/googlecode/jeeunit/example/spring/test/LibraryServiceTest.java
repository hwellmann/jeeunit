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
