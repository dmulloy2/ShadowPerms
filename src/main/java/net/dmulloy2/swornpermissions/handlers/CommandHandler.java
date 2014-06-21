/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.commands.CmdHelp;
import net.dmulloy2.swornpermissions.commands.SwornPermissionsCommand;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

/**
 * @author dmulloy2
 */

public class CommandHandler implements CommandExecutor
{
	private String commandPrefix;
	private List<SwornPermissionsCommand> registeredPrefixedCommands;
	private List<SwornPermissionsCommand> registeredCommands;

	private final SwornPermissions plugin;
	public CommandHandler(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.registeredCommands = new ArrayList<SwornPermissionsCommand>();
	}

	public void registerCommand(SwornPermissionsCommand command)
	{
		PluginCommand pluginCommand = plugin.getCommand(command.getName());
		if (pluginCommand != null)
		{
			pluginCommand.setExecutor(command);
			registeredCommands.add(command);
		}
		else
		{
			plugin.getLogHandler().log("Entry for command {0} is missing in plugin.yml", command.getName());
		}
	}

	public void registerPrefixedCommand(SwornPermissionsCommand command)
	{
		if (commandPrefix != null)
			registeredPrefixedCommands.add(command);
	}

	public List<SwornPermissionsCommand> getRegisteredCommands()
	{
		return registeredCommands;
	}

	public List<SwornPermissionsCommand> getRegisteredPrefixedCommands()
	{
		return registeredPrefixedCommands;
	}

	public List<SwornPermissionsCommand> getAllRegisteredCommands()
	{
		List<SwornPermissionsCommand> ret = new ArrayList<SwornPermissionsCommand>();
		ret.addAll(registeredPrefixedCommands);
		ret.addAll(registeredCommands);
		return ret;
	}

	public String getCommandPrefix()
	{
		return commandPrefix;
	}

	public void setCommandPrefix(String commandPrefix)
	{
		this.commandPrefix = commandPrefix;
		this.registeredPrefixedCommands = new ArrayList<SwornPermissionsCommand>();

		plugin.getCommand(commandPrefix).setExecutor(this);
	}

	public boolean usesCommandPrefix()
	{
		return commandPrefix != null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> argsList = new ArrayList<String>();

		if (args.length > 0)
		{
			String commandName = args[0];
			for (int i = 1; i < args.length; i++)
				argsList.add(args[i]);

			for (SwornPermissionsCommand command : registeredPrefixedCommands)
			{
				if (commandName.equalsIgnoreCase(command.getName()) || command.getAliases().contains(commandName.toLowerCase()))
				{
					command.execute(sender, argsList.toArray(new String[0]));
					return true;
				}
			}

			sender.sendMessage(FormatUtil.format("&cError: &4Unknown command \"&c{0}&4\". Try &c/swornperms help&4!", commandName));
		}
		else
		{
			new CmdHelp(plugin).execute(sender, args);
		}

		return true;
	}
}