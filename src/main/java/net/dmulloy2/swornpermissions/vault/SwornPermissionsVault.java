package net.dmulloy2.swornpermissions.vault;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.Group;
import net.dmulloy2.swornpermissions.permissions.ServerGroup;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.permissions.WorldGroup;
import net.milkbowl.vault.permission.Permission;

/**
 * @author dmulloy2
 */

public class SwornPermissionsVault extends Permission
{
	private String name;
	private SwornPermissions plugin;

	public SwornPermissionsVault(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.name = plugin.getName();
	}

	@Override
	public String[] getGroups()
	{
		Set<String> ret = new HashSet<String>();

		Map<String, Map<String, WorldGroup>> groupMaps = plugin.getPermissionHandler().getWorldGroups();
		for (Entry<String, Map<String, WorldGroup>> entry : groupMaps.entrySet())
		{
			ret.addAll(entry.getValue().keySet());
		}

		Map<String, ServerGroup> serverGroups = plugin.getPermissionHandler().getServerGroups();
		ret.addAll(serverGroups.keySet());

		return ret.toArray(new String[0]);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String[] getPlayerGroups(String world, String player)
	{
		User user = plugin.getPermissionHandler().getUser(world, player);
		if (user == null)
			return null;

		return user.getGroups().toArray(new String[0]);
	}

	@Override
	public String getPrimaryGroup(String world, String player)
	{
		return getPlayerGroups(world, player)[0];
	}

	@Override
	public boolean groupAdd(String world, String groupName, String permission)
	{
		Group group = plugin.getPermissionHandler().getGroup(world, groupName);
		if (group == null)
			return false;

		group.addPermission(permission);
		group.updatePermissions(true);
		return true;
	}

	@Override
	public boolean groupHas(String world, String groupName, String permission)
	{
		Group group = plugin.getPermissionHandler().getGroup(world, groupName);
		if (group == null)
			return false;

		return group.hasPermission(permission);
	}

	@Override
	public boolean groupRemove(String world, String groupName, String permission)
	{
		Group group = plugin.getPermissionHandler().getGroup(world, groupName);
		if (group == null)
			return false;

		group.removePermission(permission);
		group.updatePermissions(true);
		return true;
	}

	@Override
	public boolean hasGroupSupport()
	{
		return true;
	}

	@Override
	public boolean hasSuperPermsCompat()
	{
		return true;
	}

	@Override
	public boolean isEnabled()
	{
		return plugin.isEnabled();
	}

	@Override
	public boolean playerAdd(String world, String player, String permission)
	{
		User user = plugin.getPermissionHandler().getUser(world, player);
		if (user == null)
			return false;

		user.addPermission(permission);
		user.updatePermissions(true);
		return true;
	}

	@Override
	public boolean playerAddGroup(String world, String player, String groupName)
	{
		User user = plugin.getPermissionHandler().getUser(world, player);
		if (user == null)
			return false;

		Group group = plugin.getPermissionHandler().getGroup(world, groupName);
		if (group == null)
			return false;

		user.addSubGroup(group);
		user.updatePermissions(true);
		return true;
	}

	@Override
	public boolean playerHas(String world, String player, String permission)
	{
		User user = plugin.getPermissionHandler().getUser(world, player);
		if (user == null)
			return false;

		return user.hasPermission(permission);
	}

	@Override
	public boolean playerInGroup(String world, String player, String group)
	{
		User user = plugin.getPermissionHandler().getUser(world, player);
		if (user == null)
			return false;

		return user.isInGroup(group);
	}

	@Override
	public boolean playerRemove(String world, String player, String permission)
	{
		User user = plugin.getPermissionHandler().getUser(world, player);
		if (user == null)
			return false;

		user.removePermission(permission);
		user.updatePermissions(true);
		return true;
	}

	@Override
	public boolean playerRemoveGroup(String world, String player, String group)
	{
		if (! playerInGroup(world, player, group))
			return false;

		User user = plugin.getPermissionHandler().getUser(world, player);
		if (user == null)
			return false;

		user.removeSubGroup(group);
		user.updatePermissions(true);
		return true;
	}
}