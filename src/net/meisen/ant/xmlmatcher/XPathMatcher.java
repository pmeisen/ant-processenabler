package net.meisen.ant.xmlmatcher;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;

import ch.elca.el4j.services.xmlmerge.Matcher;

/**
 * Matcher to match an element in the original document, using an xPath to
 * specify the match. The xPath is specified within the
 * 
 * <b>Note:</b><br />
 * When you want to output some XML stuff you can use:
 * 
 * <pre>
 * final XMLOutputter outp = new XMLOutputter();
 * System.out.println(outp.outputString(element));
 * </pre>
 * 
 * @author pmeisen
 * 
 */
public class XPathMatcher implements Matcher {

	final static Namespace ns = Namespace
			.getNamespace("http://ant-processenabler.meisen.net/schema/xpathmatcher");
	final static String ruleAttributeName = "rule";

	@Override
	public boolean matches(final Element originalElement,
			final Element patchElement) {

		// check if the element has a rule defined
		final Attribute ruleAttribute = patchElement.getAttribute(
				ruleAttributeName, ns);
		if (ruleAttribute != null) {
			// get the rule
			final String rule = ruleAttribute.getValue();

			// check if the rule applies
			final boolean match = matchToOriginal(originalElement, rule);
			if (match) {

				// remove the rule from the element
				patchElement.removeAttribute(ruleAttribute);
			}

			return match;
		} else {
			return false;
		}
	}

	/**
	 * Checks if the path matches for the original element
	 * 
	 * @param element
	 *          the original element
	 * @param xPath
	 *          the path
	 * 
	 * @return <code>true</code> if it matches, otherwise <code>false</code>
	 */
	protected boolean matchToOriginal(final Element element, final String xPath) {

		try {
			@SuppressWarnings("unchecked")
			final List<Object> l = XPath.selectNodes(element, xPath);

			for (final Object o : l) {
				if (element.equals(o)) {
					return true;
				}
			}

			return false;
		} catch (final JDOMException e) {
			return false;
		}
	}
}
