package net.meisen.ant.xmlmatcher;

import org.jdom.Element;

import ch.elca.el4j.services.xmlmerge.matcher.TagMatcher;

/**
 * A default <code>TagMatcher</code> which logs what is compared and the result
 * of the comparison to the default <code>System.out</code>.
 * 
 * @author pmeisen
 * 
 */
public class LogTagMatcher extends TagMatcher {

	@Override
	public boolean matches(final Element originalEl, final Element patchEl) {
		final boolean res = super.matches(originalEl, patchEl);

		// get the values
		final String org = originalEl == null ? null : originalEl.getName();
		final String patch = patchEl == null ? null : patchEl.getName();

		// write the output as promised
		System.out.println("Comparing " + org + " and " + patch
				+ " with result " + res);

		// return the result
		return res;
	}
}
