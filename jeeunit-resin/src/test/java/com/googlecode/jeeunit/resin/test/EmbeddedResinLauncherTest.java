package com.googlecode.jeeunit.resin.test;

import org.junit.Test;

import com.googlecode.jeeunit.ContainerLauncherLookup;
import com.googlecode.jeeunit.spi.ContainerLauncher;


public class EmbeddedResinLauncherTest {

    @Test
    public void launchResin() {
        ContainerLauncher launcher = ContainerLauncherLookup.getContainerLauncher();
        launcher.launch();
        launcher.autodeploy();
    }
}
