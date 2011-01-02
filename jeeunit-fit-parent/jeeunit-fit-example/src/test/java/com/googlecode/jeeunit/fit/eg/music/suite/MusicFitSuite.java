package com.googlecode.jeeunit.fit.eg.music.suite;

import org.junit.runner.RunWith;

import com.googlecode.jeeunit.fit.junit.FitConfiguration;
import com.googlecode.jeeunit.fit.junit.FitSuite;

/**
 * An example showing how to run a suite of FIT tests under JUnit.
 * @author hwellmann
 *
 */
@RunWith(FitSuite.class)
@FitConfiguration(inputDir = "src/test/fit", outputDir = "target/fit")
public class MusicFitSuite {

}
