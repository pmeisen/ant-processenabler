package net.meisen.ant.tasks.types;

import java.io.Serializable;
import java.util.Locale;

/**
 * A key used to identify a hook.
 * 
 * @author pmeisen
 * 
 */
public class HookKey implements Serializable {
	private static final long serialVersionUID = 1271670032844449820L;

	private final String namespace;
	private final String name;

	/**
	 * Constructor using no namespace and just a name.
	 * 
	 * @param name
	 *          the name of the hook
	 */
	public HookKey(final String name) {
		this(null, name);
	}

	/**
	 * Constructor which specifies the namespace and the name of the hook.
	 * 
	 * @param namespace
	 *          the namespace of the hook
	 * @param name
	 *          the name of the hook
	 */
	public HookKey(final String namespace, final String name) {
		if (!isValidName(name)) {
			throw new IllegalArgumentException("The name cannot be empty.");
		}

		this.namespace = namespace;
		this.name = HookKey.normalizeName(name);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		} else if (super.equals(o)) {
			return true;
		} else if (o instanceof HookKey) {
			final HookKey key = (HookKey) o;

			return key.name.equals(name)
					&& ((key.namespace == null && namespace == null) || key.namespace
							.equals(namespace));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new String(namespace + ":" + name).hashCode();
	}

	/**
	 * Check if a string is a valid name for a <code>HookKey</code>.
	 * 
	 * @param name
	 *          the string to check
	 * @return <code>true</code> if the name consists of valid name characters,
	 *         otherwise <code>false</code>
	 */
	public static boolean isValidName(final String name) {
		return (name != null && name.length() > 0);
	}

	public static String normalizeName(final String name) {
		return name.toLowerCase(Locale.ENGLISH);
	}
}
