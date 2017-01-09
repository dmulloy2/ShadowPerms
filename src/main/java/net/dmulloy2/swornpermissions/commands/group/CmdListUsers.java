/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.group;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.types.Group;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.User;
import net.dmulloy2.util.ListUtil;

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
		final String groupName = group.getName().toLowerCase();

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
				final List<String> users = new ArrayList<>();
				for (User user : plugin.getPermissionHandler().getAllUsers(world))
				{
					String userGroup = user.getGroupName() != null ? user.getGroupName().toLowerCase() : "";
					if (userGroup.equals(groupName) || ListUtil.containsIgnoreCase(user.getSubGroupNames(), groupName))
					{
						String name = firstNonNull(user.getName(), user.getLastKnownBy());
						if (name != null)
							users.add(name);
					}
				}

				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						plugin.getLogHandler().debug("Returning to sync");

						if (users.isEmpty())
						{
							err(sender, "No users found in this group!");
							return;
						}

						sendMessage(sender, "&3---- &eUsers in {0} &3----", WordUtils.capitalize(group.getName()));

						for (String name : users)
						{
							sendMessage(sender, "&b - &e{0}", name);
						}
					}
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	}

	private static <T> T firstNonNull(T first, T second)
	{
		return first != null ? first : second;
	}
}