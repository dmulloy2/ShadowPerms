/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.types;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.data.backend.SQLBackend;
import net.dmulloy2.types.MyMaterial;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;

/**
 * @author dmulloy2
 */

public class User extends Permissible implements Reloadable
{
	private Group group;
	private String groupName;

	private List<Group> subGroups;
	private List<String> subGroupNames;

	private PermissionAttachment attachment;

	private String uniqueId;
	private String lastKnownBy;

	private Player player;

	// Base constructor
	private User(ShadowPerms plugin, String name, String uniqueId, String world)
	{
		super(plugin, name, world);
		this.uniqueId = uniqueId;
		this.subGroups = new ArrayList<>();
		this.subGroupNames = new ArrayList<>();
	}

	@Deprecated
	public User(ShadowPerms plugin, String name, String world)
	{
		this(plugin, name, null, world);
	}

	public User(ShadowPerms plugin, OfflinePlayer player, String world)
	{
		this(plugin, player.getName(), player.getUniqueId().toString(), world);

		if (player instanceof Player)
			this.player = (Player) player;
	}

	public User(ShadowPerms plugin, OfflinePlayer player, String world, MemorySection section)
	{
		this(plugin, player, world);
		this.loadFromDisk(section);
	}

	public User(ShadowPerms plugin, OfflinePlayer player, String world, ResultSet results) throws SQLException
	{
		this(plugin, player, world);
		this.loadFromSQL(results);
	}

	// ---- I/O

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
	public void loadFromSQL(ResultSet results) throws SQLException
	{
		super.loadFromSQL(results);
		this.groupName = results.getString("groupName");
		this.subGroupNames = SQLBackend.fromArrayString(results.getString("subGroups"));
		this.lastKnownBy = results.getString("lastKnownBy");
		this.uniqueId = results.getString("identifier");
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> ret = new LinkedHashMap<>();

		ret.put("lastKnownBy", lastKnownBy);
		ret.put("group", groupName);
		ret.put("subgroups", subGroupNames);
		ret.put("permissions", permissionNodes);

		if (! options.isEmpty())
			ret.put("options", options);

		return ret;
	}

	@Override
	public boolean shouldBeSaved()
	{
		return ! isInDefault() || ! permissionNodes.isEmpty() || ! subGroupNames.isEmpty() || ! options.isEmpty();
	}

