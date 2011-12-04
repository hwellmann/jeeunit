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

            config.setServerHome(props.getProperty(KEY_WAR_BASE));
            config.setServerHome(props.getProperty(KEY_SERVER_HOME));
        }
        return config;
    }
}
