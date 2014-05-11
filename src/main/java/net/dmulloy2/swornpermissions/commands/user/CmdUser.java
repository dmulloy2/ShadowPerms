/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.commands.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.StringJoiner;
import net.dmulloy2.swornpermissions.util.FormatUtil;

import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

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
		this.usesPrefix = true;

		this.registerSubCommands();
	}

	private final void registerSubCommands()
	{
		this.subCommands = Lists.newArrayList();
		this.hasSubCommands = true;

		subCommands.add(new CmdAddPermission(plugin));
		subCommands.add(new CmdAddSubgroup(plugin));
		subCommands.add(new CmdHasGroup(plugin));
		subCommands.add(new CmdHasOption(plugin));
		subCommands.add(new CmdHasPermission(plugin));
		subCommands.add(new CmdListPermissions(plugin));
		subCommands.add(new CmdRemovePermission(plugin));
		subCommands.add(new CmdRemoveSubgroup(plugin));
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

		List<String> argsList = new ArrayList<String>();

		String action = args[1];
		String name = args[2];
		for (int i = 3; i < args.length; i++)
			argsList.add(args[i]);

		for (UserCommand command : subCommands)
		{
			if (command.getAction().equalsIgnoreCase(action))
			{
				if (name.equalsIgnoreCase(command.getName()) || command.getAliases().contains(name.toLowerCase()))
				{
					command.execute(sender, user, world, argsList.toArray(new String[0]));
					return;
				}
			}
		}

		err("Invalid arguments! Try &c/swornperms help&4!");
	}

	@Override
	public List<String> getSubCommandHelp(CommandSender sender)
	{
		List<String> ret = new ArrayList<String>();
		for (UserCommand command : subCommands)
		{
			if (plugin.getPermissionHandler().hasPermission(sender, command.getPermission()))
				ret.add(command.getUsageTemplate(true));
		}

		return ret;
	}

	private final void printUserInfo(User user)
	{
		sendMessage("&3====[ &e{0} &3]====", user.getName());
		sendMessage("Group: &b{0}", user.getGroup().getName());

		Set<String> subGroups = user.getSubGroupNames();
		if (subGroups.size() > 0)
		{
			sendMessage("Sub Groups: &b{0}", FormatUtil.join("&e, &b", subGroups.toArray(new String[0])));
		}

		Set<String> permissions = user.getPermissionNodes();
		if (permissions.size() > 0)
		{
			sendMessage("Permissions: {0}", new StringJoiner("&b, &e").appendAll(permissions.toArray(new String[0])));
		}

		Map<String, Object> options = user.getOptions();
		if (options.size() > 0)
		{
			sendMessage("Options:");
			for (Entry<String, Object> entry : options.entrySet())
			{
				sendMessage("  &e{0}: &b{1}", entry.getKey(), entry.getValue());
			}
		}
	}
}