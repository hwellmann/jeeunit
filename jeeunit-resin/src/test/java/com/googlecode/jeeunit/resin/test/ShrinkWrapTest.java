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
package com.googlecode.jeeunit.resin.test;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;


public class ShrinkWrapTest {

    @Test
    public void buildWar() {
        File exported = new File(System.getProperty("java.io.tmpdir"), "exported.war");
        exported.delete();
        WebArchive war = ShrinkWrap.create(WebArchive.class).
            as(ExplodedImporter.class).importDirectory("target/classes").
            as(WebArchive.class);
        war.as(ZipExporter.class).exportTo(exported);
    }
}
