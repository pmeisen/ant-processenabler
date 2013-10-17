package net.meisen.ant.xmlmatcher;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.meisen.ant.test.util.ClassPathBuildFileTest;
import net.meisen.general.genmisc.types.Files;

import org.junit.Test;

/**
 * Tests the implementation of the <code>FullDependencyMatcher</code>.
 * 
 * @author pmeisen
 * 
 */
public class TestFullDependencyMatcher extends ClassPathBuildFileTest {

	/**
	 * Tests the simple full dependency matching
	 * 
	 * @throws IOException
	 *             if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testSimple() throws IOException {

		// copy the needed files
		copyProjectFile("build/fullDependencyMatcher/simpleXml.xml");
		copyProjectFile("build/fullDependencyMatcher/simpleXmlMerge.xml");
		copyProjectFile("build/fullDependencyMatcher/simpleXmlMerge.properties");

		// initialize Ant
		configureProject("build/fullDependencyMatcher/simpleTest.xml");

		// execute the test-target
		executeTarget("test");

		// write the log to the test's log
		System.out.println(this.getOutput());
		System.out.println(this.getError());

		// get the output
		final File output = new File(getTmpDir(), "tmpMerged.xml");

		// check if the file is created
		assertTrue(output.exists());
		assertTrue(output.isFile());

		// get the expected regular expression
		String expected = "";
		expected += "^";
		expected += "\\s*\\Q<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\E";
		expected += "\\s*\\Q<project>\\E";
		expected += ".*";
		expected += "\\s*\\Q<dependencies>\\E";
		expected += "\\s*\\Q<dependency>\\E";
		expected += "\\s*\\Q<groupId>com.extjs.GWT_2_3_0</groupId>\\E";
		expected += "\\s*\\Q<artifactId>gxt</artifactId>\\E";
		expected += "\\s*\\Q<version>2.2.5</version>\\E";
		expected += "\\s*\\Q<scope>compile</scope>\\E";
		expected += "\\s*\\Q<exclusions />\\E";
		expected += "\\s*\\Q</dependency>\\E";
		expected += "\\s*\\Q<dependency>\\E";
		expected += "\\s*\\Q<groupId>com.google.gwt</groupId>\\E";
		expected += "\\s*\\Q<artifactId>gwt-servlet</artifactId>\\E";
		expected += "\\s*\\Q<version>2.3.0</version>\\E";
		expected += "\\s*\\Q<scope>compile</scope>\\E";
		expected += "\\s*\\Q</dependency>\\E";
		expected += "\\s*\\Q</dependencies>\\E";
		expected += "\\s*\\Q</project>\\E";
		expected += "\\s*$";
		final Pattern p = Pattern.compile(expected, Pattern.MULTILINE
				| Pattern.DOTALL);

		final String result = Files.readFromFile(new File(getTmpDir(),
				"tmpMerged.xml"));

		final Matcher m = p.matcher(result);
		assertTrue(result, m.matches());
	}

	/**
	 * Tests the merging with equal, but differently written types.
	 * 
	 * @throws IOException
	 *             if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testType() throws IOException {

		// copy the needed files
		copyProjectFile("build/fullDependencyMatcher/typeXml.xml");
		copyProjectFile("build/fullDependencyMatcher/typeXmlMerge.xml");
		copyProjectFile("build/fullDependencyMatcher/typeXmlMerge.properties");

		// initialize Ant
		configureProject("build/fullDependencyMatcher/typeTest.xml");

		// execute the test-target
		executeTarget("test");

		// write the log to the test's log
		System.out.println(this.getOutput());
		System.out.println(this.getError());

		// get the output
		final File output = new File(getTmpDir(), "tmpMerged.xml");

		// check if the file is created
		assertTrue(output.exists());
		assertTrue(output.isFile());

		// get the expected regular expression
		String expected = "";
		expected += "^";
		expected += "\\s*\\Q<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\E";
		expected += "\\s*\\Q<project>\\E";
		expected += ".*";
		expected += "\\s*\\Q<dependencies>\\E";
		expected += "\\s*\\Q<dependency>\\E";
		expected += "\\s*\\Q<groupId>test</groupId>\\E";
		expected += "\\s*\\Q<artifactId>sample1</artifactId>\\E";
		expected += "\\s*\\Q<version>1.0.0</version>\\E";
		expected += "\\s*\\Q<type>jar</type>\\E";
		expected += "\\s*\\Q<classifier />\\E";
		expected += "\\s*\\Q<exclusions />\\E";
		expected += "\\s*\\Q</dependency>\\E";
		expected += "\\s*\\Q</dependencies>\\E";
		expected += "\\s*\\Q</project>\\E";
		expected += "\\s*$";
		final Pattern p = Pattern.compile(expected, Pattern.MULTILINE
				| Pattern.DOTALL);

		final String result = Files.readFromFile(new File(getTmpDir(),
				"tmpMerged.xml"));

		final Matcher m = p.matcher(result);
		assertTrue(result, m.matches());
	}

	/**
	 * Tests the merging of eclipse dependencies used by the processenabler
	 * 
	 * @throws IOException
	 *             if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testEclipse() throws IOException {
		// copy the needed files
		copyProjectFile("build/fullDependencyMatcher/eclipsePom.xml");
		copyProjectFile("build/fullDependencyMatcher/eclipsePomMerge.xml");
		copyProjectFile("build/fullDependencyMatcher/eclipseXmlMerge.properties");

		// initialize Ant
		configureProject("build/fullDependencyMatcher/eclipseTest.xml");

		// execute the test-target
		executeTarget("test");

		// write the log to the test's log
		System.out.println(this.getOutput());
		System.out.println(this.getError());

		// get the output
		final File output = new File(getTmpDir(), "tmpEclipse.xml");

		// check if the file is created
		assertTrue(output.exists());
		assertTrue(output.isFile());

		// get the expected regular expression
		String expected = "";
		expected += "^";
		expected += "\\s*\\Q<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\E";
		expected += "\\s*\\Q<project>\\E";
		expected += ".*";
		expected += "\\s*\\Q<dependencies>\\E";
		expected += "\\s*\\Q<dependency>\\E";
		expected += "\\s*\\Q<groupId>net.meisen.general</groupId>\\E";
		expected += "\\s*\\Q<artifactId>net-meisen-general-gen-dummy</artifactId>\\E";
		expected += "\\s*\\Q<version>TRUNK-SNAPSHOT</version>\\E";
		expected += "\\s*\\Q</dependency>\\E";
		expected += "\\s*\\Q<dependency>\\E";
		expected += "\\s*\\Q<groupId>net.meisen.web</groupId>\\E";
		expected += "\\s*\\Q<artifactId>net-meisen-web-cbwaf-share</artifactId>\\E";
		expected += "\\s*\\Q<version>TRUNK-SNAPSHOT</version>\\E";
		expected += "\\s*\\Q</dependency>\\E";
		expected += "\\s*\\Q<dependency>\\E";
		expected += "\\s*\\Q<groupId>net.meisen.web</groupId>\\E";
		expected += "\\s*\\Q<artifactId>net-meisen-web-cbwaf-api</artifactId>\\E";
		expected += "\\s*\\Q<version>TRUNK-SNAPSHOT</version>\\E";
		expected += "\\s*\\Q<classifier>${maven.attachment.fullcompiled}</classifier>\\E";
		expected += "\\s*\\Q</dependency>\\E";
		expected += "\\s*\\Q</dependencies>\\E";
		expected += "\\s*\\Q</project>\\E";
		expected += "\\s*$";
		final Pattern p = Pattern.compile(expected, Pattern.MULTILINE
				| Pattern.DOTALL);

		final String result = Files.readFromFile(new File(getTmpDir(),
				"tmpEclipse.xml"));

		final Matcher m = p.matcher(result);
		assertTrue(result, m.matches());
	}
}
