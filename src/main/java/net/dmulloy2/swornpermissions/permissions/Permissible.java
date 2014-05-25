/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.dmulloy2.swornpermissions.SwornPermissions;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.permissions.Permission;

/**
 * @author dmulloy2
 */

public abstract class Permissible implements ConfigurationSerializable
{
	protected Set<String> permissionNodes;
	protected List<String> sortedPermissions;
	protected Map<String, Boolean> permissions;

	protected Map<String, Object> options;
	protected String prefix;
	protected String suffix;

	protected final String name;
	protected final SwornPermissions plugin;

	// Base Constructor
	public Permissible(SwornPermissions plugin, String name)
	{
		this.name = name;
		this.plugin = plugin;
		this.permissionNodes = new HashSet<String>();
		this.sortedPermissions = new ArrayList<String>();
		this.permissions = new LinkedHashMap<String, Boolean>();
		this.options = new LinkedHashMap<String, Object>();
		this.prefix = "";
		this.suffix = "";
	}

	public Permissible(SwornPermissions plugin, String name, MemorySection section)
	{
		this(plugin, name);
		this.loadFromDisk(section);
	}

	// ---- I/O

	public void loadFromDisk(MemorySection section)
	{
		this.permissionNodes = new HashSet<String>(section.getStringList("permissions"));

		if (section.isSet("options"))
		{
			Map<String, Object> values = section.getConfigurationSection("options").getValues(false);
			for (String key : values.keySet())
				options.put(key.toLowerCase(), values.get(key));

			this.prefix = options.containsKey("prefix") ? (String) options.get("prefix") : "";
			this.suffix = options.containsKey("suffix") ? (String) options.get("suffix") : "";
		}
	}

	public abstract boolean shouldBeSaved();

	public String getSaveName()
	{
		return name;
	}

	// ---- Permission Management

	public final void addPermission(Permission permission)
	{
		addPermission(permission.getName());
	}

	public final void addPermission(String node)
	{
		permissionNodes.add(node);
	}

	public final void removePermission(Permission permission)
	{
		removePermission(permission.getName());
	}

	public final void removePermission(String node)
	{
		permissionNodes.remove(node);
	}

	public final boolean hasPermission(String permission)
	{
		boolean negative = permission.startsWith("-");
		permission = negative ? permission.substring(1) : permission;

		if (permissions.containsKey(permission))
		{
			boolean value = permissions.get(permission);
			return negative ? ! value : value;
		}

		return false;
	}

	// Whether or not this permissible has a node
	// Does not take into account wildcards or children
	public final boolean hasPermissionNode(String node)
	{
		return sortedPermissions.contains(node);
	}

	public final String getMatchingPermission(String node)
	{
		List<String> permissions = new ArrayList<String>(sortedPermissions);
		if (permissions.contains("*") && ! node.startsWith("-"))
			return "*";

		// Remove *
		permissions.remove("*");

		// Iterate and try to find a match
		for (String permission : permissions)
		{
			if (node.matches(permission))
				return permission;
		}

		// No match :(
		return null;
	}

	protected final void updatePermissionMap()
	{
		// Sort the nodes
		List<String> permissionNodes = sortPermissions();

		// Update sorted permissions list
		this.sortedPermissions = permissionNodes;

		// Get matching permissions
		permissionNodes = getMatchingNodes(permissionNodes);

		// Get children
		permissionNodes = getAllChildren(permissionNodes);

		Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();

		// Values
		for (String permissionNode : permissionNodes)
		{
			permissionNode = permissionNode.toLowerCase();
			boolean value = ! permissionNode.startsWith("-");
			permissions.put(value ? permissionNode : permissionNode.substring(1), value);
		}

		// Update permission map
		this.permissions = permissions;
	}

	protected abstract List<String> sortPermissions();

	// Order in this method is extremely important. Negated nodes need to be
	// last in the list, so they can replace any conflicting positive nodes.
	// Also, if a player has a raw positive and a raw negative node, the
	// positive node will be chosen.
	protected final List<String> sort(Set<String> permissions)
	{
		Set<String> ret = new HashSet<String>();

		// Add * permission first
		if (permissions.contains("*"))
		{
			ret.add("*");
			permissions.remove("*");
		}

		// Add positive nodes first
		for (String permission : new HashSet<String>(permissions))
		{
			if (! permission.startsWith("-"))
			{
				ret.add(permission);
				permissions.remove(permission);
			}
		}

		// Add negative nodes last
		for (String permission : new HashSet<String>(permissions))
		{
			if (permission.startsWith("-"))
			{
				// Do we have a positive permission node?
				if (! permissions.contains(permission.substring(1)))
				{
					ret.add(permission);
				}
			}
		}

		List<String> sorted = new ArrayList<String>(ret);
		Collections.reverse(sorted);
		return sorted;
	}

