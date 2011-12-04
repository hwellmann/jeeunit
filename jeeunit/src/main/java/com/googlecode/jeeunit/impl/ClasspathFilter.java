package com.googlecode.jeeunit.impl;

import java.io.File;
import java.io.FileFilter;

public class ClasspathFilter implements FileFilter {
    
    private String[] excludes;

    public ClasspathFilter(String... basenamePrefix) {
        this.excludes = basenamePrefix;        
    }
    
    @Override
    public boolean accept(File pathname) {
        String path = pathname.getPath();
        String baseName = path.substring(path.lastIndexOf('/') + 1);
        for (String exclude : excludes) {                
            if (baseName.startsWith(exclude))
                return false;
        }
        return true;
    }
}
