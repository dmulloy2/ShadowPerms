/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.permissions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.util.Util;

import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import com.google.common.collect.Sets;

/**
 * @author dmulloy2
 */

public class User extends Permissible
{
	protected String groupName;
	protected Set<String> subGroupNames;

	protected Group group;
	protected Set<Group> subGroups;

	protected World world;
	protected PermissionAttachment attachment;

	// UUID Stuff
	protected String lastKnownBy;
	protected String uniqueId;

	public User(SwornPermissions plugin, String name)
	{
		super(plugin, name);
		this.group = null;
		this.groupName = null;
		this.subGroups = Sets.newHashSet();
		this.subGroupNames = Sets.newHashSet();
	}

	public User(SwornPermissions plugin, Player player)
	{
		this(plugin, player.getName());
		this.world = player.getWorld();
	}

	public User(SwornPermissions plugin, String name, MemorySection section)
	{
		this(plugin, name);
		this.loadFromDisk(section);
	}

	public User(SwornPermissions plugin, Player player, MemorySection section)
	{
		this(plugin, player);
		this.loadFromDisk(section);
	}

	// ---- Memory Management

	@Override
	public void loadFromDisk(MemorySection section)
	{
		super.loadFromDisk(section);
		this.groupName = section.getString("group");
		this.subGroupNames = new HashSet<String>(section.getStringList("subgroups"));
		this.lastKnownBy = section.getString("lastKnownBy");
		this.uniqueId = section.getName();
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> ret = new HashMap<String, Object>();

		ret.put("group", groupName);
		ret.put("subgroups", new ArrayList<String>(subGroupNames));
		ret.put("permissions", new ArrayList<String>(permissionNodes));
		ret.put("options", options);
		ret.put("lastKnownBy", lastKnownBy);

		return ret;
	}

	@Override
	public boolean shouldBeSaved()
	{
		return ! group.equals(plugin.getPermissionHandler().getDefaultGroup(world)) || ! permissionNodes.isEmpty()
				|| ! subGroupNames.isEmpty() || ! options.isEmpty();
	}

	@Override
	public String getSaveName()
	{
		return uniqueId;
	}

	private static Field attachmentPermissions;

	static
	{
		try
		{
			attachmentPermissions = PermissionAttachment.class.getDeclaredField("permissions");
			attachmentPermissions.setAccessible(true);
		} catch (Throwable ex) { }
	}

	// ---- Permission Handling

	public void updatePermissions()
	{
		updatePermissions(false);
	}

