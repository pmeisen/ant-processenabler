package net.meisen.ant.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.meisen.ant.tasks.types.HookKey;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.AntlibDefinition;
import org.apache.tools.ant.taskdefs.MacroDef.Attribute;

/**
 * Task used to define a hook, i.e. a point which can be trigger by the task
 * {@link TriggerHooks} task. If the defined hook is triggered all the specified
 * hook-tasks (see {@link Hook}) are executed.
 * 
 * @author pmeisen
 * 
 */
public class DefineHook extends AntlibDefinition {
	private final static String idRefHooks = DefineHook.class.getName()
			+ ":definedHooks";

	private String namespace;
	private String name;
	private List<Attribute> attributes = new ArrayList<Attribute>();

	/**
	 * Get the identifier of the reference to retrieve all the defined hooks.
	 * 
	 * @return the identifier of the reference to retrieve all the defined hooks
	 */
	public final static String getIdRefDefinedHooks() {
		return idRefHooks;
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
	 * Gets this hook's attributes.
	 * 
	 * @return the hook's attributes
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * Add an attribute element.
	 * 
	 * @param attribute
	 *          an attribute nested element.
	 */
	public void addConfiguredAttribute(final Attribute attribute) {

		if (attribute.getName() == null) {
			throw new BuildException(
					"The attribute nested element needs a 'name' attribute");
		}

		// make sure the attribute is unique
		for (final Attribute att : attributes) {
			if (att.getName().equals(attribute.getName())) {
				throw new BuildException("The name '" + attribute.getName()
						+ "' has already been used in another attribute element");
			}
		}
		attributes.add(attribute);
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

		this.name = HookKey.normalizeName(name);
	}

	@Override
	public void init() {
		final Project project = getProject();

		// add a reference
		if (!project.hasReference(getIdRefDefinedHooks())) {

			// add a new instance
			project.addReference(getIdRefDefinedHooks(),
					new HashMap<HookKey, DefineHook>());
		}
	}

	/**
	 * Gets a set of all the defined hooks. The set can be modified, i.e. hooks
	 * can be added or removed, if so those hooks will be considered on the next
	 * execution.
	 * 
	 * @return a set of all the defined hooks
	 */
	@SuppressWarnings("unchecked")
	protected Map<HookKey, DefineHook> getDefinedHooks() {
		return (Map<HookKey, DefineHook>) getProject().getReference(
				getIdRefDefinedHooks());
	}

	@Override
	public void execute() {
		if (name == null || "".equals(name)) {
			throw new BuildException("You must specify a name for the hook.");
		} else if (getOwningTarget() != null && getOwningTarget().getName() != null
				&& !"".equals(getOwningTarget().getName())) {
			throw new BuildException(
					"A hook can only be defined on a top level, i.e. not within a target ('"
							+ getOwningTarget().getName() + ".");
		}

		// create the key
		final HookKey key = new HookKey(namespace, name);

		if (isDefinedHook(key)) {
			throw new BuildException("There are several hooks with the name '" + name
					+ "' defined.");
		}

		// add the hook
		getDefinedHooks().put(key, this);
	}

	/**
	 * Checks if a hook with the specified name is defined (namespace is
	 * considered to be <code>null</code>, i.e. not defined).
	 * 
	 * @param name
	 *          the name of the hook to check
	 * 
	 * @return <code>true</code> if a hook with the specified name exists,
	 *         otherwise <code>false</code>
	 */
	public boolean isDefinedHook(final String name) {
		return isDefinedHook(null, name);
	}

	/**
	 * Checks if a hook with the specified namespace and name is defined.
	 * 
	 * @param namespace
	 *          the namespace of the hook to be checked
	 * @param name
	 *          the name of the hook to check
	 * 
	 * @return <code>true</code> if a hook with the specified namespace and name
	 *         exists, otherwise <code>false</code>
	 */
	public boolean isDefinedHook(final String namespace, final String name) {
		return isDefinedHook(new HookKey(namespace, name));
	}

	/**
	 * Checks if a hook with the specified <code>HookKey</code> is defined.
	 * 
	 * @param hook
	 *          the <code>HookKey</code> of the hook to be checked
	 * 
	 * @return <code>true</code> if a hook with the specified <code>HookKey</code>
	 *         exists, otherwise <code>false</code>
	 */
	public boolean isDefinedHook(final HookKey hook) {
		return getDefinedHooks().containsKey(hook);
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
}
