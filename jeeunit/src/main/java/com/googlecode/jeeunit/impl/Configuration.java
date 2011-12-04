package com.googlecode.jeeunit.impl;

public class Configuration {

    private int httpPort = Integer.parseInt(Constants.HTTP_PORT_DEFAULT);
    private boolean enableWeldListener;
    private String warBase;

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

}