	public void updatePermissions(boolean force)
	{
		// Always keep UUID stuff up-to-date
		updateUniqueID(getPlayer());

		World oldWorld = world;
		World newWorld = getPlayer().getWorld();

		boolean updatePermissions = false;

		if (oldWorld == null || force || ! plugin.getDataHandler().areGroupsLinked(oldWorld, newWorld))
		{
			this.group = null;
			this.subGroups = new HashSet<Group>();

			// Default group
			if (groupName == null || groupName.isEmpty())
			{
				this.group = plugin.getPermissionHandler().getDefaultGroup(newWorld);
				this.groupName = group.getName();
			}

			// Update group
			this.group = plugin.getPermissionHandler().getGroup(newWorld, groupName);
			if (group == null)
			{
				this.group = plugin.getPermissionHandler().getDefaultGroup(newWorld);
				this.groupName = group.getName();
			}

			for (String subGroupName : subGroupNames)
			{
				Group subGroup = plugin.getPermissionHandler().getGroup(newWorld, subGroupName);
				if (group != null)
					subGroups.add(subGroup);
			}

			updatePermissions = true;
		}

		// Update world variable
		this.world = newWorld;

		// Do we need to continue?
		if (! force && ! updatePermissions)
			return;

		// Update prefix
		this.prefix = findPrefix();

		// Update permission map
		updatePermissionMap();

		// Apply our permissions

		try
		{
			// Reset attachment
			resetAttachment();

			// Apply the new permissions
			attachmentPermissions.set(attachment, new LinkedHashMap<String, Boolean>(permissions));

			// Recalculate permissions
			getPlayer().recalculatePermissions();
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "applying permissions for " + name));
		}
	}

	// ---- Player Management

	public final Player getPlayer()
	{
		return Util.matchPlayer(name);
	}

	public final boolean isOnline()
	{
		Player player = getPlayer();
		return player != null && player.isOnline();
	}

	public final void onQuit()
	{
		removeAttachment();
		plugin.getPermissionHandler().removeUser(getName());
	}

	// ---- UUID Management

	public void updateUniqueID(Player player)
	{
		if (player == null)
			return;

		this.uniqueId = player.getUniqueId().toString();
		this.lastKnownBy = player.getName();
	}

	public String getUniqueId()
	{
		return uniqueId;
	}

	public void setUniqueId(UUID uuid)
	{
		this.uniqueId = uuid.toString();
	}

	public UUID getUUID()
	{
		return UUID.fromString(uniqueId);
	}

	public String getLastKnownBy()
	{
		return lastKnownBy;
	}

	public void setLastKnownBy(String lastKnownBy)
	{
		this.lastKnownBy = lastKnownBy;
	}

	// ---- Attachment

	public final void resetAttachment()
	{
		removeAttachment();
		attachment = getPlayer().addAttachment(plugin);
	}

	public final void removeAttachment()
	{
		if (attachment != null)
		{
			attachment.remove();
			attachment = null;
		}
	}

	// ---- Vault Compat

	public final List<String> getGroups()
	{
		List<String> ret = new ArrayList<String>();
		ret.add(groupName);
		ret.addAll(subGroupNames);

		if (ret.isEmpty())
			ret.add(plugin.getPermissionHandler().getDefaultGroup(world).getName());

		return ret;
	}

	public final boolean isInGroup(String groupName)
	{
		return groupName.equalsIgnoreCase(groupName);
	}

	public final boolean isInSubGroup(String groupName)
	{
		for (String subGroup : subGroupNames)
		{
			if (subGroup.equalsIgnoreCase(groupName))
				return true;
		}

		return false;
	}

	// ---- Getters and Setters

	public String getDisplayName()
	{
		if (options.containsKey("name"))
			return (String) options.get("name");

		return getPlayer().getName();
	}

	public void setDisplayName(String name)
	{
		options.put("name", name);
	}

	public Group getGroup()
	{
		return group;
	}

	public void setGroup(Group group)
	{
		this.group = group;
		this.groupName = group.getName();
	}

	public String getGroupName()
	{
		return groupName;
	}

	/**
	 * @deprecated For conversion use ONLY
	 */
	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public Set<Group> getSubGroups()
	{
		return subGroups;
	}

	public Set<String> getSubGroupNames()
	{
		return subGroupNames;
	}

	/**
	 * @deprecated For conversion use ONLY
	 */
	public void setSubGroupNames(Set<String> subGroupNames)
	{
		this.subGroupNames = subGroupNames;
	}

	@Override
	public Set<String> getAllPermissionNodes()
	{
		// Add the nodes in reverse order... Seems to work better
		Set<String> ret = new HashSet<String>();

		// Add subgroup nodes first
		for (Group subGroup : subGroups)
		{
			ret.addAll(subGroup.getAllPermissionNodes());
		}

		// Then add main group nodes
		ret.addAll(group.getAllPermissionNodes());

		// Add User-specific permissions last
		ret.addAll(permissionNodes);

		return ret;
	}

	public final void addSubGroup(Group subGroup)
	{
		subGroups.add(subGroup);
		subGroupNames.add(subGroup.getName());
	}

	public final void removeSubGroup(String groupName)
	{
		subGroupNames.remove(groupName);

		for (Group subGroup : subGroups)
		{
			if (subGroup.getName().equalsIgnoreCase(groupName))
				subGroups.remove(subGroup);
		}
	}

	public final void removeSubGroup(Group group)
	{
		removeSubGroup(group.getName());
	}

	@Override
	public final String getPrefix()
	{
		if (prefix.isEmpty())
			prefix = findPrefix(); // Prefix is transient

		return prefix;
	}

	public final void resetPrefix()
	{
		options.remove("prefix");
		this.prefix = findPrefix();
	}

	private final String findPrefix()
	{
		if (options.containsKey("prefix"))
			return (String) options.get("prefix");

		// Main group
		if (! group.getPrefix().isEmpty())
			return group.getPrefix();

		// Sub groups
		for (Group subGroup : subGroups)
		{
			if (! subGroup.getPrefix().isEmpty())
				return subGroup.getPrefix();
		}

		return "";
	}

	public final void resetSuffix()
	{
		options.remove("suffix");
		this.suffix = "";
	}

	// ---- Generic Methods

	@Override
	public String toString()
	{
		return "User { name = " + getName() + ", world = " + world.getName() + " }";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof User)
		{
			User that = (User) obj;
			if (! this.getName().equals(that.getName()))
				return false;

			return this.world.equals(that.world);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 87;
		hash *= getName().hashCode();
		hash *= world.hashCode();
		return hash;
	}
}