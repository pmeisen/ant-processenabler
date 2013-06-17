package net.meisen.ant.xmlmatcher;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Content;
import org.jdom.Element;

import ch.elca.el4j.services.xmlmerge.AbstractXmlMergeException;
import ch.elca.el4j.services.xmlmerge.Action;
import ch.elca.el4j.services.xmlmerge.Mapper;
import ch.elca.el4j.services.xmlmerge.Matcher;
import ch.elca.el4j.services.xmlmerge.action.AbstractMergeAction;

/**
 * Action to merge an original and a patch element, whereby the order of the
 * elements does not matter. 
 * 
 * @author pmeisen
 * 
 */
public class MergeAction extends AbstractMergeAction {

	@Override
	public void perform(final Element originalElement,
			final Element patchElement, final Element outputParentElement)
			throws AbstractXmlMergeException {
		
		if (patchElement == null && originalElement == null) {

			// nothing to change
			return;
		} else if (originalElement == null) {
			outputParentElement.addContent((Content) patchElement.clone());
		} else if (patchElement == null) {
			outputParentElement.addContent((Content) originalElement.clone());
		} else {
			final Element newRoot = ((Element) originalElement.clone());
			newRoot.removeContent();

			@SuppressWarnings("unchecked")
			final List<Element> orgChildren = originalElement.getChildren();
			@SuppressWarnings("unchecked")
			final List<Element> patchChildren = patchElement.getChildren();
			final List<Element> appliedPatches = new ArrayList<Element>();
			
			for (final Element orgEl : orgChildren) {
				Element match = null;

				for (final Element patchEl : patchChildren) {
					final Matcher matcher = (Matcher) m_matcherFactory
							.getOperation(orgEl, patchEl);

					if (matcher.matches(orgEl, patchEl)) {
						match = patchEl;
						break;
					}
				}

				// check what we have to do with the match
				if (match == null) {
					applyAction(orgEl, null, newRoot);
				} else {
					applyAction(orgEl, match, newRoot);

					// remove it
					appliedPatches.add(match);
				}
			}

			// merge the rest
			for (final Element patchEl : patchChildren) {

				// add it now, if its not added yet
				if (!appliedPatches.contains(patchEl)) {
					applyAction(null, patchEl, newRoot);
				}
			}

			// add the new root element
			outputParentElement.addContent(newRoot);
		}
	}

	/**
	 * Applies the action which performs the merge between two source elements.
	 * 
	 * @param workingParent
	 *            Output parent element
	 * @param originalElement
	 *            Original element
	 * @param patchElement
	 *            Patch element
	 * @throws AbstractXmlMergeException
	 *             if an error occurred during the merge
	 */
	private void applyAction(final Element originalElement,
			final Element patchElement, final Element outputParentElement)
			throws AbstractXmlMergeException {
		final Action action = (Action) m_actionFactory.getOperation(
				originalElement, patchElement);
		final Mapper mapper = (Mapper) m_mapperFactory.getOperation(
				originalElement, patchElement);

		// Propagate the factories to deeper merge actions
		// TODO: find a way to make it cleaner
		if (action instanceof MergeAction) {
			MergeAction mergeAction = (MergeAction) action;
			mergeAction.setActionFactory(m_actionFactory);
			mergeAction.setMapperFactory(m_mapperFactory);
			mergeAction.setMatcherFactory(m_matcherFactory);
		}

		action.perform(originalElement, mapper.map(patchElement),
				outputParentElement);
	}
}
