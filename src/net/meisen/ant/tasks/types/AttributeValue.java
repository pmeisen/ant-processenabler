package net.meisen.ant.tasks.types;

import java.util.Locale;

import org.apache.tools.ant.BuildException;

public class AttributeValue {
	private String name;
	private String value;

	/**
	 * The name of the attribute.
	 * 
	 * @param name
	 *          the name of the attribute
	 */
	public void setName(String name) {
		if (!isValidName(name)) {
			throw new BuildException("Illegal name [" + name + "] for an attribute");
		}

		this.name = name.toLowerCase(Locale.ENGLISH);
	}

	/**
	 * @return the name of the attribute
	 */
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Check if a string is a valid name for an <code>Attribute</code>.
	 * 
	 * @param name
	 *          the string to check
	 * @return <code>true</code> if the name consists of valid name characters,
	 *         otherwise <code>false</code>
	 */
	public static boolean isValidName(final String name) {
		return (name != null && name.length() > 0);
	}
}
