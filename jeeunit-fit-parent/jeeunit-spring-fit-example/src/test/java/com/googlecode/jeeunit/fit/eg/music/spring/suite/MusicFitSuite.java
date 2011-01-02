package com.googlecode.jeeunit.fit.eg.music.spring.suite;

import org.junit.runner.RunWith;

import com.googlecode.jeeunit.fit.junit.FitConfiguration;
import com.googlecode.jeeunit.fit.junit.spring.SpringFitSuite;

/**
 * An example showing how to run a Spring-enabled suite of FIT tests under JUnit.
 * @author hwellmann
 *
 */
@RunWith(SpringFitSuite.class)
@FitConfiguration(inputDir = "src/test/fit", outputDir = "target/fit")
public class MusicFitSuite {

}
