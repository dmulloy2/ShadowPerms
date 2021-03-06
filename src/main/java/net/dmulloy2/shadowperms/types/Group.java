/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.types;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.shadowperms.ShadowPerms;

import org.bukkit.configuration.MemorySection;

/**
 * @author dmulloy2
 */

public abstract class Group extends Permissible
{
	protected List<String> parents;
	protected List<Group> parentGroups;

	public Group(ShadowPerms plugin, String name, String world)
	{
		super(plugin, name, world);
		this.parents = new ArrayList<String>();
		this.parentGroups = new ArrayList<Group>();
	}

	public Group(ShadowPerms plugin, String name, String world, MemorySection section)
	{
		this(plugin, name, world);
		this.loadFromDisk(section);
	}

	public abstract void updatePermissions(boolean force, boolean users);

	// ---- Parents

	public abstract boolean hasParentGroup();

	public abstract boolean hasParentGroup(Group parent);

	public abstract void addParentGroup(Group parent);

	public abstract void removeParentGroup(Group parent);

	public List<Group> getParentGroups()
	{
		return parentGroups;
	}

	public List<Group> getAllParentGroups()
	{
		List<Group> ret = new ArrayList<>();
		for (Group group : parentGroups)
			ret.addAll(group.getAllParentGroups());
		return ret;
	}
}