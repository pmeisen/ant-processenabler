package net.meisen.ant.xmlmatcher;

import org.jdom.Element;

import ch.elca.el4j.services.xmlmerge.Matcher;

/**
 * Checks if two maven dependency match
 * 
 * @author pmeisen
 * 
 */
public class DependencyMatcher implements Matcher {

	/**
	 * The tag used to identify dependencies
	 */
	public final static String DEPENDENCIESTAG = "dependencies";
	/**
	 * The groupId identifier in maven
	 */
	public final static String ATTRIB_GROUPID = "groupId";
	/**
	 * The artifactId identifier in maven
	 */
	public final static String ATTRIB_ARTIFACT = "artifactId";
	/**
	 * The version identifier in maven
	 */
	public final static String ATTRIB_VERSION = "version";
	/**
	 * The type identifier in maven
	 */
	public final static String ATTRIB_TYPE = "type";
	/**
	 * The classifier identifier in maven
	 */
	public final static String ATTRIB_CLASSIFIER = "classifier";

	@Override
	public boolean matches(final Element original, final Element patch) {
		final boolean returnValue;
		if (matchedArtifacts(original, patch) && matchedVersion(original, patch)) {
			returnValue = true;
		} else {
			returnValue = false;
		}

		return returnValue;
	}

	/**
	 * Checks if the specified artifacts, i.e. group and artifact match.
	 * 
	 * @param original
	 *          the original element
	 * @param patch
	 *          the patch element
	 * 
	 * @return <code>true</code> if the artifacts (i.e. group and artifact) match,
	 *         otherwise <code>false</code>
	 */
	protected boolean matchedArtifacts(final Element original, final Element patch) {

		// the groupId and the artifactId must be equal
		final String groupId = original.getChildText(ATTRIB_GROUPID);
		final String newGroupId = patch.getChildText(ATTRIB_GROUPID);
		final String artifactId = original.getChildText(ATTRIB_ARTIFACT);
		final String newArtifactId = patch.getChildText(ATTRIB_ARTIFACT);

		return equals(groupId, newGroupId) && equals(artifactId, newArtifactId);
	}

	/**
	 * Checks if the version match, i.e. if the version of the original and patch
	 * are equal (equal means also that the patch doesn't have any version at all
	 * defined).
	 * 
	 * @param original
	 *          the original element
	 * @param patch
	 *          the patch element
	 * 
	 * @return <code>true</code> if the versions are equal, otherwise
	 *         <code>false</code>
	 */
	protected boolean matchedVersion(final Element original, final Element patch) {

		// get the original and patch group
		final String version = original.getChildText(ATTRIB_VERSION);
		final String newVersion = patch.getChildText(ATTRIB_VERSION);

		if (version == null) {
			return false;
		} else if (version.equals(newVersion)) {
			return true;
		} else if (newVersion == null) {
			return true;
		} else {
			System.err.println("The version of a matched dependency does not fit '"
					+ version + "' vs. '" + newVersion + "', please match the versions.");

			return false;
		}
	}

	/**
	 * Checks if two strings are equal to eachother.
	 * 
	 * @param a
	 *          the first string
	 * @param b
	 *          the second string
	 * @param nullEqualsEmpty
	 *          <code>true</code> if an empty string is assumed to be equal to
	 *          <code>null</code>, otherwise <code>false</code>
	 * 
	 * @return <code>true</code> if the strings are equal, otherwise
	 *         <code>false</code>
	 */
	protected boolean equals(final String a, final String b,
			final boolean nullEqualsEmpty) {

		if (nullEqualsEmpty) {
			return (isEmptyOrNull(a) && isEmptyOrNull(b))
					|| (a != null && a.equals(b));
		} else {
			return (a == null && b == null) || (a != null && a.equals(b));
		}
	}

	/**
	 * Checks if the two strings are equal. Whereby the empty string isn't equal
	 * to <code>null</code>
	 * 
	 * @param a
	 *          the first string
	 * @param b
	 *          the second string
	 * 
	 * @return <code>true</code> if the strings are equal, otherwise
	 *         <code>false</code>
	 * 
	 * @see #equals(String, String, boolean)
	 */
	protected boolean equals(final String a, final String b) {
		return equals(a, b, false);
	}

	/**
	 * Checks if the passed string is empty or <code>null</code>.
	 * 
	 * @param a
	 *          the string to check
	 * @return <code>true</code> if the passed string is empty or
	 *         <code>null</code>, otherwise <code>false</code>
	 */
	protected boolean isEmptyOrNull(final String a) {
		return a == null || "".equals(a);
	}
}
