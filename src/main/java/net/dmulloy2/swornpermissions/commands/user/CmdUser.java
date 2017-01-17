/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.User;
import net.dmulloy2.types.StringJoiner;

import org.bukkit.World;

/**
 * @author dmulloy2
 */

public class CmdUser extends SwornPermissionsCommand
{
	private List<UserCommand> subCommands;

	public CmdUser(SwornPermissions plugin)
	{
		super(plugin);
		this.name = "user";
		this.requiredArgs.add("user");
		this.optionalArgs.add("action");
		this.optionalArgs.add("args");
		this.optionalArgs.add("world");
		this.description = "Modify a user's permissions";
		this.hasSubCommands = true;
		this.usesPrefix = true;

		this.registerSubCommands();
	}

	private final void registerSubCommands()
	{
		this.subCommands = new ArrayList<UserCommand>();

		subCommands.add(new CmdAddPermission(plugin));
		subCommands.add(new CmdAddSubgroup(plugin));
		subCommands.add(new CmdAddTemp(plugin));
		subCommands.add(new CmdHasGroup(plugin));
		subCommands.add(new CmdHasOption(plugin));
		subCommands.add(new CmdHasPermission(plugin));
		subCommands.add(new CmdListPermissions(plugin));
		subCommands.add(new CmdRemovePermission(plugin));
		subCommands.add(new CmdRemoveSubgroup(plugin));
		subCommands.add(new CmdRemoveTemp(plugin));
		subCommands.add(new CmdReset(plugin));
		subCommands.add(new CmdSetGroup(plugin));
		subCommands.add(new CmdSetOption(plugin));
		subCommands.add(new CmdSetPrefix(plugin));
		subCommands.add(new CmdSetSuffix(plugin));
	}

	@Override
	public void perform()
	{
		World world = getWorld();
		User user = getUser(0, world, true);
		if (user == null)
			return;

		if (args.length == 1)
		{
			printUserInfo(user);
			return;
		}

		String action = "";
		String name = "";
		List<String> argsList = new ArrayList<>();

		if (args.length == 2)
		{
			action = "";
			name = args[1];
			for (int i = 2; i < args.length; i++)
				argsList.add(args[i]);
		}
		else
		{
			action = args[1];
			name = args[2];
			for (int i = 3; i < args.length; i++)
				argsList.add(args[i]);
		}

		for (UserCommand command : subCommands)
		{
			if (command.getAction().equalsIgnoreCase(action))
			{
				if (command.getName().equalsIgnoreCase(name) || command.getAliases().contains(name.toLowerCase()))
				{
					command.execute(sender, user, world, argsList.toArray(new String[0]));
					return;
				}
			}
		}

		err("Invalid arguments! Try &c/swornperms help&4!");
	}

	@Override
	public List<UserCommand> getSubCommands()
	{
		return subCommands;
	}

	private final void printUserInfo(User user)
	{
		if (! hasPermission(Permission.USER_VIEW_INFO))
		{
			err("You do not have permission to perform this command!");
			return;
		}

		sendMessage("&3---- &e{0} &3----", user.getName());
		sendMessage("&bGroup&e: {0}", user.getGroupName());

		List<String> subGroups = user.getSubGroupNames();
		if (! subGroups.isEmpty())
		{
			sendMessage("&bSub Groups&e: {0}", new StringJoiner("&b, &e").appendAll(subGroups));
		}

		List<String> permissions = user.getPermissionNodes();
		if (! permissions.isEmpty())
		{
			sendMessage("&bPermissions&e: {0}", new StringJoiner("&b, &e").appendAll(permissions));
		}

		Map<String, Object> options = user.getOptions();
		if (options.size() > 0)
		{
			sendMessage("&bOptions&e:");
			for (Entry<String, Object> entry : options.entrySet())
			{
				sendMessage("  &b{0}&e: \"&f{1}&e\"", entry.getKey(), entry.getValue());
			}
		}
	}
}