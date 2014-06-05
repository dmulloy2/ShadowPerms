/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.util.Util;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.permissions.Permission;

/**
 * @author dmulloy2
 */

public abstract class Permissible implements ConfigurationSerializable
{
	protected Map<String, Long> timestamps;
	protected List<String> permissionNodes;
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
		this.timestamps = new HashMap<String, Long>();
		this.permissionNodes = new ArrayList<String>();
		this.sortedPermissions = new ArrayList<String>();
		this.permissions = new LinkedHashMap<String, Boolean>();
		this.options = new HashMap<String, Object>();
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
		this.permissionNodes = new ArrayList<String>(section.getStringList("permissions"));

		if (section.isSet("options"))
		{
			Map<String, Object> values = section.getConfigurationSection("options").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
				if (! entry.getValue().toString().isEmpty())
					options.put(entry.getKey().toLowerCase(), entry.getValue());

			this.prefix = options.containsKey("prefix") ? (String) options.get("prefix") : "";
			this.suffix = options.containsKey("suffix") ? (String) options.get("suffix") : "";
		}

		if (section.isSet("timestamps"))
		{
			Map<String, Object> values = section.getConfigurationSection("timestamps").getValues(false);
			for (Entry<String, Object> entry : values.entrySet())
			{
				long expires = (long) entry.getValue();
				if (expires < System.currentTimeMillis())
					options.put(entry.getKey(), expires);
			}
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

	public final void addTempPermission(String node, long expires)
	{
		timestamps.put(node, expires);
	}

	public final void removePermission(Permission permission)
	{
		removePermission(permission.getName());
	}

	public final void removePermission(String node)
	{
		permissionNodes.remove(node);
	}

	public final void removeTempPermission(String node)
	{
		timestamps.remove(node);
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

	public final boolean hasTempPermission(String node)
	{
		cleanTempPermissions();
		return timestamps.containsKey(node);
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

		// Add * first
		if (permissionNodes.contains("*"))
		{
			permissionNodes.remove("*");
			permissions.put("*", true);
		}

		// Add positive nodes next
		for (String permission : new ArrayList<String>(permissionNodes))
		{
			if (! permission.startsWith("-"))
			{
				permissionNodes.remove(permission);
				permissions.put(permission, true);
			}
		}

		// Add negative nodes last
		for (String permission : new ArrayList<String>(permissionNodes))
		{
			permission = permission.substring(1);

			permissionNodes.remove(permission);
			permissions.remove(permission);

			permissions.put(permission, false);
		}

		// Update permission map
		this.permissions = permissions;
	}

	public abstract void updatePermissions(boolean force);

	protected abstract List<String> sortPermissions();

	// Order: *, positive, negative
	// If there is a positive node and a negative node, the positive node is chosen
	protected final List<String> sort(List<String> permissions)
	{
		List<String> ret = new ArrayList<String>();

		// Add * permission first
		if (permissions.contains("*"))
		{
			permissions.remove("*");
			ret.add("*");
		}

		// Add positive nodes next
		for (String permission : new ArrayList<String>(permissions))
		{
			if (! permission.startsWith("-"))
			{
				permissions.remove(permission);
				if (! ret.contains(permission))
					ret.add(permission);
			}
		}

		// Add negative nodes last
		for (String permission : new ArrayList<String>(permissions))
		{
			permissions.remove(permission);
			if (! ret.contains(permission.substring(1)) && ! ret.contains(permission))
				ret.add(permission);
		}

		return Util.removeDuplicates(ret);
	}

	// Wildcard support
	protected final List<String> getMatchingNodes(List<String> permissionNodes)
	{
		List<String> ret = new ArrayList<String>();

		for (String node : permissionNodes)
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

	public final Map<String, Boolean> getPermissions()
	{
		if (permissions.isEmpty())
			updatePermissionMap();

		return new LinkedHashMap<String, Boolean>(permissions);
	}

	public List<String> getPermissionNodes()
	{
		List<String> ret = new ArrayList<String>();

		// Permission nodes
		ret.addAll(permissionNodes);

		// Temp Permissions
		cleanTempPermissions();
		ret.addAll(timestamps.keySet());

		return ret;
	}

	protected final void cleanTempPermissions()
	{
		for (Entry<String, Long> entry : timestamps.entrySet())
		{
			if (entry.getValue() > System.currentTimeMillis())
				timestamps.remove(entry.getKey());
		}
	}

	public List<String> getAllPermissionNodes()
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
	public final void setPermissionNodes(List<String> permissionNodes)
	{
		this.permissionNodes = permissionNodes;
	}

	/**
	 * @deprecated For conversion use ONLY
	 */
	public final void setOptions(Map<String, Object> options)
	{
		for (Entry<String, Object> entry : options.entrySet())
			this.options.put(entry.getKey().toLowerCase(), entry.getValue());
	}

	// ---- Required Abstract Methods

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();
}