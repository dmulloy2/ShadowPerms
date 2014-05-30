/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.permissions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.util.Util;

import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

/**
 * @author dmulloy2
 */

public class User extends Permissible
{
	protected String groupName;
	protected List<String> subGroupNames;

	protected Group group;
	protected List<Group> subGroups;

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
		this.subGroups = new ArrayList<Group>();
		this.subGroupNames = new ArrayList<String>();
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
		this.subGroupNames = section.getStringList("subgroups");
		this.lastKnownBy = section.getString("lastKnownBy");
		this.uniqueId = section.getName();
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> ret = new LinkedHashMap<String, Object>();

		ret.put("lastKnownBy", lastKnownBy);
		ret.put("group", groupName);
		ret.put("subgroups", subGroupNames);
		ret.put("permissions", permissionNodes);
		ret.put("options", options);

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

	@Override
	public void updatePermissions(boolean force)
	{
		// Online check
		Player player = getPlayer();
		if (player == null || ! player.isOnline())
			return;

		// Always keep UUID stuff up-to-date
		updateUniqueID(player);

		World newWorld = player.getWorld();

		boolean updatePermissions = force || world == null;

		if (updatePermissions || ! plugin.getMirrorHandler().areGroupsLinked(world, newWorld))
		{
			this.group = null;
			this.subGroups = new ArrayList<Group>();

			// Default group
			if (groupName == null || groupName.isEmpty())
			{
				this.group = plugin.getPermissionHandler().getDefaultGroup(newWorld);

				if (group == null)
				{
					plugin.getLogHandler().log(Level.SEVERE, "Failed to find a default group! {0} will not have any perms!", name);
					return;
				}

				this.groupName = group.getName();
			}

			// Update group
			this.group = plugin.getPermissionHandler().getGroup(newWorld, groupName);
			if (group == null)
			{
				this.group = plugin.getPermissionHandler().getDefaultGroup(newWorld);

				if (group == null)
				{
					plugin.getLogHandler().log(Level.SEVERE, "Failed to find a default group! {0} will not have any perms!", name);
					return;
				}

				this.groupName = group.getName();
			}

			// Update subgroups
			for (String subGroupName : subGroupNames)
			{
				Group subGroup = plugin.getPermissionHandler().getGroup(newWorld, subGroupName);
				if (group != null)
					subGroups.add(subGroup);
			}

			updatePermissions = true;
		}

		// Update world
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

	@Override
	protected final List<String> sortPermissions()
	{
		Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();

		// Add subgroup permissions first
		permissions.putAll(getSubgroupPermissions());

		// Then add main group permissions
		permissions.putAll(getGroupPermissions());

		// Finally user-specific nodes
		List<String> userPerms = sort(getPermissionNodes());

		for (String userPerm : new ArrayList<String>(userPerms))
		{
			boolean value = ! userPerm.startsWith("-");
			permissions.put(value ? userPerm : userPerm.substring(1), value);
		}

		List<String> ret = new ArrayList<String>();

		for (Entry<String, Boolean> entry : permissions.entrySet())
		{
			String node = entry.getKey();
			boolean value = entry.getValue();
			ret.add(value ? node : "-" + node);
		}

		// Sort and return
		return sort(ret);
	}

	// ---- Player Management

	public final Player getPlayer()
	{
		return Util.matchPlayer(name);
	}

	public final boolean isOnline()
	{
		Player player = getPlayer();
		if (player != null && player.isOnline())
			return plugin.getMirrorHandler().areUsersLinked(world, player.getWorld());

		return false;
	}

	public final void onQuit()
	{
		removeAttachment();
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

	// ---- Getters and Listters

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

	public List<Group> getSubGroups()
	{
		return subGroups;
	}

	public List<String> getSubGroupNames()
	{
		return subGroupNames;
	}

	/**
	 * @deprecated For conversion use ONLY
	 */
	public void setSubGroupNames(List<String> subGroupNames)
	{
		this.subGroupNames = subGroupNames;
	}

	@Override
	public List<String> getAllPermissionNodes()
	{
		List<String> ret = new ArrayList<String>();

		// Add subgroup nodes
		ret.addAll(getSubgroupNodes());

		// Add group nodes
		ret.addAll(getGroupNodes());

		// Add user-specific nodes
		ret.addAll(permissionNodes);

		return ret;
	}

	// Main group nodes
	private final List<String> getGroupNodes()
	{
		List<String> ret = new ArrayList<String>();
		ret.addAll(group.getAllPermissionNodes());
		return ret;
	}

	// Subgroup nodes
	private final List<String> getSubgroupNodes()
	{
		List<String> ret = new ArrayList<String>();

		for (Group subGroup : subGroups)
			ret.addAll(subGroup.getAllPermissionNodes());

		return ret;
	}

	// Main group permissions
	private final Map<String, Boolean> getGroupPermissions()
	{
		group.update(false);
		return group.getPermissions();
	}

	// Subgroup permissions
	private final Map<String, Boolean> getSubgroupPermissions()
	{
		Map<String, Boolean> ret = new LinkedHashMap<String, Boolean>();

		for (Group subGroup : subGroups)
		{
			subGroup.update(false);
			ret.putAll(group.getPermissions());
		}

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
		hash *= lastKnownBy.hashCode();
		hash *= world.hashCode();
		return hash;
	}
}