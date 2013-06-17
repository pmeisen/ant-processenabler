package net.meisen.ant.xmlmatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.Element;

import ch.elca.el4j.services.xmlmerge.Action;
import ch.elca.el4j.services.xmlmerge.action.InsertAction;

/**
 * Same as the {@link InsertAction}, but instead of adding the element to the
 * end, it will be added as first element.<br/>
 * <br/>
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
public class InsertFirstAction implements Action {

	@Override
	public void perform(final Element originalElement,
			final Element patchElement, final Element outputParentElement) {

		// if we don't have anything, we just clone the original
		if (patchElement == null && originalElement != null) {
			outputParentElement.addContent((Element) originalElement.clone());
		} else {
			
			// search for the first position
			@SuppressWarnings("unchecked")
			final List<Content> outputContent = outputParentElement.getContent();

			// iterate over all elements and find the first occurrence
			int i = -1;
			final Iterator<Content> it = outputContent.iterator();
			while (it.hasNext()) {
				final Content content = it.next();
				if (content instanceof Element
						&& ((Element) content).getQualifiedName().equals(
								patchElement.getQualifiedName())) {
					i = outputParentElement.indexOf(content);
					break;
				}
			}

			// add the element
			final List<Content> toAdd = new ArrayList<Content>();
			toAdd.add(patchElement);

			// add the patchelement
			i = Math.max(i - 1, 0);
			outputContent.add(i, patchElement);
			
			// if we have an original element we should add this as well
			if (originalElement != null) {
				outputParentElement.addContent(Math.max(i + 1, 0),
						(Element) originalElement.clone());
			}
		}
	}
}
