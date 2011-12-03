package com.googlecode.jeeunit.tomcat7;

import org.apache.catalina.loader.WebappClassLoader;

public class NoCleanupWebappClassLoader extends WebappClassLoader {
    

    public NoCleanupWebappClassLoader() {
        super();
    }


    public NoCleanupWebappClassLoader(ClassLoader parent) {
        super(parent);                
    }

    
    
    @Override
    protected void clearReferences() {
        // do nothing
    }
}
