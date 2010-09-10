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

package com.googlecode.jeeunit.example.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.googlecode.jeeunit.example.model.Author;
import com.googlecode.jeeunit.example.model.Book;

@Stateless
public class LibraryService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @PersistenceContext
    private EntityManager em;
    
    public void fillLibrary()
    {
        if (getNumBooks() != 0)
            return;
        
        Author mann = new Author();
        mann.setFirstName("Thomas");
        mann.setLastName("Mann");
        
        Author steinbeck = new Author();
        steinbeck.setFirstName("John");
        steinbeck.setLastName("Steinbeck");
        
        Book buddenbrooks = new Book();
        buddenbrooks.setTitle("Buddenbrooks");
        buddenbrooks.setAuthor(mann);
        mann.getBooks().add(buddenbrooks);
        
        Book eden = new Book();
        eden.setTitle("East of Eden");
        eden.setAuthor(steinbeck);
        steinbeck.getBooks().add(eden);
        
        em.persist(mann);
        em.persist(steinbeck);
        em.persist(buddenbrooks);
        em.persist(eden);
    }
    
    public List<Book> findBooksByAuthor(String lastName)
    {
        String jpql = "select b from Book b where b.author.lastName = :lastName";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("lastName", lastName);
        List<Book> books = query.getResultList();
        return books;       
    }
    
    public List<Book> findBooksByTitle(String title)
    {
        String jpql = "select b from Book b where b.title = :title";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("title", title);
        List<Book> books = query.getResultList();
        return books;       
    }
    
    public long getNumBooks()
    {
        String jpql = "select count(b) from Book b";
        Long numBooks = (Long) em.createQuery(jpql).getSingleResult();
        return numBooks;
    }
}
