/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.permissions;

import net.dmulloy2.swornpermissions.SwornPermissions;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.MemorySection;

/**
 * Used for manipulating OfflinePlayers' permissions
 * 
 * @author dmulloy2
 */

public class OfflineUser extends User
{
	private OfflineUser(SwornPermissions plugin, String name)
	{
		super(plugin, name);
	}

	public OfflineUser(SwornPermissions plugin, OfflinePlayer player)
	{
		this(plugin, player.getName());
	}

	public OfflineUser(SwornPermissions plugin, String name, MemorySection section)
	{
		this(plugin, name);
		this.loadFromDisk(section);
	}

	public OfflineUser(SwornPermissions plugin, OfflinePlayer player, MemorySection section)
	{
		this(plugin, player.getName());
		this.loadFromDisk(section);
	}

	@Override
	public void updatePermissions()
	{
		// Do nothing...
	}
}