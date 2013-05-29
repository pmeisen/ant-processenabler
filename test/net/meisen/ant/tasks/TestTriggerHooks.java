package net.meisen.ant.tasks;

import net.meisen.ant.test.util.ClassPathBuildFileTest;
import net.meisen.general.genmisc.collections.Collections;

import org.apache.tools.ant.BuildException;
import org.junit.Test;

/**
 * Tests the implementation of the <code>TriggerHook</code>.
 * 
 * @author pmeisen
 * 
 */
public class TestTriggerHooks extends ClassPathBuildFileTest {

	/**
	 * Tests an invalid definition of a <code>TriggerHook</code> concerning the
	 * defined attributes.
	 */
	@Test
	public void testInvalidAttributesForTriggerHooks() {

		// initialize Ant
		configureProject("build/triggerHooks/invalidAttributesForTriggerHooks.xml");

		// execute the target
		try {
			executeTarget("testTarget");
			fail("No exception was thrown.");
		} catch (final BuildException e) {
			assertEquals(
					"The attribute values ('testattribute2') do not match the defined attributes ('testattribute1, testattribute2 (default: true)').",
					e.getMessage());
		}
	}

	/**
	 * Tests an invalid definition of a <code>TriggerHook</code> concerning the
	 * position (i.e. defined on top-level).
	 */
	@Test
	public void testInvalidPositionOfTriggerHooks() {

		// execute the target
		try {
			configureProject("build/triggerHooks/invalidPositionOfTriggerHooks.xml");
			fail("No exception was thrown.");
		} catch (final BuildException e) {
			assertEquals(
					"A trigger cannot be defined on top-level, i.e. you can only use triggers within a target.",
					e.getMessage());
		}
	}

	/**
	 * Tests the simple parsing and execution of a <code>TriggerHook</code>.
	 */
	@Test
	public void testSimpleTrigger() {
		// initialize Ant
		configureProject("build/triggerHooks/simpleTriggerHooks.xml");

		final TriggerHooks triggerHooks = findTask(getTarget("testTarget"),
				TriggerHooks.class);
		assertNotNull(triggerHooks.getAttributeValues());
		assertEquals(triggerHooks.getAttributeValues().size(), 1);
		assertEquals(Collections.get(0, triggerHooks.getAttributeValues())
				.getName(), "testattribute1");

		// execute the target
		executeTarget("testTarget");

		// the trigger should have been fired
		assertEquals(getLog(), "Called SimpleHook");
	}

	/**
	 * Tests a more complex definition of a <code>TriggerHook</code>.
	 */
	@Test
	public void testComplexTrigger() {
		// initialize Ant
		configureProject("build/triggerHooks/complexTriggerHooks.xml");

		final TriggerHooks triggerHooks = findTask(getTarget("testTarget"),
				TriggerHooks.class);
		assertNotNull(triggerHooks.getAttributeValues());
		assertEquals(triggerHooks.getAttributeValues().size(), 1);
		assertEquals(Collections.get(0, triggerHooks.getAttributeValues())
				.getName(), "testattribute1");

		// execute the target
		executeTarget("testTarget");

		// the trigger should have been fired
		assertTrue(getLog().endsWith(
				"testTarget,firstComplexHook,secondComplexHook,thirdComplexHook"));
	}

	/**
	 * Tests a definition using prioritized hooks.
	 */
	@Test
	public void testPrioritizedHooks() {
		// initialize Ant
		configureProject("build/triggerHooks/prioritizedHooks.xml");

		// execute the target
		executeTarget("testTarget");

		// the trigger should have been fired
		assertEquals(getLog(),
				"testTarget,thirdPriorityHook,secondPriorityHook,firstPriorityHook");
	}
}
