package com.googlecode.jeeunit.fit.eg.music.weld.suite;

import org.junit.runner.RunWith;

import com.googlecode.jeeunit.fit.junit.FitConfiguration;
import com.googlecode.jeeunit.fit.junit.cdi.CdiFitSuite;

@RunWith(CdiFitSuite.class)
@FitConfiguration(inputDir = "src/test/fit", outputDir = "target/fit")
public class MusicWeldFitSuite {

}
