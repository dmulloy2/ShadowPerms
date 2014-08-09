/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.permissions;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.swornpermissions.SwornPermissions;

import org.bukkit.configuration.MemorySection;

/**
 * @author dmulloy2
 */

public abstract class Group extends Permissible
{
	protected List<String> parents;
	protected List<Group> parentGroups;

	public Group(SwornPermissions plugin, String name, String world)
	{
		super(plugin, name, world);
		this.parents = new ArrayList<String>();
		this.parentGroups = new ArrayList<Group>();
	}

	public Group(SwornPermissions plugin, String name, String world, MemorySection section)
	{
		this(plugin, name, world);
		this.loadFromDisk(section);
	}

	// ---- Parents

	public abstract boolean hasParentGroup();

	public abstract boolean hasParentGroup(Group parent);

	public abstract void addParentGroup(Group parent);

	public abstract void removeParentGroup(Group parent);

	public List<Group> getParentGroups()
	{
		return parentGroups;
	}
}