package net.meisen.ant.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import net.meisen.ant.tasks.types.AttributeValue;
import net.meisen.ant.tasks.types.HookKey;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.AntlibDefinition;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.taskdefs.MacroInstance;
import org.apache.tools.ant.taskdefs.MacroDef.Attribute;

import edu.emory.mathcs.backport.java.util.Collections;

public class TriggerHooks extends AntlibDefinition {

	private String namespace;
	private String name;

	private Map<String, AttributeValue> attributeValues = new LinkedHashMap<String, AttributeValue>();

	/**
	 * Gets this hook's <code>AttributeValue</code> instances.
	 * 
	 * @return the hook's <code>AttributeValue</code> instances
	 */
	public Collection<AttributeValue> getAttributeValues() {
		return attributeValues.values();
	}

	/**
	 * Add an attribute element.
	 * 
	 * @param attribute
	 *          an attribute nested element.
	 */
	public void addConfiguredAttributeValue(final AttributeValue attribute) {

		if (attribute.getName() == null) {
			throw new BuildException(
					"The attribute nested element needs a 'name' attribute");
		}

		// make sure the attribute is unique
		if (attributeValues.containsKey(attribute.getName())) {
			throw new BuildException("The name '" + attribute.getName()
					+ "' has already been used in another attributeValue element");
		}

		attributeValues.put(attribute.getName(), attribute);
	}

	/**
	 * Gets the name of the hook.
	 * 
	 * @return the name of the hook
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the hook. The name must be specified otherwise the
	 * execution fails.
	 * 
	 * @param name
	 *          the name of the hook
	 */
	public void setName(final String name) {
		if (!HookKey.isValidName(name)) {
			throw new BuildException("Illegal name [" + name + "] for hook");
		}

		this.name = name.toLowerCase(Locale.ENGLISH);
	}

	/**
	 * Gets the namespace of the hook, can be <code>null</code>.
	 * 
	 * @return the namespace of the hook, can be <code>null</code>
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Defines the namespace of the hook.
	 * 
	 * @param namespace
	 *          the namespace of the hook
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Gets a set of all the defined hooks.
	 * 
	 * @return a set of all the defined hooks
	 */
	@SuppressWarnings("unchecked")
	protected Map<HookKey, DefineHook> getDefinedHooks() {
		final Map<HookKey, DefineHook> hooks = (Map<HookKey, DefineHook>) getProject()
				.getReference(DefineHook.getIdRefDefinedHooks());

		if (hooks == null) {
			return new HashMap<HookKey, DefineHook>();
		} else {
			return Collections.unmodifiableMap(hooks);
		}
	}

	/**
	 * Gets a set of all the hooks.
	 * 
	 * @return a set of all the defined hooks
	 */
	@SuppressWarnings("unchecked")
	protected Map<HookKey, List<Hook>> getAllHooks() {
		final Map<HookKey, List<Hook>> hooks = (Map<HookKey, List<Hook>>) getProject()
				.getReference(Hook.getIdRefHooks());

		if (hooks == null) {
			return new HashMap<HookKey, List<Hook>>();
		} else {
			return Collections.unmodifiableMap(hooks);
		}
	}

	/**
	 * Gets the <code>DefineHook</code> task which defined the hook, which should
	 * be triggered by this trigger.
	 * 
	 * @return the <code>DefineHook</code> of this trigger
	 */
	protected DefineHook getDefinedHook() {
		return getDefinedHooks().get(new HookKey(namespace, name));
	}

	@SuppressWarnings("unchecked")
	protected List<Hook> getHooks() {
		final List<Hook> hooks = getAllHooks().get(new HookKey(namespace, name));

		if (hooks == null) {
			return new ArrayList<Hook>();
		} else {
			return Collections.unmodifiableList(hooks);
		}
	}

	@Override
	public void execute() {
		if (name == null || "".equals(name)) {
			throw new BuildException(
					"You must specify the name of the hook for the trigger.");
		} else if (getOwningTarget() == null || getOwningTarget().getName() == null
				|| "".equals(getOwningTarget().getName())) {
			throw new BuildException(
					"A trigger cannot be defined on top-level, i.e. you can only use triggers within a target.");
		}

		// get the hooks
		final List<Hook> hooks = getHooks();
		for (final Hook hook : hooks) {
			executeDelayed(hook);
		}
	}

	protected List<Attribute> getAttributes() {

		// get the definition
		final DefineHook definition = getDefinedHook();
		if (definition == null) {
			throw new BuildException("There is no hook defined with the identifier '"
					+ (namespace == null ? name : (namespace + ":" + name)) + "'.");
		} else if (!validAttributes(definition.getAttributes())) {
			String separator;

			// get some nice outputs of the attributes
			String strAttr = "";
			separator = "";
			for (final Attribute attr : definition.getAttributes()) {
				strAttr += separator
						+ attr.getName()
						+ (attr.getDefault() == null ? "" : " (default: "
								+ attr.getDefault() + ")");
				separator = ", ";
			}

			// get some nice outputs of the values
			String strAttrValue = "";
			separator = "";
			for (final String attrValue : attributeValues.keySet()) {
				strAttrValue += separator + attrValue;
				separator = ", ";
			}

			throw new BuildException("The attribute values ('" + strAttrValue
					+ "') do not match the defined attributes ('" + strAttr + "').");
		}

		return definition.getAttributes();
	}

	public void executeDelayed(final Hook hook) {

		final MacroDef macroDef = new MacroDef() {

			public UnknownElement getNestedTask() {
				final UnknownElement ret = super.getNestedTask();

				// remove all the children added by super
				if (ret.getChildren() != null) {
					ret.getChildren().clear();
				}

				// add the nestedSequentials of this
				for (final UnknownElement e : hook.getNested()) {
					ret.addChild(e);
					ret.getWrapper().addChild(e.getWrapper());
				}

				return ret;
			}
		};
		// this is just called so that the nestedSequential isn't null
		macroDef.createSequential();
		macroDef.setProject(getProject());

		// generate an instance of this definition
		final MacroInstance instance = new MacroInstance();
		instance.setProject(getProject());
		instance.setOwningTarget(getOwningTarget());
		instance.setMacroDef(macroDef);

		// add the attributes to the macroDef and instance
		for (final Attribute attribute : getAttributes()) {
			final String name = attribute.getName();
			final AttributeValue attrVal = attributeValues.get(name);

			// add the attribute
			macroDef.addConfiguredAttribute(attribute);

			// check if we found one
			if (attrVal != null) {
				instance.setDynamicAttribute(name.toLowerCase(), attrVal.getValue());
			}
		}

		// execute
		instance.execute();
	}

	protected boolean validAttributes(final List<Attribute> attributes) {

		// check if we have a value for each attribute
		for (final Attribute attribute : attributes) {
			final String name = attribute.getName();

			if (attributeValues.containsKey(name)) {
				continue;
			} else if (attribute.getDefault() != null) {
				continue;
			} else {
				return false;
			}
		}

		return true;
	}
}
