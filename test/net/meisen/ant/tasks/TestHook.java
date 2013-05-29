package net.meisen.ant.tasks;

import net.meisen.ant.test.util.ClassPathBuildFileTest;

import org.apache.tools.ant.BuildFileTest;
import org.junit.Test;

/**
 * The test will run as jUnit 3 test, because of {@link BuildFileTest}. So all
 * jUnit 4 annotations are ignored. We use them anyways maybe it will be
 * possible one day.
 * 
 * @author pmeisen
 * 
 */
public class TestHook extends ClassPathBuildFileTest {

	/**
	 * Tests the parsing of a simple hook definition
	 */
	@Test
	public void testSimpleHook() {

		// initialize Ant
		configureProject("build/hook/simpleHook.xml");

		final Hook hook = findTask(getMainTarget(), Hook.class);
		assertNotNull(hook);
		assertEquals(hook.getName(), "simplehook");
	}

	/**
	 * Tests the parsing of a prioritized hook definition
	 */
	@Test
	public void testPrioritizedHook() {

		// initialize Ant
		configureProject("build/hook/prioritizedHook.xml");

		final Hook hook = findTask(getMainTarget(), Hook.class);
		assertNotNull(hook);
		assertEquals(hook.getName(), "prioritizedhook");
		assertEquals(hook.getPriority(), 5);
	}
}