	private boolean isInDefault()
	{
		try
		{
			Group defaultGroup = plugin.getPermissionHandler().getDefaultGroup(worldName);
			return defaultGroup != null && defaultGroup.equals(getGroup());
		} catch (Throwable ex) { }
		return false;
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
		} catch (Throwable ignored) { }
	}

	// ---- Permission Handling

	@Override
	public final void updatePermissions(boolean force)
	{
		updatePermissions(getPlayer(), force);
	}

	public final void updatePermissions(Player player, boolean force)
	{
		if (player == null)
		{
			plugin.getLogHandler().debug(Level.WARNING, "{0} does not have a valid player instance!", name);
			return;
		}

		World oldWorld = getWorld();
		World newWorld = player.getWorld();
		this.worldName = newWorld.getName();

		// Specific conditions
		if (force || oldWorld == null || group == null || ! plugin.getMirrorHandler().areGroupsLinked(oldWorld, newWorld))
		{
			// Update group
			this.group = null;
			this.group = getGroup();
			if (group == null)
			{
				plugin.getLogHandler().log(Level.WARNING, "Failed to find group {0} for {1}. Using default group.", groupName, name);

				// Default group
				this.group = plugin.getPermissionHandler().getDefaultGroup(newWorld);
				if (group == null)
				{
					plugin.getLogHandler().log(Level.SEVERE, "Failed to find a default group! {0} will not have any perms!", name);
					return;
				}
			}

			this.groupName = group.getName();

			// Update subgroups
			this.subGroups = new ArrayList<>();
			this.subGroups = getSubGroups();

			// Reset prefix
			this.prefix = "";
			this.prefix = findPrefix();

			updateDisplayName();

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
	}

	@Override
	protected final Set<String> sortPermissions()
	{
		Map<String, Boolean> permissions = new LinkedHashMap<>();

		// Add default permissions first
		permissions.putAll(getDefaultPermissions(getPlayer()));

		// Then subgroup permissions
		permissions.putAll(getSubgroupPermissions());

		// Then add main group permissions
		permissions.putAll(getGroupPermissions());

		// Finally user-specific nodes
		List<String> userNodes = getPermissionNodes();
		userNodes = getAllChildren(userNodes);
		userNodes = getMatchingNodes(userNodes);

		for (String node : userNodes)
		{
			boolean value = ! node.startsWith("-");
			permissions.put(value ? node : node.substring(1), value);
		}

		List<String> ret = new ArrayList<>();

		for (Entry<String, Boolean> entry : permissions.entrySet())
		{
			String node = entry.getKey();
			boolean value = entry.getValue();
			ret.add(value ? node : "-" + node);
		}

		// Sort and return
		return sort(ret);
	}

	private Map<String, Boolean> getDefaultPermissions(Player player)
	{
		Map<String, Boolean> defaultPermissions = new HashMap<>();

		Set<Permission> defaults = plugin.getServer().getPluginManager().getDefaultPermissions(player.isOp());
		for (Permission permission : defaults)
			defaultPermissions.put(permission.getName(), true);

		return defaultPermissions;
	}

	@Override
	public final boolean hasPermission(String permission)
	{
		if (isOnline())
		{
			Player player = getPlayer();
			return hasPermission(player, permission);
		}

		return super.hasPermission(permission);
	}

	private boolean hasPermission(Player base, String node)
	{
		String permCheck = node;
		int index;
		while (true)
		{
			if (base.isPermissionSet(permCheck))
				return base.hasPermission(permCheck);

			index = node.lastIndexOf('.');
			if (index < 1)
				return base.hasPermission("*");

			node = node.substring(0, index);
			permCheck = node + ".*";
		}
	}

	public final void reset()
	{
		this.group = null;
		this.groupName = null;
		this.subGroups.clear();
		this.subGroupNames.clear();
		this.permissions.clear();
		this.permissionNodes.clear();
		this.sortedPermissions.clear();
		this.options.clear();

		updatePermissions(true);
	}

	// ---- Player Management

	public final Player getPlayer()
	{
		if (player == null)
			this.player = Util.matchPlayer(uniqueId);
		return player;
	}

	public final boolean isOnline()
	{
		Player player = getPlayer();
		return player != null && plugin.getMirrorHandler().areUsersLinked(getWorld(), player.getWorld());
	}

	public void logout()
	{
		removeAttachment();
		this.player = null;
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

	/**
	 * @deprecated Conversion use ONLY!
	 */
	@Deprecated
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

	/**
	 * @deprecated Conversion use ONLY!
	 */
	@Deprecated
	public void setLastKnownBy(String lastKnownBy)
	{
		this.lastKnownBy = lastKnownBy;
	}

	// ---- Attachment

	private final void resetAttachment()
	{
		removeAttachment();
		attachment = getPlayer().addAttachment(plugin);
	}

	private void removeAttachment()
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
		List<String> ret = new ArrayList<>();
		ret.add(getGroupName());
		ret.addAll(subGroupNames);

		if (ret.isEmpty())
		{
			Group group = getGroup();
			ret.add(group.getName());
		}

		return ret;
	}

	public final boolean isInGroup(String group)
	{
		return groupName.equalsIgnoreCase(group);
	}

	public final boolean isInSubGroup(String group)
	{
		for (String subGroup : subGroupNames)
		{
			if (subGroup.equalsIgnoreCase(group))
				return true;
		}

		return false;
	}

	public final boolean isGroupApplicable(String groupName)
	{
		// Check their actual groups first
		if (isInGroup(groupName) || isInSubGroup(groupName))
			return true;

		// Get all applicable groups
		for (Group group : getApplicableGroups())
		{
			if (group.getName().equalsIgnoreCase(groupName))
				return true;
		}

		return false;
	}

	private Collection<Group> getApplicableGroups()
	{
		Set<Group> ret = new HashSet<>(group.getAllParentGroups());
		for (Group group : subGroups)
			ret.addAll(group.getAllParentGroups());

		return ret;
	}

	// ---- Getters and Setters

	public String getDisplayName()
	{
		if (options.containsKey("name"))
			return (String) options.get("name");

		Player player = getPlayer();
		return player == null ? lastKnownBy : player.getName();
	}

	public void setDisplayName(String name)
	{
		if (name != null && name.equals(getName()))
			name = null;

		setOption("name", name);
		updateDisplayName();
	}

	private void updateDisplayName()
	{
		if (! plugin.getChatHandler().isSetDisplay()) return;

		Player player = getPlayer();
		if (player == null) return;

		player.setDisplayName(getDisplayName());
	}

	public Group getGroup()
	{
		if (group == null)
		{
			// Default group
			if (groupName == null || groupName.isEmpty())
				group = plugin.getPermissionHandler().getDefaultGroup(worldName);
			else
				group = plugin.getPermissionHandler().getGroup(worldName, groupName);
		}

		return group;
	}

	public void setGroup(Group group)
	{
		this.group = group;
		this.groupName = group.getName();
	}

	public String getGroupName()
	{
		if (groupName == null && getGroup() != null)
			groupName = getGroup().getName();

		return groupName;
	}

	public List<Group> getSubGroups()
	{
		if (subGroups == null)
			subGroups = new ArrayList<>();

		if (subGroups.isEmpty() && ! subGroupNames.isEmpty())
		{
			for (String subGroupName : subGroupNames)
			{
				Group subGroup = plugin.getPermissionHandler().getGroup(getWorld(), subGroupName);
				if (subGroup != null)
					subGroups.add(subGroup);
			}
		}

		return subGroups;
	}

	public List<String> getSubGroupNames()
	{
		return subGroupNames;
	}

	@Override
	public List<String> getAllPermissionNodes()
	{
		List<String> ret = new ArrayList<>();

		// Add subgroup nodes
		ret.addAll(getSubgroupNodes());

		// Add group nodes
		ret.addAll(getGroup().getAllPermissionNodes());

		// Add user-specific nodes
		ret.addAll(getPermissionNodes());

		return ret;
	}

	// Subgroup nodes
	private List<String> getSubgroupNodes()
	{
		List<String> ret = new ArrayList<>();

		for (Group subGroup : getSubGroups())
			ret.addAll(subGroup.getAllPermissionNodes());

		return ret;
	}

	// Main group permissions
	private Map<String, Boolean> getGroupPermissions()
	{
		return getGroup().getPermissions();
	}

	// Subgroup permissions
	private Map<String, Boolean> getSubgroupPermissions()
	{
		Map<String, Boolean> ret = new LinkedHashMap<>();

		for (Group subGroup : getSubGroups())
			ret.putAll(subGroup.getPermissions());

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
		subGroups.removeIf(subGroup -> subGroup.getName().equalsIgnoreCase(groupName));
	}

	@Override
	public final String getPrefix()
	{
		if (prefix.isEmpty())
			prefix = findPrefix(); // Prefix is transient

		return prefix;
	}

	@Override
	public final String findPrefix()
	{
		if (options.containsKey("prefix"))
			return (String) options.get("prefix");

		// Main group
		Group group = getGroup();
		if (group != null && ! group.getPrefix().isEmpty())
			return group.getPrefix();

		// Sub groups
		for (Group subGroup : getSubGroups())
		{
			if (! subGroup.getPrefix().isEmpty())
				return subGroup.getPrefix();
		}

		return "";
	}

	@Override
	public boolean hasOption(String key)
	{
		Group group = getGroup();
		return super.hasOption(key) || (group != null && group.hasOption(key));
	}

	@Override
	public Object getOption(String key)
	{
		Group group = getGroup();
		return super.hasOption(key) ? super.getOption(key) : (group != null ? group.getOption(key) : null);
	}

	// ---- AntiItem

	public final boolean canUse(String regexPrefix, MyMaterial material)
	{
		if (! regexPrefix.startsWith("-"))
			regexPrefix = "-" + regexPrefix;

		for (String permission : sortedPermissions)
		{
			// Data-specific
			if (permission.contains(":"))
			{
				String node = permission.substring(0, permission.lastIndexOf(":"));
				if (node.matches(regexPrefix + material.getMaterial().name()))
				{
					String data = permission.substring(permission.lastIndexOf(":") + 1);
					if (data.matches(material.getData() + ""))
						return false;
				}
			}
			else
			{
				if (permission.matches(regexPrefix + material.getMaterial().name()))
					return false;
			}
		}

		return true;
	}

	public final boolean matches(String identifier)
	{
		return identifier.equalsIgnoreCase(uniqueId) || identifier.equalsIgnoreCase(name)
				|| identifier.equalsIgnoreCase(ChatColor.stripColor(getDisplayName()));
	}

	public final String describeTo(CommandSender sender)
	{
		return describeTo(sender, false);
	}

	public final String describeTo(CommandSender sender, boolean possession)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (player.getUniqueId().toString().equals(uniqueId))
				return "You" + (possession ? "r" : "");
		}

		return getName() + (possession ? "'s" : "");
	}

	// ---- Generic Methods

	@Override
	public String toString()
	{
		return "User[name=" + name + ", world=" + worldName + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof User)
		{
			User that = (User) obj;
			return this.uniqueId.equals(that.uniqueId) && this.worldName.equals(that.worldName);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(uniqueId, worldName);
	}

	@Override
	public void reload()
	{
		plugin.getDataHandler().reloadUser(this);
		updatePermissions(true);
	}
}