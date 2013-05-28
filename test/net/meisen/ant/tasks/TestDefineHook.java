package net.meisen.ant.tasks;

import java.io.IOException;

import net.meisen.ant.test.util.ClassPathBuildFileTest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
import org.junit.Test;

/**
 * The test will run as jUnit 3 test, because of {@link BuildFileTest}. So all
 * jUnit 4 annotations are ignored. We use them anyways maybe it will be
 * possible one day.
 * 
 * @author pmeisen
 * 
 */
public class TestDefineHook extends ClassPathBuildFileTest {

	/**
	 * Tests the simple definition of a hook.
	 */
	@Test
	public void testSimplyDefinedHook() {

		// initialize Ant
		configureProject("build/defineHook/simplyDefinedHooks.xml");

		final Project project = getProject();

		// create a task and check if we have the references correctly
		final DefineHook defineHook = (DefineHook) project.createTask("defineHook");
		defineHook.isDefinedHook("SimpleHook1");
		defineHook.isDefinedHook("SimpleHook2");
	}

	/**
	 * Tests the definition of imported hooks.
	 * 
	 * @throws IOException
	 *           if the build.xml cannot be found or copied
	 */
	@Test
	public void testImportedDefinedHook() throws IOException {

		// initialize Ant
		copyProjectFile("build/defineHook/importWithDefinedHooks.xml");
		configureProject("build/defineHook/importedDefinedHooks.xml");

		final Project project = getProject();

		// create a task and check if we have the references correctly
		final DefineHook defineHook = (DefineHook) project.createTask("defineHook");
		assertTrue(defineHook.isDefinedHook("NotImportedHook1"));
		assertTrue(defineHook.isDefinedHook("NotImportedHook2"));
		assertTrue(defineHook.isDefinedHook("ImportHook1"));
		assertTrue(defineHook.isDefinedHook("ImportHook2"));

		// other shouldn't be defined
		assertFalse(defineHook.isDefinedHook("SimpleHook1"));
		assertFalse(defineHook.isDefinedHook("SimpleHook2"));
	}

	/**
	 * Tests the exception which should be thrown if an invalid duplicate hook is
	 * defined.
	 */
	@Test
	public void testInvalidDuplicate() {
		try {
			configureProject("build/defineHook/invalidDuplicateHookDefinition.xml");
			fail("No exception was thrown.");
		} catch (final BuildException e) {
			assertEquals(
					"There are several hooks with the name 'invalidduplicatehookdefinition' defined.",
					e.getMessage());
		}
	}

	/**
	 * Tests the definition of a hook with a namespace.
	 */
	@Test
	public void testSimplyNamespaceDefinedHooks() {

		// initialize Ant
		configureProject("build/defineHook/simplyNamespaceDefinedHooks.xml");

		final Project project = getProject();

		// create a task and check if we have the references correctly
		final DefineHook defineHook = (DefineHook) project.createTask("defineHook");
		assertTrue(defineHook.isDefinedHook("simplyNamespace", "SimpleHook1"));
		assertTrue(defineHook.isDefinedHook("simplyNamespace", "SimpleHook2"));

		// other shouldn't be defined
		assertFalse(defineHook.isDefinedHook("SimpleHook1"));
		assertFalse(defineHook.isDefinedHook("SimpleHook2"));
	}

	/**
	 * Tests the definition of a hook with attributes.
	 */
	@Test
	public void testDefinedHooksWithAttributes() {

		// initialize Ant
		configureProject("build/defineHook/definedHooksWithAttributes.xml");

		// get the main-target and the defined hook
		final DefineHook task = findTask(getMainTarget(), DefineHook.class);
		assertNotNull(task);

		// we should have two attributes
		assertEquals(task.getAttributes().size(), 2);
		assertEquals(task.getAttributes().get(0).getName(), "test1");
		assertEquals(task.getAttributes().get(0).getDefault(), null);
		assertEquals(task.getAttributes().get(1).getName(), "test2");
		assertEquals(task.getAttributes().get(1).getDefault(), "true");
	}

}
