package net.meisen.ant.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.meisen.ant.tasks.types.HookKey;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.AntlibDefinition;
import org.apache.tools.ant.taskdefs.MacroDef.NestedSequential;

/**
 * Implementations of a hook. A hook is executed whenever a trigger is fired for
 * the specified hook. This can be seen as an event driven mechanism.
 * 
 * @author pmeisen
 * 
 */
public class Hook extends AntlibDefinition {
	private final static String idRefHooks = Hook.class.getName() + ":hooks";

	private String namespace;
	private String name;
	private int priority = 0;

	private NestedSequential nestedSequential;

	/**
	 * Gets the identifier of the reference used to refer to the available hook
	 * instances.
	 * 
	 * @return the identifier of the reference used to refer to the available hook
	 *         instances
	 */
	public final static String getIdRefHooks() {
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
	 * Gets a set of all the hooks. The set can be modified, i.e. hooks can be
	 * added or removed, if so those hooks will be considered on the next
	 * execution.
	 * 
	 * @return a set of all the defined hooks
	 */
	@SuppressWarnings("unchecked")
	protected Map<HookKey, List<Hook>> getHooks() {
		return (Map<HookKey, List<Hook>>) getProject()
				.getReference(getIdRefHooks());
	}

	@Override
	public void init() {
		final Project project = getProject();

		// add a reference
		if (!project.hasReference(getIdRefHooks())) {

			// add a new instance
			project.addReference(getIdRefHooks(), new HashMap<HookKey, List<Hook>>());
		}
	}

	@Override
	public void execute() {
		final HookKey key = new HookKey(namespace, name);
		final Map<HookKey, List<Hook>> hooks = getHooks();

		// check if we already have a list
		List<Hook> list = hooks.get(key);
		if (list == null) {

			// create the new list and add it
			list = new ArrayList<Hook>();
			hooks.put(key, list);
		}

		// add it
		list.add(this);
	}

	/**
	 * Get all the nested tasks defined for the hook.
	 * 
	 * @return all the nested tasks, defined for <code>this</code> instance
	 */
	@SuppressWarnings("unchecked")
	public List<UnknownElement> getNested() {
		return (List<UnknownElement>) nestedSequential.getNested();
	}

	/**
	 * This is the sequential nested element of the macrodef.
	 * 
	 * @return a sequential element to be configured.
	 */
	public NestedSequential createSequential() {
		if (this.nestedSequential != null) {
			throw new BuildException("Only one sequential allowed");
		}
		this.nestedSequential = new NestedSequential();
		return this.nestedSequential;
	}

	/**
	 * Gets the priority of the hook.
	 * 
	 * @return the priority of the hook
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Sets the priority of the hook.
	 * 
	 * @param priority
	 *          the priority of the hook
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
