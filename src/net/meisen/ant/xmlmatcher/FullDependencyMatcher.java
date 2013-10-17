package net.meisen.ant.xmlmatcher;

import org.jdom.Element;

/**
 * A matcher which check a dependency completely, i.e. group, artifact, type,
 * classifier and version
 * 
 * @author pmeisen
 * 
 */
public class FullDependencyMatcher extends DependencyMatcher {

	@Override
	protected boolean matchedArtifacts(final Element original, final Element patch) {

		// the groupId and the artifactId must be equal
		if (super.matchedArtifacts(original, patch)) {

			// we also need the classifier, it has to be equal as well
			final String classifier = original.getChildText(ATTRIB_CLASSIFIER);
			final String newClassifier = patch.getChildText(ATTRIB_CLASSIFIER);

			final String type = original.getChildText(ATTRIB_TYPE);
			final String newType = patch.getChildText(ATTRIB_TYPE);

			return equals(classifier, newClassifier, true)
					&& equalTypes(type, newType);
		} else {
			return false;
		}
	}

	@Override
	protected boolean matchedVersion(final Element original, final Element patch) {

		if (super.matchedVersion(original, patch)) {

			// get the original and patch group
			final String version = original.getChildText(ATTRIB_VERSION);
			final String newVersion = patch.getChildText(ATTRIB_VERSION);

			if (version == null || newVersion == null) {
				return false;
			} else if (version.equals(newVersion)) {
				return true;
			} else {
				System.err.println("The version of a matched dependency does not fit '"
						+ version + "' vs. '" + newVersion
						+ "', please match the versions.");

				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Checks if the two types are equal
	 * 
	 * @param typeA
	 *          the first type
	 * @param typeB
	 *          the second type
	 * 
	 * @return <code>true</code> if the types are equal concerning maven,
	 *         otherwise <code>false</code>
	 */
	protected boolean equalTypes(final String typeA, final String typeB) {
		if (equals(typeA, typeB, true)) {
			return true;
		} else if ("jar".equals(typeA) && isEmptyOrNull(typeB)) {
			return true;
		} else if ("jar".equals(typeB) && isEmptyOrNull(typeA)) {
			return true;
		} else if (typeA == null || typeB == null) {
			return false;
		} else if (typeA.equals(typeB)) {
			return true;
		} else {
			return false;
		}
	}
}
