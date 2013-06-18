package net.meisen.ant.test.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import net.meisen.general.genmisc.types.Files;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.junit.After;
import org.junit.Before;

/**
 * Helper method which enables us to load the "build.xml" from the classpath for
 * a test.
 * 
 * @author pmeisen
 * 
 */
public abstract class ClassPathBuildFileTest extends BuildFileTest {

	private String testTmpDir;

	@Before
	@Override
	public void setUp() {

		// generate a temporary directory for the test
		final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		final File testTmpDir = new File(tmpDir, UUID.randomUUID().toString());

		// create the directory
		testTmpDir.mkdirs();
		this.testTmpDir = Files.getCanonicalPath(testTmpDir);
	}

	@Override
	public void configureProject(final String filename, final int logLevel) {
		File file;

		// create the file
		try {
			file = copyProjectFile(filename);
		} catch (final IOException e) {
			System.err.println("Could not find the file: '" + filename + "' ('"
					+ e.getMessage() + "')");
			e.printStackTrace();

			// we just try to get it anyways
			file = new File(filename);
		}

		super.configureProject(Files.getCanonicalPath(file), logLevel);
	}

	/**
	 * Copies the specified file into the project directory. This is mainly used
	 * when the used build.xml has imports.
	 * 
	 * @param filename
	 *          the file to be copied from classpath (relative to
	 *          <code>this</code>)
	 * 
	 * @return the new location of the file
	 * 
	 * @throws IOException
	 *           if the file cannot be copied
	 */
	public File copyProjectFile(final String filename) throws IOException {
		// copy the buildFile to that directory
		final InputStream stream = getClass().getResourceAsStream(filename);

		// create the file
		final File inputFile = new File(filename);
		final File file = new File(this.testTmpDir, inputFile.getName());
		Files.copyStreamToFile(stream, file);

		return file;
	}

	/**
	 * Gets the main target of the current project.
	 * 
	 * @return the main target of the current project
	 */
	protected Target getMainTarget() {
		return getTarget("");
	}

	/**
	 * Gets the specified target for the current project, fails if the target
	 * doesn't exist.
	 * 
	 * @param name
	 *          the name of the target to be returned from the current project
	 * 
	 * @return the target with the specified <code>name</code>
	 */
	protected Target getTarget(final String name) {
		final Target target = (Target) getProject().getTargets().get(name);
		assertNotNull(target);

		return target;

	}

	/**
	 * Searches the specified task (the first occurence) within the specified
	 * target.
	 * 
	 * @param target
	 *          the target to search for the task for
	 * @param clazz
	 *          the type of the task to be returned (if several are found the
	 *          first one is returned)
	 * 
	 * @return the found task
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Task> T findTask(final Target target,
			final Class<T> clazz) {
		for (final Task task : target.getTasks()) {

			if (UnknownElement.class.equals(task.getClass())) {
				final UnknownElement unknownTask = (UnknownElement) task;

				// make sure we have the real thing
				unknownTask.maybeConfigure();

				// check it
				if (clazz.equals(unknownTask.getRealThing().getClass())) {
					return (T) unknownTask.getRealThing();
				}
			} else if (clazz.equals(task.getClass())) {
				return (T) task;
			}
		}

		return null;
	}

	@After
	@Override
	public void tearDown() {

		final File file = new File(testTmpDir);
		
		// delete the directory
		if (file.exists()) {
			assertTrue("Could not delete '" + testTmpDir + "'", Files.deleteDir(file));
		}
	}

	/**
	 * Gets the used temporary test directory for the test.
	 * 
	 * @return the used temporary test directory
	 */
	protected File getTmpDir() {
		return new File(testTmpDir);
	}
}
