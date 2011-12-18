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
