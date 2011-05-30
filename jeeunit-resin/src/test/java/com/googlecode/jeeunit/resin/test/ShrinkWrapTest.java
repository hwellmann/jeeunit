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
        WebArchive war = ShrinkWrap.create(WebArchive.class).
            as(ExplodedImporter.class).importDirectory("target/classes").
            as(WebArchive.class);
        war.as(ZipExporter.class).exportTo(new File("exported.war"));
    }
}
