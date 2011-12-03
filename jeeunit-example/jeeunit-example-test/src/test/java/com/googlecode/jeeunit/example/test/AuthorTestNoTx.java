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

package com.googlecode.jeeunit.example.test;

import javax.ejb.EJBException;
import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.googlecode.jeeunit.JeeunitRunner;
import com.googlecode.jeeunit.example.service.LibraryServiceNoTx;

@RunWith(JeeunitRunner.class)
public class AuthorTestNoTx {

    @Inject
    private LibraryServiceNoTx service;
    
    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Test
    public void byAuthor() {
        // Caused by: javax.persistence.TransactionRequiredException: null
        exceptions.expect(EJBException.class);
        
        service.fillLibrary();
    }
}
