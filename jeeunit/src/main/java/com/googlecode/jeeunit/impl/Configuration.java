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

public class Configuration {

    private int httpPort = Integer.parseInt(Constants.HTTP_PORT_DEFAULT);
    private boolean enableWeldListener;
    private String warBase;
    private String serverHome;

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isEnableWeldListener() {
        return enableWeldListener;
    }

    public void setEnableWeldListener(boolean enableWeldListener) {
        this.enableWeldListener = enableWeldListener;
    }

    public String getWarBase() {
        return warBase;
    }

    public void setWarBase(String warBase) {
        this.warBase = warBase;
    }

    public String getServerHome() {
        return serverHome;
    }

    public void setServerHome(String serverHome) {
        this.serverHome = serverHome;
    }

}
