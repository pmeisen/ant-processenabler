package net.meisen.ant;

import net.meisen.ant.tasks.TestDefineHook;
import net.meisen.ant.tasks.TestHook;
import net.meisen.ant.tasks.TestTriggerHooks;
import net.meisen.ant.tasks.types.TestHookKey;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The tests to check the ant-implementations.
 * 
 * @author pmeisen
 * 
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TestHookKey.class, TestDefineHook.class, TestHook.class,
		TestTriggerHooks.class })
public class AllTests {
	// nothing to be defined here
}