	// Wildcard support
	protected final List<String> getMatchingNodes(List<String> permissions)
	{
		List<String> ret = new ArrayList<String>();

		for (String node : permissions)
		{
			// '*' is handled later
			if (node.equals("*"))
			{
				ret.add("*");
				continue;
			}

			ret.add(node);
			boolean negative = node.startsWith("-");
			node = negative ? node.substring(1) : node;

			for (Permission permission : plugin.getServer().getPluginManager().getPermissions())
			{
				String name = permission.getName();
				if (name.matches(node))
				{
					name = negative ? "-" + name : name;
					ret.add(name);
				}
			}
		}

		return ret;
	}

	protected final List<String> getAllChildren(List<String> permissions)
	{
		List<String> ret = new ArrayList<String>();

		for (String permission : permissions)
		{
			boolean negative = permission.startsWith("-");
			ret.add(permission);

			String node = negative ? permission.substring(1) : permission;
			List<String> children = getChildren(node);
			if (children != null)
			{
				for (String child : children)
				{
					child = negative ? "-" + child : child;
					ret.add(child);
				}
			}
		}

		return ret;
	}

	protected final List<String> getChildren(String node)
	{
		if (node.equals("*"))
		{
			List<String> ret = new ArrayList<String>();
			for (Permission permission : plugin.getPermissionHandler().getRegisteredPermissions())
			{
				ret.add(permission.getName());
				List<String> children = getChildren(permission);
				if (children != null)
					ret.addAll(children);
			}

			return ! ret.isEmpty() ? ret : null;
		}

		return getChildren(plugin.getPermissionHandler().getPermission(node));
	}

	protected final List<String> getChildren(Permission permission)
	{
		if (permission == null)
			return null;

		Map<String, Boolean> children = permission.getChildren();
		if (children == null || children.isEmpty())
			return null;

		List<String> ret = new ArrayList<String>();
		for (Entry<String, Boolean> child : children.entrySet())
		{
			if (child.getValue())
			{
				ret.add(child.getKey());
				List<String> childNodes = getChildren(child.getKey());
				if (childNodes != null)
					ret.addAll(childNodes);
			}
		}

		return ! ret.isEmpty() ? ret : null;
	}

	// ---- Getters and Setters

	public final String getName()
	{
		return name;
	}

	public Set<String> getPermissionNodes()
	{
		return new HashSet<String>(permissionNodes);
	}

	public Set<String> getAllPermissionNodes()
	{
		return getPermissionNodes();
	}

	public Map<String, Object> getOptions()
	{
		return new LinkedHashMap<String, Object>(options);
	}

	public Object getOption(String key)
	{
		return options.get(key.toLowerCase());
	}

	public final void setOption(String key, Object value)
	{
		key = key.toLowerCase();

		if (value == null)
			options.remove(key);
		else
			options.put(key, value);
	}

	public boolean hasOption(String key)
	{
		return options.containsKey(key.toLowerCase());
	}

	public String getPrefix()
	{
		return prefix;
	}

	public final void setPrefix(String prefix)
	{
		options.put("prefix", prefix);
		this.prefix = prefix;
	}

	public boolean hasPrefix()
	{
		return prefix != "";
	}

	public String getSuffix()
	{
		return suffix;
	}

	public final void setSuffix(String suffix)
	{
		options.put("suffix", suffix);
		this.suffix = suffix;
	}

	public boolean hasSuffix()
	{
		return suffix != "";
	}

	// ---- Conversion Methods

	/**
	 * @deprecated For conversion use ONLY
	 */
	public final void setPermissionNodes(Set<String> permissionNodes)
	{
		this.permissionNodes = permissionNodes;
	}

	/**
	 * @deprecated For conversion use ONLY
	 */
	public final void setOptions(Map<String, Object> options)
	{
		for (String key : options.keySet())
			this.options.put(key.toLowerCase(), options.get(key));
	}

	// ---- Required Abstract Methods

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();
}