/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.Group;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.util.FormatUtil;

import org.apache.commons.lang.WordUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author dmulloy2
 */

public class CmdListUsers extends GroupCommand
{
	public CmdListUsers(SwornPermissions plugin)
	{
		super(plugin);
		this.action = "list";
		this.name = "users";
		this.description = "List users in a group";
		this.permission = Permission.GROUP_LIST_USERS;
	}

	@Override
	public void perform()
	{
		// Default group check
		if (group.equals(plugin.getPermissionHandler().getDefaultGroup(world)))
		{
			err("You cannot display users in the default group!");
			return;
		}

		// Store important references
		final Group group = this.group;
		final World world = this.world;
		final CommandSender sender = this.sender;

		sendpMessage("Building list for group: {0}", group.getName());
		plugin.getLogHandler().debug("Calculating users in group {0} in async task", group.getName());

		// Calculate async
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				// Build users list
				final List<User> users = new ArrayList<User>();
				for (User user : plugin.getPermissionHandler().getAllUsers(world))
				{
					if (user.getGroupName() == null)
						continue;

					if (user.getGroupName().equals(group.getName()) || user.getSubGroupNames().contains(group.getName()))
						users.add(user);
				}

				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						plugin.getLogHandler().debug("Returning to sync");

						if (users.isEmpty())
						{
							sender.sendMessage(FormatUtil.format("&cError: &4No users found in this group!"));
							return;
						}

						sender.sendMessage(FormatUtil.format("&3====[ &e{0} &3]====", WordUtils.capitalize(group.getName())));

						for (User user : users)
						{
							sender.sendMessage(FormatUtil.format("&b - &e{0}", user.getLastKnownBy()));
						}
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	
	}
}