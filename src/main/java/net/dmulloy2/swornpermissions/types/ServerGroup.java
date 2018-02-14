/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.types;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dmulloy2.swornpermissions.SwornPermissions;

import org.bukkit.configuration.MemorySection;

/**
 * @author dmulloy2
 */

public class ServerGroup extends Group
{
	public ServerGroup(SwornPermissions plugin, String name)
	{
		super(plugin, name, null);
	}

	public ServerGroup(SwornPermissions plugin, String name, MemorySection section)
	{
		this(plugin, name);
		this.loadFromDisk(section);
	}

	// ---- I/O

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> ret = new LinkedHashMap<>();

		ret.put("permissions", permissionNodes);
		ret.put("options", options);

		return ret;
	}

	@Override
	public boolean shouldBeSaved()
	{
		return true;
	}

	// ---- Permissions

	@Override
	public Set<String> sortPermissions()
	{
		List<String> groupPerms = getPermissionNodes();
		groupPerms = getAllChildren(groupPerms);
		groupPerms = getMatchingNodes(groupPerms);
		return sort(groupPerms);
	}

	// ---- Parent Groups (Server Groups cannot have parents)

	@Override
	public boolean hasParentGroup()
	{
		return false;
	}

	@Override
	public boolean hasParentGroup(Group parent)
	{
		return false;
	}

	@Override
	public void addParentGroup(Group parent)
	{
		throw new IllegalStateException("Server Groups cannot have parents!");
	}

	@Override
	public void removeParentGroup(Group parent)
	{
		throw new IllegalStateException("Server Groups cannot have parents!");
	}

	@Override
	public String findPrefix()
	{
		return options.containsKey("prefix") ? (String) options.get("prefix") : "";
	}

	// ---- Utility

	@Override
	public void updatePermissions(boolean force)
	{
		updatePermissions(force, true);
	}

	@Override
	public void updatePermissions(boolean force, boolean children)
	{
		if (! permissions.isEmpty() && ! force)
			return;

		// Update permission map
		updatePermissionMap();

		if (! children)
			return;

		// Update any world groups that inherit this group
		for (Group group : plugin.getPermissionHandler().getAllGroups())
		{
			if (group.getParentGroups() != null && group.getParentGroups().contains(this))
				group.updatePermissions(force, children);
		}
	}

	@Override
	public String toString()
	{
		return "ServerGroup[name=" + name + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ServerGroup)
		{
			ServerGroup that = (ServerGroup) obj;
			return this.name.equals(that.name);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 88;
		hash *= 1 + name.hashCode();
		return hash;
	}
}