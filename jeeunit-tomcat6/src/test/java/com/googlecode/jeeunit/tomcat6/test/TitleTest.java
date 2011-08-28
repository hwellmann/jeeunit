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

package com.googlecode.jeeunit.tomcat6.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.jeeunit.JeeunitRunner;
import com.googlecode.jeeunit.example.model.Book;
import com.googlecode.jeeunit.example.service.bmt.LibraryService;

@RunWith(JeeunitRunner.class)
public class TitleTest {
    
    @Inject
    private LibraryService service;
    
    @Before
    public void setUp()
    {
        service.fillLibrary();
    }
    
    @Test
    public void byTitle()
    {
        List<Book> books = service.findBooksByTitle("East of Eden");
        assertEquals(1, books.size());
        
        Book book = books.get(0);
        assertEquals("Steinbeck", book.getAuthor().getLastName());
    }


}
