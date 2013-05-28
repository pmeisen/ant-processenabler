package net.meisen.ant.tasks.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the implementation of a <code>HookKey</code>.
 * 
 * @author pmeisen
 * 
 */
public class TestHookKey {

	/**
	 * Tests the equality of two <code>HookKey</code> instances.
	 */
	@Test
	public void testEquality() {
		final HookKey key = new HookKey("A", "B");

		// identity equality
		assertTrue(key.equals(key));

		// not equal to null
		assertFalse(key.equals(null));

		// create an equal key
		final HookKey keyEqual = new HookKey("A", "B");
		assertTrue(key.equals(keyEqual));
		assertTrue(keyEqual.equals(key));

		// now create several not equal one
		final HookKey keyUnequal = new HookKey("E", "F");
		assertFalse(key.equals(keyUnequal));
		assertFalse(keyUnequal.equals(key));
		final HookKey keyUnequalName = new HookKey("A", "C");
		assertFalse(key.equals(keyUnequalName));
		assertFalse(keyUnequalName.equals(key));
		final HookKey keyUnequalNamespace = new HookKey("D", "B");
		assertFalse(key.equals(keyUnequalNamespace));
		assertFalse(keyUnequalNamespace.equals(key));

		// test some null equalities with namespaces
		final HookKey nullKey = new HookKey(null, "A");
		assertTrue(nullKey.equals(nullKey));
		assertFalse(key.equals(nullKey));
		assertFalse(nullKey.equals(key));

		final HookKey secNullKey = new HookKey(null, "A");
		assertTrue(secNullKey.equals(nullKey));
		assertTrue(nullKey.equals(secNullKey));

		final HookKey thirdNullKey = new HookKey(null, "B");
		assertFalse(secNullKey.equals(thirdNullKey));
		assertFalse(thirdNullKey.equals(secNullKey));
	}
}
