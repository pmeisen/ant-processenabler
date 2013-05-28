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

	public File copyProjectFile(final String filename) throws IOException {
		// copy the buildFile to that directory
		final InputStream stream = getClass().getResourceAsStream(filename);

		// create the file
		final File inputFile = new File(filename);
		final File file = new File(this.testTmpDir, inputFile.getName());
		Files.copyStreamToFile(stream, file);

		return file;
	}

	protected Target getMainTarget() {
		return getTarget("");
	}

	protected Target getTarget(final String name) {
		final Target target = (Target) getProject().getTargets().get(name);
		assertNotNull(target);

		return target;

	}

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

		// delete the directory
		assertTrue(Files.deleteDir(new File(this.testTmpDir)));
	}
}
