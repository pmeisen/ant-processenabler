package net.meisen.ant.xmlmatcher;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.meisen.ant.test.util.ClassPathBuildFileTest;
import net.meisen.general.genmisc.types.Files;

import org.junit.Test;

/**
 * Tests the implementation of the <code>InsertFirstAction</code>.
 * 
 * @see InsertFirstAction
 * 
 * @author pmeisen
 * 
 */
public class TestInsertFirstAction extends ClassPathBuildFileTest {

	/**
	 * Tests the simple insertion
	 * 
	 * @throws IOException
	 *           if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testSimple() throws IOException {

		// copy the needed files
		copyProjectFile("build/insertFirstAction/simpleXml.xml");
		copyProjectFile("build/insertFirstAction/simpleXmlMerge.xml");
		copyProjectFile("build/insertFirstAction/simpleXmlMerge.properties");

		// initialize Ant
		configureProject("build/insertFirstAction/simpleTest.xml");

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
		expected += "\\s*\\Q<node value=\"BEFOREDEFAULT\" />\\E";
		expected += "\\s*\\Q<node value=\"DEFAULT\" />\\E";
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
	 * Tests the first insertion of an element on an empty XML document.
	 * 
	 * @throws IOException
	 *           if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testEmpty() throws IOException {

		// copy the needed files
		copyProjectFile("build/insertFirstAction/emptyXml.xml");
		copyProjectFile("build/insertFirstAction/emptyXmlMerge.xml");
		copyProjectFile("build/insertFirstAction/emptyXmlMerge.properties");

		// initialize Ant
		configureProject("build/insertFirstAction/emptyTest.xml");

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
		expected += "\\s*\\Q<node value=\"FIRST\" />\\E";
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
	 * Tests the first insertion of an element for eclipse project files.
	 * 
	 * @throws IOException
	 *           if one of the files needed by the build cannot be copied
	 */
	@Test
	public void testEclipse() throws IOException {

		// copy the needed files
		copyProjectFile("build/insertFirstAction/eclipseXml.xml");
		copyProjectFile("build/insertFirstAction/eclipseXmlMerge.xml");
		copyProjectFile("build/insertFirstAction/eclipseXmlMerge.properties");

		// initialize Ant
		configureProject("build/insertFirstAction/eclipseTest.xml");

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
		expected += "\\s*\\Q<projectDescription>\\E";
		expected += "\\s*\\Q<name>test</name>\\E";
		expected += "\\s*\\Q<comment />\\E";
		expected += "\\s*\\Q<projects />\\E";
		expected += "\\s*\\Q<buildSpec>\\E";
		expected += "\\s*\\Q<buildCommand>\\E";
		expected += "\\s*\\Q<name>org.eclipse.ajdt.core.ajbuilder</name>\\E";
		expected += "\\s*\\Q<arguments />\\E";
		expected += "\\s*\\Q</buildCommand>\\E";
		expected += "\\s*\\Q</buildSpec>\\E";
		expected += "\\s*\\Q<natures>\\E";
		expected += "\\s*\\Q<nature>org.eclipse.ajdt.ui.ajnature</nature>\\E";
		expected += "\\s*\\Q<nature>org.eclipse.jdt.core.javanature</nature>\\E";
		expected += "\\s*\\Q</natures>\\E";
		expected += "\\s*\\Q</projectDescription>\\E";
		expected += "\\s*$";
		final Pattern p = Pattern.compile(expected, Pattern.MULTILINE
				| Pattern.DOTALL);

		final String result = Files.readFromFile(new File(getTmpDir(),
				"tmpMerged.xml"));
		final Matcher m = p.matcher(result);

		assertTrue(result, m.matches());
	}
}
