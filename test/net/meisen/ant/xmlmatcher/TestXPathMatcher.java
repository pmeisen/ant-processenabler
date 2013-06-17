package net.meisen.ant.xmlmatcher;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import net.meisen.ant.test.util.ClassPathBuildFileTest;
import net.meisen.general.genmisc.types.Files;

/**
 * Tests the implementation of the <code>XPathMatcher</code>.
 * 
 * @author pmeisen
 * 
 */
public class TestXPathMatcher extends ClassPathBuildFileTest {

	/**
	 * Tests the simple matching
	 * 
	 * @throws IOException
	 *           if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testSimple() throws IOException {

		// copy the needed files
		copyProjectFile("build/xPathMatcher/simpleXml.xml");
		copyProjectFile("build/xPathMatcher/simpleXmlMerge.xml");
		copyProjectFile("build/xPathMatcher/simpleXmlMerge.properties");

		// initialize Ant
		configureProject("build/xPathMatcher/simpleTest.xml");

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
		expected += "\\s*\\Q<root>\\E";
		expected += "\\s*\\Q<node attribute=\"something\" />\\E";
		expected += "\\s*\\Q<node attribute=\"cake\">\\E";
		expected += "\\s*\\Q<cake name=\"chocolate\" />\\E";
		expected += "\\s*\\Q<cake name=\"applepie\" />\\E";
		expected += "\\s*\\Q<cake name=\"blackforestcake\" />\\E";
		expected += "\\s*\\Q</node>\\E";
		expected += "\\s*\\Q</root>\\E";
		expected += "\\s*$";
		final Pattern p = Pattern.compile(expected, Pattern.MULTILINE
				| Pattern.DOTALL);

		final String result = Files.readFromFile(new File(getTmpDir(),
				"tmpMerged.xml"));
		final Matcher m = p.matcher(result);

		assertTrue(result, m.matches());
	}

	/**
	 * Tests the multiple matching
	 * 
	 * @throws IOException
	 *           if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testMultiple() throws IOException {

		// copy the needed files
		copyProjectFile("build/xPathMatcher/multipleXml.xml");
		copyProjectFile("build/xPathMatcher/multipleXmlMerge.xml");
		copyProjectFile("build/xPathMatcher/multipleXmlMerge.properties");

		// initialize Ant
		configureProject("build/xPathMatcher/multipleTest.xml");

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
		expected += "\\s*\\Q<root>\\E";
		expected += "\\s*\\Q<node attribute=\"something\" />\\E";
		expected += "\\s*\\Q<node attribute=\"cake\">\\E";
		expected += "\\s*\\Q<cake name=\"chocolate\" />\\E";
		expected += "\\s*\\Q<cake name=\"applepie\" />\\E";
		expected += "\\s*\\Q<cake name=\"blackforestcake\" />\\E";
		expected += "\\s*\\Q</node>\\E";
		expected += "\\s*\\Q<node attribute=\"car\">\\E";
		expected += "\\s*\\Q<cake name=\"audi\" />\\E";
		expected += "\\s*\\Q<cake name=\"ford\" />\\E";
		expected += "\\s*\\Q<cake name=\"toyota\" />\\E";
		expected += "\\s*\\Q</node>\\E";
		expected += "\\s*\\Q</root>\\E";
		expected += "\\s*$";
		final Pattern p = Pattern.compile(expected, Pattern.MULTILINE
				| Pattern.DOTALL);

		final String result = Files.readFromFile(new File(getTmpDir(),
				"tmpMerged.xml"));
		final Matcher m = p.matcher(result);

		assertTrue(result, m.matches());
	}
	
	/**
	 * Tests a deeper nested matching
	 * 
	 * @throws IOException
	 *           if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testDeeperNested() throws IOException {

		// copy the needed files
		copyProjectFile("build/xPathMatcher/deeperNestedXml.xml");
		copyProjectFile("build/xPathMatcher/deeperNestedXmlMerge.xml");
		copyProjectFile("build/xPathMatcher/deeperNestedXmlMerge.properties");

		// initialize Ant
		configureProject("build/xPathMatcher/deeperNestedTest.xml");

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
		expected += "\\s*\\Q<root>\\E";
		expected += "\\s*\\Q<node attribute=\"something\" />\\E";
		expected += "\\s*\\Q<node attribute=\"cake\">\\E";
		expected += "\\s*\\Q<subnode attribute=\"notme\">\\E";
		expected += "\\s*\\Q<cake name=\"another\" />\\E";
		expected += "\\s*\\Q</subnode>\\E";
		expected += "\\s*\\Q<subnode attribute=\"deeper\">\\E";
		expected += "\\s*\\Q<cake name=\"chocolate\" />\\E";
		expected += "\\s*\\Q<cake name=\"applepie\" />\\E";
		expected += "\\s*\\Q<cake name=\"blackforestcake\" />\\E";
		expected += "\\s*\\Q</subnode>\\E";
		expected += "\\s*\\Q</node>\\E";
		expected += "\\s*\\Q</root>\\E";
		expected += "\\s*$";
		final Pattern p = Pattern.compile(expected, Pattern.MULTILINE
				| Pattern.DOTALL);

		final String result = Files.readFromFile(new File(getTmpDir(),
				"tmpMerged.xml"));
		final Matcher m = p.matcher(result);

		assertTrue(result, m.matches());
	}
}
