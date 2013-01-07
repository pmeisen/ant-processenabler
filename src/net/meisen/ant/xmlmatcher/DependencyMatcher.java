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

	@Override
	public boolean matches(final Element original, final Element patch) {

		// get the original and patch group
		final String groupId = original.getChildText(ATTRIB_GROUPID);
		final String newGroupId = patch.getChildText(ATTRIB_GROUPID);

		final boolean returnValue;
		if (groupId == null || newGroupId == null
				|| !groupId.trim().equals(newGroupId.trim())) {
			returnValue = false;
		} else {

			// get the original and patch artifact
			final String artifactId = original.getChildText(ATTRIB_ARTIFACT);
			final String newArtifactId = patch.getChildText(ATTRIB_ARTIFACT);

			if (artifactId == null || newArtifactId == null
					|| !artifactId.trim().equals(newArtifactId.trim())) {
				returnValue = false;
			} else {
				returnValue = true;
			}
		}

		return returnValue;
	}

}
