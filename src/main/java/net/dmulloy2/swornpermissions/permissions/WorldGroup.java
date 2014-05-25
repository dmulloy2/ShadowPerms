/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.permissions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.UniformSet;

import org.bukkit.configuration.MemorySection;

/**
 * @author dmulloy2
 */

public class WorldGroup extends Group
{
	private String worldName;
	private boolean defaultGroup;

	public WorldGroup(SwornPermissions plugin, String name, String world)
	{
		super(plugin, name);
		this.worldName = world;
	}

	public WorldGroup(SwornPermissions plugin, String name, String world, MemorySection section)
	{
		this(plugin, name, world);
		this.loadFromDisk(section);
	}

	// ---- I/O

	@Override
	public void loadFromDisk(MemorySection section)
	{
		super.loadFromDisk(section);
		this.defaultGroup = section.getBoolean("default", false);
		this.parents = new UniformSet<String>(section.getStringList("parents"));
	}

	public void loadParentGroups()
	{
		for (String parent : parents)
		{
			Group group = plugin.getPermissionHandler().getGroup(worldName, parent);
			if (group != null)
				parentGroups.add(group);
			else
				plugin.getLogHandler().log(Level.WARNING, "Could not find parent group \"{0}\" for group {1}", parent, name);
		}
	}

	@Override
	public boolean shouldBeSaved()
	{
		return true;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> ret = new LinkedHashMap<String, Object>();

		ret.put("default", defaultGroup);
		ret.put("permissions", new ArrayList<String>(permissionNodes));
		ret.put("parents", new ArrayList<String>(parents));
		ret.put("options", options);

		return ret;
	}

	// ---- Getters

	@Override
	public Set<String> getAllPermissionNodes()
	{
		Set<String> ret = new UniformSet<String>();

		// Add parent nodes
		ret.addAll(getParentNodes());

		// Add group nodes
		ret.addAll(permissionNodes);

		return ret;
	}

	private final Set<String> getParentNodes()
	{
		Set<String> ret = new UniformSet<String>();

		// Add all nodes from parent groups
		if (parents != null)
		{
			for (Group parent : parentGroups)
				ret.addAll(parent.getAllPermissionNodes());
		}

		return ret;
	}

	@Override
	public Set<String> sortPermissions()
	{
		Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();

		// Add parent nodes first
		Set<String> parentNodes = sort(getParentNodes());

		for (String parentNode : new UniformSet<String>(parentNodes))
		{
			boolean value = ! parentNode.startsWith("-");
			permissions.put(value ? parentNode : parentNode.substring(1), value);
		}

		// Add group-specific nodes last
		Set<String> groupNodes = sort(getPermissionNodes());

		for (String groupNode : new UniformSet<String>(groupNodes))
		{
			boolean value = ! groupNode.startsWith("-");
			permissions.put(value ? groupNode : groupNode.substring(1), value);
		}

		Set<String> ret = new UniformSet<String>();

		for (String node : permissions.keySet())
		{
			boolean value = permissions.get(node);
			ret.add(value ? node : "-" + node);
		}

		// Sort and return
		return sort(ret);
	}

	// ---- Parent Groups

	@Override
	public boolean hasParentGroup()
	{
		return parents.size() > 0;
	}

	@Override
	public boolean hasParentGroup(Group parent)
	{
		return hasParentGroup() && parentGroups.contains(parent);
	}

	@Override
	public void addParentGroup(Group parent)
	{
		parentGroups.add(parent);
		parents.add(parent.getName());
	}

	@Override
	public void removeParentGroup(Group parent)
	{
		parentGroups.remove(parent);
		parents.remove(parent.getName());
	}

	/**
	 * @deprecated For conversion use ONLY
	 */
	public void setParentGroups(Set<String> parents)
	{
		this.parents = parents;
	}

	// ---- Default

	public boolean isDefaultGroup()
	{
		return defaultGroup;
	}

	// ---- Utility

	@Override
	public void update()
	{
		// Update perms map
		updatePermissionMap();

		// Update child groups
		for (Group group : plugin.getPermissionHandler().getGroups(worldName))
		{
			if (group.getParentGroups() != null && group.getParentGroups().contains(this))
				group.update();
		}

		// Update users with this group
		for (User user : plugin.getPermissionHandler().getUsers(worldName))
		{
			if (user.getGroup().equals(this) || user.getSubGroups().contains(this))
				user.updatePermissions(true);
		}
	}

	// ---- Generic Methods

	@Override
	public String toString()
	{
		return "WorldGroup { name = " + name + ", world = " + worldName + " }";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof WorldGroup)
		{
			WorldGroup that = (WorldGroup) obj;
			if (! this.getName().equals(that.getName()))
				return false;

			return this.worldName.equals(that.worldName);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 89;
		hash *= name.hashCode();
		hash *= worldName.hashCode();
		return hash;
	}
}