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

import static com.googlecode.jeeunit.impl.Constants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationLoader {

    public Configuration load() {
        Configuration config = new Configuration();
        Properties props = new Properties();
        InputStream is = getClass().getResourceAsStream("/" + CONFIG_PROPERTIES);
        if (is != null) {
            try {
                props.load(is);
            }
            catch (IOException exc) {
                throw new RuntimeException(exc);
            }
            String httpPortString = props.getProperty(KEY_HTTP_PORT, HTTP_PORT_DEFAULT);
            config.setHttpPort(Integer.valueOf(httpPortString));
            
            String weldListenerString = props.getProperty(KEY_WELD_LISTENER, "false");
            config.setEnableWeldListener(Boolean.parseBoolean(weldListenerString));

            config.setWarBase(props.getProperty(KEY_WAR_BASE));
            config.setServerHome(props.getProperty(KEY_SERVER_HOME));
        }
        return config;
    }
}
