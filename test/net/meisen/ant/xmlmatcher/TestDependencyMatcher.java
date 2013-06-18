package net.meisen.ant.xmlmatcher;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.meisen.ant.test.util.ClassPathBuildFileTest;
import net.meisen.general.genmisc.types.Files;

import org.junit.Test;

/**
 * Tests the implementation of the <code>DependencyMatcher</code>.
 * 
 * @author pmeisen
 * 
 */
public class TestDependencyMatcher extends ClassPathBuildFileTest {

	/**
	 * Tests the simple dependency matching
	 * 
	 * @throws IOException
	 *           if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testSimple() throws IOException {

		// copy the needed files
		copyProjectFile("build/dependencyMatcher/simpleXml.xml");
		copyProjectFile("build/dependencyMatcher/simpleXmlMerge.xml");
		copyProjectFile("build/dependencyMatcher/simpleXmlMerge.properties");

		// initialize Ant
		configureProject("build/dependencyMatcher/simpleTest.xml");

		// execute the test-target
		executeTarget("test");

		// write the log to the test's log
		System.out.println(this.getOutput());

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
		expected += "\\s*\\Q<classifier>${maven.attachment.webappresources}</classifier>\\E";
		expected += "\\s*\\Q</dependency>\\E";
		expected += "\\s*\\Q<dependency>\\E";
		expected += "\\s*\\Q<groupId>com.google.gwt</groupId>\\E";
		expected += "\\s*\\Q<artifactId>gwt-servlet</artifactId>\\E";
		expected += "\\s*\\Q<version>2.3.0</version>\\E";
		expected += "\\s*\\Q<scope>compile</scope>\\E";
		expected += "\\s*\\Q</dependency>\\E";
		expected += "\\s*\\Q<dependency>\\E";
		expected += "\\s*\\Q<groupId>another.gwt.plugin</groupId>\\E";
		expected += "\\s*\\Q<artifactId>puggy</artifactId>\\E";
		expected += "\\s*\\Q<version>1.0.0</version>\\E";
		expected += "\\s*\\Q<classifier>${maven.attachment.webappresources}</classifier>\\E";
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
	 * Tests the logging of an error message.
	 * 
	 * @throws IOException
	 *           if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testExpectedException() throws IOException {

		// copy the needed files
		copyProjectFile("build/dependencyMatcher/expectVersionExceptionXml.xml");
		copyProjectFile("build/dependencyMatcher/expectVersionExceptionXmlMerge.xml");
		copyProjectFile("build/dependencyMatcher/expectVersionExceptionXmlMerge.properties");

		// initialize Ant
		configureProject("build/dependencyMatcher/expectVersionExceptionTest.xml");

		// execute the test-target
		executeTarget("test");

		// check the errors
		assertEquals(
				getError().trim(),
				"The version of a matched dependency does not fit '2.2.5' vs. '3.2.4', please match the versions.");
	}
}
