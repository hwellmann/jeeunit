/*
 * Copyright 2011 Harald Wellmann
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

package com.googlecode.jeeunit.example.test.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.googlecode.jeeunit.example.model.Book;
import com.googlecode.jeeunit.example.service.LibraryService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/test-context.xml")
public class AuthorTest {

    @Inject
    private LibraryService libraryService;

    @Before
    public void setUp() {
        libraryService.fillLibrary();
    }

    @Test
    public void byAuthor() {
        List<Book> books = libraryService.findBooksByAuthor("Mann");
        assertEquals(1, books.size());

        Book book = books.get(0);
        assertEquals("Buddenbrooks", book.getTitle());
    }

    /**
     * Test case for Glassfish <a
     * href="https://glassfish.dev.java.net/issues/show_bug.cgi?id=12599">bug #12599</a>.
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    @Ignore
    public void serialization() throws IOException, ClassNotFoundException {
        long expectedNumBooks = libraryService.getNumBooks();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(libraryService);
        oos.close();

        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        assertTrue(obj instanceof LibraryService);
        
        // the deserialized proxy throws a NullPointerException on method invocation
        long numBooks = ((LibraryService) obj).getNumBooks();
        assertEquals(expectedNumBooks, numBooks);
    }

}
